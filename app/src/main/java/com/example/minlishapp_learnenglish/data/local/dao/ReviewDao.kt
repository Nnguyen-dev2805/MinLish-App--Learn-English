package com.example.minlishapp_learnenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minlishapp_learnenglish.data.local.entity.ReviewLogEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewStateEntity
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity

data class DailyReviewCount(
    val reviewDate: String,
    val reviewCount: Int,
    val correctCount: Int
)

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_states WHERE userId = :userId AND wordId = :wordId LIMIT 1")
    suspend fun getReviewState(userId: Long, wordId: Long): ReviewStateEntity?

    @Query("SELECT * FROM review_states WHERE userId = :userId")
    suspend fun getAllReviewStates(userId: Long): List<ReviewStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReviewState(state: ReviewStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReviewStates(states: List<ReviewStateEntity>)

    @Insert
    suspend fun insertReviewLog(log: ReviewLogEntity): Long

    @Query(
        """
        SELECT w.*
        FROM vocabulary_words AS w
        LEFT JOIN review_states AS rs ON rs.wordId = w.id AND rs.userId = :userId
        WHERE rs.wordId IS NULL OR rs.lastReviewedAt IS NULL
        ORDER BY w.id ASC
        LIMIT :limit
        """
    )
    suspend fun getNewCards(userId: Long, limit: Int): List<VocabularyWordEntity>

    @Query(
        """
        SELECT w.*
        FROM vocabulary_words AS w
        LEFT JOIN review_states AS rs ON rs.wordId = w.id AND rs.userId = :userId
        WHERE w.deckId = :deckId AND (rs.wordId IS NULL OR rs.lastReviewedAt IS NULL)
        ORDER BY w.id ASC
        LIMIT :limit
        """
    )
    suspend fun getNewCardsByDeck(userId: Long, deckId: Long, limit: Int): List<VocabularyWordEntity>

    @Query(
        """
        SELECT w.*
        FROM vocabulary_words AS w
        INNER JOIN review_states AS rs ON rs.wordId = w.id AND rs.userId = :userId
        WHERE rs.nextDueAt IS NOT NULL AND rs.nextDueAt <= :today
        ORDER BY rs.nextDueAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueCards(userId: Long, today: String, limit: Int): List<VocabularyWordEntity>

    @Query(
        """
        SELECT w.*
        FROM vocabulary_words AS w
        INNER JOIN review_states AS rs ON rs.wordId = w.id AND rs.userId = :userId
        WHERE w.deckId = :deckId AND rs.nextDueAt IS NOT NULL AND rs.nextDueAt <= :today
        ORDER BY rs.nextDueAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueCardsByDeck(userId: Long, deckId: Long, today: String, limit: Int): List<VocabularyWordEntity>

    @Query(
        """
        SELECT COUNT(*) FROM review_states
        WHERE userId = :userId AND (isLearned = 1 OR repetitions > 0)
        """
    )
    suspend fun countLearnedWords(userId: Long): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM vocabulary_words AS w
        LEFT JOIN review_states AS rs ON rs.wordId = w.id AND rs.userId = :userId
        WHERE rs.wordId IS NULL OR rs.lastReviewedAt IS NULL
        """
    )
    suspend fun countNewCards(userId: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM review_states
        WHERE userId = :userId AND nextDueAt IS NOT NULL AND nextDueAt <= :today
        """
    )
    suspend fun countDueCards(userId: Long, today: String): Int

    @Query("SELECT COUNT(*) FROM review_logs WHERE userId = :userId")
    suspend fun countReviews(userId: Long): Int

    @Query("SELECT COUNT(*) FROM review_logs WHERE userId = :userId AND isCorrect = 1")
    suspend fun countCorrectReviews(userId: Long): Int

    @Query(
        """
        SELECT reviewDate,
            CAST(COUNT(*) AS INTEGER) AS reviewCount,
            CAST(SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) AS INTEGER) AS correctCount
        FROM review_logs
        WHERE userId = :userId
        GROUP BY reviewDate
        ORDER BY reviewDate DESC
        LIMIT :limit
        """
    )
    suspend fun getDailyReviewCounts(userId: Long, limit: Int): List<DailyReviewCount>

    @Query(
        """
        SELECT DISTINCT reviewDate FROM review_logs
        WHERE userId = :userId
        ORDER BY reviewDate DESC
        """
    )
    suspend fun getReviewDatesDescending(userId: Long): List<String>

    @Query("DELETE FROM review_states WHERE userId = :userId AND wordId = :wordId")
    suspend fun deleteReviewState(userId: Long, wordId: Long)
}
