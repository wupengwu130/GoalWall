// Package: com.goalwall.ui.goal
// Layer: Test — ViewModel
// Responsibility: Verifies GoalDetailViewModel mutation coordination and side effects.
// Dependencies: MockK, kotlinx.coroutines.test
package com.goalwall.ui.goal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.goalwall.MainDispatcherRule
import com.goalwall.data.model.Goal
import com.goalwall.data.model.GoalDetail
import com.goalwall.data.model.Milestone
import com.goalwall.data.repository.GoalRepository
import com.goalwall.data.repository.ProgressRepository
import com.goalwall.notification.NotificationHelper
import com.goalwall.worker.enqueueWidgetSync
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GoalDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appContext = mockk<Context>(relaxed = true)
    private val goalRepository = mockk<GoalRepository>()
    private val progressRepository = mockk<ProgressRepository>()
    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic("com.goalwall.worker.WorkerExtensionsKt")
        every { appContext.enqueueWidgetSync() } just Runs
        every { progressRepository.observeProgressHistory(GOAL_ID) } returns flowOf(emptyList())
        every { appContext.getString(any()) } returns "message"
        every { appContext.getString(any(), any()) } returns "message"
    }

    @After
    fun tearDown() {
        unmockkStatic("com.goalwall.worker.WorkerExtensionsKt")
    }

    @Test
    fun incrementProgress_callsRepositoryAndTriggersWidgetSync() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = false))
            coEvery { goalRepository.incrementCurrentValue(GOAL_ID, 1, null) } returns 1
            val viewModel = createViewModel()

            viewModel.incrementProgress()

            coVerify { goalRepository.incrementCurrentValue(GOAL_ID, 1, null) }
            verify { appContext.enqueueWidgetSync() }
        }

    @Test
    fun incrementProgress_five_callsRepositoryWithDeltaFive() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = false))
            coEvery { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_FIVE, null) } returns DELTA_FIVE
            val viewModel = createViewModel()

            viewModel.incrementProgress(delta = DELTA_FIVE)

            coVerify { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_FIVE, null) }
            verify { appContext.enqueueWidgetSync() }
        }

    @Test
    fun incrementProgress_ten_callsRepositoryWithDeltaTen() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = false))
            coEvery { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_TEN, null) } returns DELTA_TEN
            val viewModel = createViewModel()

            viewModel.incrementProgress(delta = DELTA_TEN)

            coVerify { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_TEN, null) }
            verify { appContext.enqueueWidgetSync() }
        }

    @Test
    fun incrementProgress_custom_callsRepositoryWithCustomDelta() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = false))
            coEvery { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_CUSTOM, null) } returns DELTA_CUSTOM
            val viewModel = createViewModel()

            viewModel.incrementProgress(delta = DELTA_CUSTOM)

            coVerify { goalRepository.incrementCurrentValue(GOAL_ID, DELTA_CUSTOM, null) }
            verify { appContext.enqueueWidgetSync() }
        }

    @Test
    fun toggleMilestone_falseToTrue_sendsMilestoneNotification() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = false))
            coEvery { goalRepository.toggleMilestone(MILESTONE_ID, true) } returns Unit
            val viewModel = createViewModel()

            viewModel.toggleMilestone(MILESTONE_ID, true)

            coVerify { goalRepository.toggleMilestone(MILESTONE_ID, true) }
            verify {
                notificationHelper.showMilestoneAchieved(
                    goalTitle = "Read",
                    milestoneTitle = "Chapter 1",
                )
            }
        }

    @Test
    fun toggleMilestone_trueToFalse_doesNotSendMilestoneNotification() =
        runTest {
            every { goalRepository.observeGoalDetail(GOAL_ID) } returns MutableStateFlow(goalDetail(completed = true))
            coEvery { goalRepository.toggleMilestone(MILESTONE_ID, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.toggleMilestone(MILESTONE_ID, false)

            coVerify { goalRepository.toggleMilestone(MILESTONE_ID, false) }
            verify(exactly = 0) {
                notificationHelper.showMilestoneAchieved(any(), any())
            }
        }

    private fun createViewModel(): GoalDetailViewModel =
        GoalDetailViewModel(
            appContext = appContext,
            goalRepository = goalRepository,
            progressRepository = progressRepository,
            notificationHelper = notificationHelper,
            savedStateHandle = SavedStateHandle(mapOf("goalId" to GOAL_ID)),
        )

    private fun goalDetail(completed: Boolean): GoalDetail =
        GoalDetail(
            goal =
                Goal(
                    id = GOAL_ID,
                    title = "Read",
                    targetValue = 10,
                    currentValue = 1,
                    unit = "pages",
                    startDate = 1L,
                ),
            milestones =
                listOf(
                    Milestone(
                        id = MILESTONE_ID,
                        goalId = GOAL_ID,
                        title = "Chapter 1",
                        targetValue = 1,
                        completed = completed,
                    ),
                ),
        )

    private companion object {
        const val GOAL_ID = 1L
        const val MILESTONE_ID = 10L
        const val DELTA_FIVE = 5
        const val DELTA_TEN = 10
        const val DELTA_CUSTOM = 23
    }
}
