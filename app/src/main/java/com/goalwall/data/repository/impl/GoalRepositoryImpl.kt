package com.goalwall.data.repository.impl

import com.goalwall.data.db.dao.GoalDao
import com.goalwall.data.db.dao.MilestoneDao
import com.goalwall.data.repository.GoalRepository
import com.goalwall.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UnusedPrivateProperty")
@Singleton
class GoalRepositoryImpl
    @Inject
    constructor(
        private val goalDao: GoalDao,
        private val milestoneDao: MilestoneDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : GoalRepository {
        override suspend fun addGoal() = TODO("Not yet implemented")

        override suspend fun updateGoal() = TODO("Not yet implemented")

        override suspend fun deleteGoal(goalId: Long) = TODO("Not yet implemented")

        override suspend fun updateProgress(
            goalId: Long,
            progress: Float,
        ) = TODO("Not yet implemented")

        override suspend fun addMilestone(
            goalId: Long,
            title: String,
        ) = TODO("Not yet implemented")

        override suspend fun toggleMilestone(
            milestoneId: Long,
            done: Boolean,
        ) = TODO("Not yet implemented")
    }
