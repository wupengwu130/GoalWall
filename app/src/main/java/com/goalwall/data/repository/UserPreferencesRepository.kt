package com.goalwall.data.repository

/**
 * Placeholder — full API in Task 6.3 (Architecture.md §10).
 */
interface UserPreferencesRepository {
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
}
