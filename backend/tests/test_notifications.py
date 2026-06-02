from fastapi.testclient import TestClient
from sqlalchemy import select

from app.models.notification_preference import NotificationPreference
from app.models.user import User


def test_notification_preferences_require_auth(client: TestClient) -> None:
    response = client.get("/api/v1/notifications/preferences")

    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHORIZED"


def test_get_notification_preferences_creates_default(client: TestClient) -> None:
    headers = _auth_headers(client)

    response = client.get("/api/v1/notifications/preferences", headers=headers)

    assert response.status_code == 200
    assert response.json() == {
        "daily_time": "20:00",
        "timezone": "Asia/Ho_Chi_Minh",
        "email_enabled": False,
        "push_enabled": True,
    }

    with _session(client) as db:
        user = db.scalar(select(User).where(User.email == "learner@example.com"))
        assert user is not None
        preference = db.get(NotificationPreference, user.id)
        assert preference is not None
        assert preference.daily_time == "20:00"


def test_patch_notification_preferences_updates_all_fields(client: TestClient) -> None:
    headers = _auth_headers(client)

    response = client.patch(
        "/api/v1/notifications/preferences",
        headers=headers,
        json={
            "daily_time": "21:30",
            "timezone": "Asia/Ho_Chi_Minh",
            "email_enabled": True,
            "push_enabled": False,
        },
    )

    assert response.status_code == 200
    assert response.json() == {
        "daily_time": "21:30",
        "timezone": "Asia/Ho_Chi_Minh",
        "email_enabled": True,
        "push_enabled": False,
    }

    get_response = client.get("/api/v1/notifications/preferences", headers=headers)
    assert get_response.status_code == 200
    assert get_response.json() == response.json()


def test_patch_notification_preferences_partial_update_keeps_old_values(
    client: TestClient,
) -> None:
    headers = _auth_headers(client)
    initial_response = client.patch(
        "/api/v1/notifications/preferences",
        headers=headers,
        json={
            "daily_time": "19:15",
            "timezone": "Asia/Ho_Chi_Minh",
            "email_enabled": True,
            "push_enabled": False,
        },
    )
    assert initial_response.status_code == 200

    response = client.patch(
        "/api/v1/notifications/preferences",
        headers=headers,
        json={"daily_time": "07:05"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "daily_time": "07:05",
        "timezone": "Asia/Ho_Chi_Minh",
        "email_enabled": True,
        "push_enabled": False,
    }


def test_patch_notification_preferences_rejects_invalid_daily_time(
    client: TestClient,
) -> None:
    headers = _auth_headers(client)

    response = client.patch(
        "/api/v1/notifications/preferences",
        headers=headers,
        json={"daily_time": "24:00"},
    )

    assert response.status_code == 422
    assert response.json()["code"] == "VALIDATION_ERROR"


def test_patch_notification_preferences_rejects_blank_timezone(client: TestClient) -> None:
    headers = _auth_headers(client)

    response = client.patch(
        "/api/v1/notifications/preferences",
        headers=headers,
        json={"timezone": "   "},
    )

    assert response.status_code == 422
    assert response.json()["code"] == "VALIDATION_ERROR"


def test_notification_preferences_are_user_scoped(client: TestClient) -> None:
    user_a_headers = _auth_headers(client, email="a@example.com")
    user_b_headers = _auth_headers(client, email="b@example.com")

    response_a = client.patch(
        "/api/v1/notifications/preferences",
        headers=user_a_headers,
        json={"daily_time": "06:45", "push_enabled": False},
    )
    response_b = client.get("/api/v1/notifications/preferences", headers=user_b_headers)

    assert response_a.status_code == 200
    assert response_b.status_code == 200
    assert response_a.json()["daily_time"] == "06:45"
    assert response_a.json()["push_enabled"] is False
    assert response_b.json() == {
        "daily_time": "20:00",
        "timezone": "Asia/Ho_Chi_Minh",
        "email_enabled": False,
        "push_enabled": True,
    }


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


def _session(client: TestClient):
    return client.app.state.testing_session_local()
