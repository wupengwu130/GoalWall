package com.goalwall.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.goalwall.data.db.entity.GoalEntity

/**
 * Placeholder — queries implemented in Task 3.3 (Architecture.md §6).
 */
@Dao
interface GoalDao {
    /** Room KSP scaffold only (no @Query). Replaced/extended in Task 3.3. */
    @Insert
    suspend fun insert(entity: GoalEntity): Long
}
