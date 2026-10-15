package com.example.ridepassenger2.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore("session")

/**
 * Persistent session store (DataStore). Persists who is signed in so the app can
 * skip auth on relaunch and restore session seamlessly.
 */
object SessionManager {

    private val LOGGED_IN = booleanPreferencesKey("logged_in")
    private val PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
    private val NAME = stringPreferencesKey("name")
    private val HANDLE = stringPreferencesKey("handle")
    private val PHONE = stringPreferencesKey("phone")
    private val EMAIL = stringPreferencesKey("email")
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val PROFILE_PIC = stringPreferencesKey("profile_pic")

    data class Session(
        val loggedIn: Boolean = false,
        val name: String = "",
        val handle: String = "",
        val phone: String = "",
        val email: String = "",
        val profileCompleted: Boolean? = null,
        val accessToken: String? = null,
        val refreshToken: String? = null,
        val profilePictureUrl: String? = null
    ) {
        val isProfileComplete: Boolean
            get() = profileCompleted ?: (name.isNotBlank() && phone.isNotBlank())
    }

    fun observe(context: Context): Flow<Session> =
        context.sessionStore.data.map { prefs ->
            Session(
                loggedIn = prefs[LOGGED_IN] == true,
                name = prefs[NAME].orEmpty().takeUnless { it == "Rida Rider" }.orEmpty(),
                handle = prefs[HANDLE] ?: "",
                phone = prefs[PHONE] ?: "",
                email = prefs[EMAIL] ?: "",
                profileCompleted = prefs[PROFILE_COMPLETED],
                accessToken = prefs[ACCESS_TOKEN],
                refreshToken = prefs[REFRESH_TOKEN],
                profilePictureUrl = prefs[PROFILE_PIC]
            )
        }

    suspend fun isLoggedIn(context: Context): Boolean =
        context.sessionStore.data.map { it[LOGGED_IN] == true }.first()

    suspend fun save(
        context: Context,
        name: String,
        handle: String,
        phone: String = "",
        email: String = "",
        profileCompleted: Boolean? = null,
        accessToken: String? = null,
        refreshToken: String? = null,
        profilePictureUrl: String? = null
    ) {
        context.sessionStore.edit { prefs ->
            prefs[LOGGED_IN] = true
            if (name.isBlank() || name.trim() == "Rida Rider") prefs.remove(NAME)
            else prefs[NAME] = name.trim()
            prefs[HANDLE] = handle
            if (phone.isNotBlank()) prefs[PHONE] = phone
            if (email.isNotBlank()) prefs[EMAIL] = email
            if (profileCompleted == null) prefs.remove(PROFILE_COMPLETED)
            else prefs[PROFILE_COMPLETED] = profileCompleted
            if (!accessToken.isNullOrBlank()) prefs[ACCESS_TOKEN] = accessToken
            if (!refreshToken.isNullOrBlank()) prefs[REFRESH_TOKEN] = refreshToken
            if (!profilePictureUrl.isNullOrBlank()) prefs[PROFILE_PIC] = profilePictureUrl
        }
    }

    suspend fun saveGoogleSession(
        context: Context,
        result: GoogleAuthManager.GoogleUserResult
    ) {
        context.sessionStore.edit { prefs ->
            prefs[LOGGED_IN] = true
            if (result.displayName.isBlank()) prefs.remove(NAME)
            else prefs[NAME] = result.displayName.trim()
            prefs[HANDLE] = "@${result.email.substringBefore("@")}"
            prefs[EMAIL] = result.email
            prefs[ACCESS_TOKEN] = result.idToken
            result.profilePictureUrl?.let { prefs[PROFILE_PIC] = it }
        }
    }

    suspend fun updateTokens(context: Context, newAccessToken: String, newRefreshToken: String? = null) {
        context.sessionStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = newAccessToken
            if (!newRefreshToken.isNullOrBlank()) {
                prefs[REFRESH_TOKEN] = newRefreshToken
            }
        }
    }

    suspend fun updateProfile(context: Context, name: String, phone: String, email: String) {
        context.sessionStore.edit { prefs ->
            if (name.isNotBlank()) prefs[NAME] = name.trim() else prefs.remove(NAME)
            if (phone.isNotBlank()) prefs[PHONE] = phone else prefs.remove(PHONE)
            if (email.isNotBlank()) {
                prefs[EMAIL] = email
                if (prefs[HANDLE].isNullOrBlank()) prefs[HANDLE] = email
            }
            prefs.remove(PROFILE_COMPLETED)
        }
    }

    suspend fun clear(context: Context) {
        context.sessionStore.edit { it.clear() }
    }
}

/** Shared validation for email/password auth fields and non-auth phone forms. */
object AuthValidation {
    fun emailError(value: String): String? =
        com.example.ridepassenger2.ui.common.TzEmail.validate(value)?.let {
            if (value.isBlank()) "Enter your email address" else "Enter a valid email address"
        }

    fun phoneError(value: String): String? =
        if (com.example.ridepassenger2.ui.common.TzPhone.normalize(value) == null) {
            "Enter a valid Tanzania phone number"
        } else null

    fun passwordError(value: String): String? {
        if (value.isEmpty()) return "Enter your password"
        if (value.length < 6) return "Password must be at least 6 characters"
        return null
    }

}
