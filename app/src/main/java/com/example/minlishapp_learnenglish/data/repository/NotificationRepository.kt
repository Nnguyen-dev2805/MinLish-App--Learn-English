package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.map
import com.example.minlishapp_learnenglish.data.remote.api.NotificationApi
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateNotificationPreferencesRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.toDomain
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import com.squareup.moshi.Moshi

interface NotificationRepository {
    suspend fun getPreferences(): AppResult<NotificationSettings>

    suspend fun updatePreferences(
        dailyTime: String? = null,
        timezone: String? = null,
        emailEnabled: Boolean? = null,
        pushEnabled: Boolean? = null
    ): AppResult<NotificationSettings>
}

class DefaultNotificationRepository(
    private val notificationApi: NotificationApi,
    private val moshi: Moshi
) : NotificationRepository {
    override suspend fun getPreferences(): AppResult<NotificationSettings> {
        return safeApiCall(moshi) {
            notificationApi.getPreferences()
        }.map { it.toDomain() }
    }

    override suspend fun updatePreferences(
        dailyTime: String?,
        timezone: String?,
        emailEnabled: Boolean?,
        pushEnabled: Boolean?
    ): AppResult<NotificationSettings> {
        return safeApiCall(moshi) {
            notificationApi.updatePreferences(
                UpdateNotificationPreferencesRequestDto(
                    dailyTime = dailyTime,
                    timezone = timezone,
                    emailEnabled = emailEnabled,
                    pushEnabled = pushEnabled
                )
            )
        }.map { it.toDomain() }
    }
}
