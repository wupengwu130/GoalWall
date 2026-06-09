package com.goalwall.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.goalwall.data.UserPreferences
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val DAILY_REMINDER_WORK = "daily_reminder"

fun Context.scheduleDailyReminder(
    hour: Int = UserPreferences.DEFAULT_REMINDER_HOUR,
    minute: Int = UserPreferences.DEFAULT_REMINDER_MINUTE,
) {
    val request =
        PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(
                calculateDelayUntilNextReminder(hour = hour, minute = minute),
                TimeUnit.MILLISECONDS,
            )
            .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        DAILY_REMINDER_WORK,
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
    )
}

internal fun calculateDelayUntilNextReminder(
    hour: Int,
    minute: Int,
): Long {
    val now = LocalDateTime.now()
    var nextRun =
        now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
    if (!nextRun.isAfter(now)) {
        nextRun = nextRun.plusDays(1)
    }
    return Duration.between(now, nextRun).toMillis()
}
