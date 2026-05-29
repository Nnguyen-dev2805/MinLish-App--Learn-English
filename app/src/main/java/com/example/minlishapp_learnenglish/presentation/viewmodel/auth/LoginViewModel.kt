package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.usecase.auth.CheckSessionUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.LoginUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.SessionDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isCheckingSession: Boolean = true,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val apiError: String? = null
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val checkSessionUseCase: CheckSessionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    init {
        checkSessionOnStart()
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, apiError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, apiError = null) }
    }

    fun login() {
        val current = _uiState.value
        val emailError = if (current.email.isBlank()) "Email không được để trống." else null
        val passwordError = if (current.password.isBlank()) "Password không được để trống." else null
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = loginUseCase(current.email.trim(), current.password)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(AuthEffect.NavigateHome)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, apiError = result.error.message) }
                    _effects.emit(AuthEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun forgotPassword() {
        viewModelScope.launch {
            _effects.emit(AuthEffect.ShowSnackbar("Chức năng quên mật khẩu sẽ bổ sung sau."))
        }
    }

    fun googleLogin() {
        viewModelScope.launch {
            _effects.emit(AuthEffect.ShowSnackbar("Google login sẽ bổ sung sau khi có OAuth config."))
        }
    }

    private fun checkSessionOnStart() {
        viewModelScope.launch {
            val destination = runCatching { checkSessionUseCase() }
                .getOrDefault(SessionDestination.Login)
            when (destination) {
                SessionDestination.Home -> _effects.emit(AuthEffect.NavigateHome)
                SessionDestination.Login -> Unit
            }
            _uiState.update { it.copy(isCheckingSession = false) }
        }
    }
}
