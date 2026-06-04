// Package: com.goalwall.widget
// Layer: Widget — Data Provider
// Responsibility: Provides one-shot goal reads for Widget via GoalDao.
// Dependencies: GoalDao, data.model.*
// Forbidden imports: ui.**, data.repository.**, kotlinx.coroutines.Dispatchers
package com.goalwall.widget

import com.goalwall.data.db.dao.GoalDao
import com.goalwall.data.model.Goal
import com.goalwall.data.model.toModel
import javax.inject.Inject

class WidgetDataProvider
    @Inject
    constructor(
        private val goalDao: GoalDao,
    ) {
        suspend fun getTopGoals(limit: Int = 3): List<Goal> = goalDao.getTopByProgress(limit).map { it.toModel() }
    }
