package com.example.minlishapp_learnenglish.core.network

import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenStorage.getAccessToken()
        val request = if (accessToken.isNullOrBlank() || originalRequest.header(AUTHORIZATION) != null) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header(AUTHORIZATION, "Bearer $accessToken")
                .build()
        }
        return chain.proceed(request)
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
    }
}
