package com.example.minlishapp_learnenglish.presentation.viewmodel.profile

import com.example.minlishapp_learnenglish.core.notification.ReminderScheduler
import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.data.repository.NotificationRepository
import com.example.minlishapp_learnenglish.domain.model.AuthSession
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import com.example.minlishapp_learnenglish.domain.model.User
import com.example.minlishapp_learnenglish.domain.usecase.notification.GetNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.notification.UpdateNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.GetProfileUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.LogoutUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.UpdateProfileUseCase
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load success exposes profile and notification settings`() = runTest {
        val scheduler = FakeReminderScheduler()
        val viewModel = createViewModel(reminderScheduler = scheduler)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Min Lish", state.name)
        assertEquals("student@minlish.app", state.email)
        assertEquals("20:00", state.dailyTime)
        assertEquals(1, scheduler.scheduleCalls)
    }

    @Test
    fun `load error exposes retryable error state`() = runTest {
        val error = AppError.Network("Không thể kết nối máy chủ.")
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(getMeResult = AppResult.Failure(error))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(error.message, state.errorMessage)
    }

    @Test
    fun `update profile success updates state and emits snackbar`() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = createViewModel(authRepository = authRepository)
        val effects = mutableListOf<ProfileEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.NameChanged("New Name"))
        viewModel.onEvent(ProfileEvent.DailyNewWordsChanged("15"))
        viewModel.onEvent(ProfileEvent.SaveProfile)
        advanceUntilIdle()

        assertEquals("New Name", viewModel.uiState.value.name)
        assertEquals("15", viewModel.uiState.value.dailyNewWordsInput)
        assertTrue(effects.contains(ProfileEffect.ShowSnackbar("Đã lưu hồ sơ.")))
        assertEquals(1, authRepository.updateCalls)
    }

    @Test
    fun `update notification success schedules reminder and emits snackbar`() = runTest {
        val scheduler = FakeReminderScheduler()
        val notificationRepository = FakeNotificationRepository()
        val viewModel = createViewModel(
            notificationRepository = notificationRepository,
            reminderScheduler = scheduler
        )
        val effects = mutableListOf<ProfileEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.DailyTimeChanged("21:30"))
        viewModel.onEvent(ProfileEvent.PushEnabledChanged(false))
        viewModel.onEvent(ProfileEvent.SaveNotifications)
        advanceUntilIdle()

        assertEquals("21:30", viewModel.uiState.value.dailyTime)
        assertFalse(viewModel.uiState.value.pushEnabled)
        assertTrue(effects.contains(ProfileEffect.ShowSnackbar("Đã lưu nhắc học.")))
        assertEquals(2, scheduler.scheduleCalls)
        assertEquals(1, notificationRepository.updateCalls)
    }

    @Test
    fun `logout clears token and emits navigate login`() = runTest {
        val tokenStorage = FakeTokenStorage(refreshToken = "refresh-token")
        val viewModel = createViewModel(tokenStorage = tokenStorage)
        val effects = mutableListOf<ProfileEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.Logout)
        advanceUntilIdle()

        assertTrue(tokenStorage.clearCalled)
        assertTrue(effects.contains(ProfileEffect.NavigateLogin))
    }
}

private fun createViewModel(
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    notificationRepository: FakeNotificationRepository = FakeNotificationRepository(),
    tokenStorage: FakeTokenStorage = FakeTokenStorage(refreshToken = "refresh-token"),
    reminderScheduler: FakeReminderScheduler = FakeReminderScheduler()
): ProfileViewModel {
    return ProfileViewModel(
        getProfileUseCase = GetProfileUseCase(authRepository),
        updateProfileUseCase = UpdateProfileUseCase(authRepository),
        getNotificationSettingsUseCase = GetNotificationSettingsUseCase(notificationRepository),
        updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(notificationRepository),
        logoutUseCase = LogoutUseCase(authRepository, tokenStorage),
        reminderScheduler = reminderScheduler
    )
}

private class FakeAuthRepository(
    private val getMeResult: AppResult<User> = AppResult.Success(sampleUser),
    private val logoutResult: AppResult<Unit> = AppResult.Success(Unit)
) : AuthRepository {
    var updateCalls = 0
        private set

    override suspend fun login(email: String, password: String): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun refresh(refreshToken: String): AppResult<String> {
        return AppResult.Success("access-token")
    }

    override suspend fun logout(refreshToken: String): AppResult<Unit> {
        return logoutResult
    }

    override suspend fun getMe(): AppResult<User> {
        return getMeResult
    }

    override suspend fun updateMe(
        name: String?,
        goal: String?,
        level: String?,
        dailyNewWords: Int?
    ): AppResult<User> {
        updateCalls += 1
        return AppResult.Success(
            sampleUser.copy(
                name = name ?: sampleUser.name,
                goal = goal ?: sampleUser.goal,
                level = level ?: sampleUser.level,
                dailyNewWords = dailyNewWords ?: sampleUser.dailyNewWords
            )
        )
    }
}

private class FakeNotificationRepository(
    private val preferencesResult: AppResult<NotificationSettings> = AppResult.Success(sampleSettings)
) : NotificationRepository {
    var updateCalls = 0
        private set

    override suspend fun getPreferences(): AppResult<NotificationSettings> {
        return preferencesResult
    }

    override suspend fun updatePreferences(
        dailyTime: String?,
        timezone: String?,
        emailEnabled: Boolean?,
        pushEnabled: Boolean?
    ): AppResult<NotificationSettings> {
        updateCalls += 1
        return AppResult.Success(
            sampleSettings.copy(
                dailyTime = dailyTime ?: sampleSettings.dailyTime,
                timezone = timezone ?: sampleSettings.timezone,
                emailEnabled = emailEnabled ?: sampleSettings.emailEnabled,
                pushEnabled = pushEnabled ?: sampleSettings.pushEnabled
            )
        )
    }
}

private class FakeTokenStorage(
    private var refreshToken: String?
) : TokenStorage {
    var clearCalled = false
        private set

    override fun getAccessToken(): String? = "access-token"

    override fun getRefreshToken(): String? = refreshToken

    override fun saveTokens(accessToken: String, refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun updateAccessToken(accessToken: String) = Unit

    override fun clearTokens() {
        clearCalled = true
        refreshToken = null
    }
}

private class FakeReminderScheduler : ReminderScheduler {
    var scheduleCalls = 0
        private set
    var cancelCalls = 0
        private set

    override fun schedule(settings: NotificationSettings) {
        scheduleCalls += 1
    }

    override fun cancel() {
        cancelCalls += 1
    }
}

private val sampleUser = User(
    id = 1L,
    email = "student@minlish.app",
    name = "Min Lish",
    goal = "General English",
    level = "A1 Beginner",
    dailyNewWords = 10
)

private val sampleSettings = NotificationSettings(
    dailyTime = "20:00",
    timezone = "Asia/Ho_Chi_Minh",
    emailEnabled = false,
    pushEnabled = true
)
