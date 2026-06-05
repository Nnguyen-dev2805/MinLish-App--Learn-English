package com.example.minlishapp_learnenglish.presentation.viewmodel.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.ReviewSessionSummary
import com.example.minlishapp_learnenglish.domain.usecase.learning.GetReviewCardsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.learning.SubmitReviewUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// class chứa mọi state để UI vẽ màn hình
data class FlashcardUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val cards: List<ReviewCard> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val errorMessage: String? = null,
    val isReviewSession: Boolean = false,
    val summary: ReviewSessionSummary = ReviewSessionSummary()
) {
    val currentCard: ReviewCard?
        get() = cards.getOrNull(currentIndex)

    val currentPosition: Int
        get() = when {
            cards.isEmpty() -> 0
            isReviewSession && summary.totalCards > 0 ->
                (summary.reviewedCards + 1).coerceAtMost(summary.totalCards)
            currentIndex >= cards.size -> cards.size
            else -> currentIndex + 1
        }

    val progressFraction: Float
        get() {
            val total = if (isReviewSession && summary.totalCards > 0) {
                summary.totalCards
            } else {
                cards.size
            }
            return if (total == 0) 0f else currentPosition.toFloat() / total.toFloat()
        }

    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && cards.isEmpty()

    val isCompleted: Boolean
        get() = !isLoading && cards.isNotEmpty() && currentIndex >= cards.size
}

// các event mà UI gửi cho VM
sealed interface FlashcardEvent {
    data object Retry : FlashcardEvent
    data object ShowAnswer : FlashcardEvent
    data object PreviousCard : FlashcardEvent
    data object NextCard : FlashcardEvent
    data object BackClicked : FlashcardEvent
    data class SubmitRating(val rating: ReviewRating) : FlashcardEvent
}

// event chỉ xảy ra 1 lần
sealed interface FlashcardEffect {
    data object NavigateBack : FlashcardEffect
    data class NavigateReviewResults(val summary: ReviewSessionSummary) : FlashcardEffect
    data class ShowSnackbar(val message: String) : FlashcardEffect
}

class FlashcardViewModel(
    private val getReviewCardsUseCase: GetReviewCardsUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val deckId: Long? = null,
    private val mode: String? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<FlashcardEffect>()
    val effects: SharedFlow<FlashcardEffect> = _effects.asSharedFlow()

    private var cardStartedAtMs: Long = System.currentTimeMillis()
    private val wrongAttemptsByCardId = mutableMapOf<Long, Int>()
    private val isDueReviewSession: Boolean
        get() = mode == "due"

    init {
        loadCards()
    }

    fun onEvent(event: FlashcardEvent) {
        when (event) {
            FlashcardEvent.Retry -> loadCards()
            FlashcardEvent.ShowAnswer -> toggleAnswer()
            FlashcardEvent.PreviousCard -> goToPreviousCard()
            FlashcardEvent.NextCard -> goToNextCard()
            FlashcardEvent.BackClicked -> navigateBack()
            is FlashcardEvent.SubmitRating -> submitRating(event.rating)
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
                    isAnswerVisible = false
                )
            }

            when (val result = getReviewCardsUseCase(deckId = deckId, mode = mode)) {
                is AppResult.Success -> {
                    cardStartedAtMs = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cards = result.data,
                            currentIndex = 0,
                            isAnswerVisible = false,
                            errorMessage = null,
                            isReviewSession = isDueReviewSession,
                            summary = ReviewSessionSummary(totalCards = result.data.size)
                        )
                    }
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

    private fun toggleAnswer() {
        _uiState.update {
            if (it.currentCard == null || it.isSubmitting) {
                it
            } else {
                it.copy(isAnswerVisible = !it.isAnswerVisible)
            }
        }
    }

    private fun goToPreviousCard() {
        _uiState.update { state ->
            if (state.isSubmitting || state.currentIndex <= 0) {
                state
            } else {
                cardStartedAtMs = System.currentTimeMillis()
                state.copy(
                    currentIndex = state.currentIndex - 1,
                    isAnswerVisible = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun goToNextCard() {
        _uiState.update { state ->
            if (state.isSubmitting || state.cards.isEmpty() || state.currentIndex >= state.cards.lastIndex) {
                state
            } else {
                cardStartedAtMs = System.currentTimeMillis()
                state.copy(
                    currentIndex = state.currentIndex + 1,
                    isAnswerVisible = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun submitRating(rating: ReviewRating) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        if (state.isSubmitting || !state.isAnswerVisible) return

        if (isDueReviewSession && rating == ReviewRating.Again) {
            handleWrongReviewAnswer(card)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val responseMs = elapsedResponseMs()
            when (
                val result = submitReviewUseCase(
                    vocabularyItemId = card.id,
                    rating = rating,
                    responseMs = responseMs
                )
            ) {
                is AppResult.Success -> {
                    val updatedSummary = _uiState.value.summary.record(
                        rating = result.data.rating,
                        isCorrect = result.data.isCorrect
                    )
                    val isComplete = if (isDueReviewSession) {
                        removeCurrentReviewCard(updatedSummary)
                    } else {
                        moveToNextLearningCard(updatedSummary)
                    }
                    cardStartedAtMs = System.currentTimeMillis()
                    if (isComplete) {
                        _effects.emit(FlashcardEffect.NavigateReviewResults(updatedSummary))
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isAnswerVisible = true,
                            errorMessage = result.error.message
                        )
                    }
                    _effects.emit(FlashcardEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun handleWrongReviewAnswer(card: ReviewCard) {
        viewModelScope.launch {
            val attempts = (wrongAttemptsByCardId[card.id] ?: 0) + 1
            wrongAttemptsByCardId[card.id] = attempts

            if (attempts < 2) {
                _uiState.update { state ->
                    val cards = state.cards.toMutableList()
                    if (cards.isNotEmpty()) {
                        val current = cards.removeAt(state.currentIndex)
                        cards.add(current)
                    }
                    state.copy(
                        cards = cards,
                        currentIndex = state.currentIndex.coerceAtMost((cards.size - 1).coerceAtLeast(0)),
                        isAnswerVisible = false,
                        errorMessage = null
                    )
                }
                cardStartedAtMs = System.currentTimeMillis()
                _effects.emit(FlashcardEffect.ShowSnackbar("Incorrect. This word was moved to the end of the review."))
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = submitReviewUseCase(
                    vocabularyItemId = card.id,
                    rating = ReviewRating.Again,
                    responseMs = elapsedResponseMs()
                )
            ) {
                is AppResult.Success -> {
                    val updatedSummary = _uiState.value.summary.record(
                        rating = result.data.rating,
                        isCorrect = false
                    )
                    val isComplete = removeCurrentReviewCard(updatedSummary)
                    cardStartedAtMs = System.currentTimeMillis()
                    if (isComplete) {
                        _effects.emit(FlashcardEffect.NavigateReviewResults(updatedSummary))
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isAnswerVisible = true,
                            errorMessage = result.error.message
                        )
                    }
                    _effects.emit(FlashcardEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun moveToNextLearningCard(updatedSummary: ReviewSessionSummary): Boolean {
        val nextIndex = _uiState.value.currentIndex + 1
        _uiState.update {
            it.copy(
                isSubmitting = false,
                currentIndex = nextIndex,
                isAnswerVisible = false,
                summary = updatedSummary
            )
        }
        return nextIndex >= _uiState.value.cards.size
    }

    private fun removeCurrentReviewCard(updatedSummary: ReviewSessionSummary): Boolean {
        var isComplete = false
        _uiState.update { state ->
            val cards = state.cards.toMutableList()
            if (cards.isNotEmpty()) {
                cards.removeAt(state.currentIndex)
            }
            isComplete = cards.isEmpty()
            state.copy(
                isSubmitting = false,
                cards = cards,
                currentIndex = state.currentIndex.coerceAtMost((cards.size - 1).coerceAtLeast(0)),
                isAnswerVisible = false,
                summary = updatedSummary
            )
        }
        return isComplete
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(FlashcardEffect.NavigateBack)
        }
    }

    private fun elapsedResponseMs(): Int {
        val elapsed = System.currentTimeMillis() - cardStartedAtMs
        return elapsed.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
