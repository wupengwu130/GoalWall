// Package: com.goalwall.data.db
// Layer: Data — Room Database
// Responsibility: Registers Room entities, DAOs, and type converters.
// Dependencies: Room, GoalStatusConverter, entity/dao packages
// Forbidden imports: ui.**, worker.**
package com.goalwall.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.goalwall.data.db.converter.GoalStatusConverter
import com.goalwall.data.db.dao.GoalDao
import com.goalwall.data.db.dao.MilestoneDao
import com.goalwall.data.db.dao.ProgressDao
import com.goalwall.data.db.entity.GoalEntity
import com.goalwall.data.db.entity.MilestoneEntity
import com.goalwall.data.db.entity.ProgressEntity

@TypeConverters(GoalStatusConverter::class)
@Database(
    entities = [
        GoalEntity::class,
        MilestoneEntity::class,
        ProgressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class GoalWallDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    abstract fun milestoneDao(): MilestoneDao

    abstract fun progressDao(): ProgressDao
}
