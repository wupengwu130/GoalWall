package com.goalwall.core.navigation

/**
 * Type-safe route definitions for Navigation Compose.
 * Add feature routes here as screens are implemented.
 */
sealed interface Screen {
    val route: String

    data object Home : Screen {
        override val route: String = "home"
    }
}
