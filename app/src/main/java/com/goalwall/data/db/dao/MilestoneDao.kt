// Package: com.goalwall.data.db.dao
// Layer: Data — Room DAO
// Responsibility: Exposes milestone queries and mutations for Room.
// Dependencies: Room, MilestoneEntity
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goalwall.data.db.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY createdAt ASC")
    fun observeByGoal(goalId: Long): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MilestoneEntity): Long

    @Query("UPDATE milestones SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(
        id: Long,
        completed: Boolean,
    )

    @Query("DELETE FROM milestones WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: Long)
}
