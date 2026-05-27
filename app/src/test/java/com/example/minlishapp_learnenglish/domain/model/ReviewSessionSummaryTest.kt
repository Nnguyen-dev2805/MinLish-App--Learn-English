package com.example.minlishapp_learnenglish.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReviewSessionSummaryTest {
    @Test
    fun `accuracy returns zero when no cards reviewed`() {
        val summary = ReviewSessionSummary(totalCards = 0)

        assertEquals(0, summary.accuracy)
        assertEquals(0, summary.nextReviewCount)
        assertFalse(summary.isComplete)
    }

    @Test
    fun `record updates counts and accuracy`() {
        val summary = ReviewSessionSummary(totalCards = 4)
            .record(ReviewRating.Good, isCorrect = true)
            .record(ReviewRating.Again, isCorrect = false)
            .record(ReviewRating.Easy, isCorrect = true)
            .record(ReviewRating.Hard, isCorrect = true)

        assertEquals(4, summary.reviewedCards)
        assertEquals(3, summary.correctCount)
        assertEquals(1, summary.againCount)
        assertEquals(1, summary.hardCount)
        assertEquals(1, summary.goodCount)
        assertEquals(1, summary.easyCount)
        assertEquals(75, summary.accuracy)
        assertEquals(2, summary.nextReviewCount)
    }
}
