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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val goal: String = "General English",
    val level: String = "A1 Beginner",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SetupEvent {
    data class GoalChanged(val value: String) : SetupEvent
    data class LevelChanged(val value: String) : SetupEvent
    data object Submit : SetupEvent
    data object Skip : SetupEvent
}

sealed interface SetupEffect {
    data object NavigateHome : SetupEffect
    data class ShowSnackbar(val message: String) : SetupEffect
}

class SetupViewModel(
    private val authRepository: AuthRepository,
    private val userName: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SetupEffect>()
    val effects: SharedFlow<SetupEffect> = _effects.asSharedFlow()

    fun onEvent(event: SetupEvent) {
        when (event) {
            is SetupEvent.GoalChanged -> _uiState.update { it.copy(goal = event.value, errorMessage = null) }
            is SetupEvent.LevelChanged -> _uiState.update { it.copy(level = event.value, errorMessage = null) }
            SetupEvent.Submit -> submit()
            SetupEvent.Skip -> {
                viewModelScope.launch {
                    _effects.emit(SetupEffect.NavigateHome)
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.updateMe(
                name = userName,
                goal = state.goal,
                level = state.level,
                dailyNewWords = 10
            )) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(SetupEffect.NavigateHome)
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.error.message) }
                    _effects.emit(SetupEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
