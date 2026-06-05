package com.goalwall.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.goalwall.R

object NotificationChannels {
    const val REMINDER = "goalwall_reminder"
    const val REMINDER_CHANNEL_ID = REMINDER
    const val MILESTONE = "goalwall_milestone"
    const val DAILY_SUMMARY = "goalwall_daily"
}

fun Context.createNotificationChannels() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val manager = getSystemService(NotificationManager::class.java) ?: return
    listOf(
        NotificationChannel(
            NotificationChannels.REMINDER,
            getString(R.string.notification_channel_reminder),
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
        NotificationChannel(
            NotificationChannels.MILESTONE,
            getString(R.string.notification_channel_milestone),
            NotificationManager.IMPORTANCE_HIGH,
        ),
        NotificationChannel(
            NotificationChannels.DAILY_SUMMARY,
            getString(R.string.notification_channel_daily_summary),
            NotificationManager.IMPORTANCE_LOW,
        ),
    ).forEach { manager.createNotificationChannel(it) }
}
