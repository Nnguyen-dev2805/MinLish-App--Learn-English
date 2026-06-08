package com.example.minlishapp_learnenglish.ui.screens.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Quiz
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.viewModel.learning.ReviewDueChoice
import com.example.minlishapp_learnenglish.viewModel.learning.ReviewDueUiState
import com.example.minlishapp_learnenglish.ui.components.EmptyStateView
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun ReviewDueScreen(
    uiState: ReviewDueUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onChoiceSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingStateView(message = "Loading due words...")
            }
        }
        uiState.errorMessage != null && uiState.cards.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                ReviewDueTopBar(onBack = onBack)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ErrorStateView(
                        title = "Unable to load review",
                        message = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
            }
        }
        uiState.isEmpty -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                ReviewDueTopBar(onBack = onBack)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        title = "No words due for review",
                        message = "You are all caught up. Due words will appear here later."
                    )
                }
            }
        }
        else -> {
            ReviewDueContent(
                uiState = uiState,
                onBack = onBack,
                onChoiceSelected = onChoiceSelected,
                onSubmitAnswer = onSubmitAnswer,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ReviewDueContent(
    uiState: ReviewDueUiState,
    onBack: () -> Unit,
    onChoiceSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = uiState.currentCard ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = MinLishSpacing.screenMargin, vertical = MinLishSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
    ) {
        ReviewDueTopBar(onBack = onBack)
        ReviewDueProgress(
            current = uiState.currentPosition,
            total = uiState.summary.totalCards,
            progress = uiState.progressFraction
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(MinLishSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.tertiaryFixed,
                    contentColor = MaterialTheme.colorScheme.onTertiaryFixed
                ) {
                    Text(
                        text = "REVIEW QUIZ",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = card.word,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                card.pronunciation?.takeIf { it.isNotBlank() }?.let { pronunciation ->
                    Text(
                        text = pronunciation,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "Choose the correct meaning",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
                ) {
                    uiState.choices.forEachIndexed { index, choice ->
                        ReviewChoiceRow(
                            choice = choice,
                            label = ('A' + index).toString(),
                            selected = uiState.selectedChoiceIndex == index,
                            enabled = !uiState.isSubmitting,
                            onClick = { onChoiceSelected(index) }
                        )
                    }
                }
            }
        }
        MinLishButton(
            text = "Submit Answer",
            icon = Icons.Outlined.CheckCircle,
            loading = uiState.isSubmitting,
            enabled = uiState.selectedChoiceIndex != null && !uiState.isSubmitting,
            onClick = onSubmitAnswer,
            modifier = Modifier.fillMaxWidth()
        )
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(MinLishSpacing.sm))
    }
}

@Composable
private fun ReviewDueTopBar(onBack: () -> Unit) {
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
                    Icon(imageVector = Icons.Outlined.Quiz, contentDescription = null)
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
                    text = "Review Due",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }
}

@Composable
private fun ReviewDueProgress(
    current: Int,
    total: Int,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Review Progress",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$current / $total",
                style = MaterialTheme.typography.labelLarge,
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
            trackColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
private fun ReviewChoiceRow(
    choice: ReviewDueChoice,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(MinLishSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
