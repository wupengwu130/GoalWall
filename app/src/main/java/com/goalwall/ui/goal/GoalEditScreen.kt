// Package: com.goalwall.ui.goal
// Layer: UI — Screen
// Responsibility: 目标新建/编辑页 Compose UI，消费 GoalEditViewModel 状态与事件
// Dependencies: GoalEditViewModel, GoalEditUiState, GoalEditEvent
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**
package com.goalwall.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

private val presetColors =
    listOf(
        "#4F8EF7",
        "#F7724F",
        "#4FD9B3",
        "#F7D44F",
        "#A04FF7",
    )

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalEditViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val errorTitleRequired = stringResource(R.string.goal_edit_error_title_required)
    val errorTargetValue = stringResource(R.string.goal_edit_error_target_value_positive)
    val errorUnitRequired = stringResource(R.string.goal_edit_error_unit_required)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalEditEvent.NavigateBack -> onNavigateBack()
                is GoalEditEvent.ShowSnackbar ->
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            stringResource(
                                if (viewModel.isEditMode) {
                                    R.string.goal_edit_title_edit
                                } else {
                                    R.string.goal_edit_title_create
                                },
                            ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.goal_edit_content_description_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        GoalEditForm(
            uiState = uiState,
            viewModel = viewModel,
            errorTitleRequired = errorTitleRequired,
            errorTargetValue = errorTargetValue,
            errorUnitRequired = errorUnitRequired,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditForm(
    uiState: GoalEditUiState,
    viewModel: GoalEditViewModel,
    errorTitleRequired: String,
    errorTargetValue: String,
    errorUnitRequired: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GoalEditTextFields(
            uiState = uiState,
            viewModel = viewModel,
        )
        Spacer(modifier = Modifier.height(16.dp))
        GoalEditStartDatePicker(
            startDate = uiState.startDate,
            onStartDateChange = viewModel::onStartDateChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        GoalEditTargetDatePicker(
            targetDate = uiState.targetDate,
            onTargetDateChange = viewModel::onTargetDateChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        GoalEditColorPicker(
            selectedColor = uiState.color,
            onColorChange = viewModel::onColorChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.saveGoal(
                    titleRequiredError = errorTitleRequired,
                    targetValueError = errorTargetValue,
                    unitRequiredError = errorUnitRequired,
                )
            },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(R.string.goal_edit_button_save))
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalEditTextFields(
    uiState: GoalEditUiState,
    viewModel: GoalEditViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text(stringResource(R.string.goal_edit_label_title)) },
            isError = uiState.titleError != null,
            supportingText = uiState.titleError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(R.string.goal_edit_label_description)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (uiState.targetValue == 0) "" else uiState.targetValue.toString(),
            onValueChange = { input ->
                val parsed = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                viewModel.onTargetValueChange(parsed)
            },
            label = { Text(stringResource(R.string.goal_edit_label_target_value)) },
            isError = uiState.targetValueError != null,
            supportingText = uiState.targetValueError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.unit,
            onValueChange = viewModel::onUnitChange,
            label = { Text(stringResource(R.string.goal_edit_label_unit)) },
            isError = uiState.unitError != null,
            supportingText = uiState.unitError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditStartDatePicker(
    startDate: Long,
    onStartDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    val startDateFormatted =
        remember(startDate) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate))
        }

    OutlinedButton(
        onClick = { showStartDatePicker = true },
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text =
                stringResource(R.string.goal_edit_label_start_date) +
                    ": " +
                    startDateFormatted,
        )
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onStartDateChange)
                        showStartDatePicker = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditTargetDatePicker(
    targetDate: Long?,
    onTargetDateChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTargetDatePicker by remember { mutableStateOf(false) }
    val targetDateFormatted =
        remember(targetDate) {
            targetDate?.let { date ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
            }
        }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { showTargetDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text =
                    stringResource(R.string.goal_edit_label_target_date) +
                        ": " +
                        (
                            targetDateFormatted
                                ?: stringResource(R.string.goal_edit_label_target_date_none)
                        ),
            )
        }
        if (targetDate != null) {
            TextButton(
                onClick = { onTargetDateChange(null) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.goal_edit_button_clear_date))
            }
        }
    }

    if (showTargetDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = targetDate ?: System.currentTimeMillis(),
            )
        DatePickerDialog(
            onDismissRequest = { showTargetDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onTargetDateChange(it) }
                        showTargetDatePicker = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalEditColorPicker(
    selectedColor: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(R.string.goal_edit_label_color))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            presetColors.forEach { hex ->
                val isSelected = selectedColor == hex
                val colorDescription =
                    stringResource(R.string.goal_edit_content_description_color_option, hex)
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(AndroidColor.parseColor(hex)))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        Color.Transparent
                                    },
                                shape = CircleShape,
                            )
                            .clickable { onColorChange(hex) }
                            .semantics {
                                contentDescription = colorDescription
                            },
                )
            }
        }
    }
}
