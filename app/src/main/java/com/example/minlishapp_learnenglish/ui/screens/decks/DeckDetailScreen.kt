package com.example.minlishapp_learnenglish.ui.screens.decks

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.presentation.viewmodel.decks.DeckDetailUiState
import com.example.minlishapp_learnenglish.ui.components.DeckHeroIcon
import com.example.minlishapp_learnenglish.ui.components.EmptyStateView
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.ReadOnlyBadge
import com.example.minlishapp_learnenglish.ui.components.TagChip
import com.example.minlishapp_learnenglish.ui.components.WordPreviewCard
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import androidx.compose.material.icons.outlined.FileUpload

@Composable
fun DeckDetailScreen(
    uiState: DeckDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddWord: () -> Unit,
    onEditWord: (Long) -> Unit,
    onImport: (String, ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            var fileName = "import.xlsx"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                onImport(fileName, bytes)
            }
        }
    }

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
                DeckDetailTopBar(
                    title = uiState.deck?.displayTitle ?: "Deck Detail",
                    onBack = onBack,
                    canEdit = uiState.deck?.isReadOnly == false && uiState.deck?.isSeed == false,
                    onImportClick = {
                        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                )
            }
            when {
                uiState.isLoading -> {
                    item {
                        LoadingStateView(message = "Đang tải deck...")
                    }
                }
                uiState.errorMessage != null && uiState.deck == null -> {
                    item {
                        ErrorStateView(
                            title = "Không tải được Deck Detail",
                            message = uiState.errorMessage,
                            onRetry = onRetry
                        )
                    }
                }
                uiState.deck != null -> {
                    item {
                        DeckDetailHero(deck = uiState.deck)
                    }
                    item {
                        DeckActionRow()
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vocabulary List",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.words.size} words",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (uiState.isEmpty) {
                        item {
                            EmptyStateView(
                                title = "Deck chưa có từ",
                                message = "Thêm từ sẽ được triển khai ở Phase 6."
                            )
                        }
                    } else {
                        items(uiState.words, key = { it.id }) { word ->
                            WordPreviewCard(
                                word = word,
                                canEdit = uiState.deck.isReadOnly.not() && uiState.deck.isSeed.not(),
                                onEditClick = { onEditWord(word.id) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.deck?.isReadOnly == false && uiState.deck.isSeed.not()) {
            FloatingActionButton(
                onClick = onAddWord,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MinLishSpacing.screenMargin),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add word")
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
private fun DeckDetailTopBar(
    title: String,
    onBack: () -> Unit,
    canEdit: Boolean = false,
    onImportClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (canEdit) {
            IconButton(onClick = onImportClick) {
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = "Import Excel")
            }
        }
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
        }
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
        }
    }
}

@Composable
private fun DeckDetailHero(deck: VocabularyDeck) {
    MinLishCard(tonal = false) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deck.displayTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (deck.isReadOnly || deck.isSeed) {
                        ReadOnlyBadge()
                    }
                }
                Text(
                    text = deck.displayDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)) {
                    DeckMetric(label = "Total Words", value = deck.wordCount.toString())
                    DeckMetric(label = "Mastered", value = "0")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                    deck.tags.take(3).forEach { tag -> TagChip(text = "#$tag") }
                }
            }
            DeckHeroIcon()
        }
    }
}

@Composable
private fun DeckMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DeckActionRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
    ) {
        DeckActionCard(
            title = "Learn New Words",
            icon = Icons.Outlined.PlayCircle,
            modifier = Modifier.weight(1f)
        )
        DeckActionCard(
            title = "Review Due",
            icon = Icons.Outlined.History,
            modifier = Modifier.weight(1f),
            tonal = true
        )
    }
}

@Composable
private fun DeckActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    tonal: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (tonal) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (tonal) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
        border = if (tonal) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)) else null
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
