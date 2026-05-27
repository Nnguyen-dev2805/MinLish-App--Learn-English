package com.example.minlishapp_learnenglish.core.result

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import java.io.IOException

object ErrorMapper {
    fun fromHttpStatus(
        statusCode: Int,
        detail: String?,
        code: String?
    ): AppError {
        val message = detail.orEmpty().ifBlank { defaultMessageFor(statusCode) }
        return when (statusCode) {
            400, 422 -> AppError.Validation(message = message, code = code)
            401 -> AppError.Unauthorized(message = message, code = code ?: "UNAUTHORIZED")
            403 -> AppError.Forbidden(message = message, code = code ?: "FORBIDDEN")
            404 -> AppError.NotFound(message = message, code = code ?: "NOT_FOUND")
            in 500..599 -> AppError.Server(message = message, code = code ?: "SERVER_ERROR")
            else -> AppError.Unknown(message = message, code = code ?: "HTTP_$statusCode")
        }
    }

    fun fromThrowable(throwable: Throwable): AppError {
        return when (throwable) {
            is IOException -> AppError.Network()
            is JsonDataException,
            is JsonEncodingException -> AppError.Serialization()
            else -> AppError.Unknown(message = throwable.message ?: AppError.Unknown().message)
        }
    }

    fun toUiError(error: AppError): UiError {
        return UiError(
            message = error.message,
            code = error.code,
            canRetry = error is AppError.Network || error is AppError.Server || error is AppError.Unknown
        )
    }

    private fun defaultMessageFor(statusCode: Int): String {
        return when (statusCode) {
            400, 422 -> "Dữ liệu nhập chưa hợp lệ."
            401 -> "Phiên đăng nhập đã hết hạn."
            403 -> "Bạn không có quyền thực hiện thao tác này."
            404 -> "Không tìm thấy dữ liệu."
            in 500..599 -> "Máy chủ đang gặp sự cố."
            else -> "Đã có lỗi xảy ra."
        }
    }
}
