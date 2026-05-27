package com.example.minlishapp_learnenglish.presentation.viewmodel.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import com.example.minlishapp_learnenglish.domain.usecase.decks.CreateWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.DeleteWordUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.GetDeckItemsUseCase
import com.example.minlishapp_learnenglish.domain.usecase.decks.UpdateWordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordEditorUiState(
    val deckId: Long,
    val itemId: Long? = null,
    val isLoadingInitial: Boolean = itemId != null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val description: String = "",
    val example: String = "",
    val collocation: String = "",
    val relatedWordsText: String = "",
    val note: String = "",
    val wordError: String? = null,
    val meaningError: String? = null,
    val apiError: String? = null
) {
    val isEditMode: Boolean = itemId != null
}

sealed interface WordEditorEvent {
    data class WordChanged(val value: String) : WordEditorEvent
    data class PronunciationChanged(val value: String) : WordEditorEvent
    data class MeaningChanged(val value: String) : WordEditorEvent
    data class DescriptionChanged(val value: String) : WordEditorEvent
    data class ExampleChanged(val value: String) : WordEditorEvent
    data class CollocationChanged(val value: String) : WordEditorEvent
    data class RelatedWordsChanged(val value: String) : WordEditorEvent
    data class NoteChanged(val value: String) : WordEditorEvent
    data object Submit : WordEditorEvent
    data object Delete : WordEditorEvent
    data object BackClicked : WordEditorEvent
    data object RetryLoad : WordEditorEvent
}

sealed interface WordEditorEffect {
    data object NavigateBack : WordEditorEffect
    data class NavigateBackWithRefresh(val message: String) : WordEditorEffect
    data class ShowSnackbar(val message: String) : WordEditorEffect
}

class WordEditorViewModel(
    private val deckId: Long,
    private val itemId: Long?,
    private val getDeckItemsUseCase: GetDeckItemsUseCase,
    private val createWordUseCase: CreateWordUseCase,
    private val updateWordUseCase: UpdateWordUseCase,
    private val deleteWordUseCase: DeleteWordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        WordEditorUiState(deckId = deckId, itemId = itemId)
    )
    val uiState: StateFlow<WordEditorUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<WordEditorEffect>()
    val effects: SharedFlow<WordEditorEffect> = _effects.asSharedFlow()

    init {
        if (itemId != null) {
            loadWordForEdit()
        }
    }

    fun onEvent(event: WordEditorEvent) {
        when (event) {
            is WordEditorEvent.WordChanged -> _uiState.update {
                it.copy(word = event.value, wordError = null, apiError = null)
            }
            is WordEditorEvent.PronunciationChanged -> _uiState.update {
                it.copy(pronunciation = event.value, apiError = null)
            }
            is WordEditorEvent.MeaningChanged -> _uiState.update {
                it.copy(meaning = event.value, meaningError = null, apiError = null)
            }
            is WordEditorEvent.DescriptionChanged -> _uiState.update {
                it.copy(description = event.value, apiError = null)
            }
            is WordEditorEvent.ExampleChanged -> _uiState.update {
                it.copy(example = event.value, apiError = null)
            }
            is WordEditorEvent.CollocationChanged -> _uiState.update {
                it.copy(collocation = event.value, apiError = null)
            }
            is WordEditorEvent.RelatedWordsChanged -> _uiState.update {
                it.copy(relatedWordsText = event.value, apiError = null)
            }
            is WordEditorEvent.NoteChanged -> _uiState.update {
                it.copy(note = event.value, apiError = null)
            }
            WordEditorEvent.Submit -> submit()
            WordEditorEvent.Delete -> delete()
            WordEditorEvent.BackClicked -> emitEffect(WordEditorEffect.NavigateBack)
            WordEditorEvent.RetryLoad -> loadWordForEdit()
        }
    }

    private fun loadWordForEdit() {
        val editingItemId = itemId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInitial = true, apiError = null) }
            when (val result = getDeckItemsUseCase(deckId)) {
                is AppResult.Success -> {
                    val word = result.data.firstOrNull { it.id == editingItemId }
                    if (word == null) {
                        _uiState.update {
                            it.copy(
                                isLoadingInitial = false,
                                apiError = "Không tìm thấy từ vựng để chỉnh sửa."
                            )
                        }
                    } else {
                        _uiState.update { it.populateFrom(word).copy(isLoadingInitial = false) }
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoadingInitial = false, apiError = result.error.message)
                    }
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val wordError = if (state.word.isBlank()) "Word không được để trống." else null
        val meaningError = if (state.meaning.isBlank()) "Meaning không được để trống." else null
        if (wordError != null || meaningError != null) {
            _uiState.update {
                it.copy(wordError = wordError, meaningError = meaningError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, apiError = null) }
            val result = if (state.itemId == null) {
                createWordUseCase(
                    deckId = state.deckId,
                    word = state.word,
                    pronunciation = state.pronunciation,
                    meaning = state.meaning,
                    description = state.description,
                    example = state.example,
                    collocation = state.collocation,
                    relatedWords = parseRelatedWords(state.relatedWordsText),
                    note = state.note
                )
            } else {
                updateWordUseCase(
                    itemId = state.itemId,
                    word = state.word,
                    pronunciation = state.pronunciation,
                    meaning = state.meaning,
                    description = state.description,
                    example = state.example,
                    collocation = state.collocation,
                    relatedWords = parseRelatedWords(state.relatedWordsText),
                    note = state.note
                )
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    val message = if (state.itemId == null) {
                        "Đã thêm từ mới."
                    } else {
                        "Đã cập nhật từ vựng."
                    }
                    _effects.emit(WordEditorEffect.NavigateBackWithRefresh(message))
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isSaving = false, apiError = result.error.message)
                    }
                    _effects.emit(WordEditorEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun delete() {
        val editingItemId = itemId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, apiError = null) }
            when (val result = deleteWordUseCase(editingItemId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _effects.emit(WordEditorEffect.NavigateBackWithRefresh("Đã xoá từ vựng."))
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isDeleting = false, apiError = result.error.message)
                    }
                    _effects.emit(WordEditorEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun parseRelatedWords(value: String): List<String> {
        return value.split(',', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun emitEffect(effect: WordEditorEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    private fun WordEditorUiState.populateFrom(word: VocabularyWord): WordEditorUiState {
        return copy(
            word = word.word,
            pronunciation = word.pronunciation.orEmpty(),
            meaning = word.meaning,
            description = word.description.orEmpty(),
            example = word.example.orEmpty(),
            collocation = word.collocation.orEmpty(),
            relatedWordsText = word.relatedWords.joinToString(", "),
            note = word.note.orEmpty(),
            wordError = null,
            meaningError = null,
            apiError = null
        )
    }
}
