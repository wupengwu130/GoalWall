// Package: com.goalwall.ui.settings
// Layer: UI — ViewModel
// Responsibility: 订阅 UserPreferences，提供设置项读写
// Dependencies: UserPreferences
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**, androidx.navigation.**
package com.goalwall.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.UserPreferences
import com.goalwall.util.LocaleHelper
import com.goalwall.worker.ReminderWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = UserPreferences.DEFAULT_REMINDER_HOUR,
    val reminderMinute: Int = UserPreferences.DEFAULT_REMINDER_MINUTE,
    val themeMode: String = UserPreferences.THEME_SYSTEM,
    val language: String = UserPreferences.LANGUAGE_ZH,
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
        private val reminderWorkScheduler: ReminderWorkScheduler,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            combine(
                userPreferences.reminderEnabled,
                userPreferences.reminderHour,
                userPreferences.reminderMinute,
                userPreferences.themeMode,
                userPreferences.language,
            ) { reminderEnabled, hour, minute, theme, language ->
                SettingsUiState(
                    reminderEnabled = reminderEnabled,
                    reminderHour = hour,
                    reminderMinute = minute,
                    themeMode = theme,
                    language = language,
                )
            }
                .catch { /* Keep defaults when DataStore read fails */ }
                .onEach { state -> _uiState.value = state }
                .launchIn(viewModelScope)
        }

        fun setReminderEnabled(enabled: Boolean) {
            viewModelScope.launch { userPreferences.setReminderEnabled(enabled) }
        }

        fun setReminderTime(
            hour: Int,
            minute: Int,
        ) {
            viewModelScope.launch {
                userPreferences.setReminderTime(hour, minute)
                reminderWorkScheduler.reschedule()
            }
        }

        fun setThemeMode(mode: String) {
            viewModelScope.launch { userPreferences.setThemeMode(mode) }
        }

        fun setLanguage(language: String) {
            viewModelScope.launch {
                userPreferences.setLanguage(language)
                LocaleHelper.applyLanguage(language)
            }
        }
    }
