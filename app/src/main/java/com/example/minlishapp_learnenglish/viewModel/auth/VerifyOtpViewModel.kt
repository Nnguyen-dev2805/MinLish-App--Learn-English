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

data class VerifyOtpUiState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val otpError: String? = null
)

class VerifyOtpViewModel(
    private val authRepository: AuthRepository,
    private val otpManager: OtpManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        VerifyOtpUiState(email = otpManager.getEmail().orEmpty())
    )
    val uiState: StateFlow<VerifyOtpUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun onOtpChange(value: String) {
        _uiState.update {
            it.copy(otp = value.filter(Char::isDigit).take(6), otpError = null)
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank()) {
            viewModelScope.launch { _effects.emit(AuthEffect.NavigateForgotPassword) }
            return
        }
        if (state.otp.length < 6) {
            _uiState.update { it.copy(otpError = "Enter the 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.verifyOtp(email, state.otp)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(AuthEffect.NavigateResetPassword)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, otpError = result.error.message) }
                    _effects.emit(AuthEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _effects.emit(AuthEffect.NavigateForgotPassword)
        }
    }

}
