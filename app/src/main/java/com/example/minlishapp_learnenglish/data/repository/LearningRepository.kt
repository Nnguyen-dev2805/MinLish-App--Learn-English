package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.map
import com.example.minlishapp_learnenglish.data.remote.api.LearningApi
import com.example.minlishapp_learnenglish.data.remote.dto.SubmitReviewRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.toDomain
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import com.squareup.moshi.Moshi

interface LearningRepository {
    suspend fun getDailyPlan(): AppResult<DailyLearningPlan>
    suspend fun getReviewCards(): AppResult<List<ReviewCard>>
    suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult>
}

class DefaultLearningRepository(
    private val learningApi: LearningApi,
    private val moshi: Moshi
) : LearningRepository {
    override suspend fun getDailyPlan(): AppResult<DailyLearningPlan> {
        return safeApiCall(moshi) {
            learningApi.getDailyPlan()
        }.map { it.toDomain() }
    }

    override suspend fun getReviewCards(): AppResult<List<ReviewCard>> {
        return safeApiCall(moshi) {
            learningApi.getReviewCards()
        }.map { response ->
            response.items.map { it.toDomain() }
        }
    }

    override suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult> {
        val request = SubmitReviewRequestDto(
            vocabularyItemId = vocabularyItemId,
            rating = rating.name,
            responseMs = responseMs
        )
        return safeApiCall(moshi) {
            learningApi.submitReview(request)
        }.map { it.toDomain() }
    }
}
