package com.goalwall.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val DAILY_REMINDER_WORK = "daily_reminder"
private const val REMINDER_HOUR_OF_DAY = 9

fun Context.scheduleDailyReminder() {
    val request =
        PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayUntilNextNineAm(), TimeUnit.MILLISECONDS)
            .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        DAILY_REMINDER_WORK,
        ExistingPeriodicWorkPolicy.KEEP,
        request,
    )
}

private fun calculateDelayUntilNextNineAm(): Long {
    val now = LocalDateTime.now()
    var nextRun =
        now
            .withHour(REMINDER_HOUR_OF_DAY)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
    if (!nextRun.isAfter(now)) {
        nextRun = nextRun.plusDays(1)
    }
    return Duration.between(now, nextRun).toMillis()
}
