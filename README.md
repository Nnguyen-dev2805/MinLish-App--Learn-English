# MinLish — Learn English Vocabulary

Ứng dụng Android học từ vựng tiếng Anh bằng flashcard và spaced repetition (SM-2). Chạy **hoàn toàn local** — không cần server hay internet.

Mở project bằng **Android Studio** → chọn emulator/thiết bị → Run `app`.

---

## Tech stack

| Hạng mục | Công nghệ |
|----------|-----------|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Kiến trúc | MVVM + Clean Architecture (tầng domain/data/ui) |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines, StateFlow |
| Navigation | Navigation Compose |
| Background | WorkManager (nhắc học local) |
| DI | Manual — `AppContainer` (không Hilt) |
| Test | JUnit, Coroutines Test, Room Testing |

**minSdk 24** · **targetSdk 36** · **Java 11**

---

## Kiến trúc

```
┌─────────────┐   UiState    ┌─────────────┐
│  ui/screens │ ◄─────────── │  ViewModel  │
│  (Compose)  │              └──────┬──────┘
└─────────────┘                     │
                             ┌──────▼──────┐
                             │  UseCase    │  (tùy màn)
                             └──────┬──────┘
                             ┌──────▼──────┐
                             │ Repository  │  ← Mapper Entity → Domain
                             └──────┬──────┘
                             ┌──────▼──────┐
                             │  DAO/Entity │
                             └──────┬──────┘
                                    ▼
                              SQLite (minlish.db)
```

**Luồng dữ liệu:** `UI → ViewModel → Repository → DAO → SQLite`

- **Entity** (`data/local/entity`) — bảng Room, không leak lên UI
- **Domain Model** (`domain/model`) — model nghiệp vụ sạch
- **Mapper** (`data/local/mapper`) — Entity → Domain
- **UiState** (`viewModel/`) — state màn hình, Compose đọc qua `StateFlow`
- **AppResult** (`core/result`) — bọc Success / Failure, UI không parse exception

Không Retrofit, không DTO — toàn bộ dữ liệu nằm trên thiết bị.

---

## Cấu trúc thư mục

```
app/src/main/java/.../minlishapp_learnenglish/
├── core/           # AppContainer, AppResult, SM-2, audio, notification
├── data/
│   ├── local/      # entity, dao, database, mapper
│   └── repository/ # Auth, Deck, Learning, Analytics, Notification
├── domain/
│   ├── model/
│   └── usecase/
├── viewModel/      # ViewModel + UiState theo feature
├── navigation/     # Routes, AppNavGraph
└── ui/             # screens, components, theme

app/src/main/assets/
├── seed_vocabulary.json   # 30 unit, 600 từ
└── seed_media/            # ảnh + audio flashcard
```

`MainActivity` khởi tạo `AppContainer` → inject database, repositories, use cases → `DatabaseSeeder.seedCatalogIfEmpty()`.

---

## Tính năng chính

- Đăng ký / đăng nhập local (Room)
- 30 bộ từ seed (600 từ, ảnh + audio từ Anki APKG)
- Tạo deck và quản lý từ vựng cá nhân
- Flashcard với rating Again / Hard / Good / Easy (SM-2)
- Dashboard tiến độ: streak, accuracy, từ đến hạn
- Nhắc học local qua WorkManager
- Tiến độ riêng theo user — catalog seed dùng chung

---

## Dữ liệu theo user

| Dữ liệu | Phạm vi |
|---------|---------|
| Deck seed + từ vựng | Chung (`userId = null`) |
| Tiến độ học, review log | Riêng từng user |
| Deck tự tạo, notification | Riêng owner |

---

## Chạy lại seed từ APKG

```bash
python scripts/extract_apkg_to_seed_json.py
```

Nguồn: `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`

Sau khi đổi seed hoặc schema DB: **Clear Storage** hoặc gỡ cài app.
