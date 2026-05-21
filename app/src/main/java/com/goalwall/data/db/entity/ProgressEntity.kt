package com.goalwall.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder — business fields added in Task 3.1 (Architecture.md §6).
 */
@Entity(tableName = "progress_records")
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
