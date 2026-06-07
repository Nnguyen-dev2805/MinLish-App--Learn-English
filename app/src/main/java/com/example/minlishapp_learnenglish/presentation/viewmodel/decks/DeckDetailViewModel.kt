package com.example.minlishapp_learnenglish.presentation.viewmodel.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeckDetailUiState(
    val isLoading: Boolean = true,
    val deck: VocabularyDeck? = null,
    val words: List<VocabularyWord> = emptyList(),
    val errorMessage: String? = null,
    val isExporting: Boolean = false,
    val exportFileName: String? = null,
    val exportFileBytes: ByteArray? = null
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && words.isEmpty()
}

sealed interface DeckDetailEvent {
    data object Retry : DeckDetailEvent
    data object BackClicked : DeckDetailEvent
    data object AddWordClicked : DeckDetailEvent
    data class EditWordClicked(val wordId: Long) : DeckDetailEvent
}

sealed interface DeckDetailEffect {
    data object NavigateBack : DeckDetailEffect
    data class NavigateAddWord(val deckId: Long) : DeckDetailEffect
    data class NavigateEditWord(val deckId: Long, val wordId: Long) : DeckDetailEffect
    data class ShowSnackbar(val message: String) : DeckDetailEffect
}

class DeckDetailViewModel(
    private val deckId: Long,
    private val deckRepository: DeckRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeckDetailUiState())
    val uiState: StateFlow<DeckDetailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DeckDetailEffect>()
    val effects: SharedFlow<DeckDetailEffect> = _effects.asSharedFlow()

    init {
        loadDeck()
    }

    fun onEvent(event: DeckDetailEvent) {
        when (event) {
            DeckDetailEvent.Retry -> loadDeck()
            DeckDetailEvent.BackClicked -> emitEffect(DeckDetailEffect.NavigateBack)
            DeckDetailEvent.AddWordClicked -> handleAddWord()
            is DeckDetailEvent.EditWordClicked -> handleEditWord(event.wordId)
        }
    }

    fun loadDeck() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val deckResult = deckRepository.getDeck(deckId)) {
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = deckResult.error.message)
                    }
                }

                is AppResult.Success -> {
                    when (val wordsResult = deckRepository.getDeckItems(deckId)) {
                        is AppResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    deck = deckResult.data,
                                    errorMessage = wordsResult.error.message
                                )
                            }
                        }

                        is AppResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    deck = deckResult.data,
                                    words = wordsResult.data,
                                    errorMessage = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleAddWord() {
        val deck = _uiState.value.deck
        if (deck == null || deck.isReadOnly || deck.isSeed) {
            emitEffect(DeckDetailEffect.ShowSnackbar("Seed decks are read-only."))
            return
        }
        emitEffect(DeckDetailEffect.NavigateAddWord(deck.id))
    }

    private fun handleEditWord(wordId: Long) {
        val deck = _uiState.value.deck
        if (deck == null || deck.isReadOnly || deck.isSeed) {
            emitEffect(DeckDetailEffect.ShowSnackbar("Seed decks are read-only."))
            return
        }
        emitEffect(DeckDetailEffect.NavigateEditWord(deck.id, wordId))
    }

    private fun emitEffect(effect: DeckDetailEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    fun importExcel(fileName: String, fileBytes: ByteArray) {
        val deck = _uiState.value.deck
        if (deck == null || deck.isReadOnly || deck.isSeed) {
            emitEffect(DeckDetailEffect.ShowSnackbar("Seed decks are read-only."))
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = deckRepository.importDeckItems(deckId, fileName, fileBytes)) {
                is AppResult.Success -> {
                    emitEffect(DeckDetailEffect.ShowSnackbar("Imported ${result.data} words successfully."))
                    loadDeck() // Reload to show new words
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.error.message) }
                    emitEffect(DeckDetailEffect.ShowSnackbar("Import failed: ${result.error.message}"))
                }
            }
        }
    }

    fun exportExcel() {
        val deck = _uiState.value.deck
        if (deck == null || deck.isReadOnly || deck.isSeed) {
            emitEffect(DeckDetailEffect.ShowSnackbar("Seed decks are read-only."))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            when (val result = deckRepository.exportDeckItems(deck.id)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportFileName = "${deck.safeFileName()}_vocabulary.xlsx",
                            exportFileBytes = result.data
                        )
                    }
                }

                is AppResult.Failure -> {
                    _uiState.update { it.copy(isExporting = false) }
                    emitEffect(DeckDetailEffect.ShowSnackbar("Export failed: ${result.error.message}"))
                }
            }
        }
    }

    fun onExportSaved(success: Boolean) {
        _uiState.update {
            it.copy(exportFileName = null, exportFileBytes = null)
        }
        emitEffect(
            DeckDetailEffect.ShowSnackbar(
                if (success) "Exported deck successfully." else "Export canceled."
            )
        )
    }

    private fun VocabularyDeck.safeFileName(): String {
        return displayTitle
            .trim()
            .replace(Regex("[^A-Za-z0-9_-]+"), "_")
            .trim('_')
            .ifBlank { "deck" }
    }
}
