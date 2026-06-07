import argparse
import hashlib
import html
import json
import re
import shutil
import sqlite3
import tempfile
import zipfile
from pathlib import Path


FIELD_SEPARATOR = "\x1f"
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".gif"}
SOUND_PATTERN = re.compile(r"\[sound:([^\]]+)\]")
IMAGE_PATTERN = re.compile(r"<img[^>]+src=['\"]([^'\"]+)['\"][^>]*>", re.IGNORECASE)


def clean_text(value: str | None) -> str:
    if not value:
        return ""

    text = value
    text = re.sub(r"\[sound:[^\]]+\]", " ", text)
    text = re.sub(r"<img[^>]*>", " ", text, flags=re.IGNORECASE)
    text = re.sub(r"\{\{c\d+::(.*?)(?:::[^}]*)?\}\}", r"\1", text)
    text = re.sub(r"<br\s*/?>", "\n", text, flags=re.IGNORECASE)
    text = re.sub(r"</(div|li|p|ul|ol)>", "\n", text, flags=re.IGNORECASE)
    text = re.sub(r"<[^>]+>", " ", text)
    text = html.unescape(text)
    text = text.replace("\u00a0", " ")
    text = re.sub(r"[ \t\r\f\v]+", " ", text)
    text = re.sub(r"\n\s*", "\n", text)
    return text.strip()


def get_field(fields: dict[str, str], *names: str) -> str:
    for name in names:
        value = clean_text(fields.get(name))
        if value:
            return value
    return ""


def split_explanation(raw_explanation: str) -> tuple[str, str]:
    explanation = clean_text(raw_explanation)
    if "→" not in explanation:
        return explanation, ""

    definition, example = explanation.split("→", 1)
    return definition.strip(), example.strip()


def first_sound_filename(value: str | None) -> str:
    if not value:
        return ""
    match = SOUND_PATTERN.search(value)
    return match.group(1).strip() if match else ""


def first_image_filename(value: str | None) -> str:
    if not value:
        return ""
    match = IMAGE_PATTERN.search(value)
    return match.group(1).strip() if match else ""


def normalize_deck_name(deck_name: str) -> str:
    short_name = deck_name.split("::")[-1].strip()
    match = re.search(r"Unit\s+(\d+)", short_name, flags=re.IGNORECASE)
    if match:
        return f"Unit {int(match.group(1))}: 4000 Essential Words"
    return short_name or deck_name


def safe_asset_filename(filename: str) -> str:
    path = Path(filename)
    stem = re.sub(r"[^A-Za-z0-9._-]+", "_", path.stem).strip("._")
    suffix = path.suffix.lower()
    if not stem:
        stem = hashlib.sha1(filename.encode("utf-8")).hexdigest()[:12]
    return f"{stem}{suffix}"


def extract_media_map(package: zipfile.ZipFile) -> dict[str, str]:
    if "media" not in package.namelist():
        return {}

    raw_media = json.loads(package.read("media").decode("utf-8"))
    return {
        original_name: zip_member
        for zip_member, original_name in raw_media.items()
        if isinstance(original_name, str)
    }


def copy_media_file(
    package: zipfile.ZipFile,
    media_map: dict[str, str],
    filename: str,
    media_output_dir: Path,
    copied_files: dict[str, str],
) -> str:
    if not filename:
        return ""
    if filename in copied_files:
        return copied_files[filename]

    zip_member = media_map.get(filename)
    if zip_member is None and filename in package.namelist():
        zip_member = filename
    if zip_member is None:
        return ""

    media_output_dir.mkdir(parents=True, exist_ok=True)
    asset_name = safe_asset_filename(filename)
    asset_path = media_output_dir / asset_name

    with package.open(zip_member) as source, asset_path.open("wb") as target:
        shutil.copyfileobj(source, target)

    asset_url = f"asset://seed_media/{asset_name}"
    copied_files[filename] = asset_url
    return asset_url


def extract_seed(apkg_path: Path, media_output_dir: Path | None = None) -> list[dict]:
    with zipfile.ZipFile(apkg_path) as package:
        media_map = extract_media_map(package)
        copied_files: dict[str, str] = {}
        collection_name = next(
            (
                name
                for name in package.namelist()
                if name in ("collection.anki2", "collection.anki21")
            ),
            None,
        )
        if collection_name is None:
            raise ValueError("APKG does not contain collection.anki2 or collection.anki21")

        with tempfile.TemporaryDirectory() as temp_dir:
            package.extract(collection_name, temp_dir)
            collection_path = Path(temp_dir) / collection_name
            connection = sqlite3.connect(collection_path)
            connection.row_factory = sqlite3.Row
            try:
                col = connection.execute("SELECT decks, models FROM col").fetchone()
                decks = json.loads(col["decks"])
                models = json.loads(col["models"])

                rows = connection.execute(
                    """
                    SELECT
                        notes.id AS note_id,
                        notes.mid AS model_id,
                        notes.flds AS fields,
                        cards.did AS deck_id
                    FROM notes
                    INNER JOIN cards ON cards.nid = notes.id
                    ORDER BY cards.did, notes.id
                    """
                ).fetchall()
            finally:
                connection.close()

        grouped: dict[str, dict] = {}
        seen_note_ids: set[int] = set()

        for row in rows:
            note_id = int(row["note_id"])
            if note_id in seen_note_ids:
                continue
            seen_note_ids.add(note_id)

            deck = decks.get(str(row["deck_id"]), {})
            deck_name = deck.get("name", "Vocabulary")
            if "::Unit" not in deck_name:
                continue

            model = models[str(row["model_id"])]
            field_names = [field["name"] for field in model.get("flds", [])]
            field_values = str(row["fields"]).split(FIELD_SEPARATOR)
            fields = dict(zip(field_names, field_values))

            word = get_field(fields, "Keyword", "Word", "Front")
            meaning = get_field(fields, "Short Vietnamese", "Meaning", "Full Vietnamese")
            pronunciation = get_field(fields, "Transcription", "Pronunciation")
            definition, example = split_explanation(fields.get("Explanation", ""))
            note = get_field(fields, "Full Vietnamese")
            suggestion = get_field(fields, "Suggestion")

            if not word or not meaning:
                continue

            group_key = deck_name
            if group_key not in grouped:
                grouped[group_key] = {
                    "name": normalize_deck_name(deck_name),
                    "description": f"Vocabulary from {deck_name.split('::')[-1].strip()} of 4000 Essential English Words Book 2.",
                    "tags": ["4000-essential", "book-2"],
                    "sourceName": "4000 Essential English Words - Book 2",
                    "sourceUnit": normalize_deck_name(deck_name),
                    "words": [],
                }

            grouped[group_key]["words"].append(
                {
                    "word": word,
                    "pronunciation": pronunciation,
                    "meaning": meaning,
                    "description": definition,
                    "example": example,
                    "note": note,
                    "suggestion": suggestion,
                    "imageUrl": copy_media_file(
                        package=package,
                        media_map=media_map,
                        filename=first_image_filename(fields.get("IMG")),
                        media_output_dir=media_output_dir,
                        copied_files=copied_files,
                    ) if media_output_dir is not None else "",
                    "wordAudioUrl": copy_media_file(
                        package=package,
                        media_map=media_map,
                        filename=first_sound_filename(fields.get("Sound")),
                        media_output_dir=media_output_dir,
                        copied_files=copied_files,
                    ) if media_output_dir is not None else "",
                    "meaningAudioUrl": copy_media_file(
                        package=package,
                        media_map=media_map,
                        filename=first_sound_filename(fields.get("Meaning")),
                        media_output_dir=media_output_dir,
                        copied_files=copied_files,
                    ) if media_output_dir is not None else "",
                    "exampleAudioUrl": copy_media_file(
                        package=package,
                        media_map=media_map,
                        filename=first_sound_filename(fields.get("Example")),
                        media_output_dir=media_output_dir,
                        copied_files=copied_files,
                    ) if media_output_dir is not None else "",
                }
            )

        return list(grouped.values())


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Extract vocabulary from an Anki APKG file into a simple seed JSON file."
    )
    parser.add_argument(
        "apkg",
        nargs="?",
        default="data/4000_Essential_English_Words_2_-_Vietnamese.apkg",
        help="Path to the source APKG file.",
    )
    parser.add_argument(
        "--out",
        default="app/src/main/assets/seed_vocabulary.json",
        help="Output JSON path used by the Android app assets folder.",
    )
    parser.add_argument(
        "--media-out",
        default="app/src/main/assets/seed_media",
        help="Output folder for APKG image and audio assets.",
    )
    args = parser.parse_args()

    apkg_path = Path(args.apkg)
    output_path = Path(args.out)
    media_output_dir = Path(args.media_out)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if media_output_dir.exists():
        shutil.rmtree(media_output_dir)

    decks = extract_seed(apkg_path, media_output_dir)
    output_path.write_text(
        json.dumps(decks, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    total_words = sum(len(deck["words"]) for deck in decks)
    print(f"Wrote {len(decks)} decks and {total_words} words to {output_path}")
    print(f"Copied media files to {media_output_dir}")


if __name__ == "__main__":
    main()
