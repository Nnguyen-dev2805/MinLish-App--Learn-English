# MinLish Backend

FastAPI backend for the MinLish Vocabulary App.

## Setup

```bash
cd backend
cp .env.example .env
docker compose up -d
alembic upgrade head
python -m app.scripts.import_anki_apkg ../data/4000_Essential_English_Words_2_-_Vietnamese.apkg
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Swagger UI:

```txt
http://localhost:8000/docs
```

Android emulator reaches the backend through:

```txt
http://10.0.2.2:8000/api/v1/
```

## Tests

```bash
cd backend
.venv/bin/python -m pytest
```

## Demo Notes

- Android emulator gọi backend qua `http://10.0.2.2:8000/api/v1/`.
- Nếu app báo `Không thể kết nối máy chủ`, kiểm tra backend đang chạy ở port `8000`.
- Nếu tab Decks không có `Unit 01` đến `Unit 30`, chạy lại script import Anki seed ở trên.
