package com.example.minlishapp_learnenglish.domain.model

data class VocabularyDeck(
    val id: Long,
    val name: String,
    val description: String?,
    val tags: List<String>,
    val isPublic: Boolean,
    val isSeed: Boolean,
    val isReadOnly: Boolean,
    val sourceName: String?,
    val sourceUnit: String?,
    val wordCount: Int
) {
    val displayTitle: String = sourceUnit ?: name
    val displayDescription: String = description ?: sourceName ?: "Custom vocabulary deck"
}

data class VocabularyWord(
    val id: Long,
    val deckId: Long,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    val description: String?,
    val example: String?,
    val collocation: String?,
    val relatedWords: List<String>,
    val note: String?,
    val suggestion: String?,
    val imageUrl: String?,
    val wordAudioUrl: String?,
    val meaningAudioUrl: String?,
    val exampleAudioUrl: String?
) {
    val hasMedia: Boolean = imageUrl != null ||
        wordAudioUrl != null ||
        meaningAudioUrl != null ||
        exampleAudioUrl != null
}
