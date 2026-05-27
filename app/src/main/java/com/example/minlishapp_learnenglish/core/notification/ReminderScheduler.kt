package com.example.minlishapp_learnenglish.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

interface ReminderScheduler {
    fun schedule(settings: NotificationSettings)
    fun cancel()
}

class WorkManagerReminderScheduler(
    context: Context
) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule(settings: NotificationSettings) {
        if (!settings.pushEnabled) {
            cancel()
            return
        }

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelayMillis(settings), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun calculateInitialDelayMillis(settings: NotificationSettings): Long {
        val parts = settings.dailyTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: DEFAULT_HOUR
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: DEFAULT_MINUTE
        val timeZone = TimeZone.getTimeZone(settings.timezone.ifBlank { DEFAULT_TIMEZONE })

        val now = Calendar.getInstance(timeZone)
        val target = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    companion object {
        private const val WORK_NAME = "minlish_daily_study_reminder"
        private const val DEFAULT_HOUR = 20
        private const val DEFAULT_MINUTE = 0
        private const val DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh"
    }
}
