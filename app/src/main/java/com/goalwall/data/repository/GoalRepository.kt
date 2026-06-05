// Package: com.goalwall.data.repository
// Layer: Data — Repository
// Responsibility: Aggregates goal data, exposes Flow to ViewModel, maps Entity to Model.
// Dependencies: GoalDao, MilestoneDao, data.model.*
// Forbidden imports: ui.**, worker.**, kotlinx.coroutines.Dispatchers
package com.goalwall.data.repository

import androidx.room.withTransaction
import com.goalwall.data.db.GoalWallDatabase
import com.goalwall.data.db.dao.GoalDao
import com.goalwall.data.db.dao.MilestoneDao
import com.goalwall.data.db.dao.ProgressDao
import com.goalwall.data.db.entity.GoalEntity
import com.goalwall.data.db.entity.MilestoneEntity
import com.goalwall.data.db.entity.ProgressEntity
import com.goalwall.data.model.Goal
import com.goalwall.data.model.GoalDetail
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.model.toDetail
import com.goalwall.data.model.toEntity
import com.goalwall.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository
    @Inject
    constructor(
        private val database: GoalWallDatabase,
        private val goalDao: GoalDao,
        private val milestoneDao: MilestoneDao,
        private val progressDao: ProgressDao,
    ) {
        val goals: Flow<List<Goal>> =
            goalDao.observeAll().map { list -> list.map { it.toModel() } }

        val allGoals: Flow<List<Goal>> =
            goalDao.observeAllIncludingArchived()
                .map { list -> list.map { it.toModel() } }

        fun observeGoalDetail(goalId: Long): Flow<GoalDetail?> =
            goalDao.observeWithMilestones(goalId).map { it?.toDetail() }

        suspend fun addGoal(
            title: String,
            targetValue: Int,
            unit: String,
            startDate: Long,
            targetDate: Long? = null,
            description: String? = null,
            color: String = "#4F8EF7",
        ): Long =
            goalDao.insert(
                GoalEntity(
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    currentValue = 0,
                    unit = unit,
                    startDate = startDate,
                    targetDate = targetDate,
                    status = GoalStatus.ACTIVE,
                    color = color,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )

        suspend fun updateGoal(goal: Goal) = goalDao.update(goal.toEntity())

        suspend fun deleteGoal(goalId: Long) = goalDao.deleteById(goalId)

        suspend fun updateCurrentValue(
            goalId: Long,
            currentValue: Int,
        ) {
            goalDao.updateCurrentValue(goalId, currentValue)
        }

        suspend fun incrementCurrentValue(
            goalId: Long,
            delta: Int,
            note: String? = null,
        ): Int {
            if (delta == 0) return 0

            return database.withTransaction {
                val goal = goalDao.getById(goalId) ?: return@withTransaction 0
                val boundedNewValue = (goal.currentValue + delta).coerceIn(0, goal.targetValue)
                val appliedDelta = boundedNewValue - goal.currentValue
                if (appliedDelta != 0) {
                    val now = System.currentTimeMillis()
                    goalDao.updateCurrentValue(
                        id = goalId,
                        currentValue = boundedNewValue,
                        now = now,
                    )
                    progressDao.insert(
                        ProgressEntity(
                            goalId = goalId,
                            value = appliedDelta,
                            note = note,
                            recordDate = now,
                            createdAt = now,
                        ),
                    )
                }
                appliedDelta
            }
        }

        suspend fun countGoalsByStatus(status: GoalStatus): Int = goalDao.countByStatus(status)

        suspend fun setStatus(
            goalId: Long,
            status: GoalStatus,
        ) {
            goalDao.updateStatus(goalId, status)
        }

        suspend fun addMilestone(
            goalId: Long,
            title: String,
            targetValue: Int,
        ): Long =
            milestoneDao.insert(
                MilestoneEntity(
                    goalId = goalId,
                    title = title,
                    targetValue = targetValue,
                ),
            )

        suspend fun toggleMilestone(
            milestoneId: Long,
            completed: Boolean,
        ) = milestoneDao.setCompleted(milestoneId, completed)
    }
