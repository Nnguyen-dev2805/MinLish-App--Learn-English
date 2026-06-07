package com.example.minlishapp_learnenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity

@Dao
interface WordDao {
    @Query("SELECT * FROM vocabulary_words ORDER BY id ASC LIMIT :limit")
    suspend fun getAllWords(limit: Int): List<VocabularyWordEntity>

    @Query("SELECT * FROM vocabulary_words WHERE deckId = :deckId ORDER BY id ASC")
    suspend fun getWordsByDeck(deckId: Long): List<VocabularyWordEntity>

    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Long): VocabularyWordEntity?

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    suspend fun countAllWords(): Int

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE deckId = :deckId")
    suspend fun countWordsByDeck(deckId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyWordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyWordEntity>): List<Long>

    @Update
    suspend fun updateWord(word: VocabularyWordEntity)

    @Delete
    suspend fun deleteWord(word: VocabularyWordEntity)

    @Query("DELETE FROM vocabulary_words WHERE id = :wordId")
    suspend fun deleteWordById(wordId: Long)
}
