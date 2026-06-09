// Package: com.goalwall.data.repository
// Layer: Test — Repository
// Responsibility: Verifies atomic progress updates against an in-memory Room database.
// Dependencies: Room, Robolectric, kotlinx.coroutines.test
package com.goalwall.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.goalwall.data.db.GoalWallDatabase
import com.goalwall.data.db.entity.ProgressEntity
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.model.SetStatusResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GoalRepositoryTest {
    private companion object {
        const val DELTA_FIVE = 5
        const val DELTA_TEN = 10
        const val DELTA_CUSTOM = 23
        const val TARGET_PARTIAL_CLAMP = 95
    }

    private lateinit var database: GoalWallDatabase
    private lateinit var repository: GoalRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, GoalWallDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            GoalRepository(
                database = database,
                goalDao = database.goalDao(),
                milestoneDao = database.milestoneDao(),
                progressDao = database.progressDao(),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun incrementCurrentValue_plusOne_updatesGoalAndInsertsProgress() =
        runTest {
            val goalId = insertGoal(targetValue = 3)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = 1)

            assertGoalCurrentValue(goalId, expected = 1)
            assertProgressValues(goalId, listOf(1))
            assertEquals(1, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_minusOne_updatesGoalAndInsertsNegativeProgress() =
        runTest {
            val goalId = insertGoal(targetValue = 3)
            repository.incrementCurrentValue(goalId, delta = 2)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = -1)

            assertGoalCurrentValue(goalId, expected = 1)
            assertProgressValues(goalId, listOf(2, -1))
            assertEquals(-1, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_plusFive_updatesGoalAndInsertsProgress() =
        runTest {
            val goalId = insertGoal(targetValue = 20)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = DELTA_FIVE)

            assertGoalCurrentValue(goalId, expected = DELTA_FIVE)
            assertProgressValues(goalId, listOf(DELTA_FIVE))
            assertEquals(DELTA_FIVE, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_plusTen_updatesGoalAndInsertsProgress() =
        runTest {
            val goalId = insertGoal(targetValue = 50)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = DELTA_TEN)

            assertGoalCurrentValue(goalId, expected = DELTA_TEN)
            assertProgressValues(goalId, listOf(DELTA_TEN))
            assertEquals(DELTA_TEN, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_customDelta_updatesGoalAndInsertsProgress() =
        runTest {
            val goalId = insertGoal(targetValue = 100)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = DELTA_CUSTOM)

            assertGoalCurrentValue(goalId, expected = DELTA_CUSTOM)
            assertProgressValues(goalId, listOf(DELTA_CUSTOM))
            assertEquals(DELTA_CUSTOM, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_aboveTarget_clampsAndReturnsAppliedDelta() =
        runTest {
            val goalId = insertGoal(targetValue = 2)
            repository.incrementCurrentValue(goalId, delta = 1)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = 5)

            assertGoalCurrentValue(goalId, expected = 2)
            assertProgressValues(goalId, listOf(1, 1))
            assertEquals(1, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_partialClamp_recordsActualAppliedDelta() =
        runTest {
            val goalId = insertGoal(targetValue = 100)
            repository.incrementCurrentValue(goalId, delta = TARGET_PARTIAL_CLAMP)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = DELTA_TEN)

            assertGoalCurrentValue(goalId, expected = 100)
            assertProgressValues(goalId, listOf(TARGET_PARTIAL_CLAMP, DELTA_FIVE))
            assertEquals(DELTA_FIVE, appliedDelta)
        }

    @Test
    fun incrementCurrentValue_belowZero_clampsAndReturnsAppliedDelta() =
        runTest {
            val goalId = insertGoal(targetValue = 3)
            repository.incrementCurrentValue(goalId, delta = 1)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = -5)

            assertGoalCurrentValue(goalId, expected = 0)
            assertProgressValues(goalId, listOf(1, -1))
            assertEquals(-1, appliedDelta)
        }

    @Test
    fun setStatus_archiveWithoutProgress_returnsArchiveNotAllowed() =
        runTest {
            val goalId = insertGoal(targetValue = 10)

            val result = repository.setStatus(goalId, GoalStatus.ARCHIVED)

            assertEquals(SetStatusResult.ARCHIVE_NOT_ALLOWED, result)
            assertEquals(GoalStatus.ACTIVE, database.goalDao().getById(goalId)?.status)
        }

    @Test
    fun setStatus_archiveWithProgress_succeeds() =
        runTest {
            val goalId = insertGoal(targetValue = 10)
            repository.incrementCurrentValue(goalId, delta = 1)

            val result = repository.setStatus(goalId, GoalStatus.ARCHIVED)

            assertEquals(SetStatusResult.SUCCESS, result)
            assertEquals(GoalStatus.ARCHIVED, database.goalDao().getById(goalId)?.status)
        }

    private suspend fun insertGoal(targetValue: Int): Long =
        repository.addGoal(
            title = "Read",
            targetValue = targetValue,
            unit = "pages",
            startDate = 1L,
        )

    private suspend fun assertGoalCurrentValue(
        goalId: Long,
        expected: Int,
    ) {
        assertEquals(expected, database.goalDao().getById(goalId)?.currentValue)
    }

    private suspend fun assertProgressValues(
        goalId: Long,
        expected: List<Int>,
    ) {
        val records: List<ProgressEntity> = database.progressDao().observeByGoal(goalId).first()
        assertEquals(expected, records.sortedBy { it.id }.map { it.value })
    }
}
