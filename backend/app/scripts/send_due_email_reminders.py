from __future__ import annotations

from app.services.email_reminder_scheduler import EmailReminderScheduler


def main() -> None:
    scheduler = EmailReminderScheduler()
    if not scheduler.email_service.is_configured:
        print("SMTP is not configured. Please update backend/.env first.")
        return

    sent_count = scheduler.send_due_reminders()
    print(f"Done. Sent {sent_count} reminder email(s).")


if __name__ == "__main__":
    main()
