package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.data.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) = authRepository.login(email, password)
}
