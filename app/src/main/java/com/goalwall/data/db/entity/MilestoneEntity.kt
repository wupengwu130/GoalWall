// Package: com.goalwall.data.db.entity
// Layer: Data — Room Entity
// Responsibility: Persists milestone records linked to goals.
// Dependencies: Room, GoalEntity
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId")],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val targetValue: Int,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
