package com.example.minlishapp_learnenglish.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.notification.ReminderScheduler
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import com.example.minlishapp_learnenglish.domain.model.User
import com.example.minlishapp_learnenglish.domain.usecase.notification.GetNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.notification.UpdateNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.GetProfileUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.LogoutUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PROFILE_DEFAULT_GOAL = "General English"
private const val PROFILE_DEFAULT_LEVEL = "A1 Beginner"
private const val PROFILE_DEFAULT_DAILY_NEW_WORDS = 10
private const val PROFILE_DEFAULT_DAILY_TIME = "20:00"
private const val PROFILE_DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh"

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isSavingNotifications: Boolean = false,
    val isLoggingOut: Boolean = false,
    val userId: Long? = null,
    val email: String = "",
    val name: String = "",
    val goal: String = PROFILE_DEFAULT_GOAL,
    val level: String = PROFILE_DEFAULT_LEVEL,
    val dailyNewWordsInput: String = PROFILE_DEFAULT_DAILY_NEW_WORDS.toString(),
    val dailyTime: String = PROFILE_DEFAULT_DAILY_TIME,
    val timezone: String = PROFILE_DEFAULT_TIMEZONE,
    val emailEnabled: Boolean = false,
    val pushEnabled: Boolean = true,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val dailyNewWordsError: String? = null,
    val dailyTimeError: String? = null
) {
    val hasProfile: Boolean
        get() = userId != null
}

sealed interface ProfileEvent {
    data object Retry : ProfileEvent
    data object Refresh : ProfileEvent
    data class NameChanged(val value: String) : ProfileEvent
    data class GoalChanged(val value: String) : ProfileEvent
    data class LevelChanged(val value: String) : ProfileEvent
    data class DailyNewWordsChanged(val value: String) : ProfileEvent
    data class DailyTimeChanged(val value: String) : ProfileEvent
    data class EmailEnabledChanged(val value: Boolean) : ProfileEvent
    data class PushEnabledChanged(val value: Boolean) : ProfileEvent
    data object SaveProfile : ProfileEvent
    data object SaveNotifications : ProfileEvent
    data object Logout : ProfileEvent
}

sealed interface ProfileEffect {
    data class ShowSnackbar(val message: String) : ProfileEffect
    data object NavigateLogin : ProfileEffect
}

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        loadProfileAndSettings()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Retry -> loadProfileAndSettings()
            ProfileEvent.Refresh -> refresh()
            is ProfileEvent.NameChanged -> updateName(event.value)
            is ProfileEvent.GoalChanged -> _uiState.update { it.copy(goal = event.value) }
            is ProfileEvent.LevelChanged -> _uiState.update { it.copy(level = event.value) }
            is ProfileEvent.DailyNewWordsChanged -> updateDailyNewWords(event.value)
            is ProfileEvent.DailyTimeChanged -> updateDailyTime(event.value)
            is ProfileEvent.EmailEnabledChanged -> {
                _uiState.update { it.copy(emailEnabled = event.value) }
            }
            is ProfileEvent.PushEnabledChanged -> {
                _uiState.update { it.copy(pushEnabled = event.value) }
            }
            ProfileEvent.SaveProfile -> saveProfile()
            ProfileEvent.SaveNotifications -> saveNotifications()
            ProfileEvent.Logout -> logout()
        }
    }

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
            when {
                profileResult is AppResult.Success && settingsResult is AppResult.Success -> {
                    val user = profileResult.data
                    val settings = settingsResult.data
                    _uiState.value = user.toUiState(settings)
                    reminderScheduler.schedule(settings)
                }
                profileResult is AppResult.Failure -> showLoadError(profileResult.error.message)
                settingsResult is AppResult.Failure -> showLoadError(settingsResult.error.message)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isRefreshing = true, errorMessage = null)
            }
            val profileResult = getProfileUseCase()
            val settingsResult = getNotificationSettingsUseCase()
            when {
                profileResult is AppResult.Success && settingsResult is AppResult.Success -> {
                    val settings = settingsResult.data
                    _uiState.value = profileResult.data.toUiState(settings)
                    reminderScheduler.schedule(settings)
                    _effects.emit(ProfileEffect.ShowSnackbar("Đã làm mới hồ sơ."))
                }
                profileResult is AppResult.Failure -> {
                    showRefreshError(profileResult.error.message)
                }
                settingsResult is AppResult.Failure -> {
                    showRefreshError(settingsResult.error.message)
                }
            }
        }
    }

    private fun saveProfile() {
        val currentState = _uiState.value
        val name = currentState.name.trim()
        val dailyNewWords = currentState.dailyNewWordsInput.toIntOrNull()
        val validationError = validateProfile(name, dailyNewWords)
        if (validationError != null) {
            _uiState.update { state ->
                state.copy(
                    nameError = if (name.isBlank()) "Tên không được để trống." else null,
                    dailyNewWordsError = validationError.takeIf { dailyNewWords == null || dailyNewWords !in 1..100 }
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSavingProfile = true, errorMessage = null)
            }
            when (
                val result = updateProfileUseCase(
                    name = name,
                    goal = currentState.goal.takeUnless { it.isBlank() },
                    level = currentState.level.takeUnless { it.isBlank() },
                    dailyNewWords = dailyNewWords ?: PROFILE_DEFAULT_DAILY_NEW_WORDS
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingProfile = false,
                            userId = result.data.id,
                            email = result.data.email,
                            name = result.data.name.orEmpty(),
                            goal = result.data.goal ?: PROFILE_DEFAULT_GOAL,
                            level = result.data.level ?: PROFILE_DEFAULT_LEVEL,
                            dailyNewWordsInput = result.data.dailyNewWords.toString(),
                            nameError = null,
                            dailyNewWordsError = null
                        )
                    }
                    _effects.emit(ProfileEffect.ShowSnackbar("Đã lưu hồ sơ."))
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isSavingProfile = false, errorMessage = result.error.message)
                    }
                    _effects.emit(ProfileEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun saveNotifications() {
        val currentState = _uiState.value
        val validationError = validateDailyTime(currentState.dailyTime)
        if (validationError != null) {
            _uiState.update { it.copy(dailyTimeError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSavingNotifications = true, errorMessage = null)
            }
            when (
                val result = updateNotificationSettingsUseCase(
                    dailyTime = currentState.dailyTime,
                    timezone = currentState.timezone.ifBlank { PROFILE_DEFAULT_TIMEZONE },
                    emailEnabled = currentState.emailEnabled,
                    pushEnabled = currentState.pushEnabled
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingNotifications = false,
                            dailyTime = result.data.dailyTime,
                            timezone = result.data.timezone,
                            emailEnabled = result.data.emailEnabled,
                            pushEnabled = result.data.pushEnabled,
                            dailyTimeError = null
                        )
                    }
                    reminderScheduler.schedule(result.data)
                    _effects.emit(ProfileEffect.ShowSnackbar("Đã lưu nhắc học."))
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isSavingNotifications = false, errorMessage = result.error.message)
                    }
                    _effects.emit(ProfileEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

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

    private fun updateName(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = null
            )
        }
    }

    private fun updateDailyNewWords(value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(
                dailyNewWordsInput = filtered,
                dailyNewWordsError = null
            )
        }
    }

    private fun updateDailyTime(value: String) {
        _uiState.update {
            it.copy(
                dailyTime = value.take(5),
                dailyTimeError = null
            )
        }
    }

    private fun showLoadError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = message
            )
        }
    }

    private suspend fun showRefreshError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = message
            )
        }
        _effects.emit(ProfileEffect.ShowSnackbar(message))
    }

    private fun validateProfile(name: String, dailyNewWords: Int?): String? {
        if (name.isBlank()) return "Tên không được để trống."
        if (dailyNewWords == null || dailyNewWords !in 1..100) {
            return "Số từ mới mỗi ngày phải từ 1 đến 100."
        }
        return null
    }

    private fun validateDailyTime(value: String): String? {
        if (!TIME_REGEX.matches(value)) return "Giờ nhắc học cần có định dạng HH:mm."
        val hour = value.substringBefore(":").toInt()
        val minute = value.substringAfter(":").toInt()
        return if (hour in 0..23 && minute in 0..59) null else "Giờ nhắc học không hợp lệ."
    }

    private fun User.toUiState(settings: NotificationSettings): ProfileUiState {
        return ProfileUiState(
            isLoading = false,
            userId = id,
            email = email,
            name = name.orEmpty(),
            goal = goal ?: PROFILE_DEFAULT_GOAL,
            level = level ?: PROFILE_DEFAULT_LEVEL,
            dailyNewWordsInput = dailyNewWords.toString(),
            dailyTime = settings.dailyTime,
            timezone = settings.timezone.ifBlank { PROFILE_DEFAULT_TIMEZONE },
            emailEnabled = settings.emailEnabled,
            pushEnabled = settings.pushEnabled
        )
    }

    companion object {
        val goalOptions = listOf("General English", "IELTS Vocabulary", "Academic English", "Business English")
        val levelOptions = listOf("A1 Beginner", "A2 Elementary", "B1 Intermediate", "B2 Upper Intermediate", "C1 Advanced")

        private val TIME_REGEX = Regex("""^\d{2}:\d{2}$""")
    }
}
