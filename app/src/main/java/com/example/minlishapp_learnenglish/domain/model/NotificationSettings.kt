package com.example.minlishapp_learnenglish.domain.model

data class NotificationSettings(
    val dailyTime: String,
    val timezone: String,
    val emailEnabled: Boolean,
    val pushEnabled: Boolean
)
