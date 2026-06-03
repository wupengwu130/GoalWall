// Package: com.goalwall.data.model
// Layer: Data — Mapping
// Responsibility: Converts between Room data structures and UI models.
// Dependencies: data.db.dao, data.db.entity
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.model

import com.goalwall.data.db.dao.GoalWithMilestones
import com.goalwall.data.db.entity.GoalEntity
import com.goalwall.data.db.entity.MilestoneEntity
import com.goalwall.data.db.entity.ProgressEntity

fun GoalEntity.toModel(): Goal =
    Goal(
        id = id,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startDate = startDate,
        targetDate = targetDate,
        status = status,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun MilestoneEntity.toModel(): Milestone =
    Milestone(
        id = id,
        goalId = goalId,
        title = title,
        targetValue = targetValue,
        completed = completed,
        createdAt = createdAt,
    )

fun GoalWithMilestones.toDetail(): GoalDetail =
    GoalDetail(
        goal = goal.toModel(),
        milestones = milestones.map { it.toModel() },
    )

fun ProgressEntity.toModel(): ProgressRecord =
    ProgressRecord(
        id = id,
        goalId = goalId,
        value = value,
        note = note,
        recordDate = recordDate,
        createdAt = createdAt,
    )

fun Goal.toEntity(): GoalEntity =
    GoalEntity(
        id = id,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startDate = startDate,
        targetDate = targetDate,
        status = status,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
