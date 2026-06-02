package com.goalwall.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goalwall.data.repository.GoalRepository
import com.goalwall.di.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher

@HiltWorker
class WidgetRefreshWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        @Suppress("UnusedPrivateProperty") private val goalRepository: GoalRepository,
        @IoDispatcher @Suppress("UnusedPrivateProperty") private val ioDispatcher: CoroutineDispatcher,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result = Result.success()
    }
