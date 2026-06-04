// Package: com.goalwall.ui.navigation
// Layer: UI — Navigation
// Responsibility: Defines type-safe Navigation Compose routes for all app screens.
// Dependencies: None
// Forbidden imports: data.**, androidx.room.**
package com.goalwall.ui.navigation

sealed class Screen(
    val route: String,
) {
    data object GoalList : Screen("goal_list")

    data object Dashboard : Screen("dashboard")

    data object Settings : Screen("settings")

    data class GoalDetail(
        val goalId: Long,
    ) : Screen(ROUTE) {
        companion object {
            const val ROUTE = "goal_detail/{goalId}"

            fun createRoute(goalId: Long): String = "goal_detail/$goalId"
        }
    }

    data class GoalEdit(
        val goalId: Long? = null,
    ) : Screen(ROUTE) {
        companion object {
            const val ROUTE = "goal_edit?goalId={goalId}"

            fun createRoute(goalId: Long? = null): String =
                if (goalId != null) {
                    "goal_edit?goalId=$goalId"
                } else {
                    "goal_edit"
                }
        }
    }
}
