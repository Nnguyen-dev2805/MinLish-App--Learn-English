package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.core.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.first

class CheckSessionUseCase(
    private val tokenStorage: TokenStorage,
    private val userPreferencesStorage: UserPreferencesStorage
) {
    suspend operator fun invoke(): SessionDestination {
        val hasAccessToken = !tokenStorage.getAccessToken().isNullOrBlank()
        if (hasAccessToken) return SessionDestination.Home

        val onboardingSeen = userPreferencesStorage.isOnboardingSeen.first()
        return if (onboardingSeen) {
            SessionDestination.Login
        } else {
            SessionDestination.Onboarding
        }
    }
}

enum class SessionDestination {
    Home,
    Onboarding,
    Login
}
