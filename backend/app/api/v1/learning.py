from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.learning import (
    DailyPlanResponse,
    ReviewCardsResponse,
    SubmitReviewRequest,
    SubmitReviewResponse,
)
from app.services.learning_service import LearningService

router = APIRouter(prefix="/learning", tags=["learning"])


def get_learning_service(db: Annotated[Session, Depends(get_db)]) -> LearningService:
    return LearningService(db)


@router.get("/daily-plan", response_model=DailyPlanResponse)
def get_daily_plan(
    current_user: Annotated[User, Depends(get_current_user)],
    learning_service: Annotated[LearningService, Depends(get_learning_service)],
) -> DailyPlanResponse:
    return learning_service.get_daily_plan(current_user)


@router.get("/review-cards", response_model=ReviewCardsResponse)
def get_review_cards(
    current_user: Annotated[User, Depends(get_current_user)],
    learning_service: Annotated[LearningService, Depends(get_learning_service)],
) -> ReviewCardsResponse:
    return learning_service.get_review_cards(current_user)


@router.post("/reviews", response_model=SubmitReviewResponse)
def submit_review(
    request: SubmitReviewRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    learning_service: Annotated[LearningService, Depends(get_learning_service)],
) -> SubmitReviewResponse:
    return learning_service.submit_review(current_user, request)
