// Package: com.goalwall
// Layer: UI — Activity
// Responsibility: Single-activity entry hosting theme, scaffold, bottom nav, and AppNavHost.
// Dependencies: AppNavHost, GoalWallTheme, Screen
// Forbidden imports: data.**, androidx.room.**
package com.goalwall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.goalwall.ui.navigation.AppNavHost
import com.goalwall.ui.navigation.Screen
import com.goalwall.ui.theme.GoalWallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoalWallTheme {
                GoalWallMainContent()
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalWallMainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = isTopLevelDestination(currentRoute)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GoalWallNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalWallNavigationBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.GoalList.route,
            onClick = { navController.navigateToTopLevel(Screen.GoalList.route) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.nav_tab_goals),
                )
            },
            label = { Text(stringResource(R.string.nav_tab_goals)) },
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { navController.navigateToTopLevel(Screen.Dashboard.route) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.nav_tab_dashboard),
                )
            },
            label = { Text(stringResource(R.string.nav_tab_dashboard)) },
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = { navController.navigateToTopLevel(Screen.Settings.route) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.nav_tab_settings),
                )
            },
            label = { Text(stringResource(R.string.nav_tab_settings)) },
        )
    }
}

private fun isTopLevelDestination(route: String?): Boolean =
    route == Screen.GoalList.route ||
        route == Screen.Dashboard.route ||
        route == Screen.Settings.route

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
