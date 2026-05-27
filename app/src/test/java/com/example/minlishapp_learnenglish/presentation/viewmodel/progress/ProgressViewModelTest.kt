package com.example.minlishapp_learnenglish.presentation.viewmodel.progress

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AnalyticsRepository
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.example.minlishapp_learnenglish.domain.usecase.progress.LoadProgressAnalyticsUseCase
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load progress success exposes dashboard activity and retention`() = runTest {
        val viewModel = progressViewModel(FakeAnalyticsRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(120, state.stats?.learnedWords)
        assertEquals(2, state.activities.size)
        assertEquals(80.0, state.retention?.retentionRate ?: 0.0, 0.0)
    }

    @Test
    fun `dashboard error exposes error state`() = runTest {
        val error = AppError.Network("Không thể kết nối máy chủ.")
        val viewModel = progressViewModel(
            FakeAnalyticsRepository(dashboardResult = AppResult.Failure(error))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(error.message, state.errorMessage)
        assertNull(state.stats)
    }

    @Test
    fun `retention error exposes error state`() = runTest {
        val error = AppError.Server("Máy chủ đang gặp sự cố.")
        val viewModel = progressViewModel(
            FakeAnalyticsRepository(retentionResult = AppResult.Failure(error))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(error.message, state.errorMessage)
    }

    @Test
    fun `retry reloads after an error`() = runTest {
        val repository = FakeAnalyticsRepository(
            dashboardResult = AppResult.Failure(AppError.Network())
        )
        val viewModel = progressViewModel(repository)
        advanceUntilIdle()
        assertEquals("Không thể kết nối máy chủ.", viewModel.uiState.value.errorMessage)

        repository.dashboardResult = AppResult.Success(sampleStats)
        viewModel.onEvent(ProgressEvent.Retry)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(120, state.stats?.learnedWords)
    }
}

private fun progressViewModel(repository: AnalyticsRepository): ProgressViewModel {
    return ProgressViewModel(
        loadProgressAnalyticsUseCase = LoadProgressAnalyticsUseCase(repository)
    )
}

private class FakeAnalyticsRepository(
    var dashboardResult: AppResult<ProgressStats> = AppResult.Success(sampleStats),
    var activityResult: AppResult<List<DailyActivity>> = AppResult.Success(sampleActivities),
    var retentionResult: AppResult<RetentionStats> = AppResult.Success(sampleRetention)
) : AnalyticsRepository {
    override suspend fun getDashboard(): AppResult<ProgressStats> = dashboardResult

    override suspend fun getActivity(): AppResult<List<DailyActivity>> = activityResult

    override suspend fun getRetention(): AppResult<RetentionStats> = retentionResult
}

private val sampleStats = ProgressStats(
    learnedWords = 120,
    dueToday = 8,
    streak = 5,
    accuracy = 82.5,
    levelEstimation = "Intermediate"
)

private val sampleActivities = listOf(
    DailyActivity(date = "2026-05-25", reviewCount = 12, correctCount = 10),
    DailyActivity(date = "2026-05-26", reviewCount = 32, correctCount = 26)
)

private val sampleRetention = RetentionStats(
    retentionRate = 80.0,
    totalReviews = 20,
    retainedReviews = 16
)
