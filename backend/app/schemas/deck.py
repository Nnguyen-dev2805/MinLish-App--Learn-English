from pydantic import BaseModel, Field


class DeckCreateRequest(BaseModel):
    name: str = Field(min_length=1, max_length=160)
    description: str | None = None
    tags: list[str] | None = None


class DeckUpdateRequest(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=160)
    description: str | None = None
    tags: list[str] | None = None


class DeckResponse(BaseModel):
    id: int
    name: str
    description: str | None = None
    tags: list[str] = Field(default_factory=list)
    is_public: bool
    is_seed: bool
    is_read_only: bool
    source_name: str | None = None
    source_unit: str | None = None
    word_count: int


class DeckListResponse(BaseModel):
    items: list[DeckResponse]
