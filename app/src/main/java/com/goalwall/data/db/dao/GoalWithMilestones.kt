// Package: com.goalwall.data.db.dao
// Layer: Data — Room relation
// Responsibility: Groups a goal entity with its milestone children.
// Dependencies: Room, GoalEntity, MilestoneEntity
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.goalwall.data.db.entity.GoalEntity
import com.goalwall.data.db.entity.MilestoneEntity

data class GoalWithMilestones(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId",
    )
    val milestones: List<MilestoneEntity>,
)
