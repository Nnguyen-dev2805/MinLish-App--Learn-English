package com.example.minlishapp_learnenglish.ui.screens.decks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.viewModel.decks.DeckFilter
import com.example.minlishapp_learnenglish.viewModel.decks.DeckListUiState
import com.example.minlishapp_learnenglish.ui.components.DeckCard
import com.example.minlishapp_learnenglish.ui.components.EmptyStateView
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun DeckListScreen(
    uiState: DeckListUiState,
    snackbarHostState: SnackbarHostState,
    onSearchChange: (String) -> Unit,
    onFilterSelected: (DeckFilter) -> Unit,
    onDeckClick: (Long) -> Unit,
    onCreateDeck: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = MinLishSpacing.screenMargin,
                top = MinLishSpacing.md,
                end = MinLishSpacing.screenMargin,
                bottom = 96.dp
            )
        ) {
            item {
                DeckListTopBar()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                    Text(
                        text = "Vocabulary Decks",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Organize and master your language journey.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                DeckSearchField(
                    value = uiState.query,
                    onValueChange = onSearchChange
                )
            }
            item {
                DeckFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
            when {
                uiState.isLoading -> {
                    item {
                        LoadingStateView(
                            message = "Loading decks...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                uiState.errorMessage != null && uiState.decks.isEmpty() -> {
                    item {
                        ErrorStateView(
                            title = "Unable to load decks",
                            message = uiState.errorMessage,
                            onRetry = onRetry
                        )
                    }
                }
                uiState.isEmpty -> {
                    item {
                        EmptyStateView(
                            title = "No decks yet",
                            message = "Create your first deck or check backend seed data."
                        )
                    }
                }
                uiState.isSearchEmpty -> {
                    item {
                        EmptyStateView(
                            title = "No matching decks",
                            message = "Try searching by unit, tag, or another deck name."
                        )
                    }
                }
                else -> {
                    items(uiState.filteredDecks, key = { it.id }) { deck ->
                        DeckCard(
                            deck = deck,
                            onClick = { onDeckClick(deck.id) }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCreateDeck,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MinLishSpacing.screenMargin),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Create deck")
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DeckListTopBar() {
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
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "ML", style = MaterialTheme.typography.labelLarge)
                }
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
private fun DeckSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search your decks...") },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun DeckFilterRow(
    selectedFilter: DeckFilter,
    onFilterSelected: (DeckFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
    ) {
        DeckFilter.entries.forEach { filter ->
            DeckFilterChip(
                text = filter.label,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeckFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryFixed
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
