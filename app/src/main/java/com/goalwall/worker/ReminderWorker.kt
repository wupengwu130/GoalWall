package com.goalwall.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goalwall.data.UserPreferences
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.repository.GoalRepository
import com.goalwall.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted private val appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val goalRepository: GoalRepository,
        private val notificationHelper: NotificationHelper,
        private val userPreferences: UserPreferences,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            if (userPreferences.reminderEnabled.first()) {
                val activeCount = goalRepository.countGoalsByStatus(GoalStatus.ACTIVE)

                if (activeCount > 0) {
                    notificationHelper.showDailyReminder(activeCount)
                }
            }
            return Result.success()
        }
    }
