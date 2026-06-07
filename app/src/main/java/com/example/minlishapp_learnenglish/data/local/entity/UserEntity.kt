package com.example.minlishapp_learnenglish.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String?,
    val goal: String?,
    val level: String?,
    val dailyNewWords: Int = 10,
    val isLoggedIn: Boolean = false
)
