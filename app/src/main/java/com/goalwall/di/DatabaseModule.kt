package com.goalwall.di

import android.content.Context
import androidx.room.Room
import com.goalwall.core.database.GoalWallDatabase
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
            "goalwall.db",
        ).build()
}
