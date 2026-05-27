package com.example.minlishapp_learnenglish.data.remote.dto

import com.example.minlishapp_learnenglish.domain.model.AuthSession
import com.example.minlishapp_learnenglish.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class GoogleLoginRequestDto(
    @param:Json(name = "id_token") val idToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshRequestDto(
    @param:Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class LogoutRequestDto(
    @param:Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserRequestDto(
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "goal") val goal: String? = null,
    @param:Json(name = "level") val level: String? = null,
    @param:Json(name = "daily_new_words") val dailyNewWords: Int? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @param:Json(name = "access_token") val accessToken: String,
    @param:Json(name = "refresh_token") val refreshToken: String,
    @param:Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class RefreshResponseDto(
    @param:Json(name = "access_token") val accessToken: String
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "email") val email: String,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "goal") val goal: String? = null,
    @param:Json(name = "level") val level: String? = null,
    @param:Json(name = "daily_new_words") val dailyNewWords: Int = 10
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        name = name,
        goal = goal,
        level = level,
        dailyNewWords = dailyNewWords
    )
}

fun AuthResponseDto.toDomain(): AuthSession {
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}
