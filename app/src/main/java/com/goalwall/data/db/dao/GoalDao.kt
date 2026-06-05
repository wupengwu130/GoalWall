// Package: com.goalwall.data.db.dao
// Layer: Data — Room DAO
// Responsibility: Exposes goal queries and mutations for Room.
// Dependencies: Room, GoalEntity, GoalStatus
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.goalwall.data.db.entity.GoalEntity
import com.goalwall.data.model.GoalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE status != 'ARCHIVED' ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY updatedAt DESC")
    fun observeAllIncludingArchived(): Flow<List<GoalEntity>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun observeWithMilestones(goalId: Long): Flow<GoalWithMilestones?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getById(goalId: Long): GoalEntity?

    @Query("SELECT COUNT(*) FROM goals WHERE status = :status")
    suspend fun countByStatus(status: GoalStatus): Int

    @Query(
        """
        SELECT * FROM goals
        WHERE status = 'ACTIVE'
        ORDER BY (CAST(currentValue AS REAL) / CASE WHEN targetValue = 0 THEN 1 ELSE targetValue END) DESC
        LIMIT :limit
        """,
    )
    suspend fun getTopByProgress(limit: Int): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoalEntity): Long

    @Update
    suspend fun update(entity: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE goals SET currentValue = :currentValue, updatedAt = :now WHERE id = :id")
    suspend fun updateCurrentValue(
        id: Long,
        currentValue: Int,
        now: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE goals SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: GoalStatus,
        now: Long = System.currentTimeMillis(),
    )
}
