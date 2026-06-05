// Package: com.goalwall.data
// Layer: Data — Preferences
// Responsibility: DataStore Preferences 封装，存储用户偏好（提醒开关、主题模式）
// Dependencies: DataStore Preferences, ApplicationContext
// Forbidden imports: ui.**, data.db.**, data.repository.**
package com.goalwall.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
            val THEME_MODE = stringPreferencesKey("theme_mode")
        }

        val reminderEnabled: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.REMINDER_ENABLED] ?: true
            }

        val themeMode: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.THEME_MODE] ?: "system"
            }

        suspend fun setReminderEnabled(enabled: Boolean) {
            context.dataStore.edit { preferences ->
                preferences[Keys.REMINDER_ENABLED] = enabled
            }
        }

        suspend fun setThemeMode(mode: String) {
            context.dataStore.edit { preferences ->
                preferences[Keys.THEME_MODE] = mode
            }
        }
    }
