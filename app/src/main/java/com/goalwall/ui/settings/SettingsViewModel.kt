// Package: com.goalwall.ui.settings
// Layer: UI — ViewModel
// Responsibility: 订阅 UserPreferences，提供设置项读写
// Dependencies: UserPreferences
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**, androidx.navigation.**
package com.goalwall.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalwall.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val reminderEnabled: Boolean = true,
    val themeMode: String = "system",
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            combine(
                userPreferences.reminderEnabled,
                userPreferences.themeMode,
            ) { reminder, theme ->
                SettingsUiState(reminderEnabled = reminder, themeMode = theme)
            }
                .onEach { state -> _uiState.value = state }
                .launchIn(viewModelScope)
        }

        fun setReminderEnabled(enabled: Boolean) {
            viewModelScope.launch { userPreferences.setReminderEnabled(enabled) }
        }

        fun setThemeMode(mode: String) {
            viewModelScope.launch { userPreferences.setThemeMode(mode) }
        }
    }
