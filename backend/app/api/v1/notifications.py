from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.notification import (
    NotificationPreferenceResponse,
    UpdateNotificationPreferenceRequest,
)
from app.services.notification_service import NotificationService

router = APIRouter(prefix="/notifications", tags=["notifications"])


def get_notification_service(
    db: Annotated[Session, Depends(get_db)],
) -> NotificationService:
    return NotificationService(db)


@router.get("/preferences", response_model=NotificationPreferenceResponse)
def get_notification_preferences(
    current_user: Annotated[User, Depends(get_current_user)],
    notification_service: Annotated[
        NotificationService,
        Depends(get_notification_service),
    ],
) -> NotificationPreferenceResponse:
    return notification_service.get_preferences(current_user)


@router.patch("/preferences", response_model=NotificationPreferenceResponse)
def update_notification_preferences(
    request: UpdateNotificationPreferenceRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    notification_service: Annotated[
        NotificationService,
        Depends(get_notification_service),
    ],
) -> NotificationPreferenceResponse:
    return notification_service.update_preferences(current_user, request)
