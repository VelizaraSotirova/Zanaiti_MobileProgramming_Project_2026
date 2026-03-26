package bg.zanaiti.craftguide.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_KEY]
        }

    val userIdFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]?.toLongOrNull()
        }

    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id.toString()
        }
    }
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    // За синхронно взимане на токена (за интерцептора)
    fun getToken(): String? {
        return runBlocking {
            tokenFlow.first()
        }
    }

    fun getUsername(): String? {
        return runBlocking {
            usernameFlow.first()
        }
    }

    fun getUserId(): Long {
        return runBlocking {
            userIdFlow.first() ?: 0L
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}