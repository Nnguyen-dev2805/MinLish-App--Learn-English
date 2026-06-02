package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.usecase.auth.ForgotPasswordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val codeSent: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val otpError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val apiError: String? = null
)

sealed interface ForgotPasswordEffect {
    data object NavigateLogin : ForgotPasswordEffect
    data class ShowSnackbar(val message: String) : ForgotPasswordEffect
}

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ForgotPasswordEffect>()
    val effects: SharedFlow<ForgotPasswordEffect> = _effects.asSharedFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, apiError = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value.filter(Char::isDigit).take(6), otpError = null, apiError = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null, apiError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, apiError = null) }
    }

    fun sendCode() {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = forgotPasswordUseCase(email)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, codeSent = true) }
                    _effects.emit(ForgotPasswordEffect.ShowSnackbar(result.data))
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, apiError = result.error.message) }
                    _effects.emit(ForgotPasswordEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        var hasError = false
        var otpError: String? = null
        var passwordError: String? = null
        var confirmError: String? = null

        if (state.otp.length < 6) {
            otpError = "Enter the 6-digit code"
            hasError = true
        }
        if (state.newPassword.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }
        if (state.confirmPassword != state.newPassword) {
            confirmError = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    otpError = otpError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = resetPasswordUseCase(state.email.trim(), state.otp, state.newPassword)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ForgotPasswordEffect.ShowSnackbar(result.data))
                    _effects.emit(ForgotPasswordEffect.NavigateLogin)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, apiError = result.error.message) }
                    _effects.emit(ForgotPasswordEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun backToLogin() {
        viewModelScope.launch {
            _effects.emit(ForgotPasswordEffect.NavigateLogin)
        }
    }
}
