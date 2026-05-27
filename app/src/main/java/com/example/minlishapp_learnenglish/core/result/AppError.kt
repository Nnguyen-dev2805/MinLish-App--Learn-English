package com.example.minlishapp_learnenglish.core.result

sealed interface AppError {
    val message: String
    val code: String?

    data class Validation(
        override val message: String,
        override val code: String? = null
    ) : AppError

    data class Unauthorized(
        override val message: String = "Phiên đăng nhập đã hết hạn.",
        override val code: String? = "UNAUTHORIZED"
    ) : AppError

    data class Forbidden(
        override val message: String = "Bạn không có quyền thực hiện thao tác này.",
        override val code: String? = "FORBIDDEN"
    ) : AppError

    data class NotFound(
        override val message: String = "Không tìm thấy dữ liệu.",
        override val code: String? = "NOT_FOUND"
    ) : AppError

    data class Network(
        override val message: String = "Không thể kết nối máy chủ.",
        override val code: String? = "NETWORK_ERROR"
    ) : AppError

    data class Server(
        override val message: String = "Máy chủ đang gặp sự cố.",
        override val code: String? = "SERVER_ERROR"
    ) : AppError

    data class Serialization(
        override val message: String = "Dữ liệu trả về không đúng định dạng.",
        override val code: String? = "SERIALIZATION_ERROR"
    ) : AppError

    data class Unknown(
        override val message: String = "Đã có lỗi xảy ra.",
        override val code: String? = "UNKNOWN_ERROR"
    ) : AppError
}
