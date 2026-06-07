package com.example.minlishapp_learnenglish.core

import android.content.Context
import com.example.minlishapp_learnenglish.core.notification.ReminderScheduler
import com.example.minlishapp_learnenglish.core.notification.WorkManagerReminderScheduler
import com.example.minlishapp_learnenglish.data.local.database.DatabaseSeeder
import com.example.minlishapp_learnenglish.data.local.database.MinLishDatabase
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
import com.example.minlishapp_learnenglish.domain.usecase.home.LoadHomeUseCase
import com.example.minlishapp_learnenglish.domain.usecase.progress.LoadProgressAnalyticsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    /*
    Khi MainActivity khởi tạo AppContainer(applicationContext), 
    dòng appContext = context.applicationContext lấy Application Context 
    context gắn với toàn app, sống suốt vòng đời ứng dụng, không bị hủy khi đóng một màn hình.
    appContext được truyền xuống Room database và WorkManagerReminderScheduler để lưu dữ liệu local
    và lên lịch nhắc học.
    Dùng Application Context thay vì Activity Context tránh rò rỉ bộ nhớ và đảm bảo các 
    dependency này dùng được xuyên suốt app, không chỉ trong một Activity.
    */
    private val appContext = context.applicationContext

    private val database: MinLishDatabase = MinLishDatabase.getDatabase(appContext)
    private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userDao = database.userDao()
    private val deckDao = database.deckDao()
    private val wordDao = database.wordDao()
    private val reviewDao = database.reviewDao()
    private val notificationSettingsDao = database.notificationSettingsDao()

    val authRepository: AuthRepository = DefaultAuthRepository(
        userDao = userDao
    )
    val learningRepository: LearningRepository = DefaultLearningRepository(
        context = appContext,
        database = database,
        wordDao = wordDao,
        reviewDao = reviewDao,
        userDao = userDao
    )
    val analyticsRepository: AnalyticsRepository = DefaultAnalyticsRepository(
        context = appContext,
        database = database,
        reviewDao = reviewDao,
        wordDao = wordDao,
        userDao = userDao
    )
    val deckRepository: DeckRepository = DefaultDeckRepository(
        context = appContext,
        database = database,
        deckDao = deckDao,
        wordDao = wordDao,
        reviewDao = reviewDao,
        userDao = userDao
    )
    val notificationRepository: NotificationRepository = DefaultNotificationRepository(
        database = database,
        notificationSettingsDao = notificationSettingsDao,
        userDao = userDao
    )
    val reminderScheduler: ReminderScheduler = WorkManagerReminderScheduler(appContext)

    val loadHomeUseCase = LoadHomeUseCase(
        authRepository = authRepository,
        analyticsRepository = analyticsRepository,
        learningRepository = learningRepository
    )
    val loadProgressAnalyticsUseCase = LoadProgressAnalyticsUseCase(analyticsRepository)

    init {
        // Seed dữ liệu mẫu ngay khi app khởi động để màn hình Decks/Home/Learn có dữ liệu local.
        databaseScope.launch {
            DatabaseSeeder.seedCatalogIfEmpty(appContext, database)
        }
    }
}
