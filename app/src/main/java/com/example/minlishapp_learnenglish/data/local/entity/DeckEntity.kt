package com.example.minlishapp_learnenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long? = null,
    val name: String,
    val description: String?,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false,
    val isSeed: Boolean = false,
    val isReadOnly: Boolean = false,
    val sourceName: String? = null,
    val sourceUnit: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
