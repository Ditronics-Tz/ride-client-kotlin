package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class NotificationStateMachineTest {
    @Test fun markingOnePageItemReadPreservesUnreadItemsOutsidePage() = runBlocking {
        val api = Proxy.newProxyInstance(RidaApi::class.java.classLoader, arrayOf(RidaApi::class.java)) { _, m, _ ->
            when (m.name) {
                "getInbox" -> InboxPage(notifications = listOf(InboxItem(id = "one", kind = "ride", title = "Update", body = "Ride", createdAt = "2026-11-07T00:00:00Z")), unreadCount = 50)
                "markNotificationRead" -> emptyMap<String, Any>()
                else -> error(m.name)
            }
        } as RidaApi
        val repo = PassengerNotificationRepository { api }
        repo.refreshInbox()
        repo.markRead("one")
        assertEquals(49, repo.unreadCount.value)
        repo.markRead("one")
        assertEquals(49, repo.unreadCount.value)
    }
    @Test fun logoutDiscardsDelayedInboxAndUnreadCount() = runBlocking {
        lateinit var response: kotlin.coroutines.Continuation<InboxPage>
        val api = Proxy.newProxyInstance(RidaApi::class.java.classLoader, arrayOf(RidaApi::class.java)) { _, _, args ->
            @Suppress("UNCHECKED_CAST")
            response = args.last() as kotlin.coroutines.Continuation<InboxPage>
            kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
        } as RidaApi
        val repo = PassengerNotificationRepository { api }
        val pending = launch(start = CoroutineStart.UNDISPATCHED) { repo.refreshInbox() }
        repo.reset()
        response.resumeWith(Result.success(InboxPage(unreadCount = 50)))
        pending.join()
        assertEquals(0, repo.unreadCount.value)
        assertTrue(repo.notifications.value.isEmpty())
        assertFalse(repo.isLoading.value)
    }

}
