// Package: com.goalwall.ui.goal
// Layer: UI — ViewModel
// Responsibility: Observes goal detail and progress history, coordinates mutations.
// Dependencies: GoalRepository, ProgressRepository, NotificationHelper, GoalDetailUiState, GoalDetailEvent
// Forbidden imports: androidx.room.**, com.goalwall.data.db.**, androidx.compose.**, androidx.navigation.**
package com.goalwall.ui.goal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.repository.GoalRepository
import com.goalwall.data.repository.ProgressRepository
import com.goalwall.notification.NotificationHelper
import com.goalwall.worker.enqueueWidgetSync
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        @ApplicationContext private val appContext: Context,
        private val goalRepository: GoalRepository,
        private val progressRepository: ProgressRepository,
        private val notificationHelper: NotificationHelper,
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
                appContext.enqueueWidgetSync()
            }
        }

        fun toggleMilestone(
            milestoneId: Long,
            completed: Boolean,
        ) {
            viewModelScope.launch {
                val detail = _uiState.value.detail ?: return@launch
                val milestone =
                    detail.milestones.find { it.id == milestoneId } ?: return@launch
                val wasCompleted = milestone.completed

                goalRepository.toggleMilestone(milestoneId, completed)

                if (!wasCompleted && completed) {
                    notificationHelper.showMilestoneAchieved(
                        goalTitle = detail.goal.title,
                        milestoneTitle = milestone.title,
                    )
                }
                appContext.enqueueWidgetSync()
            }
        }

        fun addMilestone(
            title: String,
            targetValue: Int,
        ) {
            viewModelScope.launch {
                goalRepository.addMilestone(goalId, title, targetValue)
                appContext.enqueueWidgetSync()
            }
        }

        fun onEditClick() {
            viewModelScope.launch {
                _events.send(GoalDetailEvent.NavigateToEdit(goalId))
            }
        }
    }
