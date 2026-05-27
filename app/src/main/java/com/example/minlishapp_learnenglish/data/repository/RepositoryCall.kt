package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.ErrorMapper
import com.example.minlishapp_learnenglish.data.remote.dto.ErrorResponseDto
import com.squareup.moshi.Moshi
import retrofit2.HttpException

internal suspend fun <T> safeApiCall(
    moshi: Moshi,
    block: suspend () -> T
): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (httpException: HttpException) {
        AppResult.Failure(httpException.toAppError(moshi))
    } catch (throwable: Throwable) {
        AppResult.Failure(ErrorMapper.fromThrowable(throwable))
    }
}

private fun HttpException.toAppError(moshi: Moshi): com.example.minlishapp_learnenglish.core.result.AppError {
    val errorBody = parseErrorBody(moshi)
    return ErrorMapper.fromHttpStatus(
        statusCode = code(),
        detail = errorBody?.detail ?: message(),
        code = errorBody?.code
    )
}

private fun HttpException.parseErrorBody(moshi: Moshi): ErrorResponseDto? {
    val errorBody = response()?.errorBody()?.string() ?: return null
    return runCatching {
        moshi.adapter(ErrorResponseDto::class.java).fromJson(errorBody)
    }.getOrNull()
}
