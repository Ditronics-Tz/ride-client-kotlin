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
 * Mock session store (DataStore). Persists who is signed in so the app can
 * skip auth on relaunch and show the real user name on Profile.
 */
object SessionManager {

    private val LOGGED_IN = booleanPreferencesKey("logged_in")
    private val NAME = stringPreferencesKey("name")
    private val HANDLE = stringPreferencesKey("handle")

    data class Session(
        val loggedIn: Boolean = false,
        val name: String = "Rida Rider",
        val handle: String = ""
    )

    fun observe(context: Context): Flow<Session> =
        context.sessionStore.data.map { prefs ->
            Session(
                loggedIn = prefs[LOGGED_IN] == true,
                name = prefs[NAME] ?: "Rida Rider",
                handle = prefs[HANDLE] ?: ""
            )
        }

    suspend fun isLoggedIn(context: Context): Boolean =
        context.sessionStore.data.map { it[LOGGED_IN] == true }.first()

    suspend fun save(context: Context, name: String, handle: String) {
        context.sessionStore.edit { prefs ->
            prefs[LOGGED_IN] = true
            prefs[NAME] = name.ifBlank { "Rida Rider" }
            prefs[HANDLE] = handle
        }
    }

    suspend fun clear(context: Context) {
        context.sessionStore.edit { it.clear() }
    }
}

/** Cheap shared validators for the mock auth forms. */
object AuthValidation {
    private val EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val TZ_PHONE = Regex("^(\\+255|0)[67]\\d{8}$")

    fun identifierError(value: String): String? {
        val v = value.trim().replace(" ", "")
        if (v.isEmpty()) return "Enter your email or phone number"
        if ("@" in v) {
            if (!EMAIL.matches(v)) return "That email doesn't look right"
        } else if (!TZ_PHONE.matches(v)) {
            return "Use a valid number like 0712 345 678"
        }
        return null
    }

    fun passwordError(value: String): String? {
        if (value.isEmpty()) return "Enter your password"
        if (value.length < 6) return "Password must be at least 6 characters"
        return null
    }

    fun nameError(value: String): String? {
        if (value.trim().length < 2) return "Enter your full name"
        return null
    }

    /** Display name + handle derived from the sign-in identifier. */
    fun displayFor(identifier: String): Pair<String, String> {
        val v = identifier.trim()
        return if ("@" in v) {
            val nick = v.substringBefore("@").replace(".", " ").replace("_", " ")
                .split(" ").filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
            nick.ifBlank { "Rida Rider" } to "@${v.substringBefore("@")}"
        } else {
            "Rida Rider" to v
        }
    }
}
