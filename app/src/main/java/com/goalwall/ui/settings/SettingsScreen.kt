// Package: com.goalwall.ui.settings
// Layer: UI — Screen
// Responsibility: 设置页 UI，提醒开关、主题选择、版本信息
// Dependencies: SettingsViewModel
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**
package com.goalwall.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R
import com.goalwall.data.UserPreferences

private val themeOptionResources =
    listOf(
        UserPreferences.THEME_SYSTEM to R.string.settings_theme_system,
        UserPreferences.THEME_LIGHT to R.string.settings_theme_light,
        UserPreferences.THEME_DARK to R.string.settings_theme_dark,
    )

private val languageOptionResources =
    listOf(
        UserPreferences.LANGUAGE_SYSTEM to R.string.settings_language_system,
        UserPreferences.LANGUAGE_ZH to R.string.settings_language_zh,
        UserPreferences.LANGUAGE_EN to R.string.settings_language_en,
    )

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    val versionName =
        remember(context) {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
                .versionName
                .orEmpty()
        }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                viewModel.setReminderTime(hour, minute)
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
            )
        },
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            settingsNotificationSection(
                reminderEnabled = uiState.reminderEnabled,
                reminderHour = uiState.reminderHour,
                reminderMinute = uiState.reminderMinute,
                onReminderChange = viewModel::setReminderEnabled,
                onReminderTimeClick = { showTimePicker = true },
            )
            settingsLanguageSection(
                selectedLanguage = uiState.language,
                onLanguageChange = viewModel::setLanguage,
            )
            settingsAppearanceSection(
                selectedThemeMode = uiState.themeMode,
                onThemeModeChange = viewModel::setThemeMode,
            )
            settingsAboutSection(versionName = versionName)
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time_title)) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
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

private fun LazyListScope.settingsNotificationSection(
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onReminderChange: (Boolean) -> Unit,
    onReminderTimeClick: () -> Unit,
) {
    item {
        SettingsSectionTitle(textRes = R.string.settings_section_notification)
    }
    item {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_reminder_title)) },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.settings_reminder_summary,
                        reminderHour,
                        reminderMinute,
                    ),
                )
            },
            trailingContent = {
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChange,
                )
            },
        )
        HorizontalDivider()
    }
    item {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_reminder_time_title)) },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.settings_reminder_time_value,
                        reminderHour,
                        reminderMinute,
                    ),
                )
            },
            modifier = Modifier.clickable(onClick = onReminderTimeClick),
        )
        HorizontalDivider()
    }
}

private fun LazyListScope.settingsLanguageSection(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
) {
    item {
        SettingsSectionTitle(textRes = R.string.settings_section_language)
    }
    items(languageOptionResources, key = { it.first }) { (value, labelRes) ->
        ListItem(
            headlineContent = { Text(stringResource(labelRes)) },
            leadingContent = {
                RadioButton(
                    selected = selectedLanguage == value,
                    onClick = { onLanguageChange(value) },
                )
            },
            modifier = Modifier.clickable { onLanguageChange(value) },
        )
    }
    item {
        HorizontalDivider()
    }
}

private fun LazyListScope.settingsAppearanceSection(
    selectedThemeMode: String,
    onThemeModeChange: (String) -> Unit,
) {
    item {
        SettingsSectionTitle(textRes = R.string.settings_section_appearance)
    }
    item {
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
    items(themeOptionResources, key = { it.first }) { (value, labelRes) ->
        ListItem(
            headlineContent = { Text(stringResource(labelRes)) },
            leadingContent = {
                RadioButton(
                    selected = selectedThemeMode == value,
                    onClick = { onThemeModeChange(value) },
                )
            },
            modifier = Modifier.clickable { onThemeModeChange(value) },
        )
    }
    item {
        HorizontalDivider()
    }
}

private fun LazyListScope.settingsAboutSection(versionName: String) {
    item {
        SettingsSectionTitle(textRes = R.string.settings_section_about)
    }
    item {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_version_title)) },
            trailingContent = {
                Text(
                    text = versionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun SettingsSectionTitle(
    textRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
