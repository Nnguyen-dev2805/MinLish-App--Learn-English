from __future__ import annotations

import asyncio
from datetime import datetime
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

from sqlalchemy import select

from app.db.session import SessionLocal
from app.models.notification_preference import NotificationPreference
from app.models.user import User
from app.services.email_service import EmailService


class EmailReminderScheduler:
    """Simple in-process email reminder loop for the course project."""

    def __init__(self, check_seconds: int = 60) -> None:
        self.check_seconds = max(check_seconds, 10)
        self.email_service = EmailService()
        self._sent_keys: set[tuple[int, str]] = set()
        self._running = False

    async def run(self) -> None:
        if not self.email_service.is_configured:
            print("Email reminder scheduler skipped: SMTP is not configured.", flush=True)
            return

        self._running = True
        print("Email reminder scheduler started.", flush=True)
        while self._running:
            try:
                await asyncio.to_thread(self.send_due_reminders)
            except Exception as exc:
                print(f"Email reminder scheduler error: {exc}", flush=True)
            await asyncio.sleep(self.check_seconds)

    def stop(self) -> None:
        self._running = False

    def send_due_reminders(self) -> int:
        sent_count = 0
        with SessionLocal() as db:
            rows = db.execute(
                select(User, NotificationPreference)
                .join(NotificationPreference, NotificationPreference.user_id == User.id)
                .where(NotificationPreference.email_enabled.is_(True))
            ).all()

            for user, preference in rows:
                if not self._should_send_now(user.id, preference):
                    continue
                if self.email_service.send_daily_reminder(user.email, user.name):
                    sent_count += 1
                    print(f"Sent reminder email to {user.email}", flush=True)

        return sent_count

    def _should_send_now(self, user_id: int, preference: NotificationPreference) -> bool:
        try:
            timezone = ZoneInfo(preference.timezone)
        except ZoneInfoNotFoundError:
            timezone = ZoneInfo("Asia/Ho_Chi_Minh")

        now = datetime.now(timezone)
        current_time = now.strftime("%H:%M")
        if current_time != preference.daily_time:
            return False

        sent_key = (user_id, now.date().isoformat())
        if sent_key in self._sent_keys:
            return False

        self._sent_keys.add(sent_key)
        return True
