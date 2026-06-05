// Package: com.goalwall.ui.navigation
// Layer: UI — Navigation
// Responsibility: Registers Navigation Compose routes and placeholder destinations.
// Dependencies: Screen, strings.xml
// Forbidden imports: data.**, androidx.room.**
package com.goalwall.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goalwall.ui.dashboard.DashboardScreen
import com.goalwall.ui.goal.GoalDetailScreen
import com.goalwall.ui.goal.GoalEditScreen
import com.goalwall.ui.goal.GoalListScreen
import com.goalwall.ui.settings.SettingsScreen

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
            GoalListScreen(
                onNavigateToDetail = { goalId ->
                    navController.navigate(Screen.GoalDetail.createRoute(goalId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.GoalEdit.createRoute())
                },
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onGoalClick = { goalId ->
                    navController.navigate(Screen.GoalDetail.createRoute(goalId))
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.GoalDetail.ROUTE,
            arguments =
                listOf(
                    navArgument("goalId") {
                        type = NavType.LongType
                    },
                ),
        ) {
            GoalDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { goalId ->
                    navController.navigate(Screen.GoalEdit.editRoute(goalId))
                },
            )
        }
        composable(route = Screen.GoalEdit.ROUTE_CREATE) {
            GoalEditScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.GoalEdit.ROUTE_EDIT,
            arguments =
                listOf(
                    navArgument("goalId") {
                        type = NavType.LongType
                    },
                ),
        ) {
            GoalEditScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
