from fastapi.testclient import TestClient


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
        "detail": "Email hoặc mật khẩu không đúng.",
        "code": "INVALID_CREDENTIALS",
    }


def test_me_requires_bearer_token(client: TestClient) -> None:
    response = client.get("/api/v1/users/me")

    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHORIZED"


def test_google_login_reports_not_configured(client: TestClient) -> None:
    response = client.post("/api/v1/auth/google", json={"id_token": "token"})

    assert response.status_code == 400
    assert response.json() == {
        "detail": "Google login chưa được cấu hình trong v1.",
        "code": "GOOGLE_LOGIN_NOT_CONFIGURED",
    }
