package com.example.minlishapp_learnenglish.domain.usecase.home

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AnalyticsRepository
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.domain.model.HomeDashboard

class LoadHomeUseCase(
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): AppResult<HomeDashboard> {
        val userResult = authRepository.getMe()
        if (userResult is AppResult.Failure) return userResult

        val dashboardResult = analyticsRepository.getDashboard()
        if (dashboardResult is AppResult.Failure) return dashboardResult

        val dailyPlanResult = learningRepository.getDailyPlan()
        if (dailyPlanResult is AppResult.Failure) return dailyPlanResult

        val activityResult = analyticsRepository.getActivity()
        if (activityResult is AppResult.Failure) return activityResult

        return AppResult.Success(
            HomeDashboard(
                user = (userResult as AppResult.Success).data,
                stats = (dashboardResult as AppResult.Success).data,
                dailyPlan = (dailyPlanResult as AppResult.Success).data,
                activities = (activityResult as AppResult.Success).data
            )
        )
    }
}
