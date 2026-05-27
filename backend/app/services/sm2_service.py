from dataclasses import dataclass
from datetime import datetime, timedelta, timezone

from app.schemas.learning import ReviewRating

MIN_EASE_FACTOR = 1.3
DEFAULT_EASE_FACTOR = 2.5


@dataclass(frozen=True)
class SrsState:
    repetitions: int = 0
    interval_days: int = 0
    ease_factor: float = DEFAULT_EASE_FACTOR


@dataclass(frozen=True)
class SrsResult:
    repetitions: int
    interval_days: int
    ease_factor: float
    due_at: datetime
    status: str


def apply_sm2_review(
    state: SrsState,
    rating: ReviewRating,
    reviewed_at: datetime | None = None,
) -> SrsResult:
    now = _ensure_utc(reviewed_at or datetime.now(timezone.utc))
    quality = _quality_for_rating(rating)
    ease_factor = _next_ease_factor(state.ease_factor, quality)

    if rating == ReviewRating.again:
        return SrsResult(
            repetitions=0,
            interval_days=0,
            ease_factor=ease_factor,
            due_at=now + timedelta(minutes=10),
            status="learning",
        )

    repetitions = state.repetitions + 1
    interval_days = _next_interval_days(
        previous_repetitions=state.repetitions,
        previous_interval_days=state.interval_days,
        ease_factor=ease_factor,
        rating=rating,
    )
    return SrsResult(
        repetitions=repetitions,
        interval_days=interval_days,
        ease_factor=ease_factor,
        due_at=now + timedelta(days=interval_days),
        status="review" if repetitions >= 2 else "learning",
    )


def _next_interval_days(
    previous_repetitions: int,
    previous_interval_days: int,
    ease_factor: float,
    rating: ReviewRating,
) -> int:
    if rating == ReviewRating.hard:
        if previous_repetitions < 2:
            return 1
        return max(1, round(max(previous_interval_days, 1) * max(1.0, ease_factor * 0.7)))

    if previous_repetitions == 0:
        return 1
    if previous_repetitions == 1:
        base_interval = 6
    else:
        base_interval = max(1, round(max(previous_interval_days, 1) * ease_factor))

    if rating == ReviewRating.easy:
        return max(base_interval + 1, round(base_interval * 1.3))
    return base_interval


def _next_ease_factor(current_ease_factor: float, quality: int) -> float:
    updated = current_ease_factor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    return max(MIN_EASE_FACTOR, round(updated, 2))


def _quality_for_rating(rating: ReviewRating) -> int:
    return {
        ReviewRating.again: 2,
        ReviewRating.hard: 3,
        ReviewRating.good: 4,
        ReviewRating.easy: 5,
    }[rating]


def _ensure_utc(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)
