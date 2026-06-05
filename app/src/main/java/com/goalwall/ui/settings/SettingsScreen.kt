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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R

private val themeOptionResources =
    listOf(
        "system" to R.string.settings_theme_system,
        "light" to R.string.settings_theme_light,
        "dark" to R.string.settings_theme_dark,
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
    val versionName =
        remember(context) {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
                .versionName
                .orEmpty()
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
                onReminderChange = viewModel::setReminderEnabled,
            )
            settingsAppearanceSection(
                selectedThemeMode = uiState.themeMode,
                onThemeModeChange = viewModel::setThemeMode,
            )
            settingsAboutSection(versionName = versionName)
        }
    }
}

private fun LazyListScope.settingsNotificationSection(
    reminderEnabled: Boolean,
    onReminderChange: (Boolean) -> Unit,
) {
    item {
        SettingsSectionTitle(textRes = R.string.settings_section_notification)
    }
    item {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_reminder_title)) },
            supportingContent = { Text(stringResource(R.string.settings_reminder_summary)) },
            trailingContent = {
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChange,
                )
            },
        )
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
