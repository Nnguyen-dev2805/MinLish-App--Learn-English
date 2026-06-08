import asyncio
from contextlib import asynccontextmanager
from collections.abc import AsyncGenerator

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app.api.v1.router import api_router
from app.core.config import get_settings
from app.core.exceptions import register_exception_handlers
from app.services.email_reminder_scheduler import EmailReminderScheduler


# Vòng đời của app FastAPI
# Nó có 2 giai đoạn:
# Trước yield -> chạy khi app bắt đầu khởi động
# Sau yield -> chạy khi app chuẩn bị tắt
# Tư duy:
# App start -> chuẩn bị tài nguyên -> app chạy -> app shutdown -> dọn tài nguyên
@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    settings = get_settings()
    scheduler_task: asyncio.Task[None] | None = None
    scheduler: EmailReminderScheduler | None = None

    if settings.email_reminder_scheduler_enabled:
        scheduler = EmailReminderScheduler(
            check_seconds=settings.email_reminder_check_seconds,
        )
        scheduler_task = asyncio.create_task(scheduler.run())

    try:
        yield
    finally:
        if scheduler is not None:
            scheduler.stop()
        if scheduler_task is not None:
            scheduler_task.cancel()
            try:
                await scheduler_task
            except asyncio.CancelledError:
                pass


def create_app() -> FastAPI:
    settings = get_settings()
    settings.resolved_static_dir.mkdir(parents=True, exist_ok=True)
    app = FastAPI(
        title=settings.app_name,
        version="0.1.0",
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        lifespan=lifespan,
    )

    register_exception_handlers(app)
    app.include_router(api_router, prefix=settings.api_v1_prefix) # nối route toàn bộ app
    app.mount(
        "/static",
        StaticFiles(directory=settings.resolved_static_dir),
        name="static",
    )

    @app.get("/health", tags=["health"])
    def root_health_check() -> dict[str, str]:
        return {
            "status": "ok",
            "app": settings.app_name,
            "environment": settings.environment,
        }

    return app


app = create_app()
