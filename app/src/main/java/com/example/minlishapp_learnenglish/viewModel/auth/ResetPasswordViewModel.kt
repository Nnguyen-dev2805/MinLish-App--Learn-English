package com.example.minlishapp_learnenglish.viewModel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.otp.OtpManager
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    private val otpManager: OtpManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun resetPassword() {
        val state = _uiState.value
        var hasError = false
        var passwordError: String? = null
        var confirmError: String? = null

        if (state.password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }
        if (state.confirmPassword != state.password) {
            confirmError = "Passwords do not match"
            hasError = true
        }
        if (hasError) {
            _uiState.update {
                it.copy(passwordError = passwordError, confirmPasswordError = confirmError)
            }
            return
        }

        val email = otpManager.getEmail()
        if (email.isNullOrBlank()) {
            viewModelScope.launch { _effects.emit(AuthEffect.NavigateForgotPassword) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (
                val result = authRepository.verifyOtpAndResetPassword(
                    email = email,
                    otp = "",
                    newPassword = state.password
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(AuthEffect.ShowSnackbar("Password reset successfully"))
                    _effects.emit(AuthEffect.NavigateLogin)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(AuthEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _effects.emit(AuthEffect.NavigateVerifyOtp)
        }
    }
}
