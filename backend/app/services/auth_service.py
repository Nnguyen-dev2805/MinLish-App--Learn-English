from collections.abc import Callable
from datetime import datetime, timedelta, timezone
from secrets import randbelow

from fastapi import status
from google.auth.transport import requests
from google.oauth2 import id_token
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import get_settings
from app.core.exceptions import ApiError
from app.core.security import (
    create_access_token,
    generate_refresh_token,
    hash_password,
    hash_refresh_token,
    verify_password,
)
from app.models.refresh_token import RefreshToken
from app.models.user import User
from app.schemas.auth import AuthResponse, MessageResponse, RefreshResponse
from app.schemas.user import UpdateUserRequest
from app.services.email_service import EmailService

OTP_EXPIRE_MINUTES = 10


class AuthService:
    def __init__(self, db: Session) -> None:
        self.db = db
        self.email_service = EmailService()

    def register(
        self,
        email: str,
        password: str,
        name: str,
        goal: str | None = None,
        level: str | None = None,
    ) -> AuthResponse:
        normalized_email = self._normalize_email(email)
        existing_user = self._get_user_by_email(normalized_email)
        if existing_user is not None:
            raise ApiError(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email đã được sử dụng.",
                code="EMAIL_ALREADY_EXISTS",
            )

        user = User(
            email=normalized_email,
            password_hash=hash_password(password),
            name=name.strip(),
            goal=goal.strip() if goal else None,
            level=level.strip() if level else None,
            daily_new_words=10,
        )
        self.db.add(user)
        self.db.flush()
        otp = self._set_email_otp(user)
        self._send_email_safely(
            lambda: self.email_service.send_verification_otp(user.email, otp),
            "Không thể gửi mã xác nhận email.",
        )

        response = self._create_auth_response(user)
        self.db.commit()
        self.db.refresh(user)
        return response

    def login(self, email: str, password: str) -> AuthResponse:
        user = self._get_user_by_email(self._normalize_email(email))
        if user is None or user.password_hash is None:
            raise self._invalid_credentials_error()

        if not verify_password(password, user.password_hash):
            raise self._invalid_credentials_error()

        if not user.email_verified:
            raise self._email_not_verified_error()

        response = self._create_auth_response(user)
        self.db.commit()
        return response

    def login_with_google(self, id_token_str: str, client_id: str) -> AuthResponse:
        try:
            idinfo = id_token.verify_oauth2_token(id_token_str, requests.Request(), client_id)
            email = idinfo.get('email')
            name = idinfo.get('name', 'Google User')
            
            if not email:
                raise ValueError("Email not provided by Google.")
                
            normalized_email = self._normalize_email(email)
            user = self._get_user_by_email(normalized_email)
            
            if user is None:
                # Create a new user if doesn't exist
                user = User(
                    email=normalized_email,
                    password_hash=None,  # No password for Google users
                    name=name,
                    email_verified=True,
                    daily_new_words=10,
                )
                self.db.add(user)
                self.db.flush()
                
            response = self._create_auth_response(user)
            self.db.commit()
            self.db.refresh(user)
            return response
        except ValueError as e:
            raise ApiError(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Google token verification failed: {str(e)}",
                code="INVALID_GOOGLE_TOKEN",
            )

    def refresh(self, refresh_token: str) -> RefreshResponse:
        token_hash = hash_refresh_token(refresh_token)
        token_record = self.db.scalar(
            select(RefreshToken).where(RefreshToken.token_hash == token_hash),
        )
        if token_record is None or token_record.revoked_at is not None:
            raise self._invalid_refresh_token_error()

        if self._is_expired(token_record.expires_at):
            raise self._invalid_refresh_token_error()

        return RefreshResponse(access_token=create_access_token(token_record.user_id))

    def logout(self, refresh_token: str) -> None:
        token_hash = hash_refresh_token(refresh_token)
        token_record = self.db.scalar(
            select(RefreshToken).where(RefreshToken.token_hash == token_hash),
        )
        if token_record is not None and token_record.revoked_at is None:
            token_record.revoked_at = datetime.now(timezone.utc)
            self.db.commit()

    def verify_email(self, email: str, otp: str) -> AuthResponse:
        user = self._get_user_by_email(self._normalize_email(email))
        if user is None or user.email_otp_hash is None:
            raise self._invalid_otp_error()

        if self._is_expired(user.email_otp_expires_at):
            raise self._invalid_otp_error()

        if not verify_password(otp.strip(), user.email_otp_hash):
            raise self._invalid_otp_error()

        user.email_verified = True
        user.email_otp_hash = None
        user.email_otp_expires_at = None
        response = self._create_auth_response(user)
        self.db.commit()
        return response

    def resend_verification_otp(self, email: str) -> MessageResponse:
        user = self._get_user_by_email(self._normalize_email(email))
        if user is None:
            return MessageResponse(message="Verification OTP sent.")

        otp = self._set_email_otp(user)
        self._send_email_safely(
            lambda: self.email_service.send_verification_otp(user.email, otp),
            "Không thể gửi lại mã xác nhận email.",
        )
        self.db.commit()
        return MessageResponse(message="Verification OTP sent.")

    def forgot_password(self, email: str) -> MessageResponse:
        user = self._get_user_by_email(self._normalize_email(email))
        if user is not None and user.password_hash is not None:
            otp = self._set_password_reset_otp(user)
            self._send_email_safely(
                lambda: self.email_service.send_password_reset_otp(user.email, otp),
                "Không thể gửi mã đặt lại mật khẩu.",
            )
            self.db.commit()
        return MessageResponse(message="If the email exists, a reset code has been sent.")

    def reset_password(self, email: str, otp: str, new_password: str) -> MessageResponse:
        user = self._get_user_by_email(self._normalize_email(email))
        if user is None or user.password_reset_otp_hash is None:
            raise self._invalid_reset_otp_error()

        if self._is_expired(user.password_reset_otp_expires_at):
            raise self._invalid_reset_otp_error()

        if not verify_password(otp.strip(), user.password_reset_otp_hash):
            raise self._invalid_reset_otp_error()

        user.password_hash = hash_password(new_password)
        user.email_verified = True
        user.password_reset_otp_hash = None
        user.password_reset_otp_expires_at = None
        self.db.commit()
        return MessageResponse(message="Password reset successfully.")

    def update_user(self, user: User, request: UpdateUserRequest) -> User:
        update_data = request.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(user, field, value)
        self.db.commit()
        self.db.refresh(user)
        return user

    def _create_auth_response(self, user: User) -> AuthResponse:
        settings = get_settings()
        refresh_token = generate_refresh_token()
        token_record = RefreshToken(
            user_id=user.id,
            token_hash=hash_refresh_token(refresh_token),
            expires_at=datetime.now(timezone.utc)
            + timedelta(days=settings.refresh_token_expire_days),
        )
        self.db.add(token_record)
        return AuthResponse(
            access_token=create_access_token(user.id),
            refresh_token=refresh_token,
            user=user,
        )

    def _set_email_otp(self, user: User) -> str:
        otp = self._generate_otp()
        user.email_verified = False
        user.email_otp_hash = hash_password(otp)
        user.email_otp_expires_at = datetime.now(timezone.utc) + timedelta(minutes=OTP_EXPIRE_MINUTES)
        return otp

    def _set_password_reset_otp(self, user: User) -> str:
        otp = self._generate_otp()
        user.password_reset_otp_hash = hash_password(otp)
        user.password_reset_otp_expires_at = datetime.now(timezone.utc) + timedelta(minutes=OTP_EXPIRE_MINUTES)
        return otp

    def _send_email_safely(self, send: Callable[[], bool], failure_detail: str) -> None:
        try:
            send()
        except Exception as exc:
            if self.email_service.is_configured:
                raise ApiError(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=failure_detail,
                    code="EMAIL_SEND_FAILED",
                ) from exc

    def _get_user_by_email(self, email: str) -> User | None:
        return self.db.scalar(select(User).where(User.email == email))

    @staticmethod
    def _normalize_email(email: str) -> str:
        return email.strip().lower()

    @staticmethod
    def _is_expired(expires_at: datetime) -> bool:
        if expires_at is None:
            return True
        if expires_at.tzinfo is None:
            expires_at = expires_at.replace(tzinfo=timezone.utc)
        return expires_at <= datetime.now(timezone.utc)

    @staticmethod
    def _generate_otp() -> str:
        return f"{randbelow(1_000_000):06d}"

    @staticmethod
    def _invalid_credentials_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Email hoặc mật khẩu không đúng.",
            code="INVALID_CREDENTIALS",
        )

    @staticmethod
    def _email_not_verified_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Please verify your email before using MinLish.",
            code="EMAIL_NOT_VERIFIED",
        )

    @staticmethod
    def _invalid_refresh_token_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token không hợp lệ hoặc đã hết hạn.",
            code="INVALID_REFRESH_TOKEN",
        )

    @staticmethod
    def _invalid_otp_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Mã xác nhận email không đúng hoặc đã hết hạn.",
            code="INVALID_EMAIL_OTP",
        )

    @staticmethod
    def _invalid_reset_otp_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Mã đặt lại mật khẩu không đúng hoặc đã hết hạn.",
            code="INVALID_PASSWORD_RESET_OTP",
        )
