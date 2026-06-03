// Package: com.goalwall.data.model
// Layer: Data — UI Model
// Responsibility: Represents a goal detail aggregate for UI consumption.
// Dependencies: Goal, Milestone
// Forbidden imports: androidx.room.**, ui.**, worker.**
package com.goalwall.data.model

data class GoalDetail(
    val goal: Goal,
    val milestones: List<Milestone>,
)
