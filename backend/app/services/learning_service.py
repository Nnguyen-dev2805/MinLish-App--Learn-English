from datetime import datetime, timezone

from fastapi import status
from sqlalchemy import exists, func, or_, select
from sqlalchemy.orm import Session

from app.core.exceptions import ApiError
from app.models.deck import Deck
from app.models.progress import ReviewLog, UserWordProgress
from app.models.user import User
from app.models.vocabulary_item import VocabularyItem
from app.schemas.learning import (
    DailyPlanResponse,
    ReviewCardResponse,
    ReviewCardsResponse,
    SubmitReviewRequest,
    SubmitReviewResponse,
)
from app.services.sm2_service import DEFAULT_EASE_FACTOR, SrsState, apply_sm2_review


class LearningService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def get_daily_plan(self, user: User) -> DailyPlanResponse:
        daily_goal = user.daily_new_words or 10
        total_available = self._count_visible_items(user)
        due_reviews = self._count_due_reviews(user)
        new_available = self._count_new_items(user)
        return DailyPlanResponse(
            daily_goal=daily_goal,
            new_cards=min(daily_goal, new_available),
            due_reviews=due_reviews,
            total_available=total_available,
        )

    def get_review_cards(
        self,
        user: User,
        deck_id: int | None = None,
        mode: str | None = None,
    ) -> ReviewCardsResponse:
        if deck_id is not None:
            requested_mode = mode or "deck_all"
            if requested_mode != "deck_all":
                raise ApiError(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Chế độ học deck không hợp lệ.",
                    code="INVALID_LEARNING_MODE",
                )
            return self._get_all_cards_in_deck(user=user, deck_id=deck_id)

        now = self._utc_now()
        due_count = self._count_due_reviews(user, now)
        limit = max(user.daily_new_words or 10, 20)
        due_limit = min(due_count, limit)

        due_rows = self.db.execute(
            select(VocabularyItem, UserWordProgress.due_at)
            .join(UserWordProgress, UserWordProgress.vocabulary_item_id == VocabularyItem.id)
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(
                UserWordProgress.user_id == user.id,
                UserWordProgress.due_at.is_not(None),
                UserWordProgress.due_at <= now,
                self._visible_deck_filter(user),
            )
            .order_by(UserWordProgress.due_at.asc(), VocabularyItem.id.asc())
            .limit(due_limit),
        ).all()

        cards = [
            self._review_card_response(item=item, is_new=False, due_at=due_at)
            for item, due_at in due_rows
        ]

        remaining = max(0, limit - len(cards))
        if remaining > 0:
            new_items = self.db.scalars(
                select(VocabularyItem)
                .join(Deck, VocabularyItem.deck_id == Deck.id)
                .where(self._visible_deck_filter(user), ~self._progress_exists_for_user(user))
                .order_by(Deck.source_unit.asc(), VocabularyItem.anki_number.asc(), VocabularyItem.id.asc())
                .limit(remaining),
            ).all()
            cards.extend(
                self._review_card_response(item=item, is_new=True, due_at=None)
                for item in new_items
            )

        return ReviewCardsResponse(items=cards)

    def _get_all_cards_in_deck(self, user: User, deck_id: int) -> ReviewCardsResponse:
        deck = self.db.scalar(
            select(Deck).where(Deck.id == deck_id, self._visible_deck_filter(user)),
        )
        if deck is None:
            raise ApiError(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Không tìm thấy deck để học.",
                code="NOT_FOUND",
            )

        rows = self.db.execute(
            select(VocabularyItem, UserWordProgress.id, UserWordProgress.due_at)
            .outerjoin(
                UserWordProgress,
                (UserWordProgress.user_id == user.id)
                & (UserWordProgress.vocabulary_item_id == VocabularyItem.id),
            )
            .where(VocabularyItem.deck_id == deck.id)
            .order_by(VocabularyItem.anki_number.asc(), VocabularyItem.id.asc()),
        ).all()

        cards = [
            self._review_card_response(
                item=item,
                is_new=progress_id is None,
                due_at=due_at,
            )
            for item, progress_id, due_at in rows
        ]
        return ReviewCardsResponse(items=cards)

    def submit_review(self, user: User, request: SubmitReviewRequest) -> SubmitReviewResponse:
        item = self._get_visible_item(user, request.vocabulary_item_id)
        now = self._utc_now()
        progress = self.db.scalar(
            select(UserWordProgress).where(
                UserWordProgress.user_id == user.id,
                UserWordProgress.vocabulary_item_id == item.id,
            ),
        )
        if progress is None:
            progress = UserWordProgress(
                user_id=user.id,
                vocabulary_item_id=item.id,
                repetitions=0,
                interval_days=0,
                ease_factor=DEFAULT_EASE_FACTOR,
                status="new",
            )
            self.db.add(progress)
            self.db.flush()

        srs_result = apply_sm2_review(
            SrsState(
                repetitions=progress.repetitions,
                interval_days=progress.interval_days,
                ease_factor=progress.ease_factor,
            ),
            rating=request.rating,
            reviewed_at=now,
        )
        progress.repetitions = srs_result.repetitions
        progress.interval_days = srs_result.interval_days
        progress.ease_factor = srs_result.ease_factor
        progress.due_at = srs_result.due_at
        progress.last_reviewed_at = now
        progress.status = srs_result.status

        is_correct = request.rating.value != "Again"
        review_log = ReviewLog(
            user_id=user.id,
            vocabulary_item_id=item.id,
            rating=request.rating.value,
            is_correct=is_correct,
            response_ms=request.response_ms,
            ease_factor_after=srs_result.ease_factor,
            next_due_at=srs_result.due_at,
            created_at=now,
        )
        self.db.add(review_log)
        self.db.commit()
        self.db.refresh(progress)

        return SubmitReviewResponse(
            vocabulary_item_id=item.id,
            rating=request.rating,
            is_correct=is_correct,
            repetitions=progress.repetitions,
            interval_days=progress.interval_days,
            ease_factor=progress.ease_factor,
            next_due_at=srs_result.due_at,
        )

    def _count_visible_items(self, user: User) -> int:
        return self.db.scalar(
            select(func.count(VocabularyItem.id))
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(self._visible_deck_filter(user)),
        ) or 0

    def _count_due_reviews(self, user: User, now: datetime | None = None) -> int:
        reviewed_at = now or self._utc_now()
        return self.db.scalar(
            select(func.count(UserWordProgress.vocabulary_item_id))
            .join(VocabularyItem, UserWordProgress.vocabulary_item_id == VocabularyItem.id)
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(
                UserWordProgress.user_id == user.id,
                UserWordProgress.due_at.is_not(None),
                UserWordProgress.due_at <= reviewed_at,
                self._visible_deck_filter(user),
            ),
        ) or 0

    def _count_new_items(self, user: User) -> int:
        return self.db.scalar(
            select(func.count(VocabularyItem.id))
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(self._visible_deck_filter(user), ~self._progress_exists_for_user(user)),
        ) or 0

    def _get_visible_item(self, user: User, vocabulary_item_id: int) -> VocabularyItem:
        item = self.db.scalar(
            select(VocabularyItem)
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(VocabularyItem.id == vocabulary_item_id, self._visible_deck_filter(user)),
        )
        if item is None:
            raise ApiError(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Không tìm thấy từ vựng để học.",
                code="NOT_FOUND",
            )
        return item

    def _review_card_response(
        self,
        item: VocabularyItem,
        is_new: bool,
        due_at: datetime | None,
    ) -> ReviewCardResponse:
        return ReviewCardResponse(
            id=item.id,
            deck_id=item.deck_id,
            word=item.word,
            pronunciation=item.pronunciation,
            meaning=item.meaning,
            description=item.description,
            example=item.example,
            note=item.note,
            image_url=item.image_url,
            word_audio_url=item.word_audio_url,
            meaning_audio_url=item.meaning_audio_url,
            example_audio_url=item.example_audio_url,
            is_new=is_new,
            due_at=self._ensure_utc(due_at) if due_at is not None else None,
        )

    def _visible_deck_filter(self, user: User):
        return or_(Deck.is_public.is_(True), Deck.user_id == user.id)

    def _progress_exists_for_user(self, user: User):
        return exists().where(
            UserWordProgress.user_id == user.id,
            UserWordProgress.vocabulary_item_id == VocabularyItem.id,
        )

    def _utc_now(self) -> datetime:
        return datetime.now(timezone.utc)

    def _ensure_utc(self, value: datetime) -> datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)
        return value.astimezone(timezone.utc)
