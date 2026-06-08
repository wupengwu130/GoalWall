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
    fun incrementCurrentValue_belowZero_clampsAndReturnsAppliedDelta() =
        runTest {
            val goalId = insertGoal(targetValue = 3)
            repository.incrementCurrentValue(goalId, delta = 1)

            val appliedDelta = repository.incrementCurrentValue(goalId, delta = -5)

            assertGoalCurrentValue(goalId, expected = 0)
            assertProgressValues(goalId, listOf(1, -1))
            assertEquals(-1, appliedDelta)
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
