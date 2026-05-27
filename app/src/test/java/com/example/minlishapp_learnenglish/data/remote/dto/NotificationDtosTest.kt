package com.example.minlishapp_learnenglish.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDtosTest {
    @Test
    fun `notification preferences response maps to domain`() {
        val dto = NotificationPreferencesResponseDto(
            dailyTime = "21:30",
            timezone = "Asia/Ho_Chi_Minh",
            emailEnabled = false,
            pushEnabled = true
        )

        val domain = dto.toDomain()

        assertEquals("21:30", domain.dailyTime)
        assertEquals("Asia/Ho_Chi_Minh", domain.timezone)
        assertFalse(domain.emailEnabled)
        assertTrue(domain.pushEnabled)
    }
}
