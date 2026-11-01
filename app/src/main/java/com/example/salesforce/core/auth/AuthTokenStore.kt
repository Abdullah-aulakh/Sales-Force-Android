
package com.example.salesforce.core.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

class AuthTokenStore(context: Context) {

    private val dataStore = context.applicationContext.authDataStore

    private companion object {
        val TOKEN = stringPreferencesKey("access_token")
        val EXPIRES_AT = longPreferencesKey("access_token_expires_at")
    }

    suspend fun getValidToken(): String? {
        val preferences = dataStore.data.first()
        val token = preferences[TOKEN]
        val expiresAt = preferences[EXPIRES_AT] ?: 0L

        if (token.isNullOrBlank() ||
            System.currentTimeMillis() >= expiresAt
        ) {
            clear()
            return null
        }

        return token
    }

    suspend fun save(token: String, expiresInSeconds: Long) {
        require(token.isNotBlank())
        require(expiresInSeconds > 0)

        dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[EXPIRES_AT] =
                System.currentTimeMillis() + expiresInSeconds * 1_000L
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN)
            preferences.remove(EXPIRES_AT)
        }
    }
}
