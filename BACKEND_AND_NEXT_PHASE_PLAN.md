# BACKEND AND NEXT PHASE IMPLEMENTATION PLAN - MinLish Vocabulary App

## 0. Nguồn đã kiểm tra

### 0.1 Repository hiện tại

Đã đọc và đối chiếu các nguồn chính:

- `IMPLEMENTATION_PLAN.md`
- `MODULE_TASK_PLAN.md`
- `md/PLAN.md`
- `md/content.txt`
- `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`
- `app/src/main/java/com/example/minlishapp_learnenglish/core/network/NetworkConfig.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/api/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/dto/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/repository/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/presentation/viewmodel/auth/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/presentation/viewmodel/home/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/auth/*.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/home/*.kt`
- `app/src/test/java/**/*.kt`

Kết luận trạng thái thực tế của repo:

- Android đã vượt trạng thái skeleton trong `MODULE_TASK_PLAN.md`; hiện đã code xong tới Phase 4.
- Đã có theme MinLish, navigation, auth UI, Home Dashboard, Retrofit, OkHttp, Moshi, token storage, `AppResult`, repositories cho Auth/Learning/Analytics.
- Chưa có thư mục `backend/`, chưa có FastAPI, chưa có PostgreSQL docker compose, chưa có Alembic migration.
- Android đang gọi backend qua `http://10.0.2.2:8000/api/v1/`.

### 0.2 Stitch MCP

Đã gọi Stitch MCP:

- `get_project`: `projects/2325080910252905506`, thành công.
- `get_screen` cho các màn sau Phase 4, thành công:
  - Deck List: `eea728fe28824df78bf25da7960e15e5`
  - Deck Detail: `be5cc20d11714924858e603012302973`
  - Create Deck: `8fe373dcb2f24a669ef5e8d6764d3d74`
  - Add Word Form: `3538149b32844c82be09a358bf4ed6ff`
  - Flashcard Learning: `63b8e048ace140af9cc15f0307816a1b`
  - Review Results: `20ad23ddfc6e4f0ea693322719580830`
  - Progress Analytics: `216a8bf066f9490f80662128bbd449f7`
  - Profile & Settings: `57446103b63a4a40a46e39791cbf7c98`

Design system Stitch cần giữ cho các phase Android tiếp theo:

- Material 3 native Compose, không dùng WebView.
- Background `#f8f9fa`.
- Primary teal `#005e53`, primary container `#00796b`, secondary container `#bbe8e4`.
- Card lớn bo 24dp, input/chip bo 12dp, button pill.
- Layout mobile 390dp, margin 16dp, spacing 8/16/24dp.
- Bottom navigation có active tonal pill.

### 0.3 Phân tích Anki `.apkg`

File: `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`

Kết quả kiểm tra:

- `.apkg` là file zip.
- Có `collection.anki2`.
- Có file `media`.
- Bảng `notes`: 600 records.
- Bảng `cards`: 600 records.
- Bảng `revlog`: 0 records.
- Có 30 decks con có card, mỗi unit 20 cards.
- File `media` có 2401 entries.
- Deck chính: `4000 Essential English Words - Book 2`.
- Unit decks: `4000 Essential English Words - Book 2::Unit 01` đến `Unit 30`.

Anki note model field order:

| Index | Field Anki | Ý nghĩa |
|---|---|---|
| 0 | `№` | Số thứ tự, ví dụ `601` |
| 1 | `IMG` | HTML ảnh, ví dụ `<img src='4000B2_601.jpg'>` |
| 2 | `Sound` | Audio từ, ví dụ `[sound:4000B2_anxious.mp3]` |
| 3 | `Suggestion` | Gợi ý chữ cái, ví dụ `a__x__ __ __ __` |
| 4 | `Short Vietnamese` | Nghĩa tiếng Việt ngắn |
| 5 | `Transcription` | Phiên âm |
| 6 | `Keyword` | Từ tiếng Anh |
| 7 | `Explanation` | Định nghĩa + ví dụ tiếng Anh dạng cloze/HTML |
| 8 | `Meaning` | Audio nghĩa |
| 9 | `Example` | Audio ví dụ |
| 10 | `Full Vietnamese` | Nghĩa tiếng Việt đầy đủ, có HTML |

Sample `anxious`:

- number: `601`
- image: `4000B2_601.jpg`
- word audio: `4000B2_anxious.mp3`
- suggestion: `a__x__ __ __ __`
- short meaning: `lo âu, băn khoăn`
- pronunciation: `['æŋ(k)ʃəs]`
- word: `anxious`
- explanation/example source: `When a person is anxious, they worry that something bad will happen. -> She was anxious about not making her appointment on time.`
- meaning audio: `4000B2_anxious_meaning.mp3`
- example audio: `4000B2_anxious_example.mp3`
- full Vietnamese: có loại từ `tính từ` và nghĩa mở rộng.

## 1. Current State Summary

### 1.1 Android đã hoàn thành

Android đã code xong tới Phase 4:

- Theme MinLish theo Stitch.
- Navigation auth + main tabs.
- `AppContainer` manual dependency injection.
- `AppResult`, `AppError`, `ErrorMapper`.
- Retrofit/OkHttp/Moshi setup.
- Auth interceptor + token authenticator skeleton.
- Token storage bằng encrypted storage.
- Splash, Onboarding, Login, Register native Compose.
- Home Dashboard native Compose.
- ViewModel/use case/repository cho Auth và Home.
- Unit tests hiện có:
  - `ErrorMapperTest`
  - `AuthViewModelTest`
  - `HomeViewModelTest`

Android API đang mong đợi backend:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/google`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `GET /api/v1/learning/daily-plan`
- `GET /api/v1/analytics/dashboard`
- `GET /api/v1/analytics/activity`

### 1.2 Backend đang thiếu

Repo hiện chưa có:

- `backend/`
- FastAPI app.
- PostgreSQL docker compose.
- SQLAlchemy models.
- Alembic migrations.
- Auth endpoints.
- Deck/item endpoints.
- Learning/SRS endpoints.
- Analytics endpoints.
- Notification preferences endpoints.
- Script import Anki `.apkg`.

### 1.3 Vì sao Register/Login báo không kết nối máy chủ

Android đang gọi:

```txt
http://10.0.2.2:8000/api/v1/
```

Trên Android Emulator, `10.0.2.2` trỏ về máy host. Nhưng repo chưa có backend chạy ở port `8000`, nên Retrofit gặp lỗi network và UI hiển thị:

```txt
Không thể kết nối máy chủ.
```

Đây không phải lỗi UI Phase 3/4. Đây là do backend chưa tồn tại/chưa chạy.

## 2. Recommended Implementation Order

Vì Android đã tới Phase 4 nhưng backend chưa có, nên tạm dừng Android Phase 5 và làm backend trước để app chạy thật.

Thứ tự khuyến nghị:

1. Backend Phase 1: FastAPI foundation + PostgreSQL + Alembic.
2. Backend Phase 2: Auth APIs để Login/Register Android chạy thật.
3. Backend Phase 3: Seed Anki data.
4. Backend Phase 4: Deck/item APIs.
5. Backend Phase 5: Learning/SRS APIs.
6. Backend Phase 6: Analytics APIs cho Home Dashboard.
7. Backend Phase 7: Notification preferences.
8. Quay lại Android Phase 5: Deck Management theo Stitch.

Lý do:

- Backend Phase 1-2 unblock Register/Login.
- Backend Phase 3-4 unblock Deck List/Deck Detail.
- Backend Phase 5 unblock Flashcard Learning.
- Backend Phase 6 làm Home Dashboard Phase 4 có data thật.
- Backend Phase 7 chuẩn bị cho Profile/Settings/Notifications.

## 3. Backend Architecture Plan

### 3.1 Stack

- Python 3.12.
- FastAPI.
- Pydantic v2.
- SQLAlchemy 2.x.
- Alembic.
- PostgreSQL.
- PyJWT hoặc python-jose.
- passlib/bcrypt hoặc bcrypt trực tiếp.
- python-multipart cho import file.
- openpyxl cho XLSX.
- pytest + httpx.

Không dùng trong v1:

- Redis.
- Celery.
- GraphQL.
- Microservices.
- Background worker phức tạp.

### 3.2 Thư mục backend

```txt
backend/
  app/
    main.py
    core/
      config.py
      security.py
      errors.py
    db/
      base.py
      session.py
    models/
      user.py
      deck.py
      vocabulary.py
      progress.py
      notification.py
    schemas/
      auth.py
      user.py
      deck.py
      vocabulary.py
      learning.py
      analytics.py
      notification.py
      common.py
    api/
      deps.py
      v1/
        router.py
        auth.py
        users.py
        decks.py
        learning.py
        analytics.py
        notifications.py
    services/
      auth_service.py
      anki_import_service.py
      deck_service.py
      learning_service.py
      analytics_service.py
      notification_service.py
    static/
      media/
        anki/
    scripts/
      import_anki_apkg.py
  alembic/
    versions/
  tests/
    conftest.py
    test_auth.py
    test_anki_import.py
    test_decks.py
    test_learning.py
    test_analytics.py
  alembic.ini
  docker-compose.yml
  pyproject.toml
  .env.example
  README.md
```

### 3.3 Layer responsibilities

- `api/v1`: route mỏng, nhận request, gọi service, trả schema.
- `schemas`: Pydantic request/response đúng Android DTO.
- `models`: SQLAlchemy ORM.
- `services`: business logic: auth, ownership, Anki import, SM-2, analytics.
- `db`: session, base metadata.
- `core`: config, security, error helpers.
- `scripts`: dev command import Anki, không expose trực tiếp cho Android.

## 4. Database Schema Plan

### 4.1 `users`

Columns:

- `id`: bigint PK.
- `email`: varchar, unique, not null, indexed.
- `password_hash`: varchar, nullable cho Google-only account.
- `google_sub`: varchar, unique nullable.
- `name`: varchar nullable.
- `goal`: varchar nullable.
- `level`: varchar nullable.
- `daily_new_words`: int not null default `10`.
- `created_at`: timestamptz not null.

Relationships:

- One-to-many `decks`.
- One-to-many `review_logs`.
- One-to-one `notification_preferences`.
- One-to-many `refresh_tokens`.

Indexes/constraints:

- Unique index `users.email`.
- Unique index `users.google_sub` where not null.

Lý do:

- Khớp Android `UserDto`.
- Cho phép email/password và Google login về sau.

### 4.2 `refresh_tokens`

Columns:

- `id`: bigint PK.
- `user_id`: FK `users.id`, not null.
- `token_hash`: varchar not null, indexed.
- `expires_at`: timestamptz not null.
- `revoked_at`: timestamptz nullable.
- `created_at`: timestamptz not null.

Relationships:

- Many-to-one `users`.

Indexes:

- `refresh_tokens.user_id`.
- `refresh_tokens.token_hash`.

Lý do:

- Logout/refresh token revoke được.
- Không lưu refresh token plain text.

### 4.3 `decks`

Columns:

- `id`: bigint PK.
- `user_id`: FK `users.id`, nullable cho public seed decks.
- `name`: varchar not null.
- `description`: text nullable.
- `tags`: jsonb not null default `[]`.
- `is_public`: bool not null default false.
- `is_seed`: bool not null default false.
- `source_name`: varchar nullable, ví dụ `4000 Essential English Words - Book 2`.
- `source_unit`: varchar nullable, ví dụ `Unit 01`.
- `created_at`: timestamptz not null.
- `updated_at`: timestamptz not null.

Relationships:

- One-to-many `vocabulary_items`.

Indexes/constraints:

- Index `decks.user_id`.
- Index `decks.is_public`.
- Unique `(source_name, source_unit)` cho seed idempotent.

Lý do:

- Hỗ trợ public seed deck read-only và deck user tự tạo.

### 4.4 `vocabulary_items`

Columns:

- `id`: bigint PK.
- `deck_id`: FK `decks.id`, not null.
- `word`: varchar not null.
- `pronunciation`: varchar nullable.
- `meaning`: text not null.
- `description`: text nullable.
- `example`: text nullable.
- `collocation`: text nullable.
- `related_words`: text nullable.
- `note`: text nullable.
- `anki_number`: varchar nullable.
- `suggestion`: varchar nullable.
- `full_vietnamese`: text nullable.
- `image_url`: text nullable.
- `word_audio_url`: text nullable.
- `meaning_audio_url`: text nullable.
- `example_audio_url`: text nullable.
- `source_key`: varchar nullable.
- `created_at`: timestamptz not null.
- `updated_at`: timestamptz not null.

Relationships:

- Many-to-one `decks`.
- One-to-many `user_word_progress`.
- One-to-many `review_logs`.

Indexes/constraints:

- Index `vocabulary_items.deck_id`.
- Index `vocabulary_items.word`.
- Unique `(deck_id, word)` cho user deck.
- Unique `(deck_id, source_key)` nullable cho seed idempotent.

Lý do:

- Bám `md/content.txt`, đồng thời lưu đủ Anki media.
- `part_of_speech` chưa đưa vào v1 vì Android/backend contract chưa chốt; có thể extract sau từ `full_vietnamese`.

### 4.5 `user_word_progress`

Columns:

- `user_id`: FK `users.id`, PK part.
- `vocabulary_item_id`: FK `vocabulary_items.id`, PK part.
- `repetitions`: int not null default `0`.
- `interval_days`: int not null default `0`.
- `ease_factor`: numeric/float not null default `2.5`.
- `due_at`: timestamptz not null default now.
- `last_reviewed_at`: timestamptz nullable.
- `status`: varchar not null default `new`.

Relationships:

- Many-to-one `users`.
- Many-to-one `vocabulary_items`.

Indexes:

- Composite index `(user_id, due_at)`.
- Composite index `(user_id, status)`.

Lý do:

- Query due review nhanh.
- Backend là source of truth cho SM-2.

### 4.6 `review_logs`

Columns:

- `id`: bigint PK.
- `user_id`: FK `users.id`, not null.
- `vocabulary_item_id`: FK `vocabulary_items.id`, not null.
- `rating`: varchar not null: `again|hard|good|easy`.
- `is_correct`: bool not null.
- `response_ms`: int nullable.
- `ease_factor_after`: numeric/float nullable.
- `next_due_at`: timestamptz nullable.
- `created_at`: timestamptz not null.

Indexes:

- Index `review_logs.user_id`.
- Composite index `(user_id, created_at)`.
- Composite index `(user_id, vocabulary_item_id)`.

Lý do:

- Là nguồn tính accuracy, streak, activity, retention.

### 4.7 `study_sessions`

Columns:

- `id`: bigint PK.
- `user_id`: FK `users.id`, not null.
- `started_at`: timestamptz not null.
- `ended_at`: timestamptz nullable.
- `new_count`: int not null default `0`.
- `review_count`: int not null default `0`.
- `correct_count`: int not null default `0`.
- `total_count`: int not null default `0`.

Indexes:

- Composite index `(user_id, started_at)`.

Lý do:

- Có thể dùng sau cho review summary; Android v1 vẫn có thể dùng local summary nếu chưa expose endpoint.

### 4.8 `notification_preferences`

Columns:

- `user_id`: FK `users.id`, PK.
- `daily_time`: time not null default `20:00`.
- `timezone`: varchar not null default `Asia/Ho_Chi_Minh`.
- `email_enabled`: bool not null default false.
- `push_enabled`: bool not null default true.

Relationships:

- One-to-one `users`.

Lý do:

- Backend lưu preference, Android WorkManager local notification xử lý push v1.

## 5. API Contract Matching Android

Common error response:

```json
{
  "detail": "Human readable error message",
  "code": "ERROR_CODE"
}
```

### 5.1 Auth APIs

| Endpoint | Request | Response | Android DTO | Backend schema | Test case |
|---|---|---|---|---|---|
| `POST /api/v1/auth/register` | `{ "email", "password", "name" }` | `{ "access_token", "refresh_token", "user" }` | `RegisterRequestDto`, `AuthResponseDto` | `RegisterRequest`, `AuthResponse` | register success, duplicate email, invalid password |
| `POST /api/v1/auth/login` | `{ "email", "password" }` | `{ "access_token", "refresh_token", "user" }` | `LoginRequestDto`, `AuthResponseDto` | `LoginRequest`, `AuthResponse` | login success, wrong password |
| `POST /api/v1/auth/google` | `{ "id_token" }` | `{ "access_token", "refresh_token", "user" }` | `GoogleLoginRequestDto`, `AuthResponseDto` | `GoogleLoginRequest`, `AuthResponse` | v1 optional/nice-to-have |
| `POST /api/v1/auth/refresh` | `{ "refresh_token" }` | `{ "access_token" }` | `RefreshRequestDto`, `RefreshResponseDto` | `RefreshRequest`, `RefreshResponse` | valid token, revoked token |
| `POST /api/v1/auth/logout` | `{ "refresh_token" }` | empty 200/204 | `LogoutRequestDto` | `LogoutRequest` | revoke token |
| `GET /api/v1/users/me` | none | user | `UserDto` | `UserResponse` | bearer valid/invalid |
| `PATCH /api/v1/users/me` | nullable profile fields | user | `UpdateUserRequestDto`, `UserDto` | `UpdateUserRequest`, `UserResponse` | update goal/level/daily words |

`UserDto` backend phải trả:

```json
{
  "id": 1,
  "email": "student@minlish.app",
  "name": "Student",
  "goal": null,
  "level": null,
  "daily_new_words": 10
}
```

### 5.2 Learning APIs Android hiện có

| Endpoint | Request | Response | Android DTO | Backend schema | Test case |
|---|---|---|---|---|---|
| `GET /api/v1/learning/daily-plan` | none | `{ "daily_goal", "new_cards", "due_reviews", "total_available" }` | `DailyPlanResponseDto` | `DailyPlanResponse` | new user, user có due reviews |

Android Phase sau sẽ cần thêm:

- `GET /api/v1/learning/review-cards`
- `POST /api/v1/learning/reviews`

### 5.3 Analytics APIs Android hiện có

| Endpoint | Request | Response | Android DTO | Backend schema | Test case |
|---|---|---|---|---|---|
| `GET /api/v1/analytics/dashboard` | none | `{ "learned_words", "due_today", "streak", "accuracy", "level_estimation" }` | `DashboardResponseDto` | `DashboardResponse` | new user returns zero, reviewed user returns metrics |
| `GET /api/v1/analytics/activity` | none | `{ "days": [{ "date", "review_count", "correct_count" }] }` | `ActivityResponseDto` | `ActivityResponse` | empty activity, 7-day activity |

### 5.4 Deck APIs Android Phase 5 sẽ cần

Backend nên chuẩn bị đúng `md/PLAN.md`:

- `GET /api/v1/decks`
- `POST /api/v1/decks`
- `GET /api/v1/decks/{deck_id}`
- `PATCH /api/v1/decks/{deck_id}`
- `DELETE /api/v1/decks/{deck_id}`
- `GET /api/v1/decks/{deck_id}/items`
- `POST /api/v1/decks/{deck_id}/items`
- `PATCH /api/v1/items/{item_id}`
- `DELETE /api/v1/items/{item_id}`
- `POST /api/v1/decks/{deck_id}/import`
- `GET /api/v1/decks/{deck_id}/export`

## 6. Anki Import Plan

### 6.1 Cách đọc `.apkg`

`.apkg` là zip:

1. Extract `collection.anki2` ra temp folder.
2. Extract `media` ra JSON map.
3. Đọc SQLite `collection.anki2`.
4. Đọc bảng `notes`, `cards`, `col`.
5. Extract media files theo map sang `backend/app/static/media/anki/book2/`.

Không expose import Anki qua Android v1. Chạy bằng script dev:

```bash
cd backend
python -m app.scripts.import_anki_apkg ../data/4000_Essential_English_Words_2_-_Vietnamese.apkg
```

### 6.2 Mapping deck/unit

Anki có 30 unit decks, mỗi unit 20 cards:

- `4000 Essential English Words - Book 2::Unit 01`
- ...
- `4000 Essential English Words - Book 2::Unit 30`

Backend tạo 30 seed decks:

- `name`: `Unit 01`, `Unit 02`, ...
- `description`: `4000 Essential English Words - Book 2`
- `tags`: `["4000-essential", "book-2", "unit-01"]`
- `is_public`: true
- `is_seed`: true
- `user_id`: null
- `source_name`: `4000 Essential English Words - Book 2`
- `source_unit`: `Unit 01`

### 6.3 Mapping note fields sang `vocabulary_items`

| Anki field | DB field |
|---|---|
| `№` | `anki_number`, `source_key` |
| `IMG` | `image_url` sau khi extract `src` |
| `Sound` | `word_audio_url` |
| `Suggestion` | `suggestion` |
| `Short Vietnamese` | `meaning` |
| `Transcription` | `pronunciation` |
| `Keyword` | `word` |
| `Explanation` | `description` + `example` sau khi clean cloze/HTML |
| `Meaning` | `meaning_audio_url` |
| `Example` | `example_audio_url` |
| `Full Vietnamese` | `full_vietnamese`, hoặc `note` nếu không thêm field riêng |

V1 mapping đề xuất:

- `description`: câu định nghĩa đầu tiên trong `Explanation`.
- `example`: câu sau ký hiệu `→` trong `Explanation`.
- `meaning`: dùng `Short Vietnamese`.
- `note`: dùng clean text của `Full Vietnamese` nếu không muốn thêm `full_vietnamese`.
- Media URL format:
  - `/static/media/anki/book2/4000B2_601.jpg`
  - `/static/media/anki/book2/4000B2_anxious.mp3`

### 6.4 Sample mapping `anxious`

```json
{
  "deck": "Unit 01",
  "anki_number": "601",
  "word": "anxious",
  "pronunciation": "['æŋ(k)ʃəs]",
  "meaning": "lo âu, băn khoăn",
  "description": "When a person is anxious, they worry that something bad will happen.",
  "example": "She was anxious about not making her appointment on time.",
  "suggestion": "a__x__ __ __ __",
  "note": "tính từ ...",
  "image_url": "/static/media/anki/book2/4000B2_601.jpg",
  "word_audio_url": "/static/media/anki/book2/4000B2_anxious.mp3",
  "meaning_audio_url": "/static/media/anki/book2/4000B2_anxious_meaning.mp3",
  "example_audio_url": "/static/media/anki/book2/4000B2_anxious_example.mp3",
  "source_key": "book2-601"
}
```

### 6.5 Idempotent seed

Script import phải chạy lại an toàn:

- Upsert deck theo `(source_name, source_unit)`.
- Upsert item theo `(deck_id, source_key)`.
- Nếu media file đã tồn tại và checksum/size giống nhau thì bỏ qua copy.
- Nếu note thiếu `word` hoặc `meaning` thì log lỗi và skip, không crash toàn bộ import.

## 7. Backend Phase Detail

### Backend Phase 1: FastAPI foundation + PostgreSQL + Alembic

Mục tiêu:

- Tạo backend chạy được ở `localhost:8000`.
- Swagger mở được.
- PostgreSQL chạy qua Docker.
- Alembic migration chạy được.
- Có health check.

File cần tạo/sửa:

- `backend/pyproject.toml`
- `backend/docker-compose.yml`
- `backend/.env.example`
- `backend/app/main.py`
- `backend/app/core/config.py`
- `backend/app/db/base.py`
- `backend/app/db/session.py`
- `backend/alembic.ini`
- `backend/alembic/env.py`
- `backend/tests/conftest.py`
- `backend/tests/test_health.py`

Input:

- `md/PLAN.md`
- Android base URL đang dùng.

Output:

- `GET /health` hoặc `GET /api/v1/health`.
- DB connection hoạt động.
- Migration initial setup.

Command cần chạy:

```bash
cd backend
docker compose up -d
alembic upgrade head
pytest
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Definition of Done:

- `pytest` pass.
- Swagger ở `http://localhost:8000/docs`.
- Android emulator có thể gọi `10.0.2.2:8000`.

### Backend Phase 2: Auth APIs

Mục tiêu:

- Làm Register/Login Android chạy thật.
- JWT access token + refresh token.
- `GET/PATCH /users/me`.

File cần tạo/sửa:

- `models/user.py`
- `models/refresh_token.py`
- `schemas/auth.py`
- `schemas/user.py`
- `api/v1/auth.py`
- `api/v1/users.py`
- `services/auth_service.py`
- `core/security.py`
- Alembic migration users/refresh_tokens.
- `tests/test_auth.py`

Endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`

Service logic:

- Hash password.
- Verify password.
- Issue access token.
- Generate refresh token, store hash.
- Revoke refresh token on logout.
- Parse bearer token in dependency.

Tests:

- Register success.
- Duplicate email returns `{ detail, code }`.
- Login wrong password.
- Refresh valid/invalid/revoked token.
- `GET /users/me` requires auth.

Definition of Done:

- Android Register/Login vào Home được nếu backend chạy.

### Backend Phase 3: Seed Anki data

Mục tiêu:

- Import 30 seed decks, 600 vocabulary items, media URLs.

File cần tạo/sửa:

- `models/deck.py`
- `models/vocabulary.py`
- `schemas/deck.py`
- `schemas/vocabulary.py`
- `services/anki_import_service.py`
- `scripts/import_anki_apkg.py`
- Alembic migration decks/vocabulary_items.
- `tests/test_anki_import.py`

Input:

- `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`

Output:

- 30 public seed decks.
- 600 items.
- Static media under `/static/media/anki/book2/`.

Tests:

- Import count đúng 30 decks/600 items.
- `anxious` mapping đúng.
- Chạy seed 2 lần không duplicate.

Definition of Done:

- DB có seed data sẵn cho deck APIs.

### Backend Phase 4: Deck/item APIs

Mục tiêu:

- Unblock Android Phase 5 Deck Management.
- Public seed decks read-only.
- User decks CRUD.
- Vocabulary item CRUD.

Endpoints:

- `GET /api/v1/decks`
- `POST /api/v1/decks`
- `GET /api/v1/decks/{deck_id}`
- `PATCH /api/v1/decks/{deck_id}`
- `DELETE /api/v1/decks/{deck_id}`
- `GET /api/v1/decks/{deck_id}/items`
- `POST /api/v1/decks/{deck_id}/items`
- `PATCH /api/v1/items/{item_id}`
- `DELETE /api/v1/items/{item_id}`

Service logic:

- User thấy public seed decks + decks của mình.
- User không sửa/xóa seed deck.
- User không sửa/xóa deck của user khác.
- Validate item: `word`, `meaning` required.

Tests:

- List seed decks.
- Create user deck.
- Ownership check 403.
- Add/edit/delete item.

Definition of Done:

- Android Phase 5 có thể dùng API thật.

### Backend Phase 5: Learning/SRS APIs

Mục tiêu:

- Unblock Flashcard Learning.
- Backend là source of truth cho SM-2.

File cần tạo/sửa:

- `models/progress.py`
- `schemas/learning.py`
- `api/v1/learning.py`
- `services/learning_service.py`
- `tests/test_learning.py`

Endpoints:

- `GET /api/v1/learning/daily-plan`
- `GET /api/v1/learning/review-cards`
- `POST /api/v1/learning/reviews`

Service logic:

- Daily plan:
  - `daily_goal` lấy từ `users.daily_new_words`.
  - `new_cards`: số từ mới còn được học hôm nay.
  - `due_reviews`: count progress `due_at <= now`.
  - `total_available`: count vocab public/user visible.
- Review cards:
  - Ưu tiên due reviews.
  - Nếu thiếu, lấy new cards từ seed/user decks.
  - Tạo default progress khi user gặp word mới.
- Submit review:
  - rating `again|hard|good|easy`.
  - Apply SM-2.
  - Insert `review_logs`.
  - Update `user_word_progress`.

Tests:

- Again due sau 10 phút.
- Good lần đầu 1 ngày.
- Good lần hai 6 ngày.
- Ease factor min 1.3.
- Review cards không trả duplicate.

Definition of Done:

- Android Phase 7 Flashcard có thể học thật.

### Backend Phase 6: Analytics APIs

Mục tiêu:

- Làm Home Dashboard Phase 4 có data thật.
- Unblock Progress Analytics.

Endpoints:

- `GET /api/v1/analytics/dashboard`
- `GET /api/v1/analytics/activity`
- `GET /api/v1/analytics/retention`

Service logic:

- `learned_words`: count progress status learning/review/mastered hoặc reviewed at least once.
- `due_today`: count progress due today.
- `streak`: ngày liên tiếp có review log.
- `accuracy`: correct reviews / total reviews.
- `level_estimation`: đơn giản theo learned words/accuracy.
- `activity`: group review logs by date.
- `retention`: good/easy count / review count.

Tests:

- New user returns zeros.
- Accuracy calculation.
- Streak calculation.
- Activity group by date.

Definition of Done:

- Android Home Dashboard không còn error khi backend chạy.

### Backend Phase 7: Notification preferences

Mục tiêu:

- Lưu preference backend.
- Android WorkManager vẫn là local notification v1.

Endpoints:

- `GET /api/v1/notifications/preferences`
- `PATCH /api/v1/notifications/preferences`

Service logic:

- Tạo default preference nếu chưa có.
- Validate timezone/time.

Tests:

- Get default preference.
- Patch daily time/push/email.

Definition of Done:

- Android Profile/Settings Phase 10 có API để lưu reminder.

## 8. Android Integration Impact

| Backend phase | Android được unblock |
|---|---|
| Backend Phase 1 | App có server để ping/test network. |
| Backend Phase 2 | Register/Login thật, token thật, `/users/me`. |
| Backend Phase 3 | Có seed data 30 decks/600 words. |
| Backend Phase 4 | Android Phase 5 Deck List/Detail/Create. |
| Backend Phase 5 | Android Phase 7 Flashcard/SRS. |
| Backend Phase 6 | Home Dashboard Phase 4 có data thật, Progress Phase 9. |
| Backend Phase 7 | Profile Settings/Notifications Phase 10. |

### 8.1 Missing backend support / v1 handling

| Điểm chưa chắc | Trạng thái | Xử lý v1 |
|---|---|---|
| `part_of_speech` | Stitch Flashcard có chip loại từ, DB/backend plan chưa có field riêng. | Ẩn chip hoặc extract sau từ `full_vietnamese`; không bắt Android phụ thuộc trong v1. |
| `cover_image_url` | Stitch Create Deck có cover, backend deck schema chưa có. | Dùng placeholder/static; thêm field sau nếu cần upload cover. |
| Forgot Password | Login có link, backend chưa có endpoint. | Snackbar/disabled/nice-to-have. |
| Smart Review | Stitch Deck List có card smart review, backend chưa có endpoint riêng. | Route sang review due cards hoặc để static. |
| Word Clubs | Home có community card, backend không có social. | Static decorative card, không click. |
| Analytics category breakdown | Progress có breakdown category, backend chỉ dashboard/activity/retention. | Static/fallback hoặc ẩn. |
| Review session summary endpoint | Backend chưa định nghĩa. | Android dùng local session summary trong v1. |
| Pagination/search/sort | Query params chưa chốt. | Backend có thể trả list nhỏ; Android local search/sort v1. |
| Google login | Backend endpoint có trong plan nhưng Android OAuth config chưa có. | Nice-to-have; email/password là must-have. |
| Notification local-only/backend-driven | Plan chốt local notification bằng WorkManager, backend lưu preference. | V1 local-only; không FCM. |

### 8.2 Stitch style cho Android phase tiếp theo

Mọi phase Android sau backend phải kiểm tra Stitch MCP trước khi code:

- Deck screens: card list, search, FAB, tonal bottom nav.
- Add Word: form dài, outlined fields, deck selector.
- Flashcard: card lớn, progress top, answer buttons rõ màu.
- Progress: metric cards, chart native Compose.
- Profile: profile header, settings rows, switches.

Không dùng HTML runtime, không WebView, không dùng screenshot làm UI.

## 9. Testing Strategy

### 9.1 Backend unit tests

Auth:

- Hash/verify password.
- Register duplicate email.
- Login wrong password.
- JWT encode/decode.
- Refresh token revoke.

Anki import:

- Read `.apkg`.
- Count 30 decks/600 items.
- Media map 2401 entries.
- Sample `anxious` mapping.
- Idempotent seed.

Learning:

- SM-2 Again/Hard/Good/Easy.
- Daily plan count.
- Review cards order.
- Submit review creates log.

Analytics:

- Dashboard zero for new user.
- Accuracy.
- Streak.
- Activity by date.
- Retention.

### 9.2 Backend integration tests

Flow:

1. Register.
2. Login.
3. Seed/import Anki.
4. List decks.
5. Get deck items.
6. Get daily plan.
7. Get review cards.
8. Submit review.
9. Dashboard changes.

Use `pytest` + `httpx.AsyncClient` or FastAPI TestClient.

### 9.3 Android tests

Hiện đã có:

- Auth ViewModel tests.
- Home ViewModel tests.
- Error mapper tests.

Sau backend:

- Manual integration với emulator.
- Không cần sửa Android nếu contract backend khớp DTO hiện tại.
- Nếu backend response mismatch, ưu tiên sửa backend schema để khớp Android/plan.

### 9.4 Manual E2E test

Commands:

```bash
cd backend
docker compose up -d
alembic upgrade head
python -m app.scripts.import_anki_apkg ../data/4000_Essential_English_Words_2_-_Vietnamese.apkg
pytest
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Android:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Manual flow:

1. Start PostgreSQL.
2. Start FastAPI.
3. Run Android emulator.
4. Register user.
5. Login user.
6. Home Dashboard load không báo network error.
7. Seed deck visible sau Backend Phase 4 + Android Phase 5.
8. Start learning sau Backend Phase 5 + Android Phase 7.

## 10. Code Agent Rules

- Không sửa Android UI nếu đang làm backend, trừ khi contract mismatch và phải báo rõ.
- Không đổi endpoint Android đang dùng nếu không cập nhật cả backend plan.
- Không bịa endpoint ngoài `md/PLAN.md`.
- Không over-engineering.
- Không thêm Celery/Redis/GraphQL/Hilt.
- Backend phải chạy local cho emulator qua `10.0.2.2:8000`.
- API base path phải là `/api/v1`.
- Error response luôn theo `{ "detail": "...", "code": "..." }`.
- DTO JSON dùng snake_case.
- Domain/backend Python dùng snake_case, Android domain dùng camelCase.
- Luôn chạy test sau mỗi phase.
- Nếu test fail, sửa trong phạm vi phase.
- Báo cáo:
  - File tạo.
  - File sửa.
  - Endpoint hoàn thành.
  - Test/build đã chạy.
  - Lỗi còn lại.

## 11. First Coding Prompt Recommendation

Dùng prompt này để bắt đầu code backend phase đầu tiên:

```text
Bạn là Senior FastAPI Backend Engineer.

Tôi đang làm đồ án MinLish Vocabulary App. Android frontend đã code xong tới Phase 4 và đang gọi backend qua:
http://10.0.2.2:8000/api/v1/

Nhiệm vụ:
Implement Backend Phase 1: FastAPI foundation + PostgreSQL + Alembic.

Chỉ làm backend foundation, chưa implement Auth business thật, chưa import Anki, chưa làm Deck/Learning/Analytics.

Trước khi code hãy đọc:
- BACKEND_AND_NEXT_PHASE_PLAN.md
- md/PLAN.md
- IMPLEMENTATION_PLAN.md
- MODULE_TASK_PLAN.md
- app/src/main/java/com/example/minlishapp_learnenglish/core/network/NetworkConfig.kt
- app/src/main/java/com/example/minlishapp_learnenglish/data/remote/api/*.kt

Phạm vi được làm:
- Tạo thư mục `backend/`.
- Tạo FastAPI app chạy được.
- Tạo PostgreSQL `docker-compose.yml`.
- Tạo SQLAlchemy 2.x base/session.
- Tạo Alembic config.
- Tạo config đọc `.env`.
- Tạo `.env.example`.
- Tạo health endpoint.
- Tạo pytest setup và test health.
- Tạo README backend ngắn nếu cần.

Không được làm:
- Không sửa Android code.
- Không implement Auth endpoints ở phase này.
- Không implement Anki import.
- Không tạo endpoint ngoài health.
- Không thêm Redis/Celery/GraphQL.
- Không over-engineering.

Yêu cầu technical:
- Python 3.12.
- FastAPI.
- Pydantic v2 / pydantic-settings.
- SQLAlchemy 2.x.
- Alembic.
- PostgreSQL.
- pytest.
- httpx cho test API.
- Base path sau này là `/api/v1`.
- Backend chạy bằng:
  uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

File dự kiến:
- backend/pyproject.toml hoặc backend/requirements.txt, chọn một cách đơn giản và nhất quán.
- backend/docker-compose.yml
- backend/.env.example
- backend/app/main.py
- backend/app/core/config.py
- backend/app/db/base.py
- backend/app/db/session.py
- backend/app/api/v1/router.py
- backend/app/api/v1/health.py
- backend/alembic.ini
- backend/alembic/env.py
- backend/tests/conftest.py
- backend/tests/test_health.py

Sau khi code xong chạy:
- cd backend
- docker compose up -d
- alembic upgrade head
- pytest
- uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

Nếu command không chạy được do môi trường, báo rõ lý do và cách chạy thủ công.

Cuối cùng báo cáo:
- File đã tạo.
- File đã sửa.
- Endpoint đã có.
- Command đã chạy.
- Test result.
- Việc còn lại cho Backend Phase 2 Auth.
```

## 12. Questions / Assumptions

Assumptions:

- Dùng PostgreSQL local qua Docker.
- Backend folder sẽ nằm cùng repo Android dưới `backend/`.
- Seed Anki public/read-only là dữ liệu demo chính.
- Email/password auth là must-have; Google login để nice-to-have.
- Android hiện tại không cần sửa nếu backend trả đúng contract.

Questions cần confirm trước các phase sau:

- Có muốn thêm `part_of_speech` thành field chính thức trong backend không?
- Có muốn deck cover image thật trong v1 không, hay giữ placeholder?
- Có cần pagination API ngay cho decks/items không, hay local search/sort đủ cho đồ án?
- Có cần import đầy đủ media ngay Backend Phase 3 không, hay text trước/media sau?
