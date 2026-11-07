package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.CreateTicketMessageRequest
import com.example.ridepassenger2.data.remote.CreateTicketRequest
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.ui.support.Ticket
import com.example.ridepassenger2.ui.support.TicketMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancelChildren

object SupportRepository {

    @Synchronized fun reset() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()
        _tickets.value = emptyList()
        _loading.value = false
        _error.value = null
    }

    private suspend fun <T> publish(block: () -> T): T {
        val job = currentCoroutineContext()[kotlinx.coroutines.Job]
        return synchronized(this) { job?.ensureActive(); block() }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @Synchronized fun refresh() {
        scope.launch {
            publish { _loading.value = true; _error.value = null }
            try {
                val resp = RidaApiClient.api.getSupportTickets()
                currentCoroutineContext().ensureActive()
                val tickets = resp.tickets.map { dto ->
                    Ticket(
                        id = dto.id,
                        subject = dto.subject,
                        topic = mapTicketTopic(dto.category),
                        status = mapTicketStatus(dto.status),
                        updated = formatRideTimestamp(dto.createdAt)
                    )
                }
                publish { _tickets.value = tickets }
            } catch (cancelled: CancellationException) { throw cancelled } catch (e: Exception) {
                publish { _error.value = e.message ?: "Could not load support tickets" }
            } finally {
                if (currentCoroutineContext().isActive) publish { _loading.value = false }
            }
        }
    }

    @Synchronized fun createTicket(subject: String, description: String, category: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val ticket = RidaApiClient.api.createSupportTicket(
                    CreateTicketRequest(subject = subject, description = description, category = category)
                )
                currentCoroutineContext().ensureActive()
                publish { refresh() }
                withContext(Dispatchers.Main) { publish { onSuccess(ticket.id) } }
            } catch (cancelled: CancellationException) { throw cancelled } catch (e: Exception) {
                withContext(Dispatchers.Main) { publish { onError(e.message ?: "Could not submit request") } }
            }
        }
    }

    suspend fun loadMessages(ticketId: String): List<TicketMsg> {
        return try {
            val resp = RidaApiClient.api.getTicketMessages(ticketId)
            resp.messages.map { m ->
                val fromUser = m.sender?.lowercase() != "agent" && m.sender?.lowercase() != "support"
                TicketMsg(fromUser = fromUser, text = m.body, system = m.sender == null)
            }
        } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized fun sendMessage(ticketId: String, body: String, onDone: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                RidaApiClient.api.sendTicketMessage(
                    ticketId,
                    RidaApiClient.generateIdempotencyKey(),
                    CreateTicketMessageRequest(body = body)
                )
                withContext(Dispatchers.Main) { publish { onDone() } }
            } catch (cancelled: CancellationException) { throw cancelled } catch (e: Exception) {
                withContext(Dispatchers.Main) { publish { onError(e.message ?: "Could not send message") } }
            }
        }
    }
}
