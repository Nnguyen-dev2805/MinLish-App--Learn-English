package com.example.minlishapp_learnenglish.domain.usecase.learning

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult

class GetReviewCardsUseCase(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): AppResult<List<ReviewCard>> {
        return learningRepository.getReviewCards()
    }
}

class SubmitReviewUseCase(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult> {
        return learningRepository.submitReview(
            vocabularyItemId = vocabularyItemId,
            rating = rating,
            responseMs = responseMs
        )
    }
}
