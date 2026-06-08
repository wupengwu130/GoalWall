// Package: com.goalwall.worker
// Layer: Test — Worker
// Responsibility: Verifies ReminderWorker notification decisions.
// Dependencies: WorkManager testing, MockK, Robolectric
package com.goalwall.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.goalwall.data.UserPreferences
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.repository.GoalRepository
import com.goalwall.notification.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ReminderWorkerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val goalRepository = mockk<GoalRepository>()
    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)
    private val userPreferences = mockk<UserPreferences>()

    @Test
    fun doWork_reminderDisabled_doesNotSendNotification() =
        runTest {
            every { userPreferences.reminderEnabled } returns flowOf(false)
            val worker = createWorker()

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            coVerify(exactly = 0) { goalRepository.countGoalsByStatus(any()) }
            verify(exactly = 0) { notificationHelper.showDailyReminder(any()) }
        }

    @Test
    fun doWork_activeGoalCountZero_doesNotSendNotification() =
        runTest {
            every { userPreferences.reminderEnabled } returns flowOf(true)
            coEvery { goalRepository.countGoalsByStatus(GoalStatus.ACTIVE) } returns 0
            val worker = createWorker()

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            verify(exactly = 0) { notificationHelper.showDailyReminder(any()) }
        }

    @Test
    fun doWork_activeGoalCountPositive_sendsDailyReminder() =
        runTest {
            every { userPreferences.reminderEnabled } returns flowOf(true)
            coEvery { goalRepository.countGoalsByStatus(GoalStatus.ACTIVE) } returns 3
            val worker = createWorker()

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            verify { notificationHelper.showDailyReminder(3) }
        }

    private fun createWorker(): ReminderWorker =
        TestListenableWorkerBuilder<ReminderWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker =
                        ReminderWorker(
                            appContext = appContext,
                            workerParams = workerParameters,
                            goalRepository = goalRepository,
                            notificationHelper = notificationHelper,
                            userPreferences = userPreferences,
                        )
                },
            ).build()
}
