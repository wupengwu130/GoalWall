// Package: com.goalwall.ui.goal
// Layer: UI — Goal detail screen
// Responsibility: Renders goal detail UI from GoalDetailUiState and forwards navigation events.
// Dependencies: GoalDetailViewModel, MilestoneItem, GoalProgressBar, Material3, strings.xml
// Forbidden imports: com.goalwall.data.repository.**, com.goalwall.data.db.**, androidx.room.**
package com.goalwall.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R
import com.goalwall.data.model.Goal
import com.goalwall.data.model.GoalDetail
import com.goalwall.data.model.Milestone
import com.goalwall.data.model.ProgressRecord
import com.goalwall.ui.goal.components.GoalProgressBar
import com.goalwall.ui.goal.components.MilestoneItem
import java.text.DateFormat
import java.util.Date

private const val LARGE_TARGET_THRESHOLD = 10
private const val QUICK_INCREMENT_FIVE = 5
private const val QUICK_INCREMENT_TEN = 10

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalDetailEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                is GoalDetailEvent.ShowUndoableSnackbar -> {
                    val result =
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Short,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoProgress(event.undoDelta)
                    }
                }
                is GoalDetailEvent.NavigateBack -> onNavigateBack()
                is GoalDetailEvent.NavigateToEdit -> onNavigateToEdit(event.goalId)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            GoalDetailTopBar(
                title = uiState.detail?.goal?.title,
                onNavigateBack = onNavigateBack,
                onEditClick = viewModel::onEditClick,
                onMarkCompleted = viewModel::markCompleted,
                onPauseGoal = viewModel::pauseGoal,
                onArchiveGoal = viewModel::archiveGoal,
            )
        },
    ) { innerPadding ->
        GoalDetailBody(
            uiState = uiState,
            onDecrease = viewModel::decrementProgress,
            onIncrease = { viewModel.incrementProgress(delta = 1) },
            onIncrementBy = viewModel::incrementProgress,
            onCompleteGoal = viewModel::completeGoal,
            onMilestoneCheckedChange = viewModel::toggleMilestone,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailTopBar(
    title: String?,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onPauseGoal: () -> Unit,
    onArchiveGoal: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title ?: stringResource(R.string.goal_detail_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.goal_detail_nav_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.goal_detail_edit),
                )
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.goal_detail_more_actions),
                )
            }
            GoalDetailActionsMenu(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                onMarkCompleted = onMarkCompleted,
                onPauseGoal = onPauseGoal,
                onArchiveGoal = onArchiveGoal,
            )
        },
    )
}

@Suppress("FunctionName")
@Composable
private fun GoalDetailActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit,
    onPauseGoal: () -> Unit,
    onArchiveGoal: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goal_detail_action_mark_completed)) },
            onClick = {
                onDismiss()
                onMarkCompleted()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goal_detail_action_pause)) },
            onClick = {
                onDismiss()
                onPauseGoal()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goal_detail_action_archive)) },
            onClick = {
                onDismiss()
                onArchiveGoal()
            },
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalDetailBody(
    uiState: GoalDetailUiState,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onIncrementBy: (Int) -> Unit,
    onCompleteGoal: () -> Unit,
    onMilestoneCheckedChange: (Long, Boolean) -> Unit,
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
                        text = stringResource(R.string.goal_detail_loading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            uiState.errorMessage != null -> {
                Text(
                    text = stringResource(R.string.goal_detail_error, uiState.errorMessage),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
            uiState.detail == null -> {
                Text(
                    text = stringResource(R.string.goal_detail_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                GoalDetailContent(
                    detail = uiState.detail,
                    progressHistory = uiState.progressHistory,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                    onIncrementBy = onIncrementBy,
                    onCompleteGoal = onCompleteGoal,
                    onMilestoneCheckedChange = onMilestoneCheckedChange,
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalDetailContent(
    detail: GoalDetail,
    progressHistory: List<ProgressRecord>,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onIncrementBy: (Int) -> Unit,
    onCompleteGoal: () -> Unit,
    onMilestoneCheckedChange: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val goal = detail.goal
    val progressPercent = (goal.progress * 100).toInt()
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        goalDetailSummaryItems(
            goal = goal,
            progressPercent = progressPercent,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
            onIncrementBy = onIncrementBy,
            onCompleteGoal = onCompleteGoal,
        )
        milestoneSectionItems(
            milestones = detail.milestones,
            onMilestoneCheckedChange = onMilestoneCheckedChange,
        )
        progressHistorySectionItems(
            progressHistory = progressHistory,
            dateFormatter = dateFormatter,
        )
    }
}

private fun LazyListScope.goalDetailSummaryItems(
    goal: Goal,
    progressPercent: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onIncrementBy: (Int) -> Unit,
    onCompleteGoal: () -> Unit,
) {
    item {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
    goal.description?.let { description ->
        item {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    item {
        Text(
            text =
                stringResource(
                    R.string.goal_detail_value_with_unit,
                    goal.currentValue,
                    goal.targetValue,
                    goal.unit,
                ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    item {
        if (goal.currentValue >= goal.targetValue) {
            Text(
                text = stringResource(R.string.goal_detail_status_completed),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = stringResource(R.string.goal_progress_percent, progressPercent),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    item {
        GoalProgressBar(
            progress = goal.progress,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    item {
        GoalDetailProgressControls(
            goal = goal,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
            onIncrementBy = onIncrementBy,
            onCompleteGoal = onCompleteGoal,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalDetailProgressControls(
    goal: Goal,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onIncrementBy: (Int) -> Unit,
    onCompleteGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = goal.currentValue >= goal.targetValue
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomIncrementDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { delta ->
                showCustomDialog = false
                onIncrementBy(delta)
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isCompleted) {
            Text(
                text = stringResource(R.string.goal_detail_status_completed),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            return
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onDecrease) {
                Text(
                    text = stringResource(R.string.goal_detail_decrease_one),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = goal.currentValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
            TextButton(onClick = onIncrease) {
                Text(
                    text = stringResource(R.string.goal_detail_increase_one),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (goal.targetValue > LARGE_TARGET_THRESHOLD) {
                TextButton(onClick = { onIncrementBy(QUICK_INCREMENT_FIVE) }) {
                    Text(
                        text = stringResource(R.string.goal_detail_increase_five),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                TextButton(onClick = { onIncrementBy(QUICK_INCREMENT_TEN) }) {
                    Text(
                        text = stringResource(R.string.goal_detail_increase_ten),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                TextButton(onClick = { showCustomDialog = true }) {
                    Text(
                        text = stringResource(R.string.goal_detail_custom_increment),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            TextButton(onClick = onCompleteGoal) {
                Text(
                    text = stringResource(R.string.goal_detail_complete_goal),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun CustomIncrementDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val parsedDelta = input.trim().toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goal_detail_custom_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        showError = false
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    supportingText =
                        if (showError) {
                            { Text(stringResource(R.string.goal_detail_custom_invalid_value)) }
                        } else {
                            null
                        },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (parsedDelta == null || parsedDelta <= 0) {
                        showError = true
                    } else {
                        onConfirm(parsedDelta)
                    }
                },
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun LazyListScope.milestoneSectionItems(
    milestones: List<Milestone>,
    onMilestoneCheckedChange: (Long, Boolean) -> Unit,
) {
    item {
        Text(
            text = stringResource(R.string.goal_detail_milestones_title),
            style = MaterialTheme.typography.titleMedium,
        )
    }
    if (milestones.isEmpty()) {
        item {
            Text(
                text = stringResource(R.string.goal_detail_milestones_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        items(
            items = milestones,
            key = { milestone -> milestone.id },
        ) { milestone ->
            MilestoneItem(
                milestone = milestone,
                onCheckedChange = { checked ->
                    onMilestoneCheckedChange(milestone.id, checked)
                },
            )
        }
    }
}

private fun LazyListScope.progressHistorySectionItems(
    progressHistory: List<ProgressRecord>,
    dateFormatter: DateFormat,
) {
    item {
        Text(
            text = stringResource(R.string.goal_detail_progress_history_title),
            style = MaterialTheme.typography.titleMedium,
        )
    }
    if (progressHistory.isEmpty()) {
        item {
            Text(
                text = stringResource(R.string.goal_detail_progress_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        items(
            items = progressHistory,
            key = { record -> record.id },
        ) { record ->
            ProgressHistoryItem(
                record = record,
                dateFormatter = dateFormatter,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun ProgressHistoryItem(
    record: ProgressRecord,
    dateFormatter: DateFormat,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = formatProgressDelta(record.value),
            style = MaterialTheme.typography.bodyLarge,
        )
        record.note?.let { note ->
            Text(
                text = stringResource(R.string.goal_detail_history_note, note),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text =
                stringResource(
                    R.string.goal_detail_record_date,
                    dateFormatter.format(Date(record.recordDate)),
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun formatProgressDelta(value: Int): String =
    if (value > 0) {
        stringResource(R.string.goal_detail_progress_delta_positive, value)
    } else {
        stringResource(R.string.goal_detail_progress_delta_negative, value)
    }
