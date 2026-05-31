package com.example.minlishapp_learnenglish.core

import android.content.Context
import com.example.minlishapp_learnenglish.core.notification.ReminderScheduler
import com.example.minlishapp_learnenglish.core.notification.WorkManagerReminderScheduler
import com.example.minlishapp_learnenglish.core.network.RetrofitFactory
import com.example.minlishapp_learnenglish.core.storage.EncryptedTokenStorage
import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.core.storage.UserPreferencesStorage
import com.example.minlishapp_learnenglish.data.remote.api.AnalyticsApi
import com.example.minlishapp_learnenglish.data.remote.api.AuthApi
import com.example.minlishapp_learnenglish.data.remote.api.DeckApi
import com.example.minlishapp_learnenglish.data.remote.api.LearningApi
import com.example.minlishapp_learnenglish.data.remote.api.NotificationApi
import com.example.minlishapp_learnenglish.data.repository.AnalyticsRepository
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.data.repository.DefaultAnalyticsRepository
import com.example.minlishapp_learnenglish.data.repository.DefaultAuthRepository
import com.example.minlishapp_learnenglish.data.repository.DefaultDeckRepository
import com.example.minlishapp_learnenglish.data.repository.DefaultLearningRepository
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import com.example.minlishapp_learnenglish.data.repository.LearningRepository
import com.example.minlishapp_learnenglish.data.repository.DefaultNotificationRepository
import com.example.minlishapp_learnenglish.data.repository.NotificationRepository
import com.example.minlishapp_learnenglish.domain.usecase.auth.CheckSessionUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.LoginUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.RegisterUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.SetOnboardingSeenUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.CreateDeckUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.CreateWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.DeleteWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDeckDetailUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDeckItemsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDecksUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.UpdateWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.home.LoadHomeUseCase
import com.example.minlishapp_learnenglish.domain.usecase.learning.GetReviewCardsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.learning.SubmitReviewUseCase
import com.example.minlishapp_learnenglish.domain.usecase.notification.GetNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.notification.UpdateNotificationSettingsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.GetProfileUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.LogoutUseCase
import com.example.minlishapp_learnenglish.domain.usecase.profile.UpdateProfileUseCase
import com.example.minlishapp_learnenglish.domain.usecase.progress.LoadProgressAnalyticsUseCase
import com.squareup.moshi.Moshi

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val tokenStorage: TokenStorage = EncryptedTokenStorage(appContext)
    val userPreferencesStorage = UserPreferencesStorage(appContext)

    val moshi: Moshi = RetrofitFactory.createMoshi()

    private val publicAuthApi: AuthApi = RetrofitFactory
        .createPublicRetrofit(moshi = moshi)
        .create(AuthApi::class.java)

    private val authenticatedRetrofit = RetrofitFactory.createAuthenticatedRetrofit(
            tokenStorage = tokenStorage,
            refreshApi = publicAuthApi,
            moshi = moshi
        )

    private val authenticatedAuthApi: AuthApi = authenticatedRetrofit
        .create(AuthApi::class.java)
    private val learningApi: LearningApi = authenticatedRetrofit
        .create(LearningApi::class.java)
    private val analyticsApi: AnalyticsApi = authenticatedRetrofit
        .create(AnalyticsApi::class.java)
    private val deckApi: DeckApi = authenticatedRetrofit
        .create(DeckApi::class.java)
    private val notificationApi: NotificationApi = authenticatedRetrofit
        .create(NotificationApi::class.java)

    val authRepository: AuthRepository = DefaultAuthRepository(
        authApi = authenticatedAuthApi,
        tokenStorage = tokenStorage,
        moshi = moshi
    )
    val learningRepository: LearningRepository = DefaultLearningRepository(
        learningApi = learningApi,
        moshi = moshi
    )
    val analyticsRepository: AnalyticsRepository = DefaultAnalyticsRepository(
        analyticsApi = analyticsApi,
        moshi = moshi
    )
    val deckRepository: DeckRepository = DefaultDeckRepository(
        deckApi = deckApi,
        moshi = moshi
    )
    val notificationRepository: NotificationRepository = DefaultNotificationRepository(
        notificationApi = notificationApi,
        moshi = moshi
    )
    val reminderScheduler: ReminderScheduler = WorkManagerReminderScheduler(appContext)

    val checkSessionUseCase = CheckSessionUseCase(
        tokenStorage = tokenStorage,
        userPreferencesStorage = userPreferencesStorage
    )
    val setOnboardingSeenUseCase = SetOnboardingSeenUseCase(userPreferencesStorage)
    val loginUseCase = LoginUseCase(authRepository)
    val googleLoginUseCase = com.example.minlishapp_learnenglish.domain.usecase.auth.GoogleLoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val loadHomeUseCase = LoadHomeUseCase(
        authRepository = authRepository,
        analyticsRepository = analyticsRepository,
        learningRepository = learningRepository
    )
    val getDecksUseCase = GetDecksUseCase(deckRepository)
    val getDeckDetailUseCase = GetDeckDetailUseCase(deckRepository)
    val getDeckItemsUseCase = GetDeckItemsUseCase(deckRepository)
    val importDeckItemsUseCase = com.example.minlishapp_learnenglish.domain.usecase.decks.ImportDeckItemsUseCase(deckRepository)
    val createDeckUseCase = CreateDeckUseCase(deckRepository)
    val createWordUseCase = CreateWordUseCase(deckRepository)
    val updateWordUseCase = UpdateWordUseCase(deckRepository)
    val deleteWordUseCase = DeleteWordUseCase(deckRepository)
    val getReviewCardsUseCase = GetReviewCardsUseCase(learningRepository)
    val submitReviewUseCase = SubmitReviewUseCase(learningRepository)
    val loadProgressAnalyticsUseCase = LoadProgressAnalyticsUseCase(analyticsRepository)
    val getProfileUseCase = GetProfileUseCase(authRepository)
    val updateProfileUseCase = UpdateProfileUseCase(authRepository)
    val logoutUseCase = LogoutUseCase(
        authRepository = authRepository,
        tokenStorage = tokenStorage
    )
    val getNotificationSettingsUseCase = GetNotificationSettingsUseCase(notificationRepository)
    val updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(notificationRepository)
}
