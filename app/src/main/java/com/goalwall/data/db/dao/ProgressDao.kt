// Package: com.goalwall.data.db.dao
// Layer: Data — Room DAO
// Responsibility: Exposes progress record queries and mutations for Room.
// Dependencies: Room, ProgressEntity
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goalwall.data.db.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_records WHERE goalId = :goalId ORDER BY recordDate DESC")
    fun observeByGoal(goalId: Long): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProgressEntity): Long

    @Query("SELECT COALESCE(SUM(value), 0) FROM progress_records WHERE goalId = :goalId")
    suspend fun sumValueByGoal(goalId: Long): Int
}
