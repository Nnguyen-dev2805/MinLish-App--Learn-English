package com.example.minlishapp_learnenglish.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.presentation.viewmodel.home.HomeUiState
import com.example.minlishapp_learnenglish.ui.components.DailyPlanCard
import com.example.minlishapp_learnenglish.ui.components.InsightCard
import com.example.minlishapp_learnenglish.ui.components.MiniActivityChart
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartLearning: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading && uiState.dashboardStats == null -> HomeLoadingState(modifier)
        uiState.errorMessage != null && uiState.dashboardStats == null -> {
            HomeErrorState(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = modifier
            )
        }
        uiState.isEmpty -> {
            HomeEmptyState(
                onRefresh = onRefresh,
                modifier = modifier
            )
        }
        else -> {
            val stats = uiState.dashboardStats ?: fallbackStats
            val dailyPlan = uiState.dailyPlan ?: fallbackDailyPlan
            HomeContent(
                stats = stats,
                dailyPlan = dailyPlan,
                uiState = uiState,
                onStartLearning = onStartLearning,
                onRefresh = onRefresh,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeContent(
    stats: ProgressStats,
    dailyPlan: DailyLearningPlan,
    uiState: HomeUiState,
    onStartLearning: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        HomeHeader(
            name = uiState.userName,
            level = uiState.userLevel ?: stats.levelEstimation,
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )
        DailyPlanCard(
            dailyPlan = dailyPlan,
            onStartLearning = onStartLearning
        )
        HomeStatsRow(stats = stats)
        MiniActivityChart(activities = uiState.activities)
        Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            InsightCard(
                title = "Try Spaced Repetition",
                message = "Reviewing cards every 2 days boosts memory.",
                icon = Icons.Outlined.Lightbulb,
                modifier = Modifier.weight(1f)
            )
            InsightCard(
                title = "Word Clubs",
                message = "Community data is planned for a later phase.",
                icon = Icons.Outlined.Groups,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(MinLishSpacing.lg))
    }
}

@Composable
private fun HomeHeader(
    name: String,
    level: String?,
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
                        text = name.firstOrNull()?.uppercase() ?: "M",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column {
                Text(
                    text = "Hello, $name!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = level ?: "MinLish",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh dashboard"
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStatsRow(stats: ProgressStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        DashboardStatCard(
            title = "Day Streak",
            value = stats.streak.toString(),
            icon = Icons.Outlined.LocalFireDepartment,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        DashboardStatCard(
            title = "Learned",
            value = stats.learnedWords.toString(),
            icon = Icons.Outlined.CheckCircle,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            highlighted = true
        )
        DashboardStatCard(
            title = "Accuracy",
            value = "${stats.accuracy.toInt()}%",
            icon = Icons.Outlined.Insights,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (highlighted) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xxs)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HomeLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        HomeHeader(name = "Learner", level = null, isRefreshing = true, onRefresh = {})
        repeat(4) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (it == 0) 210.dp else 112.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Column(
                modifier = Modifier.padding(MinLishSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Không tải được Home Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                MinLishButton(text = "Thử lại", onClick = onRetry)
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(MinLishSpacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(MinLishSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Text(
                    text = "Chưa có dữ liệu học",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tạo deck hoặc bắt đầu học để dashboard có streak, activity và accuracy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                MinLishButton(text = "Làm mới", onClick = onRefresh)
            }
        }
    }
}

private val fallbackStats = ProgressStats(
    learnedWords = 0,
    dueToday = 0,
    streak = 0,
    accuracy = 0.0,
    levelEstimation = null
)

private val fallbackDailyPlan = DailyLearningPlan(
    dailyGoal = 10,
    newCards = 0,
    dueReviews = 0,
    totalAvailable = 0
)
