from typing import Annotated

from fastapi import APIRouter, Depends, Response, status
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.deck import DeckCreateRequest, DeckListResponse, DeckResponse, DeckUpdateRequest
from app.schemas.vocabulary import (
    VocabularyItemCreateRequest,
    VocabularyItemListResponse,
    VocabularyItemResponse,
    VocabularyItemUpdateRequest,
)
from app.services.deck_service import DeckService

router = APIRouter(tags=["decks"])


def get_deck_service(db: Annotated[Session, Depends(get_db)]) -> DeckService:
    return DeckService(db)


@router.get("/decks", response_model=DeckListResponse)
def list_decks(
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> DeckListResponse:
    return deck_service.list_decks(current_user)


@router.post("/decks", response_model=DeckResponse, status_code=status.HTTP_201_CREATED)
def create_deck(
    request: DeckCreateRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> DeckResponse:
    return deck_service.create_deck(current_user, request)


@router.get("/decks/{deck_id}", response_model=DeckResponse)
def get_deck(
    deck_id: int,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> DeckResponse:
    return deck_service.get_deck(current_user, deck_id)


@router.patch("/decks/{deck_id}", response_model=DeckResponse)
def update_deck(
    deck_id: int,
    request: DeckUpdateRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> DeckResponse:
    return deck_service.update_deck(current_user, deck_id, request)


@router.delete("/decks/{deck_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_deck(
    deck_id: int,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> Response:
    deck_service.delete_deck(current_user, deck_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/decks/{deck_id}/items", response_model=VocabularyItemListResponse)
def list_deck_items(
    deck_id: int,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> VocabularyItemListResponse:
    return deck_service.list_items(current_user, deck_id)


@router.post(
    "/decks/{deck_id}/items",
    response_model=VocabularyItemResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_deck_item(
    deck_id: int,
    request: VocabularyItemCreateRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> VocabularyItemResponse:
    return deck_service.create_item(current_user, deck_id, request)


@router.patch("/items/{item_id}", response_model=VocabularyItemResponse)
def update_item(
    item_id: int,
    request: VocabularyItemUpdateRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> VocabularyItemResponse:
    return deck_service.update_item(current_user, item_id, request)


@router.delete("/items/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_item(
    item_id: int,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
) -> Response:
    deck_service.delete_item(current_user, item_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
