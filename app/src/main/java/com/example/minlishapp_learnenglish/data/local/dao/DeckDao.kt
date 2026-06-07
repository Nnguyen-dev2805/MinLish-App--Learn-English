package com.example.minlishapp_learnenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.minlishapp_learnenglish.data.local.entity.DeckEntity

@Dao
interface DeckDao {
    @Query(
        """
        SELECT * FROM decks
        WHERE userId IS NULL OR userId = :userId
        ORDER BY createdAt DESC, id DESC
        """
    )
    suspend fun getDecks(userId: Long): List<DeckEntity>

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Query("SELECT COUNT(*) FROM decks WHERE userId IS NULL")
    suspend fun countSeedDecks(): Int

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE deckId = :deckId")
    suspend fun countWords(deckId: Long): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM review_states
        INNER JOIN vocabulary_words ON vocabulary_words.id = review_states.wordId
        WHERE vocabulary_words.deckId = :deckId
            AND review_states.userId = :userId
            AND review_states.isLearned = 1
        """
    )
    suspend fun countLearnedWords(deckId: Long, userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<DeckEntity>): List<Long>

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)
}
