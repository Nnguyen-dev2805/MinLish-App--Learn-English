package com.example.minlishapp_learnenglish.domain.usecase.profile

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.domain.model.User

class GetProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<User> {
        return authRepository.getMe()
    }
}

class UpdateProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        goal: String?,
        level: String?,
        dailyNewWords: Int
    ): AppResult<User> {
        return authRepository.updateMe(
            name = name,
            goal = goal,
            level = level,
            dailyNewWords = dailyNewWords
        )
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            tokenStorage.clearTokens()
            return AppResult.Success(Unit)
        }

        val result = authRepository.logout(refreshToken)
        tokenStorage.clearTokens()
        return result
    }
}
