package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.core.storage.TokenStorage

class CheckSessionUseCase(
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(): SessionDestination {
        val hasAccessToken = !tokenStorage.getAccessToken().isNullOrBlank()
        return if (hasAccessToken) {
            SessionDestination.Home
        } else {
            SessionDestination.Login
        }
    }
}

enum class SessionDestination {
    Home,
    Login
}
