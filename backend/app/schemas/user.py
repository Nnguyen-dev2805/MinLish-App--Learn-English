from pydantic import BaseModel, ConfigDict, Field


class UserResponse(BaseModel):
    id: int
    email: str
    name: str | None = None
    goal: str | None = None
    level: str | None = None
    daily_new_words: int = 10

    model_config = ConfigDict(from_attributes=True)


class UpdateUserRequest(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=120)
    goal: str | None = Field(default=None, min_length=1, max_length=120)
    level: str | None = Field(default=None, min_length=1, max_length=60)
    daily_new_words: int | None = Field(default=None, ge=1, le=100)
