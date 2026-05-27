package com.example.minlishapp_learnenglish.data.remote.dto

import com.example.minlishapp_learnenglish.domain.model.VocabularyDeck
import com.example.minlishapp_learnenglish.domain.model.VocabularyWord
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeckListResponseDto(
    @param:Json(name = "items") val items: List<DeckResponseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DeckResponseDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "tags") val tags: List<String> = emptyList(),
    @param:Json(name = "is_public") val isPublic: Boolean,
    @param:Json(name = "is_seed") val isSeed: Boolean,
    @param:Json(name = "is_read_only") val isReadOnly: Boolean,
    @param:Json(name = "source_name") val sourceName: String? = null,
    @param:Json(name = "source_unit") val sourceUnit: String? = null,
    @param:Json(name = "word_count") val wordCount: Int
)

@JsonClass(generateAdapter = true)
data class CreateDeckRequestDto(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "tags") val tags: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateDeckRequestDto(
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "tags") val tags: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class VocabularyItemListResponseDto(
    @param:Json(name = "items") val items: List<VocabularyItemResponseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VocabularyItemResponseDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "deck_id") val deckId: Long,
    @param:Json(name = "word") val word: String,
    @param:Json(name = "pronunciation") val pronunciation: String? = null,
    @param:Json(name = "meaning") val meaning: String,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "example") val example: String? = null,
    @param:Json(name = "collocation") val collocation: String? = null,
    @param:Json(name = "related_words") val relatedWords: List<String>? = null,
    @param:Json(name = "note") val note: String? = null,
    @param:Json(name = "suggestion") val suggestion: String? = null,
    @param:Json(name = "image_url") val imageUrl: String? = null,
    @param:Json(name = "word_audio_url") val wordAudioUrl: String? = null,
    @param:Json(name = "meaning_audio_url") val meaningAudioUrl: String? = null,
    @param:Json(name = "example_audio_url") val exampleAudioUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateVocabularyItemRequestDto(
    @param:Json(name = "word") val word: String,
    @param:Json(name = "pronunciation") val pronunciation: String? = null,
    @param:Json(name = "meaning") val meaning: String,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "example") val example: String? = null,
    @param:Json(name = "collocation") val collocation: String? = null,
    @param:Json(name = "related_words") val relatedWords: List<String>? = null,
    @param:Json(name = "note") val note: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateVocabularyItemRequestDto(
    @param:Json(name = "word") val word: String? = null,
    @param:Json(name = "pronunciation") val pronunciation: String? = null,
    @param:Json(name = "meaning") val meaning: String? = null,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "example") val example: String? = null,
    @param:Json(name = "collocation") val collocation: String? = null,
    @param:Json(name = "related_words") val relatedWords: List<String>? = null,
    @param:Json(name = "note") val note: String? = null
)

fun DeckResponseDto.toDomain(): VocabularyDeck {
    return VocabularyDeck(
        id = id,
        name = name,
        description = description,
        tags = tags,
        isPublic = isPublic,
        isSeed = isSeed,
        isReadOnly = isReadOnly,
        sourceName = sourceName,
        sourceUnit = sourceUnit,
        wordCount = wordCount
    )
}

fun VocabularyItemResponseDto.toDomain(): VocabularyWord {
    return VocabularyWord(
        id = id,
        deckId = deckId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        description = description,
        example = example,
        collocation = collocation,
        relatedWords = relatedWords.orEmpty(),
        note = note,
        suggestion = suggestion,
        imageUrl = imageUrl,
        wordAudioUrl = wordAudioUrl,
        meaningAudioUrl = meaningAudioUrl,
        exampleAudioUrl = exampleAudioUrl
    )
}
