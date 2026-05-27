package com.example.minlishapp_learnenglish.data.remote.dto

import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardResponseDto(
    @param:Json(name = "learned_words") val learnedWords: Int,
    @param:Json(name = "due_today") val dueToday: Int,
    @param:Json(name = "streak") val streak: Int,
    @param:Json(name = "accuracy") val accuracy: Double,
    @param:Json(name = "level_estimation") val levelEstimation: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityResponseDto(
    @param:Json(name = "days") val days: List<DailyActivityDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyActivityDto(
    @param:Json(name = "date") val date: String,
    @param:Json(name = "review_count") val reviewCount: Int,
    @param:Json(name = "correct_count") val correctCount: Int
)

@JsonClass(generateAdapter = true)
data class RetentionResponseDto(
    @param:Json(name = "retention_rate") val retentionRate: Double,
    @param:Json(name = "total_reviews") val totalReviews: Int,
    @param:Json(name = "retained_reviews") val retainedReviews: Int
)

fun DashboardResponseDto.toDomain(): ProgressStats {
    return ProgressStats(
        learnedWords = learnedWords,
        dueToday = dueToday,
        streak = streak,
        accuracy = accuracy,
        levelEstimation = levelEstimation
    )
}

fun DailyActivityDto.toDomain(): DailyActivity {
    return DailyActivity(
        date = date,
        reviewCount = reviewCount,
        correctCount = correctCount
    )
}

fun RetentionResponseDto.toDomain(): RetentionStats {
    return RetentionStats(
        retentionRate = retentionRate,
        totalReviews = totalReviews,
        retainedReviews = retainedReviews
    )
}
