package com.example.minlishapp_learnenglish.core.network

import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.data.remote.api.AuthApi
import com.example.minlishapp_learnenglish.data.remote.dto.RefreshRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val refreshApi: AuthApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount >= MAX_AUTH_ATTEMPTS) {
            tokenStorage.clearTokens()
            return null
        }

        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        val newAccessToken = runBlocking {
            runCatching {
                refreshApi.refresh(RefreshRequestDto(refreshToken)).accessToken
            }.getOrNull()
        }

        if (newAccessToken.isNullOrBlank()) {
            tokenStorage.clearTokens()
            return null
        }

        tokenStorage.updateAccessToken(newAccessToken)
        return response.request.newBuilder()
            .header(AUTHORIZATION, "Bearer $newAccessToken")
            .build()
    }

    private val Response.responseCount: Int
        get() {
            var count = 1
            var prior = priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val MAX_AUTH_ATTEMPTS = 2
    }
}
