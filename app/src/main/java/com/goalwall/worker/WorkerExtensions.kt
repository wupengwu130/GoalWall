package com.goalwall.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

private const val UNIQUE_WIDGET_SYNC_WORK = "unique_widget_sync"

fun Context.enqueueWidgetSync() {
    val request = OneTimeWorkRequestBuilder<WidgetSyncWorker>().build()
    WorkManager.getInstance(this).enqueueUniqueWork(
        UNIQUE_WIDGET_SYNC_WORK,
        ExistingWorkPolicy.REPLACE,
        request,
    )
}
