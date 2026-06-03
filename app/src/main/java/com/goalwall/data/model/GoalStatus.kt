// Package: com.goalwall.data.model
// Layer: Data — UI Model
// Responsibility: Defines goal lifecycle status values shared by Entity and Model.
// Dependencies: None
// Forbidden imports: androidx.room.**, ui.**, worker.**
package com.goalwall.data.model

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED,
    ARCHIVED,
}
