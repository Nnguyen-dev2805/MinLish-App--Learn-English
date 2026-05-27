from datetime import datetime
from typing import TYPE_CHECKING

from sqlalchemy import DateTime, ForeignKey, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.models.user import utc_now

if TYPE_CHECKING:
    from app.models.deck import Deck


class VocabularyItem(Base):
    __tablename__ = "vocabulary_items"
    __table_args__ = (
        UniqueConstraint("deck_id", "source_key", name="uq_vocabulary_items_deck_source_key"),
    )

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    deck_id: Mapped[int] = mapped_column(
        ForeignKey("decks.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    word: Mapped[str] = mapped_column(String(160), index=True, nullable=False)
    pronunciation: Mapped[str | None] = mapped_column(String(160), nullable=True)
    meaning: Mapped[str] = mapped_column(Text, nullable=False)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    example: Mapped[str | None] = mapped_column(Text, nullable=True)
    collocation: Mapped[str | None] = mapped_column(Text, nullable=True)
    related_words: Mapped[list[str] | None] = mapped_column(JSON, nullable=True)
    note: Mapped[str | None] = mapped_column(Text, nullable=True)
    suggestion: Mapped[str | None] = mapped_column(String(160), nullable=True)
    anki_number: Mapped[str | None] = mapped_column(String(40), nullable=True)
    source_key: Mapped[str | None] = mapped_column(String(120), nullable=True)
    source_name: Mapped[str | None] = mapped_column(String(180), nullable=True)
    source_unit: Mapped[str | None] = mapped_column(String(60), nullable=True)
    image_url: Mapped[str | None] = mapped_column(String(320), nullable=True)
    word_audio_url: Mapped[str | None] = mapped_column(String(320), nullable=True)
    meaning_audio_url: Mapped[str | None] = mapped_column(String(320), nullable=True)
    example_audio_url: Mapped[str | None] = mapped_column(String(320), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
        onupdate=utc_now,
    )

    deck: Mapped["Deck"] = relationship(back_populates="vocabulary_items")
