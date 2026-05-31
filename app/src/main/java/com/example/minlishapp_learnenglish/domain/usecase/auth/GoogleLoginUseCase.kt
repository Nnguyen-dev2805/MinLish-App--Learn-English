package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.domain.model.AuthSession

class GoogleLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AppResult<AuthSession> {
        return authRepository.loginWithGoogle(idToken)
    }
}
