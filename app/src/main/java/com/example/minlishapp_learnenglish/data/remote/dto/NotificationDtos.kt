package com.example.minlishapp_learnenglish.data.remote.dto

import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationPreferencesResponseDto(
    @param:Json(name = "daily_time") val dailyTime: String,
    @param:Json(name = "timezone") val timezone: String,
    @param:Json(name = "email_enabled") val emailEnabled: Boolean,
    @param:Json(name = "push_enabled") val pushEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class UpdateNotificationPreferencesRequestDto(
    @param:Json(name = "daily_time") val dailyTime: String? = null,
    @param:Json(name = "timezone") val timezone: String? = null,
    @param:Json(name = "email_enabled") val emailEnabled: Boolean? = null,
    @param:Json(name = "push_enabled") val pushEnabled: Boolean? = null
)

fun NotificationPreferencesResponseDto.toDomain(): NotificationSettings {
    return NotificationSettings(
        dailyTime = dailyTime,
        timezone = timezone,
        emailEnabled = emailEnabled,
        pushEnabled = pushEnabled
    )
}
