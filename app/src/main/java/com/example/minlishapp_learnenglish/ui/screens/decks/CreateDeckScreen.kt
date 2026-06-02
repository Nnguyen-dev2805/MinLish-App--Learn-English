package com.example.minlishapp_learnenglish.ui.screens.decks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.CreateDeckUiState
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun CreateDeckScreen(
    uiState: CreateDeckUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onSuggestedTag: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreateDeckTopBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MinLishSpacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                    Text(
                        text = "Create New Deck",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Organize your vocabulary and customize your learning experience.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MinLishTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Deck Name",
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = uiState.nameError,
                    isError = uiState.nameError != null
                )
                MinLishTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = "Description",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                TagEditor(
                    tags = uiState.tags,
                    tagInput = uiState.tagInput,
                    onTagInputChange = onTagInputChange,
                    onAddTag = onAddTag,
                    onRemoveTag = onRemoveTag,
                    onSuggestedTag = onSuggestedTag
                )
                if (uiState.apiError != null) {
                    Text(
                        text = uiState.apiError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            MinLishButton(
                text = "Create Deck",
                onClick = onSubmit,
                icon = Icons.Outlined.Add,
                loading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MinLishSpacing.screenMargin)
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CreateDeckTopBar(onBack: () -> Unit) {
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
    }
}

@Composable
private fun TagEditor(
    tags: List<String>,
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onSuggestedTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
        Text(
            text = "Category & Tags",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MinLishSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
                ) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { onRemoveTag(tag) },
                            label = { Text(text = tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                MinLishTextField(
                    value = tagInput,
                    onValueChange = onTagInputChange,
                    label = "Add tag",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    supportingText = "Press Done or the + button to add a tag"
                )
                MinLishButton(
                    text = "Add Tag",
                    onClick = onAddTag,
                    icon = Icons.Outlined.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
        ) {
            listOf("Travel", "Grammar", "TOEIC", "Daily").forEach { tag ->
                TagSuggestion(tag = tag, onClick = { onSuggestedTag(tag) })
            }
        }
    }
}

@Composable
private fun TagSuggestion(tag: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Text(
            text = "#$tag",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
