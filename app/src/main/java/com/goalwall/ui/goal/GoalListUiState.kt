// Package: com.goalwall.ui.goal
// Layer: UI — Goal list
// Responsibility: Immutable UI state and one-shot events for the goal list screen.
// Dependencies: Goal, GoalFilter
// Forbidden imports: androidx.compose.**, kotlinx.coroutines.flow.**
package com.goalwall.ui.goal

import com.goalwall.data.model.Goal
import com.goalwall.data.model.GoalFilter

data class GoalListUiState(
    val goals: List<Goal> = emptyList(),
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed class GoalListEvent {
    data class ShowSnackbar(val message: String) : GoalListEvent()

    data class NavigateToDetail(val goalId: Long) : GoalListEvent()

    data object NavigateToCreate : GoalListEvent()
}
