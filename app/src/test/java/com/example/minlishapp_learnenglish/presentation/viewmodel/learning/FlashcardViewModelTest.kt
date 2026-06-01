package com.example.minlishapp_learnenglish.presentation.viewmodel.learning

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import com.example.minlishapp_learnenglish.domain.usecase.learning.GetReviewCardsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.learning.SubmitReviewUseCase
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load cards success exposes first card`() = runTest {
        val viewModel = flashcardViewModel(FakeLearningRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("anxious", state.currentCard?.word)
        assertEquals(1, state.currentPosition)
        assertEquals(2, state.summary.totalCards)
    }

    @Test
    fun `empty response exposes empty state`() = runTest {
        val viewModel = flashcardViewModel(
            FakeLearningRepository(cardsResult = AppResult.Success(emptyList()))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `show answer reveals answer side`() = runTest {
        val viewModel = flashcardViewModel(FakeLearningRepository())
        advanceUntilIdle()

        viewModel.onEvent(FlashcardEvent.ShowAnswer)

        assertTrue(viewModel.uiState.value.isAnswerVisible)
    }

    @Test
    fun `submit good moves to next card`() = runTest {
        val repository = FakeLearningRepository()
        val viewModel = flashcardViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(FlashcardEvent.ShowAnswer)
        viewModel.onEvent(FlashcardEvent.SubmitRating(ReviewRating.Good))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, repository.submitCalls)
        assertEquals("adapt", state.currentCard?.word)
        assertEquals(1, state.summary.reviewedCards)
        assertEquals(1, state.summary.correctCount)
        assertFalse(state.isAnswerVisible)
    }

    @Test
    fun `submit error keeps current card visible`() = runTest {
        val error = AppError.Network("Không thể kết nối máy chủ.")
        val viewModel = flashcardViewModel(
            FakeLearningRepository(submitResult = AppResult.Failure(error))
        )
        advanceUntilIdle()

        viewModel.onEvent(FlashcardEvent.ShowAnswer)
        viewModel.onEvent(FlashcardEvent.SubmitRating(ReviewRating.Good))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("anxious", state.currentCard?.word)
        assertTrue(state.isAnswerVisible)
        assertEquals(error.message, state.errorMessage)
    }

    @Test
    fun `complete session emits navigate result effect`() = runTest {
        val viewModel = flashcardViewModel(
            FakeLearningRepository(cardsResult = AppResult.Success(listOf(sampleCards.first())))
        )
        val effects = mutableListOf<FlashcardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        advanceUntilIdle()

        viewModel.onEvent(FlashcardEvent.ShowAnswer)
        viewModel.onEvent(FlashcardEvent.SubmitRating(ReviewRating.Good))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertEquals(1, viewModel.uiState.value.summary.reviewedCards)
        val effect = effects.first()
        assertTrue(effect is FlashcardEffect.NavigateReviewResults)
        assertEquals(1, (effect as FlashcardEffect.NavigateReviewResults).summary.reviewedCards)
        assertEquals(100, effect.summary.accuracy)
    }
}

private fun flashcardViewModel(repository: LearningRepository): FlashcardViewModel {
    return FlashcardViewModel(
        getReviewCardsUseCase = GetReviewCardsUseCase(repository),
        submitReviewUseCase = SubmitReviewUseCase(repository)
    )
}

private class FakeLearningRepository(
    private val cardsResult: AppResult<List<ReviewCard>> = AppResult.Success(sampleCards),
    private val submitResult: AppResult<SubmitReviewResult>? = null
) : LearningRepository {
    var submitCalls = 0
        private set

    override suspend fun getDailyPlan(): AppResult<DailyLearningPlan> {
        return AppResult.Success(
            DailyLearningPlan(
                dailyGoal = 10,
                newCards = 10,
                dueReviews = 0,
                totalAvailable = 600
            )
        )
    }

    override suspend fun getReviewCards(
        deckId: Long?,
        mode: String?
    ): AppResult<List<ReviewCard>> = cardsResult

    override suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult> {
        submitCalls += 1
        return submitResult ?: AppResult.Success(
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

private val sampleCards = listOf(
    ReviewCard(
        id = 1L,
        deckId = 1L,
        word = "anxious",
        pronunciation = "['aeng(k)shes]",
        meaning = "lo âu, băn khoăn",
        description = "When a person is anxious, they worry that something bad will happen.",
        example = "She was anxious about the test.",
        note = "Seed word",
        imageUrl = "/static/media/anki/book2/4000B2_601.jpg",
        wordAudioUrl = "/static/media/anki/book2/4000B2_anxious.mp3",
        meaningAudioUrl = null,
        exampleAudioUrl = null,
        isNew = true,
        dueAt = null
    ),
    ReviewCard(
        id = 2L,
        deckId = 1L,
        word = "adapt",
        pronunciation = null,
        meaning = "thích nghi",
        description = null,
        example = null,
        note = null,
        imageUrl = null,
        wordAudioUrl = null,
        meaningAudioUrl = null,
        exampleAudioUrl = null,
        isNew = true,
        dueAt = null
    )
)
