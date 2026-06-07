package com.example.minlishapp_learnenglish.data.repository

import android.content.Context
import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.local.dao.DailyReviewCount
import com.example.minlishapp_learnenglish.data.local.dao.ReviewDao
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.dao.WordDao
import com.example.minlishapp_learnenglish.data.local.database.DatabaseSeeder
import com.example.minlishapp_learnenglish.data.local.database.MinLishDatabase
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException

interface AnalyticsRepository {
    suspend fun getDashboard(): AppResult<ProgressStats>
    suspend fun getActivity(): AppResult<List<DailyActivity>>
    suspend fun getRetention(): AppResult<RetentionStats>
}

class DefaultAnalyticsRepository(
    private val context: Context,
    private val database: MinLishDatabase,
    private val reviewDao: ReviewDao,
    private val wordDao: WordDao,
    private val userDao: UserDao
) : AnalyticsRepository {
    override suspend fun getDashboard(): AppResult<ProgressStats> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val totalReviews = reviewDao.countReviews(userId)
            val correctReviews = reviewDao.countCorrectReviews(userId)
            val accuracy = if (totalReviews == 0) {
                0.0
            } else {
                (correctReviews.toDouble() / totalReviews.toDouble()) * 100.0
            }
            val userLevel = userDao.getLoggedInUser()?.level
            val learnedWords = reviewDao.countLearnedWords(userId)
            ProgressStats(
                learnedWords = learnedWords,
                dueToday = reviewDao.countDueCards(userId, today()),
                streak = calculateStreak(reviewDao.getReviewDatesDescending(userId)),
                accuracy = accuracy,
                levelEstimation = userLevel ?: estimateLevel(wordDao.countAllWords(), learnedWords)
            )
        }
    }

    override suspend fun getActivity(): AppResult<List<DailyActivity>> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val reviewCountsByDate = reviewDao.getDailyReviewCounts(userId, limit = ACTIVITY_DAYS)
                .associateBy { it.reviewDate }

            recentDates(ACTIVITY_DAYS).map { date ->
                reviewCountsByDate[date]?.toDailyActivity()
                    ?: DailyActivity(
                        date = date,
                        reviewCount = 0,
                        correctCount = 0
                    )
            }
        }
    }

    override suspend fun getRetention(): AppResult<RetentionStats> {
        return localCall {
            val userId = userDao.requireUserId()
            ensureSeedData(userId)
            val totalReviews = reviewDao.countReviews(userId)
            val retainedReviews = reviewDao.countCorrectReviews(userId)
            val retentionRate = if (totalReviews == 0) {
                0.0
            } else {
                (retainedReviews.toDouble() / totalReviews.toDouble()) * 100.0
            }
            RetentionStats(
                retentionRate = retentionRate,
                totalReviews = totalReviews,
                retainedReviews = retainedReviews
            )
        }
    }

    private fun calculateStreak(reviewDatesDescending: List<String>): Int {
        val reviewDates = reviewDatesDescending.toSet()
        var streak = 0
        val calendar = Calendar.getInstance()

        while (reviewDates.contains(DATE_FORMAT.format(calendar.time))) {
            streak += 1
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun recentDates(days: Int): List<String> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))
        return List(days) {
            DATE_FORMAT.format(calendar.time).also {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun DailyReviewCount.toDailyActivity(): DailyActivity {
        return DailyActivity(
            date = reviewDate,
            reviewCount = reviewCount,
            correctCount = correctCount
        )
    }

    private fun estimateLevel(totalWords: Int, learnedWords: Int): String {
        return when {
            totalWords == 0 || learnedWords == 0 -> "New"
            learnedWords < 50 -> "A1 Beginner"
            learnedWords < 150 -> "A2 Elementary"
            learnedWords < 300 -> "B1 Intermediate"
            else -> "B2 Upper Intermediate"
        }
    }

    private fun today(): String = DATE_FORMAT.format(Calendar.getInstance().time)

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
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(message = error.message ?: "Invalid analytics data."))
        } catch (error: Exception) {
            AppResult.Failure(
                AppError.Unknown(
                    message = error.message ?: "Local analytics error."
                )
            )
        }
    }

    companion object {
        private const val ACTIVITY_DAYS = 7
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}
