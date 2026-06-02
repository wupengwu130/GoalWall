package com.goalwall.data.repository

/**
 * Placeholder — full API in Task 3.7 (Architecture.md §5).
 */
interface ProgressRepository {
    suspend fun recordProgress(
        goalId: Long,
        value: Float,
        note: String = "",
    )
}
