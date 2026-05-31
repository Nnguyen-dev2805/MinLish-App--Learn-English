from datetime import datetime, timedelta, timezone

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
from app.schemas.auth import AuthResponse, RefreshResponse
from app.schemas.user import UpdateUserRequest


class AuthService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def register(self, email: str, password: str, name: str) -> AuthResponse:
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
            daily_new_words=10,
        )
        self.db.add(user)
        self.db.flush()

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

    def _get_user_by_email(self, email: str) -> User | None:
        return self.db.scalar(select(User).where(User.email == email))

    @staticmethod
    def _normalize_email(email: str) -> str:
        return email.strip().lower()

    @staticmethod
    def _is_expired(expires_at: datetime) -> bool:
        if expires_at.tzinfo is None:
            expires_at = expires_at.replace(tzinfo=timezone.utc)
        return expires_at <= datetime.now(timezone.utc)

    @staticmethod
    def _invalid_credentials_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Email hoặc mật khẩu không đúng.",
            code="INVALID_CREDENTIALS",
        )

    @staticmethod
    def _invalid_refresh_token_error() -> ApiError:
        return ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token không hợp lệ hoặc đã hết hạn.",
            code="INVALID_REFRESH_TOKEN",
        )
