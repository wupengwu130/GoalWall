// Package: com.goalwall.ui.goal
// Layer: UI — Goal detail
// Responsibility: Immutable UI state and one-shot events for the goal detail screen.
// Dependencies: GoalDetail, ProgressRecord
// Forbidden imports: androidx.compose.**, kotlinx.coroutines.flow.**
package com.goalwall.ui.goal

import com.goalwall.data.model.GoalDetail
import com.goalwall.data.model.ProgressRecord

data class GoalDetailUiState(
    val detail: GoalDetail? = null,
    val progressHistory: List<ProgressRecord> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
)

sealed class GoalDetailEvent {
    data class ShowSnackbar(val message: String) : GoalDetailEvent()

    data class ShowUndoableSnackbar(
        val message: String,
        val undoDelta: Int,
    ) : GoalDetailEvent()

    data object NavigateBack : GoalDetailEvent()

    data class NavigateToEdit(val goalId: Long) : GoalDetailEvent()
}
