from typing import Annotated

from fastapi import APIRouter, Depends, File, Response, UploadFile, status
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
from app.services.import_export_service import ImportExportService

router = APIRouter(tags=["decks"])


def get_deck_service(db: Annotated[Session, Depends(get_db)]) -> DeckService:
    return DeckService(db)


def get_import_export_service(db: Annotated[Session, Depends(get_db)]) -> ImportExportService:
    return ImportExportService(db)


@router.post("/decks/{deck_id}/import", response_model=dict)
def import_deck_items(
    deck_id: int,
    current_user: Annotated[User, Depends(get_current_user)],
    deck_service: Annotated[DeckService, Depends(get_deck_service)],
    import_export_service: Annotated[ImportExportService, Depends(get_import_export_service)],
    file: UploadFile = File(...),
) -> dict:
    # First verify deck exists and user has access
    # Get deck will raise 404 if not found or 403 if user can't access
    # DeckService returns a DeckResponse schema, we need the actual model
    from app.models.deck import Deck
    deck_model = deck_service.db.query(Deck).filter(
        Deck.id == deck_id,
        Deck.user_id == current_user.id
    ).first()
    
    if not deck_model:
        from app.core.exceptions import ApiError
        raise ApiError(status_code=status.HTTP_404_NOT_FOUND, detail="Không tìm thấy bộ từ vựng hoặc bạn không có quyền.")

    imported_count = import_export_service.import_excel(current_user, deck_model, file)
    return {"message": "Nhập dữ liệu thành công.", "imported_count": imported_count}


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
