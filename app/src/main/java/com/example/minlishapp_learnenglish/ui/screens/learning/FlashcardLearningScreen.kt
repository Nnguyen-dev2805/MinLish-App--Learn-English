package com.example.minlishapp_learnenglish.ui.screens.learning

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.minlishapp_learnenglish.core.audio.SimpleAudioPlayer
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.presentation.viewmodel.learning.FlashcardUiState
import com.example.minlishapp_learnenglish.ui.components.EmptyStateView
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishOutlinedButton
import com.example.minlishapp_learnenglish.ui.components.RemoteMediaImage
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun FlashcardLearningScreen(
    uiState: FlashcardUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onShowAnswer: () -> Unit,
    onPreviousCard: () -> Unit,
    onNextCard: () -> Unit,
    onRating: (ReviewRating) -> Unit,
    onAudioError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingStateView(message = "Loading review cards...")
            }
        }
        uiState.errorMessage != null && uiState.cards.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                contentAlignment = Alignment.Center
            ) {
                ErrorStateView(
                    title = "Unable to load lesson",
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
        }
        uiState.isEmpty -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No cards due",
                    message = "You are all caught up. New review cards will appear here when they are due."
                )
            }
        }
        uiState.isCompleted -> {
            SessionCompleteState(
                uiState = uiState,
                onBack = onBack,
                modifier = modifier
            )
        }
        else -> {
            FlashcardContent(
                uiState = uiState,
                onBack = onBack,
                onShowAnswer = onShowAnswer,
                onPreviousCard = onPreviousCard,
                onNextCard = onNextCard,
                onRating = onRating,
                onAudioError = onAudioError,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FlashcardContent(
    uiState: FlashcardUiState,
    onBack: () -> Unit,
    onShowAnswer: () -> Unit,
    onPreviousCard: () -> Unit,
    onNextCard: () -> Unit,
    onRating: (ReviewRating) -> Unit,
    onAudioError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val card = uiState.currentCard ?: return
    val context = LocalContext.current
    val audioPlayer = remember(context) { SimpleAudioPlayer(context) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MinLishSpacing.screenMargin, vertical = MinLishSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
    ) {
        FlashcardTopBar(onBack = onBack)
        SessionProgress(
            current = uiState.currentPosition,
            total = uiState.cards.size,
            progress = uiState.progressFraction
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FlashcardView(
                card = card,
                isAnswerVisible = uiState.isAnswerVisible,
                onToggleAnswer = onShowAnswer,
                audioPlayer = audioPlayer,
                onAudioError = onAudioError,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .aspectRatio(4f / 5f)
            )
        }
        CardNavigationRow(
            canGoPrevious = uiState.currentIndex > 0 && !uiState.isSubmitting,
            canGoNext = uiState.currentIndex < uiState.cards.lastIndex && !uiState.isSubmitting,
            onPreviousCard = onPreviousCard,
            onNextCard = onNextCard
        )
        FlashcardActions(
            isAnswerVisible = uiState.isAnswerVisible,
            isSubmitting = uiState.isSubmitting,
            onRating = onRating
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
    }
}

@Composable
private fun CardNavigationRow(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousCard: () -> Unit,
    onNextCard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        MinLishOutlinedButton(
            text = "Previous",
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            onClick = onPreviousCard,
            enabled = canGoPrevious,
            modifier = Modifier.weight(1f)
        )
        MinLishOutlinedButton(
            text = "Next",
            icon = Icons.AutoMirrored.Outlined.ArrowForward,
            onClick = onNextCard,
            enabled = canGoNext,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FlashcardTopBar(onBack: () -> Unit) {
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
                    text = "Learning Session",
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
private fun SessionProgress(
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
                text = "Session Progress",
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
private fun FlashcardView(
    card: ReviewCard,
    isAnswerVisible: Boolean,
    onToggleAnswer: () -> Unit,
    audioPlayer: SimpleAudioPlayer,
    onAudioError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(enabled = true) { onToggleAnswer() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Crossfade(
            targetState = isAnswerVisible,
            label = "flashcard-face"
        ) { answerVisible ->
            if (answerVisible) {
                FlashcardBack(
                    card = card,
                    audioPlayer = audioPlayer,
                    onAudioError = onAudioError
                )
            } else {
                FlashcardFront(
                    card = card,
                    audioPlayer = audioPlayer,
                    onAudioError = onAudioError
                )
            }
        }
    }
}

@Composable
private fun FlashcardFront(
    card: ReviewCard,
    audioPlayer: SimpleAudioPlayer,
    onAudioError: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (card.wordAudioUrl != null) {
            MediaIconButton(
                onClick = {
                    playWordAudio(
                        card = card,
                        audioPlayer = audioPlayer,
                        onAudioError = onAudioError
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(MinLishSpacing.lg)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MinLishSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.tertiaryFixed,
                contentColor = MaterialTheme.colorScheme.onTertiaryFixed
            ) {
                Text(
                    text = if (card.isNew) "NEW WORD" else "REVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(MinLishSpacing.md))
            Text(
                text = card.word,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            card.pronunciation?.takeIf { it.isNotBlank() }?.let { pronunciation ->
                Spacer(modifier = Modifier.height(MinLishSpacing.xs))
                Text(
                    text = pronunciation,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(MinLishSpacing.lg))
            Surface(
                modifier = Modifier.size(width = 64.dp, height = 4.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}
            Spacer(modifier = Modifier.height(MinLishSpacing.md))
            Text(
                text = "Tap card to see meaning",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FlashcardBack(
    card: ReviewCard,
    audioPlayer: SimpleAudioPlayer,
    onAudioError: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (card.wordAudioUrl != null) {
            MediaIconButton(
                onClick = {
                    playWordAudio(
                        card = card,
                        audioPlayer = audioPlayer,
                        onAudioError = onAudioError
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(MinLishSpacing.lg)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MinLishSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
        ) {
            Text(
                text = "Definition",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = card.meaning,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            card.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
            card.example?.takeIf { it.isNotBlank() }?.let { example ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(MinLishSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
                    ) {
                        Text(
                            text = "EXAMPLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = example,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            card.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                RemoteMediaImage(
                    imageUrl = imageUrl,
                    contentDescription = card.word,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
            Text(
                text = "Tap card to see word",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MediaIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = "Play audio"
            )
        }
    }
}

@Composable
private fun FlashcardActions(
    isAnswerVisible: Boolean,
    isSubmitting: Boolean,
    onRating: (ReviewRating) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        if (isAnswerVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
            ) {
                RatingButton(
                    label = "AGAIN",
                    hint = "<1m",
                    rating = ReviewRating.Again,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    enabled = !isSubmitting,
                    onRating = onRating,
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    label = "HARD",
                    hint = "2d",
                    rating = ReviewRating.Hard,
                    containerColor = MaterialTheme.colorScheme.tertiaryFixed,
                    contentColor = MaterialTheme.colorScheme.onTertiaryFixed,
                    enabled = !isSubmitting,
                    onRating = onRating,
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    label = "GOOD",
                    hint = "4d",
                    rating = ReviewRating.Good,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    enabled = !isSubmitting,
                    onRating = onRating,
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    label = "EASY",
                    hint = "7d",
                    rating = ReviewRating.Easy,
                    containerColor = MaterialTheme.colorScheme.primaryFixed,
                    contentColor = MaterialTheme.colorScheme.onPrimaryFixed,
                    enabled = !isSubmitting,
                    onRating = onRating,
                    modifier = Modifier.weight(1f)
                )
            }
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun playWordAudio(
    card: ReviewCard,
    audioPlayer: SimpleAudioPlayer,
    onAudioError: (String) -> Unit
) {
    val resolvedUrl = card.wordAudioUrl.asPlayableMediaUrlOrNull()
    if (resolvedUrl == null) {
        onAudioError("Unable to play audio.")
        return
    }
    audioPlayer.play(resolvedUrl) {
        onAudioError("Unable to play audio.")
    }
}

private fun String?.asPlayableMediaUrlOrNull(): String? {
    val trimmed = this?.trim().orEmpty()
    return trimmed.takeIf {
        it.startsWith("asset://", ignoreCase = true) ||
        it.startsWith("http://", ignoreCase = true) ||
            it.startsWith("https://", ignoreCase = true)
    }
}

@Composable
private fun RatingButton(
    label: String,
    hint: String,
    rating: ReviewRating,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onRating: (ReviewRating) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled) { onRating(rating) },
        shape = RoundedCornerShape(18.dp),
        color = containerColor.copy(alpha = if (enabled) 1f else 0.55f),
        contentColor = contentColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun SessionCompleteState(
    uiState: FlashcardUiState,
    onBack: () -> Unit,
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
            color = MaterialTheme.colorScheme.primaryFixed,
            contentColor = MaterialTheme.colorScheme.onPrimaryFixed,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier.padding(MinLishSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Session Complete",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${uiState.summary.reviewedCards}/${uiState.summary.totalCards} cards reviewed • ${uiState.summary.accuracy}% accuracy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                MinLishButton(
                    text = "Back to app",
                    icon = Icons.Outlined.School,
                    onClick = onBack
                )
            }
        }
    }
}
