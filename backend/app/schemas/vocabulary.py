from pydantic import BaseModel, Field


class VocabularyItemCreateRequest(BaseModel):
    word: str = Field(min_length=1, max_length=160)
    pronunciation: str | None = Field(default=None, max_length=160)
    meaning: str = Field(min_length=1)
    description: str | None = None
    example: str | None = None
    collocation: str | None = None
    related_words: list[str] | None = None
    note: str | None = None


class VocabularyItemUpdateRequest(BaseModel):
    word: str | None = Field(default=None, min_length=1, max_length=160)
    pronunciation: str | None = Field(default=None, max_length=160)
    meaning: str | None = Field(default=None, min_length=1)
    description: str | None = None
    example: str | None = None
    collocation: str | None = None
    related_words: list[str] | None = None
    note: str | None = None


class VocabularyItemResponse(BaseModel):
    id: int
    deck_id: int
    word: str
    pronunciation: str | None = None
    meaning: str
    description: str | None = None
    example: str | None = None
    collocation: str | None = None
    related_words: list[str] | None = None
    note: str | None = None
    suggestion: str | None = None
    image_url: str | None = None
    word_audio_url: str | None = None
    meaning_audio_url: str | None = None
    example_audio_url: str | None = None


class VocabularyItemListResponse(BaseModel):
    items: list[VocabularyItemResponse]
