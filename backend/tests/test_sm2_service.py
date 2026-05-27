from datetime import datetime, timedelta, timezone

from app.schemas.learning import ReviewRating
from app.services.sm2_service import SrsState, apply_sm2_review


def test_again_resets_repetitions_and_due_after_ten_minutes() -> None:
    now = datetime(2026, 5, 27, 8, 0, tzinfo=timezone.utc)

    result = apply_sm2_review(
        SrsState(repetitions=4, interval_days=12, ease_factor=2.5),
        ReviewRating.again,
        reviewed_at=now,
    )

    assert result.repetitions == 0
    assert result.interval_days == 0
    assert result.due_at == now + timedelta(minutes=10)
    assert result.status == "learning"


def test_good_first_review_sets_one_day_interval() -> None:
    now = datetime(2026, 5, 27, 8, 0, tzinfo=timezone.utc)

    result = apply_sm2_review(SrsState(), ReviewRating.good, reviewed_at=now)

    assert result.repetitions == 1
    assert result.interval_days == 1
    assert result.due_at == now + timedelta(days=1)


def test_good_second_review_sets_six_day_interval() -> None:
    now = datetime(2026, 5, 27, 8, 0, tzinfo=timezone.utc)

    result = apply_sm2_review(
        SrsState(repetitions=1, interval_days=1, ease_factor=2.5),
        ReviewRating.good,
        reviewed_at=now,
    )

    assert result.repetitions == 2
    assert result.interval_days == 6
    assert result.due_at == now + timedelta(days=6)
    assert result.status == "review"


def test_easy_adds_interval_bonus() -> None:
    now = datetime(2026, 5, 27, 8, 0, tzinfo=timezone.utc)

    good_result = apply_sm2_review(
        SrsState(repetitions=2, interval_days=6, ease_factor=2.5),
        ReviewRating.good,
        reviewed_at=now,
    )
    easy_result = apply_sm2_review(
        SrsState(repetitions=2, interval_days=6, ease_factor=2.5),
        ReviewRating.easy,
        reviewed_at=now,
    )

    assert easy_result.interval_days > good_result.interval_days


def test_ease_factor_never_drops_below_minimum() -> None:
    now = datetime(2026, 5, 27, 8, 0, tzinfo=timezone.utc)
    state = SrsState(repetitions=10, interval_days=30, ease_factor=1.31)

    result = apply_sm2_review(state, ReviewRating.again, reviewed_at=now)

    assert result.ease_factor == 1.3
