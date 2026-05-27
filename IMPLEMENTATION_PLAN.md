# Kế hoạch triển khai Android cho MinLish

## 1. Tóm tắt điều hành

Tài liệu này mô tả cách triển khai MinLish Vocabulary App thành ứng dụng Android native bằng Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM/Clean Architecture, Retrofit, và contract backend/data đã được định nghĩa trong `md/PLAN.md`.

Các asset thiết kế từ Stitch đã được kiểm tra và export vào:

- `stitch_exports/screens/`
- `stitch_exports/code/`
- `stitch_exports/README.md`

UI từ Stitch cần được giữ lại về mặt thị giác, nhưng phải triển khai bằng Compose native. HTML đã tải xuống chỉ dùng làm tài liệu tham chiếu giao diện, không dùng làm code production và không dùng WebView.

Repository hiện tại vẫn là Android Compose skeleton:

- `MainActivity.kt` vẫn render `Hello Android`.
- `ui/theme` vẫn dùng theme Material màu tím mặc định.
- Chưa có implementation cho Android repository/data/ViewModel/navigation.
- Chưa có source code backend trong repo; nguồn sự thật hiện tại là `md/PLAN.md` và `md/content.txt`.

Plan backend/data đã định nghĩa auth, vocabulary, learning/SRS, analytics, notification preferences, seed data Anki và API contracts. Phần Android cần map các màn hình Stitch vào những API đó, đồng thời đánh dấu rõ các tính năng UI mà backend plan hiện chưa hỗ trợ.

## 2. Phân tích thiết kế Stitch

Design system:

- Screen/asset ID: `asset-stub-assets-8af7ff622bd8411d8f88f034f824131d-1779781985759`
- Kết quả MCP: đây là design-system asset, không phải screen thường. `get_screen` trả về `Requested entity was not found`.
- Export khả dụng: `stitch_exports/code/design_system_tokens.md`
- Mục đích UI chính: định nghĩa look and feel Material 3 cho MinLish.
- Thành phần thị giác chính: bảng màu teal/mint, typography Inter, margin 16dp, spacing scale 8dp, card 24dp, button dạng pill, bottom navigation Material 3.
- Compose components tái sử dụng cần có: `MinLishTheme`, `MinLishButton`, `MinLishCard`, `MinLishTextField`, `MinLishBottomBar`.
- State cần có: không áp dụng.
- Phụ thuộc backend/data: không có.

| Màn hình | ID | Mục đích | Thành phần UI chính | Compose components tái sử dụng | State cần có | Phụ thuộc backend/data |
|---|---|---|---|---|---|---|
| Splash Screen | `be5d4dab43884985b7c01758dc5286da` | Màn hình brand entry và kiểm tra token để quyết định luồng auth. | Logo ở giữa, tên app, subtitle, progress bar nhẹ, tông teal/mint. | `MinLishLogo`, `SplashProgressBar`. | loading, token-valid, token-missing, error fallback. | `TokenStorage`, optional `/auth/refresh`. |
| Onboarding Screen | `f7c972080d1e42ffb609a40464fa6092` | Giới thiệu spaced repetition và điều hướng sang auth. | Hero logo card, headline, mô tả ngắn, primary CTA, nút login phụ. | `MinLishLogo`, `PrimaryButton`, `TonalButton`. | default, loading optional. | Flag onboarding-seen local trong DataStore. |
| Login Screen | `3d4f3ebc052b43eea183a9a10a38f62e` | Đăng nhập email/password và entry cho Google login. | Auth card bo góc, field email/password, link forgot password, nút Google, link register. | `MinLishTextField`, `PasswordField`, `PrimaryButton`, `SocialLoginButton`. | idle, validating, loading, error. | `POST /auth/login`; `POST /auth/google` là nice-to-have; forgot password chưa có trong backend plan. |
| Register Screen | `dc4f6f6c20fa4f3ca4ef6e67cf283165` | Tạo tài khoản và thu thập learning preferences ban đầu. | Field full name/email/password, dropdown goal, dropdown level, checkbox terms, CTA. | `MinLishTextField`, `MinLishDropdown`, `MinLishCheckboxRow`, `PrimaryButton`. | idle, validation-error, loading, success, error. | `POST /auth/register`, sau đó `PATCH /users/me` cho goal/level nếu register endpoint chưa nhận các field này. |
| Home Dashboard | `bd93ded4673e4e9a86ae15849b2687a3` | Tổng quan học hôm nay và điểm vào chính để học. | Greeting, daily plan cards, start learning button, stat cards, mini chart tuần, tip/community cards, bottom nav. | `DailyPlanCard`, `StatCard`, `MiniActivityChart`, `InsightCard`, `MinLishBottomBar`. | loading, success, empty-dashboard, error. | `GET /analytics/dashboard`, `GET /learning/daily-plan`, `GET /analytics/activity`. |
| Deck List | `eea728fe28824df78bf25da7960e15e5` | Browse public/user decks và bắt đầu học theo deck. | Header, search field, deck cards có tags và word counts, Smart Review card, FAB, bottom nav. | `SearchField`, `DeckCard`, `TagChip`, `SmartReviewCard`, `MinLishFab`, `MinLishBottomBar`. | loading, success, empty, search-empty, error. | `GET /decks`; Smart Review/AI chưa có trong backend plan, nên để static/nice-to-have nếu chưa bổ sung. |
| Deck Detail | `be5cc20d11714924858e603012302973` | Hiển thị thống kê deck, actions và vocabulary list. | Top app bar, mastery progress, word counts, action cards, filter/sort, word cards, FAB, bottom nav. | `ProgressSummaryCard`, `DeckActionButton`, `WordCard`, `FilterSortRow`, `MinLishFab`. | loading, success, empty-words, error, read-only public deck. | `GET /decks/{deck_id}`, `GET /decks/{deck_id}/items`, optional deck-scoped learning query chưa được định nghĩa. |
| Create Deck | `8fe373dcb2f24a669ef5e8d6764d3d74` | Tạo custom vocabulary deck. | Top app bar, cover photo placeholder, text fields, tag chips, public toggle, CTA, bottom nav. | `CoverPhotoPickerPlaceholder`, `MinLishTextField`, `TagInput`, `MinLishSwitch`, `PrimaryButton`. | idle, validation-error, loading, success, error. | `POST /decks`; backend plan cần xác nhận `is_public`, `tags`, và optional `cover_image_url`. |
| Add Word Form | `3538149b32844c82be09a358bf4ed6ff` | Thêm vocabulary item với các field ngữ cảnh. | Deck selector, input word/pronunciation/meaning/description/example/collocation/related/note, stats card, save button, bottom nav. | `DeckSelector`, `MinLishTextField`, `MinLishTextArea`, `SaveButton`, `StatCard`. | idle, validation-error, loading, success, error. | `POST /decks/{deck_id}/items`; audio upload và pronunciation play chưa có trong backend plan. |
| Flashcard Learning | `63b8e048ace140af9cc15f0307816a1b` | Review card và submit SRS rating. | Session progress, audio button, flashcard lớn, part-of-speech chip, word/pronunciation/definition/example, show answer, 4 review buttons. | `SessionProgressBar`, `FlashcardView`, `AudioButton`, `ReviewActionButton`. | loading, front, back, submitting-rating, completed, empty, error. | `GET /learning/review-cards`, `POST /learning/reviews`; backend chưa có `part_of_speech`, nên dùng null/fallback nếu schema chưa đổi. |
| Review Results | `20ad23ddfc6e4f0ea693322719580830` | Tóm tắt phiên học đã hoàn thành. | Celebration icon, circular accuracy indicator, quote, total/mastered/review soon cards, next action buttons. | `CircularAccuracyIndicator`, `ResultStatCard`, `PrimaryButton`, `TonalButton`. | loading-summary, success, error. | Data từ local review session state cộng với `POST /learning/reviews`; backend chưa định nghĩa session summary endpoint. |
| Progress Analytics | `216a8bf066f9490f80662128bbd449f7` | Hiển thị tiến độ học và retention. | Metric cards, weekly/monthly tabs, bar chart, retention level card, learning breakdown progress bars, bottom nav. | `MetricCard`, `SegmentedControl`, `BarChart`, `RetentionCard`, `ProgressChartCard`. | loading, success, empty, error. | `GET /analytics/dashboard`, `GET /analytics/activity`, `GET /analytics/retention`; category breakdown endpoint chưa được định nghĩa. |
| Profile & Settings | `57446103b63a4a40a46e39791cbf7c98` | Quản lý profile, goals, reminders và account actions. | Profile header, toggles, account rows, support rows, streak card, bottom nav. | `ProfileHeader`, `SettingsSwitchRow`, `SettingsNavRow`, `StreakCard`, `MinLishBottomBar`. | loading, success, saving, error. | `GET/PATCH /users/me`, `GET/PATCH /notifications/preferences`; support/feedback chưa phải backend features. |

## 3. Kế hoạch navigation

Route graph:

```txt
splash
onboarding
login
register
main/home
main/decks
main/learn
main/progress
main/profile
deckDetail/{deckId}
createDeck
addWord?deckId={deckId}
editWord/{wordId}
flashcardSession?deckId={deckId}
reviewResults?sessionId={sessionId}
practiceQuiz?deckId={deckId}
```

Auth flow:

- `splash` kiểm tra token storage.
- Nếu không có token và user chưa xem onboarding: đi tới `onboarding`.
- Nếu không có token và user đã xem onboarding: đi tới `login`.
- `onboarding -> login` hoặc `onboarding -> register`.
- `login/register` thành công thì lưu access/refresh token và điều hướng tới `main/home`.
- API trả 401 thì kích hoạt refresh; refresh thất bại thì clear token và điều hướng về `login`.

Main bottom navigation flow:

- `main/home`
- `main/decks`
- `main/learn`
- `main/progress`
- `main/profile`

Detail screens:

- `deckDetail/{deckId}`
- `createDeck`
- `addWord?deckId={deckId}`
- `editWord/{wordId}`
- `flashcardSession?deckId={deckId}`
- `reviewResults?sessionId={sessionId}`
- `practiceQuiz?deckId={deckId}`

Các màn hình nên ẩn bottom navigation:

- `splash`
- `onboarding`
- `login`
- `register`
- `flashcardSession`
- `reviewResults`

Các màn hình có thể giữ bottom navigation:

- `home`
- `decks`
- `deckDetail`
- `createDeck`
- `addWord`
- `progress`
- `profile`

Arguments:

- `deckId`: dùng cho deck detail, default deck khi add word, học theo deck, practice theo deck.
- `wordId`: dùng cho edit word detail.
- `sessionId`: dùng cho review results nếu backend sau này tạo session records; nếu chưa có thì dùng `ReviewSessionSummary` lưu tạm trong memory.

## 4. Kế hoạch architecture

Cấu trúc package khuyến nghị:

```txt
app/src/main/java/com/example/minlishapp_learnenglish/
  core/
    network/
    result/
    storage/
    notification/
    time/
  data/
    remote/
      dto/
      api/
    local/
    repository/
  domain/
    model/
    usecase/
  navigation/
  presentation/
    viewmodel/
  ui/
    components/
    screens/
    theme/
```

Trách nhiệm từng layer:

- `ui/screens/`: các Compose screen thuần, render state và gửi event lên ViewModel.
- `ui/components/`: các Compose components tái sử dụng, giữ đúng design.
- `presentation/viewmodel/`: ViewModel theo màn hình, UI state, UI event, one-time effect.
- `domain/model/`: model cấp app, độc lập với Retrofit/JSON.
- `domain/usecase/`: orchestration nhỏ như login, load dashboard, submit review.
- `data/remote/`: Retrofit API interfaces và DTO classes khớp `md/PLAN.md`.
- `data/local/`: token storage, onboarding flag, settings cache, optional deck cache.
- `data/repository/`: implementation của repositories, DTO mapping, error handling.
- `core/network/`: Retrofit builder, OkHttp auth interceptor, token refresh handling.
- `core/result/`: `AppResult`, `AppError`, error mapping.
- `navigation/`: route constants, nav graph, bottom bar destination model.

Dependency injection:

- V1 giữ đơn giản bằng manual `AppContainer`.
- Chưa thêm Hilt cho tới khi độ phức tạp thật sự cần.

## 5. Kế hoạch tích hợp backend

Nguồn sự thật:

- `md/PLAN.md`
- `md/content.txt`

Những điểm backend còn thiếu hoặc chưa rõ cần xác nhận:

- Chưa có forgot password endpoint.
- Deck cover photo chưa có trong DB schema.
- `part_of_speech` có trong Stitch nhưng chưa có trong DB schema.
- Deck-scoped learning endpoint chưa được định nghĩa rõ.
- Session summary endpoint chưa được định nghĩa rõ.
- Category breakdown cho analytics chưa được định nghĩa rõ.
- Pagination, sorting và search query parameters chưa được định nghĩa.
- Response shape chính xác của refresh token chưa đầy đủ.

| Màn hình | User action | ViewModel function | UseCase | Repository method | API endpoint | UI state update |
|---|---|---|---|---|---|---|
| Splash | App start | `checkSession()` | `CheckSessionUseCase` | `authRepository.refreshIfNeeded()` | optional `POST /auth/refresh` | navigate Home/Login/Onboarding |
| Onboarding | Get Started | `onGetStarted()` | `SetOnboardingSeenUseCase` | local DataStore | local only | navigate Register |
| Onboarding | Log in | `onLogin()` | none | local only | local only | navigate Login |
| Login | Log In | `login(email,password)` | `LoginUseCase` | `authRepository.login()` | `POST /auth/login` | loading -> success/error |
| Login | Continue Google | `loginWithGoogle()` | `GoogleLoginUseCase` | `authRepository.loginGoogle()` | `POST /auth/google` | loading -> success/error |
| Register | Create Account | `register(form)` | `RegisterUseCase` | `authRepository.register()` | `POST /auth/register` | loading -> success/error |
| Register | Save goal/level | `updateInitialProfile()` | `UpdateProfileUseCase` | `userRepository.updateMe()` | `PATCH /users/me` | profile saved |
| Home | Load screen | `loadHome()` | `LoadHomeUseCase` | `analyticsRepository.dashboard()`, `learningRepository.dailyPlan()` | `GET /analytics/dashboard`, `GET /learning/daily-plan` | loading -> data/empty/error |
| Home | Start Learning | `startLearning()` | `StartLearningUseCase` | `learningRepository.getReviewCards()` | `GET /learning/review-cards` | navigate Flashcard |
| Deck List | Load decks | `loadDecks()` | `LoadDecksUseCase` | `deckRepository.getDecks()` | `GET /decks` | loading -> decks/empty/error |
| Deck List | Search | `searchDecks(query)` | local filter first | repository optional | query not specified | filtered list |
| Deck List | Add Deck | `onCreateDeck()` | none | none | none | navigate CreateDeck |
| Deck Detail | Load detail | `loadDeck(deckId)` | `LoadDeckDetailUseCase` | `deckRepository.getDeck()`, `deckRepository.getItems()` | `GET /decks/{deck_id}`, `GET /decks/{deck_id}/items` | loading -> detail/error |
| Create Deck | Save | `createDeck(form)` | `CreateDeckUseCase` | `deckRepository.createDeck()` | `POST /decks` | loading -> navigate detail/error |
| Add Word | Save | `saveWord(form)` | `CreateWordUseCase` | `deckRepository.createItem()` | `POST /decks/{deck_id}/items` | loading -> success/error |
| Flashcard | Show answer | `showAnswer()` | local | none | none | front -> back |
| Flashcard | Rate card | `submitReview(rating)` | `SubmitReviewUseCase` | `learningRepository.submitReview()` | `POST /learning/reviews` | submitting -> next/completed/error |
| Review Results | View summary | `loadSummary()` | local/session usecase | optional analytics | no endpoint yet | summary data |
| Progress | Load analytics | `loadProgress()` | `LoadProgressUseCase` | `analyticsRepository.dashboard/activity/retention()` | analytics endpoints | loading -> data/error |
| Profile | Load profile | `loadProfile()` | `LoadProfileUseCase` | `userRepository.getMe()`, `notificationRepository.getPreferences()` | `GET /users/me`, `GET /notifications/preferences` | loading -> data/error |
| Profile | Toggle reminders | `updateNotifications()` | `UpdateNotificationSettingsUseCase` | `notificationRepository.updatePreferences()` | `PATCH /notifications/preferences` | saving -> saved/error |
| Profile | Logout | `logout()` | `LogoutUseCase` | `authRepository.logout()` | `POST /auth/logout` | clear tokens -> Login |

Error handling:

- Map HTTP 400/422 thành validation errors.
- Map HTTP 401 thành refresh flow.
- Map HTTP 403 thành thông báo permission/read-only deck.
- Map HTTP 404 thành not-found empty/error state.
- Map network timeout/offline thành `NetworkError` có thể retry.
- Hiển thị lỗi one-time qua snackbar effects.

Loading behavior:

- Dùng skeleton/placeholder cho list và dashboard.
- Disable submit buttons khi đang POST/PATCH.
- Giữ dữ liệu cũ trong lúc refresh nếu an toàn.

## 6. Mapping data model

Domain models:

```kotlin
data class User(
    val id: Long,
    val email: String,
    val name: String?,
    val goal: String?,
    val level: String?,
    val dailyNewWords: Int
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class VocabularyDeck(
    val id: Long,
    val name: String,
    val description: String?,
    val tags: List<String>,
    val isPublic: Boolean,
    val itemCount: Int,
    val sourceName: String?,
    val sourceUnit: String?
)

data class VocabularyWord(
    val id: Long,
    val deckId: Long,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    val description: String?,
    val example: String?,
    val collocation: String?,
    val relatedWords: String?,
    val note: String?,
    val hint: String?,
    val imageUrl: String?,
    val wordAudioUrl: String?,
    val meaningAudioUrl: String?,
    val exampleAudioUrl: String?,
    val sourceNumber: Int?,
    val partOfSpeech: String? = null
)

data class ReviewSchedule(
    val vocabularyItemId: Long,
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextDueAt: Instant,
    val status: ReviewStatus
)

data class ReviewResult(
    val vocabularyItemId: Long,
    val rating: ReviewRating,
    val schedule: ReviewSchedule
)

data class DailyLearningPlan(
    val dailyGoal: Int,
    val newCards: Int,
    val dueReviews: Int,
    val totalAvailable: Int
)

data class ProgressStats(
    val learnedWords: Int,
    val dueToday: Int,
    val streak: Int,
    val accuracy: Double,
    val levelEstimation: String?
)

data class NotificationSettings(
    val dailyTime: LocalTime,
    val timezone: String,
    val pushEnabled: Boolean,
    val emailEnabled: Boolean
)
```

Quy tắc mapping:

- Retrofit DTOs mirror backend JSON snake_case.
- Domain models dùng Kotlin camelCase.
- UI models có thể flatten fields để dễ hiển thị, ví dụ `DeckCardUiModel`.
- Date từ backend nên là ISO-8601 string và parse sang `Instant`.
- Media URL nên được resolve theo backend base URL nếu là relative URL, ví dụ `/static/...`.
- `partOfSpeech` nullable vì backend plan chưa định nghĩa field này.

## 7. Kế hoạch triển khai Compose

| File path | Composable | ViewModel | UiState | Events/actions | Components tái sử dụng | Data source |
|---|---|---|---|---|---|---|
| `ui/screens/splash/SplashScreen.kt` | `SplashScreen` | `SplashViewModel` | `SplashUiState` | app start | `MinLishLogo`, `SplashProgressBar` | token storage |
| `ui/screens/onboarding/OnboardingScreen.kt` | `OnboardingScreen` | `OnboardingViewModel` | `OnboardingUiState` | get started, login | `MinLishLogo`, buttons | DataStore |
| `ui/screens/auth/LoginScreen.kt` | `LoginScreen` | `LoginViewModel` | `LoginUiState` | email, password, login, google, register | text fields, buttons | AuthRepository |
| `ui/screens/auth/RegisterScreen.kt` | `RegisterScreen` | `RegisterViewModel` | `RegisterUiState` | register, select goal, select level, terms | dropdowns, checkbox, button | AuthRepository/UserRepository |
| `ui/screens/home/HomeScreen.kt` | `HomeScreen` | `HomeViewModel` | `HomeUiState` | refresh, start learning | dashboard cards/charts | AnalyticsRepository/LearningRepository |
| `ui/screens/decks/DeckListScreen.kt` | `DeckListScreen` | `DeckListViewModel` | `DeckListUiState` | search, open deck, create deck | search, deck card, FAB | DeckRepository |
| `ui/screens/decks/DeckDetailScreen.kt` | `DeckDetailScreen` | `DeckDetailViewModel` | `DeckDetailUiState` | learn, review, sort, add word | word card, summary card | DeckRepository/LearningRepository |
| `ui/screens/decks/CreateDeckScreen.kt` | `CreateDeckScreen` | `CreateDeckViewModel` | `CreateDeckUiState` | update form, save, toggle public | form fields, chips, switch | DeckRepository |
| `ui/screens/words/AddWordScreen.kt` | `AddWordScreen` | `AddWordViewModel` | `AddWordUiState` | update form, save | deck selector, text areas | DeckRepository |
| `ui/screens/learning/FlashcardScreen.kt` | `FlashcardScreen` | `FlashcardViewModel` | `FlashcardUiState` | flip, play audio, rate | flashcard, review buttons | LearningRepository |
| `ui/screens/learning/ReviewResultsScreen.kt` | `ReviewResultsScreen` | `ReviewResultsViewModel` | `ReviewResultsUiState` | another session, back home | circular progress, result cards | local session summary |
| `ui/screens/progress/ProgressScreen.kt` | `ProgressScreen` | `ProgressViewModel` | `ProgressUiState` | range tab, refresh | metric cards, charts | AnalyticsRepository |
| `ui/screens/profile/ProfileSettingsScreen.kt` | `ProfileSettingsScreen` | `ProfileSettingsViewModel` | `ProfileSettingsUiState` | toggles, edit profile, logout | settings rows, switches | UserRepository/NotificationRepository |

## 8. Kế hoạch giữ đúng thiết kế

Color tokens:

- Thay theme tím mặc định bằng Stitch Material 3 palette.
- Primary: `#005E53`.
- Primary container: `#00796B`.
- Background/surface: `#F8F9FA`.
- Surface containers: `#FFFFFF`, `#F3F4F5`, `#EDEEEF`, `#E7E8E9`.
- Secondary/mint: `#BBE8E4`, `#97F3E2`, `#7AD7C6`.
- Tertiary/amber: dùng ít, chủ yếu cho streak và feedback động viên tích cực.

Typography:

- Dùng Inter nếu thêm font asset.
- Nếu chưa thêm font asset, dùng Material default làm fallback nhưng giữ mapping size/weight.
- Map Stitch tokens sang Material typography:
  - `headlineLarge`: 32sp, SemiBold.
  - `headlineMedium`: 28sp, SemiBold.
  - `titleLarge`: 22sp, Medium.
  - `bodyLarge`: 16sp.
  - `bodyMedium`: 14sp.
  - `labelLarge`: 14sp Medium.
  - `labelMedium`: 12sp Medium.

Spacing:

- Horizontal screen margin 16dp.
- Small gaps 8dp.
- Normal stack spacing 16dp.
- Section spacing 24dp.

Corner radius:

- Cards/dialogs: 24dp.
- Inputs/chips: 12dp.
- Standard buttons/FAB: pill.

Card styles:

- Ưu tiên tonal surfaces thay vì shadow nặng.
- Dùng `surfaceContainerLowest` cho cards và `surfaceContainerLow` cho vùng inactive.

Button styles:

- Primary filled teal button với text trắng.
- Tonal mint button với text dark teal.
- Review buttons có phân cấp màu rõ và fixed height.

Icons:

- Dùng Material Symbols equivalent qua `Icons.Default`/`Icons.Outlined` khi có sẵn.
- Nếu thiếu icon Material tương ứng, chọn icon Compose Material gần nhất.

Bottom navigation:

- Dùng M3 `NavigationBar`.
- Active item dùng tonal pill/container.
- Icons: Home, Decks, Learn, Progress, Profile.

Layouts:

- Dùng `Scaffold`.
- Dùng `LazyColumn` cho màn hình dài.
- Auth/splash/onboarding là full-screen, không có bottom bar.
- Content luôn align theo margin 16dp.

## 9. Kế hoạch component library

Reusable components:

- `MinLishButton`
- `MinLishTonalButton`
- `MinLishIconButton`
- `MinLishTextField`
- `MinLishPasswordField`
- `MinLishDropdown`
- `MinLishSwitch`
- `StatCard`
- `DailyPlanCard`
- `DeckCard`
- `WordCard`
- `TagChip`
- `SearchField`
- `FlashcardView`
- `AudioButton`
- `ReviewActionButton`
- `SessionProgressBar`
- `CircularAccuracyIndicator`
- `ProgressChartCard`
- `MiniActivityChart`
- `RetentionCard`
- `SettingsSwitchRow`
- `SettingsNavRow`
- `EmptyStateView`
- `ErrorStateView`
- `LoadingStateView`
- `MinLishBottomBar`
- `MinLishTopBar`
- `MinLishFab`

Quy tắc component:

- Components nhận domain/UI models và callbacks, không nhận repositories.
- Components nên preview được bằng fake data.
- Các control có format cố định cần height ổn định và width responsive.

## 10. Kế hoạch quản lý state

Pattern:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: UiError? = null
)

sealed interface ScreenEvent

sealed interface ScreenEffect {
    data class Navigate(val route: String) : ScreenEffect
    data class ShowSnackbar(val message: String) : ScreenEffect
}
```

State theo từng màn hình:

- Splash: `Checking`, `Authenticated`, `Unauthenticated`, `Error`.
- Onboarding: `Idle`.
- Login/Register: form fields, field errors, loading, API error, navigation effect.
- Home: dashboard stats, daily plan, activity, loading, error, empty.
- DeckList: query, decks, filtered decks, loading, error, empty.
- DeckDetail: deck, words, sort/filter, read-only flag, loading, error.
- CreateDeck/AddWord: form state, validation errors, save loading, success effect.
- Flashcard: cards, current index, isAnswerShown, isSubmitting, session summary, error.
- ReviewResults: summary, loading optional, actions.
- Progress: selected range, dashboard, activity, retention, category breakdown optional.
- ProfileSettings: user, notification settings, saving row states, logout effect.

One-time events:

- Navigation.
- Snackbar.
- Token-expired logout.
- Save success.
- Notification permission request.

## 11. Kế hoạch offline / local cache

Backend/data plan hiện tại chưa định nghĩa chiến lược offline-first đầy đủ.

Local storage khuyến nghị cho v1:

- EncryptedSharedPreferences: access token, refresh token.
- DataStore: onboarding-seen flag, base settings, cached notification time.
- Optional Room cache về sau:
  - public/user decks.
  - deck details.
  - last dashboard response.
  - pending local notification settings.

Những gì nên hoạt động offline trong v1:

- App mở được cached shell.
- User có thể xem last-loaded deck list nếu có thêm Room cache.
- Notifications vẫn có thể chạy dựa trên schedule đã lưu.

Những gì không nên hứa trong v1:

- Tạo/sửa deck offline.
- Submit reviews offline.
- Conflict resolution.

Sync và conflict handling nếu sau này thêm Room:

- Read-through cache cho decks và words.
- Network wins cho public seed decks.
- User edits cần server confirmation.
- Không queue conflicting edits trong v1.

## 12. Kế hoạch tích hợp SRS / SM-2

SM-2 logic nằm ở đâu:

- Source of truth: backend `learning_service`.
- Android không tự tính lại final schedule để persist.
- Android chỉ hiển thị `interval_days`, `ease_factor`, và `next_due_at` do server trả về.

Mapping rating:

| Button | API value | Quality |
|---|---|---|
| Again | `again` | 2 |
| Hard | `hard` | 3 |
| Good | `good` | 4 |
| Easy | `easy` | 5 |

Flow:

1. `FlashcardViewModel.loadCards(deckId?)` gọi `GET /learning/review-cards`.
2. User bấm Show Answer.
3. User bấm Again/Hard/Good/Easy.
4. ViewModel gọi `POST /learning/reviews`.
5. Backend cập nhật repetitions, interval, ease factor, due date.
6. ViewModel cập nhật current session summary và chuyển sang card tiếp theo.
7. Khi hết cards, điều hướng sang Review Results.

Persistence:

- `POST /learning/reviews` persist review result và progress.
- Android giữ tạm `ReviewSessionSummary` cho Review Results screen.

## 13. Kế hoạch tích hợp notification

Daily reminder flow:

- Profile screen load `GET /notifications/preferences`.
- User bật/tắt daily reminders hoặc đổi reminder time.
- ViewModel gọi `PATCH /notifications/preferences`.
- Khi thành công, Android schedule/cancel WorkManager local notification.

Due review reminder flow:

- WorkManager chạy vào giờ đã cấu hình.
- Worker có thể gọi `GET /learning/daily-plan`.
- Nếu `due_reviews > 0`, hiển thị local notification.
- Bấm notification mở `flashcardSession`.

Local vs backend:

- V1 dùng local notification qua WorkManager.
- Backend chỉ lưu preferences.
- Không dùng FCM trong v1.
- Email reminder là nice-to-have và không chặn demo.

Storage:

- Backend: `notification_preferences`.
- Android: DataStore mirror để schedule.

Permissions:

- Android 13+ cần runtime permission `POST_NOTIFICATIONS`.
- Xin permission từ Settings screen khi user bật push/local reminders.

## 14. Milestones triển khai

Phase 1: Project structure, theme, navigation

- Add dependencies trong Gradle.
- Thay theme tím mặc định bằng Stitch tokens.
- Tạo route graph và bottom bar.
- Tạo nền tảng reusable components.
- Render static versions của toàn bộ Stitch screens bằng fake data.

Phase 2: Auth screens

- Implement Splash, Onboarding, Login, Register.
- Add token storage.
- Add Retrofit/OkHttp basic setup.
- Connect login/register với API.

Phase 3: Home and dashboard data

- Implement Home ViewModel.
- Connect daily plan, dashboard, activity.
- Add loading/error/empty states.

Phase 4: Deck management

- Implement Deck List, Deck Detail, Create Deck, Add Word.
- Connect deck/item APIs.
- Add search/filter local behavior.
- Add public/read-only handling.

Phase 5: Flashcard learning and SRS

- Implement Flashcard screen.
- Connect review cards và submit review APIs.
- Implement session summary và Review Results screen.

Phase 6: Progress analytics

- Implement analytics screen.
- Add Compose Canvas bar charts/progress bars.
- Connect dashboard/activity/retention APIs.

Phase 7: Profile, settings, notifications

- Implement profile/settings screen.
- Connect user profile và notification preferences.
- Add WorkManager local reminder.
- Add notification permission flow.

Phase 8: Polish, error states, testing

- Add previews cho reusable components.
- Add ViewModel unit tests.
- Add repository tests với fake API.
- Add Compose smoke tests.
- Run Android build và lint.

## 15. Đánh giá rủi ro

| Rủi ro | Ảnh hưởng | Cách giảm rủi ro |
|---|---|---|
| Stitch generated code là HTML/Tailwind hướng web. | Không thể tái sử dụng trực tiếp trong Android native. | Chỉ dùng HTML làm visual reference; dựng lại bằng Compose. |
| Một số UI features chưa có trong backend plan. | Implementation có thể bị chặn vì thiếu endpoint. | Đánh dấu feature thiếu và implement static/fallback UI cho tới khi backend mở rộng. |
| Chart không map trực tiếp sang Compose components có sẵn. | Progress screen có thể tốn thời gian hơn. | Dùng custom `Canvas`/simple bars cho v1. |
| Auth token handling dễ bị lỗi. | User có thể bị kẹt sau 401. | Centralize trong OkHttp interceptor và AuthRepository refresh flow. |
| Date/time handling cho SRS có thể sai timezone. | Due review reminders và dashboard có thể sai. | Dùng ISO-8601 `Instant`, convert display theo local timezone. |
| Media URL từ Anki có thể lớn hoặc thiếu. | Flashcard media có thể load lỗi. | Text-first UI; chỉ hiển thị audio/image khi có URL. |
| Bottom nav trên màn hình form dài có thể chật. | Create/Add form có thể kém usability. | Giữ bottom bar nếu cần khớp Stitch, nhưng cho phép ẩn nếu QA thấy overlap. |
| Dynamic color có thể override Stitch palette. | UI không còn giống design. | Disable dynamic color mặc định trong MinLish theme. |

## 16. Files cần tạo / chỉnh sửa

Concrete file tree:

```txt
IMPLEMENTATION_PLAN.md                              create
stitch_exports/README.md                            create
stitch_exports/code/design_system_tokens.md         create
stitch_exports/code/*.html                          create
stitch_exports/screens/*.png                        create

app/build.gradle.kts                                modify
gradle/libs.versions.toml                           modify
app/src/main/AndroidManifest.xml                    modify

app/src/main/java/com/example/minlishapp_learnenglish/MainActivity.kt  modify

app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Color.kt       modify
app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Theme.kt       modify
app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Type.kt        modify

app/src/main/java/com/example/minlishapp_learnenglish/core/network/           create
app/src/main/java/com/example/minlishapp_learnenglish/core/result/            create
app/src/main/java/com/example/minlishapp_learnenglish/core/storage/           create
app/src/main/java/com/example/minlishapp_learnenglish/core/notification/      create

app/src/main/java/com/example/minlishapp_learnenglish/data/remote/api/        create
app/src/main/java/com/example/minlishapp_learnenglish/data/remote/dto/        create
app/src/main/java/com/example/minlishapp_learnenglish/data/local/             create
app/src/main/java/com/example/minlishapp_learnenglish/data/repository/        create

app/src/main/java/com/example/minlishapp_learnenglish/domain/model/           create
app/src/main/java/com/example/minlishapp_learnenglish/domain/usecase/         create

app/src/main/java/com/example/minlishapp_learnenglish/navigation/             create
app/src/main/java/com/example/minlishapp_learnenglish/presentation/viewmodel/ create

app/src/main/java/com/example/minlishapp_learnenglish/ui/components/          create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/splash/      create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/onboarding/  create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/auth/        create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/home/        create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/decks/       create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/words/       create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/learning/    create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/progress/    create
app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/profile/     create

md/PLAN.md                                          keep existing
md/content.txt                                      keep existing
backend/                                            review required; not implemented yet
```

Cần review trước khi code:

- Có thêm Room cache trong v1 không.
- Có thêm `part_of_speech` vào backend schema không.
- Có thêm `cover_image_url` vào deck schema không.
- Có expose deck-scoped learning query không.
- Forgot Password sẽ ẩn hay thêm vào backend.

## 17. Checklist trước khi code

Cần confirm trước implementation:

- API base URL cho emulator: dự kiến `http://10.0.2.2:8000/api/v1`.
- Auth token format: Bearer JWT.
- Access token expiry và refresh token expiry.
- Exact refresh endpoint response shape.
- Date format: dự kiến ISO-8601 UTC strings.
- Error response format: dự kiến `{ "detail": "...", "code": "..." }`.
- Pagination requirement cho decks và vocabulary items.
- Search/filter/sort query parameters cho deck list và word list.
- Import/export là MVP hay làm sau.
- Notifications là local-only hay backend-driven.
- Google login là MVP hay nice-to-have.
- Forgot Password xuất hiện trong UI nhưng disabled, hay cần endpoint thật.
- Stitch "Smart Review" là static/nice-to-have hay phải có API-backed feature.
- Stitch "Word Clubs" có ẩn trong v1 không.
- Profile Support/Feedback là static links hay bỏ.
- Có thêm `part_of_speech` vào `vocabulary_items` không.
- Deck cover photo upload có làm không, hay thay bằng static placeholder.
- Media playback từ Anki audio có bắt buộc trong v1 không hay defer.
