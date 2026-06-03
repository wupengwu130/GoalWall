// Package: com.goalwall.data.model
// Layer: Data — UI Model
// Responsibility: Represents a progress history record without Room annotations.
// Dependencies: None
// Forbidden imports: androidx.room.**, ui.**, worker.**
package com.goalwall.data.model

data class ProgressRecord(
    val id: Long = 0,
    val goalId: Long,
    val value: Int,
    val note: String? = null,
    val recordDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)
