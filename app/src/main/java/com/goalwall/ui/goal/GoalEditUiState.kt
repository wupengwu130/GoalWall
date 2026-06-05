// Package: com.goalwall.ui.goal
// Layer: UI — UiState & Event
// Responsibility: 目标新建/编辑页的不可变状态和一次性事件定义
// Dependencies: 无外部依赖
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**
package com.goalwall.ui.goal

data class GoalEditUiState(
    val title: String = "",
    val description: String = "",
    val targetValue: Int = 0,
    val unit: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val color: String = "#4F8EF7",
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val targetValueError: String? = null,
    val unitError: String? = null,
)

sealed class GoalEditEvent {
    data object NavigateBack : GoalEditEvent()

    data class ShowSnackbar(val message: String) : GoalEditEvent()
}
