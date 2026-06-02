"""add email otp fields to users

Revision ID: 20260527_0005
Revises: 20260527_0004
Create Date: 2026-05-27 00:00:00.000000
"""

from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa

revision: str = "20260527_0005"
down_revision: str | None = "20260527_0004"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "users",
        sa.Column("email_verified", sa.Boolean(), nullable=False, server_default=sa.text("false")),
    )
    op.add_column("users", sa.Column("email_otp_hash", sa.String(length=255), nullable=True))
    op.add_column("users", sa.Column("email_otp_expires_at", sa.DateTime(timezone=True), nullable=True))
    op.add_column("users", sa.Column("password_reset_otp_hash", sa.String(length=255), nullable=True))
    op.add_column(
        "users",
        sa.Column("password_reset_otp_expires_at", sa.DateTime(timezone=True), nullable=True),
    )
    op.alter_column("users", "email_verified", server_default=None)


def downgrade() -> None:
    op.drop_column("users", "password_reset_otp_expires_at")
    op.drop_column("users", "password_reset_otp_hash")
    op.drop_column("users", "email_otp_expires_at")
    op.drop_column("users", "email_otp_hash")
    op.drop_column("users", "email_verified")
