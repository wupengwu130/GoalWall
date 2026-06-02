package com.goalwall.di

import com.goalwall.common.GoalWallDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Singleton
    fun provideDispatchers(): GoalWallDispatchers = GoalWallDispatchers()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(dispatchers: GoalWallDispatchers): CoroutineDispatcher = dispatchers.io
}
