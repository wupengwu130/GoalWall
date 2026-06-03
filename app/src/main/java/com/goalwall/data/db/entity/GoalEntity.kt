// Package: com.goalwall.data.db.entity
// Layer: Data — Room Entity
// Responsibility: Persists goal records in the local Room database.
// Dependencies: Room, GoalStatus
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.goalwall.data.model.GoalStatus

@Entity(
    tableName = "goals",
    indices = [Index(value = ["title"])],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetValue: Int,
    val currentValue: Int = 0,
    val unit: String,
    val startDate: Long,
    val targetDate: Long? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val color: String = "#4F8EF7",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
