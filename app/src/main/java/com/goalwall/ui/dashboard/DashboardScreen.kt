// Package: com.goalwall.ui.dashboard
// Layer: UI — Screen
// Responsibility: 仪表盘页，展示目标汇总统计、进度概览与热门目标
// Dependencies: DashboardViewModel, SummaryCard, GoalCard（复用）
// Forbidden imports: data.db.**, data.repository.**, androidx.room.**
package com.goalwall.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalwall.R
import com.goalwall.ui.dashboard.components.SummaryCard
import com.goalwall.ui.goal.components.GoalCard

private const val FULL_CIRCLE_DEGREES = 360f
private const val ARC_START_ANGLE_DEGREES = -90f
private val ProgressRingSize = 120.dp
private val ProgressRingStrokeWidth = 14.dp

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onGoalClick: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            DashboardLoadingContent(paddingValues = paddingValues)
        } else {
            DashboardLoadedContent(
                uiState = uiState,
                paddingValues = paddingValues,
                onGoalClick = onGoalClick,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun DashboardLoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Suppress("FunctionName")
@Composable
private fun DashboardLoadedContent(
    uiState: DashboardUiState,
    paddingValues: PaddingValues,
    onGoalClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        item {
            Text(
                text = stringResource(R.string.dashboard_section_stats),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            DashboardStatsRow(uiState = uiState)
        }
        item {
            DashboardLifecycleStatsRow(uiState = uiState)
        }
        item {
            DashboardOverallProgressRing(averageProgress = uiState.activeAverageProgress)
        }
        item {
            Text(
                text = stringResource(R.string.dashboard_section_top_goals),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        dashboardTopGoalsItems(
            uiState = uiState,
            onGoalClick = onGoalClick,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun DashboardStatsRow(uiState: DashboardUiState) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(
            title = stringResource(R.string.dashboard_stat_total),
            value = uiState.totalGoals.toString(),
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            title = stringResource(R.string.dashboard_stat_completed),
            value = uiState.completedGoals.toString(),
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            title = stringResource(R.string.dashboard_stat_avg_progress),
            value =
                stringResource(
                    R.string.dashboard_avg_progress_format,
                    uiState.activeAverageProgress * 100,
                ),
            icon = Icons.Default.Refresh,
            modifier = Modifier.weight(1f),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun DashboardLifecycleStatsRow(uiState: DashboardUiState) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(
            title = stringResource(R.string.dashboard_stat_active),
            value = uiState.activeGoals.toString(),
            icon = Icons.Default.Refresh,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            title = stringResource(R.string.dashboard_stat_archived),
            value = uiState.archivedGoals.toString(),
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f),
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.dashboardTopGoalsItems(
    uiState: DashboardUiState,
    onGoalClick: (Long) -> Unit,
) {
    if (uiState.topGoals.isEmpty()) {
        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_no_goals),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        items(uiState.topGoals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                onClick = { onGoalClick(goal.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun DashboardOverallProgressRing(
    averageProgress: Float,
    modifier: Modifier = Modifier,
) {
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
    ) {
        Canvas(modifier = Modifier.size(ProgressRingSize)) {
            val stroke = ProgressRingStrokeWidth.toPx()
            val sweep = FULL_CIRCLE_DEGREES * averageProgress
            drawArc(
                color = trackColor,
                startAngle = ARC_START_ANGLE_DEGREES,
                sweepAngle = FULL_CIRCLE_DEGREES,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = ARC_START_ANGLE_DEGREES,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text =
                    stringResource(
                        R.string.dashboard_avg_progress_format,
                        averageProgress * 100,
                    ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.dashboard_overall_progress_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
