package com.example.minlishapp_learnenglish.domain.usecase.decks

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository

class ImportDeckItemsUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(deckId: Long, fileName: String, fileBytes: ByteArray): AppResult<Int> {
        return deckRepository.importDeckItems(deckId, fileName, fileBytes)
    }
}
