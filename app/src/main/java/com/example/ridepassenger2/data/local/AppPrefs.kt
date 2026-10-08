package com.example.ridepassenger2.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appPrefsStore by preferencesDataStore("rida_prefs")

/**
 * Alive-app prefs: payment method, saved Home/Work, recents, ride prefs,
 * notification toggles. Everything the old "coming soon" toasts needed.
 */
object AppPrefs {

    private val PAYMENT = stringPreferencesKey("payment_method")
    private val HOME_QUERY = stringPreferencesKey("home_query")
    private val WORK_QUERY = stringPreferencesKey("work_query")
    private val RECENTS = stringSetPreferencesKey("recent_queries")
    private val SEAT_PREF = stringPreferencesKey("seat_pref")
    private val NOTIF_RIDES = booleanPreferencesKey("notif_rides")
    private val NOTIF_PROMOS = booleanPreferencesKey("notif_promos")
    private val SHARE_LOCATION = booleanPreferencesKey("share_location")

    val PAYMENT_OPTIONS = listOf("Cash", "M-Pesa", "Tigo Pesa", "HaloPesa", "Card")

    fun payment(context: Context): Flow<String> =
        context.appPrefsStore.data.map { it[PAYMENT] ?: "Cash" }

    suspend fun setPayment(context: Context, method: String) {
        context.appPrefsStore.edit { it[PAYMENT] = method }
    }

    fun homeQuery(context: Context): Flow<String?> =
        context.appPrefsStore.data.map { it[HOME_QUERY] }

    fun workQuery(context: Context): Flow<String?> =
        context.appPrefsStore.data.map { it[WORK_QUERY] }

    suspend fun setHome(context: Context, query: String) {
        context.appPrefsStore.edit { it[HOME_QUERY] = query }
    }

    suspend fun setWork(context: Context, query: String) {
        context.appPrefsStore.edit { it[WORK_QUERY] = query }
    }

    suspend fun clearPlace(context: Context, home: Boolean) {
        context.appPrefsStore.edit { it.remove(if (home) HOME_QUERY else WORK_QUERY) }
    }

    fun recents(context: Context): Flow<List<String>> =
        context.appPrefsStore.data.map { it[RECENTS]?.toList().orEmpty() }

    suspend fun pushRecent(context: Context, query: String) {
        val q = query.trim()
        if (q.length < 2) return
        context.appPrefsStore.edit { prefs ->
            val cur = (prefs[RECENTS] ?: emptySet()).toMutableList()
            cur.remove(q)
            cur.add(0, q)
            prefs[RECENTS] = cur.take(5).toSet()
        }
    }

    suspend fun clearRecents(context: Context) {
        context.appPrefsStore.edit { it[RECENTS] = emptySet() }
    }

    fun seatPref(context: Context): Flow<String> =
        context.appPrefsStore.data.map { it[SEAT_PREF] ?: "Any seat" }

    suspend fun setSeatPref(context: Context, value: String) {
        context.appPrefsStore.edit { it[SEAT_PREF] = value }
    }

    fun notifRides(context: Context): Flow<Boolean> =
        context.appPrefsStore.data.map { it[NOTIF_RIDES] ?: true }

    fun notifPromos(context: Context): Flow<Boolean> =
        context.appPrefsStore.data.map { it[NOTIF_PROMOS] ?: false }

    fun shareLocation(context: Context): Flow<Boolean> =
        context.appPrefsStore.data.map { it[SHARE_LOCATION] ?: true }

    suspend fun setNotifRides(context: Context, v: Boolean) {
        context.appPrefsStore.edit { it[NOTIF_RIDES] = v }
    }

    suspend fun setNotifPromos(context: Context, v: Boolean) {
        context.appPrefsStore.edit { it[NOTIF_PROMOS] = v }
    }

    suspend fun setShareLocation(context: Context, v: Boolean) {
        context.appPrefsStore.edit { it[SHARE_LOCATION] = v }
    }

    suspend fun getString(context: Context, key: String, fallback: String = ""): String {
        val prefs = context.appPrefsStore.data.first()
        return when (key) {
            "home" -> prefs[HOME_QUERY] ?: fallback
            "work" -> prefs[WORK_QUERY] ?: fallback
            "payment" -> prefs[PAYMENT] ?: "Cash"
            else -> fallback
        }
    }
}
