// Package: com.goalwall.ui.goal.components
// Layer: UI — Goal components
// Responsibility: Displays a clamped Material3 linear progress indicator.
// Dependencies: Material3
// Forbidden imports: data.**, androidx.navigation.**
package com.goalwall.ui.goal.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.goalwall.ui.theme.GoalWallTheme

@Suppress("FunctionName")
@Composable
fun GoalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    LinearProgressIndicator(
        progress = { clampedProgress },
        modifier = modifier,
    )
}

@Suppress("FunctionName", "UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun GoalProgressBarPreview() {
    GoalWallTheme {
        GoalProgressBar(progress = 0.75f)
    }
}
