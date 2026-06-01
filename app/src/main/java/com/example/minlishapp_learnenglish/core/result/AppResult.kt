package com.example.minlishapp_learnenglish.core.result

// out T: tức T chỉ được làm dữ liệu đầu ra không được làm dữ liệu đầu vào
// không thể viết hàm có tham số T ở trong được
// chỉ cho phép lấy ra T không cho phép nhét vào T
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

// nếu thành công thì đổi từ T sang R
inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }
}
