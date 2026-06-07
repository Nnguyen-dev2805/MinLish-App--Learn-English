package com.example.minlishapp_learnenglish.data.repository

import android.content.Context
import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.local.dao.ReviewDao
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.dao.WordDao
import com.example.minlishapp_learnenglish.data.local.database.DatabaseSeeder
import com.example.minlishapp_learnenglish.data.local.database.MinLishDatabase
import com.example.minlishapp_learnenglish.data.local.entity.ReviewLogEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewStateEntity
import com.example.minlishapp_learnenglish.data.local.mapper.toReviewCard
import com.example.minlishapp_learnenglish.data.local.mapper.toSubmitReviewResult
import com.example.minlishapp_learnenglish.domain.model.DailyLearningPlan
import com.example.minlishapp_learnenglish.domain.model.ReviewCard
import com.example.minlishapp_learnenglish.domain.model.ReviewRating
import com.example.minlishapp_learnenglish.domain.model.SubmitReviewResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException

interface LearningRepository {
    suspend fun getDailyPlan(): AppResult<DailyLearningPlan>
    suspend fun getReviewCards(
        deckId: Long? = null,
        mode: String? = null
    ): AppResult<List<ReviewCard>>
    suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult>
}

class DefaultLearningRepository(
    private val context: Context,
    private val database: MinLishDatabase,
    private val wordDao: WordDao,
    private val reviewDao: ReviewDao,
    private val userDao: UserDao
) : LearningRepository {
    override suspend fun getDailyPlan(): AppResult<DailyLearningPlan> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val dailyGoal = userDao.getLoggedInUser()?.dailyNewWords ?: DEFAULT_DAILY_GOAL
            val today = today()
            DailyLearningPlan(
                dailyGoal = dailyGoal,
                newCards = reviewDao.countNewCards(userId),
                dueReviews = reviewDao.countDueCards(userId, today),
                totalAvailable = wordDao.countAllWords()
            )
        }
    }

    override suspend fun getReviewCards(
        deckId: Long?,
        mode: String?
    ): AppResult<List<ReviewCard>> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val limit = userDao.getLoggedInUser()?.dailyNewWords ?: DEFAULT_DAILY_GOAL
            val cards = when (mode) {
                "due" -> if (deckId == null) {
                    reviewDao.getDueCards(userId = userId, today = today(), limit = REVIEW_LIMIT)
                } else {
                    reviewDao.getDueCardsByDeck(
                        userId = userId,
                        deckId = deckId,
                        today = today(),
                        limit = REVIEW_LIMIT
                    )
                }
                "deck_all" -> if (deckId == null) {
                    wordDao.getAllWords(limit = REVIEW_LIMIT)
                } else {
                    wordDao.getWordsByDeck(deckId)
                }
                else -> if (deckId == null) {
                    reviewDao.getNewCards(userId = userId, limit = limit)
                } else {
                    reviewDao.getNewCardsByDeck(userId = userId, deckId = deckId, limit = limit)
                }
            }

            cards.map { word ->
                word.toReviewCard(reviewDao.getReviewState(userId, word.id))
            }
        }
    }

    override suspend fun submitReview(
        vocabularyItemId: Long,
        rating: ReviewRating,
        responseMs: Int?
    ): AppResult<SubmitReviewResult> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val word = wordDao.getWordById(vocabularyItemId)
                ?: throw LocalNotFoundException("Review card not found.")
            val currentState = reviewDao.getReviewState(userId, vocabularyItemId)
                ?: ReviewStateEntity(userId = userId, wordId = vocabularyItemId)
            val isCorrect = rating != ReviewRating.Again
            val nextState = currentState.nextState(
                rating = rating,
                reviewedAt = nowDateTime(),
                nextDueAt = nextDueDate(currentState.intervalDays, rating),
                isCorrect = isCorrect
            )

            reviewDao.upsertReviewState(nextState)
            reviewDao.insertReviewLog(
                ReviewLogEntity(
                    userId = userId,
                    wordId = vocabularyItemId,
                    deckId = word.deckId,
                    rating = rating.name,
                    isCorrect = isCorrect,
                    responseMs = responseMs,
                    reviewedAt = nowDateTime(),
                    reviewDate = today()
                )
            )

            nextState.toSubmitReviewResult(
                rating = rating,
                isCorrect = isCorrect
            )
        }
    }

    private fun ReviewStateEntity.nextState(
        rating: ReviewRating,
        reviewedAt: String,
        nextDueAt: String,
        isCorrect: Boolean
    ): ReviewStateEntity {
        val nextRepetitions = when (rating) {
            ReviewRating.Again -> 0
            else -> repetitions + 1
        }
        val nextIntervalDays = when (rating) {
            ReviewRating.Again -> 1
            ReviewRating.Hard -> (intervalDays + 1).coerceAtLeast(1)
            ReviewRating.Good -> (intervalDays * 2).coerceAtLeast(2)
            ReviewRating.Easy -> (intervalDays * 3).coerceAtLeast(4)
        }
        val nextEaseFactor = when (rating) {
            ReviewRating.Again -> (easeFactor - 0.2).coerceAtLeast(1.3)
            ReviewRating.Hard -> (easeFactor - 0.1).coerceAtLeast(1.3)
            ReviewRating.Good -> easeFactor
            ReviewRating.Easy -> easeFactor + 0.15
        }

        return copy(
            repetitions = nextRepetitions,
            intervalDays = nextIntervalDays,
            easeFactor = nextEaseFactor,
            nextDueAt = nextDueAt,
            lastReviewedAt = reviewedAt,
            isLearned = isCorrect || repetitions > 0
        )
    }

    private fun nextDueDate(currentIntervalDays: Int, rating: ReviewRating): String {
        val intervalDays = when (rating) {
            ReviewRating.Again -> 1
            ReviewRating.Hard -> (currentIntervalDays + 1).coerceAtLeast(1)
            ReviewRating.Good -> (currentIntervalDays * 2).coerceAtLeast(2)
            ReviewRating.Easy -> (currentIntervalDays * 3).coerceAtLeast(4)
        }
        return dateAfterDays(intervalDays)
    }

    private fun today(): String = requireNotNull(DATE_FORMAT.get()).format(Calendar.getInstance().time)

    private fun nowDateTime(): String = requireNotNull(DATE_TIME_FORMAT.get()).format(Calendar.getInstance().time)

    private fun dateAfterDays(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return requireNotNull(DATE_FORMAT.get()).format(calendar.time)
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

    companion object {
        private const val DEFAULT_DAILY_GOAL = 10
        private const val REVIEW_LIMIT = 50

        private val DATE_FORMAT = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }
        private val DATE_TIME_FORMAT = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        }
    }
}
