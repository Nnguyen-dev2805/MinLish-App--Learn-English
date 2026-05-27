package com.example.minlishapp_learnenglish.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.usecase.home.LoadHomeUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val userName: String = "Learner",
    val userLevel: String? = null,
    val dashboardStats: ProgressStats? = null,
    val dailyPlan: DailyLearningPlan? = null,
    val activities: List<DailyActivity> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)

sealed interface HomeEffect {
    data object NavigateLearn : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}

class HomeViewModel(
    private val loadHomeUseCase: LoadHomeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        loadHomeInternal(isRefresh = false)
    }

    fun retry() {
        loadHomeInternal(isRefresh = false)
    }

    fun refresh() {
        loadHomeInternal(isRefresh = true)
    }

    fun startLearning() {
        viewModelScope.launch {
            _effects.emit(HomeEffect.NavigateLearn)
        }
    }

    private fun loadHomeInternal(isRefresh: Boolean) {
        val currentState = _uiState.value
        if (currentState.isRefreshing || (currentState.isLoading && currentState.dashboardStats != null)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.dashboardStats == null,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            when (val result = loadHomeUseCase()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            userName = result.data.user.name?.takeIf { name -> name.isNotBlank() }
                                ?: result.data.user.email.substringBefore("@").ifBlank { "Learner" },
                            userLevel = result.data.user.level,
                            dashboardStats = result.data.stats,
                            dailyPlan = result.data.dailyPlan,
                            activities = result.data.activities,
                            errorMessage = null,
                            isEmpty = result.data.isEmpty
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.error.message
                        )
                    }
                    if (_uiState.value.dashboardStats != null) {
                        _effects.emit(HomeEffect.ShowSnackbar(result.error.message))
                    }
                }
            }
        }
    }
}
