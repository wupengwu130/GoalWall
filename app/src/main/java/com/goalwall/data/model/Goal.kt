// Package: com.goalwall.data.model
// Layer: Data — UI Model
// Responsibility: Represents a goal without Room annotations for UI consumption.
// Dependencies: GoalStatus
// Forbidden imports: androidx.room.**, ui.**, worker.**
package com.goalwall.data.model

data class Goal(
    val id: Long = 0,
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
) {
    val progress: Float
        get() =
            if (targetValue == 0) {
                0f
            } else {
                (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
            }
}

enum class GoalFilter {
    ACTIVE,
    ARCHIVED,
    ALL,
    COMPLETED,
    PAUSED,
}
