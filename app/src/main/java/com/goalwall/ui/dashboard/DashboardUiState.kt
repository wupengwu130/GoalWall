// Package: com.goalwall.ui.dashboard
// Layer: UI — UiState
// Responsibility: 仪表盘页不可变状态，持有目标汇总数据
// Dependencies: data.model.Goal
// Forbidden imports: data.db.**, androidx.room.**
package com.goalwall.ui.dashboard

import com.goalwall.data.model.Goal

data class DashboardUiState(
    val totalGoals: Int = 0,
    val activeGoals: Int = 0,
    val archivedGoals: Int = 0,
    val completedGoals: Int = 0,
    val activeAverageProgress: Float = 0f,
    val topGoals: List<Goal> = emptyList(),
    val recentlyCompletedGoals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
)
