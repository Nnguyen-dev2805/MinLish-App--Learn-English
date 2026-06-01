from fastapi import status
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session

from app.core.exceptions import ApiError
from app.models.deck import Deck
from app.models.progress import UserWordProgress
from app.models.user import User
from app.models.vocabulary_item import VocabularyItem
from app.schemas.deck import DeckCreateRequest, DeckListResponse, DeckResponse, DeckUpdateRequest
from app.schemas.vocabulary import (
    VocabularyItemCreateRequest,
    VocabularyItemListResponse,
    VocabularyItemResponse,
    VocabularyItemUpdateRequest,
)


class DeckService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def list_decks(self, user: User) -> DeckListResponse:
        rows = self._visible_deck_count_statement(user).all()
        return DeckListResponse(
            items=[
                self._deck_response(
                    deck=deck,
                    word_count=word_count,
                    learned_count=self._learned_count(user, deck.id),
                )
                for deck, word_count in rows
            ],
        )

    def create_deck(self, user: User, request: DeckCreateRequest) -> DeckResponse:
        deck = Deck(
            user_id=user.id,
            name=request.name.strip(),
            description=self._clean_optional_text(request.description),
            tags=self._clean_tags(request.tags),
            is_public=False,
            is_seed=False,
            is_read_only=False,
        )
        self.db.add(deck)
        self.db.commit()
        self.db.refresh(deck)
        return self._deck_response(deck, word_count=0, learned_count=0)

    def get_deck(self, user: User, deck_id: int) -> DeckResponse:
        deck = self._get_visible_deck(user, deck_id)
        return self._deck_response(
            deck=deck,
            word_count=self._word_count(deck.id),
            learned_count=self._learned_count(user, deck.id),
        )

    def update_deck(self, user: User, deck_id: int, request: DeckUpdateRequest) -> DeckResponse:
        deck = self._get_visible_deck(user, deck_id)
        self._ensure_deck_can_be_modified(user, deck)

        update_data = request.model_dump(exclude_unset=True)
        if "name" in update_data and update_data["name"] is not None:
            deck.name = update_data["name"].strip()
        if "description" in update_data:
            deck.description = self._clean_optional_text(update_data["description"])
        if "tags" in update_data:
            deck.tags = self._clean_tags(update_data["tags"])

        self.db.commit()
        self.db.refresh(deck)
        return self._deck_response(
            deck=deck,
            word_count=self._word_count(deck.id),
            learned_count=self._learned_count(user, deck.id),
        )

    def delete_deck(self, user: User, deck_id: int) -> None:
        deck = self._get_visible_deck(user, deck_id)
        self._ensure_deck_can_be_modified(user, deck)
        self.db.delete(deck)
        self.db.commit()

    def list_items(self, user: User, deck_id: int) -> VocabularyItemListResponse:
        deck = self._get_visible_deck(user, deck_id)
        items = self.db.scalars(
            select(VocabularyItem)
            .where(VocabularyItem.deck_id == deck.id)
            .order_by(VocabularyItem.anki_number, VocabularyItem.word, VocabularyItem.id),
        ).all()
        return VocabularyItemListResponse(items=[self._item_response(item) for item in items])

    def create_item(
        self,
        user: User,
        deck_id: int,
        request: VocabularyItemCreateRequest,
    ) -> VocabularyItemResponse:
        deck = self._get_visible_deck(user, deck_id)
        self._ensure_deck_can_be_modified(user, deck)

        item = VocabularyItem(
            deck_id=deck.id,
            word=request.word.strip(),
            pronunciation=self._clean_optional_text(request.pronunciation),
            meaning=request.meaning.strip(),
            description=self._clean_optional_text(request.description),
            example=self._clean_optional_text(request.example),
            collocation=self._clean_optional_text(request.collocation),
            related_words=self._clean_tags(request.related_words),
            note=self._clean_optional_text(request.note),
        )
        self.db.add(item)
        self.db.commit()
        self.db.refresh(item)
        return self._item_response(item)

    def update_item(
        self,
        user: User,
        item_id: int,
        request: VocabularyItemUpdateRequest,
    ) -> VocabularyItemResponse:
        item = self._get_visible_item(user, item_id)
        self._ensure_deck_can_be_modified(user, item.deck)

        update_data = request.model_dump(exclude_unset=True)
        if "word" in update_data and update_data["word"] is not None:
            item.word = update_data["word"].strip()
        if "pronunciation" in update_data:
            item.pronunciation = self._clean_optional_text(update_data["pronunciation"])
        if "meaning" in update_data and update_data["meaning"] is not None:
            item.meaning = update_data["meaning"].strip()
        if "description" in update_data:
            item.description = self._clean_optional_text(update_data["description"])
        if "example" in update_data:
            item.example = self._clean_optional_text(update_data["example"])
        if "collocation" in update_data:
            item.collocation = self._clean_optional_text(update_data["collocation"])
        if "related_words" in update_data:
            item.related_words = self._clean_tags(update_data["related_words"])
        if "note" in update_data:
            item.note = self._clean_optional_text(update_data["note"])

        self.db.commit()
        self.db.refresh(item)
        return self._item_response(item)

    def delete_item(self, user: User, item_id: int) -> None:
        item = self._get_visible_item(user, item_id)
        self._ensure_deck_can_be_modified(user, item.deck)
        self.db.delete(item)
        self.db.commit()

    def _visible_deck_count_statement(self, user: User):
        return self.db.execute(
            select(Deck, func.count(VocabularyItem.id))
            .outerjoin(VocabularyItem, VocabularyItem.deck_id == Deck.id)
            .where(self._visible_deck_filter(user))
            .group_by(Deck.id)
            .order_by(Deck.is_seed.desc(), Deck.source_unit.asc(), Deck.id.asc()),
        )

    def _get_visible_deck(self, user: User, deck_id: int) -> Deck:
        deck = self.db.scalar(
            select(Deck).where(Deck.id == deck_id, self._visible_deck_filter(user)),
        )
        if deck is None:
            raise self._not_found_error("Không tìm thấy deck.")
        return deck

    def _get_visible_item(self, user: User, item_id: int) -> VocabularyItem:
        item = self.db.scalar(
            select(VocabularyItem)
            .join(Deck, VocabularyItem.deck_id == Deck.id)
            .where(VocabularyItem.id == item_id, self._visible_deck_filter(user)),
        )
        if item is None:
            raise self._not_found_error("Không tìm thấy từ vựng.")
        return item

    def _visible_deck_filter(self, user: User):
        return or_(Deck.is_public.is_(True), Deck.user_id == user.id)

    def _ensure_deck_can_be_modified(self, user: User, deck: Deck) -> None:
        if deck.is_seed or deck.is_read_only:
            raise ApiError(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Seed deck là dữ liệu chỉ đọc.",
                code="SEED_DECK_READ_ONLY",
            )
        if deck.user_id != user.id:
            raise ApiError(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Bạn không có quyền sửa deck này.",
                code="DECK_FORBIDDEN",
            )

    def _word_count(self, deck_id: int) -> int:
        return self.db.scalar(
            select(func.count(VocabularyItem.id)).where(VocabularyItem.deck_id == deck_id),
        ) or 0

    def _learned_count(self, user: User, deck_id: int) -> int:
        return self.db.scalar(
            select(func.count(UserWordProgress.id))
            .join(VocabularyItem, VocabularyItem.id == UserWordProgress.vocabulary_item_id)
            .where(
                UserWordProgress.user_id == user.id,
                VocabularyItem.deck_id == deck_id,
                UserWordProgress.last_reviewed_at.is_not(None),
            ),
        ) or 0

    def _deck_response(self, deck: Deck, word_count: int, learned_count: int) -> DeckResponse:
        return DeckResponse(
            id=deck.id,
            name=deck.name,
            description=deck.description,
            tags=deck.tags or [],
            is_public=deck.is_public,
            is_seed=deck.is_seed,
            is_read_only=deck.is_read_only,
            source_name=deck.source_name,
            source_unit=deck.source_unit,
            word_count=word_count,
            learned_count=learned_count,
        )

    def _item_response(self, item: VocabularyItem) -> VocabularyItemResponse:
        return VocabularyItemResponse(
            id=item.id,
            deck_id=item.deck_id,
            word=item.word,
            pronunciation=item.pronunciation,
            meaning=item.meaning,
            description=item.description,
            example=item.example,
            collocation=item.collocation,
            related_words=item.related_words,
            note=item.note,
            suggestion=item.suggestion,
            image_url=item.image_url,
            word_audio_url=item.word_audio_url,
            meaning_audio_url=item.meaning_audio_url,
            example_audio_url=item.example_audio_url,
        )

    def _clean_optional_text(self, value: str | None) -> str | None:
        if value is None:
            return None
        cleaned = value.strip()
        return cleaned or None

    def _clean_tags(self, values: list[str] | None) -> list[str] | None:
        if values is None:
            return None
        cleaned = [value.strip() for value in values if value.strip()]
        return cleaned or None

    @staticmethod
    def _not_found_error(detail: str) -> ApiError:
        return ApiError(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=detail,
            code="NOT_FOUND",
        )
