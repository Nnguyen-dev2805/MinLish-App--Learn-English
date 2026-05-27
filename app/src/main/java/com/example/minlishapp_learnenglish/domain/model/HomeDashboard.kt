package com.example.minlishapp_learnenglish.domain.model

data class HomeDashboard(
    val user: User,
    val stats: ProgressStats,
    val dailyPlan: DailyLearningPlan,
    val activities: List<DailyActivity>
) {
    val isEmpty: Boolean
        get() = stats.learnedWords == 0 &&
            stats.dueToday == 0 &&
            dailyPlan.newCards == 0 &&
            dailyPlan.dueReviews == 0 &&
            dailyPlan.totalAvailable == 0 &&
            activities.isEmpty()
}
