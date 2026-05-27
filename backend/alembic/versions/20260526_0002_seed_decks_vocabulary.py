"""create seed deck and vocabulary tables

Revision ID: 20260526_0002
Revises: 20260526_0001
Create Date: 2026-05-26 00:00:00.000000
"""

from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa

revision: str = "20260526_0002"
down_revision: str | None = "20260526_0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "decks",
        sa.Column("id", sa.Integer(), autoincrement=True, nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=True),
        sa.Column("name", sa.String(length=160), nullable=False),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("tags", sa.JSON(), nullable=True),
        sa.Column("is_public", sa.Boolean(), server_default=sa.text("false"), nullable=False),
        sa.Column("is_seed", sa.Boolean(), server_default=sa.text("false"), nullable=False),
        sa.Column("is_read_only", sa.Boolean(), server_default=sa.text("false"), nullable=False),
        sa.Column("source_name", sa.String(length=180), nullable=True),
        sa.Column("source_unit", sa.String(length=60), nullable=True),
        sa.Column("extra_metadata", sa.JSON(), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("source_name", "source_unit", name="uq_decks_source_name_unit"),
    )
    op.create_index(op.f("ix_decks_user_id"), "decks", ["user_id"], unique=False)
    op.create_index(op.f("ix_decks_source_name"), "decks", ["source_name"], unique=False)
    op.create_index(op.f("ix_decks_source_unit"), "decks", ["source_unit"], unique=False)

    op.create_table(
        "vocabulary_items",
        sa.Column("id", sa.Integer(), autoincrement=True, nullable=False),
        sa.Column("deck_id", sa.Integer(), nullable=False),
        sa.Column("word", sa.String(length=160), nullable=False),
        sa.Column("pronunciation", sa.String(length=160), nullable=True),
        sa.Column("meaning", sa.Text(), nullable=False),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("example", sa.Text(), nullable=True),
        sa.Column("collocation", sa.Text(), nullable=True),
        sa.Column("related_words", sa.JSON(), nullable=True),
        sa.Column("note", sa.Text(), nullable=True),
        sa.Column("suggestion", sa.String(length=160), nullable=True),
        sa.Column("anki_number", sa.String(length=40), nullable=True),
        sa.Column("source_key", sa.String(length=120), nullable=True),
        sa.Column("source_name", sa.String(length=180), nullable=True),
        sa.Column("source_unit", sa.String(length=60), nullable=True),
        sa.Column("image_url", sa.String(length=320), nullable=True),
        sa.Column("word_audio_url", sa.String(length=320), nullable=True),
        sa.Column("meaning_audio_url", sa.String(length=320), nullable=True),
        sa.Column("example_audio_url", sa.String(length=320), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(["deck_id"], ["decks.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("deck_id", "source_key", name="uq_vocabulary_items_deck_source_key"),
    )
    op.create_index(op.f("ix_vocabulary_items_deck_id"), "vocabulary_items", ["deck_id"], unique=False)
    op.create_index(op.f("ix_vocabulary_items_word"), "vocabulary_items", ["word"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_vocabulary_items_word"), table_name="vocabulary_items")
    op.drop_index(op.f("ix_vocabulary_items_deck_id"), table_name="vocabulary_items")
    op.drop_table("vocabulary_items")
    op.drop_index(op.f("ix_decks_source_unit"), table_name="decks")
    op.drop_index(op.f("ix_decks_source_name"), table_name="decks")
    op.drop_index(op.f("ix_decks_user_id"), table_name="decks")
    op.drop_table("decks")
