package com.example.minlishapp_learnenglish.ui.screens.decks

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.WordEditorUiState
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishOutlinedButton
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun WordEditorScreen(
    uiState: WordEditorUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onWordChange: (String) -> Unit,
    onPronunciationChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onCollocationChange: (String) -> Unit,
    onRelatedWordsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WordEditorTopBar(onBack = onBack)
            when {
                uiState.isLoadingInitial -> {
                    LoadingStateView(
                        message = "Loading vocabulary word...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MinLishSpacing.screenMargin)
                    )
                }

                uiState.apiError != null && uiState.isEditMode && uiState.word.isBlank() -> {
                    ErrorStateView(
                        title = "Unable to load vocabulary word",
                        message = uiState.apiError,
                        onRetry = onRetryLoad,
                        modifier = Modifier.padding(MinLishSpacing.screenMargin)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = MinLishSpacing.screenMargin),
                        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
                    ) {
                        WordEditorHeader(isEditMode = uiState.isEditMode)
                        WordEditorForm(
                            uiState = uiState,
                            onWordChange = onWordChange,
                            onPronunciationChange = onPronunciationChange,
                            onMeaningChange = onMeaningChange,
                            onDescriptionChange = onDescriptionChange,
                            onExampleChange = onExampleChange,
                            onCollocationChange = onCollocationChange,
                            onRelatedWordsChange = onRelatedWordsChange,
                            onNoteChange = onNoteChange
                        )
                        StatsNote(isEditMode = uiState.isEditMode)
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }

        if (!uiState.isLoadingInitial && (uiState.apiError == null || uiState.word.isNotBlank())) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier.padding(MinLishSpacing.screenMargin),
                    verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
                ) {
                    if (uiState.apiError != null) {
                        Text(
                            text = uiState.apiError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (uiState.isEditMode) {
                        MinLishOutlinedButton(
                            text = "Delete Word",
                            icon = Icons.Outlined.Delete,
                            onClick = onDelete,
                            enabled = !uiState.isSaving && !uiState.isDeleting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MinLishButton(
                        text = if (uiState.isEditMode) "Save Changes" else "Save Word",
                        icon = Icons.Outlined.Save,
                        onClick = onSubmit,
                        loading = uiState.isSaving || uiState.isDeleting,
                        enabled = !uiState.isDeleting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WordEditorTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MinLishSpacing.screenMargin, vertical = MinLishSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "MinLish",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WordEditorHeader(isEditMode: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
        Text(
            text = if (isEditMode) "Edit Word" else "Add New Word",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isEditMode) {
                "Update context and examples for this vocabulary item."
            } else {
                "Build your personal vocabulary deck with detailed context."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WordEditorForm(
    uiState: WordEditorUiState,
    onWordChange: (String) -> Unit,
    onPronunciationChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onCollocationChange: (String) -> Unit,
    onRelatedWordsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            DeckContextCard(deckId = uiState.deckId)
            MinLishTextField(
                value = uiState.word,
                onValueChange = onWordChange,
                label = "Word",
                modifier = Modifier.fillMaxWidth(),
                supportingText = uiState.wordError,
                isError = uiState.wordError != null
            )
            MinLishTextField(
                value = uiState.pronunciation,
                onValueChange = onPronunciationChange,
                label = "Pronunciation",
                modifier = Modifier.fillMaxWidth(),
                supportingText = "Audio upload is not supported in backend v1"
            )
            MinLishTextField(
                value = uiState.meaning,
                onValueChange = onMeaningChange,
                label = "Meaning (Vietnamese)",
                modifier = Modifier.fillMaxWidth(),
                supportingText = uiState.meaningError,
                isError = uiState.meaningError != null
            )
            MinLishTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "English Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            MinLishTextField(
                value = uiState.example,
                onValueChange = onExampleChange,
                label = "Examples",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            MinLishTextField(
                value = uiState.collocation,
                onValueChange = onCollocationChange,
                label = "Collocations",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            MinLishTextField(
                value = uiState.relatedWordsText,
                onValueChange = onRelatedWordsChange,
                label = "Related Words",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                supportingText = "Separate with commas or line breaks"
            )
            MinLishTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = "Personal Note",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        }
    }
}

@Composable
private fun DeckContextCard(deckId: Long) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MinLishSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.padding(MinLishSpacing.xs)
                )
            }
            Column {
                Text(
                    text = "Target Deck",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Deck ID #$deckId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsNote(isEditMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
        ) {
            Text(
                text = if (isEditMode) "Word Detail" else "Your Stats",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isEditMode) {
                    "Changes will be saved to your personal deck."
                } else {
                    "Adding this word will make your custom deck ready for future learning sessions."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
