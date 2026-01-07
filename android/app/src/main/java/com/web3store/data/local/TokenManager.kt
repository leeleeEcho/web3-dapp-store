package com.web3store.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val EXPIRES_AT_KEY = longPreferencesKey("token_expires_at")
        private val USER_JSON_KEY = stringPreferencesKey("user_json")
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val isLoggedInFlow: Flow<Boolean> = context.authDataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT_KEY] ?: 0L
        token != null && System.currentTimeMillis() < expiresAt
    }

    suspend fun saveAuth(token: String, expiresIn: Long, userJson: String) {
        val expiresAt = System.currentTimeMillis() + (expiresIn * 1000)
        context.authDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[EXPIRES_AT_KEY] = expiresAt
            preferences[USER_JSON_KEY] = userJson
        }
    }

    suspend fun getToken(): String? {
        val preferences = context.authDataStore.data.first()
        val token = preferences[TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT_KEY] ?: 0L

        return if (token != null && System.currentTimeMillis() < expiresAt) {
            token
        } else {
            null
        }
    }

    suspend fun getUserJson(): String? {
        return context.authDataStore.data.first()[USER_JSON_KEY]
    }

    suspend fun isLoggedIn(): Boolean {
        val preferences = context.authDataStore.data.first()
        val token = preferences[TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT_KEY] ?: 0L
        return token != null && System.currentTimeMillis() < expiresAt
    }

    suspend fun clearAuth() {
        context.authDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(EXPIRES_AT_KEY)
            preferences.remove(USER_JSON_KEY)
        }
    }
}
