from datetime import datetime
from typing import TYPE_CHECKING

from sqlalchemy import Boolean, DateTime, Float, ForeignKey, Index, Integer, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.models.user import utc_now

if TYPE_CHECKING:
    from app.models.user import User
    from app.models.vocabulary_item import VocabularyItem


class UserWordProgress(Base):
    # Trạng thái của 1 từ đối với 1 user
    __tablename__ = "user_word_progress"
    __table_args__ = (
        UniqueConstraint("user_id", "vocabulary_item_id", name="uq_user_word_progress_user_item"),
        Index("ix_user_word_progress_user_due", "user_id", "due_at"),
    )

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    vocabulary_item_id: Mapped[int] = mapped_column(
        ForeignKey("vocabulary_items.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    repetitions: Mapped[int] = mapped_column(Integer, nullable=False, default=0) # số lần user đã ôn đúng/liên tiếp với từ này
    interval_days: Mapped[int] = mapped_column(Integer, nullable=False, default=0) # khoảng cách ngày đến lần ôn tiếp theo
    ease_factor: Mapped[float] = mapped_column(Float, nullable=False, default=2.5) # hệ số độ dễ của từ dùng cho thuật toán SM-2
    due_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True) # thời điểm từ này cần được ôn lại
    last_reviewed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True) # lần cuối user ôn từ này là khi nào
    status: Mapped[str] = mapped_column(String(40), nullable=False, default="new") # trạng thái của từ : new, agin. good, hard,..
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    ) # thời điểm tạo
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
        onupdate=utc_now,
    ) # thời điểm cập nhật

    user: Mapped["User"] = relationship()
    vocabulary_item: Mapped["VocabularyItem"] = relationship()


class ReviewLog(Base):  # Lịch sử từng lần user review 1 từ
    # UsewordProgresss: trạng thái hiện tại
    # ReviewLog: lịch sử từng lần học/ôn
    # Ví dụ: user học 5 lần từ "apple" thì có 1 process nhưng có 5 review log
    __tablename__ = "review_logs"
    __table_args__ = (
        Index("ix_review_logs_user_created", "user_id", "created_at"),
    )

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    vocabulary_item_id: Mapped[int] = mapped_column(
        ForeignKey("vocabulary_items.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    rating: Mapped[str] = mapped_column(String(20), nullable=False)
    is_correct: Mapped[bool] = mapped_column(Boolean, nullable=False) # user trả lời đúng hay sai
    response_ms: Mapped[int | None] = mapped_column(Integer, nullable=True) # thời gian phản hồi
    ease_factor_after: Mapped[float] = mapped_column(Float, nullable=False) # ease factor sau lần review này
    next_due_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False) # thời điểm ôn tiếp theo sau lần review này
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    )

    user: Mapped["User"] = relationship()
    vocabulary_item: Mapped["VocabularyItem"] = relationship()


class StudySession(Base):
    __tablename__ = "study_sessions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
        nullable=False,
    )
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    ended_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    new_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    review_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    correct_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    total_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)

    user: Mapped["User"] = relationship()
