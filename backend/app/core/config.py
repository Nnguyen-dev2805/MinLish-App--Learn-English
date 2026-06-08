from functools import lru_cache
from pathlib import Path

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

# Settings không chỉ là class bình thường.
# Nó có khả năng đọc config từ:
# - giá trị default trong code
# - biến môi trường
# - file .env
class Settings(BaseSettings):
    app_name: str = "MinLish Backend"
    environment: str = "local"
    api_v1_prefix: str = "/api/v1"
    secret_key: str = Field(
        default="change-me-in-production-minlish-local-secret-key",
        validation_alias="SECRET_KEY",
    )
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    refresh_token_expire_days: int = 30
    database_url: str = Field(
        default="postgresql+psycopg://minlish:minlish@localhost:5432/minlish",
        validation_alias="DATABASE_URL",
    )
    google_client_id: str = Field(
        default="",
        validation_alias="GOOGLE_CLIENT_ID",
    )
    smtp_host: str = Field(default="", validation_alias="SMTP_HOST")
    smtp_port: int = Field(default=587, validation_alias="SMTP_PORT")
    smtp_username: str = Field(default="", validation_alias="SMTP_USERNAME")
    smtp_password: str = Field(default="", validation_alias="SMTP_PASSWORD")
    smtp_from_email: str = Field(default="", validation_alias="SMTP_FROM_EMAIL")
    smtp_from_name: str = Field(default="MinLish Vocabulary App", validation_alias="SMTP_FROM_NAME")
    email_reminder_scheduler_enabled: bool = Field(
        default=False,
        validation_alias="EMAIL_REMINDER_SCHEDULER_ENABLED",
    )
    email_reminder_check_seconds: int = Field(
        default=60,
        validation_alias="EMAIL_REMINDER_CHECK_SECONDS",
    )
    static_dir: str = Field(default="static", validation_alias="STATIC_DIR")
    backend_cors_origins: list[str] = Field(default_factory=list)

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def resolved_static_dir(self) -> Path:
        path = Path(self.static_dir)
        if path.is_absolute():
            return path
        return Path(__file__).resolve().parents[2] / path


# cache kết quả để không tạo mới nhiều lần
@lru_cache
def get_settings() -> Settings:
    return Settings()
