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
    val currentValue: Int,
    val unit: String,
    val startDate: Long,
    val targetDate: Long? = null,
    val status: GoalStatus,
    val createdAt: Long,
    val updatedAt: Long,
)
