package com.example.minlishapp_learnenglish.data.remote.dto

import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DailyPlanResponseDto(
    @param:Json(name = "daily_goal") val dailyGoal: Int,
    @param:Json(name = "new_cards") val newCards: Int,
    @param:Json(name = "due_reviews") val dueReviews: Int,
    @param:Json(name = "total_available") val totalAvailable: Int
)

fun DailyPlanResponseDto.toDomain(): DailyLearningPlan {
    return DailyLearningPlan(
        dailyGoal = dailyGoal,
        newCards = newCards,
        dueReviews = dueReviews,
        totalAvailable = totalAvailable
    )
}

@JsonClass(generateAdapter = true)
data class ReviewCardsResponseDto(
    @param:Json(name = "items") val items: List<ReviewCardResponseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ReviewCardResponseDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "deck_id") val deckId: Long,
    @param:Json(name = "word") val word: String,
    @param:Json(name = "pronunciation") val pronunciation: String? = null,
    @param:Json(name = "meaning") val meaning: String,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "example") val example: String? = null,
    @param:Json(name = "note") val note: String? = null,
    @param:Json(name = "image_url") val imageUrl: String? = null,
    @param:Json(name = "word_audio_url") val wordAudioUrl: String? = null,
    @param:Json(name = "meaning_audio_url") val meaningAudioUrl: String? = null,
    @param:Json(name = "example_audio_url") val exampleAudioUrl: String? = null,
    @param:Json(name = "is_new") val isNew: Boolean,
    @param:Json(name = "due_at") val dueAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SubmitReviewRequestDto(
    @param:Json(name = "vocabulary_item_id") val vocabularyItemId: Long,
    @param:Json(name = "rating") val rating: String,
    @param:Json(name = "response_ms") val responseMs: Int? = null
)

@JsonClass(generateAdapter = true)
data class SubmitReviewResponseDto(
    @param:Json(name = "vocabulary_item_id") val vocabularyItemId: Long,
    @param:Json(name = "rating") val rating: String,
    @param:Json(name = "is_correct") val isCorrect: Boolean,
    @param:Json(name = "repetitions") val repetitions: Int,
    @param:Json(name = "interval_days") val intervalDays: Int,
    @param:Json(name = "ease_factor") val easeFactor: Double,
    @param:Json(name = "next_due_at") val nextDueAt: String
)

fun ReviewCardResponseDto.toDomain(): ReviewCard {
    return ReviewCard(
        id = id,
        deckId = deckId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        description = description,
        example = example,
        note = note,
        imageUrl = imageUrl,
        wordAudioUrl = wordAudioUrl,
        meaningAudioUrl = meaningAudioUrl,
        exampleAudioUrl = exampleAudioUrl,
        isNew = isNew,
        dueAt = dueAt
    )
}

fun SubmitReviewResponseDto.toDomain(): SubmitReviewResult {
    return SubmitReviewResult(
        vocabularyItemId = vocabularyItemId,
        rating = ReviewRating.valueOf(rating),
        isCorrect = isCorrect,
        repetitions = repetitions,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        nextDueAt = nextDueAt
    )
}
