// Package: com.goalwall.di
// Layer: DI — Hilt Module
// Responsibility: Provides GoalWallDatabase and DAO singletons.
// Dependencies: Room, GoalWallDatabase, DAO interfaces
// Forbidden imports: ui.**
package com.goalwall.di

import android.content.Context
import androidx.room.Room
import com.goalwall.data.db.GoalWallDatabase
import com.goalwall.data.db.dao.GoalDao
import com.goalwall.data.db.dao.MilestoneDao
import com.goalwall.data.db.dao.ProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideGoalWallDatabase(
        @ApplicationContext context: Context,
    ): GoalWallDatabase =
        Room.databaseBuilder(
            context,
            GoalWallDatabase::class.java,
            "goalwall_database",
        ).build()

    @Provides
    @Singleton
    fun provideGoalDao(database: GoalWallDatabase): GoalDao = database.goalDao()

    @Provides
    @Singleton
    fun provideMilestoneDao(database: GoalWallDatabase): MilestoneDao = database.milestoneDao()

    @Provides
    @Singleton
    fun provideProgressDao(database: GoalWallDatabase): ProgressDao = database.progressDao()
}
