package com.example.minlishapp_learnenglish.data.remote.api

import com.example.minlishapp_learnenglish.data.remote.dto.NotificationPreferencesResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateNotificationPreferencesRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface NotificationApi {
    @GET("notifications/preferences")
    suspend fun getPreferences(): NotificationPreferencesResponseDto

    @PATCH("notifications/preferences")
    suspend fun updatePreferences(
        @Body request: UpdateNotificationPreferencesRequestDto
    ): NotificationPreferencesResponseDto
}
