# MODULE TASK PLAN - MinLish Vocabulary App

## 0. Nguồn đã kiểm tra

### 0.1 Stitch MCP

- Project: `MinLish Vocabulary App Design`
- Project ID: `2325080910252905506`
- Đã gọi `get_project`: thành công.
- Đã gọi `list_screens`: thành công.
- Đã gọi `get_screen` cho 13 màn hình chính: thành công.
- Đã thử `get_screen` cho Design System asset `asset-stub-assets-8af7ff622bd8411d8f88f034f824131d-1779781985759`: Stitch trả `Requested entity was not found`.
- Design System được lấy từ `get_project` và `list_design_systems`.

### 0.2 Stitch exports trong repository

- `stitch_exports/README.md`
- `stitch_exports/screens/*.png`
- `stitch_exports/code/*.html`
- `stitch_exports/code/design_system_tokens.md`

Không phát hiện khác biệt lớn về danh sách screen giữa Stitch MCP và `stitch_exports`: cả hai đều có 13 màn hình chính. Design System cũng khớp: đây là asset, không phải screen thường. Stitch MCP là nguồn mới nhất; `stitch_exports` dùng để đọc HTML/text và đối chiếu visual.

### 0.3 Repository hiện tại

Đã đọc các file:

- `md/PLAN.md`
- `IMPLEMENTATION_PLAN.md`
- `md/content.txt`
- `stitch_exports/README.md`
- `stitch_exports/code/design_system_tokens.md`
- `stitch_exports/code/*.html`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/example/minlishapp_learnenglish/MainActivity.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Color.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Theme.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/ui/theme/Type.kt`

Kết luận trạng thái repo:

- Android app hiện là Compose skeleton.
- `MainActivity.kt` đang hiển thị `Hello Android`.
- Theme vẫn là template Material màu tím mặc định.
- Chưa có navigation graph.
- Chưa có data layer, repository, Retrofit API, token storage, ViewModel theo feature.
- Chưa có source backend trong repo; backend/data/API contract lấy từ `md/PLAN.md` và `IMPLEMENTATION_PLAN.md`.

## 1. Tổng quan kiến trúc

MinLish là Android app học từ vựng bằng flashcard, SM-2 spaced repetition, deck từ vựng, daily plan, analytics và notification.

Stack Android đã chốt:

- Kotlin.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- MVVM.
- Coroutine/Flow.
- Retrofit + OkHttp + Moshi.
- DataStore.
- EncryptedSharedPreferences.
- WorkManager.

Flow kiến trúc:

```txt
Compose Screen
  -> ViewModel
  -> UseCase nếu cần
  -> Repository
  -> Retrofit API
  -> Backend FastAPI
  -> PostgreSQL
```

Nguyên tắc mapping UI/data:

- UI phải giữ visual style từ Stitch: teal/mint palette, Inter typography, Material 3, card 24dp, input 12dp, button pill, bottom navigation tonal pill.
- Data không lấy từ HTML Stitch. Data lấy từ backend contract trong `md/PLAN.md`.
- HTML Stitch chỉ dùng làm visual reference, không dùng làm runtime UI, không dùng WebView.
- App cuối cùng phải dựng bằng native Jetpack Compose components.
- Những phần Stitch UI cần data nhưng backend chưa có sẽ đánh dấu `Missing backend support`, không tự tạo endpoint mới.

Nguồn data chính theo `md/PLAN.md`:

- Seed data Anki: `data/4000_Essential_English_Words_2_-_Vietnamese.apkg`.
- User-created decks/words.
- User import CSV/XLSX.
- Review logs, progress, study sessions, notification preferences.

Design tokens chính từ Stitch:

| Nhóm | Giá trị |
|---|---|
| Primary | `#005e53` |
| Primary container | `#00796b` |
| Background/surface | `#f8f9fa` |
| Surface lowest | `#ffffff` |
| Surface low | `#f3f4f5` |
| Surface container | `#edeeef` |
| Secondary container | `#bbe8e4` |
| Tertiary amber | `#ffba38` |
| Text chính | `#191c1d` |
| Text phụ | `#3e4946` |
| Font | Inter |
| Margin mobile | 16dp |
| Spacing scale | 8dp |
| Card radius | 24dp |
| Input/chip radius | 12dp |
| Button radius | pill |

## 2. Danh sách module

1. Theme & Design System.
2. Navigation.
3. Core Result/Error Handling.
4. Network/Auth Interceptor.
5. Token Storage.
6. Auth.
7. Home Dashboard.
8. Deck Management.
9. Word Management.
10. Flashcard Learning.
11. Review Results.
12. Progress Analytics.
13. Profile & Settings.
14. Notifications.
15. Testing & QA.

## 3. Chi tiết từng module

### 3.1 Theme & Design System

Mục tiêu:

- Thay theme tím mặc định bằng MinLish design system từ Stitch.
- Tạo bộ component nền tảng để các màn hình giữ cùng style.
- Không dùng WebView, không copy HTML Stitch làm UI.

File cần tạo/sửa:

- Sửa `ui/theme/Color.kt`.
- Sửa `ui/theme/Theme.kt`.
- Sửa `ui/theme/Type.kt`.
- Tạo `ui/theme/Shape.kt`.
- Tạo `ui/theme/Spacing.kt`.
- Tạo `ui/components/MinLishButton.kt`.
- Tạo `ui/components/MinLishTextField.kt`.
- Tạo `ui/components/MinLishCard.kt`.
- Tạo `ui/components/MinLishTopBar.kt`.
- Tạo `ui/components/MinLishBottomBar.kt`.
- Tạo `ui/components/StateViews.kt`.

Screen Stitch liên quan:

- Design System asset.
- Tất cả màn hình.

Data/API liên quan:

- Không cần API.
- Dùng local/static preview data.

Domain models cần dùng:

- Không bắt buộc.

DTO cần dùng:

- Không có.

Repository methods cần có:

- Không có.

ViewModel functions cần có:

- Không có.

UiState/UiEvent/UiEffect:

- Không có state nghiệp vụ.

Compose components cần tạo:

- `MinLishTheme`.
- `MinLishButton`.
- `MinLishTonalButton`.
- `MinLishOutlinedButton`.
- `MinLishTextField`.
- `MinLishPasswordField`.
- `MinLishCard`.
- `TagChip`.
- `StatCard`.
- `EmptyStateView`.
- `ErrorStateView`.
- `LoadingStateView`.
- `MinLishBottomBar`.
- `MinLishTopBar`.

States:

- Loading/empty/error components là reusable shell.
- Success state không áp dụng ở theme module.

Acceptance criteria:

- Dynamic color mặc định không override Stitch palette.
- App dùng màu teal/mint thay vì purple template.
- Buttons, cards, inputs, chips có shape đúng Stitch.
- Component preview có fake data.

Test cases:

- Build app thành công.
- Preview render không lỗi.
- Kiểm tra bằng mắt: màu primary, background, card radius, button radius khớp Stitch.

### 3.2 Navigation

Mục tiêu:

- Tạo route graph rõ ràng cho auth flow, main tabs và detail screens.
- Bottom navigation chỉ hiện ở các màn hình phù hợp.

File cần tạo/sửa:

- Tạo `navigation/Routes.kt`.
- Tạo `navigation/AppNavGraph.kt`.
- Tạo `navigation/MainDestinations.kt`.
- Sửa `MainActivity.kt` để gọi `MinLishApp`.
- Tạo `ui/MinLishApp.kt`.

Screen Stitch liên quan:

- Splash Screen.
- Onboarding Screen.
- Login Screen.
- Register Screen.
- Home Dashboard.
- Deck List.
- Deck Detail.
- Create Deck.
- Add Word Form.
- Flashcard Learning.
- Review Results.
- Progress Analytics.
- Profile & Settings.

Data/API liên quan:

- Token status từ local storage.
- Không gọi API trực tiếp ở navigation.

Domain models cần dùng:

- `AuthSession` nếu cần xác định initial route.

DTO cần dùng:

- Không có.

Repository methods cần có:

- `TokenRepository.hasSession()`.
- `TokenRepository.clearSession()`.

ViewModel functions cần có:

- `SplashViewModel.checkSession()`.

UiState/UiEvent/UiEffect:

- `SplashUiState.Checking`.
- `SplashUiState.Authenticated`.
- `SplashUiState.Unauthenticated`.
- `SplashEffect.Navigate(route)`.

Compose components:

- `MinLishBottomBar`.
- `MinLishTopBar`.

States:

- Loading: splash checking.
- Empty: không áp dụng.
- Error: token invalid thì clear token và vào login.
- Success: điều hướng đúng route.

Acceptance criteria:

- App start tại `splash`.
- Nếu chưa đăng nhập: đi tới onboarding/login.
- Nếu đăng nhập hợp lệ: đi tới home.
- Bottom nav hiện ở `home`, `decks`, `deckDetail`, `createDeck`, `addWord`, `progress`, `profile`.
- Bottom nav ẩn ở `splash`, `onboarding`, `login`, `register`, `flashcardSession`, `reviewResults`.

Test cases:

- Navigation smoke test cho từng route.
- Kiểm tra route argument `deckId`, `wordId`, `sessionId`.
- Back navigation từ detail về list hoạt động.

### 3.3 Core Result/Error Handling

Mục tiêu:

- Chuẩn hóa success/error/loading cho repository và ViewModel.
- Không để screen tự parse exception hoặc HTTP code.

File cần tạo/sửa:

- Tạo `core/result/AppResult.kt`.
- Tạo `core/result/AppError.kt`.
- Tạo `core/result/ErrorMapper.kt`.
- Tạo `core/result/UiError.kt`.

Screen Stitch liên quan:

- Tất cả màn hình có loading/error/empty.

Data/API liên quan:

- Common error format theo `md/PLAN.md`:

```json
{
  "detail": "Human readable error message",
  "code": "ERROR_CODE"
}
```

Domain models:

- Không bắt buộc.

DTO cần dùng:

- `ErrorResponseDto(detail, code)`.

Repository methods:

- Repository trả `AppResult<T>`.

ViewModel functions:

- Mỗi ViewModel map `AppError` sang `UiError`.

UiState/UiEvent/UiEffect:

- `isLoading`.
- `error: UiError?`.
- `isEmpty`.
- `Effect.ShowSnackbar(message)`.

Compose components:

- `LoadingStateView`.
- `ErrorStateView`.
- `EmptyStateView`.

States:

- Loading khi API đang chạy.
- Empty khi list rỗng hoặc daily plan không có card.
- Error khi network/API/validation fail.
- Success khi data đầy đủ.

Acceptance criteria:

- Repository không throw exception trực tiếp lên UI.
- UI có retry callback cho lỗi network.
- Validation error hiển thị gần field nếu liên quan form.

Test cases:

- Map HTTP 400/422 thành validation error.
- Map 401 thành auth expired.
- Map network timeout thành network error.
- Map unknown exception thành unknown error.

### 3.4 Network/Auth Interceptor

Mục tiêu:

- Tạo Retrofit client kết nối FastAPI.
- Tự attach Bearer token.
- Xử lý 401 theo refresh flow.

File cần tạo/sửa:

- Tạo `core/network/NetworkConfig.kt`.
- Tạo `core/network/RetrofitFactory.kt`.
- Tạo `core/network/AuthInterceptor.kt`.
- Tạo `core/network/TokenAuthenticator.kt`.
- Tạo `data/remote/api/AuthApi.kt`.
- Tạo `data/remote/api/DeckApi.kt`.
- Tạo `data/remote/api/LearningApi.kt`.
- Tạo `data/remote/api/AnalyticsApi.kt`.
- Tạo `data/remote/api/NotificationApi.kt`.
- Sửa `gradle/libs.versions.toml`.
- Sửa `app/build.gradle.kts`.

Screen Stitch liên quan:

- Login/Register.
- Home.
- Decks.
- Flashcard.
- Progress.
- Profile.

Data/API liên quan:

- Base URL emulator: `http://10.0.2.2:8000/api/v1`.
- Auth header: `Authorization: Bearer <access_token>`.
- Refresh: `POST /auth/refresh`.

Domain models:

- `AuthSession`.
- `User`.

DTO cần dùng:

- `AuthResponseDto`.
- `RefreshRequestDto`.
- `RefreshResponseDto`.
- `ErrorResponseDto`.

Repository methods:

- `AuthRepository.refresh(refreshToken)`.
- Các repository feature gọi API tương ứng.

ViewModel functions:

- Không gọi trực tiếp ở UI; network layer tự dùng trong repository.

UiState/UiEvent/UiEffect:

- Nếu refresh fail: `Effect.NavigateToLogin`.

Compose components:

- Không có.

States:

- Loading ở repository caller.
- Error nếu network down hoặc refresh fail.

Acceptance criteria:

- Tất cả request cần auth đều có Bearer token.
- Khi 401 và refresh token hợp lệ, request được retry.
- Khi refresh fail, token bị clear và app về login.
- Không gọi API từ Composable.

Test cases:

- Fake API trả 401 rồi refresh success.
- Fake API trả 401 rồi refresh fail.
- Verify header Authorization.

### 3.5 Token Storage

Mục tiêu:

- Lưu access token/refresh token an toàn.
- Lưu onboarding-seen và notification mirror settings.

File cần tạo/sửa:

- Tạo `core/storage/TokenStorage.kt`.
- Tạo `core/storage/EncryptedTokenStorage.kt`.
- Tạo `core/storage/UserPreferencesStorage.kt`.
- Tạo `data/local/PreferencesKeys.kt`.

Screen Stitch liên quan:

- Splash.
- Onboarding.
- Login.
- Register.
- Profile & Settings.

Data/API liên quan:

- Auth token từ `/auth/login`, `/auth/register`, `/auth/google`, `/auth/refresh`.

Domain models:

- `AuthSession`.

DTO cần dùng:

- Không lưu DTO trực tiếp, chỉ lưu token string và optional user snapshot nếu cần.

Repository methods:

- `TokenRepository.saveSession(session)`.
- `TokenRepository.getAccessToken()`.
- `TokenRepository.getRefreshToken()`.
- `TokenRepository.clearSession()`.
- `UserPreferencesRepository.setOnboardingSeen(value)`.
- `UserPreferencesRepository.isOnboardingSeen()`.

ViewModel functions:

- `SplashViewModel.checkSession()`.
- `OnboardingViewModel.completeOnboarding()`.
- `LoginViewModel.login()`.
- `RegisterViewModel.register()`.
- `ProfileSettingsViewModel.logout()`.

UiState/UiEvent/UiEffect:

- Navigation effect sau khi token được lưu/clear.

Compose components:

- Không có.

States:

- Loading khi đọc token ở splash.
- Error fallback nếu storage lỗi.
- Success khi session có/không có.

Acceptance criteria:

- Token không lưu plain DataStore.
- Logout clear token.
- Onboarding chỉ hiện lần đầu.

Test cases:

- Save/read/clear token.
- Onboarding flag true/false.
- Logout xong app về login.

### 3.6 Auth

Mục tiêu:

- Implement splash, onboarding, login, register theo Stitch.
- Kết nối email/password với backend.
- Google login để nice-to-have nếu OAuth chưa sẵn sàng.
- Forgot Password không tự tạo endpoint mới.

File cần tạo/sửa:

- Tạo `ui/screens/splash/SplashScreen.kt`.
- Tạo `ui/screens/onboarding/OnboardingScreen.kt`.
- Tạo `ui/screens/auth/LoginScreen.kt`.
- Tạo `ui/screens/auth/RegisterScreen.kt`.
- Tạo `presentation/viewmodel/splash/SplashViewModel.kt`.
- Tạo `presentation/viewmodel/onboarding/OnboardingViewModel.kt`.
- Tạo `presentation/viewmodel/auth/LoginViewModel.kt`.
- Tạo `presentation/viewmodel/auth/RegisterViewModel.kt`.
- Tạo `data/remote/dto/AuthDtos.kt`.
- Tạo `data/repository/AuthRepository.kt`.
- Tạo `domain/usecase/auth/LoginUseCase.kt`.
- Tạo `domain/usecase/auth/RegisterUseCase.kt`.
- Tạo `domain/usecase/auth/LogoutUseCase.kt`.

Screen Stitch liên quan:

- Splash Screen: `be5d4dab43884985b7c01758dc5286da`.
- Onboarding Screen: `f7c972080d1e42ffb609a40464fa6092`.
- Login Screen: `3d4f3ebc052b43eea183a9a10a38f62e`.
- Register Screen: `dc4f6f6c20fa4f3ca4ef6e67cf283165`.

Data/API liên quan từ `md/PLAN.md`:

- `POST /auth/register`.
- `POST /auth/login`.
- `POST /auth/google`.
- `POST /auth/refresh`.
- `POST /auth/logout`.
- `GET /users/me`.
- `PATCH /users/me`.

Domain models:

- `User`.
- `AuthSession`.

DTO cần dùng:

- `RegisterRequestDto(email, password, name)`.
- `LoginRequestDto(email, password)`.
- `GoogleLoginRequestDto(idToken)`.
- `AuthResponseDto(access_token, refresh_token, user)`.
- `UserDto`.
- `UpdateUserRequestDto(name, goal, level, daily_new_words)`.

Repository methods:

- `login(email, password): AppResult<AuthSession>`.
- `register(name, email, password): AppResult<AuthSession>`.
- `loginWithGoogle(idToken): AppResult<AuthSession>`.
- `refresh(refreshToken): AppResult<String>`.
- `logout(refreshToken): AppResult<Unit>`.
- `getMe(): AppResult<User>`.
- `updateMe(request): AppResult<User>`.

ViewModel functions:

- `SplashViewModel.checkSession()`.
- `OnboardingViewModel.onGetStarted()`.
- `OnboardingViewModel.onLoginClick()`.
- `LoginViewModel.onEmailChange(value)`.
- `LoginViewModel.onPasswordChange(value)`.
- `LoginViewModel.login()`.
- `LoginViewModel.onGoogleLoginClick()`.
- `RegisterViewModel.onNameChange(value)`.
- `RegisterViewModel.onEmailChange(value)`.
- `RegisterViewModel.onPasswordChange(value)`.
- `RegisterViewModel.onGoalChange(value)`.
- `RegisterViewModel.onLevelChange(value)`.
- `RegisterViewModel.register()`.

UiState/UiEvent/UiEffect:

- `LoginUiState(email, password, fieldErrors, isLoading, apiError)`.
- `RegisterUiState(name, email, password, goal, level, dailyNewWords, acceptedTerms, fieldErrors, isLoading, apiError)`.
- `AuthEffect.NavigateHome`.
- `AuthEffect.NavigateRegister`.
- `AuthEffect.NavigateLogin`.
- `AuthEffect.ShowSnackbar`.

Compose components:

- `MinLishLogo`.
- `MinLishTextField`.
- `MinLishPasswordField`.
- `MinLishDropdown`.
- `MinLishCheckboxRow`.
- `MinLishButton`.
- `MinLishTonalButton`.
- `SocialLoginButton`.

States:

- Loading: disable submit button, show progress.
- Empty: không áp dụng.
- Error: validation/API/network error.
- Success: save token, navigate home.

Acceptance criteria:

- User đăng ký bằng email/password được.
- User đăng nhập được.
- Token được lưu local.
- Auth screen giữ visual Stitch.
- Forgot Password nếu backend chưa có: hiển thị disabled hoặc ẩn trong v1, không gọi endpoint bịa.
- Google login nếu chưa đủ OAuth: để disabled/nice-to-have.

Test cases:

- Login email rỗng.
- Login password rỗng.
- Login API success.
- Login API wrong password.
- Register password dưới 6 ký tự.
- Register duplicate email.
- Register success lưu token và vào Home.

### 3.7 Home Dashboard

Mục tiêu:

- Hiển thị greeting, daily plan, due review, streak, accuracy, mini activity chart.
- Giữ layout Home Dashboard từ Stitch.

File cần tạo/sửa:

- Tạo `ui/screens/home/HomeScreen.kt`.
- Tạo `presentation/viewmodel/home/HomeViewModel.kt`.
- Tạo `domain/usecase/home/LoadHomeUseCase.kt`.
- Tạo `data/repository/AnalyticsRepository.kt`.
- Tạo `data/repository/LearningRepository.kt`.
- Tạo `data/remote/dto/AnalyticsDtos.kt`.
- Tạo `data/remote/dto/LearningDtos.kt`.
- Tạo `ui/components/DailyPlanCard.kt`.
- Tạo `ui/components/MiniActivityChart.kt`.
- Tạo `ui/components/InsightCard.kt`.

Screen Stitch liên quan:

- Home Dashboard: `bd93ded4673e4e9a86ae15849b2687a3`.

Data/API liên quan:

- `GET /analytics/dashboard`.
- `GET /learning/daily-plan`.
- `GET /analytics/activity`.

Domain models:

- `User`.
- `DailyLearningPlan`.
- `ProgressStats`.
- `DailyActivity`.

DTO cần dùng:

- `DashboardDto(learned_words, due_today, streak, accuracy, level_estimation)`.
- `DailyPlanDto(daily_goal, new_cards, due_reviews, total_available)`.
- `ActivityResponseDto(days)`.
- `DailyActivityDto(date, review_count, correct_count)`.

Repository methods:

- `analyticsRepository.getDashboard()`.
- `analyticsRepository.getActivity()`.
- `learningRepository.getDailyPlan()`.

ViewModel functions:

- `loadHome()`.
- `refresh()`.
- `onStartLearningClick()`.
- `onOpenDecksClick()`.
- `onRetry()`.

UiState/UiEvent/UiEffect:

- `HomeUiState(isLoading, dashboard, dailyPlan, activities, error, isRefreshing)`.
- `HomeEvent.Refresh`.
- `HomeEvent.StartLearning`.
- `HomeEffect.NavigateFlashcard`.
- `HomeEffect.ShowSnackbar`.

Compose components:

- `DailyPlanCard`.
- `StatCard`.
- `MiniActivityChart`.
- `InsightCard`.
- `MinLishBottomBar`.
- `LoadingStateView`.
- `ErrorStateView`.
- `EmptyStateView`.

States:

- Loading: skeleton cards.
- Empty: chưa có activity/review nào.
- Error: dashboard load fail, có retry.
- Success: hiển thị dashboard + plan.

Acceptance criteria:

- Home load được dashboard và daily plan.
- `Start Learning` mở Flashcard.
- Chart render được bằng Compose Canvas hoặc simple bars.
- Word Clubs trong Stitch là `Missing backend support`; v1 dùng static/nice-to-have hoặc ẩn.

Test cases:

- API success với activity có data.
- API success nhưng activity empty.
- Daily plan `due_reviews = 0`.
- Network error hiển thị retry.

### 3.8 Deck Management

Mục tiêu:

- Hiển thị danh sách deck public/user.
- Xem chi tiết deck.
- Tạo/sửa/xóa deck theo ownership.
- Giữ style Deck List, Deck Detail, Create Deck từ Stitch.

File cần tạo/sửa:

- Tạo `ui/screens/decks/DeckListScreen.kt`.
- Tạo `ui/screens/decks/DeckDetailScreen.kt`.
- Tạo `ui/screens/decks/CreateDeckScreen.kt`.
- Tạo `presentation/viewmodel/decks/DeckListViewModel.kt`.
- Tạo `presentation/viewmodel/decks/DeckDetailViewModel.kt`.
- Tạo `presentation/viewmodel/decks/CreateDeckViewModel.kt`.
- Tạo `data/repository/DeckRepository.kt`.
- Tạo `data/remote/dto/DeckDtos.kt`.
- Tạo `domain/model/VocabularyDeck.kt`.
- Tạo `domain/usecase/decks/LoadDecksUseCase.kt`.
- Tạo `domain/usecase/decks/LoadDeckDetailUseCase.kt`.
- Tạo `domain/usecase/decks/CreateDeckUseCase.kt`.
- Tạo `ui/components/DeckCard.kt`.
- Tạo `ui/components/SearchField.kt`.
- Tạo `ui/components/ProgressSummaryCard.kt`.
- Tạo `ui/components/DeckActionButton.kt`.

Screen Stitch liên quan:

- Deck List: `eea728fe28824df78bf25da7960e15e5`.
- Deck Detail: `be5cc20d11714924858e603012302973`.
- Create Deck: `8fe373dcb2f24a669ef5e8d6764d3d74`.

Data/API liên quan:

- `GET /decks`.
- `POST /decks`.
- `GET /decks/{deck_id}`.
- `PATCH /decks/{deck_id}`.
- `DELETE /decks/{deck_id}`.
- `GET /decks/{deck_id}/export`.
- `POST /decks/{deck_id}/import`.

Domain models:

- `VocabularyDeck`.
- `VocabularyWord`.
- `ImportResult`.

DTO cần dùng:

- `DeckDto(id, name, description, tags, is_public, item_count, source_name, source_unit)`.
- `CreateDeckRequestDto(name, description, tags)`.
- `UpdateDeckRequestDto(name, description, tags)`.
- `ImportResultDto(total_rows, success_count, failed_count, errors)`.

Repository methods:

- `getDecks()`.
- `getDeck(deckId)`.
- `createDeck(request)`.
- `updateDeck(deckId, request)`.
- `deleteDeck(deckId)`.
- `importDeck(deckId, file)`.
- `exportDeck(deckId)`.

ViewModel functions:

- `DeckListViewModel.loadDecks()`.
- `DeckListViewModel.onSearchChange(query)`.
- `DeckListViewModel.openDeck(deckId)`.
- `DeckListViewModel.openCreateDeck()`.
- `DeckDetailViewModel.load(deckId)`.
- `DeckDetailViewModel.onStartLearning()`.
- `DeckDetailViewModel.onAddWord()`.
- `DeckDetailViewModel.onDeleteDeck()`.
- `CreateDeckViewModel.onNameChange(value)`.
- `CreateDeckViewModel.onDescriptionChange(value)`.
- `CreateDeckViewModel.onTagAdd(value)`.
- `CreateDeckViewModel.createDeck()`.

UiState/UiEvent/UiEffect:

- `DeckListUiState(isLoading, query, decks, filteredDecks, error)`.
- `DeckDetailUiState(isLoading, deck, words, isReadOnly, error)`.
- `CreateDeckUiState(name, description, tags, fieldErrors, isSaving, error)`.
- Effects: navigate detail, navigate add word, show snackbar.

Compose components:

- `DeckCard`.
- `SearchField`.
- `TagChip`.
- `SmartReviewCard`.
- `ProgressSummaryCard`.
- `DeckActionButton`.
- `MinLishFab`.
- `MinLishBottomBar`.

States:

- Loading: deck skeleton list.
- Empty: chưa có deck.
- Search empty: query không match.
- Error: fail load decks/detail.
- Success: list/detail/form saved.

Acceptance criteria:

- Deck List hiển thị public seed decks và user decks.
- Mở deck detail bằng `deckId`.
- Public seed deck read-only: không hiện edit/delete destructive actions.
- Create deck gọi đúng `POST /decks`.
- `cover_image_url` trong Stitch là `Missing backend support`; v1 dùng placeholder/static hoặc đề xuất thêm field trước khi code.
- Smart Review card là `Missing backend support`; v1 static/nice-to-have.

Test cases:

- Load decks success.
- Load decks empty.
- Search local filter.
- Create deck validation: name bắt buộc.
- Delete own deck success.
- Public deck không cho edit/delete.

### 3.9 Word Management

Mục tiêu:

- Thêm/sửa/xóa vocabulary item.
- Form phải hỗ trợ các field trong backend plan và seed Anki mapping.

File cần tạo/sửa:

- Tạo `ui/screens/words/AddWordScreen.kt`.
- Tạo `ui/screens/words/EditWordScreen.kt`.
- Tạo `presentation/viewmodel/words/AddWordViewModel.kt`.
- Tạo `presentation/viewmodel/words/EditWordViewModel.kt`.
- Tạo `domain/model/VocabularyWord.kt`.
- Tạo `domain/usecase/words/CreateWordUseCase.kt`.
- Tạo `domain/usecase/words/UpdateWordUseCase.kt`.
- Tạo `domain/usecase/words/DeleteWordUseCase.kt`.
- Tạo `data/remote/dto/VocabularyItemDtos.kt`.
- Tạo `ui/components/WordForm.kt`.
- Tạo `ui/components/DeckSelector.kt`.
- Tạo `ui/components/WordCard.kt`.

Screen Stitch liên quan:

- Add Word Form: `3538149b32844c82be09a358bf4ed6ff`.
- Deck Detail word list.

Data/API liên quan:

- `GET /decks/{deck_id}/items`.
- `POST /decks/{deck_id}/items`.
- `PATCH /items/{item_id}`.
- `DELETE /items/{item_id}`.

Domain models:

- `VocabularyWord`.
- `VocabularyDeck`.

DTO cần dùng:

- `VocabularyItemDto`.
- `CreateVocabularyItemRequestDto`.
- `UpdateVocabularyItemRequestDto`.

Required fields:

- `word`.
- `meaning`.

Optional fields:

- `pronunciation`.
- `description`.
- `example`.
- `collocation`.
- `related_words`.
- `note`.
- `hint`.
- `image_url`.
- `word_audio_url`.
- `meaning_audio_url`.
- `example_audio_url`.
- `source_number`.

Repository methods:

- `getItems(deckId)`.
- `createItem(deckId, request)`.
- `updateItem(itemId, request)`.
- `deleteItem(itemId)`.

ViewModel functions:

- `loadDecksForSelector()`.
- `loadWord(wordId)`.
- `onDeckSelected(deckId)`.
- `onWordChange(value)`.
- `onPronunciationChange(value)`.
- `onMeaningChange(value)`.
- `onDescriptionChange(value)`.
- `onExampleChange(value)`.
- `onCollocationChange(value)`.
- `onRelatedWordsChange(value)`.
- `onNoteChange(value)`.
- `saveWord()`.
- `deleteWord()`.

UiState/UiEvent/UiEffect:

- `WordFormUiState(selectedDeckId, decks, form, fieldErrors, isSaving, error)`.
- `WordFormEffect.NavigateBack`.
- `WordFormEffect.ShowSnackbar`.

Compose components:

- `WordForm`.
- `DeckSelector`.
- `MinLishTextField`.
- `MinLishTextArea`.
- `SaveButton`.
- `WordCard`.

States:

- Loading: loading deck selector/word detail.
- Empty: không có deck để thêm từ.
- Error: load/save fail.
- Success: save thành công, navigate back hoặc clear form.

Acceptance criteria:

- Add Word form gọi đúng `POST /decks/{deck_id}/items`.
- Edit Word form gọi đúng `PATCH /items/{item_id}`.
- Không cho lưu nếu thiếu `word` hoặc `meaning`.
- Audio/image upload chưa có backend support; v1 chỉ hiển thị field/media nếu URL đã có từ seed data.

Test cases:

- Form validation word empty.
- Form validation meaning empty.
- Save success.
- API validation error.
- Edit existing word success.
- Delete word success với own deck.

### 3.10 Flashcard Learning

Mục tiêu:

- Hiển thị review cards.
- Cho user lật card và submit rating `again`, `hard`, `good`, `easy`.
- Backend là source of truth cho SM-2.

File cần tạo/sửa:

- Tạo `ui/screens/learning/FlashcardScreen.kt`.
- Tạo `presentation/viewmodel/learning/FlashcardViewModel.kt`.
- Tạo `domain/model/ReviewCard.kt`.
- Tạo `domain/model/ReviewRating.kt`.
- Tạo `domain/model/ReviewSchedule.kt`.
- Tạo `domain/usecase/learning/LoadReviewCardsUseCase.kt`.
- Tạo `domain/usecase/learning/SubmitReviewUseCase.kt`.
- Tạo `data/remote/dto/LearningDtos.kt`.
- Tạo `ui/components/FlashcardView.kt`.
- Tạo `ui/components/ReviewActionButton.kt`.
- Tạo `ui/components/SessionProgressBar.kt`.
- Tạo `ui/components/AudioButton.kt`.

Screen Stitch liên quan:

- Flashcard Learning: `63b8e048ace140af9cc15f0307816a1b`.

Data/API liên quan:

- `GET /learning/review-cards`.
- `POST /learning/reviews`.

Domain models:

- `ReviewCard`.
- `VocabularyWord`.
- `ReviewRating`.
- `ReviewSchedule`.
- `ReviewResult`.
- `ReviewSessionSummary`.

DTO cần dùng:

- `ReviewCardsResponseDto(cards)`.
- `ReviewCardDto(vocabulary_item_id, word, pronunciation, meaning, description, example, image_url, word_audio_url)`.
- `SubmitReviewRequestDto(vocabulary_item_id, rating, response_ms)`.
- `SubmitReviewResponseDto(vocabulary_item_id, rating, repetitions, interval_days, ease_factor, next_due_at, status)`.

Repository methods:

- `learningRepository.getReviewCards()`.
- `learningRepository.submitReview(request)`.

ViewModel functions:

- `loadCards(deckId: Long?)`.
- `showAnswer()`.
- `playAudio()`.
- `submitRating(rating)`.
- `moveNext()`.
- `finishSession()`.
- `retry()`.

UiState/UiEvent/UiEffect:

- `FlashcardUiState(isLoading, cards, currentIndex, isAnswerShown, isSubmitting, sessionSummary, error)`.
- `FlashcardEvent.ShowAnswer`.
- `FlashcardEvent.SubmitRating`.
- `FlashcardEffect.NavigateReviewResults`.
- `FlashcardEffect.ShowSnackbar`.

Compose components:

- `SessionProgressBar`.
- `FlashcardView`.
- `AudioButton`.
- `ReviewActionButton`.
- `MinLishButton`.

States:

- Loading: fetching cards.
- Empty: không có card đến hạn.
- Front: chỉ hiện word/pronunciation/image/audio.
- Back: hiện meaning/description/example/note.
- Submitting: disable rating buttons.
- Error: retry.
- Completed: navigate Review Results.

Acceptance criteria:

- User lật card được.
- Submit `again`, `hard`, `good`, `easy` đúng API values.
- Android không tự persist SM-2 schedule; chỉ nhận result từ backend.
- `part_of_speech` là `Missing backend support`; v1 ẩn chip hoặc hiển thị fallback nếu backend chưa thêm field.

Test cases:

- Load cards success.
- Load cards empty.
- Submit rating success chuyển card kế.
- Submit rating fail giữ card hiện tại.
- Session summary đếm total/correct/again/hard/good/easy.

### 3.11 Review Results

Mục tiêu:

- Tóm tắt phiên học sau Flashcard.
- Dùng local session summary nếu backend chưa có endpoint summary.

File cần tạo/sửa:

- Tạo `ui/screens/learning/ReviewResultsScreen.kt`.
- Tạo `presentation/viewmodel/learning/ReviewResultsViewModel.kt`.
- Tạo `domain/model/ReviewSessionSummary.kt`.
- Tạo `domain/usecase/learning/GetReviewSessionSummaryUseCase.kt`.
- Tạo `ui/components/CircularAccuracyIndicator.kt`.
- Tạo `ui/components/ResultStatCard.kt`.

Screen Stitch liên quan:

- Review Results: `20ad23ddfc6e4f0ea693322719580830`.

Data/API liên quan:

- Không có endpoint summary trong backend plan.
- Dữ liệu lấy từ local in-memory/session state sau khi submit reviews.
- Có thể refresh dashboard sau đó qua `GET /analytics/dashboard`.

Domain models:

- `ReviewSessionSummary`.
- `ReviewResult`.

DTO cần dùng:

- Không bắt buộc v1.

Repository methods:

- Không cần API riêng trong v1.
- Optional `analyticsRepository.getDashboard()` nếu muốn cập nhật số liệu.

ViewModel functions:

- `loadSummary(sessionId)`.
- `onReviewAgainClick()`.
- `onBackHomeClick()`.
- `onOpenProgressClick()`.

UiState/UiEvent/UiEffect:

- `ReviewResultsUiState(isLoading, summary, error)`.
- `ReviewResultsEffect.NavigateFlashcard`.
- `ReviewResultsEffect.NavigateHome`.
- `ReviewResultsEffect.NavigateProgress`.

Compose components:

- `CircularAccuracyIndicator`.
- `ResultStatCard`.
- `MinLishButton`.
- `MinLishTonalButton`.

States:

- Loading: nếu đọc summary async.
- Empty/error: session summary không tồn tại.
- Success: hiển thị total reviewed, accuracy, mastered/review soon.

Acceptance criteria:

- Sau khi học xong, app hiển thị summary đúng.
- Không gọi endpoint chưa có.
- Nếu app mất summary, fallback về Home với snackbar.

Test cases:

- Summary 20 words, 75% accuracy render đúng.
- Empty summary xử lý an toàn.
- Button học tiếp điều hướng Flashcard.
- Button Home điều hướng Home.

### 3.12 Progress Analytics

Mục tiêu:

- Hiển thị tiến độ học, streak, accuracy, retention, activity chart.
- Chart làm native Compose Canvas/simple bar.

File cần tạo/sửa:

- Tạo `ui/screens/progress/ProgressScreen.kt`.
- Tạo `presentation/viewmodel/progress/ProgressViewModel.kt`.
- Tạo `domain/model/ProgressStats.kt`.
- Tạo `domain/model/DailyActivity.kt`.
- Tạo `domain/model/RetentionStats.kt`.
- Tạo `domain/usecase/progress/LoadProgressUseCase.kt`.
- Tạo `ui/components/MetricCard.kt`.
- Tạo `ui/components/ProgressChartCard.kt`.
- Tạo `ui/components/BarChart.kt`.
- Tạo `ui/components/RetentionCard.kt`.
- Tạo `ui/components/SegmentedControl.kt`.

Screen Stitch liên quan:

- Progress Analytics: `216a8bf066f9490f80662128bbd449f7`.

Data/API liên quan:

- `GET /analytics/dashboard`.
- `GET /analytics/activity`.
- `GET /analytics/retention`.

Domain models:

- `ProgressStats`.
- `DailyActivity`.
- `RetentionStats`.

DTO cần dùng:

- `DashboardDto`.
- `ActivityResponseDto`.
- `DailyActivityDto`.
- `RetentionDto(retention_rate, good_or_easy_count, review_count)`.

Repository methods:

- `analyticsRepository.getDashboard()`.
- `analyticsRepository.getActivity(range?)`.
- `analyticsRepository.getRetention(range?)`.

ViewModel functions:

- `loadProgress()`.
- `onRangeSelected(range)`.
- `refresh()`.
- `retry()`.

UiState/UiEvent/UiEffect:

- `ProgressUiState(isLoading, selectedRange, stats, activities, retention, categoryBreakdown, error)`.
- `ProgressEvent.SelectRange`.
- `ProgressEffect.ShowSnackbar`.

Compose components:

- `MetricCard`.
- `SegmentedControl`.
- `BarChart`.
- `RetentionCard`.
- `ProgressChartCard`.
- `MinLishBottomBar`.

States:

- Loading: metric/card skeleton.
- Empty: chưa có review logs.
- Error: retry.
- Success: chart + metrics.

Acceptance criteria:

- Hiển thị dashboard metrics đúng backend.
- Activity chart render không crash với list rỗng.
- Category breakdown trong Stitch là `Missing backend support`; v1 dùng static/fallback hoặc ẩn.

Test cases:

- Activity 7 ngày render đúng.
- Retention zero review_count không chia cho 0.
- API error hiển thị retry.
- Range tab đổi state local.

### 3.13 Profile & Settings

Mục tiêu:

- Hiển thị profile, goal, level, daily new words, notification settings.
- Cho user logout.

File cần tạo/sửa:

- Tạo `ui/screens/profile/ProfileSettingsScreen.kt`.
- Tạo `presentation/viewmodel/profile/ProfileSettingsViewModel.kt`.
- Tạo `domain/usecase/profile/LoadProfileUseCase.kt`.
- Tạo `domain/usecase/profile/UpdateProfileUseCase.kt`.
- Tạo `data/repository/UserRepository.kt`.
- Tạo `data/remote/dto/UserDtos.kt`.
- Tạo `ui/components/ProfileHeader.kt`.
- Tạo `ui/components/SettingsSwitchRow.kt`.
- Tạo `ui/components/SettingsNavRow.kt`.
- Tạo `ui/components/StreakCard.kt`.

Screen Stitch liên quan:

- Profile & Settings: `57446103b63a4a40a46e39791cbf7c98`.

Data/API liên quan:

- `GET /users/me`.
- `PATCH /users/me`.
- `GET /notifications/preferences`.
- `PATCH /notifications/preferences`.
- `POST /auth/logout`.

Domain models:

- `User`.
- `NotificationSettings`.

DTO cần dùng:

- `UserDto`.
- `UpdateUserRequestDto`.
- `NotificationPreferencesDto`.
- `UpdateNotificationPreferencesRequestDto`.

Repository methods:

- `userRepository.getMe()`.
- `userRepository.updateMe(request)`.
- `notificationRepository.getPreferences()`.
- `notificationRepository.updatePreferences(request)`.
- `authRepository.logout()`.

ViewModel functions:

- `loadProfile()`.
- `onNameChange(value)`.
- `onGoalChange(value)`.
- `onLevelChange(value)`.
- `onDailyNewWordsChange(value)`.
- `saveProfile()`.
- `onReminderToggle(enabled)`.
- `onReminderTimeChange(time)`.
- `logout()`.

UiState/UiEvent/UiEffect:

- `ProfileSettingsUiState(isLoading, user, notificationSettings, isSavingProfile, isSavingNotification, error)`.
- `ProfileSettingsEffect.RequestNotificationPermission`.
- `ProfileSettingsEffect.NavigateLogin`.
- `ProfileSettingsEffect.ShowSnackbar`.

Compose components:

- `ProfileHeader`.
- `SettingsSwitchRow`.
- `SettingsNavRow`.
- `StreakCard`.
- `MinLishBottomBar`.

States:

- Loading: profile skeleton.
- Empty: không áp dụng nếu user bắt buộc có.
- Error: load/save fail.
- Success: profile/settings loaded.
- Saving row-level: disable switch/field đang lưu.

Acceptance criteria:

- Load user profile và notification preferences.
- Update profile gọi `PATCH /users/me`.
- Toggle notification gọi `PATCH /notifications/preferences`.
- Logout clear token và về Login.
- Support/Feedback rows trong Stitch: static/nice-to-have nếu backend chưa có.

Test cases:

- Load profile success.
- Save profile validation daily_new_words min/max.
- Toggle reminder success.
- Logout success.
- API 401 về login.

### 3.14 Notifications

Mục tiêu:

- Nhắc học local bằng WorkManager.
- Backend chỉ lưu preference trong v1.
- Không dùng FCM trong v1 trừ khi team đổi scope.

File cần tạo/sửa:

- Tạo `core/notification/NotificationScheduler.kt`.
- Tạo `core/notification/DailyReminderWorker.kt`.
- Tạo `core/notification/NotificationPermissionHelper.kt`.
- Tạo `data/repository/NotificationRepository.kt`.
- Tạo `data/remote/dto/NotificationDtos.kt`.
- Sửa `AndroidManifest.xml` để thêm permission `POST_NOTIFICATIONS`.

Screen Stitch liên quan:

- Profile & Settings.
- Splash/Home nếu notification mở app vào learning.

Data/API liên quan:

- `GET /notifications/preferences`.
- `PATCH /notifications/preferences`.
- Worker có thể gọi `GET /learning/daily-plan` để biết `due_reviews`.

Domain models:

- `NotificationSettings`.
- `DailyLearningPlan`.

DTO cần dùng:

- `NotificationPreferencesDto(daily_time, timezone, push_enabled, email_enabled)`.
- `UpdateNotificationPreferencesRequestDto`.
- `DailyPlanDto`.

Repository methods:

- `notificationRepository.getPreferences()`.
- `notificationRepository.updatePreferences(settings)`.
- `learningRepository.getDailyPlan()`.

ViewModel functions:

- `ProfileSettingsViewModel.onReminderToggle(enabled)`.
- `ProfileSettingsViewModel.onReminderTimeChange(time)`.

UiState/UiEvent/UiEffect:

- `ProfileSettingsEffect.RequestNotificationPermission`.
- `ProfileSettingsEffect.ShowSnackbar`.

Compose components:

- `SettingsSwitchRow`.
- `TimePickerRow` hoặc native Material time picker nếu thêm dependency.

States:

- Loading: preferences loading.
- Error: schedule/save fail.
- Success: scheduled/cancelled.

Acceptance criteria:

- Android 13+ xin permission khi bật reminder.
- Bật reminder schedule WorkManager local.
- Tắt reminder cancel WorkManager.
- Notification tap mở app vào Flashcard hoặc Home.
- Email reminder là nice-to-have, không chặn app.

Test cases:

- Enable reminder with permission granted.
- Enable reminder with permission denied.
- Disable reminder cancels worker.
- Worker no due reviews: không show notification.
- Worker due reviews > 0: show notification.

### 3.15 Testing & QA

Mục tiêu:

- Đảm bảo từng phase build được và có test/manual QA rõ ràng.
- Tập trung vào flow demo đồ án.

File cần tạo/sửa:

- Tạo/sửa test trong `app/src/test/...`.
- Tạo/sửa Compose test trong `app/src/androidTest/...`.
- Tạo `docs/demo_script.md` nếu team cần.
- Không bắt buộc tạo docs trong phase đầu nếu chưa được giao.

Screen Stitch liên quan:

- Tất cả màn hình.

Data/API liên quan:

- Fake repositories cho ViewModel tests.
- Backend local theo `md/PLAN.md`.

Domain models:

- Tất cả domain models chính.

DTO cần dùng:

- Test DTO mapping cho auth/deck/learning/analytics.

Repository methods:

- Test các repository quan trọng: Auth, Deck, Learning, Analytics, Notification.

ViewModel functions:

- Test Login, Register, Home, DeckList, DeckDetail, Flashcard, Progress, Profile.

UiState/UiEvent/UiEffect:

- Verify loading -> success/error.
- Verify one-time navigation effects.

Compose components:

- Smoke test Login, Home, Deck List, Flashcard.

States:

- Loading/empty/error/success phải có manual test case.

Acceptance criteria:

- `./gradlew :app:assembleDebug` chạy được sau mỗi phase.
- Unit tests chính chạy được nếu đã có test.
- Manual QA demo flow không crash.

Test cases:

- Register -> Login -> Home.
- Home start learning.
- Deck List -> Deck Detail.
- Add Word.
- Flashcard submit Good.
- Review Results.
- Progress load.
- Profile notification toggle.

## 4. Thứ tự triển khai theo phase

### Phase 1: Theme + project structure + navigation skeleton

Input cần có:

- Stitch design tokens.
- Screen list từ Stitch MCP.
- Android skeleton hiện tại.

Output mong đợi:

- Package structure được tạo.
- Theme MinLish thay theme tím.
- Route graph skeleton.
- Static/fake screens cơ bản để kiểm tra navigation.

Những file thay đổi:

- `app/build.gradle.kts`.
- `gradle/libs.versions.toml`.
- `MainActivity.kt`.
- `ui/theme/*`.
- `ui/MinLishApp.kt`.
- `navigation/*`.
- `ui/components/*`.

Điều kiện hoàn thành:

- App build được.
- Splash/Login/Home fake route chạy được.
- Bottom nav hiển thị đúng màn hình fake.
- Không có API call thật.

Cách kiểm thử:

- Chạy build.
- Mở emulator, kiểm tra navigation skeleton.
- So sánh màu/shape với Stitch.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 2: Core/network/auth/token storage

Input cần có:

- API base URL.
- Auth token format.
- Auth endpoints trong `md/PLAN.md`.

Output mong đợi:

- Retrofit/OkHttp/Moshi setup.
- Token storage.
- AppResult/AppError.
- Auth interceptor/refresh flow skeleton.

Những file thay đổi:

- `core/network/*`.
- `core/result/*`.
- `core/storage/*`.
- `data/remote/api/AuthApi.kt`.
- `data/remote/dto/AuthDtos.kt`.
- `data/repository/AuthRepository.kt`.

Điều kiện hoàn thành:

- Repository có thể gọi fake/mock AuthApi.
- Token save/read/clear hoạt động.
- Không gọi API trong Composable.

Cách kiểm thử:

- Unit test token storage.
- Unit test error mapper.
- Mock 401 refresh flow.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 3: Auth UI

Input cần có:

- Stitch Splash, Onboarding, Login, Register.
- AuthRepository từ Phase 2.

Output mong đợi:

- Splash/onboarding/login/register native Compose.
- Login/register connect API.
- Token lưu sau success.

Những file thay đổi:

- `ui/screens/splash/*`.
- `ui/screens/onboarding/*`.
- `ui/screens/auth/*`.
- `presentation/viewmodel/splash/*`.
- `presentation/viewmodel/onboarding/*`.
- `presentation/viewmodel/auth/*`.
- `navigation/AppNavGraph.kt`.

Điều kiện hoàn thành:

- User login/register được nếu backend chạy.
- UI có loading/error/validation.
- Forgot Password xử lý disabled/ẩn nếu chưa có backend.

Cách kiểm thử:

- Manual: register -> home.
- Manual: login sai password -> error.
- Unit test LoginViewModel/RegisterViewModel.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 4: Home dashboard

Input cần có:

- `GET /analytics/dashboard`.
- `GET /learning/daily-plan`.
- `GET /analytics/activity`.

Output mong đợi:

- Home Dashboard giữ style Stitch.
- Hiển thị dashboard, daily plan, activity mini chart.

Những file thay đổi:

- `ui/screens/home/*`.
- `presentation/viewmodel/home/*`.
- `data/remote/api/AnalyticsApi.kt`.
- `data/remote/api/LearningApi.kt`.
- `data/remote/dto/AnalyticsDtos.kt`.
- `data/remote/dto/LearningDtos.kt`.
- `data/repository/AnalyticsRepository.kt`.
- `data/repository/LearningRepository.kt`.

Điều kiện hoàn thành:

- Home load success/error/empty.
- Start Learning điều hướng Flashcard.
- Word Clubs không phụ thuộc backend trong v1.

Cách kiểm thử:

- Fake repository test.
- Manual với backend local.
- Kiểm tra chart rỗng không crash.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 5: Deck list/detail/create

Input cần có:

- `GET /decks`.
- `POST /decks`.
- `GET /decks/{deck_id}`.
- `GET /decks/{deck_id}/items`.

Output mong đợi:

- Deck List.
- Deck Detail.
- Create Deck.
- Local search/filter cơ bản.

Những file thay đổi:

- `ui/screens/decks/*`.
- `presentation/viewmodel/decks/*`.
- `data/remote/api/DeckApi.kt`.
- `data/remote/dto/DeckDtos.kt`.
- `data/repository/DeckRepository.kt`.
- `domain/model/VocabularyDeck.kt`.

Điều kiện hoàn thành:

- Hiển thị seed decks/user decks.
- Tạo deck thành công.
- Mở deck detail thấy items.
- Public deck read-only.

Cách kiểm thử:

- Manual: Decks -> Detail -> Create.
- Unit test DeckListViewModel.
- Unit test CreateDeckViewModel validation.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 6: Add word/edit word

Input cần có:

- `POST /decks/{deck_id}/items`.
- `PATCH /items/{item_id}`.
- `DELETE /items/{item_id}`.

Output mong đợi:

- Add Word Form.
- Edit Word.
- Delete Word nếu own deck.

Những file thay đổi:

- `ui/screens/words/*`.
- `presentation/viewmodel/words/*`.
- `data/remote/dto/VocabularyItemDtos.kt`.
- `domain/model/VocabularyWord.kt`.
- `domain/usecase/words/*`.
- `ui/components/WordForm.kt`.

Điều kiện hoàn thành:

- Add word save được.
- Edit word save được.
- Validate word/meaning bắt buộc.
- Không expose edit/delete cho public seed deck.

Cách kiểm thử:

- Manual add word.
- Manual edit word.
- Unit test validation.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 7: Flashcard + SRS review submit

Input cần có:

- `GET /learning/review-cards`.
- `POST /learning/reviews`.
- SM-2 backend đã hoạt động.

Output mong đợi:

- Flashcard screen native Compose.
- Show answer.
- Rating buttons submit đúng.
- Session summary local.

Những file thay đổi:

- `ui/screens/learning/FlashcardScreen.kt`.
- `presentation/viewmodel/learning/FlashcardViewModel.kt`.
- `domain/model/ReviewCard.kt`.
- `domain/model/ReviewRating.kt`.
- `domain/model/ReviewSchedule.kt`.
- `domain/usecase/learning/*`.
- `ui/components/FlashcardView.kt`.
- `ui/components/ReviewActionButton.kt`.

Điều kiện hoàn thành:

- Submit Again/Hard/Good/Easy đúng API value.
- Backend trả next schedule và UI chuyển card.
- Empty due cards hiển thị đúng.

Cách kiểm thử:

- Manual học 1 card.
- Unit test submit rating.
- Unit test completed session.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 8: Review results

Input cần có:

- Local `ReviewSessionSummary` từ Phase 7.

Output mong đợi:

- Review Results screen giữ style Stitch.
- Hiển thị total reviewed, accuracy, review soon/mastered.

Những file thay đổi:

- `ui/screens/learning/ReviewResultsScreen.kt`.
- `presentation/viewmodel/learning/ReviewResultsViewModel.kt`.
- `domain/model/ReviewSessionSummary.kt`.
- `ui/components/CircularAccuracyIndicator.kt`.
- `ui/components/ResultStatCard.kt`.

Điều kiện hoàn thành:

- Flashcard completed -> Review Results.
- Không gọi endpoint summary chưa có.
- Button Home/Học tiếp hoạt động.

Cách kiểm thử:

- Manual complete session.
- Unit test summary calculation.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 9: Progress analytics

Input cần có:

- `GET /analytics/dashboard`.
- `GET /analytics/activity`.
- `GET /analytics/retention`.

Output mong đợi:

- Progress screen native Compose.
- Chart bằng Canvas/simple bars.
- Retention và stats cards.

Những file thay đổi:

- `ui/screens/progress/*`.
- `presentation/viewmodel/progress/*`.
- `domain/model/ProgressStats.kt`.
- `domain/model/DailyActivity.kt`.
- `domain/model/RetentionStats.kt`.
- `ui/components/BarChart.kt`.
- `ui/components/RetentionCard.kt`.

Điều kiện hoàn thành:

- Metrics load từ backend.
- Empty analytics không crash.
- Category breakdown xử lý fallback/ẩn nếu backend chưa hỗ trợ.

Cách kiểm thử:

- Manual load analytics.
- Unit test ProgressViewModel.
- Compose smoke chart với empty list.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 10: Profile/settings/notifications

Input cần có:

- `GET /users/me`.
- `PATCH /users/me`.
- `GET /notifications/preferences`.
- `PATCH /notifications/preferences`.

Output mong đợi:

- Profile & Settings screen.
- Update profile.
- Notification preference.
- WorkManager local notification.

Những file thay đổi:

- `ui/screens/profile/*`.
- `presentation/viewmodel/profile/*`.
- `data/repository/UserRepository.kt`.
- `data/repository/NotificationRepository.kt`.
- `core/notification/*`.
- `AndroidManifest.xml`.

Điều kiện hoàn thành:

- Load/save profile.
- Toggle reminder schedule/cancel.
- Logout clear token.
- Android 13+ permission handled.

Cách kiểm thử:

- Manual edit profile.
- Manual toggle reminder.
- Manual logout.
- Unit test notification scheduler wrapper nếu có fake.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Phase 11: Polish/testing

Input cần có:

- Các phase trước đã chạy được.
- Backend local có seed data.

Output mong đợi:

- Loading/error/empty states đồng bộ.
- Compose previews cho components.
- Unit tests ViewModel/repository chính.
- Demo flow ổn định.

Những file thay đổi:

- Test files trong `app/src/test`.
- Android test files trong `app/src/androidTest`.
- Optional docs/report nếu team giao.

Điều kiện hoàn thành:

- Project build.
- Core tests pass.
- Demo script chạy được: register -> home -> deck -> flashcard -> results -> progress -> settings.

Cách kiểm thử:

- Manual end-to-end.
- Unit tests.
- Compose smoke tests.

Lệnh nên chạy:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

## 5. Các điểm chưa chắc chắn cần confirm

| Điểm cần confirm | Hiện trạng | Rủi ro nếu không confirm | Xử lý v1 đề xuất |
|---|---|---|---|
| `part_of_speech` | Stitch Flashcard có chip loại từ, backend schema chưa có. | UI cần data nhưng API không trả. | Ẩn khỏi UI hoặc fallback null. Nếu muốn hiện thật thì thêm API/backend field trước khi Android code. |
| `cover_image_url` | Create Deck có cover photo, backend deck schema chưa có. | Form tạo deck không có nơi lưu cover. | Static/fallback placeholder trong v1. Nếu bắt buộc upload cover thì thêm backend field + upload flow. |
| Forgot Password | Login screen có link, backend không có endpoint. | Bấm vào không xử lý được. | Ẩn hoặc disabled trong v1; để nice-to-have. |
| Smart Review | Deck List có Smart Review card, backend chưa có AI/smart endpoint riêng. | Dễ bịa logic ngoài scope. | Static/nice-to-have; có thể route sang `GET /learning/review-cards` nếu chỉ là học due cards. |
| Word Clubs | Home có Word Clubs, backend không có social/community. | Scope phình to. | Ẩn khỏi v1 hoặc static decorative card không click. |
| Analytics category breakdown | Progress có breakdown dạng category, backend chỉ có dashboard/activity/retention. | Không có data thật. | Static/fallback hoặc ẩn; nếu muốn thật thì thêm endpoint sau. |
| Review session summary endpoint | Backend chưa định nghĩa endpoint summary. | Review Results không có nguồn API. | Dùng local session summary trong Android v1. |
| Pagination/search/sort | Backend chưa chốt query params. | Deck/word list có thể lệch API. | Local search/sort v1 với data đã load; confirm trước khi thêm query API. |
| Google login | Có endpoint trong plan nhưng OAuth setup là nice-to-have. | Mất thời gian cấu hình, dễ chặn demo. | Email/password là must-have; Google login disabled/nice-to-have nếu chưa có client ID. |
| Notification local-only hay backend-driven | `md/PLAN.md` chốt WorkManager local, backend lưu preferences. | Có thể nhầm sang FCM/email. | V1 local-only bằng WorkManager; backend-driven/FCM để nice-to-have. |

## 6. Nguyên tắc code

- Không dùng WebView.
- Không copy HTML Stitch trực tiếp vào Android.
- Dựng UI bằng native Jetpack Compose.
- Không gọi API trực tiếp trong Composable.
- Composable chỉ nhận `UiState` và callback.
- ViewModel gọi UseCase/Repository, không gọi Retrofit trực tiếp nếu có Repository phù hợp.
- ViewModel không phụ thuộc Android `Context` trừ khi thật cần; notification permission/scheduler nên đi qua abstraction.
- Không over-engineering: chưa dùng Hilt ở v1, dùng manual `AppContainer` nếu đủ.
- Reusable components nhỏ, dễ preview.
- UI phải giữ style Stitch: color, spacing, shape, typography, bottom nav.
- Data phải theo backend plan trong `md/PLAN.md`.
- Không tự ý đổi API contract khi chưa ghi rõ.
- Không tự bịa endpoint cho feature Stitch chưa có backend support.
- Không sửa file ngoài phạm vi phase.
- Không refactor unrelated code.
- Sau mỗi phase phải build/test nếu có thể.
- Nếu build/test không chạy được, phải báo lý do rõ ràng.
- Public seed deck là read-only với user thường.
- Backend là source of truth cho SM-2 schedule.
- Android chỉ hiển thị media nếu URL có sẵn; thiếu media thì text-first UI vẫn chạy.

## 7. Checklist trước khi code

Trước mỗi phase, AI/code agent phải làm:

- Đọc lại phase tương ứng trong `MODULE_TASK_PLAN.md`.
- Đọc file liên quan trong `md/PLAN.md`.
- Đọc phần tương ứng trong `IMPLEMENTATION_PLAN.md`.
- Kiểm tra existing code bằng `rg --files` và đọc file hiện có trước khi sửa.
- Kiểm tra Stitch screen/design tokens liên quan.
- Kiểm tra API/data contract trong `md/PLAN.md`.
- Xác định rõ file nào tạo, file nào sửa.
- Chỉ sửa đúng file cần sửa.
- Không tự ý refactor ngoài phạm vi.
- Không đổi API contract nếu chưa được duyệt.
- Build/test sau khi sửa.
- Báo lại:
  - File đã tạo.
  - File đã sửa.
  - Chức năng đã xong.
  - Chức năng chưa làm.
  - Test/build đã chạy.
  - Lỗi còn lại nếu có.

Checklist riêng khi làm UI:

- Không dùng WebView.
- Không dùng screenshot làm UI.
- Không paste HTML/Tailwind vào Compose.
- Dùng component nhỏ.
- Kiểm tra loading/empty/error/success.
- Kiểm tra text không tràn trên mobile width 390dp.
- Kiểm tra bottom nav không che CTA/form.

Checklist riêng khi làm data/API:

- DTO dùng snake_case mapping theo backend.
- Domain model dùng camelCase.
- Repository trả `AppResult`.
- 401 đi qua refresh flow.
- Error response parse theo `{ detail, code }`.
- Date/time parse ISO-8601.

## 8. Task breakdown cho team

| Task name | Role phù hợp | Input | Output | Dependencies | Definition of Done |
|---|---|---|---|---|---|
| Chốt Android architecture skeleton | Android Architecture/Data | Repo skeleton, plan files | Package structure, AppContainer plan, route skeleton | Phase 1 | Build pass, không có API call thật, route fake chạy được |
| Implement MinLish theme tokens | Android UI/Compose | Stitch Design System tokens | Theme colors/type/shape/spacing | Không có | UI không còn màu tím template, preview components khớp Stitch |
| Build reusable component library | Android UI/Compose | Stitch HTML/screens | Buttons, cards, fields, chips, bottom bar | Theme tokens | Components preview được, không phụ thuộc repository |
| Implement navigation graph | Android UI/Compose | Route plan | Auth graph, main tabs, detail routes | Theme skeleton | Route args hoạt động, bottom nav ẩn/hiện đúng |
| Implement result/error core | Android Architecture/Data | Common error format | `AppResult`, `AppError`, mapper | Không có | Unit test error mapper pass |
| Implement Retrofit APIs | Android Architecture/Data | `md/PLAN.md` API contract | Api interfaces + DTOs | Gradle deps | DTO names/fields khớp contract, không bịa endpoint |
| Implement token storage | Android Architecture/Data | Auth response contract | Encrypted token storage + preferences | Core deps | Save/read/clear token test pass |
| Implement auth repository | Android Architecture/Data | AuthApi, TokenStorage | AuthRepository | Retrofit + storage | Login/register save token, refresh flow test được |
| Implement Splash/Onboarding/Login/Register UI | Android UI/Compose | Stitch auth screens | Native Compose auth screens | Theme/navigation/auth repo | Login/register flow chạy, loading/error có đủ |
| Backend auth endpoints | Backend/API | `md/PLAN.md` auth contract | Register/login/refresh/logout/me | Backend foundation | Swagger có endpoint, tests pass |
| Backend seed Anki import | Backend/API | `.apkg` data file | 30 decks, 600 items, media mapping | DB schema | Import đúng sample `anxious`, seed deck read-only |
| Backend deck/item APIs | Backend/API | DB schema, API contract | Deck/item CRUD | Auth backend | Ownership/public rules đúng, tests pass |
| Implement Deck list/detail/create | Android UI/Compose | Stitch deck screens, DeckRepository | Deck UI + ViewModels | Theme/navigation/deck API | Public/user decks hiển thị, create deck chạy |
| Implement Word form | Android UI/Compose | Stitch Add Word screen | Add/Edit Word UI + ViewModels | Deck API | Validate word/meaning, save/edit/delete chạy |
| Backend learning/SRS APIs | Backend/API | SM-2 rules | Daily plan, review cards, submit review | Deck/item data | SM-2 unit tests pass, next_due_at đúng |
| Implement Flashcard learning | Android UI/Compose | Stitch Flashcard screen | Flashcard UI + rating submit | Learning API | Again/Hard/Good/Easy submit đúng, completed session có summary |
| Implement Review Results | Android UI/Compose | Stitch Review Results | Results UI | Flashcard summary | Summary đúng, không gọi endpoint chưa có |
| Backend analytics APIs | Backend/API | Review logs/study sessions | Dashboard/activity/retention | Learning APIs | Metrics đúng qua tests |
| Implement Progress Analytics | Android UI/Compose | Stitch Progress screen | Metrics + chart native Compose | Analytics API | Empty/data/error states đúng |
| Backend notification preferences | Backend/API | Notification schema | GET/PATCH preferences | Auth backend | Preference lưu đúng timezone/time |
| Implement Profile Settings | Android UI/Compose | Stitch Profile screen | Profile/settings UI | User/notification APIs | Load/save profile, logout, toggle reminder |
| Implement WorkManager reminder | Android Architecture/Data | Notification settings | Local notification scheduler | Profile settings | Schedule/cancel hoạt động, permission handled |
| Test ViewModels | QA/Tester + Android | Fake repositories | Unit test suite | ViewModels | Loading/success/error covered |
| Manual end-to-end QA | QA/Tester | Backend local + app | Bug list + pass/fail report | Full integration | Demo flow chạy ổn định |
| Update report/demo docs | Documentation/Report | Final app behavior | Setup guide, demo script, screenshots | QA pass | Team có tài liệu thuyết trình |

## 9. Ghi chú triển khai cho AI/code agent sau này

Khi bắt đầu code từng phase, prompt nên chỉ định rõ:

- Phase nào đang làm.
- File nào được phép sửa.
- File nào không được sửa.
- API contract lấy từ đâu.
- Stitch screen nào là visual reference.
- Build/test command bắt buộc chạy.

Ví dụ cách giao phase:

```txt
Hãy implement Phase 1 trong MODULE_TASK_PLAN.md.
Chỉ được sửa Gradle, MainActivity, ui/theme, ui/components, navigation.
Không implement network/auth thật.
Giữ style Stitch theo design_system_tokens.md.
Sau khi sửa chạy ./gradlew :app:assembleDebug và báo kết quả.
```

Những điểm phải hỏi lại trước khi code production:

- Có thêm `part_of_speech` vào backend không?
- Có thêm `cover_image_url` vào deck không?
- Forgot Password ẩn hay làm thật?
- Google login có phải MVP không?
- Smart Review và Word Clubs có ẩn khỏi v1 không?
- Pagination/search/sort làm local hay API-backed?
- Notification giữ local-only hay mở rộng FCM/backend-driven?
