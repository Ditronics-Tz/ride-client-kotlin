package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.osmdroid.util.GeoPoint
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume

class RideStateMachineTest {
    private fun ride(status: String = "assigned") = RideResponse("ride", status,
        pickup = PointDto(-6.8, 39.2, "Pickup"), dropoff = PointDto(-6.9, 39.3, "Dropoff"))
    private class Harness : AutoCloseable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var handler: (String, Array<out Any?>) -> Any? = { name, _ ->
            when (name) { "getRides" -> RideListResponse(); "getQuote" -> RideQuoteResponse(2000, 1000, 300); else -> error(name) }
        }
        val api = Proxy.newProxyInstance(RidaApi::class.java.classLoader, arrayOf(RidaApi::class.java)) { _, m, args -> handler(m.name, args ?: emptyArray()) } as RidaApi
        val repo = PassengerRideRepository({ api }, scope, { "key-${++keys}" },
            resolvePoint = { PointDto(-6.8, 39.2, it) },
            now = { java.time.Instant.parse("2026-11-07T00:00:00Z") }, callbackDispatcher = Dispatchers.Unconfined)
        var keys = 0
        fun restore(value: RideResponse) { handler = { name, _ -> if (name == "getRides") RideListResponse(listOf(value)) else error(name) }; repo.restoreActiveRideIfAny() }
        fun route() { repo.saveRoute(GeoPoint(-6.8, 39.2), GeoPoint(-6.9, 39.3), "Dropoff") }
        override fun close() { scope.cancel() }
    }
    @Test fun failedCancellationKeepsActiveRideAndShowsRetryError() = Harness().use { h ->
        h.restore(ride())
        h.handler = { name, _ -> if (name == "getRides") RideListResponse() else throw java.io.IOException("offline") }
        h.repo.cancelRide()
        assertEquals("ride", h.repo.activeRide.value?.rideId)
        assertNotNull(h.repo.rideActionError.value)
    }
    @Test fun doubleTapWhileCreateSuspendedOnlySendsOneCommand() = Harness().use { h ->
        h.route()
        var calls = 0
        h.handler = { name, _ -> if (name == "createRide") { calls++; COROUTINE_SUSPENDED } else error(name) }
        repeat(2) { h.repo.requestRide("Pickup", "Dropoff", "2000", "Standard") }
        assertEquals(1, calls)
    }
    @Test fun retryAfterLostCreateResponseReusesIdempotencyKey() = Harness().use { h ->
        h.route()
        val keys = mutableListOf<String>()
        h.handler = { name, args -> if (name == "createRide") { keys += args[0] as String; throw java.io.IOException("response lost") } else error(name) }
        repeat(2) { h.repo.requestRide("Pickup", "Dropoff", "2000", "Standard") }
        assertEquals(2, keys.size)
        assertEquals(keys[0], keys[1])
    }
    @Test fun backendTerminalStatusesNeverRestoreAsSearchingRides() = Harness().use { h ->
        for (status in listOf("cancelled_by_passenger", "cancelled_by_driver", "cancelled_by_admin", "no_driver_found", "expired", "finished")) {
            h.restore(ride(status))
            assertNull("terminal status $status", h.repo.activeRide.value)
        }
        assertEquals(RidePhase.ARRIVING, mapStatusToPhase("driver_arrived"))
    }
    @Test fun staleQuoteCannotReplaceCurrentRouteQuote() = Harness().use { h ->
        val requests = mutableListOf<Continuation<RideQuoteResponse>>()
        h.handler = { name, args -> if (name == "getQuote") {
            @Suppress("UNCHECKED_CAST")
            requests += args.last() as Continuation<RideQuoteResponse>
            COROUTINE_SUSPENDED
        } else error(name) }
        h.route(); h.route()
        requests[1].resume(RideQuoteResponse(3000, 1000, 300))
        requests[0].resume(RideQuoteResponse(1000, 1000, 300))
        assertEquals(3000L, h.repo.quoteTzs.value)
    }
    @Test fun historicalRebookingResolvesRouteAfterClear() = Harness().use { h ->
        var calls = 0
        h.handler = { name, _ -> when (name) {
            "createRide" -> { calls++; ride("searching") }
            "getQuote" -> RideQuoteResponse(2000, 1000, 300)
            else -> error(name)
        } }
        h.repo.clearRoute()
        h.repo.requestRide("Historical pickup", "Historical dropoff", "2000", "Standard")
        assertEquals(1, calls)
        assertEquals("ride", h.repo.activeRide.value?.rideId)
    }
    @Test fun logoutDiscardsLateRestoreAndAllPrivateState() = Harness().use { h ->
        lateinit var response: Continuation<RideListResponse>
        h.handler = { _, args ->
            @Suppress("UNCHECKED_CAST")
            response = args.last() as Continuation<RideListResponse>
            COROUTINE_SUSPENDED
        }
        h.repo.restoreActiveRideIfAny()
        h.repo.reset()
        response.resume(RideListResponse(listOf(ride())))
        assertNull(h.repo.activeRide.value)
        assertTrue(h.repo.history.value.isEmpty())
        assertFalse(h.repo.rideActionPending.value)
    }

    @Test fun scheduleSuccessWaitsForServerAndDoubleTapIsIgnored() = Harness().use { h ->
        lateinit var response: Continuation<ScheduledRideResponse>
        var calls = 0; var acknowledged = false
        h.handler = { name, args -> when(name) {
            "createScheduledRide" -> { calls++
                @Suppress("UNCHECKED_CAST")
                response = args.last() as Continuation<ScheduledRideResponse>; COROUTINE_SUSPENDED }
            "getScheduledRides" -> ScheduledRideListResponse()
            else -> error(name)
        } }
        repeat(2) { h.repo.scheduleRide("Pickup", "Dropoff", "Tomorrow", "10:00 AM", "Standard", "2000") { acknowledged = true } }
        assertEquals(1, calls); assertFalse(acknowledged); assertTrue(h.repo.schedulePending.value)
        response.resume(ScheduledRideResponse("scheduled", "upcoming", PointDto(-6.8,39.2,"Pickup"), PointDto(-6.9,39.3,"Dropoff"), scheduledFor="2026-11-08T07:00:00Z"))
        assertTrue(acknowledged); assertFalse(h.repo.schedulePending.value)
    }
    @Test fun ambiguousScheduleRetryRetainsKeyAndNeverClaimsSuccess() = Harness().use { h ->
        val keys = mutableListOf<String>(); var acknowledged = false
        h.handler = { name, args -> if(name == "createScheduledRide") { keys += args[0] as String; throw java.io.IOException("response lost") } else error(name) }
        repeat(2) { h.repo.scheduleRide("Pickup", "Dropoff", "Tomorrow", "10:00 AM", "Standard", "2000") { acknowledged = true } }
        assertEquals(2, keys.size); assertEquals(keys[0], keys[1]); assertFalse(acknowledged)
        assertNotNull(h.repo.rideActionError.value)
    }
    @Test fun invalidScheduledTimeDoesNotSilentlyBookTenAm() = Harness().use { h ->
        var calls = 0
        h.handler = { _, _ -> calls++; error("No booking should be sent") }
        h.repo.scheduleRide("Pickup", "Dropoff", "Tomorrow", "invalid time", "Standard", "2000")
        assertEquals(0, calls); assertNotNull(h.repo.rideActionError.value)
        assertFalse(h.repo.schedulePending.value)
    }
    @Test fun olderHistoryCannotEraseNewerCompletedRide() = Harness().use { h ->
        val responses = mutableListOf<Continuation<RideListResponse>>()
        h.handler = { _, args ->
            @Suppress("UNCHECKED_CAST")
            responses += args.last() as Continuation<RideListResponse>; COROUTINE_SUSPENDED
        }
        h.repo.refreshHistory(); h.repo.refreshHistory()
        responses[1].resume(RideListResponse(listOf(ride("completed"))))
        responses[0].resume(RideListResponse())
        assertEquals("ride", h.repo.history.value.singleOrNull()?.rideId)
    }

    @Test fun notificationOpensRequestedHistoricalRideInsteadOfCurrentRide() = Harness().use { h ->
        h.restore(ride())
        h.handler = { name, args -> if(name == "getRide") { assertEquals("old-ride", args[0]); ride("completed").copy(id="old-ride") } else error(name) }
        var historical: Boolean? = null
        h.repo.openRideDetails("old-ride") { historical = it }
        assertEquals(true, historical)
        assertEquals("old-ride", h.repo.selectedHistory.value?.rideId)
        assertEquals("ride", h.repo.activeRide.value?.rideId)
    }

}
