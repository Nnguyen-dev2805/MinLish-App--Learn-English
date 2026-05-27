package com.example.minlishapp_learnenglish.domain.usecase.decks

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.DeckRepository
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord

class GetDecksUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(): AppResult<List<VocabularyDeck>> {
        return deckRepository.getDecks()
    }
}

class GetDeckDetailUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(deckId: Long): AppResult<VocabularyDeck> {
        return deckRepository.getDeck(deckId)
    }
}

class GetDeckItemsUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(deckId: Long): AppResult<List<VocabularyWord>> {
        return deckRepository.getDeckItems(deckId)
    }
}

class CreateDeckUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        tags: List<String>
    ): AppResult<VocabularyDeck> {
        return deckRepository.createDeck(
            name = name,
            description = description,
            tags = tags
        )
    }
}

class CreateWordUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(
        deckId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord> {
        return deckRepository.createWord(
            deckId = deckId,
            word = word,
            pronunciation = pronunciation,
            meaning = meaning,
            description = description,
            example = example,
            collocation = collocation,
            relatedWords = relatedWords,
            note = note
        )
    }
}

class UpdateWordUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(
        itemId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord> {
        return deckRepository.updateWord(
            itemId = itemId,
            word = word,
            pronunciation = pronunciation,
            meaning = meaning,
            description = description,
            example = example,
            collocation = collocation,
            relatedWords = relatedWords,
            note = note
        )
    }
}

class DeleteWordUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(itemId: Long): AppResult<Unit> {
        return deckRepository.deleteWord(itemId)
    }
}
