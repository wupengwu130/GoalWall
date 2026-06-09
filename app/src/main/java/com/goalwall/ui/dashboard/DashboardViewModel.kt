// Package: com.goalwall.ui.dashboard
// Layer: UI — ViewModel
// Responsibility: 订阅 goalRepository.allGoals，派生仪表盘汇总统计
// Dependencies: GoalRepository
// Forbidden imports: data.db.**, androidx.room.**, androidx.navigation.**
package com.goalwall.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val RECENTLY_COMPLETED_LIMIT = 5

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val goalRepository: GoalRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DashboardUiState())
        val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

        init {
            goalRepository.allGoals
                .onEach { goals ->
                    val nonArchived = goals.filter { it.status != GoalStatus.ARCHIVED }
                    val activeGoals = goals.filter { it.status == GoalStatus.ACTIVE }

                    _uiState.update {
                        it.copy(
                            totalGoals = nonArchived.size,
                            activeGoals = activeGoals.size,
                            archivedGoals = goals.count { goal -> goal.status == GoalStatus.ARCHIVED },
                            completedGoals = nonArchived.count { goal -> goal.status == GoalStatus.COMPLETED },
                            activeAverageProgress =
                                if (activeGoals.isEmpty()) {
                                    0f
                                } else {
                                    activeGoals.map { goal -> goal.progress }.average().toFloat()
                                },
                            topGoals =
                                activeGoals
                                    .sortedByDescending { goal -> goal.progress }
                                    .take(3),
                            recentlyCompletedGoals =
                                goals
                                    .filter { goal -> goal.status == GoalStatus.COMPLETED }
                                    .sortedByDescending { goal -> goal.updatedAt }
                                    .take(RECENTLY_COMPLETED_LIMIT),
                            isLoading = false,
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }
