package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.map
import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.data.remote.api.AuthApi
import com.example.minlishapp_learnenglish.data.remote.dto.GoogleLoginRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.LoginRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.LogoutRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.RefreshRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.RegisterRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateUserRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.toDomain
import com.example.minlishapp_learnenglish.domain.model.AuthSession
import com.example.minlishapp_learnenglish.domain.model.User
import com.squareup.moshi.Moshi

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<AuthSession>
    suspend fun register(name: String, email: String, password: String): AppResult<AuthSession>
    suspend fun loginWithGoogle(idToken: String): AppResult<AuthSession>
    suspend fun refresh(refreshToken: String): AppResult<String>
    suspend fun logout(refreshToken: String): AppResult<Unit>
    suspend fun getMe(): AppResult<User>
    suspend fun updateMe(
        name: String? = null,
        goal: String? = null,
        level: String? = null,
        dailyNewWords: Int? = null
    ): AppResult<User>
}

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val moshi: Moshi
) : AuthRepository {
    override suspend fun login(email: String, password: String): AppResult<AuthSession> {
        return saveSessionResult {
            authApi.login(LoginRequestDto(email = email, password = password))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<AuthSession> {
        return saveSessionResult {
            authApi.register(RegisterRequestDto(email = email, password = password, name = name))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<AuthSession> {
        return saveSessionResult {
            authApi.loginWithGoogle(GoogleLoginRequestDto(idToken = idToken))
        }
    }

    override suspend fun refresh(refreshToken: String): AppResult<String> {
        return safeApiCall(moshi) {
            authApi.refresh(RefreshRequestDto(refreshToken = refreshToken)).accessToken
        }.also { result ->
            if (result is AppResult.Success) {
                tokenStorage.updateAccessToken(result.data)
            }
        }
    }

    override suspend fun logout(refreshToken: String): AppResult<Unit> {
        val result = safeApiCall(moshi) {
            authApi.logout(LogoutRequestDto(refreshToken = refreshToken))
        }
        if (result is AppResult.Success) {
            tokenStorage.clearTokens()
        }
        return result
    }

    override suspend fun getMe(): AppResult<User> {
        return safeApiCall(moshi) {
            authApi.getMe()
        }.map { it.toDomain() }
    }

    override suspend fun updateMe(
        name: String?,
        goal: String?,
        level: String?,
        dailyNewWords: Int?
    ): AppResult<User> {
        return safeApiCall(moshi) {
            authApi.updateMe(
                UpdateUserRequestDto(
                    name = name,
                    goal = goal,
                    level = level,
                    dailyNewWords = dailyNewWords
                )
            )
        }.map { it.toDomain() }
    }

    private suspend fun saveSessionResult(
        call: suspend () -> com.example.minlishapp_learnenglish.data.remote.dto.AuthResponseDto
    ): AppResult<AuthSession> {
        return safeApiCall(moshi, call).map { response ->
            response.toDomain().also { session ->
                tokenStorage.saveTokens(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken
                )
            }
        }
    }
}
