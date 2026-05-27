from fastapi import APIRouter

from app.api.v1.analytics import router as analytics_router
from app.api.v1.auth import router as auth_router
from app.api.v1.decks import router as decks_router
from app.api.v1.health import router as health_router
from app.api.v1.learning import router as learning_router
from app.api.v1.notifications import router as notifications_router
from app.api.v1.users import router as users_router

api_router = APIRouter()
api_router.include_router(analytics_router)
api_router.include_router(auth_router)
api_router.include_router(decks_router)
api_router.include_router(health_router)
api_router.include_router(learning_router)
api_router.include_router(notifications_router)
api_router.include_router(users_router)
