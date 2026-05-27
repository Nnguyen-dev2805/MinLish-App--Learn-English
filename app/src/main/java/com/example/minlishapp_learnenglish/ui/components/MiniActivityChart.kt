package com.example.minlishapp_learnenglish.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun MiniActivityChart(
    activities: List<DailyActivity>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Activity", style = MaterialTheme.typography.titleLarge)
                TagChip(text = "This Week")
            }
            if (activities.isEmpty()) {
                EmptyActivityBars()
            } else {
                ActivityBars(activities = activities.takeLast(7))
            }
        }
    }
}

@Composable
private fun ActivityBars(activities: List<DailyActivity>) {
    val maxReviews = maxOf(1, activities.maxOfOrNull { it.reviewCount } ?: 1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
        verticalAlignment = Alignment.Bottom
    ) {
        activities.forEachIndexed { index, activity ->
            val fraction = (activity.reviewCount.toFloat() / maxReviews).coerceIn(0.08f, 1f)
            ActivityBar(
                label = activity.date.takeLast(2),
                fraction = fraction,
                selected = index == activities.lastIndex
            )
        }
    }
}

@Composable
private fun EmptyActivityBars() {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
        verticalAlignment = Alignment.Bottom
    ) {
        labels.forEach { label ->
            ActivityBar(label = label, fraction = 0.08f, selected = false)
        }
    }
}

@Composable
private fun RowScope.ActivityBar(
    label: String,
    fraction: Float,
    selected: Boolean
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp * fraction)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryFixedDim
                    }
                )
        )
        Spacer(modifier = Modifier.height(MinLishSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
