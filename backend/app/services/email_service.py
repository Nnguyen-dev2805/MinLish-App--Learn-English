from __future__ import annotations

import smtplib
from email.message import EmailMessage

from app.core.config import get_settings


class EmailService:
    """Small SMTP helper for the course-project email features."""

    def __init__(self) -> None:
        self.settings = get_settings()

    @property
    def is_configured(self) -> bool:
        return all(
            [
                self.settings.smtp_host,
                self.settings.smtp_username,
                self.settings.smtp_password,
                self.from_email,
            ]
        )

    @property
    def from_email(self) -> str:
        return self.settings.smtp_from_email or self.settings.smtp_username

    def send_email(self, to_email: str, subject: str, body: str) -> bool:
        if not self.is_configured:
            return False

        message = EmailMessage()
        message["Subject"] = subject
        message["From"] = f"{self.settings.smtp_from_name} <{self.from_email}>"
        message["To"] = to_email
        message.set_content(body)

        with smtplib.SMTP(self.settings.smtp_host, self.settings.smtp_port) as smtp:
            smtp.starttls()
            smtp.login(self.settings.smtp_username, self.settings.smtp_password)
            smtp.send_message(message)
        return True

    def send_verification_otp(self, to_email: str, otp: str) -> bool:
        return self.send_email(
            to_email=to_email,
            subject="Verify your MinLish email",
            body=(
                "Welcome to MinLish!\n\n"
                f"Your email verification code is: {otp}\n"
                "This code expires in 10 minutes.\n\n"
                "If you did not create a MinLish account, you can ignore this email."
            ),
        )

    def send_password_reset_otp(self, to_email: str, otp: str) -> bool:
        return self.send_email(
            to_email=to_email,
            subject="Reset your MinLish password",
            body=(
                "You requested a MinLish password reset.\n\n"
                f"Your reset code is: {otp}\n"
                "This code expires in 10 minutes.\n\n"
                "If this was not you, you can ignore this email."
            ),
        )

    def send_daily_reminder(self, to_email: str, user_name: str | None) -> bool:
        display_name = user_name or "learner"
        return self.send_email(
            to_email=to_email,
            subject="Time to review your MinLish words",
            body=(
                f"Hi {display_name},\n\n"
                "Your MinLish vocabulary review is ready. Open the app and keep your streak going!\n\n"
                "MinLish Vocabulary App"
            ),
        )
