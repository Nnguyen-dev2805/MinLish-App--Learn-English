package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.domain.usecase.auth.CheckSessionUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.SessionDestination
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
    private val checkSessionUseCase: CheckSessionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState(isChecking = true)
            val destination = runCatching { checkSessionUseCase() }.getOrDefault(SessionDestination.Login)
            _uiState.value = SplashUiState(isChecking = false)
            _effects.emit(
                when (destination) {
                    SessionDestination.Home -> AuthEffect.NavigateHome
                    SessionDestination.Login -> AuthEffect.NavigateLogin
                }
            )
        }
    }
}
