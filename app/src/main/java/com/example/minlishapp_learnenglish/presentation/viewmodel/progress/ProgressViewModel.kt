package com.example.minlishapp_learnenglish.presentation.viewmodel.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.example.minlishapp_learnenglish.domain.usecase.progress.LoadProgressAnalyticsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val stats: ProgressStats? = null,
    val activities: List<DailyActivity> = emptyList(),
    val retention: RetentionStats? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() {
            val currentStats = stats ?: return false
            val currentRetention = retention ?: return false
            return !isLoading &&
                errorMessage == null &&
                currentStats.learnedWords == 0 &&
                currentStats.dueToday == 0 &&
                currentStats.streak == 0 &&
                currentStats.accuracy == 0.0 &&
                activities.all { it.reviewCount == 0 && it.correctCount == 0 } &&
                currentRetention.totalReviews == 0
        }
}

sealed interface ProgressEvent {
    data object Retry : ProgressEvent
    data object Refresh : ProgressEvent
}

sealed interface ProgressEffect {
    data class ShowSnackbar(val message: String) : ProgressEffect
}

class ProgressViewModel(
    private val loadProgressAnalyticsUseCase: LoadProgressAnalyticsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProgressEffect>()
    val effects: SharedFlow<ProgressEffect> = _effects.asSharedFlow()

    init {
        loadProgress()
    }

    fun onEvent(event: ProgressEvent) {
        when (event) {
            ProgressEvent.Retry -> loadProgress()
            ProgressEvent.Refresh -> refresh()
        }
    }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.stats == null,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
            load(isRefresh = false)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    errorMessage = null
                )
            }
            load(isRefresh = true)
        }
    }

    private suspend fun load(isRefresh: Boolean) {
        when (val result = loadProgressAnalyticsUseCase()) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        stats = result.data.stats,
                        activities = result.data.activities,
                        retention = result.data.retention,
                        errorMessage = null
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
                if (isRefresh) {
                    _effects.emit(ProgressEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
