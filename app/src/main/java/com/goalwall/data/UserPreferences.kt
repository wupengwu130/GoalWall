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
import androidx.datastore.preferences.core.intPreferencesKey
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
            val REMINDER_HOUR = intPreferencesKey("reminder_hour")
            val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val LANGUAGE = stringPreferencesKey("language")
        }

        val reminderEnabled: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.REMINDER_ENABLED] ?: true
            }

        val reminderHour: Flow<Int> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.REMINDER_HOUR] ?: DEFAULT_REMINDER_HOUR
            }

        val reminderMinute: Flow<Int> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.REMINDER_MINUTE] ?: DEFAULT_REMINDER_MINUTE
            }

        val themeMode: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.THEME_MODE] ?: THEME_SYSTEM
            }

        val language: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[Keys.LANGUAGE] ?: LANGUAGE_ZH
            }

        suspend fun setReminderEnabled(enabled: Boolean) {
            context.dataStore.edit { preferences ->
                preferences[Keys.REMINDER_ENABLED] = enabled
            }
        }

        suspend fun setReminderTime(
            hour: Int,
            minute: Int,
        ) {
            context.dataStore.edit { preferences ->
                preferences[Keys.REMINDER_HOUR] = hour
                preferences[Keys.REMINDER_MINUTE] = minute
            }
        }

        suspend fun setThemeMode(mode: String) {
            context.dataStore.edit { preferences ->
                preferences[Keys.THEME_MODE] = mode
            }
        }

        suspend fun setLanguage(language: String) {
            context.dataStore.edit { preferences ->
                preferences[Keys.LANGUAGE] = language
            }
        }

        companion object {
            const val DEFAULT_REMINDER_HOUR = 9
            const val DEFAULT_REMINDER_MINUTE = 0

            const val THEME_SYSTEM = "system"
            const val THEME_LIGHT = "light"
            const val THEME_DARK = "dark"

            const val LANGUAGE_SYSTEM = "system"
            const val LANGUAGE_ZH = "zh"
            const val LANGUAGE_EN = "en"
        }
    }
