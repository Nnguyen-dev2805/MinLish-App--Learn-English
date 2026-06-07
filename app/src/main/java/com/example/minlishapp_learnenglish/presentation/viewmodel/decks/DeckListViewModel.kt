package com.example.minlishapp_learnenglish.presentation.viewmodel.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeckListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val selectedFilter: DeckFilter = DeckFilter.All,
    val decks: List<VocabularyDeck> = emptyList(),
    val filteredDecks: List<VocabularyDeck> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && decks.isEmpty()
    val isSearchEmpty: Boolean = !isLoading &&
        errorMessage == null &&
        decks.isNotEmpty() &&
        filteredDecks.isEmpty()
}

sealed interface DeckListEvent {
    data class SearchChanged(val query: String) : DeckListEvent
    data class FilterSelected(val filter: DeckFilter) : DeckListEvent
    data class DeckSelected(val deckId: Long) : DeckListEvent
    data object CreateDeckClicked : DeckListEvent
    data object Retry : DeckListEvent
    data object Refresh : DeckListEvent
}

sealed interface DeckListEffect {
    data class NavigateDeckDetail(val deckId: Long) : DeckListEffect
    data object NavigateCreateDeck : DeckListEffect
    data class ShowSnackbar(val message: String) : DeckListEffect
}

enum class DeckFilter(val label: String) {
    All("All Decks"),
    Seed("Seed Decks"),
    Mine("My Decks")
}

class DeckListViewModel(
    private val deckRepository: DeckRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeckListUiState())
    val uiState: StateFlow<DeckListUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DeckListEffect>()
    val effects: SharedFlow<DeckListEffect> = _effects.asSharedFlow()

    init {
        loadDecks()
    }

    fun onEvent(event: DeckListEvent) {
        when (event) {
            is DeckListEvent.SearchChanged -> updateSearch(event.query)
            is DeckListEvent.FilterSelected -> updateFilter(event.filter)
            is DeckListEvent.DeckSelected -> emitEffect(DeckListEffect.NavigateDeckDetail(event.deckId))
            DeckListEvent.CreateDeckClicked -> emitEffect(DeckListEffect.NavigateCreateDeck)
            DeckListEvent.Refresh -> loadDecks(isRefresh = true)
            DeckListEvent.Retry -> loadDecks(isRefresh = false)
        }
    }

    fun loadDecks(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.decks.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            when (val result = deckRepository.getDecks()) {
                is AppResult.Success -> {
                    _uiState.update {
                        val filtered = filterDecks(result.data, it.query, it.selectedFilter)
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            decks = result.data,
                            filteredDecks = filtered,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.error.message
                        )
                    }
                    if (_uiState.value.decks.isNotEmpty()) {
                        _effects.emit(DeckListEffect.ShowSnackbar(result.error.message))
                    }
                }
            }
        }
    }

    private fun updateSearch(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                filteredDecks = filterDecks(it.decks, query, it.selectedFilter)
            )
        }
    }

    private fun updateFilter(filter: DeckFilter) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredDecks = filterDecks(it.decks, it.query, filter)
            )
        }
    }

    private fun filterDecks(
        decks: List<VocabularyDeck>,
        query: String,
        selectedFilter: DeckFilter
    ): List<VocabularyDeck> {
        val normalizedQuery = query.trim().lowercase()
        return decks.filter { deck ->
            deck.matchesFilter(selectedFilter) && deck.matchesSearch(normalizedQuery)
        }
    }

    private fun VocabularyDeck.matchesFilter(filter: DeckFilter): Boolean {
        return when (filter) {
            DeckFilter.All -> true
            DeckFilter.Seed -> isSeed || (isPublic && isReadOnly)
            DeckFilter.Mine -> !isSeed && !isPublic
        }
    }

    private fun VocabularyDeck.matchesSearch(normalizedQuery: String): Boolean {
        if (normalizedQuery.isEmpty()) return true
        return name.contains(normalizedQuery, ignoreCase = true) ||
            sourceUnit.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            sourceName.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            tags.any { it.contains(normalizedQuery, ignoreCase = true) }
    }

    private fun emitEffect(effect: DeckListEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
