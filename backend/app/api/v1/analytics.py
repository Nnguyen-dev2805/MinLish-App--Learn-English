from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.analytics import ActivityResponse, DashboardResponse, RetentionResponse
from app.services.analytics_service import AnalyticsService

router = APIRouter(prefix="/analytics", tags=["analytics"])


def get_analytics_service(db: Annotated[Session, Depends(get_db)]) -> AnalyticsService:
    return AnalyticsService(db)


@router.get("/dashboard", response_model=DashboardResponse)
def get_dashboard(
    current_user: Annotated[User, Depends(get_current_user)],
    analytics_service: Annotated[AnalyticsService, Depends(get_analytics_service)],
) -> DashboardResponse:
    return analytics_service.get_dashboard(current_user)


@router.get("/activity", response_model=ActivityResponse)
def get_activity(
    current_user: Annotated[User, Depends(get_current_user)],
    analytics_service: Annotated[AnalyticsService, Depends(get_analytics_service)],
) -> ActivityResponse:
    return analytics_service.get_activity(current_user)


@router.get("/retention", response_model=RetentionResponse)
def get_retention(
    current_user: Annotated[User, Depends(get_current_user)],
    analytics_service: Annotated[AnalyticsService, Depends(get_analytics_service)],
) -> RetentionResponse:
    return analytics_service.get_retention(current_user)
