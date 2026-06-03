// Package: com.goalwall.data.db.converter
// Layer: Data — Room TypeConverter
// Responsibility: Persists GoalStatus enum as String in Room tables.
// Dependencies: Room, GoalStatus
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db.converter

import androidx.room.TypeConverter
import com.goalwall.data.model.GoalStatus

class GoalStatusConverter {
    @TypeConverter
    fun fromGoalStatus(status: GoalStatus): String = status.name

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus = GoalStatus.valueOf(value)
}
