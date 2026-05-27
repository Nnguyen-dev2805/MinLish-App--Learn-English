from __future__ import annotations

from dataclasses import dataclass, field
import html
import json
from pathlib import Path
import re
import shutil
import sqlite3
import tempfile
from zipfile import ZipFile

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import get_settings
from app.models.deck import Deck
from app.models.vocabulary_item import VocabularyItem

SOURCE_NAME = "4000 Essential English Words - Book 2"
MEDIA_URL_PREFIX = "/static/media/anki/book2"
FIELD_SEPARATOR = "\x1f"

IMAGE_RE = re.compile(r"<img[^>]+src=[\"']([^\"']+)[\"']", re.IGNORECASE)
SOUND_RE = re.compile(r"\[sound:([^\]]+)\]", re.IGNORECASE)
HTML_TAG_RE = re.compile(r"<[^>]+>")
CLOZE_RE = re.compile(r"\{\{c\d+::(.*?)(?:::.*?)?\}\}")


@dataclass(frozen=True)
class FailedAnkiRow:
    note_id: int
    reason: str


@dataclass
class AnkiImportResult:
    total_notes: int = 0
    media_entries: int = 0
    media_extracted: int = 0
    media_skipped: int = 0
    decks_created: int = 0
    decks_updated: int = 0
    items_created: int = 0
    items_updated: int = 0
    failed_rows: list[FailedAnkiRow] = field(default_factory=list)

    @property
    def successful_items(self) -> int:
        return self.items_created + self.items_updated


@dataclass(frozen=True)
class AnkiNote:
    note_id: int
    deck_id: str
    deck_name: str
    fields: dict[str, str]


@dataclass(frozen=True)
class ParsedVocabulary:
    source_unit: str
    anki_number: str
    source_key: str
    word: str
    pronunciation: str | None
    meaning: str
    description: str | None
    example: str | None
    suggestion: str | None
    note: str | None
    image_url: str | None
    word_audio_url: str | None
    meaning_audio_url: str | None
    example_audio_url: str | None


class AnkiImportService:
    def __init__(
        self,
        db: Session,
        static_root: Path | None = None,
        media_url_prefix: str = MEDIA_URL_PREFIX,
    ) -> None:
        self.db = db
        settings = get_settings()
        self.static_root = static_root or settings.resolved_static_dir
        self.media_url_prefix = media_url_prefix.rstrip("/")
        self.static_media_dir = self.static_root / "media" / "anki" / "book2"

    def import_apkg(self, apkg_path: str | Path) -> AnkiImportResult:
        path = Path(apkg_path)
        if not path.exists():
            raise FileNotFoundError(f"Không tìm thấy file Anki: {path}")

        result = AnkiImportResult()
        with ZipFile(path) as archive:
            media_map = self._read_media_map(archive)
            result.media_entries = len(media_map)
            media_lookup, extracted_count, skipped_count = self._extract_media(archive, media_map)
            result.media_extracted = extracted_count
            result.media_skipped = skipped_count

            notes = self._read_notes(archive)

        result.total_notes = len(notes)
        deck_cache: dict[str, Deck] = {}

        for source_unit in sorted({self._source_unit(note) for note in notes}, key=self._unit_sort_key):
            deck, created = self._upsert_seed_deck(source_unit)
            deck_cache[source_unit] = deck
            if created:
                result.decks_created += 1
            else:
                result.decks_updated += 1

        for note in notes:
            try:
                parsed = self._parse_note(note, media_lookup)
            except ValueError as exc:
                result.failed_rows.append(FailedAnkiRow(note_id=note.note_id, reason=str(exc)))
                continue

            deck = deck_cache[parsed.source_unit]
            _, created = self._upsert_vocabulary_item(deck, parsed)
            if created:
                result.items_created += 1
            else:
                result.items_updated += 1

        self.db.commit()
        return result

    def _read_media_map(self, archive: ZipFile) -> dict[str, str]:
        raw = archive.read("media").decode("utf-8")
        media_map = json.loads(raw)
        return {str(key): str(value) for key, value in media_map.items()}

    def _extract_media(
        self,
        archive: ZipFile,
        media_map: dict[str, str],
    ) -> tuple[dict[str, str], int, int]:
        self.static_media_dir.mkdir(parents=True, exist_ok=True)
        archive_members = set(archive.namelist())
        media_lookup: dict[str, str] = {}
        extracted_count = 0
        skipped_count = 0

        for archive_name, filename in media_map.items():
            if archive_name not in archive_members:
                continue

            safe_filename = self._safe_media_filename(filename)
            if safe_filename is None:
                continue

            target_path = self.static_media_dir / safe_filename
            media_lookup[safe_filename] = self._media_url(safe_filename)
            archive_info = archive.getinfo(archive_name)
            if target_path.exists() and target_path.stat().st_size == archive_info.file_size:
                skipped_count += 1
                continue

            with archive.open(archive_name) as source, target_path.open("wb") as target:
                shutil.copyfileobj(source, target)
            extracted_count += 1

        return media_lookup, extracted_count, skipped_count

    def _read_notes(self, archive: ZipFile) -> list[AnkiNote]:
        with tempfile.TemporaryDirectory() as temp_dir:
            collection_path = Path(temp_dir) / "collection.anki2"
            collection_path.write_bytes(archive.read("collection.anki2"))

            connection = sqlite3.connect(collection_path)
            connection.row_factory = sqlite3.Row
            try:
                collection_row = connection.execute("select decks, models from col limit 1").fetchone()
                if collection_row is None:
                    raise ValueError("Không đọc được metadata Anki trong bảng col.")

                deck_names = self._read_deck_names(collection_row["decks"])
                field_names = self._read_field_names(collection_row["models"])
                rows = connection.execute(
                    """
                    select notes.id as note_id, notes.flds as fields, cards.did as deck_id
                    from notes
                    join cards on cards.nid = notes.id
                    order by cards.did, notes.id
                    """,
                ).fetchall()
            finally:
                connection.close()

        return [
            AnkiNote(
                note_id=int(row["note_id"]),
                deck_id=str(row["deck_id"]),
                deck_name=deck_names.get(str(row["deck_id"]), ""),
                fields=self._map_note_fields(field_names, row["fields"]),
            )
            for row in rows
        ]

    def _read_deck_names(self, decks_json: str) -> dict[str, str]:
        decks = json.loads(decks_json)
        return {
            str(deck_id): str(deck.get("name", ""))
            for deck_id, deck in decks.items()
            if isinstance(deck, dict)
        }

    def _read_field_names(self, models_json: str) -> list[str]:
        models = json.loads(models_json)
        for model in models.values():
            fields = model.get("flds")
            if isinstance(fields, list):
                return [str(field.get("name", "")) for field in fields if isinstance(field, dict)]
        raise ValueError("Không đọc được field mapping từ Anki model.")

    def _map_note_fields(self, field_names: list[str], raw_fields: str) -> dict[str, str]:
        values = raw_fields.split(FIELD_SEPARATOR)
        return {
            field_name: values[index] if index < len(values) else ""
            for index, field_name in enumerate(field_names)
        }

    def _parse_note(
        self,
        note: AnkiNote,
        media_lookup: dict[str, str],
    ) -> ParsedVocabulary:
        anki_number = self._clean_text(note.fields.get("№", ""))
        word = self._clean_text(note.fields.get("Keyword", ""))
        meaning = self._clean_text(note.fields.get("Short Vietnamese", ""))
        if not word:
            raise ValueError("Thiếu Keyword/word.")
        if not meaning:
            raise ValueError(f"Thiếu Short Vietnamese/meaning cho word '{word}'.")
        if not anki_number:
            raise ValueError(f"Thiếu số thứ tự Anki cho word '{word}'.")

        description, example = self._parse_explanation(note.fields.get("Explanation", ""))
        source_unit = self._source_unit(note, anki_number)
        source_key = f"book2-{anki_number}"

        return ParsedVocabulary(
            source_unit=source_unit,
            anki_number=anki_number,
            source_key=source_key,
            word=word,
            pronunciation=self._clean_text_or_none(note.fields.get("Transcription", "")),
            meaning=meaning,
            description=description,
            example=example,
            suggestion=self._clean_text_or_none(note.fields.get("Suggestion", "")),
            note=self._clean_text_or_none(note.fields.get("Full Vietnamese", "")),
            image_url=self._media_url_from_html(note.fields.get("IMG", ""), media_lookup),
            word_audio_url=self._media_url_from_sound(note.fields.get("Sound", ""), media_lookup),
            meaning_audio_url=self._media_url_from_sound(note.fields.get("Meaning", ""), media_lookup),
            example_audio_url=self._media_url_from_sound(note.fields.get("Example", ""), media_lookup),
        )

    def _parse_explanation(self, raw_value: str) -> tuple[str | None, str | None]:
        value = html.unescape(self._replace_cloze(raw_value))
        parts = re.split(r"\s*(?:→|->)\s*", value, maxsplit=1)
        description = self._clean_text_or_none(parts[0])
        example = self._clean_text_or_none(parts[1]) if len(parts) > 1 else None
        return description, example

    def _source_unit(self, note: AnkiNote, anki_number: str | None = None) -> str:
        if "::" in note.deck_name:
            unit = note.deck_name.rsplit("::", 1)[-1].strip()
            if unit:
                return unit

        number = anki_number or self._clean_text(note.fields.get("№", ""))
        return self._unit_from_anki_number(number)

    def _unit_from_anki_number(self, anki_number: str) -> str:
        try:
            number = int(anki_number)
        except ValueError:
            return "Unit 01"

        unit_number = ((number - 601) // 20) + 1
        unit_number = min(max(unit_number, 1), 30)
        return f"Unit {unit_number:02d}"

    def _upsert_seed_deck(self, source_unit: str) -> tuple[Deck, bool]:
        deck = self.db.scalar(
            select(Deck).where(
                Deck.source_name == SOURCE_NAME,
                Deck.source_unit == source_unit,
            ),
        )
        created = deck is None
        if deck is None:
            deck = Deck(source_name=SOURCE_NAME, source_unit=source_unit)
            self.db.add(deck)

        deck.user_id = None
        deck.name = source_unit
        deck.description = SOURCE_NAME
        deck.tags = self._deck_tags(source_unit)
        deck.is_public = True
        deck.is_seed = True
        deck.is_read_only = True
        deck.extra_metadata = {
            "source": "anki_apkg",
            "book": "book2",
            "words_per_unit": 20,
        }
        self.db.flush()
        return deck, created

    def _upsert_vocabulary_item(
        self,
        deck: Deck,
        parsed: ParsedVocabulary,
    ) -> tuple[VocabularyItem, bool]:
        item = self.db.scalar(
            select(VocabularyItem).where(
                VocabularyItem.deck_id == deck.id,
                VocabularyItem.source_key == parsed.source_key,
            ),
        )
        created = item is None
        if item is None:
            item = VocabularyItem(deck_id=deck.id)
            self.db.add(item)

        item.word = parsed.word
        item.pronunciation = parsed.pronunciation
        item.meaning = parsed.meaning
        item.description = parsed.description
        item.example = parsed.example
        item.collocation = None
        item.related_words = None
        item.note = parsed.note
        item.suggestion = parsed.suggestion
        item.anki_number = parsed.anki_number
        item.source_key = parsed.source_key
        item.source_name = SOURCE_NAME
        item.source_unit = parsed.source_unit
        item.image_url = parsed.image_url
        item.word_audio_url = parsed.word_audio_url
        item.meaning_audio_url = parsed.meaning_audio_url
        item.example_audio_url = parsed.example_audio_url
        self.db.flush()
        return item, created

    def _deck_tags(self, source_unit: str) -> list[str]:
        unit_slug = source_unit.lower().replace(" ", "-")
        return ["4000-essential", "book-2", unit_slug, "seed"]

    def _media_url_from_html(self, value: str, media_lookup: dict[str, str]) -> str | None:
        match = IMAGE_RE.search(value)
        if match is None:
            return None
        return self._lookup_media_url(match.group(1), media_lookup)

    def _media_url_from_sound(self, value: str, media_lookup: dict[str, str]) -> str | None:
        match = SOUND_RE.search(value)
        if match is None:
            return None
        return self._lookup_media_url(match.group(1), media_lookup)

    def _lookup_media_url(self, filename: str, media_lookup: dict[str, str]) -> str | None:
        safe_filename = self._safe_media_filename(filename)
        if safe_filename is None:
            return None
        return media_lookup.get(safe_filename)

    def _media_url(self, filename: str) -> str:
        return f"{self.media_url_prefix}/{filename}"

    def _safe_media_filename(self, filename: str) -> str | None:
        normalized = filename.replace("\\", "/").strip()
        if not normalized:
            return None
        safe_filename = Path(normalized).name
        if safe_filename in {"", ".", ".."}:
            return None
        return safe_filename

    def _clean_text_or_none(self, value: str) -> str | None:
        cleaned = self._clean_text(value)
        return cleaned or None

    def _clean_text(self, value: str) -> str:
        text = html.unescape(self._replace_cloze(value))
        text = text.replace("\xa0", " ")
        text = HTML_TAG_RE.sub(" ", text)
        return " ".join(text.split()).strip()

    def _replace_cloze(self, value: str) -> str:
        previous = None
        current = value
        while previous != current:
            previous = current
            current = CLOZE_RE.sub(r"\1", current)
        return current

    def _unit_sort_key(self, source_unit: str) -> tuple[int, str]:
        match = re.search(r"(\d+)", source_unit)
        if match is None:
            return (999, source_unit)
        return (int(match.group(1)), source_unit)
