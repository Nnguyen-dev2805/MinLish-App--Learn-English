package com.example.minlishapp_learnenglish.core.result

data class UiError(
    val message: String,
    val code: String? = null,
    val canRetry: Boolean = true,
    val fieldErrors: Map<String, String> = emptyMap()
)
