package com.example.minlishapp_learnenglish.domain.usecase.notification

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.NotificationRepository
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings

class GetNotificationSettingsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): AppResult<NotificationSettings> {
        return notificationRepository.getPreferences()
    }
}

class UpdateNotificationSettingsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        dailyTime: String,
        timezone: String,
        emailEnabled: Boolean,
        pushEnabled: Boolean
    ): AppResult<NotificationSettings> {
        return notificationRepository.updatePreferences(
            dailyTime = dailyTime,
            timezone = timezone,
            emailEnabled = emailEnabled,
            pushEnabled = pushEnabled
        )
    }
}
