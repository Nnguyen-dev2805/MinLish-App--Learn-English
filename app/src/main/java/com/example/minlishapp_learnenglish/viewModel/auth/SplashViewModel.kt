package com.example.minlishapp_learnenglish.viewModel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SplashUiState(
    val isChecking: Boolean = true
)

class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState(isChecking = true)
            val hasLoggedInUser = authRepository.getMe() is AppResult.Success
            _uiState.value = SplashUiState(isChecking = false)
            _effects.emit(
                if (hasLoggedInUser) {
                    AuthEffect.NavigateHome
                } else {
                    AuthEffect.NavigateLogin
                }
            )
        }
    }
}
