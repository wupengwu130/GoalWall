package com.goalwall.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goalwall.data.db.entity.SchemaPlaceholderEntity

@Database(
    entities = [SchemaPlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GoalWallDatabase : RoomDatabase()
