// Package: com.goalwall.worker
// Layer: Worker — BroadcastReceiver
// Responsibility: Restores scheduled work and widget sync after device reboot.
// Dependencies: WorkerScheduler, WorkerExtensions
// Forbidden imports: data.db.**, data.repository.**, ui.**
package com.goalwall.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.scheduleDailyReminder()
            context.enqueueWidgetSync()
        }
    }
}
