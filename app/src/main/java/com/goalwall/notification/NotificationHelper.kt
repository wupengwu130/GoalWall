// Package: com.goalwall.notification
// Layer: Notification
// Responsibility: 统一构建与展示应用通知，供 Worker 及其他模块复用
// Dependencies: NotificationChannels, NotificationCompat, MainActivity
// Forbidden imports: data.repository.**, data.db.**, ui.viewmodel.**, worker.**
package com.goalwall.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.goalwall.MainActivity
import com.goalwall.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun showReminderNotification(
            title: String,
            message: String,
        ) {
            if (!canPostNotifications()) {
                return
            }

            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannels.REMINDER)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(createMainActivityPendingIntent())
                    .build()

            NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
        }

        fun showMilestoneAchieved(
            goalTitle: String,
            milestoneTitle: String,
        ) {
            if (!canPostNotifications()) {
                return
            }

            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannels.MILESTONE)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.milestone_notification_title))
                    .setContentText(
                        context.getString(
                            R.string.milestone_notification_message,
                            goalTitle,
                            milestoneTitle,
                        ),
                    )
                    .setAutoCancel(true)
                    .setContentIntent(createMainActivityPendingIntent())
                    .build()

            NotificationManagerCompat
                .from(context)
                .notify(milestoneTitle.hashCode(), notification)
        }

        fun showDailyReminder(pendingGoalCount: Int) {
            if (!canPostNotifications()) {
                return
            }

            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannels.DAILY_SUMMARY)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(
                        context.getString(
                            R.string.reminder_notification_message,
                            pendingGoalCount,
                        ),
                    )
                    .setAutoCancel(true)
                    .setContentIntent(createMainActivityPendingIntent())
                    .build()

            NotificationManagerCompat.from(context).notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
        }

        private fun canPostNotifications(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return true
            }
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun createMainActivityPendingIntent(): PendingIntent {
            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            return PendingIntent.getActivity(
                context,
                PENDING_INTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        companion object {
            private const val REMINDER_NOTIFICATION_ID = 2001
            private const val DAILY_REMINDER_NOTIFICATION_ID = 1001
            private const val PENDING_INTENT_REQUEST_CODE = 0
        }
    }
