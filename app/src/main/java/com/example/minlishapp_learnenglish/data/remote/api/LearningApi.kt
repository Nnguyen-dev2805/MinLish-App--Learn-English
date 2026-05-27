package com.example.minlishapp_learnenglish.data.remote.api

import com.example.minlishapp_learnenglish.data.remote.dto.DailyPlanResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.ReviewCardsResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.SubmitReviewRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.SubmitReviewResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LearningApi {
    @GET("learning/daily-plan")
    suspend fun getDailyPlan(): DailyPlanResponseDto

    @GET("learning/review-cards")
    suspend fun getReviewCards(): ReviewCardsResponseDto

    @POST("learning/reviews")
    suspend fun submitReview(
        @Body request: SubmitReviewRequestDto
    ): SubmitReviewResponseDto
}
