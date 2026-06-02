package com.example.minlishapp_learnenglish.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.minlishapp_learnenglish.MainActivity
import com.example.minlishapp_learnenglish.R
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val dailyTime = inputData.getString(KEY_DAILY_TIME) ?: DEFAULT_DAILY_TIME
        val timezone = inputData.getString(KEY_TIMEZONE) ?: DEFAULT_TIMEZONE

        if (canPostNotifications()) {
            showNotification()
        }

        scheduleNextReminder(dailyTime, timezone)
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(notificationManager)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(applicationContext, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(applicationContext)
        }

        val notification = builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MinLish reminder")
            .setContentText("A few words today keeps your vocabulary moving.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun scheduleNextReminder(dailyTime: String, timezone: String) {
        val request = buildRequest(
            dailyTime = dailyTime,
            timezone = timezone,
            forceTomorrow = true
        )
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val WORK_NAME = "minlish_daily_study_reminder"
        const val CHANNEL_ID = "minlish_daily_reminder"
        private const val KEY_DAILY_TIME = "daily_time"
        private const val KEY_TIMEZONE = "timezone"
        private const val DEFAULT_DAILY_TIME = "20:00"
        private const val DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh"
        private const val DEFAULT_HOUR = 20
        private const val DEFAULT_MINUTE = 0
        private const val CHANNEL_NAME = "Daily study reminders"
        private const val NOTIFICATION_ID = 2805

        fun buildRequest(
            dailyTime: String,
            timezone: String,
            forceTomorrow: Boolean
        ): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(
                    calculateDelayMillis(
                        dailyTime = dailyTime,
                        timezone = timezone,
                        forceTomorrow = forceTomorrow
                    ),
                    TimeUnit.MILLISECONDS
                )
                .setInputData(
                    workDataOf(
                        KEY_DAILY_TIME to dailyTime,
                        KEY_TIMEZONE to timezone
                    )
                )
                .build()
        }

        private fun calculateDelayMillis(
            dailyTime: String,
            timezone: String,
            forceTomorrow: Boolean
        ): Long {
            val parts = dailyTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: DEFAULT_HOUR
            val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: DEFAULT_MINUTE
            val timeZone = TimeZone.getTimeZone(timezone.ifBlank { DEFAULT_TIMEZONE })

            val now = Calendar.getInstance(timeZone)
            val target = Calendar.getInstance(timeZone).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (forceTomorrow || !after(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
        }
    }
}
