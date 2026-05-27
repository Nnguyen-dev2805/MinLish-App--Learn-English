from datetime import date, datetime, timedelta, timezone

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.progress import ReviewLog, UserWordProgress
from app.models.user import User
from app.schemas.analytics import (
    ActivityResponse,
    DailyActivityResponse,
    DashboardResponse,
    RetentionResponse,
)


class AnalyticsService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def get_dashboard(self, user: User) -> DashboardResponse:
        now = self._utc_now()
        learned_words = self._count_learned_words(user)
        due_today = self._count_due_today(user, now)
        total_reviews = self._count_reviews(user)
        correct_reviews = self._count_correct_reviews(user)

        accuracy = self._percentage(correct_reviews, total_reviews)
        return DashboardResponse(
            learned_words=learned_words,
            due_today=due_today,
            streak=self._calculate_streak(user, now.date()),
            accuracy=accuracy,
            level_estimation=self._level_estimation(learned_words),
        )

    def get_activity(self, user: User) -> ActivityResponse:
        today = self._utc_now().date()
        days = [today - timedelta(days=offset) for offset in range(6, -1, -1)]
        counts = {day: {"review_count": 0, "correct_count": 0} for day in days}

        logs = self.db.scalars(
            select(ReviewLog).where(ReviewLog.user_id == user.id),
        ).all()
        for log in logs:
            log_date = self._ensure_utc(log.created_at).date()
            if log_date in counts:
                counts[log_date]["review_count"] += 1
                if log.is_correct:
                    counts[log_date]["correct_count"] += 1

        return ActivityResponse(
            days=[
                DailyActivityResponse(
                    date=day.isoformat(),
                    review_count=counts[day]["review_count"],
                    correct_count=counts[day]["correct_count"],
                )
                for day in days
            ],
        )

    def get_retention(self, user: User) -> RetentionResponse:
        total_reviews = self._count_reviews(user)
        retained_reviews = self.db.scalar(
            select(func.count(ReviewLog.id)).where(
                ReviewLog.user_id == user.id,
                ReviewLog.rating.in_(("Good", "Easy")),
            ),
        ) or 0

        return RetentionResponse(
            retention_rate=self._percentage(retained_reviews, total_reviews),
            total_reviews=total_reviews,
            retained_reviews=retained_reviews,
        )

    def _count_learned_words(self, user: User) -> int:
        return self.db.scalar(
            select(func.count(UserWordProgress.id)).where(
                UserWordProgress.user_id == user.id,
                UserWordProgress.last_reviewed_at.is_not(None),
            ),
        ) or 0

    def _count_due_today(self, user: User, now: datetime) -> int:
        return self.db.scalar(
            select(func.count(UserWordProgress.id)).where(
                UserWordProgress.user_id == user.id,
                UserWordProgress.due_at.is_not(None),
                UserWordProgress.due_at <= now,
            ),
        ) or 0

    def _count_reviews(self, user: User) -> int:
        return self.db.scalar(
            select(func.count(ReviewLog.id)).where(ReviewLog.user_id == user.id),
        ) or 0

    def _count_correct_reviews(self, user: User) -> int:
        return self.db.scalar(
            select(func.count(ReviewLog.id)).where(
                ReviewLog.user_id == user.id,
                ReviewLog.is_correct.is_(True),
            ),
        ) or 0

    def _calculate_streak(self, user: User, today: date) -> int:
        reviewed_days = {
            self._ensure_utc(created_at).date()
            for created_at in self.db.scalars(
                select(ReviewLog.created_at).where(ReviewLog.user_id == user.id),
            ).all()
        }
        if not reviewed_days:
            return 0

        current_day = today if today in reviewed_days else max(reviewed_days)
        streak = 0
        while current_day in reviewed_days:
            streak += 1
            current_day -= timedelta(days=1)
        return streak

    def _level_estimation(self, learned_words: int) -> str | None:
        if learned_words == 0:
            return None
        if learned_words < 200:
            return "Beginner"
        if learned_words < 500:
            return "Intermediate"
        return "Advanced"

    def _percentage(self, numerator: int, denominator: int) -> float:
        if denominator == 0:
            return 0.0
        return round((numerator / denominator) * 100, 2)

    def _utc_now(self) -> datetime:
        return datetime.now(timezone.utc)

    def _ensure_utc(self, value: datetime) -> datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)
        return value.astimezone(timezone.utc)
