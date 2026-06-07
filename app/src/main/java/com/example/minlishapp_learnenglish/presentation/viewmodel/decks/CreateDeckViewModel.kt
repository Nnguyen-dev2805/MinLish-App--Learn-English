package com.example.minlishapp_learnenglish.presentation.viewmodel.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateDeckUiState(
    val name: String = "",
    val description: String = "",
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val apiError: String? = null
)

sealed interface CreateDeckEvent {
    data class NameChanged(val value: String) : CreateDeckEvent
    data class DescriptionChanged(val value: String) : CreateDeckEvent
    data class TagInputChanged(val value: String) : CreateDeckEvent
    data class RemoveTag(val tag: String) : CreateDeckEvent
    data class SuggestedTagSelected(val tag: String) : CreateDeckEvent
    data object AddTag : CreateDeckEvent
    data object Submit : CreateDeckEvent
    data object BackClicked : CreateDeckEvent
}

sealed interface CreateDeckEffect {
    data object NavigateBack : CreateDeckEffect
    data class NavigateDeckDetail(val deckId: Long) : CreateDeckEffect
    data class ShowSnackbar(val message: String) : CreateDeckEffect
}

class CreateDeckViewModel(
    private val deckRepository: DeckRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateDeckUiState())
    val uiState: StateFlow<CreateDeckUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CreateDeckEffect>()
    val effects: SharedFlow<CreateDeckEffect> = _effects.asSharedFlow()

    fun onEvent(event: CreateDeckEvent) {
        when (event) {
            is CreateDeckEvent.NameChanged -> _uiState.update {
                it.copy(name = event.value, nameError = null, apiError = null)
            }
            is CreateDeckEvent.DescriptionChanged -> _uiState.update {
                it.copy(description = event.value, apiError = null)
            }
            is CreateDeckEvent.TagInputChanged -> _uiState.update {
                it.copy(tagInput = event.value)
            }
            CreateDeckEvent.AddTag -> addCurrentTag()
            is CreateDeckEvent.RemoveTag -> _uiState.update {
                it.copy(tags = it.tags.filterNot { tag -> tag.equals(event.tag, ignoreCase = true) })
            }
            is CreateDeckEvent.SuggestedTagSelected -> addTag(event.tag)
            CreateDeckEvent.Submit -> submit()
            CreateDeckEvent.BackClicked -> emitEffect(CreateDeckEffect.NavigateBack)
        }
    }

    private fun addCurrentTag() {
        val tag = _uiState.value.tagInput
        addTag(tag)
        _uiState.update { it.copy(tagInput = "") }
    }

    private fun addTag(rawTag: String) {
        val normalizedTag = rawTag.trim().removePrefix("#")
        if (normalizedTag.isEmpty()) return
        _uiState.update { state ->
            if (state.tags.any { it.equals(normalizedTag, ignoreCase = true) }) {
                state.copy(tagInput = "")
            } else {
                state.copy(tags = state.tags + normalizedTag, tagInput = "")
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Deck name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null, nameError = null) }
            when (
                val result = deckRepository.createDeck(
                    name = state.name,
                    description = state.description,
                    tags = state.tags
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(CreateDeckEffect.NavigateDeckDetail(result.data.id))
                }

                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, apiError = result.error.message)
                    }
                    _effects.emit(CreateDeckEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun emitEffect(effect: CreateDeckEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
