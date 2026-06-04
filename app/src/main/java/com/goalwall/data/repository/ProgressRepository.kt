// Package: com.goalwall.data.repository
// Layer: Data — Repository
// Responsibility: Aggregates progress history, exposes Flow to ViewModel, maps Entity to Model.
// Dependencies: ProgressDao, data.model.*
// Forbidden imports: ui.**, worker.**, kotlinx.coroutines.Dispatchers
package com.goalwall.data.repository

import com.goalwall.data.db.dao.ProgressDao
import com.goalwall.data.db.entity.ProgressEntity
import com.goalwall.data.model.ProgressRecord
import com.goalwall.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository
    @Inject
    constructor(
        private val progressDao: ProgressDao,
    ) {
        fun observeProgressHistory(goalId: Long): Flow<List<ProgressRecord>> =
            progressDao.observeByGoal(goalId).map { list -> list.map { it.toModel() } }

        suspend fun recordProgress(
            goalId: Long,
            value: Int,
            note: String? = null,
        ) {
            progressDao.insert(
                ProgressEntity(
                    goalId = goalId,
                    value = value,
                    note = note,
                    recordDate = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
