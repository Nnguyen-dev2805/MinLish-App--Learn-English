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


def test_analytics_endpoints_require_auth(client: TestClient) -> None:
    response = client.get("/api/v1/analytics/dashboard")

    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHORIZED"


def test_new_user_analytics_returns_empty_values(client: TestClient) -> None:
    headers = _auth_headers(client)

    dashboard = client.get("/api/v1/analytics/dashboard", headers=headers)
    activity = client.get("/api/v1/analytics/activity", headers=headers)
    retention = client.get("/api/v1/analytics/retention", headers=headers)

    assert dashboard.status_code == 200
    assert dashboard.json() == {
        "learned_words": 0,
        "due_today": 0,
        "streak": 0,
        "accuracy": 0.0,
        "level_estimation": None,
    }
    assert activity.status_code == 200
    days = activity.json()["days"]
    assert len(days) == 7
    assert all(day["review_count"] == 0 for day in days)
    assert all(day["correct_count"] == 0 for day in days)
    assert days[-1]["date"] == datetime.now(timezone.utc).date().isoformat()
    assert retention.status_code == 200
    assert retention.json() == {
        "retention_rate": 0.0,
        "total_reviews": 0,
        "retained_reviews": 0,
    }


def test_analytics_after_good_and_again_reviews(client: TestClient) -> None:
    item_ids = _seed_public_items(client, count=2)
    headers = _auth_headers(client)

    good_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": item_ids[0], "rating": "Good", "response_ms": 1200},
    )
    again_response = client.post(
        "/api/v1/learning/reviews",
        headers=headers,
        json={"vocabulary_item_id": item_ids[1], "rating": "Again", "response_ms": 2400},
    )
    assert good_response.status_code == 200
    assert again_response.status_code == 200

    dashboard = client.get("/api/v1/analytics/dashboard", headers=headers)
    activity = client.get("/api/v1/analytics/activity", headers=headers)
    retention = client.get("/api/v1/analytics/retention", headers=headers)

    assert dashboard.status_code == 200
    dashboard_body = dashboard.json()
    assert dashboard_body["learned_words"] == 2
    assert dashboard_body["due_today"] == 0
    assert dashboard_body["streak"] >= 1
    assert dashboard_body["accuracy"] == 50.0
    assert dashboard_body["level_estimation"] == "Beginner"

    today = datetime.now(timezone.utc).date().isoformat()
    today_activity = activity.json()["days"][-1]
    assert today_activity == {
        "date": today,
        "review_count": 2,
        "correct_count": 1,
    }
    assert retention.json() == {
        "retention_rate": 50.0,
        "total_reviews": 2,
        "retained_reviews": 1,
    }


def test_activity_groups_last_seven_days(client: TestClient) -> None:
    item_ids = _seed_public_items(client, count=1)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "analytics@example.com")
    now = datetime.now(timezone.utc)
    two_days_ago = now - timedelta(days=2)

    with _session(client) as db:
        db.add_all(
            [
                ReviewLog(
                    user_id=user_id,
                    vocabulary_item_id=item_ids[0],
                    rating="Good",
                    is_correct=True,
                    response_ms=1000,
                    ease_factor_after=2.5,
                    next_due_at=now + timedelta(days=1),
                    created_at=now,
                ),
                ReviewLog(
                    user_id=user_id,
                    vocabulary_item_id=item_ids[0],
                    rating="Again",
                    is_correct=False,
                    response_ms=2000,
                    ease_factor_after=2.2,
                    next_due_at=two_days_ago + timedelta(minutes=10),
                    created_at=two_days_ago,
                ),
            ],
        )
        db.commit()

    response = client.get("/api/v1/analytics/activity", headers=headers)

    assert response.status_code == 200
    days = response.json()["days"]
    assert len(days) == 7
    counts_by_date = {day["date"]: day for day in days}
    assert counts_by_date[now.date().isoformat()]["review_count"] == 1
    assert counts_by_date[now.date().isoformat()]["correct_count"] == 1
    assert counts_by_date[two_days_ago.date().isoformat()]["review_count"] == 1
    assert counts_by_date[two_days_ago.date().isoformat()]["correct_count"] == 0


def test_dashboard_streak_uses_nearest_recent_review_day(client: TestClient) -> None:
    item_ids = _seed_public_items(client, count=1)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "analytics@example.com")
    now = datetime.now(timezone.utc)
    yesterday = now - timedelta(days=1)
    two_days_ago = now - timedelta(days=2)

    with _session(client) as db:
        db.add_all(
            [
                ReviewLog(
                    user_id=user_id,
                    vocabulary_item_id=item_ids[0],
                    rating="Good",
                    is_correct=True,
                    response_ms=1000,
                    ease_factor_after=2.5,
                    next_due_at=yesterday + timedelta(days=1),
                    created_at=yesterday,
                ),
                ReviewLog(
                    user_id=user_id,
                    vocabulary_item_id=item_ids[0],
                    rating="Good",
                    is_correct=True,
                    response_ms=1000,
                    ease_factor_after=2.5,
                    next_due_at=two_days_ago + timedelta(days=1),
                    created_at=two_days_ago,
                ),
            ],
        )
        db.commit()

    response = client.get("/api/v1/analytics/dashboard", headers=headers)

    assert response.status_code == 200
    assert response.json()["streak"] == 2


def test_dashboard_level_estimation_thresholds(client: TestClient) -> None:
    item_ids = _seed_public_items(client, count=200)
    headers = _auth_headers(client)
    user_id = _user_id_for_email(client, "analytics@example.com")
    now = datetime.now(timezone.utc)

    with _session(client) as db:
        db.add_all(
            UserWordProgress(
                user_id=user_id,
                vocabulary_item_id=item_id,
                repetitions=1,
                interval_days=1,
                ease_factor=2.5,
                due_at=now + timedelta(days=1),
                last_reviewed_at=now,
                status="learning",
            )
            for item_id in item_ids
        )
        db.commit()

    response = client.get("/api/v1/analytics/dashboard", headers=headers)

    assert response.status_code == 200
    assert response.json()["learned_words"] == 200
    assert response.json()["level_estimation"] == "Intermediate"


def _auth_headers(client: TestClient, email: str = "analytics@example.com") -> dict[str, str]:
    response = client.post(
        "/api/v1/auth/register",
        json={
            "email": email,
            "password": "secret123",
            "name": "Analytics Learner",
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


def _seed_public_items(client: TestClient, count: int) -> list[int]:
    with _session(client) as db:
        return _seed_public_items_in_session(db, count)


def _session(client: TestClient):
    return client.app.state.testing_session_local()


def _seed_public_items_in_session(db: Session, count: int) -> list[int]:
    deck = Deck(
        user_id=None,
        name="Unit 01",
        description=SOURCE_NAME,
        tags=["4000-essential", "book-2", "unit-01", "seed"],
        is_public=True,
        is_seed=True,
        is_read_only=True,
        source_name=SOURCE_NAME,
        source_unit="Unit 01",
    )
    db.add(deck)
    db.flush()

    item_ids: list[int] = []
    for index in range(count):
        item = VocabularyItem(
            deck_id=deck.id,
            word=f"word-{index + 1}",
            meaning=f"meaning-{index + 1}",
            anki_number=str(601 + index),
            source_key=f"analytics-{index + 1}",
            source_name=SOURCE_NAME,
            source_unit="Unit 01",
        )
        db.add(item)
        db.flush()
        item_ids.append(item.id)
    db.commit()
    return item_ids
