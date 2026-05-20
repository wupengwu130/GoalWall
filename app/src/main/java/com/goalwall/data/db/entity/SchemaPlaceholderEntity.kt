package com.goalwall.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Internal placeholder so Room can initialize before domain entities are added.
 * Remove when real entities (Goal, Milestone, etc.) are introduced.
 */
@Entity(tableName = "schema_placeholder")
internal data class SchemaPlaceholderEntity(
    @PrimaryKey val id: Int = 0,
)
