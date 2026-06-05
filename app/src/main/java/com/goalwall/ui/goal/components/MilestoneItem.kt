// Package: com.goalwall.ui.goal.components
// Layer: UI — Goal components
// Responsibility: Displays a single milestone row with completion checkbox.
// Dependencies: Milestone, Material3, strings.xml
// Forbidden imports: com.goalwall.data.repository.**, androidx.navigation.**
package com.goalwall.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.goalwall.R
import com.goalwall.data.model.Milestone

@Suppress("FunctionName")
@Composable
fun MilestoneItem(
    milestone: Milestone,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = milestone.completed,
            onCheckedChange = onCheckedChange,
        )
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.milestone_target_value, milestone.targetValue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
