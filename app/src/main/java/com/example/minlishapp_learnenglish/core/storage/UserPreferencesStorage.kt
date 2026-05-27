package com.example.minlishapp_learnenglish.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.minlishapp_learnenglish.data.local.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "minlish_user_preferences")

class UserPreferencesStorage(
    context: Context
) {
    private val dataStore = context.applicationContext.userPreferencesDataStore

    val isOnboardingSeen: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_SEEN] ?: false
    }

    suspend fun setOnboardingSeen(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_SEEN] = value
        }
    }
}
