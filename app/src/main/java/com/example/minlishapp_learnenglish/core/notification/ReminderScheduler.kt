package com.example.minlishapp_learnenglish.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.minlishapp_learnenglish.domain.model.NotificationSettings

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

        val request = DailyReminderWorker.buildRequest(
            dailyTime = settings.dailyTime,
            timezone = settings.timezone,
            forceTomorrow = false
        )

        workManager.enqueueUniqueWork(
            DailyReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(DailyReminderWorker.WORK_NAME)
    }
}
