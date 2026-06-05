package com.example.minlishapp_learnenglish.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun DailyPlanCard(
    dailyPlan: DailyLearningPlan,
    onStartReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            Text(
                text = "Daily Plan",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
                DailyPlanLine(
                    icon = Icons.Outlined.AutoStories,
                    title = "${dailyPlan.newCards} New Words",
                    subtitle = if (dailyPlan.newCards > 0) "Ready to explore" else "No new words today"
                )
                DailyPlanLine(
                    icon = Icons.Outlined.History,
                    title = "${dailyPlan.dueReviews} Due for Review",
                    subtitle = if (dailyPlan.dueReviews > 0) "Keep them fresh" else "All reviews are clear"
                )
            }
            Button(
                onClick = onStartReview,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryFixed,
                    contentColor = MaterialTheme.colorScheme.onPrimaryFixed
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = MinLishSpacing.xs)
                ) {
                    Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = null)
                    Text(text = "Review Due", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DailyPlanLine(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(MinLishSpacing.xs)
            )
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
