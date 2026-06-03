package com.goalwall.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goalwall.data.repository.ProgressRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyResetWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        @Suppress("UnusedPrivateProperty") private val progressRepository: ProgressRepository,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result = Result.success()
    }
