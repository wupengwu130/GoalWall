// Package: com.goalwall.ui.goal
// Layer: UI — ViewModel
// Responsibility: Observes goal detail and progress history, coordinates mutations.
// Dependencies: GoalRepository, ProgressRepository, GoalDetailUiState, GoalDetailEvent
// Forbidden imports: androidx.room.**, com.goalwall.data.db.**, androidx.compose.**, androidx.navigation.**
package com.goalwall.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.repository.GoalRepository
import com.goalwall.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel
    @Inject
    constructor(
        private val goalRepository: GoalRepository,
        private val progressRepository: ProgressRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val goalId: Long =
            checkNotNull(savedStateHandle.get<Long>("goalId"))

        private val _uiState = MutableStateFlow(GoalDetailUiState())
        val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

        private val _events = Channel<GoalDetailEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            goalRepository.observeGoalDetail(goalId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
                .onEach { detail ->
                    _uiState.update {
                        it.copy(
                            detail = detail,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .launchIn(viewModelScope)

            progressRepository.observeProgressHistory(goalId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message)
                    }
                }
                .onEach { history ->
                    _uiState.update {
                        it.copy(
                            progressHistory = history,
                            errorMessage = null,
                        )
                    }
                }
                .launchIn(viewModelScope)
        }

        fun setCurrentValue(
            newValue: Int,
            note: String? = null,
        ) {
            viewModelScope.launch {
                val detail = _uiState.value.detail ?: return@launch
                val oldValue = detail.goal.currentValue
                val boundedNewValue =
                    newValue.coerceIn(
                        0,
                        detail.goal.targetValue,
                    )
                val delta = boundedNewValue - oldValue
                goalRepository.updateCurrentValue(goalId, boundedNewValue)
                if (delta != 0) {
                    progressRepository.recordProgress(goalId, delta, note)
                }
            }
        }

        fun toggleMilestone(
            milestoneId: Long,
            completed: Boolean,
        ) {
            viewModelScope.launch {
                goalRepository.toggleMilestone(milestoneId, completed)
            }
        }

        fun addMilestone(
            title: String,
            targetValue: Int,
        ) {
            viewModelScope.launch {
                goalRepository.addMilestone(goalId, title, targetValue)
            }
        }

        fun onEditClick() {
            viewModelScope.launch {
                _events.send(GoalDetailEvent.NavigateToEdit(goalId))
            }
        }
    }
