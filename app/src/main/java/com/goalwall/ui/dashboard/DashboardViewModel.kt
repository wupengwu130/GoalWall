// Package: com.goalwall.ui.dashboard
// Layer: UI — ViewModel
// Responsibility: 订阅 goalRepository.goals，派生仪表盘汇总统计
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

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val goalRepository: GoalRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DashboardUiState())
        val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

        init {
            goalRepository.goals
                .onEach { goals ->
                    _uiState.update {
                        it.copy(
                            totalGoals = goals.size,
                            completedGoals = goals.count { g -> g.status == GoalStatus.COMPLETED },
                            averageProgress =
                                if (goals.isEmpty()) {
                                    0f
                                } else {
                                    goals.map { g -> g.progress }.average().toFloat()
                                },
                            topGoals =
                                goals
                                    .sortedByDescending { g -> g.progress }
                                    .take(3),
                            isLoading = false,
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }
