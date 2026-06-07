package com.example.minlishapp_learnenglish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary_words",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class VocabularyWordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    val description: String?,
    val example: String?,
    val collocation: String?,
    val relatedWords: List<String> = emptyList(),
    val note: String?,
    val suggestion: String? = null,
    val imageUrl: String? = null,
    val wordAudioUrl: String? = null,
    val meaningAudioUrl: String? = null,
    val exampleAudioUrl: String? = null
)
