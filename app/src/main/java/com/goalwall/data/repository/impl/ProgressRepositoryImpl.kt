package com.goalwall.data.repository.impl

import com.goalwall.data.db.dao.ProgressDao
import com.goalwall.data.repository.ProgressRepository
import com.goalwall.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UnusedPrivateProperty")
@Singleton
class ProgressRepositoryImpl
    @Inject
    constructor(
        private val progressDao: ProgressDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ProgressRepository {
        override suspend fun recordProgress(
            goalId: Long,
            value: Float,
            note: String,
        ) = TODO("Not yet implemented")
    }
