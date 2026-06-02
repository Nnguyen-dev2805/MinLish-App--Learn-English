package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.usecase.auth.ResendVerificationOtpUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.VerifyEmailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerifyEmailUiState(
    val email: String,
    val otp: String = "",
    val isLoading: Boolean = false,
    val otpError: String? = null,
    val apiError: String? = null
)

sealed interface VerifyEmailEffect {
    data object NavigateSetup : VerifyEmailEffect
    data object NavigateLogin : VerifyEmailEffect
    data class ShowSnackbar(val message: String) : VerifyEmailEffect
}

class VerifyEmailViewModel(
    email: String,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val resendVerificationOtpUseCase: ResendVerificationOtpUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyEmailUiState(email = email))
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<VerifyEmailEffect>()
    val effects: SharedFlow<VerifyEmailEffect> = _effects.asSharedFlow()

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value.filter(Char::isDigit).take(6), otpError = null, apiError = null) }
    }

    fun verify() {
        val state = _uiState.value
        if (state.otp.length < 6) {
            _uiState.update { it.copy(otpError = "Enter the 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = verifyEmailUseCase(state.email, state.otp)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(VerifyEmailEffect.ShowSnackbar("Email verified successfully."))
                    _effects.emit(VerifyEmailEffect.NavigateSetup)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, apiError = result.error.message) }
                    _effects.emit(VerifyEmailEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun resend() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = resendVerificationOtpUseCase(_uiState.value.email)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(VerifyEmailEffect.ShowSnackbar(result.data))
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, apiError = result.error.message) }
                    _effects.emit(VerifyEmailEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun backToLogin() {
        viewModelScope.launch {
            _effects.emit(VerifyEmailEffect.NavigateLogin)
        }
    }
}
