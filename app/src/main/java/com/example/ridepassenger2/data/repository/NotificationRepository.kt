package com.example.ridepassenger2.data.repository

import android.content.Context
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.remote.InboxItem
import com.example.ridepassenger2.data.remote.InboxPage
import com.example.ridepassenger2.data.remote.RidaApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object NotificationRepository : PassengerNotificationRepository()

open class PassengerNotificationRepository(
    private val apiProvider: () -> com.example.ridepassenger2.data.remote.RidaApi = { RidaApiClient.api }
) {

    private val lock = Mutex()
    private val boundary = Any()
    private var generation = 0L
    private fun snapshot(): Long = synchronized(boundary) { generation }
    private fun publish(owner: Long, block: () -> Unit) = synchronized(boundary) {
        if (generation == owner) block()
    }

    fun reset() = synchronized(boundary) {
        generation++
        stopLongPolling()
        _inboxPage.value = InboxPage()
        _notifications.value = emptyList()
        _unreadCount.value = 0
        _isLoading.value = false
        latestId = null
    }

    private val _inboxPage = MutableStateFlow(InboxPage())
    val inboxPage: StateFlow<InboxPage> = _inboxPage.asStateFlow()

    private val _notifications = MutableStateFlow<List<InboxItem>>(emptyList())
    val notifications: StateFlow<List<InboxItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var latestId: String? = null
    private var longPollJob: Job? = null

    suspend fun refreshInbox(context: Context? = null, limit: Int = 20, offset: Int = 0) = lock.withLock {
        val owner = snapshot()
        try {
            _isLoading.value = true
            val page = apiProvider().getInbox(limit = limit, offset = offset)
            currentCoroutineContext().ensureActive()
            publish(owner) {
            _inboxPage.value = page
            _unreadCount.value = page.unreadCount

            val newItems = page.notifications
            if (offset == 0) {
                _notifications.value = newItems
            } else {
                val existingIds = _notifications.value.map { it.id }.toSet()
                val merged = _notifications.value + newItems.filterNot { it.id in existingIds }
                _notifications.value = merged
            }

            latestId = _notifications.value.firstOrNull()?.id
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) {
            // Silently handle transient errors
        } finally {
            publish(owner) { _isLoading.value = false }
        }
    }

    suspend fun markRead(id: String) = lock.withLock {
        val owner = snapshot()
        try {
            apiProvider().markNotificationRead(id)
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            currentCoroutineContext().ensureActive()
            publish(owner) {
            val updated = _notifications.value.map { item ->
                if (item.id == id && item.readAt == null) {
                    item.copy(readAt = nowIso)
                } else item
            }
            val changed = _notifications.value.count { it.id == id && it.readAt.isNullOrBlank() }
            _notifications.value = updated
            _unreadCount.value = (_unreadCount.value - changed).coerceAtLeast(0)
            _inboxPage.value = _inboxPage.value.copy(unreadCount = _unreadCount.value,
                notifications = _inboxPage.value.notifications.map { item -> updated.find { it.id == item.id } ?: item })
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) {
            // Silently handle error
        }
    }

    suspend fun markAllRead() = lock.withLock {
        val owner = snapshot()
        try {
            apiProvider().markAllNotificationsRead()
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            currentCoroutineContext().ensureActive()
            publish(owner) {
            val updated = _notifications.value.map { item ->
                if (item.readAt == null) item.copy(readAt = nowIso) else item
            }
            _notifications.value = updated
            _unreadCount.value = 0L
            _inboxPage.value = _inboxPage.value.copy(unreadCount = 0,
                notifications = _inboxPage.value.notifications.map { item -> updated.find { it.id == item.id } ?: item })
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) {
            // Silently handle error
        }
    }

    fun startLongPolling(context: Context, scope: CoroutineScope) {
        stopLongPolling()

        longPollJob = scope.launch(Dispatchers.IO) {
            var backoffMs = 2000L

            while (isActive) {
                try {
                    val loggedIn = SessionManager.isLoggedIn(context)
                    if (!loggedIn) {
                        delay(3000L)
                        continue
                    }

                    // First ensure we have initial inbox data
                    if (_notifications.value.isEmpty()) {
                        refreshInbox(context)
                    }

                    val owner = snapshot()
                    val update = apiProvider().getInboxUpdates(after = latestId)
                    currentCoroutineContext().ensureActive()
                    backoffMs = 2000L // Reset backoff on successful response

                    if (update.changed) {
                        refreshInbox(context)
                        publish(owner) { update.latestId?.let { latestId = it } }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) {
                    // Transient error or 5xx/429 -> bounded backoff
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(15000L)
                }
            }
        }
    }

    fun stopLongPolling() {
        longPollJob?.cancel()
        longPollJob = null
    }
}
