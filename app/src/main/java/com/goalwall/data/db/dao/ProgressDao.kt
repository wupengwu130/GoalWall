package com.goalwall.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.goalwall.data.db.entity.ProgressEntity

/**
 * Placeholder — queries implemented in Task 3.4 (Architecture.md §5).
 */
@Dao
interface ProgressDao {
    /** Room KSP scaffold only (no @Query). Replaced/extended in Task 3.4. */
    @Insert
    suspend fun insert(entity: ProgressEntity): Long
}
