package com.example.minlishapp_learnenglish.domain.model

data class DailyLearningPlan(
    val dailyGoal: Int,
    val newCards: Int,
    val dueReviews: Int,
    val totalAvailable: Int
)

enum class ReviewRating {
    Again,
    Hard,
    Good,
    Easy
}

data class ReviewCard(
    val id: Long,
    val deckId: Long,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    val description: String?,
    val example: String?,
    val note: String?,
    val imageUrl: String?,
    val wordAudioUrl: String?,
    val meaningAudioUrl: String?,
    val exampleAudioUrl: String?,
    val isNew: Boolean,
    val dueAt: String?
) {
    val hasAudio: Boolean
        get() = wordAudioUrl != null || meaningAudioUrl != null || exampleAudioUrl != null
}

data class SubmitReviewResult(
    val vocabularyItemId: Long,
    val rating: ReviewRating,
    val isCorrect: Boolean,
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextDueAt: String
)

data class ReviewSessionSummary(
    val totalCards: Int = 0,
    val reviewedCards: Int = 0,
    val correctCount: Int = 0,
    val againCount: Int = 0,
    val hardCount: Int = 0,
    val goodCount: Int = 0,
    val easyCount: Int = 0
) {
    val isComplete: Boolean
        get() = totalCards > 0 && reviewedCards >= totalCards

    val accuracy: Int
        get() = if (reviewedCards == 0) 0 else ((correctCount * 100f) / reviewedCards).toInt()

    val nextReviewCount: Int
        get() = againCount + hardCount

    fun record(rating: ReviewRating, isCorrect: Boolean): ReviewSessionSummary {
        return copy(
            reviewedCards = reviewedCards + 1,
            correctCount = correctCount + if (isCorrect) 1 else 0,
            againCount = againCount + if (rating == ReviewRating.Again) 1 else 0,
            hardCount = hardCount + if (rating == ReviewRating.Hard) 1 else 0,
            goodCount = goodCount + if (rating == ReviewRating.Good) 1 else 0,
            easyCount = easyCount + if (rating == ReviewRating.Easy) 1 else 0
        )
    }
}
