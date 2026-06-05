package com.goalwall.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.goalwall.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GoalWallWidgetEntryPoint {
    fun widgetDataProvider(): WidgetDataProvider
}

class GoalWallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val widgetDataProvider =
            EntryPointAccessors
                .fromApplication(
                    context.applicationContext,
                    GoalWallWidgetEntryPoint::class.java,
                ).widgetDataProvider()
        val goals = widgetDataProvider.getTopGoals(limit = 3)
        val titleText = context.getString(R.string.widget_title)
        val emptyText = context.getString(R.string.widget_empty)
        val goalItems =
            goals.map { goal ->
                GoalWallWidgetGoalItem(
                    title = goal.title,
                    valueText =
                        context.getString(
                            R.string.goal_value_ratio,
                            goal.currentValue,
                            goal.targetValue,
                        ),
                    progressText =
                        context.getString(
                            R.string.goal_progress_percent,
                            (goal.progress * 100).toInt(),
                        ),
                )
            }

        provideContent {
            GlanceTheme {
                GoalWallWidgetContent(
                    title = titleText,
                    emptyText = emptyText,
                    goals = goalItems,
                )
            }
        }
    }
}

private data class GoalWallWidgetGoalItem(
    val title: String,
    val valueText: String,
    val progressText: String,
)

@Suppress("FunctionName")
@Composable
private fun GoalWallWidgetContent(
    title: String,
    emptyText: String,
    goals: List<GoalWallWidgetGoalItem>,
) {
    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(
            text = title,
            style = TextStyle(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (goals.isEmpty()) {
            Text(text = emptyText)
        } else {
            goals.forEach { goal ->
                GoalWallWidgetGoalRow(goal = goal)
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalWallWidgetGoalRow(goal: GoalWallWidgetGoalItem) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(text = goal.title)
        Text(text = goal.valueText)
        Text(text = goal.progressText)
    }
}
