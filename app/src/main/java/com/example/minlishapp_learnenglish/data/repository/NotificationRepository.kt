package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.local.dao.NotificationSettingsDao
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.database.DatabaseSeeder
import com.example.minlishapp_learnenglish.data.local.database.MinLishDatabase
import com.example.minlishapp_learnenglish.data.local.entity.NotificationSettingsEntity
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import kotlinx.coroutines.CancellationException

interface NotificationRepository {
    suspend fun getPreferences(): AppResult<NotificationSettings>

    suspend fun updatePreferences(
        dailyTime: String? = null,
        timezone: String? = null,
        emailEnabled: Boolean? = null,
        pushEnabled: Boolean? = null
    ): AppResult<NotificationSettings>
}

class DefaultNotificationRepository(
    private val database: MinLishDatabase,
    private val notificationSettingsDao: NotificationSettingsDao,
    private val userDao: UserDao
) : NotificationRepository {
    override suspend fun getPreferences(): AppResult<NotificationSettings> {
        return localCall {
            val userId = userDao.requireUserId()
            DatabaseSeeder.seedUserIfNeeded(database, userId)
            val settings = loadOrCreateSettings(userId)
            settings.toNotificationSettings()
        }
    }

    override suspend fun updatePreferences(
        dailyTime: String?,
        timezone: String?,
        emailEnabled: Boolean?,
        pushEnabled: Boolean?
    ): AppResult<NotificationSettings> {
        return localCall {
            val userId = userDao.requireUserId()
            DatabaseSeeder.seedUserIfNeeded(database, userId)
            val currentSettings = loadOrCreateSettings(userId)
            val updatedSettings = currentSettings.copy(
                dailyTime = dailyTime ?: currentSettings.dailyTime,
                timezone = timezone ?: currentSettings.timezone,
                emailEnabled = emailEnabled ?: currentSettings.emailEnabled,
                pushEnabled = pushEnabled ?: currentSettings.pushEnabled
            )
            notificationSettingsDao.upsertSettings(updatedSettings)
            updatedSettings.toNotificationSettings()
        }
    }

    private suspend fun loadOrCreateSettings(userId: Long): NotificationSettingsEntity {
        val existing = notificationSettingsDao.getSettings(userId)
        if (existing != null) return existing

        val created = NotificationSettingsEntity(userId = userId)
        notificationSettingsDao.upsertSettings(created)
        return created
    }

    private fun NotificationSettingsEntity.toNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            dailyTime = dailyTime,
            timezone = timezone,
            emailEnabled = emailEnabled,
            pushEnabled = pushEnabled
        )
    }

    private suspend fun <T> localCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: LocalAuthRequiredException) {
            AppResult.Failure(
                AppError.Validation(message = error.message ?: "Please log in first.")
            )
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(
                AppError.Validation(message = error.message ?: "Invalid input.")
            )
        } catch (error: Exception) {
            AppResult.Failure(
                AppError.Unknown(
                    message = error.message ?: "Local database error."
                )
            )
        }
    }
}
