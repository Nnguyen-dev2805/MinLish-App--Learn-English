from sqlalchemy.orm import Session

from app.models.notification_preference import NotificationPreference
from app.models.user import User
from app.schemas.notification import UpdateNotificationPreferenceRequest

DEFAULT_DAILY_TIME = "20:00"
DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh"


class NotificationService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def get_preferences(self, user: User) -> NotificationPreference:
        preference = self.db.get(NotificationPreference, user.id)
        if preference is not None:
            return preference

        preference = NotificationPreference(
            user_id=user.id,
            daily_time=DEFAULT_DAILY_TIME,
            timezone=DEFAULT_TIMEZONE,
            email_enabled=False,
            push_enabled=True,
        )
        self.db.add(preference)
        self.db.commit()
        self.db.refresh(preference)
        return preference

    def update_preferences(
        self,
        user: User,
        request: UpdateNotificationPreferenceRequest,
    ) -> NotificationPreference:
        preference = self.get_preferences(user)
        update_data = request.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(preference, field, value)

        self.db.commit()
        self.db.refresh(preference)
        return preference
