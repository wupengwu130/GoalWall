package com.goalwall.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.goalwall.MainActivity
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
        val topGoal = goals.firstOrNull()
        val backgroundColor = parseWidgetColor(topGoal?.color)
        val textColor = backgroundColor.toContrastTextColor()
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
                    backgroundColor = backgroundColor,
                    textColor = textColor,
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
    backgroundColor: Color,
    textColor: Color,
) {
    val textStyle = TextStyle(color = ColorProvider(textColor))
    val titleStyle = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(textColor))

    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
    ) {
        Text(
            text = title,
            style = titleStyle,
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (goals.isEmpty()) {
            Text(text = emptyText, style = textStyle)
        } else {
            goals.forEach { goal ->
                GoalWallWidgetGoalRow(goal = goal, textStyle = textStyle)
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun GoalWallWidgetGoalRow(
    goal: GoalWallWidgetGoalItem,
    textStyle: TextStyle,
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(text = goal.title, style = textStyle)
        Text(text = goal.valueText, style = textStyle)
        Text(text = goal.progressText, style = textStyle)
    }
}
