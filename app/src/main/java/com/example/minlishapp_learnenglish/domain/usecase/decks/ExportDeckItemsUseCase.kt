package com.example.minlishapp_learnenglish.domain.usecase.decks

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository

class ExportDeckItemsUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(deckId: Long): AppResult<ByteArray> {
        return deckRepository.exportDeckItems(deckId)
    }
}
