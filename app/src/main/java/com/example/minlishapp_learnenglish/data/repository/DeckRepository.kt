package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.map
import com.example.minlishapp_learnenglish.data.remote.api.DeckApi
import com.example.minlishapp_learnenglish.data.remote.dto.CreateDeckRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.CreateVocabularyItemRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateVocabularyItemRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.toDomain
import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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
}

class DefaultDeckRepository(
    private val deckApi: DeckApi,
    private val moshi: Moshi
) : DeckRepository {
    override suspend fun getDecks(): AppResult<List<VocabularyDeck>> {
        return safeApiCall(moshi) {
            deckApi.getDecks()
        }.map { response ->
            response.items.map { it.toDomain() }
        }
    }

    override suspend fun getDeck(deckId: Long): AppResult<VocabularyDeck> {
        return safeApiCall(moshi) {
            deckApi.getDeck(deckId)
        }.map { it.toDomain() }
    }

    override suspend fun getDeckItems(deckId: Long): AppResult<List<VocabularyWord>> {
        return safeApiCall(moshi) {
            deckApi.getDeckItems(deckId)
        }.map { response ->
            response.items.map { it.toDomain() }
        }
    }

    override suspend fun createDeck(
        name: String,
        description: String?,
        tags: List<String>
    ): AppResult<VocabularyDeck> {
        val request = CreateDeckRequestDto(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
        )
        return safeApiCall(moshi) {
            deckApi.createDeck(request)
        }.map { it.toDomain() }
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
        val request = CreateVocabularyItemRequestDto(
            word = word.trim(),
            pronunciation = cleanOptionalText(pronunciation),
            meaning = meaning.trim(),
            description = cleanOptionalText(description),
            example = cleanOptionalText(example),
            collocation = cleanOptionalText(collocation),
            relatedWords = cleanList(relatedWords),
            note = cleanOptionalText(note)
        )
        return safeApiCall(moshi) {
            deckApi.createDeckItem(deckId, request)
        }.map { it.toDomain() }
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
        val request = UpdateVocabularyItemRequestDto(
            word = word.trim(),
            pronunciation = cleanOptionalText(pronunciation),
            meaning = meaning.trim(),
            description = cleanOptionalText(description),
            example = cleanOptionalText(example),
            collocation = cleanOptionalText(collocation),
            relatedWords = cleanList(relatedWords),
            note = cleanOptionalText(note)
        )
        return safeApiCall(moshi) {
            deckApi.updateItem(itemId, request)
        }.map { it.toDomain() }
    }

    override suspend fun deleteWord(itemId: Long): AppResult<Unit> {
        return safeApiCall(moshi) {
            deckApi.deleteItem(itemId)
        }
    }

    override suspend fun importDeckItems(deckId: Long, fileName: String, fileBytes: ByteArray): AppResult<Int> {
        return safeApiCall(moshi) {
            val mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull()
            val requestBody = fileBytes.toRequestBody(mediaType)
            val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
            deckApi.importDeckItems(deckId, part)
        }.map { it.imported_count }
    }

    private fun cleanOptionalText(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun cleanList(values: List<String>): List<String>? {
        return values.map { it.trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
    }
}
