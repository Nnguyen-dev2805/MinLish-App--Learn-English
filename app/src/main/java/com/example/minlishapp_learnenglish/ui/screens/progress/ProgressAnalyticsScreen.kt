package com.example.minlishapp_learnenglish.ui.screens.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.example.minlishapp_learnenglish.presentation.viewmodel.progress.ProgressUiState
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun ProgressAnalyticsScreen(
    uiState: ProgressUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading && uiState.stats == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingStateView(message = "Loading progress...")
            }
        }
        uiState.errorMessage != null && uiState.stats == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                contentAlignment = Alignment.Center
            ) {
                ErrorStateView(
                    title = "Unable to load progress",
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
        }
        else -> {
            ProgressContent(
                uiState = uiState,
                onRefresh = onRefresh,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats ?: emptyStats
    val retention = uiState.retention ?: emptyRetention

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        ProgressHeader(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )
        if (uiState.isEmpty) {
            ZeroStateBanner()
        }
        MasteryOverview(stats = stats)
        WordsLearnedChart(activities = uiState.activities)
        RetentionCard(retention = retention)
        LevelEstimationCard(stats = stats, retention = retention)
        LearningBreakdownFallback(stats = stats, retention = retention)
        Spacer(modifier = Modifier.height(MinLishSpacing.lg))
    }
}

@Composable
private fun ProgressHeader(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Column {
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Progress Analytics",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}

@Composable
private fun ZeroStateBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(MinLishSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Insights, contentDescription = null)
            Text(
                text = "No learning data yet. Study a few flashcards to start updating the charts.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MasteryOverview(stats: ProgressStats) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
        Text(
            text = "Mastery Overview",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            OverviewCard(
                label = "Total Words",
                value = stats.learnedWords.toString(),
                helper = "${stats.dueToday} due today",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            OverviewCard(
                label = "Study Streak",
                value = "${stats.streak} Days",
                helper = "Keep the rhythm",
                icon = Icons.Outlined.LocalFireDepartment,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            OverviewCard(
                label = "Accuracy",
                value = "${stats.accuracy.toInt()}%",
                helper = "Correct reviews",
                icon = Icons.Outlined.CheckCircle,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            OverviewCard(
                label = "Level",
                value = stats.levelEstimation ?: "New",
                helper = "Based on learned words",
                icon = Icons.Outlined.Leaderboard,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OverviewCard(
    label: String,
    value: String,
    helper: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = tint,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = helper,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint
                )
            }
        }
    }
}

@Composable
private fun WordsLearnedChart(activities: List<DailyActivity>) {
    MinLishCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Words Learned",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Weekly",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            ActivityLineChart(activities = activities.takeLast(7))
        }
    }
}

@Composable
private fun ActivityLineChart(activities: List<DailyActivity>) {
    val values = if (activities.isEmpty()) {
        List(7) { 0 }
    } else {
        activities.map { it.reviewCount }
    }
    val labels = if (activities.isEmpty()) {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    } else {
        activities.map { it.date.takeLast(2) }
    }
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val fill = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.22f)
    val maxValue = maxOf(1, values.maxOrNull() ?: 1)

    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
        ) {
            val horizontalPadding = 8.dp.toPx()
            val chartWidth = size.width - horizontalPadding * 2
            val chartHeight = size.height - 16.dp.toPx()
            repeat(3) { index ->
                val y = chartHeight * (index + 1) / 4f
                drawLine(
                    color = grid,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = values.mapIndexed { index, value ->
                val x = horizontalPadding + (chartWidth / (values.size - 1).coerceAtLeast(1)) * index
                val normalized = value.toFloat() / maxValue.toFloat()
                val y = chartHeight - normalized * (chartHeight - 16.dp.toPx())
                Offset(x, y)
            }

            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { point -> lineTo(point.x, point.y) }
                }
                val fillPath = Path().apply {
                    moveTo(points.first().x, chartHeight)
                    points.forEach { point -> lineTo(point.x, point.y) }
                    lineTo(points.last().x, chartHeight)
                    close()
                }
                drawPath(path = fillPath, color = fill)
                drawPath(
                    path = linePath,
                    color = primary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = primary,
                        radius = if (index == points.lastIndex) 5.dp.toPx() else 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RetentionCard(retention: RetentionStats) {
    MinLishCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Retention Rate",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${retention.retentionRate.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            RetentionBars(retentionRate = retention.retentionRate)
            Text(
                text = "${retention.retainedReviews}/${retention.totalReviews} reviews retained with Good or Easy ratings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RetentionBars(retentionRate: Double) {
    val base = retentionRate.toFloat().coerceIn(0f, 100f)
    val bars = listOf(
        (base * 0.76f).coerceAtLeast(8f),
        (base * 0.58f).coerceAtLeast(8f),
        base.coerceAtLeast(8f),
        (base * 0.70f).coerceAtLeast(8f),
        (base * 0.86f).coerceAtLeast(8f)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { index, value ->
            RetentionBar(
                label = "L${index + 1}",
                fraction = value / 100f,
                selected = index == 2
            )
        }
    }
}

@Composable
private fun RowScope.RetentionBar(
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
                .height(124.dp * fraction.coerceIn(0.08f, 1f))
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
        )
        Spacer(modifier = Modifier.height(MinLishSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LevelEstimationCard(
    stats: ProgressStats,
    retention: RetentionStats
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryFixed,
        contentColor = MaterialTheme.colorScheme.onPrimaryFixed
    ) {
        Row(
            modifier = Modifier.padding(MinLishSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Outlined.Leaderboard, contentDescription = null)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xxs)) {
                Text(
                    text = stats.levelEstimation ?: "New Learner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Retention ${retention.retentionRate.toInt()}% • ${stats.learnedWords} words learned",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LearningBreakdownFallback(
    stats: ProgressStats,
    retention: RetentionStats
) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
        Text(
            text = "Learning Breakdown",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        BreakdownRow(
            title = "Words Reviewed",
            value = "${stats.learnedWords} learned",
            progress = (stats.learnedWords / 600f).coerceIn(0f, 1f)
        )
        BreakdownRow(
            title = "Accuracy",
            value = "${stats.accuracy.toInt()}%",
            progress = (stats.accuracy / 100.0).toFloat().coerceIn(0f, 1f)
        )
        BreakdownRow(
            title = "Retention",
            value = "${retention.retentionRate.toInt()}%",
            progress = (retention.retentionRate / 100.0).toFloat().coerceIn(0f, 1f)
        )
        Text(
            text = "Category breakdown is not supported by backend v1, so this section uses the available summary metrics.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    value: String,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

private val emptyStats = ProgressStats(
    learnedWords = 0,
    dueToday = 0,
    streak = 0,
    accuracy = 0.0,
    levelEstimation = null
)

private val emptyRetention = RetentionStats(
    retentionRate = 0.0,
    totalReviews = 0,
    retainedReviews = 0
)
