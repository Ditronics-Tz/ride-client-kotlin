package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.*
import com.example.ridepassenger2.data.local.SessionWriteGuard
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume

class SecondaryScreenStateTest {
    private fun api(handler: (String, Array<out Any?>) -> Any?): RidaApi =
        Proxy.newProxyInstance(RidaApi::class.java.classLoader, arrayOf(RidaApi::class.java)) { _, m, args -> handler(m.name, args ?: emptyArray()) } as RidaApi
    @Test fun unresolvedSavedPlaceNeverSendsCityCenterOrCommitsCache() = runBlocking {
        var calls = 0; var cached = false
        val writer = SavedPlaceWriter(api { _, _ -> calls++; error("Unexpected call") }, { null }) { _, _ -> cached = true }
        try { writer.save("home", null, "Home", "Unknown address"); fail("Unresolved address accepted") } catch (_: IllegalArgumentException) { }
        assertEquals(0, calls); assertFalse(cached)
    }
    @Test fun failedSavedPlaceDoesNotCommitLocalShortcut() = runBlocking {
        var cached = false
        val writer = SavedPlaceWriter(api { _, _ -> throw java.io.IOException("offline") }, { PointDto(-6.8,39.2,it) }) { _, _ -> cached = true }
        try { writer.save("home", null, "Home", "Actual address"); fail("Expected failure") } catch (_: Exception) { }
        assertFalse(cached)
    }
    @Test fun successfulSavedPlaceCommitsAcknowledgedAddress() = runBlocking {
        var cached = ""
        val saved = SavedPlaceDto("id", "home", "Home", "Server address", -6.8, 39.2)
        val writer = SavedPlaceWriter(api { _, _ -> saved }, { PointDto(-6.8,39.2,it) }) { _, address -> cached = address }
        assertEquals(saved, writer.save("home", null, "Home", "Typed address"))
        assertEquals("Server address", cached)
    }
    @Test fun failedPreferenceSaveRetainsAcknowledgedSettingAndOffersRetry() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val editor = CommunicationPreferences(api { name, _ -> if (name == "getPreferences") PassengerPreferencesDto(rideUpdates=true) else throw java.io.IOException("offline") }, scope)
            editor.refresh(); editor.change("ride_updates", false)
            assertEquals(true, editor.state.value.preferences.rideUpdates)
            assertNotNull(editor.state.value.error)
            assertFalse(editor.state.value.saving)
        } finally { scope.cancel() }
    }
    @Test fun preferencesCannotBeEditedWhileInitialLoadOrSaveIsPending() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var changes = 0
            lateinit var response: Continuation<PassengerPreferencesDto>
            val editor = CommunicationPreferences(api { name, args ->
                if (name == "getPreferences") PassengerPreferencesDto() else {
                    changes++
                    @Suppress("UNCHECKED_CAST")
                    response = args.last() as Continuation<PassengerPreferencesDto>
                    COROUTINE_SUSPENDED
                }
            }, scope)
            editor.change("ride_updates", false); assertEquals(0, changes)
            editor.refresh(); editor.change("ride_updates", false); editor.change("sms_enabled", false)
            assertEquals(1, changes)
            response.resume(PassengerPreferencesDto(rideUpdates=false))
            assertEquals(false, editor.state.value.preferences.rideUpdates)
        } finally { scope.cancel() }
    }
    @Test fun staleProfileRefreshCannotRecreateLogoutOrOverwriteNewLogin() {
        assertFalse(SessionWriteGuard.canApply(false, "", "old-login"))
        assertFalse(SessionWriteGuard.canApply(true, "new-login", "old-login"))
        assertTrue(SessionWriteGuard.canApply(true, "same-login", "same-login"))
        assertTrue(SessionWriteGuard.canApply(false, "", null)) // explicit new login
    }
}
