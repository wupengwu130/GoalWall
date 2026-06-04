// Package: com.goalwall.ui.theme
// Layer: UI — Theme
// Responsibility: Applies GoalWall Material3 theme with light/dark color schemes.
// Dependencies: Color.kt, Type.kt, Material3
// Forbidden imports: data.**, androidx.room.**
package com.goalwall.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        primary = GoalWallPrimary,
        onPrimary = GoalWallOnPrimary,
        surface = GoalWallSurface,
        onSurface = GoalWallOnSurface,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = GoalWallPrimaryDark,
        onPrimary = GoalWallOnSurfaceDark,
        surface = GoalWallSurfaceDark,
        onSurface = GoalWallOnSurfaceDark,
    )

@Suppress("FunctionName")
@Composable
fun GoalWallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoalWallTypography,
        content = content,
    )
}
