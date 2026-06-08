package com.example.minlishapp_learnenglish.ui.screens.decks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.PlayCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.viewModel.decks.DeckDetailUiState
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
import android.provider.OpenableColumns
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun DeckDetailScreen(
    uiState: DeckDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLearnDeck: (Long) -> Unit,
    onAddWord: () -> Unit,
    onEditWord: (Long) -> Unit,
    onImport: (String, ByteArray) -> Unit,
    onExport: () -> Unit,
    onExportSaved: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            var fileName = "import.csv"
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
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "text/csv"
        )
    ) { uri ->
        val fileBytes = uiState.exportFileBytes
        if (uri == null || fileBytes == null) {
            if (fileBytes != null) onExportSaved(false)
            return@rememberLauncherForActivityResult
        }
        val saved = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(fileBytes)
            } ?: error("Cannot open export file.")
        }.isSuccess
        onExportSaved(saved)
    }

    LaunchedEffect(uiState.exportFileBytes) {
        val fileName = uiState.exportFileName
        if (fileName != null && uiState.exportFileBytes != null) {
            exportFileLauncher.launch(fileName)
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
                    onBack = onBack
                )
            }
            when {
                uiState.isLoading -> {
                    item {
                        LoadingStateView(message = "Loading deck...")
                    }
                }
                uiState.errorMessage != null && uiState.deck == null -> {
                    item {
                        ErrorStateView(
                            title = "Unable to load deck detail",
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
                        DeckActionRow(
                            onLearnDeck = { onLearnDeck(uiState.deck.id) }
                        )
                    }
                    if (uiState.deck.isReadOnly.not() && uiState.deck.isSeed.not()) {
                        item {
                            DeckImportExportRow(
                                isExporting = uiState.isExporting,
                                onImportClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                onExportClick = onExport
                            )
                        }
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
                                title = "No words in this deck",
                                message = "Add a word to start building this deck."
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
    onBack: () -> Unit
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
    }
}

@Composable
private fun DeckImportExportRow(
    isExporting: Boolean,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        DeckSmallActionButton(
            title = "Import CSV",
            icon = Icons.Outlined.FileUpload,
            onClick = onImportClick,
            modifier = Modifier.weight(1f)
        )
        DeckSmallActionButton(
            title = if (isExporting) "Exporting..." else "Export CSV",
            icon = Icons.Outlined.FileDownload,
            onClick = onExportClick,
            enabled = !isExporting,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DeckSmallActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(vertical = MinLishSpacing.sm, horizontal = MinLishSpacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = title,
                modifier = Modifier.padding(start = MinLishSpacing.xs),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    DeckMetric(label = "Learned", value = deck.learnedCount.toString())
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)
                ) {
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
private fun DeckActionRow(onLearnDeck: () -> Unit) {
    DeckActionCard(
        title = "Learn New Words",
        icon = Icons.Outlined.PlayCircle,
        onClick = onLearnDeck,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeckActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = MinLishSpacing.lg),
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
