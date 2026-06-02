package com.goalwall.di

import com.goalwall.data.repository.GoalRepository
import com.goalwall.data.repository.ProgressRepository
import com.goalwall.data.repository.UserPreferencesRepository
import com.goalwall.data.repository.impl.GoalRepositoryImpl
import com.goalwall.data.repository.impl.ProgressRepositoryImpl
import com.goalwall.data.repository.impl.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
