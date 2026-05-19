package com.goalwall.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goalwall.core.database.internal.SchemaPlaceholderEntity

@Database(
    entities = [SchemaPlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GoalWallDatabase : RoomDatabase()
