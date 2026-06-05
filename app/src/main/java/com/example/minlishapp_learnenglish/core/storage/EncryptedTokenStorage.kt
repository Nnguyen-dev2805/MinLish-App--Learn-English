package com.example.minlishapp_learnenglish.core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedTokenStorage(
    context: Context
) : TokenStorage {
    private val appContext = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    @Suppress("DEPRECATION") // Tắt cảnh báo
    private val preferences = EncryptedSharedPreferences.create(
        appContext,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getAccessToken(): String? {
        return preferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun getRefreshToken(): String? {
        return preferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun updateAccessToken(accessToken: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    override fun clearTokens() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "minlish_secure_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
