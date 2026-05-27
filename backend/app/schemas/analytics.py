from pydantic import BaseModel


class DashboardResponse(BaseModel):
    learned_words: int
    due_today: int
    streak: int
    accuracy: float
    level_estimation: str | None = None


class DailyActivityResponse(BaseModel):
    date: str
    review_count: int
    correct_count: int


class ActivityResponse(BaseModel):
    days: list[DailyActivityResponse]


class RetentionResponse(BaseModel):
    retention_rate: float
    total_reviews: int
    retained_reviews: int
