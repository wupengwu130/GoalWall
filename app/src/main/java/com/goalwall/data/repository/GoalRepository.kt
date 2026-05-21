package com.goalwall.data.repository

/**
 * Placeholder — full API in Task 3.6 (Architecture.md §5).
 */
interface GoalRepository {
    suspend fun addGoal()
    suspend fun updateGoal()
    suspend fun deleteGoal(goalId: Long)
    suspend fun updateProgress(goalId: Long, progress: Float)
    suspend fun addMilestone(goalId: Long, title: String)
    suspend fun toggleMilestone(milestoneId: Long, done: Boolean)
}
