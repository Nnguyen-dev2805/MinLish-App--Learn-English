# MinLish App - Kế hoạch triển khai cho team

## 1. Mục tiêu và phạm vi

### 1.1 Mục tiêu sản phẩm

MinLish là ứng dụng Android hỗ trợ học từ vựng tiếng Anh bằng:

- Flashcard.
- Spaced Repetition System theo SM-2.
- Học qua ngữ cảnh: định nghĩa tiếng Anh, ví dụ, nghĩa tiếng Việt, hình ảnh/audio nếu có.
- Theo dõi tiến độ học: streak, accuracy, số từ đã học, số từ đến hạn ôn.

### 1.2 Phạm vi v1 cho đồ án

Must-have:

- Đăng ký, đăng nhập bằng email/password.
- JWT authentication.
- Quản lý profile có name, goal, level, daily new words.
- Quản lý deck từ vựng.
- Xem danh sách từ vựng theo deck.
- Import seed data từ file Anki `.apkg` đã có.
- Học flashcard với 4 rating: Again, Hard, Good, Easy.
- Backend tính SM-2 và next review time.
- Daily plan gồm từ mới và từ cần ôn.
- Dashboard tiến độ có learned words, due today, streak, accuracy.
- Cài đặt giờ nhắc học local trên Android.

Should-have:

- Import CSV/XLSX cho user.
- Export deck ra CSV.
- Practice quiz trắc nghiệm.
- Biểu đồ daily activity và retention rate.
- Ảnh/audio từ Anki nếu kịp tích hợp media.

Nice-to-have:

- Google login.
- Email reminder.
- Full media player cho audio meaning/example.

Out of scope v1:

- Admin dashboard.
- Payment/subscription.
- Social sharing.
- Real-time sync/offline-first phức tạp.
- Redis/Celery/microservices/GraphQL.

## 2. Kiến trúc tổng thể

### 2.1 System architecture

```txt
Android Kotlin App
        |
        | REST API + JWT
        v
FastAPI Backend
        |
        | SQLAlchemy ORM
        v
PostgreSQL
```

### 2.2 Stack chốt

Backend:

- Python 3.12.
- FastAPI.
- Pydantic v2.
- SQLAlchemy 2.x.
- Alembic.
- PostgreSQL.
- PyJWT.
- passlib/bcrypt.
- python-multipart.
- openpyxl.
- pytest.

Frontend Android:

- Kotlin.
- Jetpack Compose.
- Material3.
- Navigation Compose.
- ViewModel.
- Coroutine/Flow.
- Retrofit.
- OkHttp.
- Moshi.
- DataStore.
- EncryptedSharedPreferences.
- WorkManager.

Infrastructure local:

- PostgreSQL chạy bằng Docker Compose.
- Backend local: `http://localhost:8000`.
- Android emulator gọi backend qua: `http://10.0.2.2:8000/api/v1`.

## 3. Data source và mapping Anki

### 3.1 Nguồn data chính

Data của MinLish lấy từ 4 nguồn:

- Seed data từ Anki `.apkg`: file hiện có `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`.
- Data user tự tạo: user tạo deck và thêm từ thủ công.
- Data user import: CSV/XLSX.
- Data sinh ra khi học: review logs, progress, study sessions, streak, accuracy.

Với đồ án, seed data Anki là nguồn demo chính vì ổn định, không phụ thuộc external API.

### 3.2 Kết quả phân tích file Anki

File:

- `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`

Thông tin:

- Dung lượng khoảng 119 MB.
- Có `collection.anki2`.
- Có 600 notes/cards.
- Chia thành 30 units.
- Mỗi unit có 20 từ.
- Có 2401 media files:
  - 1800 file `.mp3`.
  - 600 file `.jpg`.
  - 1 file `.png`.
- Không có lịch sử học (`revlog = 0`), nên khi import vào MinLish thì tất cả progress bắt đầu từ đầu.

Deck structure:

```txt
4000 Essential English Words - Book 2
4000 Essential English Words - Book 2::Unit 01
...
4000 Essential English Words - Book 2::Unit 30
```

Anki fields:

| Field Anki | Ý nghĩa | Map sang MinLish |
|---|---|---|
| `№` | Số thứ tự 601-1200 | `source_number` |
| `IMG` | Ảnh minh họa | `image_url` |
| `Sound` | Audio phát âm từ | `word_audio_url` |
| `Suggestion` | Gợi ý chữ cái | `hint` |
| `Short Vietnamese` | Nghĩa ngắn tiếng Việt | `meaning` |
| `Transcription` | Phiên âm | `pronunciation` |
| `Keyword` | Từ tiếng Anh | `word` |
| `Explanation` | Định nghĩa EN + ví dụ EN | `description`, `example` |
| `Meaning` | Audio đọc nghĩa | `meaning_audio_url` |
| `Example` | Audio đọc ví dụ | `example_audio_url` |
| `Full Vietnamese` | Nghĩa đầy đủ tiếng Việt | `note` |

Normalized example:

```txt
word: anxious
pronunciation: ['æŋ(k)ʃəs]
meaning: lo âu, băn khoăn
description: When a person is anxious, they worry that something bad will happen.
example: She was anxious about not making her appointment on time.
hint: a__x__ __ __ __
image_url: media/4000B2_601.jpg
word_audio_url: media/4000B2_anxious.mp3
meaning_audio_url: media/4000B2_anxious_meaning.mp3
example_audio_url: media/4000B2_anxious_example.mp3
source_name: 4000 Essential English Words Book 2
source_unit: Unit 01
source_number: 601
```

### 3.3 Cách tổ chức seed data trong MinLish

Để dễ demo và học theo bài, import Anki thành 30 decks:

```txt
4000 Essential English Words Book 2 - Unit 01
...
4000 Essential English Words Book 2 - Unit 30
```

Mỗi deck:

- 20 vocabulary items.
- Tags mặc định:
  - `4000-essential`
  - `book-2`
  - `unit-xx`
  - `seed`

Owner của seed deck:

- Cách khuyến nghị cho đồ án: tạo seed deck public read-only, khi user bắt đầu học thì tạo `user_word_progress` riêng cho user.
- Cách đơn giản hơn nếu cần nhanh: copy seed deck vào user demo đầu tiên.

Chốt v1:

- Backend lưu deck có `is_public`.
- Seed decks có `is_public = true`.
- User có thể xem/học seed decks.
- User-created decks có `is_public = false`.

### 3.4 Import pipeline cho Anki

Input:

- File `.apkg`.

Output:

- 30 decks public.
- 600 vocabulary items.
- Media files được extract vào thư mục static backend, ví dụ `backend/static/media/anki/book2/...`.

Processing rules:

- Đọc zip `.apkg`.
- Đọc SQLite `collection.anki2`.
- Parse `decks`, `models`, `notes`, `cards`.
- Map Anki subdeck sang deck MinLish.
- Tách `Explanation` bằng ký tự `→`:
  - phần trước là `description`.
  - phần sau là `example`.
- Remove cloze markup `{{c1::word}}`.
- Remove HTML tags không cần thiết.
- Extract media reference từ `[sound:...]` và `<img src="...">`.
- Copy media từ file số trong `.apkg` sang static filename gốc.
- Nếu một media file thiếu thì vẫn import text, media URL để null.

Validation:

- Bắt buộc có `word`.
- Bắt buộc có `meaning`.
- Nếu trùng `word` trong cùng deck thì skip hoặc update item cũ.
- Log import result: total, success_count, failed_count, failed_rows.

## 4. Database design

### 4.1 Core tables

`users`

- `id`
- `email`
- `password_hash`
- `google_sub`
- `name`
- `goal`
- `level`
- `daily_new_words`
- `created_at`
- `updated_at`

`refresh_tokens`

- `id`
- `user_id`
- `token_hash`
- `expires_at`
- `revoked_at`
- `created_at`

`decks`

- `id`
- `user_id` nullable for public seed decks.
- `name`
- `description`
- `tags`
- `is_public`
- `source_name`
- `source_unit`
- `created_at`
- `updated_at`

`vocabulary_items`

- `id`
- `deck_id`
- `word`
- `pronunciation`
- `meaning`
- `description`
- `example`
- `collocation`
- `related_words`
- `note`
- `hint`
- `image_url`
- `word_audio_url`
- `meaning_audio_url`
- `example_audio_url`
- `source_number`
- `created_at`
- `updated_at`

`user_word_progress`

- `id`
- `user_id`
- `vocabulary_item_id`
- `repetitions`
- `interval_days`
- `ease_factor`
- `due_at`
- `last_reviewed_at`
- `status`: `new`, `learning`, `review`, `mastered`
- unique key: `(user_id, vocabulary_item_id)`

`review_logs`

- `id`
- `user_id`
- `vocabulary_item_id`
- `rating`: `again`, `hard`, `good`, `easy`
- `is_correct`
- `response_ms`
- `ease_factor_after`
- `next_due_at`
- `created_at`

`study_sessions`

- `id`
- `user_id`
- `started_at`
- `ended_at`
- `new_count`
- `review_count`
- `correct_count`
- `total_count`

`notification_preferences`

- `user_id`
- `daily_time`
- `timezone`
- `email_enabled`
- `push_enabled`
- `updated_at`

### 4.2 Indexes

- `users.email`
- `refresh_tokens.user_id`
- `decks.user_id`
- `decks.is_public`
- `vocabulary_items.deck_id`
- `vocabulary_items.word`
- `user_word_progress(user_id, due_at)`
- `user_word_progress(user_id, vocabulary_item_id)`
- `review_logs(user_id, created_at)`

## 5. Module input/output

### 5.1 Auth module

Register input:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "name": "Nguyen Van A"
}
```

Register output:

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "Nguyen Van A",
    "goal": null,
    "level": null,
    "daily_new_words": 10
  }
}
```

Login input:

```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

Login output:

- Giống register output.

Error cases:

- Email đã tồn tại.
- Email/password sai.
- Password quá ngắn.
- Token hết hạn.

### 5.2 Profile module

Input:

```json
{
  "name": "Nguyen Van A",
  "goal": "IELTS",
  "level": "B1",
  "daily_new_words": 10
}
```

Output:

- User profile đã update.

Rules:

- `level` nằm trong `A1`, `A2`, `B1`, `B2`, `C1`, `C2`.
- `daily_new_words` mặc định 10, min 1, max 100.

### 5.3 Vocabulary module

Create deck input:

```json
{
  "name": "IELTS Writing",
  "description": "Academic vocabulary",
  "tags": ["IELTS", "Academic"]
}
```

Create deck output:

```json
{
  "id": 10,
  "name": "IELTS Writing",
  "description": "Academic vocabulary",
  "tags": ["IELTS", "Academic"],
  "is_public": false,
  "item_count": 0
}
```

Create vocabulary item input:

```json
{
  "word": "achieve",
  "pronunciation": "/əˈtʃiːv/",
  "meaning": "đạt được",
  "description": "To succeed in doing something.",
  "example": "She achieved her goal.",
  "collocation": "achieve success",
  "related_words": "achievement; achievable",
  "note": "Dùng nhiều trong IELTS"
}
```

Vocabulary item output:

```json
{
  "id": 100,
  "deck_id": 10,
  "word": "achieve",
  "pronunciation": "/əˈtʃiːv/",
  "meaning": "đạt được",
  "description": "To succeed in doing something.",
  "example": "She achieved her goal.",
  "collocation": "achieve success",
  "related_words": "achievement; achievable",
  "note": "Dùng nhiều trong IELTS",
  "image_url": null,
  "word_audio_url": null
}
```

Rules:

- User chỉ sửa/xóa deck của mình.
- Public seed deck chỉ read-only với user thường.
- User có thể học public deck.

### 5.4 Import/export module

CSV/XLSX import input columns:

```txt
word
pronunciation
meaning
description
example
collocation
related_words
note
tags
```

Import output:

```json
{
  "total_rows": 100,
  "success_count": 96,
  "failed_count": 4,
  "errors": [
    {
      "row": 12,
      "reason": "Missing word"
    }
  ]
}
```

Export output:

- CSV file có cùng column với import.

Anki seed import:

- Dùng command/backend script riêng cho team dev, không cần expose lên app Android v1.
- Input: `.apkg`.
- Output: seed decks/items/media trong DB và static folder.

### 5.5 Learning module

Daily plan output:

```json
{
  "daily_goal": 10,
  "new_cards": 10,
  "due_reviews": 24,
  "total_available": 600
}
```

Review cards output:

```json
{
  "cards": [
    {
      "vocabulary_item_id": 1,
      "word": "anxious",
      "pronunciation": "['æŋ(k)ʃəs]",
      "meaning": "lo âu, băn khoăn",
      "description": "When a person is anxious, they worry that something bad will happen.",
      "example": "She was anxious about not making her appointment on time.",
      "image_url": "/static/media/anki/book2/4000B2_601.jpg",
      "word_audio_url": "/static/media/anki/book2/4000B2_anxious.mp3"
    }
  ]
}
```

Submit review input:

```json
{
  "vocabulary_item_id": 1,
  "rating": "good",
  "response_ms": 3200
}
```

Submit review output:

```json
{
  "vocabulary_item_id": 1,
  "rating": "good",
  "repetitions": 1,
  "interval_days": 1,
  "ease_factor": 2.5,
  "next_due_at": "2026-05-27T08:00:00Z",
  "status": "learning"
}
```

### 5.6 Practice module

Quiz output:

```json
{
  "question_id": "1-meaning",
  "vocabulary_item_id": 1,
  "type": "choose_meaning",
  "prompt": "anxious",
  "options": [
    "lo âu, băn khoăn",
    "xấu xa, tồi tệ",
    "gồm có",
    "phong cảnh"
  ]
}
```

Answer input:

```json
{
  "question_id": "1-meaning",
  "vocabulary_item_id": 1,
  "selected_answer": "lo âu, băn khoăn",
  "response_ms": 2200
}
```

Answer output:

```json
{
  "is_correct": true,
  "correct_answer": "lo âu, băn khoăn"
}
```

### 5.7 Analytics module

Dashboard output:

```json
{
  "learned_words": 120,
  "due_today": 24,
  "streak": 5,
  "accuracy": 82.5,
  "level_estimation": "Intermediate"
}
```

Activity output:

```json
{
  "days": [
    {
      "date": "2026-05-26",
      "review_count": 32,
      "correct_count": 26
    }
  ]
}
```

Retention output:

```json
{
  "retention_rate": 78.4,
  "good_or_easy_count": 196,
  "review_count": 250
}
```

### 5.8 Notification module

Preference input:

```json
{
  "daily_time": "20:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "push_enabled": true,
  "email_enabled": false
}
```

Output:

- Preference đã lưu.
- Android lên lịch WorkManager local notification.

## 6. Backend API contract

Base path:

```txt
/api/v1
```

Auth:

| Method | Endpoint | Auth | Input | Output |
|---|---|---|---|---|
| POST | `/auth/register` | No | email, password, name | token + user |
| POST | `/auth/login` | No | email, password | token + user |
| POST | `/auth/google` | No | google id token | token + user |
| POST | `/auth/refresh` | No | refresh token | access token |
| POST | `/auth/logout` | Yes | refresh token | success |
| GET | `/users/me` | Yes | none | user |
| PATCH | `/users/me` | Yes | profile fields | user |

Vocabulary:

| Method | Endpoint | Auth | Output |
|---|---|---|---|
| GET | `/decks` | Yes | public decks + user decks |
| POST | `/decks` | Yes | created deck |
| GET | `/decks/{deck_id}` | Yes | deck detail |
| PATCH | `/decks/{deck_id}` | Yes | updated deck |
| DELETE | `/decks/{deck_id}` | Yes | success |
| GET | `/decks/{deck_id}/items` | Yes | vocabulary items |
| POST | `/decks/{deck_id}/items` | Yes | created item |
| PATCH | `/items/{item_id}` | Yes | updated item |
| DELETE | `/items/{item_id}` | Yes | success |
| POST | `/decks/{deck_id}/import` | Yes | import result |
| GET | `/decks/{deck_id}/export` | Yes | CSV file |

Learning/practice:

| Method | Endpoint | Auth | Output |
|---|---|---|---|
| GET | `/learning/daily-plan` | Yes | daily plan |
| GET | `/learning/review-cards` | Yes | cards to study |
| POST | `/learning/reviews` | Yes | updated progress |
| GET | `/practice/quiz` | Yes | quiz question |
| POST | `/practice/answers` | Yes | answer result |

Analytics/notifications:

| Method | Endpoint | Auth | Output |
|---|---|---|---|
| GET | `/analytics/dashboard` | Yes | dashboard metrics |
| GET | `/analytics/activity` | Yes | daily activity |
| GET | `/analytics/retention` | Yes | retention |
| GET | `/notifications/preferences` | Yes | preferences |
| PATCH | `/notifications/preferences` | Yes | updated preferences |

Common error format:

```json
{
  "detail": "Human readable error message",
  "code": "ERROR_CODE"
}
```

## 7. SM-2 learning rules

Rating:

| UI label | API value | Quality |
|---|---|---|
| Again | `again` | 2 |
| Hard | `hard` | 3 |
| Good | `good` | 4 |
| Easy | `easy` | 5 |

Default progress:

- `ease_factor = 2.5`.
- `repetitions = 0`.
- `interval_days = 0`.
- `status = new`.

Rules:

- Again:
  - `repetitions = 0`.
  - `interval_days = 0`.
  - `due_at = now + 10 minutes`.
  - `status = learning`.
- First successful review:
  - `interval_days = 1`.
  - `status = learning`.
- Second successful review:
  - `interval_days = 6`.
  - `status = review`.
- Later successful reviews:
  - `interval_days = round(previous_interval * ease_factor)`.
- Hard:
  - reduce ease factor.
  - keep interval at least 1 day.
- Easy:
  - increase interval with about `1.3x` bonus.
- Minimum ease factor:
  - `1.3`.

Correctness for analytics:

- `again` = incorrect.
- `hard`, `good`, `easy` = correct.

## 8. Android frontend plan

### 8.1 Frontend architecture

Dùng MVVM nhẹ, không over-engineering:

```txt
ui screen
  -> ViewModel
  -> Repository
  -> Retrofit API
  -> FastAPI backend
```

Package structure:

```txt
core/
  AppContainer
  AppResult
  TokenStorage
  AuthInterceptor
  NotificationScheduler

data/remote/
  ApiService
  dto/

data/repository/
  AuthRepository
  DeckRepository
  LearningRepository
  AnalyticsRepository
  NotificationRepository

domain/model/
  User
  Deck
  VocabularyItem
  ReviewCard
  Dashboard
  DailyActivity

ui/
  auth/
  home/
  decks/
  deckdetail/
  editor/
  learning/
  practice/
  analytics/
  settings/
```

### 8.2 Navigation

Start logic:

- Nếu có valid token: vào Home.
- Nếu không có token: vào Login.

Main tabs:

- Home.
- Decks.
- Learn.
- Analytics.
- Settings.

Secondary screens:

- Register.
- Deck detail.
- Vocabulary editor.
- Flashcard session.
- Practice quiz.

### 8.3 Screen contracts

Login screen:

- Input: email, password.
- Output: login success -> save token -> Home.
- API: `POST /auth/login`.
- States: idle, loading, error.

Register screen:

- Input: name, email, password.
- Output: register success -> save token -> Home.
- API: `POST /auth/register`.
- Validation: email not blank, password min 6.

Home screen:

- Input: none.
- Output: dashboard overview.
- API:
  - `GET /analytics/dashboard`.
  - `GET /learning/daily-plan`.
- Actions:
  - Start learning.
  - Open due cards.

Deck list screen:

- Input: none.
- Output: public decks + user decks.
- API: `GET /decks`.
- Actions:
  - create deck.
  - open deck detail.
  - import CSV/XLSX.

Deck detail screen:

- Input: `deck_id`.
- Output: deck info + vocabulary list.
- API:
  - `GET /decks/{deck_id}`.
  - `GET /decks/{deck_id}/items`.
- Actions:
  - add/edit/delete item for own deck.
  - start learning this deck.
  - export CSV.

Vocabulary editor:

- Input: item fields.
- Output: created/updated item.
- API:
  - `POST /decks/{deck_id}/items`.
  - `PATCH /items/{item_id}`.
- Required fields:
  - word.
  - meaning.

Learning screen:

- Input: review cards.
- Output: review logs + updated SRS progress.
- API:
  - `GET /learning/review-cards`.
  - `POST /learning/reviews`.
- UI:
  - front: word, pronunciation, image optional.
  - back: meaning, description, example, note.
  - buttons: Again, Hard, Good, Easy.
  - audio button optional if media URL exists.

Practice screen:

- Input: quiz question.
- Output: answer result.
- API:
  - `GET /practice/quiz`.
  - `POST /practice/answers`.
- UI:
  - prompt.
  - 4 options.
  - feedback correct/incorrect.

Analytics screen:

- Input: review logs/progress from backend.
- Output: metrics + chart.
- API:
  - `GET /analytics/dashboard`.
  - `GET /analytics/activity`.
  - `GET /analytics/retention`.
- UI:
  - learned words.
  - due today.
  - streak.
  - accuracy.
  - daily activity chart by Compose Canvas.

Settings screen:

- Input:
  - profile fields.
  - daily reminder time.
  - notification toggles.
- Output:
  - updated profile.
  - scheduled WorkManager notification.
- API:
  - `GET /users/me`.
  - `PATCH /users/me`.
  - `GET/PATCH /notifications/preferences`.

### 8.4 Frontend state rules

- Mỗi screen chính có ViewModel riêng.
- UI state dùng data class/sealed interface:
  - loading.
  - data.
  - error.
  - empty.
- ViewModel không gọi Retrofit trực tiếp.
- Repository trả `AppResult<T>`.
- Khi API trả 401:
  - thử refresh token.
  - nếu refresh fail thì logout về Login.

### 8.5 Android local notification

Input:

- `daily_time`.
- `push_enabled`.

Behavior:

- Dùng WorkManager lập lịch mỗi ngày.
- Nếu due review > 0 thì hiện notification.
- Bấm notification mở app vào Learning screen.

## 9. Backend implementation plan

Backend folder:

```txt
backend/
  app/
    main.py
    core/
      config.py
      security.py
    db/
      base.py
      session.py
    models/
    schemas/
    api/v1/
    services/
  alembic/
  tests/
  scripts/
    import_anki_apkg.py
  requirements.txt
  docker-compose.yml
  .env.example
```

Backend services:

- `auth_service`: hash password, verify password, issue/refresh/revoke token.
- `vocabulary_service`: deck/item CRUD, ownership check.
- `anki_import_service`: parse `.apkg`, extract media, seed decks/items.
- `learning_service`: daily plan, review cards, SM-2 update.
- `practice_service`: quiz generation, answer check.
- `analytics_service`: dashboard, activity, retention.
- `notification_service`: preference CRUD.

Important implementation rules:

- API routes mỏng, business logic trong service.
- SQLAlchemy models rõ ràng, migration bằng Alembic.
- Không hard-code secret key.
- Password không bao giờ trả về response.
- Public seed deck read-only với user thường.
- Media static URL có prefix `/static/...`.

## 10. Team task breakdown

### 10.1 Suggested roles

Role A - Backend foundation/auth:

- Tạo FastAPI project.
- Tạo PostgreSQL Docker Compose.
- Config `.env`.
- SQLAlchemy session.
- Alembic migrations.
- User model.
- Auth endpoints.
- JWT refresh/logout.
- Unit test auth.

Role B - Backend vocabulary/data:

- Deck/item models.
- Deck/item CRUD APIs.
- Ownership/public deck rules.
- CSV/XLSX import/export.
- Anki `.apkg` import script.
- Media extract/static serving.
- Seed data verification.

Role C - Backend learning/analytics:

- SM-2 pure function.
- Daily plan endpoint.
- Review cards endpoint.
- Submit review endpoint.
- Practice quiz endpoint.
- Dashboard/activity/retention endpoints.
- Unit test SM-2 and analytics.

Role D - Android architecture/auth/navigation:

- Add dependencies.
- AppContainer.
- Retrofit/OkHttp/Moshi.
- TokenStorage.
- AuthInterceptor.
- Navigation graph.
- Login/register screens.
- Auth ViewModel/Repository.

Role E - Android feature screens:

- Home dashboard.
- Deck list/detail.
- Vocabulary editor.
- Flashcard learning.
- Practice quiz.
- Analytics chart.
- Settings/profile/notification.

Role F - QA/integration/docs:

- API manual test collection.
- Sample test accounts.
- End-to-end demo flow.
- README setup.
- Bug tracking.
- Final report screenshots.

Nếu team ít người:

- Gộp Role A + B.
- Gộp Role C + F.
- Gộp Role D + E.

### 10.2 Work items by milestone

Milestone 1 - Backend foundation:

- FastAPI app chạy được.
- PostgreSQL Docker chạy được.
- Alembic migrate được.
- Health check endpoint.
- `.env.example`.

Output:

- Backend start command.
- DB tables đầu tiên.
- API docs Swagger chạy được.

Milestone 2 - Auth + Android login:

- Backend register/login/refresh/me.
- Android login/register.
- Token lưu local.
- Auto attach Bearer token.

Output:

- User tạo account và vào Home được.

Milestone 3 - Seed data + vocabulary:

- Anki import script.
- 30 seed decks.
- 600 vocabulary items.
- Deck list API.
- Deck detail API.
- Android deck list/detail.

Output:

- App hiển thị Book 2 Unit 01-30.
- Mỗi unit có 20 từ.

Milestone 4 - Learning SRS:

- SM-2 service.
- Daily plan.
- Review cards.
- Submit rating.
- Android flashcard screen.

Output:

- User học một card, rating xong backend cập nhật `next_due_at`.

Milestone 5 - Progress/analytics:

- Review logs.
- Dashboard metrics.
- Activity chart API.
- Retention API.
- Android Home/Analytics.

Output:

- Sau khi học, dashboard thay đổi đúng.

Milestone 6 - Practice/import/export/notification:

- Practice quiz.
- CSV/XLSX import.
- CSV export.
- Notification preference.
- WorkManager local notification.

Output:

- User làm quiz.
- User import/export deck.
- User bật nhắc học hằng ngày.

Milestone 7 - Polish/demo:

- Loading/error/empty states.
- Validation forms.
- Bug fix.
- README.
- Demo script.
- Screenshots/video.

Output:

- Demo flow ổn định từ register -> xem seed deck -> học -> dashboard -> reminder.

## 11. Testing plan

### 11.1 Backend tests

Auth:

- Register thành công.
- Không cho duplicate email.
- Login sai password bị reject.
- JWT access token hợp lệ.
- Refresh token hợp lệ.

Vocabulary:

- User tạo deck riêng.
- User không sửa/xóa public seed deck.
- User không sửa deck của user khác.
- Tạo item với word/meaning hợp lệ.
- Import CSV valid/invalid rows.

Anki import:

- Import ra đúng 30 decks.
- Import ra đúng 600 items.
- Mỗi unit có 20 items.
- Item sample `anxious` map đúng word/meaning/example/media.

Learning:

- Again due sau 10 phút.
- Good lần đầu interval 1 ngày.
- Good lần hai interval 6 ngày.
- Easy tăng interval nhanh hơn Good.
- Ease factor không thấp hơn 1.3.

Analytics:

- Accuracy tính đúng.
- Streak tính đúng.
- Due today tính đúng.
- Retention rate tính đúng.

### 11.2 Android tests

Unit tests:

- AuthRepository login success/fail.
- DeckRepository parse deck list.
- LearningRepository submit rating.
- ViewModel login state.
- ViewModel deck list loading/error/data.
- ViewModel learning submit review.

Compose smoke tests:

- Login screen render.
- Home screen render.
- Deck list render.
- Flashcard render.

Manual QA:

- Backend off -> app hiện lỗi, không crash.
- Token expired -> refresh hoặc logout.
- Empty deck -> hiện empty state.
- Public deck -> không hiện nút edit/delete.
- User deck -> hiện nút edit/delete.

## 12. Definition of Done

Backend DoD:

- Endpoint có trong Swagger.
- Request/response đúng contract.
- Có validation đầu vào.
- Có ownership check.
- Có unit test cho logic quan trọng.
- Migration chạy được trên DB mới.

Android DoD:

- UI có loading/error/empty state.
- Không gọi API trực tiếp trong Composable.
- ViewModel giữ state.
- Repository xử lý API error.
- Form có validation cơ bản.
- Màn hình không crash khi response rỗng/null.

Feature DoD:

- Có input/output rõ ràng.
- Có demo path.
- Có test hoặc manual test case.
- Có error case tối thiểu.
- Code không over-engineering.

Project DoD:

- Chạy được backend local.
- Chạy được Android app trên emulator.
- Import được Anki seed data.
- User đăng ký/đăng nhập được.
- User học flashcard và thấy dashboard update.
- Có README hướng dẫn setup.

## 13. Demo script

Demo flow để thuyết trình:

1. Start PostgreSQL và FastAPI.
2. Chạy script import Anki `.apkg`.
3. Mở Android app.
4. Đăng ký user mới.
5. Vào Home xem daily plan.
6. Vào Decks xem `4000 Essential English Words Book 2 - Unit 01`.
7. Mở deck thấy 20 từ.
8. Bấm Học ngay.
9. Lật flashcard `anxious`.
10. Chọn `Good`.
11. Backend tính next review.
12. Quay lại Dashboard thấy learned/accuracy thay đổi.
13. Vào Analytics xem activity.
14. Vào Settings bật nhắc học lúc 20:00.

## 14. Risks và cách giảm rủi ro

Risk: Media Anki nặng 119 MB làm app/backend phức tạp.

- Giảm rủi ro: v1 import text trước, media là phase sau trong cùng schema.

Risk: Google login tốn thời gian cấu hình OAuth.

- Giảm rủi ro: email/password là must-have, Google login nice-to-have.

Risk: SM-2 sai logic làm dashboard sai.

- Giảm rủi ro: viết unit test riêng cho SM-2 pure function.

Risk: Team bị chia việc chồng chéo API/frontend.

- Giảm rủi ro: chốt API contract trước, Android dùng DTO theo contract.

Risk: Data bẩn do HTML/cloze của Anki.

- Giảm rủi ro: import script normalize text, có test sample.

## 15. Assumptions chốt để team làm việc

- Team sẽ dùng file Anki hiện có làm seed data chính.
- App v1 ưu tiên chạy ổn định hơn là đầy đủ mọi tính năng nặng.
- Public seed deck read-only với user.
- User progress lưu riêng theo từng user và từng vocabulary item.
- Android notification dùng local WorkManager, không dùng FCM.
- Email reminder và Google login không chặn demo nếu chưa xong.
- Collocation và related words có thể null vì data Anki hiện tại không có field riêng cho 2 mục này.
- Media URL được thiết kế trong DB ngay từ đầu, nhưng UI audio/image có thể làm sau text learning.
