// Package: com.goalwall.ui.navigation
// Layer: UI — Navigation
// Responsibility: Registers Navigation Compose routes and placeholder destinations.
// Dependencies: Screen, strings.xml
// Forbidden imports: data.**, androidx.room.**
package com.goalwall.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goalwall.R

@Suppress("FunctionName")
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.GoalList.route,
        modifier = modifier,
    ) {
        composable(Screen.GoalList.route) {
            PlaceholderScreen(stringResource(R.string.nav_placeholder_goal_list))
        }
        composable(Screen.Dashboard.route) {
            PlaceholderScreen(stringResource(R.string.nav_placeholder_dashboard))
        }
        composable(Screen.Settings.route) {
            PlaceholderScreen(stringResource(R.string.nav_placeholder_settings))
        }
        composable(
            route = Screen.GoalDetail.ROUTE,
            arguments =
                listOf(
                    navArgument("goalId") {
                        type = NavType.LongType
                    },
                ),
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong("goalId")
            PlaceholderScreen(
                stringResource(
                    R.string.nav_placeholder_goal_detail,
                    goalId ?: 0L,
                ),
            )
        }
        composable(
            route = Screen.GoalEdit.ROUTE,
            arguments =
                listOf(
                    navArgument("goalId") {
                        type = NavType.LongType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong("goalId")
            val label =
                if (goalId != null) {
                    stringResource(R.string.nav_placeholder_goal_edit_with_id, goalId)
                } else {
                    stringResource(R.string.nav_placeholder_goal_edit)
                }
            PlaceholderScreen(label)
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
