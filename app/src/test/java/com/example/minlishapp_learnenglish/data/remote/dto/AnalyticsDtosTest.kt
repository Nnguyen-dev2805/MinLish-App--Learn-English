package com.example.minlishapp_learnenglish.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsDtosTest {
    @Test
    fun `retention dto maps to domain model`() {
        val domain = RetentionResponseDto(
            retentionRate = 87.5,
            totalReviews = 40,
            retainedReviews = 35
        ).toDomain()

        assertEquals(87.5, domain.retentionRate, 0.0)
        assertEquals(40, domain.totalReviews)
        assertEquals(35, domain.retainedReviews)
    }
}
