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
    val titleError: GoalEditValidationError? = null,
    val targetValueError: GoalEditValidationError? = null,
    val unitError: GoalEditValidationError? = null,
)

enum class GoalEditValidationError {
    TITLE_REQUIRED,
    TARGET_VALUE_INVALID,
    UNIT_REQUIRED,
}

sealed class GoalEditEvent {
    data object NavigateBack : GoalEditEvent()

    data object DataNotReady : GoalEditEvent()

    data class ShowSnackbar(val message: String) : GoalEditEvent()
}
