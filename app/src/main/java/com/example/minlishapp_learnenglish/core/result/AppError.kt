package com.example.minlishapp_learnenglish.core.result

sealed interface AppError {
    val message: String
    val code: String?

    data class Validation(
        override val message: String,
        override val code: String? = null
    ) : AppError

    data class Unauthorized(
        override val message: String = "Your session has expired.",
        override val code: String? = "UNAUTHORIZED"
    ) : AppError

    data class Forbidden(
        override val message: String = "You do not have permission to perform this action.",
        override val code: String? = "FORBIDDEN"
    ) : AppError

    data class NotFound(
        override val message: String = "Data not found.",
        override val code: String? = "NOT_FOUND"
    ) : AppError

    data class Network(
        override val message: String = "Unable to connect to the server.",
        override val code: String? = "NETWORK_ERROR"
    ) : AppError

    data class Server(
        override val message: String = "The server is currently unavailable.",
        override val code: String? = "SERVER_ERROR"
    ) : AppError

    data class Serialization(
        override val message: String = "The response format is invalid.",
        override val code: String? = "SERIALIZATION_ERROR"
    ) : AppError

    data class Unknown(
        override val message: String = "Something went wrong.",
        override val code: String? = "UNKNOWN_ERROR"
    ) : AppError
}
