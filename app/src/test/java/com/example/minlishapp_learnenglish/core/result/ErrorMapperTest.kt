package com.example.minlishapp_learnenglish.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ErrorMapperTest {
    @Test
    fun `maps 422 to validation error`() {
        val error = ErrorMapper.fromHttpStatus(
            statusCode = 422,
            detail = "Invalid email",
            code = "INVALID_EMAIL"
        )

        assertTrue(error is AppError.Validation)
        assertEquals("Invalid email", error.message)
        assertEquals("INVALID_EMAIL", error.code)
    }

    @Test
    fun `maps 401 to unauthorized error`() {
        val error = ErrorMapper.fromHttpStatus(
            statusCode = 401,
            detail = null,
            code = null
        )

        assertTrue(error is AppError.Unauthorized)
        assertEquals("UNAUTHORIZED", error.code)
    }

    @Test
    fun `maps io exception to network error`() {
        val error = ErrorMapper.fromThrowable(IOException("offline"))

        assertTrue(error is AppError.Network)
        assertEquals("NETWORK_ERROR", error.code)
    }
}
