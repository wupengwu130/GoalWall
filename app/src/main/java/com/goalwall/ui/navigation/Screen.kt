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

    data object GoalEdit : Screen("goal_edit") {
        const val ROUTE_CREATE = "goal_edit"

        const val ROUTE_EDIT = "goal_edit/{goalId}"

        fun createRoute(): String = ROUTE_CREATE

        fun editRoute(goalId: Long): String = "goal_edit/$goalId"
    }
}
