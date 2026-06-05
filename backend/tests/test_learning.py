from __future__ import annotations

from datetime import datetime, timedelta, timezone

from fastapi.testclient import TestClient
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.deck import Deck
from app.models.progress import ReviewLog, UserWordProgress
from app.models.user import User
from app.models.vocabulary_item import VocabularyItem
from app.services.anki_import_service import SOURCE_NAME


def test_learning_endpoints_require_auth(client: TestClient) -> None:
    response = client.get("/api/v1/learning/daily-plan")

    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHORIZED"


def test_daily_plan_for_new_user_counts_seed_words(client: TestClient) -> None:
    _seed_anki_decks(client)
    headers = _auth_headers(client)

    response = client.get("/api/v1/learning/daily-plan", headers=headers)

    assert response.status_code == 200
    assert response.json() == {
        "daily_goal": 10,
        "new_cards": 10,
        "due_reviews": 0,
        "total_available": 600,
    }


def test_review_cards_returns_seed_cards_without_creating_progress(client: TestClient) -> None:
    _seed_anki_decks(client)
    headers = _auth_headers(client)

    response = client.get("/api/v1/learning/review-cards", headers=headers)

    assert response.status_code == 200
    items = response.json()["items"]
    assert len(items) == 20
    anxious = items[0]
    assert anxious["word"] == "anxious"
    assert anxious["meaning"] == "lo âu, băn khoăn"
    assert anxious["is_new"] is True
    assert anxious["due_at"] is None

    with _session(client) as db:
        assert db.scalar(select(UserWordProgress).limit(1)) is None


def test_deck_all_review_cards_returns_every_word_in_deck(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    anxious_id = seed_ids["anxious_item_id"]

    submit_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 1800},
    )
    assert submit_response.status_code == 200

    response = client.get(
        f"/api/v1/learning/review-cards?deck_id={seed_ids['Unit 01']}&mode=deck_all",
        headers=headers,
    )

    assert response.status_code == 200
    items = response.json()["items"]
    assert len(items) == 20
    assert items[0]["id"] == anxious_id
    assert items[0]["word"] == "anxious"
    assert items[0]["is_new"] is False
    assert items[0]["due_at"] is not None
    assert items[1]["is_new"] is True


def test_submit_good_creates_progress_and_review_log(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    anxious_id = seed_ids["anxious_item_id"]

    response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 3500},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["vocabulary_item_id"] == anxious_id
    assert body["rating"] == "Good"
    assert body["is_correct"] is True
    assert body["repetitions"] == 2
    assert body["interval_days"] == 4
    assert body["ease_factor"] == 2.5
    assert body["next_due_at"]

    with _session(client) as db:
        progress = db.scalar(
            select(UserWordProgress).where(UserWordProgress.vocabulary_item_id == anxious_id),
        )
        assert progress is not None
        assert progress.repetitions == 2
        assert progress.interval_days == 4
        assert progress.status == "good"
        review_logs = db.scalars(select(ReviewLog)).all()
        assert len(review_logs) == 1
        assert review_logs[0].rating == "Good"
        assert review_logs[0].is_correct is True


def test_submit_again_keeps_existing_level_and_schedules_soon(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    anxious_id = seed_ids["anxious_item_id"]

    good_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 2500},
    )
    assert good_response.status_code == 200

    before_again = datetime.now(timezone.utc)
    again_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Again", "response_ms": 4200},
    )
    after_again = datetime.now(timezone.utc)

    assert again_response.status_code == 200
    body = again_response.json()
    assert body["rating"] == "Again"
    assert body["is_correct"] is False
    assert body["repetitions"] == 2
    assert body["interval_days"] == 4

    next_due_at = datetime.fromisoformat(body["next_due_at"])
    assert before_again + timedelta(minutes=9, seconds=50) <= next_due_at
    assert next_due_at <= after_again + timedelta(minutes=10, seconds=10)

    with _session(client) as db:
        progress = db.scalar(
            select(UserWordProgress).where(UserWordProgress.vocabulary_item_id == anxious_id),
        )
        assert progress is not None
        assert progress.repetitions == 2
        assert progress.interval_days == 4
        assert progress.status == "good"
        assert len(db.scalars(select(ReviewLog)).all()) == 2


def test_submit_easy_for_new_word_creates_easy_progress(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    anxious_id = seed_ids["anxious_item_id"]

    response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Easy", "response_ms": 1800},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["rating"] == "Easy"
    assert body["interval_days"] == 7

    with _session(client) as db:
        progress = db.scalar(
            select(UserWordProgress).where(UserWordProgress.vocabulary_item_id == anxious_id),
        )
        assert progress is not None
        assert progress.status == "easy"
        assert progress.interval_days == 7


def test_review_cards_mode_new_returns_only_unlearned_cards(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)

    client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": seed_ids["anxious_item_id"], "rating": "Good", "response_ms": 1500},
    )

    response = client.get("/api/v1/learning/review-cards?mode=new", headers=headers)

    assert response.status_code == 200
    items = response.json()["items"]
    assert len(items) == 10
    assert seed_ids["anxious_item_id"] not in [item["id"] for item in items]
    assert all(item["is_new"] for item in items)


def test_review_cards_mode_new_respects_daily_new_words(client: TestClient) -> None:
    _seed_anki_decks(client)
    headers = _auth_headers(client)

    with _session(client) as db:
        user = db.scalar(select(User).where(User.email == "learner@example.com"))
        assert user is not None
        user.daily_new_words = 5
        db.commit()

    response = client.get("/api/v1/learning/review-cards?mode=new", headers=headers)

    assert response.status_code == 200
    assert len(response.json()["items"]) == 5


def test_review_cards_mode_due_returns_only_due_non_mastered_cards(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "learner@example.com")
    anxious_id = seed_ids["anxious_item_id"]

    with _session(client) as db:
        mastered_item = db.scalar(
            select(VocabularyItem).where(VocabularyItem.word == "word-602"),
        )
        assert mastered_item is not None
        mastered_id = mastered_item.id
        db.add_all(
            [
                UserWordProgress(
                    user_id=user_id,
                    vocabulary_item_id=anxious_id,
                    repetitions=1,
                    interval_days=1,
                    ease_factor=2.5,
                    due_at=datetime.now(timezone.utc) - timedelta(minutes=1),
                    status="hard",
                ),
                UserWordProgress(
                    user_id=user_id,
                    vocabulary_item_id=mastered_id,
                    repetitions=3,
                    interval_days=3650,
                    ease_factor=2.5,
                    due_at=datetime.now(timezone.utc) - timedelta(minutes=1),
                    status="mastered",
                ),
            ],
        )
        db.commit()

    response = client.get("/api/v1/learning/review-cards?mode=due", headers=headers)

    assert response.status_code == 200
    ids = [item["id"] for item in response.json()["items"]]
    assert anxious_id in ids
    assert mastered_id not in ids


def test_review_due_correct_answers_upgrade_level_until_mastered(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "learner@example.com")
    anxious_id = seed_ids["anxious_item_id"]

    with _session(client) as db:
        db.add(
            UserWordProgress(
                user_id=user_id,
                vocabulary_item_id=anxious_id,
                repetitions=1,
                interval_days=2,
                ease_factor=2.5,
                due_at=datetime.now(timezone.utc) - timedelta(minutes=1),
                status="hard",
            ),
        )
        db.commit()

    first_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 1200},
    )
    assert first_response.status_code == 200
    assert first_response.json()["interval_days"] == 4

    second_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 1100},
    )
    assert second_response.status_code == 200
    assert second_response.json()["interval_days"] == 7

    third_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": anxious_id, "rating": "Good", "response_ms": 1000},
    )
    assert third_response.status_code == 200
    assert third_response.json()["interval_days"] == 3650

    with _session(client) as db:
        progress = db.scalar(
            select(UserWordProgress).where(UserWordProgress.vocabulary_item_id == anxious_id),
        )
        assert progress is not None
        assert progress.status == "mastered"


def test_review_cards_returns_due_cards_first(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "learner@example.com")
    anxious_id = seed_ids["anxious_item_id"]

    with _session(client) as db:
        progress = UserWordProgress(
            user_id=user_id,
            vocabulary_item_id=anxious_id,
            repetitions=2,
            interval_days=6,
            ease_factor=2.5,
            due_at=datetime.now(timezone.utc) - timedelta(minutes=1),
            status="review",
        )
        db.add(progress)
        db.commit()

    response = client.get("/api/v1/learning/review-cards", headers=headers)

    assert response.status_code == 200
    first_item = response.json()["items"][0]
    assert first_item["id"] == anxious_id
    assert first_item["is_new"] is False
    assert first_item["due_at"] is not None


def test_user_cannot_review_private_item_owned_by_another_user(client: TestClient) -> None:
    owner_headers = _auth_headers(client, email="owner@example.com")
    other_headers = _auth_headers(client, email="other@example.com")
    deck_response = client.post(
        "/api/v1/decks",
        headers=owner_headers,
        json={"name": "Private deck", "description": None, "tags": []},
    )
    assert deck_response.status_code == 201
    deck_id = deck_response.json()["id"]
    item_response = client.post(
        f"/api/v1/decks/{deck_id}/items",
        headers=owner_headers,
        json={"word": "private", "meaning": "riêng tư"},
    )
    assert item_response.status_code == 201
    item_id = item_response.json()["id"]

    response = client.post(
        "/api/v1/learning/reviews",
        headers=other_headers,
        json={"vocabulary_item_id": item_id, "rating": "Good", "response_ms": 1000},
    )

    assert response.status_code == 404
    assert response.json()["code"] == "NOT_FOUND"


def test_invalid_rating_returns_validation_error(client: TestClient) -> None:
    seed_ids = _seed_anki_decks(client)
    headers = _auth_headers(client)

    response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": seed_ids["anxious_item_id"], "rating": "good"},
    )

    assert response.status_code == 422
    assert response.json()["code"] == "VALIDATION_ERROR"


def _auth_headers(client: TestClient, email: str = "learner@example.com") -> dict[str, str]:
    response = client.post(
        "/api/v1/auth/register",
        json={
            "email": email,
            "password": "secret123",
            "name": "Min Learner",
        },
    )
    assert response.status_code == 200
    _verify_user_in_db(client, email)
    return {"Authorization": f"Bearer {response.json()['access_token']}"}


def _verify_user_in_db(client: TestClient, email: str) -> None:
    with _session(client) as db:
        user = db.scalar(select(User).where(User.email == email))
        assert user is not None
        user.email_verified = True
        db.commit()


def _user_id_for_email(client: TestClient, email: str) -> int:
    with _session(client) as db:
        user = db.scalar(select(User).where(User.email == email))
        assert user is not None
        return user.id


def _seed_anki_decks(client: TestClient) -> dict[str, int]:
    with _session(client) as db:
        return _seed_anki_decks_in_session(db)


def _session(client: TestClient):
    return client.app.state.testing_session_local()


def _seed_anki_decks_in_session(db: Session) -> dict[str, int]:
    seed_ids: dict[str, int] = {}
    for unit_number in range(1, 31):
        source_unit = f"Unit {unit_number:02d}"
        deck = Deck(
            user_id=None,
            name=source_unit,
            description=SOURCE_NAME,
            tags=["4000-essential", "book-2", f"unit-{unit_number:02d}", "seed"],
            is_public=True,
            is_seed=True,
            is_read_only=True,
            source_name=SOURCE_NAME,
            source_unit=source_unit,
        )
        db.add(deck)
        db.flush()
        seed_ids[source_unit] = deck.id

        for index in range(1, 21):
            anki_number = 600 + ((unit_number - 1) * 20) + index
            if unit_number == 1 and index == 1:
                anxious = _anxious_item(deck.id)
                db.add(anxious)
                db.flush()
                seed_ids["anxious_item_id"] = anxious.id
            else:
                db.add(
                    VocabularyItem(
                        deck_id=deck.id,
                        word=f"word-{anki_number}",
                        meaning=f"meaning-{anki_number}",
                        anki_number=str(anki_number),
                        source_key=f"book2-{anki_number}",
                        source_name=SOURCE_NAME,
                        source_unit=source_unit,
                    ),
                )
    db.commit()
    return seed_ids


def _anxious_item(deck_id: int) -> VocabularyItem:
    return VocabularyItem(
        deck_id=deck_id,
        word="anxious",
        pronunciation="['æŋ(k)ʃəs]",
        meaning="lo âu, băn khoăn",
        description="When a person is anxious, they worry that something bad will happen.",
        example="She was anxious about not making her appointment on time.",
        note="tính từ",
        suggestion="a__x__ __ __ __",
        anki_number="601",
        source_key="book2-601",
        source_name=SOURCE_NAME,
        source_unit="Unit 01",
        image_url="/static/media/anki/book2/4000B2_601.jpg",
        word_audio_url="/static/media/anki/book2/4000B2_anxious.mp3",
        meaning_audio_url="/static/media/anki/book2/4000B2_anxious_meaning.mp3",
        example_audio_url="/static/media/anki/book2/4000B2_anxious_example.mp3",
    )
