from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field


class ReviewRating(str, Enum):
    again = "Again"
    hard = "Hard"
    good = "Good"
    easy = "Easy"


class DailyPlanResponse(BaseModel):
    daily_goal: int
    new_cards: int
    due_reviews: int
    total_available: int


class ReviewCardResponse(BaseModel):
    id: int
    deck_id: int
    word: str
    pronunciation: str | None = None
    meaning: str
    description: str | None = None
    example: str | None = None
    note: str | None = None
    image_url: str | None = None
    word_audio_url: str | None = None
    meaning_audio_url: str | None = None
    example_audio_url: str | None = None
    is_new: bool
    due_at: datetime | None = None


class ReviewCardsResponse(BaseModel):
    items: list[ReviewCardResponse]


class SubmitReviewRequest(BaseModel):
    vocabulary_item_id: int
    rating: ReviewRating
    response_ms: int | None = Field(default=None, ge=0)


class SubmitReviewResponse(BaseModel):
    vocabulary_item_id: int
    rating: ReviewRating
    is_correct: bool
    repetitions: int
    interval_days: int
    ease_factor: float
    next_due_at: datetime
