package com.goalwall.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDER = "goalwall_reminder"
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
            "目标提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
        NotificationChannel(
            NotificationChannels.MILESTONE,
            "里程碑达成",
            NotificationManager.IMPORTANCE_HIGH,
        ),
        NotificationChannel(
            NotificationChannels.DAILY_SUMMARY,
            "每日汇总",
            NotificationManager.IMPORTANCE_LOW,
        ),
    ).forEach { manager.createNotificationChannel(it) }
}
