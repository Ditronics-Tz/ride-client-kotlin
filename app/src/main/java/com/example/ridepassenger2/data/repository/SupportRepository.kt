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

object SupportRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refresh() {
        scope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = RidaApiClient.api.getSupportTickets()
                _tickets.value = resp.tickets.map { dto ->
                    Ticket(
                        id = dto.id,
                        subject = dto.subject,
                        topic = mapTicketTopic(dto.category),
                        status = mapTicketStatus(dto.status),
                        updated = formatRideTimestamp(dto.createdAt)
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not load support tickets"
                _tickets.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun createTicket(subject: String, description: String, category: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val ticket = RidaApiClient.api.createSupportTicket(
                    CreateTicketRequest(subject = subject, description = description, category = category)
                )
                refresh()
                onSuccess(ticket.id)
            } catch (e: Exception) {
                onError(e.message ?: "Could not submit request")
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
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun sendMessage(ticketId: String, body: String, onDone: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                RidaApiClient.api.sendTicketMessage(
                    ticketId,
                    RidaApiClient.generateIdempotencyKey(),
                    CreateTicketMessageRequest(body = body)
                )
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Could not send message")
            }
        }
    }
}
