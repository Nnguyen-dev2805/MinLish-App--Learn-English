# MinLish Vocabulary App

MinLish là đồ án Android học từ vựng tiếng Anh bằng flashcard, SM-2 spaced repetition, deck từ vựng, dashboard tiến độ và nhắc học hằng ngày.

## Tech Stack

- Android: Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM, Retrofit, OkHttp, Moshi, DataStore, Encrypted token storage, WorkManager.
- Backend: FastAPI, SQLAlchemy 2.x, Alembic, PostgreSQL, Pydantic v2, JWT auth, pytest.
- Data demo: `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`.

Android emulator gọi backend qua:

```txt
http://10.0.2.2:8000/api/v1/
```

## Chạy Backend

Tạo môi trường Python nếu chưa có:

```bash
cd backend
python3 -m venv .venv
.venv/bin/python -m pip install -e '.[dev]'
cp .env.example .env
```

Khởi động PostgreSQL, migration và seed Anki:

```bash
docker compose up -d
.venv/bin/alembic upgrade head
.venv/bin/python -m app.scripts.import_anki_apkg ../data/4000_Essential_English_Words_2_-_Vietnamese.apkg
```

Chạy FastAPI:

```bash
.venv/bin/uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Kiểm tra nhanh:

```txt
http://localhost:8000/docs
http://localhost:8000/api/v1/health
```

## Chạy Android

Mở project bằng Android Studio, chọn emulator, rồi Run cấu hình `app`.

Build/test bằng terminal:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Luồng Demo Đề Xuất

1. Login hoặc Register (mở app: Login nếu chưa đăng nhập; tự vào Home nếu còn token).
2. Home Dashboard hiển thị daily plan, streak, accuracy.
3. Decks hiển thị 30 seed decks từ Anki.
4. Mở `Unit 01`, kiểm tra có từ `anxious`.
5. Tạo deck cá nhân.
6. Add/Edit/Delete word trong deck cá nhân.
7. Vào Learn, học 2-3 flashcards.
8. Xem Review Results.
9. Vào Progress để xem analytics cập nhật.
10. Vào Profile, sửa settings/reminder, rồi Logout.

## Test

Android:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Backend:

```bash
cd backend
.venv/bin/python -m pytest
```

Manual QA:

- Dùng checklist trong `QA_CHECKLIST.md`.

## Troubleshooting

### App báo `Không thể kết nối máy chủ`

- Đảm bảo FastAPI đang chạy ở port `8000`.
- Android emulator phải dùng `10.0.2.2`, không dùng `localhost`.
- Kiểm tra `NetworkConfig.API_BASE_URL` đang là `http://10.0.2.2:8000/api/v1/`.

### Port 8000 bị bận

Tìm tiến trình đang dùng port 8000 rồi dừng uvicorn cũ trước khi chạy lại backend.

### Decks không có Unit 01 đến Unit 30

Chạy lại seed:

```bash
cd backend
.venv/bin/python -m app.scripts.import_anki_apkg ../data/4000_Essential_English_Words_2_-_Vietnamese.apkg
```

Script seed được thiết kế để chạy lại không tạo duplicate.

### Login được nhưng Home lỗi

- Kiểm tra backend đã chạy migration mới nhất.
- Kiểm tra các API analytics/learning:
  - `/api/v1/learning/daily-plan`
  - `/api/v1/analytics/dashboard`
  - `/api/v1/analytics/activity`

### Notification không hiện

- Android 13+ cần cấp quyền notification.
- Phase v1 dùng local WorkManager reminder, không dùng FCM.

## V1 Limitations

- Google login cần cấu hình Android OAuth client ID.
- Forgot Password chưa có endpoint.
- Practice Quiz và import/export CSV/XLSX là phần mở rộng nếu còn thời gian.
- Audio playback cho media Anki có thể bổ sung sau nếu cần demo sâu hơn.
