package com.example.minlishapp_learnenglish.domain.model

data class ProgressStats(
    val learnedWords: Int,
    val dueToday: Int,
    val streak: Int,
    val accuracy: Double,
    val levelEstimation: String?
)

data class DailyActivity(
    val date: String,
    val reviewCount: Int,
    val correctCount: Int
)

data class RetentionStats(
    val retentionRate: Double,
    val totalReviews: Int,
    val retainedReviews: Int
)

data class ProgressAnalytics(
    val stats: ProgressStats,
    val activities: List<DailyActivity>,
    val retention: RetentionStats
) {
    val isEmpty: Boolean
        get() = stats.learnedWords == 0 &&
            stats.dueToday == 0 &&
            stats.streak == 0 &&
            stats.accuracy == 0.0 &&
            activities.all { it.reviewCount == 0 && it.correctCount == 0 } &&
            retention.totalReviews == 0
}
