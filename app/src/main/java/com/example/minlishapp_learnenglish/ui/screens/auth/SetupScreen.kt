package com.example.minlishapp_learnenglish.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.SetupUiState
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileViewModel
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishLogo
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun SetupScreen(
    uiState: SetupUiState,
    snackbarHostState: SnackbarHostState,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MinLishSpacing.screenMargin)
                .padding(top = 32.dp, bottom = MinLishSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            MinLishLogo(iconSize = 56.dp, showText = true)

            Text(
                text = "Personalize your learning",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Choose a goal and level to set up your study plan.\nYou can update them later in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MinLishSpacing.xs))

            SetupBentoCard(
                selectedGoal = uiState.goal,
                onGoalChange = onGoalChange,
                selectedLevel = uiState.level,
                onLevelChange = onLevelChange
            )

            Spacer(modifier = Modifier.height(MinLishSpacing.sm))

            MinLishButton(
                text = "Continue",
                onClick = onSubmit,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = onSkip, enabled = !uiState.isLoading) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupBentoCard(
    selectedGoal: String,
    onGoalChange: (String) -> Unit,
    selectedLevel: String,
    onLevelChange: (String) -> Unit
) {
    MinLishCard(
        modifier = Modifier.fillMaxWidth(),
        tonal = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.School, null, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(
                        text = "Personalize Learning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose your goal and current level.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MinLishSpacing.xs))

            // Section 1: Learning Goal
            Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                Text(
                    text = "Learning Goal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileViewModel.goalOptions.forEach { goal ->
                        SetupChip(
                            text = goal,
                            selected = goal == selectedGoal,
                            onClick = { onGoalChange(goal) },
                            icon = Icons.Outlined.Flag
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MinLishSpacing.sm))

            // Section 2: Current Level
            Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                Text(
                    text = "Current Level",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileViewModel.levelOptions.forEach { level ->
                        SetupChip(
                            text = level,
                            selected = level == selectedLevel,
                            onClick = { onLevelChange(level) },
                            icon = Icons.Outlined.Psychology
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.03f else 1.0f, label = "ScaleAnimation")

    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "BgAnimation"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "TextColorAnimation"
    )
    val borderStroke = if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        modifier = Modifier
            .scale(scale)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
