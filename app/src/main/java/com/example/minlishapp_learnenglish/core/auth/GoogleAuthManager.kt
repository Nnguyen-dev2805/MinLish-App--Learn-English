package com.example.minlishapp_learnenglish.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager(private val context: Context) {
    suspend fun getGoogleIdToken(): String? {
        val credentialManager = CredentialManager.create(context)
        
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(UUID.randomUUID().toString().toByteArray())
            .joinToString("") { "%02x".format(it) }

        // Use a placeholder since user hasn't set up Google Cloud Console yet
        val serverClientId = "960908040534-segh4hn131ckkudgvl4pv5pe51g6ep42.apps.googleusercontent.com"
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                return googleIdTokenCredential.idToken
            }
            throw Exception("Google Sign-In returned an unexpected credential type.")
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            throw Exception("Google Sign-In failed: ${e.message ?: "Unknown error"}")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Google Sign-In failed: ${e.message}")
        }
    }
}
