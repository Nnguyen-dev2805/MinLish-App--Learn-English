package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val goal: String = "General English",
    val level: String = "A1 Beginner",
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null,
    val apiError: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, apiError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, apiError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, apiError = null) }
    }

    fun onGoalChange(value: String) {
        _uiState.update { it.copy(goal = value) }
    }

    fun onLevelChange(value: String) {
        _uiState.update { it.copy(level = value) }
    }

    fun onTermsChange(value: Boolean) {
        _uiState.update { it.copy(acceptedTerms = value, termsError = null) }
    }

    fun register() {
        val current = _uiState.value
        val nameError = if (current.name.isBlank()) "Tên không được để trống." else null
        val emailError = if (current.email.isBlank()) "Email không được để trống." else null
        val passwordError = when {
            current.password.isBlank() -> "Password không được để trống."
            current.password.length < 6 -> "Password tối thiểu 6 ký tự."
            else -> null
        }
        val termsError = if (!current.acceptedTerms) "Bạn cần đồng ý điều khoản." else null
        if (nameError != null || emailError != null || passwordError != null || termsError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    termsError = termsError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            when (val result = registerUseCase(current.name.trim(), current.email.trim(), current.password)) {
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
}
