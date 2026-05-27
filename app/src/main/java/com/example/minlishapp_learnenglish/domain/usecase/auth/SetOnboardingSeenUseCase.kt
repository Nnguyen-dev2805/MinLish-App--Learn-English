package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.core.storage.UserPreferencesStorage

class SetOnboardingSeenUseCase(
    private val userPreferencesStorage: UserPreferencesStorage
) {
    suspend operator fun invoke() {
        userPreferencesStorage.setOnboardingSeen(true)
    }
}
