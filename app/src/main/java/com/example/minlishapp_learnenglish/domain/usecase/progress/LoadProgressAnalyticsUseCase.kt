package com.example.minlishapp_learnenglish.domain.usecase.progress

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AnalyticsRepository
import com.example.minlishapp_learnenglish.domain.model.ProgressAnalytics

class LoadProgressAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): AppResult<ProgressAnalytics> {
        val dashboardResult = analyticsRepository.getDashboard()
        if (dashboardResult is AppResult.Failure) return dashboardResult

        val activityResult = analyticsRepository.getActivity()
        if (activityResult is AppResult.Failure) return activityResult

        val retentionResult = analyticsRepository.getRetention()
        if (retentionResult is AppResult.Failure) return retentionResult

        return AppResult.Success(
            ProgressAnalytics(
                stats = (dashboardResult as AppResult.Success).data,
                activities = (activityResult as AppResult.Success).data,
                retention = (retentionResult as AppResult.Success).data
            )
        )
    }
}
