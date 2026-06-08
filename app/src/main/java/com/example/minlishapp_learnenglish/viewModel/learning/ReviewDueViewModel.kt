package com.example.minlishapp_learnenglish.viewModel.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.ReviewSessionSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewDueChoice(
    val text: String,
    val isCorrect: Boolean
)

data class ReviewDueUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val cards: List<ReviewCard> = emptyList(),
    val currentIndex: Int = 0,
    val choices: List<ReviewDueChoice> = emptyList(),
    val selectedChoiceIndex: Int? = null,
    val errorMessage: String? = null,
    val summary: ReviewSessionSummary = ReviewSessionSummary()
) {
    val currentCard: ReviewCard?
        get() = cards.getOrNull(currentIndex)

    val currentPosition: Int
        get() = when {
            summary.totalCards == 0 -> 0
            cards.isEmpty() -> summary.totalCards
            else -> (summary.reviewedCards + 1).coerceAtMost(summary.totalCards)
        }

    val progressFraction: Float
        get() = if (summary.totalCards == 0) {
            0f
        } else {
            currentPosition.toFloat() / summary.totalCards.toFloat()
        }

    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && cards.isEmpty() && summary.totalCards == 0
}

sealed interface ReviewDueEvent {
    data object Retry : ReviewDueEvent
    data object BackClicked : ReviewDueEvent
    data object SubmitAnswer : ReviewDueEvent
    data class ChoiceSelected(val index: Int) : ReviewDueEvent
}

sealed interface ReviewDueEffect {
    data object NavigateBack : ReviewDueEffect
    data class NavigateReviewResults(val summary: ReviewSessionSummary) : ReviewDueEffect
    data class ShowSnackbar(val message: String) : ReviewDueEffect
}

class ReviewDueViewModel(
    private val learningRepository: LearningRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewDueUiState())
    val uiState: StateFlow<ReviewDueUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ReviewDueEffect>()
    val effects: SharedFlow<ReviewDueEffect> = _effects.asSharedFlow()

    private val wrongAttemptsByCardId = mutableMapOf<Long, Int>()
    private var cardStartedAtMs: Long = System.currentTimeMillis()

    init {
        loadCards()
    }

    fun onEvent(event: ReviewDueEvent) {
        when (event) {
            ReviewDueEvent.Retry -> loadCards()
            ReviewDueEvent.BackClicked -> navigateBack()
            ReviewDueEvent.SubmitAnswer -> submitAnswer()
            is ReviewDueEvent.ChoiceSelected -> selectChoice(event.index)
        }
    }

    fun loadCards() {
        viewModelScope.launch {
            wrongAttemptsByCardId.clear()
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSubmitting = false,
                    errorMessage = null,
                    selectedChoiceIndex = null
                )
            }

            when (val result = learningRepository.getReviewCards(mode = "due")) {
                is AppResult.Success -> {
                    val cards = result.data
                    cardStartedAtMs = System.currentTimeMillis()
                    _uiState.value = ReviewDueUiState(
                        isLoading = false,
                        cards = cards,
                        choices = buildChoices(cards, 0),
                        summary = ReviewSessionSummary(totalCards = cards.size)
                    )
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSubmitting = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun selectChoice(index: Int) {
        _uiState.update { state ->
            if (state.isSubmitting || index !in state.choices.indices) {
                state
            } else {
                state.copy(selectedChoiceIndex = index, errorMessage = null)
            }
        }
    }

    private fun submitAnswer() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        val selectedChoice = state.selectedChoiceIndex?.let { state.choices.getOrNull(it) }
        if (state.isSubmitting) return
        if (selectedChoice == null) {
            viewModelScope.launch {
                _effects.emit(ReviewDueEffect.ShowSnackbar("Please choose an answer."))
            }
            return
        }

        if (selectedChoice.isCorrect) {
            submitReview(card = card, rating = ReviewRating.Good, isCorrect = true)
        } else {
            handleWrongAnswer(card)
        }
    }

    private fun handleWrongAnswer(card: ReviewCard) {
        viewModelScope.launch {
            val attempts = (wrongAttemptsByCardId[card.id] ?: 0) + 1
            wrongAttemptsByCardId[card.id] = attempts

            if (attempts < 2) {
                moveCurrentCardToEnd()
                _effects.emit(ReviewDueEffect.ShowSnackbar("Incorrect. This word will appear again."))
            } else {
                submitReview(card = card, rating = ReviewRating.Again, isCorrect = false)
            }
        }
    }

    private fun submitReview(card: ReviewCard, rating: ReviewRating, isCorrect: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = learningRepository.submitReview(
                    vocabularyItemId = card.id,
                    rating = rating,
                    responseMs = elapsedResponseMs()
                )
            ) {
                is AppResult.Success -> {
                    val updatedSummary = _uiState.value.summary.record(
                        rating = result.data.rating,
                        isCorrect = isCorrect
                    )
                    val isComplete = removeCurrentCard(updatedSummary)
                    if (!isCorrect) {
                        _effects.emit(ReviewDueEffect.ShowSnackbar("Keep practicing this word."))
                    }
                    if (isComplete) {
                        _effects.emit(ReviewDueEffect.NavigateReviewResults(updatedSummary))
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.error.message
                        )
                    }
                    _effects.emit(ReviewDueEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun moveCurrentCardToEnd() {
        _uiState.update { state ->
            val updatedCards = state.cards.toMutableList()
            if (updatedCards.isNotEmpty()) {
                val current = updatedCards.removeAt(state.currentIndex)
                updatedCards.add(current)
            }
            val nextIndex = state.currentIndex.coerceAtMost((updatedCards.size - 1).coerceAtLeast(0))
            cardStartedAtMs = System.currentTimeMillis()
            state.copy(
                cards = updatedCards,
                currentIndex = nextIndex,
                choices = buildChoices(updatedCards, nextIndex),
                selectedChoiceIndex = null,
                errorMessage = null
            )
        }
    }

    private fun removeCurrentCard(updatedSummary: ReviewSessionSummary): Boolean {
        var completed = false
        _uiState.update { state ->
            val updatedCards = state.cards.toMutableList()
            if (updatedCards.isNotEmpty()) {
                updatedCards.removeAt(state.currentIndex)
            }
            completed = updatedCards.isEmpty()
            val nextIndex = state.currentIndex.coerceAtMost((updatedCards.size - 1).coerceAtLeast(0))
            cardStartedAtMs = System.currentTimeMillis()
            state.copy(
                isSubmitting = false,
                cards = updatedCards,
                currentIndex = nextIndex,
                choices = buildChoices(updatedCards, nextIndex),
                selectedChoiceIndex = null,
                errorMessage = null,
                summary = updatedSummary
            )
        }
        return completed
    }

    private fun buildChoices(cards: List<ReviewCard>, currentIndex: Int): List<ReviewDueChoice> {
        val currentCard = cards.getOrNull(currentIndex) ?: return emptyList()
        val correctAnswer = currentCard.meaning.trim()
        val wrongAnswers = cards
            .asSequence()
            .filter { it.id != currentCard.id }
            .map { it.meaning.trim() }
            .filter { it.isNotBlank() && !it.equals(correctAnswer, ignoreCase = true) }
            .distinct()
            .take(3)
            .toList()

        val answers = (wrongAnswers + correctAnswer).distinct()
        if (answers.isEmpty()) return emptyList()

        val rotatedAnswers = answers.rotateBy((currentCard.id % answers.size).toInt())
        return rotatedAnswers.map { answer ->
            ReviewDueChoice(
                text = answer,
                isCorrect = answer.equals(correctAnswer, ignoreCase = true)
            )
        }
    }

    private fun List<String>.rotateBy(offset: Int): List<String> {
        if (isEmpty()) return this
        val safeOffset = offset.coerceIn(0, lastIndex)
        return drop(safeOffset) + take(safeOffset)
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(ReviewDueEffect.NavigateBack)
        }
    }

    private fun elapsedResponseMs(): Int {
        val elapsed = System.currentTimeMillis() - cardStartedAtMs
        return elapsed.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
