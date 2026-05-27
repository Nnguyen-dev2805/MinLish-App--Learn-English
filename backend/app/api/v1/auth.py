from typing import Annotated

from fastapi import APIRouter, Depends, Response, status
from sqlalchemy.orm import Session

from app.core.exceptions import ApiError
from app.db.session import get_db
from app.schemas.auth import (
    AuthResponse,
    GoogleLoginRequest,
    LoginRequest,
    LogoutRequest,
    RefreshRequest,
    RefreshResponse,
    RegisterRequest,
)
from app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["auth"])


def get_auth_service(db: Annotated[Session, Depends(get_db)]) -> AuthService:
    return AuthService(db)


@router.post("/register", response_model=AuthResponse)
def register(
    request: RegisterRequest,
    auth_service: Annotated[AuthService, Depends(get_auth_service)],
) -> AuthResponse:
    return auth_service.register(
        email=str(request.email),
        password=request.password,
        name=request.name,
    )


@router.post("/login", response_model=AuthResponse)
def login(
    request: LoginRequest,
    auth_service: Annotated[AuthService, Depends(get_auth_service)],
) -> AuthResponse:
    return auth_service.login(email=str(request.email), password=request.password)


@router.post("/google", response_model=AuthResponse)
def login_with_google(_request: GoogleLoginRequest) -> AuthResponse:
    raise ApiError(
        status_code=status.HTTP_400_BAD_REQUEST,
        detail="Google login chưa được cấu hình trong v1.",
        code="GOOGLE_LOGIN_NOT_CONFIGURED",
    )


@router.post("/refresh", response_model=RefreshResponse)
def refresh(
    request: RefreshRequest,
    auth_service: Annotated[AuthService, Depends(get_auth_service)],
) -> RefreshResponse:
    return auth_service.refresh(request.refresh_token)


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(
    request: LogoutRequest,
    auth_service: Annotated[AuthService, Depends(get_auth_service)],
) -> Response:
    auth_service.logout(request.refresh_token)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
