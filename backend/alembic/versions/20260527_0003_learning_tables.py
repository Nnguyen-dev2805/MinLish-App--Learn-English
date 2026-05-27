"""create learning progress tables

Revision ID: 20260527_0003
Revises: 20260526_0002
Create Date: 2026-05-27 00:00:00.000000
"""

from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa

revision: str = "20260527_0003"
down_revision: str | None = "20260526_0002"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "user_word_progress",
        sa.Column("id", sa.Integer(), autoincrement=True, nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("vocabulary_item_id", sa.Integer(), nullable=False),
        sa.Column("repetitions", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.Column("interval_days", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.Column("ease_factor", sa.Float(), nullable=False, server_default=sa.text("2.5")),
        sa.Column("due_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("last_reviewed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("status", sa.String(length=40), nullable=False, server_default="new"),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["vocabulary_item_id"], ["vocabulary_items.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("user_id", "vocabulary_item_id", name="uq_user_word_progress_user_item"),
    )
    op.create_index(op.f("ix_user_word_progress_user_id"), "user_word_progress", ["user_id"], unique=False)
    op.create_index(
        op.f("ix_user_word_progress_vocabulary_item_id"),
        "user_word_progress",
        ["vocabulary_item_id"],
        unique=False,
    )
    op.create_index(
        "ix_user_word_progress_user_due",
        "user_word_progress",
        ["user_id", "due_at"],
        unique=False,
    )

    op.create_table(
        "review_logs",
        sa.Column("id", sa.Integer(), autoincrement=True, nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("vocabulary_item_id", sa.Integer(), nullable=False),
        sa.Column("rating", sa.String(length=20), nullable=False),
        sa.Column("is_correct", sa.Boolean(), nullable=False),
        sa.Column("response_ms", sa.Integer(), nullable=True),
        sa.Column("ease_factor_after", sa.Float(), nullable=False),
        sa.Column("next_due_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["vocabulary_item_id"], ["vocabulary_items.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_review_logs_user_id"), "review_logs", ["user_id"], unique=False)
    op.create_index(
        op.f("ix_review_logs_vocabulary_item_id"),
        "review_logs",
        ["vocabulary_item_id"],
        unique=False,
    )
    op.create_index("ix_review_logs_user_created", "review_logs", ["user_id", "created_at"], unique=False)

    op.create_table(
        "study_sessions",
        sa.Column("id", sa.Integer(), autoincrement=True, nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("ended_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("new_count", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.Column("review_count", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.Column("correct_count", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.Column("total_count", sa.Integer(), nullable=False, server_default=sa.text("0")),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(op.f("ix_study_sessions_user_id"), "study_sessions", ["user_id"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_study_sessions_user_id"), table_name="study_sessions")
    op.drop_table("study_sessions")
    op.drop_index("ix_review_logs_user_created", table_name="review_logs")
    op.drop_index(op.f("ix_review_logs_vocabulary_item_id"), table_name="review_logs")
    op.drop_index(op.f("ix_review_logs_user_id"), table_name="review_logs")
    op.drop_table("review_logs")
    op.drop_index("ix_user_word_progress_user_due", table_name="user_word_progress")
    op.drop_index(op.f("ix_user_word_progress_vocabulary_item_id"), table_name="user_word_progress")
    op.drop_index(op.f("ix_user_word_progress_user_id"), table_name="user_word_progress")
    op.drop_table("user_word_progress")
