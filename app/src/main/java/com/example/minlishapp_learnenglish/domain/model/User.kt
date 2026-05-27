package com.example.minlishapp_learnenglish.domain.model

data class User(
    val id: Long,
    val email: String,
    val name: String?,
    val goal: String?,
    val level: String?,
    val dailyNewWords: Int
)
