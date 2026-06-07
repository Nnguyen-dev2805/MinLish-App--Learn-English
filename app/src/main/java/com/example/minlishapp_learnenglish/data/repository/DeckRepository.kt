package com.example.minlishapp_learnenglish.data.repository

import android.content.Context
import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.local.dao.DeckDao
import com.example.minlishapp_learnenglish.data.local.dao.ReviewDao
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.dao.WordDao
import com.example.minlishapp_learnenglish.data.local.database.DatabaseSeeder
import com.example.minlishapp_learnenglish.data.local.database.MinLishDatabase
import com.example.minlishapp_learnenglish.data.local.entity.DeckEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewStateEntity
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity
import com.example.minlishapp_learnenglish.data.local.mapper.toDomain
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import kotlinx.coroutines.CancellationException

interface DeckRepository {
    suspend fun getDecks(): AppResult<List<VocabularyDeck>>
    suspend fun getDeck(deckId: Long): AppResult<VocabularyDeck>
    suspend fun getDeckItems(deckId: Long): AppResult<List<VocabularyWord>>
    suspend fun createDeck(
        name: String,
        description: String?,
        tags: List<String>
    ): AppResult<VocabularyDeck>

    suspend fun createWord(
        deckId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord>

    suspend fun updateWord(
        itemId: Long,
        word: String,
        pronunciation: String?,
        meaning: String,
        description: String?,
        example: String?,
        collocation: String?,
        relatedWords: List<String>,
        note: String?
    ): AppResult<VocabularyWord>

    suspend fun deleteWord(itemId: Long): AppResult<Unit>
    
    suspend fun importDeckItems(deckId: Long, fileName: String, fileBytes: ByteArray): AppResult<Int>

    suspend fun exportDeckItems(deckId: Long): AppResult<ByteArray>
}

class DefaultDeckRepository(
    private val context: Context,
    private val database: MinLishDatabase,
    private val deckDao: DeckDao,
    private val wordDao: WordDao,
    private val reviewDao: ReviewDao,
    private val userDao: UserDao
) : DeckRepository {
    override suspend fun getDecks(): AppResult<List<VocabularyDeck>> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            deckDao.getDecks(userId).map { deck ->
                deck.toDomain(
                    wordCount = deckDao.countWords(deck.id),
                    learnedCount = deckDao.countLearnedWords(deck.id, userId)
                )
            }
        }
    }

    override suspend fun getDeck(deckId: Long): AppResult<VocabularyDeck> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val deck = requireAccessibleDeck(deckId, userId)
            deck.toDomain(
                wordCount = deckDao.countWords(deck.id),
                learnedCount = deckDao.countLearnedWords(deck.id, userId)
            )
        }
    }

    override suspend fun getDeckItems(deckId: Long): AppResult<List<VocabularyWord>> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            requireAccessibleDeck(deckId, userId)
            wordDao.getWordsByDeck(deckId).map { it.toDomain() }
        }
    }

    override suspend fun createDeck(
        name: String,
        description: String?,
        tags: List<String>
    ): AppResult<VocabularyDeck> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val cleanName = name.trim()
            require(cleanName.isNotEmpty()) { "Deck name cannot be empty." }

            val deckId = deckDao.insertDeck(
                DeckEntity(
                    userId = userId,
                    name = cleanName,
                    description = cleanOptionalText(description),
                    tags = cleanList(tags),
                    isPublic = false,
                    isSeed = false,
                    isReadOnly = false
                )
            )

            val savedDeck = deckDao.getDeckById(deckId)
                ?: throw LocalNotFoundException("Created deck not found.")
            savedDeck.toDomain(
                wordCount = 0,
                learnedCount = 0
            )
        }
    }

    override suspend fun createWord(
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
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            requireAccessibleDeck(deckId, userId)

            val cleanWord = word.trim()
            val cleanMeaning = meaning.trim()
            require(cleanWord.isNotEmpty()) { "Word cannot be empty." }
            require(cleanMeaning.isNotEmpty()) { "Meaning cannot be empty." }

            val wordId = wordDao.insertWord(
                VocabularyWordEntity(
                    deckId = deckId,
                    word = cleanWord,
                    pronunciation = cleanOptionalText(pronunciation),
                    meaning = cleanMeaning,
                    description = cleanOptionalText(description),
                    example = cleanOptionalText(example),
                    collocation = cleanOptionalText(collocation),
                    relatedWords = cleanList(relatedWords),
                    note = cleanOptionalText(note)
                )
            )
            reviewDao.upsertReviewState(ReviewStateEntity(userId = userId, wordId = wordId))

            val savedWord = wordDao.getWordById(wordId)
                ?: throw LocalNotFoundException("Created word not found.")
            savedWord.toDomain()
        }
    }

    override suspend fun updateWord(
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
        return localCall {
            val existingWord = wordDao.getWordById(itemId)
                ?: throw LocalNotFoundException("Word not found.")

            val cleanWord = word.trim()
            val cleanMeaning = meaning.trim()
            require(cleanWord.isNotEmpty()) { "Word cannot be empty." }
            require(cleanMeaning.isNotEmpty()) { "Meaning cannot be empty." }

            val updatedWord = existingWord.copy(
                word = cleanWord,
                pronunciation = cleanOptionalText(pronunciation),
                meaning = cleanMeaning,
                description = cleanOptionalText(description),
                example = cleanOptionalText(example),
                collocation = cleanOptionalText(collocation),
                relatedWords = cleanList(relatedWords),
                note = cleanOptionalText(note)
            )
            wordDao.updateWord(updatedWord)
            val savedWord = wordDao.getWordById(itemId)
                ?: throw LocalNotFoundException("Updated word not found.")
            savedWord.toDomain()
        }
    }

    override suspend fun deleteWord(itemId: Long): AppResult<Unit> {
        return localCall {
            if (wordDao.getWordById(itemId) == null) {
                throw LocalNotFoundException("Word not found.")
            }
            wordDao.deleteWordById(itemId)
        }
    }

    override suspend fun importDeckItems(
        deckId: Long,
        fileName: String,
        fileBytes: ByteArray
    ): AppResult<Int> {
        return AppResult.Failure(
            AppError.Validation(
                message = "Excel import is not available in the local study version.",
                code = "FEATURE_NOT_AVAILABLE"
            )
        )
    }

    override suspend fun exportDeckItems(deckId: Long): AppResult<ByteArray> {
        return AppResult.Failure(
            AppError.Validation(
                message = "Excel export is not available in the local study version.",
                code = "FEATURE_NOT_AVAILABLE"
            )
        )
    }

    private fun cleanOptionalText(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun cleanList(values: List<String>): List<String> {
        return values.map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private suspend fun requireAccessibleDeck(deckId: Long, userId: Long): DeckEntity {
        val deck = deckDao.getDeckById(deckId)
            ?: throw LocalNotFoundException("Deck not found.")
        if (deck.userId != null && deck.userId != userId) {
            throw LocalNotFoundException("Deck not found.")
        }
        return deck
    }

    private suspend fun ensureSeedData(userId: Long) {
        DatabaseSeeder.seedCatalogIfEmpty(context, database)
        DatabaseSeeder.seedUserIfNeeded(database, userId)
    }

    private suspend fun <T> localCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: LocalAuthRequiredException) {
            AppResult.Failure(AppError.Validation(message = error.message ?: "Please log in first."))
        } catch (error: LocalNotFoundException) {
            AppResult.Failure(AppError.NotFound(message = error.message ?: "Data not found."))
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(message = error.message ?: "Invalid input."))
        } catch (error: Exception) {
            AppResult.Failure(
                AppError.Unknown(
                    message = error.message ?: "Local database error."
                )
            )
        }
    }

    private class LocalNotFoundException(message: String) : Exception(message)
}
