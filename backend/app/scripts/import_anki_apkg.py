from __future__ import annotations

import argparse
from pathlib import Path

from app.db.session import SessionLocal
from app.services.anki_import_service import AnkiImportService


def main() -> None:
    parser = argparse.ArgumentParser(description="Import MinLish seed data from an Anki .apkg file.")
    parser.add_argument("apkg_path", type=Path, help="Path to the Anki .apkg file")
    args = parser.parse_args()

    db = SessionLocal()
    try:
        result = AnkiImportService(db).import_apkg(args.apkg_path)
    finally:
        db.close()

    print("Anki import completed")
    print(f"Total notes: {result.total_notes}")
    print(f"Decks created: {result.decks_created}")
    print(f"Decks updated: {result.decks_updated}")
    print(f"Items created: {result.items_created}")
    print(f"Items updated: {result.items_updated}")
    print(f"Media entries: {result.media_entries}")
    print(f"Media extracted: {result.media_extracted}")
    print(f"Media skipped: {result.media_skipped}")
    print(f"Failed rows: {len(result.failed_rows)}")
    for failed_row in result.failed_rows[:10]:
        print(f"- note_id={failed_row.note_id}: {failed_row.reason}")


if __name__ == "__main__":
    main()
