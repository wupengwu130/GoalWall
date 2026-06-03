// Package: com.goalwall.data.model
// Layer: Data — UI Model
// Responsibility: Represents a milestone without Room annotations for UI consumption.
// Dependencies: None
// Forbidden imports: androidx.room.**, ui.**, worker.**
package com.goalwall.data.model

data class Milestone(
    val id: Long = 0,
    val goalId: Long,
    val title: String,
    val targetValue: Int,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
