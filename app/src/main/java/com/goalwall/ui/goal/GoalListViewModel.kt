// Package: com.goalwall.ui.goal
// Layer: UI — ViewModel
// Responsibility: Observes goals, applies filters, exposes UI state and one-shot events.
// Dependencies: GoalRepository, GoalListUiState, GoalListEvent
// Forbidden imports: androidx.room.**, com.goalwall.data.db.**, androidx.compose.**, androidx.navigation.**
package com.goalwall.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.model.GoalFilter
import com.goalwall.data.model.GoalStatus
import com.goalwall.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel
    @Inject
    constructor(
        private val goalRepository: GoalRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(GoalListUiState())
        val uiState: StateFlow<GoalListUiState> = _uiState.asStateFlow()

        private val _events = Channel<GoalListEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        private val filterState = MutableStateFlow(GoalFilter.ACTIVE)

        init {
            observeGoals()
        }

        fun setFilter(filter: GoalFilter) {
            filterState.value = filter
        }

        fun onGoalClick(goalId: Long) {
            viewModelScope.launch {
                _events.send(GoalListEvent.NavigateToDetail(goalId))
            }
        }

        fun onAddGoalClick() {
            viewModelScope.launch {
                _events.send(GoalListEvent.NavigateToCreate)
            }
        }

        private fun observeGoals() {
            combine(
                goalRepository.allGoals,
                filterState,
            ) { goals, filter ->
                val filteredGoals =
                    when (filter) {
                        GoalFilter.ACTIVE -> goals.filter { it.status == GoalStatus.ACTIVE }
                        GoalFilter.ARCHIVED -> goals.filter { it.status == GoalStatus.ARCHIVED }
                        GoalFilter.COMPLETED -> goals.filter { it.status == GoalStatus.COMPLETED }
                        GoalFilter.PAUSED -> goals.filter { it.status == GoalStatus.PAUSED }
                        GoalFilter.ALL -> goals
                    }
                filteredGoals to filter
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
                .onEach { (filteredGoals, filter) ->
                    _uiState.update {
                        it.copy(
                            goals = filteredGoals,
                            filter = filter,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }
