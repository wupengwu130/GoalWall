// Package: com.goalwall.ui.goal
// Layer: UI — ViewModel
// Responsibility: 目标新建/编辑页业务逻辑，协调 GoalRepository 完成保存
// Dependencies: GoalRepository, SavedStateHandle
// Forbidden imports: data.db.**, androidx.room.**, androidx.navigation.**
package com.goalwall.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.model.Goal
import com.goalwall.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalEditViewModel
    @Inject
    constructor(
        private val goalRepository: GoalRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val goalId: Long? = savedStateHandle.get<Long>("goalId")

        private val _uiState = MutableStateFlow(GoalEditUiState())
        val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

        private val _events = Channel<GoalEditEvent>(Channel.BUFFERED)
        val events: Flow<GoalEditEvent> = _events.receiveAsFlow()

        val isEditMode: Boolean = goalId != null

        private var hasLoadedInitialData = false
        private var originalGoal: Goal? = null

        init {
            if (goalId != null) {
                viewModelScope.launch {
                    goalRepository.observeGoalDetail(goalId).collect { detail ->
                        if (!hasLoadedInitialData && detail != null) {
                            hasLoadedInitialData = true
                            originalGoal = detail.goal
                            _uiState.update { state ->
                                state.copy(
                                    title = detail.goal.title,
                                    description = detail.goal.description.orEmpty(),
                                    targetValue = detail.goal.targetValue,
                                    unit = detail.goal.unit,
                                    startDate = detail.goal.startDate,
                                    targetDate = detail.goal.targetDate,
                                    color = detail.goal.color,
                                )
                            }
                        }
                    }
                }
            }
        }

        fun onTitleChange(value: String) {
            _uiState.update { it.copy(title = value, titleError = null) }
        }

        fun onDescriptionChange(value: String) {
            _uiState.update { it.copy(description = value) }
        }

        fun onTargetValueChange(value: Int) {
            _uiState.update { it.copy(targetValue = value, targetValueError = null) }
        }

        fun onUnitChange(value: String) {
            _uiState.update { it.copy(unit = value, unitError = null) }
        }

        fun onStartDateChange(value: Long) {
            _uiState.update { it.copy(startDate = value) }
        }

        fun onTargetDateChange(value: Long?) {
            _uiState.update { it.copy(targetDate = value) }
        }

        fun onColorChange(value: String) {
            _uiState.update { it.copy(color = value) }
        }

        @Suppress("TooGenericExceptionCaught")
        fun saveGoal(
            titleRequiredError: String,
            targetValueError: String,
            unitRequiredError: String,
        ) {
            val state = _uiState.value
            var hasError = false

            if (state.title.isBlank()) {
                _uiState.update { it.copy(titleError = titleRequiredError) }
                hasError = true
            }
            if (state.targetValue <= 0) {
                _uiState.update { it.copy(targetValueError = targetValueError) }
                hasError = true
            }
            if (state.unit.isBlank()) {
                _uiState.update { it.copy(unitError = unitRequiredError) }
                hasError = true
            }
            if (hasError) return

            _uiState.update { it.copy(isSaving = true) }

            viewModelScope.launch {
                try {
                    if (goalId == null) {
                        goalRepository.addGoal(
                            title = state.title,
                            targetValue = state.targetValue,
                            unit = state.unit,
                            startDate = state.startDate,
                            targetDate = state.targetDate,
                            description = state.description,
                            color = state.color,
                        )
                    } else {
                        val original = originalGoal ?: return@launch
                        goalRepository.updateGoal(
                            original.copy(
                                title = state.title,
                                description = state.description,
                                targetValue = state.targetValue,
                                unit = state.unit,
                                startDate = state.startDate,
                                targetDate = state.targetDate,
                                color = state.color,
                            ),
                        )
                    }
                    _events.send(GoalEditEvent.NavigateBack)
                } catch (e: Exception) {
                    e.message?.takeIf { it.isNotBlank() }?.let { message ->
                        _events.send(GoalEditEvent.ShowSnackbar(message))
                    }
                } finally {
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }
