from typing import Annotated

from fastapi import Depends, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.core.exceptions import ApiError
from app.core.security import decode_access_token
from app.db.session import get_db
from app.models.user import User

bearer_scheme = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer_scheme)],
    db: Annotated[Session, Depends(get_db)],
) -> User:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Access token is missing.",
            code="UNAUTHORIZED",
        )

    user_id = decode_access_token(credentials.credentials)
    if user_id is None:
        raise ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Access token is invalid or expired.",
            code="UNAUTHORIZED",
        )

    user = db.get(User, user_id)
    if user is None:
        raise ApiError(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User does not exist.",
            code="UNAUTHORIZED",
        )
    if not user.email_verified:
        raise ApiError(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Please verify your email before using MinLish.",
            code="EMAIL_NOT_VERIFIED",
        )
    return user
