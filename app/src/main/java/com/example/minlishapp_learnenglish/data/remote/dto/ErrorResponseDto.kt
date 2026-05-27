package com.example.minlishapp_learnenglish.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponseDto(
    @param:Json(name = "detail") val detail: String? = null,
    @param:Json(name = "code") val code: String? = null
)
