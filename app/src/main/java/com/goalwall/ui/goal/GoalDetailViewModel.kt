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
import com.goalwall.R
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.model.SetStatusResult
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

        fun incrementProgress(
            delta: Int = 1,
            note: String? = null,
        ) {
            applyProgressDelta(delta = delta, note = note)
        }

        fun decrementProgress(note: String? = null) {
            applyProgressDelta(delta = -1, note = note)
        }

        fun completeGoal() {
            viewModelScope.launch {
                val goal = _uiState.value.detail?.goal ?: return@launch
                if (goal.currentValue >= goal.targetValue) return@launch
                val delta = goal.targetValue - goal.currentValue
                applyProgressDeltaInternal(
                    delta = delta,
                    note = null,
                    completionMessage = appContext.getString(R.string.goal_detail_completed_message),
                )
            }
        }

        fun undoProgress(appliedDelta: Int) {
            applyProgressDelta(delta = -appliedDelta)
        }

        private fun applyProgressDelta(
            delta: Int,
            note: String? = null,
        ) {
            viewModelScope.launch {
                applyProgressDeltaInternal(delta = delta, note = note)
            }
        }

        private suspend fun applyProgressDeltaInternal(
            delta: Int,
            note: String? = null,
            completionMessage: String? = null,
        ) {
            val appliedDelta = goalRepository.incrementCurrentValue(goalId, delta, note)
            if (appliedDelta != 0) {
                appContext.enqueueWidgetSync()
                val message =
                    completionMessage
                        ?: appContext.getString(R.string.goal_detail_progress_increased, appliedDelta)
                _events.send(
                    GoalDetailEvent.ShowUndoableSnackbar(
                        message = message,
                        undoDelta = appliedDelta,
                    ),
                )
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

        fun markCompleted() {
            updateGoalStatus(GoalStatus.COMPLETED)
        }

        fun pauseGoal() {
            updateGoalStatus(GoalStatus.PAUSED)
        }

        fun archiveGoal() {
            updateGoalStatus(GoalStatus.ARCHIVED)
        }

        private fun updateGoalStatus(status: GoalStatus) {
            viewModelScope.launch {
                when (goalRepository.setStatus(goalId, status)) {
                    SetStatusResult.SUCCESS -> appContext.enqueueWidgetSync()
                    SetStatusResult.ARCHIVE_NOT_ALLOWED ->
                        _events.send(
                            GoalDetailEvent.ShowSnackbar(
                                appContext.getString(R.string.goal_detail_archive_not_allowed),
                            ),
                        )
                    SetStatusResult.GOAL_NOT_FOUND -> Unit
                }
            }
        }

        fun onEditClick() {
            viewModelScope.launch {
                _events.send(GoalDetailEvent.NavigateToEdit(goalId))
            }
        }
    }
