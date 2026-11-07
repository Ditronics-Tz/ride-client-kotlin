package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RideLogoutConcurrencyTest {
    @Test fun logoutDuringPublicationCannotLeaveAPollingJobForThePreviousAccount() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val subscribers = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        var resetting: Thread? = null
        try {
            val ride = RideResponse("old-account-ride", "assigned",
                pickup = PointDto(-6.8,39.2,"Pickup"), dropoff = PointDto(-6.9,39.3,"Dropoff"))
            val api = Proxy.newProxyInstance(RidaApi::class.java.classLoader, arrayOf(RidaApi::class.java)) { _, m, _ ->
                if (m.name == "getRides") RideListResponse(listOf(ride)) else error(m.name)
            } as RidaApi
            val repo = PassengerRideRepository({ api }, scope)
            val owner = scope.coroutineContext[Job]!!.children.single()
            repo.activeRide.filterNotNull().onEach {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS)) { "Publication barrier timed out" }
            }.launchIn(subscribers)
            repo.restoreActiveRideIfAny()
            assertTrue("Restore never published", entered.await(5, TimeUnit.SECONDS))
            val restoring = owner.children.single()
            val resetDone = AtomicBoolean(false)
            resetting = Thread { repo.reset(); resetDone.set(true) }.apply { start() }
            // Allow either a reset that invalidates in-flight publication or one that
            // serializes with it. Never require a particular locking implementation.
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (!resetDone.get() && resetting.state != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                Thread.yield()
            }
            release.countDown()
            resetting.join(5000)
            assertTrue("Logout did not finish", resetDone.get())
            runBlocking { withTimeout(5000) { restoring.join() } }
            assertNull(repo.activeRide.value)
            assertFalse("Old work escaped logout and can poll using a later login", owner.children.any { it.isActive })
        } finally {
            release.countDown()
            resetting?.join(5000)
            subscribers.cancel()
            scope.cancel()
        }
    }
}
