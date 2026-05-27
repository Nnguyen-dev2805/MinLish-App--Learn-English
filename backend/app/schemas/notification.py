from pydantic import BaseModel, ConfigDict, Field, field_validator


class NotificationPreferenceResponse(BaseModel):
    daily_time: str
    timezone: str
    email_enabled: bool
    push_enabled: bool

    model_config = ConfigDict(from_attributes=True)


class UpdateNotificationPreferenceRequest(BaseModel):
    daily_time: str | None = Field(default=None)
    timezone: str | None = Field(default=None, min_length=1, max_length=120)
    email_enabled: bool | None = None
    push_enabled: bool | None = None

    @field_validator("daily_time")
    @classmethod
    def validate_daily_time(cls, value: str | None) -> str | None:
        if value is None:
            return value
        parts = value.split(":")
        if len(parts) != 2 or not all(part.isdigit() for part in parts):
            raise ValueError("daily_time must be HH:mm")

        hour = int(parts[0])
        minute = int(parts[1])
        if len(parts[0]) != 2 or len(parts[1]) != 2 or hour > 23 or minute > 59:
            raise ValueError("daily_time must be HH:mm")
        return value

    @field_validator("timezone")
    @classmethod
    def validate_timezone(cls, value: str | None) -> str | None:
        if value is None:
            return value
        stripped = value.strip()
        if not stripped:
            raise ValueError("timezone must not be blank")
        return stripped
