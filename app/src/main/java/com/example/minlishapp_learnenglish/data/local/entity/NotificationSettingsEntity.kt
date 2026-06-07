package com.example.minlishapp_learnenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    val userId: Long,
    val dailyTime: String = "20:00",
    val timezone: String = "Asia/Ho_Chi_Minh",
    val emailEnabled: Boolean = false,
    val pushEnabled: Boolean = true
)
