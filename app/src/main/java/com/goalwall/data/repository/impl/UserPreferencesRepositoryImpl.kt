package com.goalwall.data.repository.impl

import android.content.Context
import com.goalwall.data.repository.UserPreferencesRepository
import com.goalwall.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UnusedPrivateProperty")
@Singleton
class UserPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : UserPreferencesRepository {
        override suspend fun setReminderEnabled(enabled: Boolean) = TODO("Not yet implemented")

        override suspend fun setThemeMode(mode: String) = TODO("Not yet implemented")
    }
