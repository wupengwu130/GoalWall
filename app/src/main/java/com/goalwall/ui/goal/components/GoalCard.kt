// Package: com.goalwall.ui.goal.components
// Layer: UI — Goal components
// Responsibility: Displays a single goal summary card with progress.
// Dependencies: Goal, GoalProgressBar, Material3, strings.xml
// Forbidden imports: androidx.navigation.**, com.goalwall.data.repository.**, com.goalwall.data.db.**
package com.goalwall.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goalwall.R
import com.goalwall.data.model.Goal
import com.goalwall.data.model.GoalStatus
import com.goalwall.ui.theme.GoalWallTheme
import android.graphics.Color as AndroidColor

@Suppress("FunctionName")
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = goal.color.toComposeColor(MaterialTheme.colorScheme.primary)
    val progressPercent = (goal.progress * 100).toInt()

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
            )
            Text(
                text = stringResource(R.string.goal_value_ratio, goal.currentValue, goal.targetValue),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.goal_progress_percent, progressPercent),
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
            )
            GoalProgressBar(
                progress = goal.progress,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun String.toComposeColor(fallback: Color): Color =
    runCatching {
        Color(AndroidColor.parseColor(this))
    }.getOrElse { fallback }

@Suppress("FunctionName", "UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun GoalCardPreview() {
    GoalWallTheme {
        GoalCard(
            goal =
                Goal(
                    id = 1L,
                    title = "Daily reading",
                    targetValue = 20,
                    currentValue = 12,
                    unit = "times",
                    startDate = 0L,
                    status = GoalStatus.ACTIVE,
                    color = "#4F8EF7",
                ),
            onClick = {},
        )
    }
}
