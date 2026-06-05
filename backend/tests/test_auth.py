from fastapi.testclient import TestClient
from sqlalchemy import select

from app.models.user import User


def test_verify_email_with_otp(client: TestClient, monkeypatch) -> None:
    from app.services.auth_service import AuthService

    monkeypatch.setattr(AuthService, "_generate_otp", staticmethod(lambda: "123456"))
    register_response = client.post(
        "/api/v1/auth/register",
        json={
            "email": "verify@example.com",
            "password": "secret123",
            "name": "Verify Me",
        },
    )
    assert register_response.status_code == 200

    verify_response = client.post(
        "/api/v1/auth/verify-email",
        json={"email": "verify@example.com", "otp": "123456"},
    )

    assert verify_response.status_code == 200
    assert verify_response.json()["access_token"]


def test_verify_email_rejects_wrong_otp(client: TestClient, monkeypatch) -> None:
    from app.services.auth_service import AuthService

    monkeypatch.setattr(AuthService, "_generate_otp", staticmethod(lambda: "123456"))
    client.post(
        "/api/v1/auth/register",
        json={
            "email": "wrong-otp@example.com",
            "password": "secret123",
            "name": "Wrong Otp",
        },
    )

    response = client.post(
        "/api/v1/auth/verify-email",
        json={"email": "wrong-otp@example.com", "otp": "000000"},
    )

    assert response.status_code == 400
    assert response.json()["code"] == "INVALID_EMAIL_OTP"


def test_forgot_password_and_reset_password(client: TestClient, monkeypatch) -> None:
    from app.services.auth_service import AuthService

    monkeypatch.setattr(AuthService, "_generate_otp", staticmethod(lambda: "654321"))
    client.post(
        "/api/v1/auth/register",
        json={
            "email": "reset@example.com",
            "password": "oldpass123",
            "name": "Reset Me",
        },
    )

    forgot_response = client.post(
        "/api/v1/auth/forgot-password",
        json={"email": "reset@example.com"},
    )
    assert forgot_response.status_code == 200

    reset_response = client.post(
        "/api/v1/auth/reset-password",
        json={
            "email": "reset@example.com",
            "otp": "654321",
            "new_password": "newpass123",
        },
    )
    assert reset_response.status_code == 200

    login_response = client.post(
        "/api/v1/auth/login",
        json={"email": "reset@example.com", "password": "newpass123"},
    )
    assert login_response.status_code == 200


def test_register_login_refresh_me_update_and_logout(client: TestClient) -> None:
    register_response = client.post(
        "/api/v1/auth/register",
        json={
            "email": "Learner@example.com",
            "password": "secret123",
            "name": "Min Learner",
        },
    )

    assert register_response.status_code == 200
    register_body = register_response.json()
    assert register_body["access_token"]
    assert register_body["refresh_token"]
    assert register_body["user"] == {
        "id": 1,
        "email": "learner@example.com",
        "name": "Min Learner",
        "goal": None,
        "level": None,
        "daily_new_words": 10,
    }

    duplicate_response = client.post(
        "/api/v1/auth/register",
        json={
            "email": "learner@example.com",
            "password": "secret123",
            "name": "Other Learner",
        },
    )
    assert duplicate_response.status_code == 400
    assert duplicate_response.json()["code"] == "EMAIL_ALREADY_EXISTS"
    _verify_user_in_db(client, "learner@example.com")

    login_response = client.post(
        "/api/v1/auth/login",
        json={"email": "learner@example.com", "password": "secret123"},
    )
    assert login_response.status_code == 200
    login_body = login_response.json()
    access_token = login_body["access_token"]
    refresh_token = login_body["refresh_token"]

    me_response = client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert me_response.status_code == 200
    assert me_response.json()["email"] == "learner@example.com"

    update_response = client.patch(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {access_token}"},
        json={
            "goal": "General English",
            "level": "A1 Beginner",
            "daily_new_words": 12,
        },
    )
    assert update_response.status_code == 200
    assert update_response.json()["goal"] == "General English"
    assert update_response.json()["level"] == "A1 Beginner"
    assert update_response.json()["daily_new_words"] == 12

    refresh_response = client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token},
    )
    assert refresh_response.status_code == 200
    assert refresh_response.json()["access_token"]

    logout_response = client.post(
        "/api/v1/auth/logout",
        json={"refresh_token": refresh_token},
    )
    assert logout_response.status_code == 204

    revoked_refresh_response = client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token},
    )
    assert revoked_refresh_response.status_code == 401
    assert revoked_refresh_response.json()["code"] == "INVALID_REFRESH_TOKEN"


def test_login_rejects_invalid_credentials(client: TestClient) -> None:
    client.post(
        "/api/v1/auth/register",
        json={
            "email": "learner@example.com",
            "password": "secret123",
            "name": "Min Learner",
        },
    )

    response = client.post(
        "/api/v1/auth/login",
        json={"email": "learner@example.com", "password": "wrong-password"},
    )

    assert response.status_code == 401
    assert response.json() == {
        "detail": "Email or password is incorrect.",
        "code": "INVALID_CREDENTIALS",
    }


def test_me_requires_bearer_token(client: TestClient) -> None:
    response = client.get("/api/v1/users/me")

    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHORIZED"


def test_google_login_reports_not_configured(client: TestClient, monkeypatch) -> None:
    from app.core.config import get_settings
    monkeypatch.setenv("GOOGLE_CLIENT_ID", "")
    get_settings.cache_clear()
    try:
        response = client.post("/api/v1/auth/google", json={"id_token": "token"})
        assert response.status_code == 400
        assert response.json() == {
            "detail": "Google Client ID is not configured.",
            "code": "GOOGLE_LOGIN_NOT_CONFIGURED",
        }
    finally:
        get_settings.cache_clear()


def _verify_user_in_db(client: TestClient, email: str) -> None:
    session_local = client.app.state.testing_session_local
    with session_local() as db:
        user = db.scalar(select(User).where(User.email == email))
        assert user is not None
        user.email_verified = True
        db.commit()
