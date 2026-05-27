from __future__ import annotations

import json
from pathlib import Path
import sqlite3
from zipfile import ZipFile

from sqlalchemy import create_engine, select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.db.base import Base
from app.models.deck import Deck
from app.models.vocabulary_item import VocabularyItem
from app.services.anki_import_service import AnkiImportService

FIELD_SEPARATOR = "\x1f"


def test_import_anki_apkg_maps_anxious_and_is_idempotent(tmp_path: Path) -> None:
    apkg_path = _build_sample_apkg(tmp_path)
    session = _create_session()

    first_result = AnkiImportService(session, static_root=tmp_path / "static").import_apkg(apkg_path)
    second_result = AnkiImportService(session, static_root=tmp_path / "static").import_apkg(apkg_path)

    assert first_result.total_notes == 1
    assert first_result.decks_created == 1
    assert first_result.items_created == 1
    assert first_result.media_entries == 4
    assert first_result.media_extracted == 4
    assert second_result.decks_created == 0
    assert second_result.decks_updated == 1
    assert second_result.items_created == 0
    assert second_result.items_updated == 1

    decks = session.scalars(select(Deck)).all()
    items = session.scalars(select(VocabularyItem)).all()
    assert len(decks) == 1
    assert len(items) == 1

    deck = decks[0]
    item = items[0]
    assert deck.name == "Unit 01"
    assert deck.user_id is None
    assert deck.is_public is True
    assert deck.is_seed is True
    assert deck.is_read_only is True
    assert item.word == "anxious"
    assert item.meaning == "lo âu, băn khoăn"
    assert item.pronunciation == "['æŋ(k)ʃəs]"
    assert item.description == "When a person is anxious, they worry that something bad will happen."
    assert item.example == "She was anxious about not making her appointment on time."
    assert item.image_url == "/static/media/anki/book2/4000B2_601.jpg"
    assert item.word_audio_url == "/static/media/anki/book2/4000B2_anxious.mp3"
    assert item.source_key == "book2-601"
    assert (tmp_path / "static" / "media" / "anki" / "book2" / "4000B2_601.jpg").exists()


def _create_session():
    engine = create_engine(
        "sqlite+pysqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(bind=engine)
    TestingSessionLocal = sessionmaker(
        bind=engine,
        autocommit=False,
        autoflush=False,
        expire_on_commit=False,
    )
    return TestingSessionLocal()


def _build_sample_apkg(tmp_path: Path) -> Path:
    collection_path = tmp_path / "collection.anki2"
    connection = sqlite3.connect(collection_path)
    try:
        connection.execute("create table col (decks text not null, models text not null)")
        connection.execute("create table notes (id integer primary key, flds text not null)")
        connection.execute("create table cards (id integer primary key, nid integer not null, did integer not null)")
        connection.execute(
            "insert into col (decks, models) values (?, ?)",
            (json.dumps(_sample_decks()), json.dumps(_sample_models())),
        )
        connection.execute(
            "insert into notes (id, flds) values (?, ?)",
            (1565663345121, FIELD_SEPARATOR.join(_sample_anxious_fields())),
        )
        connection.execute(
            "insert into cards (id, nid, did) values (?, ?, ?)",
            (1, 1565663345121, 1597244024343),
        )
        connection.commit()
    finally:
        connection.close()

    apkg_path = tmp_path / "sample.apkg"
    with ZipFile(apkg_path, "w") as archive:
        archive.write(collection_path, "collection.anki2")
        archive.writestr(
            "media",
            json.dumps(
                {
                    "0": "4000B2_anxious.mp3",
                    "1": "4000B2_anxious_meaning.mp3",
                    "2": "4000B2_anxious_example.mp3",
                    "3": "4000B2_601.jpg",
                },
            ),
        )
        archive.writestr("0", b"word-audio")
        archive.writestr("1", b"meaning-audio")
        archive.writestr("2", b"example-audio")
        archive.writestr("3", b"image")
    return apkg_path


def _sample_decks() -> dict[str, dict[str, str]]:
    return {
        "1597244024342": {"name": "4000 Essential English Words - Book 2"},
        "1597244024343": {"name": "4000 Essential English Words - Book 2::Unit 01"},
    }


def _sample_models() -> dict[str, dict[str, list[dict[str, str]]]]:
    fields = [
        "№",
        "IMG",
        "Sound",
        "Suggestion",
        "Short Vietnamese",
        "Transcription",
        "Keyword",
        "Explanation",
        "Meaning",
        "Example",
        "Full Vietnamese",
    ]
    return {
        "1397924728893": {
            "flds": [{"name": field_name} for field_name in fields],
        },
    }


def _sample_anxious_fields() -> list[str]:
    return [
        "601",
        "<img src='4000B2_601.jpg'>",
        "[sound:4000B2_anxious.mp3]",
        "a__x__ __ __ __",
        "lo âu, băn khoăn",
        "['æŋ(k)ʃəs]",
        "anxious",
        (
            "<div>When a person is {{c1::anxious}}, they worry that something bad will happen.</div>"
            "→ &nbsp;She was {{c1::anxious}} about not making her appointment on time."
        ),
        "[sound:4000B2_anxious_meaning.mp3]",
        "[sound:4000B2_anxious_example.mp3]",
        "<li><b><i>tính từ</i></b> <ul><li>lo âu</li></ul></li>",
    ]
