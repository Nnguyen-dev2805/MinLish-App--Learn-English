# PROFILE_CODE_FLOW.md

# Luồng code màn hình Profile & Settings - MinLish Vocabulary App

Tài liệu này giải thích luồng code của màn hình Profile theo hướng dễ học lại và dễ trình bày đồ án. Mục tiêu là hiểu được từ giao diện Android Jetpack Compose đến ViewModel, UseCase, Repository, Retrofit API, backend FastAPI và database.

## 1. Mục tiêu của màn hình Profile

Màn hình Profile hiện xử lý các nhóm chức năng chính:

1. Hiển thị thông tin người dùng:
   - Tên người dùng.
   - Email.
   - Learning goal.
   - Current level.
   - Daily new words.

2. Cập nhật thông tin profile:
   - Sửa full name.
   - Chọn learning goal.
   - Chọn current level.
   - Nhập số từ mới muốn học mỗi ngày.
   - Bấm `Save profile` để lưu lên backend.

3. Cài đặt nhắc học:
   - Bật/tắt thông báo local trên máy bằng `Due word notifications`.
   - Bật/tắt email reminder bằng `Email reminder`.
   - Nhập giờ nhắc học theo định dạng `HH:mm`.
   - Bấm `Save reminders` để lưu settings lên backend và schedule notification local.

4. Refresh:
   - Bấm nút refresh để tải lại profile và notification settings từ backend.

5. Logout:
   - Bấm `Log out`.
   - Gọi backend logout nếu có refresh token.
   - Xoá token local.
   - Huỷ local reminder.
   - Điều hướng về Login.

## 2. File chính cần đọc

### Android UI

- `app/src/main/java/com/example/minlishapp_learnenglish/ui/screens/profile/ProfileSettingsScreen.kt`

Đây là file dựng giao diện Profile bằng Jetpack Compose.

### Android ViewModel

- `app/src/main/java/com/example/minlishapp_learnenglish/presentation/viewmodel/profile/ProfileViewModel.kt`

Đây là nơi giữ state, xử lý event từ UI, gọi use case và phát effect như snackbar/navigation.

### Android UseCase

- `app/src/main/java/com/example/minlishapp_learnenglish/domain/usecase/profile/ProfileUseCases.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/domain/usecase/notification/NotificationUseCases.kt`

Các use case là lớp trung gian mỏng giữa ViewModel và Repository.

### Android Repository

- `app/src/main/java/com/example/minlishapp_learnenglish/data/repository/AuthRepository.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/repository/NotificationRepository.kt`

Repository gọi Retrofit API và map DTO sang domain model.

### Android Retrofit API

- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/api/AuthApi.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/api/NotificationApi.kt`

Đây là nơi khai báo endpoint Android sẽ gọi.

### Android DTO / Domain model

- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/dto/AuthDtos.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/data/remote/dto/NotificationDtos.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/domain/model/User.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/domain/model/NotificationSettings.kt`

DTO là dữ liệu theo JSON backend. Domain model là dữ liệu Kotlin sạch để app dùng.

### Android local reminder

- `app/src/main/java/com/example/minlishapp_learnenglish/core/notification/ReminderScheduler.kt`
- `app/src/main/java/com/example/minlishapp_learnenglish/core/notification/DailyReminderWorker.kt`

Hai file này xử lý thông báo local trên thiết bị bằng WorkManager.

### Navigation

- `app/src/main/java/com/example/minlishapp_learnenglish/navigation/AppNavGraph.kt`

File này tạo `ProfileViewModel`, truyền state/callback xuống `ProfileSettingsScreen`, và xử lý effect điều hướng.

### Dependency container

- `app/src/main/java/com/example/minlishapp_learnenglish/core/AppContainer.kt`

File này khởi tạo API, repository, use case, reminder scheduler rồi inject thủ công vào ViewModel.

### Backend

- `backend/app/api/v1/users.py`
- `backend/app/api/v1/notifications.py`
- `backend/app/api/v1/auth.py`
- `backend/app/services/auth_service.py`
- `backend/app/services/notification_service.py`
- `backend/app/models/user.py`
- `backend/app/models/notification_preference.py`
- `backend/app/schemas/user.py`
- `backend/app/schemas/notification.py`

## 3. Kiến trúc tổng quan

Luồng chuẩn của màn hình Profile:

```text
ProfileSettingsScreen
-> ProfileViewModel
-> UseCase
-> Repository
-> Retrofit API
-> FastAPI backend
-> PostgreSQL
```

Với notification local:

```text
ProfileSettingsScreen
-> ProfileViewModel
-> ReminderScheduler
-> WorkManager
-> DailyReminderWorker
-> Android Notification
```

Điểm quan trọng:

- Composable không gọi API trực tiếp.
- UI chỉ nhận `ProfileUiState` và gửi callback.
- ViewModel quyết định khi nào gọi backend, khi nào validate, khi nào show snackbar.
- Repository là nơi gọi Retrofit.
- Backend lưu dữ liệu user và notification preferences vào PostgreSQL.

## 4. Luồng khởi tạo màn hình Profile

Trong `AppNavGraph.kt`, route `Routes.Profile` tạo `ProfileViewModel`.

Đoạn ý tưởng:

```kotlin
ProfileViewModel(
    getProfileUseCase = appContainer.getProfileUseCase,
    updateProfileUseCase = appContainer.updateProfileUseCase,
    getNotificationSettingsUseCase = appContainer.getNotificationSettingsUseCase,
    updateNotificationSettingsUseCase = appContainer.updateNotificationSettingsUseCase,
    logoutUseCase = appContainer.logoutUseCase,
    reminderScheduler = appContainer.reminderScheduler
)
```

Sau đó:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

Ý nghĩa:

- ViewModel giữ `uiState`.
- Compose subscribe state bằng `collectAsState()`.
- Khi ViewModel update state, UI tự render lại.

Khi `ProfileViewModel` được tạo, block `init` chạy:

```kotlin
init {
    loadProfileAndSettings()
}
```

Nghĩa là vừa mở màn Profile thì app tự gọi backend để lấy:

1. Thông tin user từ `/users/me`.
2. Cài đặt notification từ `/notifications/preferences`.

## 5. ProfileUiState là gì?

File:

```text
ProfileViewModel.kt
```

`ProfileUiState` là data class chứa toàn bộ dữ liệu UI cần để vẽ màn hình:

```kotlin
data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isSavingNotifications: Boolean = false,
    val isLoggingOut: Boolean = false,
    val userId: Long? = null,
    val email: String = "",
    val name: String = "",
    val goal: String = "General English",
    val level: String = "A1 Beginner",
    val dailyNewWordsInput: String = "10",
    val dailyTime: String = "20:00",
    val timezone: String = "Asia/Ho_Chi_Minh",
    val emailEnabled: Boolean = false,
    val pushEnabled: Boolean = true,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val dailyNewWordsError: String? = null,
    val dailyTimeError: String? = null
)
```

Các nhóm field:

- `isLoading`, `isRefreshing`, `isSavingProfile`, `isSavingNotifications`, `isLoggingOut`: điều khiển loading UI.
- `email`, `name`, `goal`, `level`, `dailyNewWordsInput`: dữ liệu form account.
- `dailyTime`, `timezone`, `emailEnabled`, `pushEnabled`: dữ liệu form reminders.
- `errorMessage`, `nameError`, `dailyNewWordsError`, `dailyTimeError`: lỗi hiển thị trên UI.

Computed property:

```kotlin
val hasProfile: Boolean
    get() = userId != null
```

Nếu `userId != null` nghĩa là đã tải được profile.

## 6. ProfileEvent là gì?

`ProfileEvent` biểu diễn các hành động từ người dùng gửi lên ViewModel:

```kotlin
sealed interface ProfileEvent {
    data object Retry
    data object Refresh
    data class NameChanged(val value: String)
    data class GoalChanged(val value: String)
    data class LevelChanged(val value: String)
    data class DailyNewWordsChanged(val value: String)
    data class DailyTimeChanged(val value: String)
    data class EmailEnabledChanged(val value: Boolean)
    data class PushEnabledChanged(val value: Boolean)
    data object SaveProfile
    data object SaveNotifications
    data object Logout
}
```

Ví dụ trong UI:

```kotlin
onNameChange = { value ->
    viewModel.onEvent(ProfileEvent.NameChanged(value))
}
```

Nghĩa là khi người dùng nhập tên, UI không tự xử lý logic mà gửi event về ViewModel.

## 7. ProfileEffect là gì?

`ProfileEffect` dùng cho hành động xảy ra một lần:

```kotlin
sealed interface ProfileEffect {
    data class ShowSnackbar(val message: String)
    data object NavigateLogin
}
```

Khác với `UiState`, effect không phải dữ liệu vẽ màn hình lâu dài.

Ví dụ:

- Show snackbar "Profile saved."
- Điều hướng về Login sau logout.

Trong `AppNavGraph.kt`, effect được collect:

```kotlin
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            ProfileEffect.NavigateLogin -> navController.navigate(Routes.Login) { ... }
            is ProfileEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }
}
```

## 8. Luồng tải Profile lần đầu

### Bước 1: ViewModel gọi `loadProfileAndSettings()`

Trong `ProfileViewModel.kt`:

```kotlin
private fun loadProfileAndSettings() {
    viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = !it.hasProfile,
                isRefreshing = false,
                errorMessage = null
            )
        }

        val profileResult = getProfileUseCase()
        val settingsResult = getNotificationSettingsUseCase()
        ...
    }
}
```

Ý nghĩa:

- Bật loading.
- Gọi 2 API:
  - Get profile.
  - Get notification settings.

### Bước 2: Gọi GetProfileUseCase

File:

```text
domain/usecase/profile/ProfileUseCases.kt
```

```kotlin
class GetProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<User> {
        return authRepository.getMe()
    }
}
```

UseCase này chỉ gọi `authRepository.getMe()`.

### Bước 3: Repository gọi Retrofit API

File:

```text
data/repository/AuthRepository.kt
```

```kotlin
override suspend fun getMe(): AppResult<User> {
    return safeApiCall(moshi) {
        authApi.getMe()
    }.map { it.toDomain() }
}
```

Ý nghĩa:

- `authApi.getMe()` gọi backend.
- `safeApiCall` bắt lỗi network/API và trả `AppResult`.
- DTO `UserDto` được map sang domain `User`.

### Bước 4: Retrofit endpoint

File:

```text
data/remote/api/AuthApi.kt
```

```kotlin
@GET("users/me")
suspend fun getMe(): UserDto
```

Vì Retrofit base URL là `/api/v1/`, nên endpoint thật là:

```text
GET /api/v1/users/me
```

### Bước 5: Backend xử lý `/users/me`

File:

```text
backend/app/api/v1/users.py
```

```python
@router.get("/me", response_model=UserResponse)
def get_me(current_user: Annotated[User, Depends(get_current_user)]) -> User:
    return current_user
```

Ý nghĩa:

- Backend lấy user hiện tại từ Bearer token.
- Nếu token hợp lệ, trả user.
- Nếu token không hợp lệ, trả 401.

### Bước 6: Load notification settings

ViewModel cũng gọi:

```kotlin
val settingsResult = getNotificationSettingsUseCase()
```

UseCase:

```kotlin
class GetNotificationSettingsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): AppResult<NotificationSettings> {
        return notificationRepository.getPreferences()
    }
}
```

Repository:

```kotlin
override suspend fun getPreferences(): AppResult<NotificationSettings> {
    return safeApiCall(moshi) {
        notificationApi.getPreferences()
    }.map { it.toDomain() }
}
```

Retrofit:

```kotlin
@GET("notifications/preferences")
suspend fun getPreferences(): NotificationPreferencesResponseDto
```

Endpoint thật:

```text
GET /api/v1/notifications/preferences
```

Backend:

```python
@router.get("/preferences", response_model=NotificationPreferenceResponse)
def get_notification_preferences(...):
    return notification_service.get_preferences(current_user)
```

Trong `NotificationService.get_preferences()`:

```python
preference = self.db.get(NotificationPreference, user.id)
if preference is not None:
    return preference

preference = NotificationPreference(
    user_id=user.id,
    daily_time="20:00",
    timezone="Asia/Ho_Chi_Minh",
    email_enabled=False,
    push_enabled=True,
)
```

Nếu user chưa có record notification preferences thì backend tự tạo default.

### Bước 7: ViewModel map dữ liệu vào UiState

Khi cả profile và settings đều success:

```kotlin
_uiState.value = user.toUiState(settings)
reminderScheduler.schedule(settings)
```

Hàm `toUiState()`:

```kotlin
private fun User.toUiState(settings: NotificationSettings): ProfileUiState {
    return ProfileUiState(
        isLoading = false,
        userId = id,
        email = email,
        name = name.orEmpty(),
        goal = goal ?: "General English",
        level = level ?: "A1 Beginner",
        dailyNewWordsInput = dailyNewWords.toString(),
        dailyTime = settings.dailyTime,
        timezone = settings.timezone.ifBlank { "Asia/Ho_Chi_Minh" },
        emailEnabled = settings.emailEnabled,
        pushEnabled = settings.pushEnabled
    )
}
```

Sau đó Compose tự render lại màn hình.

## 9. UI được vẽ như thế nào?

File:

```text
ProfileSettingsScreen.kt
```

Hàm chính:

```kotlin
fun ProfileSettingsScreen(...)
```

Nếu đang loading và chưa có profile:

```kotlin
LoadingStateView(message = "Loading profile...")
```

Nếu lỗi và chưa có profile:

```kotlin
ErrorStateView(
    title = "Unable to load profile",
    message = uiState.errorMessage,
    onRetry = onRetry
)
```

Nếu có dữ liệu:

```kotlin
ProfileSettingsContent(...)
```

Trong `ProfileSettingsContent`, các phần chính được vẽ theo thứ tự:

```kotlin
ProfileTopBar(...)
ProfileHero(uiState)
ProfileFormCard(...)
NotificationSettingsCard(...)
StreakCard()
AccountCard(...)
```

### ProfileTopBar

Hiển thị:

- Logo tròn chữ `M`.
- Tên app `MinLish`.
- Subtitle `Profile & Settings`.
- Nút refresh.

Nút refresh gọi:

```kotlin
onRefresh()
```

Từ `AppNavGraph`, callback này được nối với:

```kotlin
viewModel.onEvent(ProfileEvent.Refresh)
```

### ProfileHero

Hiển thị:

- Avatar initials từ tên user.
- Tên user.
- Email.
- Goal chip.
- Level chip.

Hàm lấy initials:

```kotlin
uiState.name.initials()
```

### ProfileFormCard

Hiển thị form Account:

- Full name text field.
- Email text.
- Learning goal dropdown.
- Current level dropdown.
- Daily new words text field.
- Save profile button.

Các callback:

```kotlin
onNameChange -> ProfileEvent.NameChanged
onGoalChange -> ProfileEvent.GoalChanged
onLevelChange -> ProfileEvent.LevelChanged
onDailyNewWordsChange -> ProfileEvent.DailyNewWordsChanged
onSaveProfile -> ProfileEvent.SaveProfile
```

### NotificationSettingsCard

Hiển thị:

- Switch `Due word notifications`.
- Switch `Email reminder`.
- Text field `Reminder time`.
- Text `Timezone`.
- Button `Save reminders`.

Các callback:

```kotlin
onPushEnabledChange -> ProfileEvent.PushEnabledChanged
onEmailEnabledChange -> ProfileEvent.EmailEnabledChanged
onDailyTimeChange -> ProfileEvent.DailyTimeChanged
onSaveNotifications -> ProfileEvent.SaveNotifications
```

### StreakCard

Hiện tại là UI nhắc người dùng duy trì thói quen học. Nó không gọi API.

### AccountCard

Hiển thị nút logout:

```kotlin
MinLishOutlinedButton(
    text = if (isLoggingOut) "Logging out..." else "Log out",
    onClick = onLogout
)
```

## 10. Luồng sửa và lưu Profile

### Bước 1: Người dùng nhập tên

UI:

```kotlin
MinLishTextField(
    value = uiState.name,
    onValueChange = onNameChange
)
```

Callback sang ViewModel:

```kotlin
ProfileEvent.NameChanged(value)
```

ViewModel:

```kotlin
private fun updateName(value: String) {
    _uiState.update {
        it.copy(name = value, nameError = null)
    }
}
```

Lúc này chỉ update local state, chưa gọi backend.

### Bước 2: Người dùng bấm Save profile

UI gọi:

```kotlin
onSaveProfile()
```

AppNavGraph chuyển thành:

```kotlin
viewModel.onEvent(ProfileEvent.SaveProfile)
```

ViewModel gọi:

```kotlin
private fun saveProfile()
```

### Bước 3: Validate dữ liệu

```kotlin
val name = currentState.name.trim()
val dailyNewWords = currentState.dailyNewWordsInput.toIntOrNull()
val validationError = validateProfile(name, dailyNewWords)
```

Hàm validate:

```kotlin
private fun validateProfile(name: String, dailyNewWords: Int?): String? {
    if (name.isBlank()) return "Name is required."
    if (dailyNewWords == null || dailyNewWords !in 1..100) {
        return "Daily new words must be between 1 and 100."
    }
    return null
}
```

Điều kiện:

- Name không được rỗng.
- Daily new words phải là số từ 1 đến 100.

Nếu lỗi, ViewModel set `nameError` hoặc `dailyNewWordsError`, UI sẽ hiện lỗi dưới field.

### Bước 4: Gọi UpdateProfileUseCase

```kotlin
val result = updateProfileUseCase(
    name = name,
    goal = currentState.goal.takeUnless { it.isBlank() },
    level = currentState.level.takeUnless { it.isBlank() },
    dailyNewWords = dailyNewWords ?: 10
)
```

UseCase:

```kotlin
class UpdateProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(...): AppResult<User> {
        return authRepository.updateMe(...)
    }
}
```

### Bước 5: Repository gọi Retrofit

```kotlin
override suspend fun updateMe(...): AppResult<User> {
    return safeApiCall(moshi) {
        authApi.updateMe(
            UpdateUserRequestDto(
                name = name,
                goal = goal,
                level = level,
                dailyNewWords = dailyNewWords
            )
        )
    }.map { it.toDomain() }
}
```

DTO gửi lên backend:

```kotlin
data class UpdateUserRequestDto(
    val name: String? = null,
    val goal: String? = null,
    val level: String? = null,
    val dailyNewWords: Int? = null
)
```

JSON tương ứng:

```json
{
  "name": "Nguyen Van A",
  "goal": "General English",
  "level": "A1 Beginner",
  "daily_new_words": 10
}
```

### Bước 6: Retrofit endpoint

```kotlin
@PATCH("users/me")
suspend fun updateMe(@Body request: UpdateUserRequestDto): UserDto
```

Endpoint thật:

```text
PATCH /api/v1/users/me
```

### Bước 7: Backend update user

File:

```text
backend/app/api/v1/users.py
```

```python
@router.patch("/me", response_model=UserResponse)
def update_me(
    request: UpdateUserRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    db: Annotated[Session, Depends(get_db)],
) -> User:
    return AuthService(db).update_user(current_user, request)
```

Backend schema:

```python
class UpdateUserRequest(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=120)
    goal: str | None = Field(default=None, min_length=1, max_length=120)
    level: str | None = Field(default=None, min_length=1, max_length=60)
    daily_new_words: int | None = Field(default=None, ge=1, le=100)
```

Service:

```python
def update_user(self, user: User, request: UpdateUserRequest) -> User:
    update_data = request.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(user, field, value)
    self.db.commit()
    self.db.refresh(user)
    return user
```

Nghĩa là backend chỉ update field nào được gửi lên.

### Bước 8: ViewModel nhận kết quả

Nếu success:

```kotlin
_uiState.update {
    it.copy(
        isSavingProfile = false,
        userId = result.data.id,
        email = result.data.email,
        name = result.data.name.orEmpty(),
        goal = result.data.goal ?: "General English",
        level = result.data.level ?: "A1 Beginner",
        dailyNewWordsInput = result.data.dailyNewWords.toString(),
        nameError = null,
        dailyNewWordsError = null
    )
}
_effects.emit(ProfileEffect.ShowSnackbar("Profile saved."))
```

Nếu fail:

```kotlin
_uiState.update {
    it.copy(isSavingProfile = false, errorMessage = result.error.message)
}
_effects.emit(ProfileEffect.ShowSnackbar(result.error.message))
```

## 11. Luồng lưu Notification Settings

### Bước 1: Người dùng bật/tắt Due word notifications

UI:

```kotlin
ToggleSettingRow(
    checked = uiState.pushEnabled,
    onCheckedChange = onPushEnabledChange
)
```

ViewModel:

```kotlin
ProfileEvent.PushEnabledChanged(value)
```

```kotlin
_uiState.update { it.copy(pushEnabled = event.value) }
```

Lưu ý:

- Khi bật/tắt switch, app chỉ đổi state local.
- Chưa lưu backend.
- Chưa schedule thật cho đến khi bấm `Save reminders`.

### Bước 2: Người dùng bật/tắt Email reminder

Tương tự:

```kotlin
ProfileEvent.EmailEnabledChanged(value)
```

```kotlin
_uiState.update { it.copy(emailEnabled = event.value) }
```

Ý nghĩa:

- `emailEnabled = true`: backend scheduler có thể gửi mail nhắc học nếu SMTP được cấu hình.
- `emailEnabled = false`: không gửi email reminder.

### Bước 3: Người dùng nhập giờ nhắc học

ViewModel:

```kotlin
private fun updateDailyTime(value: String) {
    _uiState.update {
        it.copy(
            dailyTime = value.take(5),
            dailyTimeError = null
        )
    }
}
```

`take(5)` giữ tối đa 5 ký tự, ví dụ `20:00`.

### Bước 4: Người dùng bấm Save reminders

ViewModel gọi:

```kotlin
private fun saveNotifications()
```

Validate giờ:

```kotlin
private fun validateDailyTime(value: String): String? {
    if (!TIME_REGEX.matches(value)) return "Reminder time must use HH:mm format."
    val hour = value.substringBefore(":").toInt()
    val minute = value.substringAfter(":").toInt()
    return if (hour in 0..23 && minute in 0..59) null else "Invalid reminder time."
}
```

Regex:

```kotlin
private val TIME_REGEX = Regex("""^\d{2}:\d{2}$""")
```

Yêu cầu:

- Định dạng phải là `HH:mm`.
- Giờ từ `00` đến `23`.
- Phút từ `00` đến `59`.

### Bước 5: Gọi UpdateNotificationSettingsUseCase

```kotlin
val result = updateNotificationSettingsUseCase(
    dailyTime = currentState.dailyTime,
    timezone = currentState.timezone.ifBlank { "Asia/Ho_Chi_Minh" },
    emailEnabled = currentState.emailEnabled,
    pushEnabled = currentState.pushEnabled
)
```

UseCase:

```kotlin
class UpdateNotificationSettingsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(...): AppResult<NotificationSettings> {
        return notificationRepository.updatePreferences(...)
    }
}
```

### Bước 6: Repository gọi Retrofit

```kotlin
override suspend fun updatePreferences(...): AppResult<NotificationSettings> {
    return safeApiCall(moshi) {
        notificationApi.updatePreferences(
            UpdateNotificationPreferencesRequestDto(
                dailyTime = dailyTime,
                timezone = timezone,
                emailEnabled = emailEnabled,
                pushEnabled = pushEnabled
            )
        )
    }.map { it.toDomain() }
}
```

DTO:

```kotlin
data class UpdateNotificationPreferencesRequestDto(
    val dailyTime: String? = null,
    val timezone: String? = null,
    val emailEnabled: Boolean? = null,
    val pushEnabled: Boolean? = null
)
```

JSON:

```json
{
  "daily_time": "20:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "email_enabled": false,
  "push_enabled": true
}
```

### Bước 7: Backend lưu notification preferences

Endpoint:

```text
PATCH /api/v1/notifications/preferences
```

File:

```text
backend/app/api/v1/notifications.py
```

```python
@router.patch("/preferences", response_model=NotificationPreferenceResponse)
def update_notification_preferences(...):
    return notification_service.update_preferences(current_user, request)
```

Service:

```python
def update_preferences(self, user: User, request: UpdateNotificationPreferenceRequest):
    preference = self.get_preferences(user)
    update_data = request.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(preference, field, value)

    self.db.commit()
    self.db.refresh(preference)
    return preference
```

Backend model:

```python
class NotificationPreference(Base):
    __tablename__ = "notification_preferences"

    user_id = ForeignKey("users.id", primary_key=True)
    daily_time = String(5), default="20:00"
    timezone = String(120), default="Asia/Ho_Chi_Minh"
    email_enabled = Boolean, default=False
    push_enabled = Boolean, default=True
```

### Bước 8: Android schedule local reminder

Nếu backend update success:

```kotlin
reminderScheduler.schedule(result.data)
_effects.emit(ProfileEffect.ShowSnackbar("Reminder settings saved."))
```

`reminderScheduler` là `WorkManagerReminderScheduler`.

Trong `ReminderScheduler.kt`:

```kotlin
override fun schedule(settings: NotificationSettings) {
    if (!settings.pushEnabled) {
        cancel()
        return
    }

    val request = DailyReminderWorker.buildRequest(
        dailyTime = settings.dailyTime,
        timezone = settings.timezone,
        forceTomorrow = false
    )

    workManager.enqueueUniqueWork(
        DailyReminderWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request
    )
}
```

Ý nghĩa:

- Nếu `pushEnabled = false`: huỷ notification local.
- Nếu `pushEnabled = true`: tạo OneTimeWorkRequest chạy vào giờ nhắc học tiếp theo.
- `ExistingWorkPolicy.REPLACE`: nếu đã có lịch cũ thì thay bằng lịch mới.

### Bước 9: DailyReminderWorker chạy

File:

```text
core/notification/DailyReminderWorker.kt
```

Khi đến giờ:

```kotlin
override suspend fun doWork(): Result {
    val dailyTime = inputData.getString(KEY_DAILY_TIME) ?: "20:00"
    val timezone = inputData.getString(KEY_TIMEZONE) ?: "Asia/Ho_Chi_Minh"

    if (canPostNotifications()) {
        showNotification()
    }

    scheduleNextReminder(dailyTime, timezone)
    return Result.success()
}
```

Ý nghĩa:

- Lấy giờ và timezone từ WorkManager input data.
- Nếu app có quyền notification thì hiển thị thông báo.
- Sau khi chạy xong, tự schedule lần tiếp theo cho ngày mai.

Thông báo hiển thị:

```kotlin
.setContentTitle("MinLish reminder")
.setContentText("A few words today keeps your vocabulary moving.")
```

## 12. Email reminder khác gì Due word notifications?

Trong UI có 2 switch:

### Due word notifications

Field:

```kotlin
pushEnabled
```

Ý nghĩa:

- Đây là notification local trên thiết bị Android.
- Android tự schedule bằng WorkManager.
- Không cần backend scheduler để hiện local notification.
- Cần quyền notification trên Android 13+.

### Email reminder

Field:

```kotlin
emailEnabled
```

Ý nghĩa:

- Setting này lưu trên backend.
- Backend scheduler sẽ đọc các user có `email_enabled = true`.
- Nếu đến đúng `daily_time`, backend gửi email qua SMTP.
- Cần cấu hình SMTP trong `.env`.

Nói ngắn gọn:

```text
pushEnabled -> Android local notification
emailEnabled -> Backend gửi email
```

## 13. Luồng Refresh

Khi người dùng bấm nút refresh ở top bar:

```kotlin
ProfileEvent.Refresh
```

ViewModel:

```kotlin
private fun refresh() {
    viewModelScope.launch {
        _uiState.update {
            it.copy(isRefreshing = true, errorMessage = null)
        }
        val profileResult = getProfileUseCase()
        val settingsResult = getNotificationSettingsUseCase()
        ...
    }
}
```

Khác `loadProfileAndSettings()`:

- `loadProfileAndSettings()` dùng khi mở màn hoặc retry.
- `refresh()` dùng khi đã có UI rồi và muốn tải lại dữ liệu.

Nếu success:

```kotlin
_uiState.value = profileResult.data.toUiState(settings)
reminderScheduler.schedule(settings)
_effects.emit(ProfileEffect.ShowSnackbar("Profile refreshed."))
```

Nếu fail:

```kotlin
showRefreshError(message)
```

## 14. Luồng Logout

### Bước 1: Người dùng bấm Log out

UI:

```kotlin
AccountCard(
    isLoggingOut = uiState.isLoggingOut,
    onLogout = onLogout
)
```

AppNavGraph:

```kotlin
onLogout = { viewModel.onEvent(ProfileEvent.Logout) }
```

ViewModel:

```kotlin
private fun logout() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoggingOut = true) }
        val result = logoutUseCase()
        reminderScheduler.cancel()
        if (result is AppResult.Failure) {
            _effects.emit(ProfileEffect.ShowSnackbar(result.error.message))
        }
        _uiState.update { it.copy(isLoggingOut = false) }
        _effects.emit(ProfileEffect.NavigateLogin)
    }
}
```

Ý nghĩa:

- Bật trạng thái logging out.
- Gọi logout use case.
- Huỷ local reminder.
- Dù backend logout fail, app vẫn điều hướng về Login sau khi clear token local.

### Bước 2: LogoutUseCase xử lý token

```kotlin
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            tokenStorage.clearTokens()
            return AppResult.Success(Unit)
        }

        val result = authRepository.logout(refreshToken)
        tokenStorage.clearTokens()
        return result
    }
}
```

Ý nghĩa:

- Nếu không có refresh token: chỉ clear token local.
- Nếu có refresh token: gọi backend logout rồi clear token local.

### Bước 3: Repository gọi backend logout

```kotlin
override suspend fun logout(refreshToken: String): AppResult<Unit> {
    val result = safeApiCall(moshi) {
        authApi.logout(LogoutRequestDto(refreshToken = refreshToken))
    }
    if (result is AppResult.Success) {
        tokenStorage.clearTokens()
    }
    return result
}
```

Retrofit:

```kotlin
@POST("auth/logout")
suspend fun logout(@Body request: LogoutRequestDto)
```

Endpoint thật:

```text
POST /api/v1/auth/logout
```

Backend:

```python
@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(request: LogoutRequest, auth_service: AuthService):
    auth_service.logout(request.refresh_token)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
```

### Bước 4: Điều hướng Login

ViewModel emit:

```kotlin
ProfileEffect.NavigateLogin
```

AppNavGraph xử lý:

```kotlin
navController.navigate(Routes.Login) {
    popUpTo(Routes.Home) { inclusive = true }
    launchSingleTop = true
}
```

Ý nghĩa:

- Chuyển về Login.
- Xoá Home khỏi back stack để user không bấm back quay lại app chính.

## 15. AppContainer nối mọi thứ như thế nào?

File:

```text
core/AppContainer.kt
```

AppContainer tạo các dependency:

```kotlin
val tokenStorage: TokenStorage = EncryptedTokenStorage(appContext)
val moshi: Moshi = RetrofitFactory.createMoshi()
val authenticatedRetrofit = RetrofitFactory.createAuthenticatedRetrofit(...)
```

Tạo API:

```kotlin
private val authenticatedAuthApi: AuthApi = authenticatedRetrofit.create(AuthApi::class.java)
private val notificationApi: NotificationApi = authenticatedRetrofit.create(NotificationApi::class.java)
```

Tạo repository:

```kotlin
val authRepository = DefaultAuthRepository(...)
val notificationRepository = DefaultNotificationRepository(...)
```

Tạo use case:

```kotlin
val getProfileUseCase = GetProfileUseCase(authRepository)
val updateProfileUseCase = UpdateProfileUseCase(authRepository)
val logoutUseCase = LogoutUseCase(authRepository, tokenStorage)
val getNotificationSettingsUseCase = GetNotificationSettingsUseCase(notificationRepository)
val updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(notificationRepository)
```

Tạo reminder scheduler:

```kotlin
val reminderScheduler: ReminderScheduler = WorkManagerReminderScheduler(appContext)
```

Đây là manual dependency injection, không dùng Hilt.

## 16. API contract liên quan Profile

### GET /api/v1/users/me

Request:

```text
Authorization: Bearer access_token
```

Response:

```json
{
  "id": 1,
  "email": "learner@example.com",
  "name": "Min Learner",
  "goal": "General English",
  "level": "A1 Beginner",
  "daily_new_words": 10
}
```

### PATCH /api/v1/users/me

Request:

```json
{
  "name": "Min Learner",
  "goal": "General English",
  "level": "A1 Beginner",
  "daily_new_words": 10
}
```

Response giống `GET /users/me`.

### GET /api/v1/notifications/preferences

Response:

```json
{
  "daily_time": "20:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "email_enabled": false,
  "push_enabled": true
}
```

### PATCH /api/v1/notifications/preferences

Request:

```json
{
  "daily_time": "20:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "email_enabled": true,
  "push_enabled": true
}
```

Response giống `GET /notifications/preferences`.

### POST /api/v1/auth/logout

Request:

```json
{
  "refresh_token": "..."
}
```

Response:

```text
204 No Content
```

## 17. Database liên quan Profile

### Bảng users

Các field Profile dùng:

```text
id
email
name
goal
level
daily_new_words
```

Ý nghĩa:

- `name`: tên hiển thị.
- `goal`: mục tiêu học, ví dụ `General English`.
- `level`: trình độ, ví dụ `A1 Beginner`.
- `daily_new_words`: số từ mới mỗi ngày, ảnh hưởng daily plan ở Home/Learn.

### Bảng notification_preferences

Model:

```python
class NotificationPreference(Base):
    user_id
    daily_time
    timezone
    email_enabled
    push_enabled
    created_at
    updated_at
```

Ý nghĩa:

- `user_id`: mỗi user có một preference.
- `daily_time`: giờ nhắc học.
- `timezone`: múi giờ.
- `email_enabled`: backend có gửi email reminder hay không.
- `push_enabled`: Android có schedule local notification hay không.

## 18. Cách trình bày ngắn gọn khi bảo vệ đồ án

Có thể nói như sau:

> Màn Profile dùng mô hình MVVM. Giao diện Compose chỉ hiển thị `ProfileUiState` và gửi các event như SaveProfile, SaveNotifications, Logout về ViewModel. ViewModel validate dữ liệu, gọi các UseCase. UseCase gọi Repository. Repository gọi Retrofit API đến FastAPI backend. Backend lưu dữ liệu user vào bảng `users` và cài đặt nhắc học vào bảng `notification_preferences`. Với thông báo local, sau khi lưu settings thành công, ViewModel gọi `ReminderScheduler`, scheduler dùng WorkManager để tạo một công việc chạy vào giờ nhắc học. Khi worker chạy, nó hiển thị Android notification và tự schedule lần tiếp theo.

## 19. Nếu muốn tự code lại màn Profile từ đầu thì đi theo thứ tự nào?

1. Tạo domain model:
   - `User`
   - `NotificationSettings`

2. Tạo DTO:
   - `UserDto`
   - `UpdateUserRequestDto`
   - `NotificationPreferencesResponseDto`
   - `UpdateNotificationPreferencesRequestDto`

3. Tạo Retrofit API:
   - `GET users/me`
   - `PATCH users/me`
   - `GET notifications/preferences`
   - `PATCH notifications/preferences`
   - `POST auth/logout`

4. Tạo Repository:
   - `AuthRepository.getMe()`
   - `AuthRepository.updateMe()`
   - `AuthRepository.logout()`
   - `NotificationRepository.getPreferences()`
   - `NotificationRepository.updatePreferences()`

5. Tạo UseCase:
   - `GetProfileUseCase`
   - `UpdateProfileUseCase`
   - `LogoutUseCase`
   - `GetNotificationSettingsUseCase`
   - `UpdateNotificationSettingsUseCase`

6. Tạo ViewModel:
   - `ProfileUiState`
   - `ProfileEvent`
   - `ProfileEffect`
   - `loadProfileAndSettings()`
   - `saveProfile()`
   - `saveNotifications()`
   - `logout()`

7. Tạo UI Compose:
   - Top bar.
   - Profile hero.
   - Account form.
   - Reminder form.
   - Logout button.

8. Nối Navigation:
   - Tạo ViewModel.
   - Collect state.
   - Collect effect.
   - Truyền callback vào screen.

9. Backend:
   - Tạo endpoint `users/me`.
   - Tạo endpoint `notifications/preferences`.
   - Lưu vào PostgreSQL.

## 20. Các lỗi thường gặp khi test Profile

### 1. Không tải được profile

Nguyên nhân thường gặp:

- Backend chưa chạy.
- Token hết hạn.
- User chưa login.
- Android base URL sai.

Màn hình sẽ hiện:

```text
Unable to load profile
```

### 2. Save profile lỗi

Nguyên nhân:

- Name rỗng.
- Daily new words không phải số.
- Daily new words ngoài khoảng 1 đến 100.
- Backend trả lỗi validation.

### 3. Save reminders lỗi

Nguyên nhân:

- Reminder time không đúng `HH:mm`.
- Ví dụ sai: `8:0`, `25:00`, `20:99`.
- Ví dụ đúng: `08:00`, `20:00`.

### 4. Không thấy local notification

Nguyên nhân:

- `pushEnabled = false`.
- Chưa bấm `Save reminders`.
- Android 13+ chưa cấp quyền notification.
- Giờ nhắc đã qua nên WorkManager schedule sang ngày hôm sau.
- Emulator có thể delay WorkManager, không chính xác tuyệt đối như AlarmManager.

### 5. Không nhận email reminder

Nguyên nhân:

- `emailEnabled = false`.
- SMTP chưa cấu hình đúng trong backend `.env`.
- Backend scheduler chưa chạy.
- Email rơi vào spam.

## 21. Tóm tắt flow từng chức năng

### Load Profile

```text
Open Profile tab
-> AppNavGraph tạo ProfileViewModel
-> init gọi loadProfileAndSettings()
-> getProfileUseCase()
-> AuthRepository.getMe()
-> GET /users/me
-> backend trả User
-> getNotificationSettingsUseCase()
-> NotificationRepository.getPreferences()
-> GET /notifications/preferences
-> backend trả NotificationSettings
-> ViewModel update ProfileUiState
-> UI render
```

### Save Profile

```text
User sửa name/goal/level/daily words
-> UI gửi ProfileEvent
-> ViewModel update local state
-> User bấm Save profile
-> ViewModel validate
-> UpdateProfileUseCase
-> AuthRepository.updateMe
-> PATCH /users/me
-> backend update users table
-> ViewModel update UiState
-> Show snackbar
```

### Save Reminders

```text
User sửa push/email/time
-> UI gửi ProfileEvent
-> ViewModel update local state
-> User bấm Save reminders
-> ViewModel validate HH:mm
-> UpdateNotificationSettingsUseCase
-> NotificationRepository.updatePreferences
-> PATCH /notifications/preferences
-> backend update notification_preferences table
-> ViewModel update UiState
-> reminderScheduler.schedule(settings)
-> WorkManager schedule local notification
-> Show snackbar
```

### Logout

```text
User bấm Log out
-> ProfileEvent.Logout
-> ViewModel logout()
-> LogoutUseCase
-> AuthRepository.logout(refreshToken)
-> POST /auth/logout
-> tokenStorage.clearTokens()
-> reminderScheduler.cancel()
-> ProfileEffect.NavigateLogin
-> AppNavGraph navigate Login
```

## 22. Ghi chú về mức độ đồ án

Màn Profile hiện có cấu trúc khá đầy đủ nhưng vẫn có thể giải thích đơn giản:

- UI: Compose.
- State: `ProfileUiState`.
- Logic màn hình: `ProfileViewModel`.
- Gọi backend: Repository + Retrofit.
- Backend: FastAPI endpoint + SQLAlchemy model.
- Local notification: WorkManager.

Khi báo cáo, không cần nói quá sâu về toàn bộ core/network. Chỉ cần nhấn mạnh:

```text
Composable không gọi API trực tiếp.
ViewModel nhận event và gọi UseCase.
Repository gọi Retrofit.
Backend lưu dữ liệu vào PostgreSQL.
```

