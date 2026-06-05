package com.goalwall.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goalwall.widget.GoalWallWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetSyncWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            GoalWallWidget().updateAll(applicationContext)
            return Result.success()
        }
    }
