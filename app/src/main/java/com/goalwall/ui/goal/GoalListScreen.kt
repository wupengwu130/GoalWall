// Package: com.goalwall.ui.goal
// Layer: UI — Goal list screen
// Responsibility: Renders goal list UI from GoalListUiState and forwards navigation events.
// Dependencies: GoalListViewModel, GoalCard, Material3, strings.xml
// Forbidden imports: com.goalwall.data.repository.**, com.goalwall.data.db.**, androidx.room.**
package com.goalwall.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R
import com.goalwall.data.model.GoalFilter
import com.goalwall.ui.goal.components.GoalCard

@Suppress("FunctionName")
@Composable
fun GoalListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: GoalListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is GoalListEvent.NavigateToDetail -> onNavigateToDetail(event.goalId)
                is GoalListEvent.NavigateToCreate -> onNavigateToCreate()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddGoalClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.goal_list_add_goal),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            GoalFilterRow(
                selectedFilter = uiState.filter,
                onFilterSelected = viewModel::setFilter,
            )
            GoalListBody(
                uiState = uiState,
                onGoalClick = viewModel::onGoalClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalFilterRow(
    selectedFilter: GoalFilter,
    onFilterSelected: (GoalFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(GoalFilter.entries.toList()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = stringResource(filterLabelRes(filter))) },
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalListBody(
    uiState: GoalListUiState,
    onGoalClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.goal_list_loading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            uiState.errorMessage != null -> {
                Text(
                    text = stringResource(R.string.goal_list_error, uiState.errorMessage),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
            uiState.goals.isEmpty() -> {
                Text(
                    text = stringResource(R.string.goal_list_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.goals,
                        key = { goal -> goal.id },
                    ) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                        )
                    }
                }
            }
        }
    }
}

private fun filterLabelRes(filter: GoalFilter): Int =
    when (filter) {
        GoalFilter.ACTIVE -> R.string.goal_filter_active
        GoalFilter.ARCHIVED -> R.string.goal_filter_archived
        GoalFilter.ALL -> R.string.goal_filter_all
        GoalFilter.COMPLETED -> R.string.goal_filter_completed
        GoalFilter.PAUSED -> R.string.goal_filter_paused
    }
