package com.example.inventoryapplication.pref

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.inventoryapplication.models.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[ID_KEY] = user.id ?: -1
            preferences[TOKEN_KEY] = user.token
            preferences[IS_SIGNIN_KEY] = user.isSignIn
            preferences[USERNAME_KEY] = user.username ?: ""
            preferences[EMAIL_KEY] = user.email ?: ""
            preferences[PHOTO_KEY] = user.photoProfile ?: ""
            preferences[ROLE_KEY] = user.role ?: ""
            Log.d("UserPreference", "Session saved with role: ${user.role}")
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            val id = preferences[ID_KEY] ?: -1
            val role = preferences[ROLE_KEY] ?: ""
            Log.d("UserPreference", "Session fetched with role: $role")
            UserModel(
                id = if (id == -1) null else id,
                token = preferences[TOKEN_KEY] ?: "",
                isSignIn = preferences[IS_SIGNIN_KEY] == true,
                username = preferences[USERNAME_KEY],
                email = preferences[EMAIL_KEY],
                photoProfile = preferences[PHOTO_KEY],
                role = role
            )
        }
    }

    suspend fun signin() {
        dataStore.edit { preferences ->
            preferences[IS_SIGNIN_KEY] = true
        }
    }

    suspend fun signout() {
        dataStore.edit { preferences ->
            preferences.clear()
            Log.d("UserPreference", "Session cleared")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val ID_KEY = intPreferencesKey("id")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_SIGNIN_KEY = booleanPreferencesKey("isSignIn")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PHOTO_KEY = stringPreferencesKey("photoProfile")
        private val ROLE_KEY = stringPreferencesKey("role")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
