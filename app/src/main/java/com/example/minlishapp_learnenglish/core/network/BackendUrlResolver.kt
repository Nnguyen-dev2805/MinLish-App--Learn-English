package com.example.minlishapp_learnenglish.core.network

object BackendUrlResolver {
    private val backendRoot = NetworkConfig.API_BASE_URL
        .substringBefore("/api/v1/")
        .trimEnd('/')

    fun resolve(url: String?): String? {
        val value = url?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return "$backendRoot/${value.trimStart('/')}"
    }
}
