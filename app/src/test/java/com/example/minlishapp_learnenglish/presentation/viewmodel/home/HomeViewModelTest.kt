package com.example.minlishapp_learnenglish.presentation.viewmodel.home

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AnalyticsRepository
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.domain.model.AuthSession
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import com.example.minlishapp_learnenglish.domain.model.User
import com.example.minlishapp_learnenglish.domain.usecase.home.LoadHomeUseCase
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load home success exposes dashboard daily plan and activity`() = runTest {
        val viewModel = HomeViewModel(
            LoadHomeUseCase(
                authRepository = FakeAuthRepository(),
                analyticsRepository = FakeAnalyticsRepository(),
                learningRepository = FakeLearningRepository()
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("Nhat Nguyen", state.userName)
        assertEquals("A1 Beginner", state.userLevel)
        assertEquals(120, state.dashboardStats?.learnedWords)
        assertEquals(24, state.dailyPlan?.dueReviews)
        assertEquals(2, state.activities.size)
    }

    @Test
    fun `load home error exposes retryable error state`() = runTest {
        val error = AppError.Network("Không thể kết nối máy chủ.")
        val viewModel = HomeViewModel(
            LoadHomeUseCase(
                authRepository = FakeAuthRepository(),
                analyticsRepository = FakeAnalyticsRepository(
                    dashboardResult = AppResult.Failure(error)
                ),
                learningRepository = FakeLearningRepository()
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(error.message, state.errorMessage)
        assertNull(state.dashboardStats)
    }

    @Test
    fun `start review emits navigate review due effect`() = runTest {
        val viewModel = HomeViewModel(
            LoadHomeUseCase(
                authRepository = FakeAuthRepository(),
                analyticsRepository = FakeAnalyticsRepository(),
                learningRepository = FakeLearningRepository()
            )
        )
        val effects = mutableListOf<HomeEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.startReview()
        advanceUntilIdle()

        assertEquals(listOf(HomeEffect.NavigateReviewDue), effects)
    }
}

private class FakeAuthRepository(
    private val getMeResult: AppResult<User> = AppResult.Success(sampleUser)
) : AuthRepository {
    override suspend fun login(email: String, password: String): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Không dùng trong test."))
    }

    override suspend fun refresh(refreshToken: String): AppResult<String> {
        return AppResult.Success("access-token")
    }

    override suspend fun logout(refreshToken: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    override suspend fun getMe(): AppResult<User> = getMeResult

    override suspend fun updateMe(
        name: String?,
        goal: String?,
        level: String?,
        dailyNewWords: Int?
    ): AppResult<User> {
        return AppResult.Success(
            sampleUser.copy(
                name = name ?: sampleUser.name,
                goal = goal ?: sampleUser.goal,
                level = level ?: sampleUser.level,
                dailyNewWords = dailyNewWords ?: sampleUser.dailyNewWords
            )
        )
    }
}

private class FakeAnalyticsRepository(
    private val dashboardResult: AppResult<ProgressStats> = AppResult.Success(sampleStats),
    private val activityResult: AppResult<List<DailyActivity>> = AppResult.Success(sampleActivities),
    private val retentionResult: AppResult<RetentionStats> = AppResult.Success(sampleRetention)
) : AnalyticsRepository {
    override suspend fun getDashboard(): AppResult<ProgressStats> = dashboardResult

    override suspend fun getActivity(): AppResult<List<DailyActivity>> = activityResult

    override suspend fun getRetention(): AppResult<RetentionStats> = retentionResult
}

private class FakeLearningRepository(
    private val dailyPlanResult: AppResult<DailyLearningPlan> = AppResult.Success(sampleDailyPlan)
) : LearningRepository {
    override suspend fun getDailyPlan(): AppResult<DailyLearningPlan> = dailyPlanResult

    override suspend fun getReviewCards(
        deckId: Long?,
        mode: String?
    ): AppResult<List<ReviewCard>> {
        return AppResult.Success(emptyList())
    }

    override suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult> {
        return AppResult.Success(
            SubmitReviewResult(
                vocabularyItemId = vocabularyItemId,
                rating = rating,
                isCorrect = rating != ReviewRating.Again,
                repetitions = 1,
                intervalDays = 1,
                easeFactor = 2.5,
                nextDueAt = "2026-05-28T00:00:00Z"
            )
        )
    }
}

private val sampleUser = User(
    id = 1L,
    email = "nhat@example.com",
    name = "Nhat Nguyen",
    goal = "General English",
    level = "A1 Beginner",
    dailyNewWords = 10
)

private val sampleStats = ProgressStats(
    learnedWords = 120,
    dueToday = 24,
    streak = 5,
    accuracy = 82.5,
    levelEstimation = "Intermediate"
)

private val sampleDailyPlan = DailyLearningPlan(
    dailyGoal = 10,
    newCards = 10,
    dueReviews = 24,
    totalAvailable = 600
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
