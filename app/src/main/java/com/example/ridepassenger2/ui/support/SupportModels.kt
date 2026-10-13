package com.example.ridepassenger2.ui.support

import com.example.ridepassenger2.ui.common.SupportFormRules

/** Support domain — pure. */
object SupportConfig { const val REVIEW_HOURS = 24 }

enum class Topic { ACCOUNT, PAYMENTS, SAFETY, COMPLAINTS, OTHER }
enum class TicketStatus { OPEN, IN_REVIEW, WAITING_FOR_YOU, RESOLVED, CLOSED }

fun ticketChip(s: TicketStatus): String = when (s) {
    TicketStatus.OPEN -> "open"; TicketStatus.IN_REVIEW -> "in review"
    TicketStatus.WAITING_FOR_YOU -> "waiting for you"; TicketStatus.RESOLVED -> "resolved"; TicketStatus.CLOSED -> "closed"
}

data class Ticket(val id: String, val subject: String, val topic: Topic, val status: TicketStatus, val updated: String)
data class TicketMsg(val fromUser: Boolean, val text: String, val system: Boolean = false)

data class RequestFormState(
    val subject: String = "",
    val description: String = "",
    val attachments: List<String> = emptyList(),
    val contact: String = "app",
    val submitting: Boolean = false,
    val error: String? = null,
    val draftSaved: Boolean = false
)

fun formErrors(s: RequestFormState): Map<String, String?> = mapOf(
    "subject" to SupportFormRules.subjectError(s.subject),
    "description" to SupportFormRules.descError(s.description),
    "attachments" to if (s.attachments.size > SupportFormRules.MAX_ATTACH) "Picha 3 pekee" else null
)

fun formValid(s: RequestFormState): Boolean = formErrors(s).values.all { it == null }

fun addAttachment(s: RequestFormState, name: String): RequestFormState {
    if (s.attachments.size >= SupportFormRules.MAX_ATTACH) return s.copy(error = "Picha 3 pekee")
    return s.copy(attachments = s.attachments + name, error = null, draftSaved = true)
}

val ACCOUNT_SUBS = listOf(
    "siwezi kuingia", "sikupata OTP", "badili namba yangu", "badili barua pepe",
    "akaunti imesimamishwa", "futa akaunti yangu", "kingine"
)
val PAY_PROBLEMS = listOf(
    "nimetozwa mara mbili", "nauli si sahihi", "malipo yameshindikana lakini pesa imetoka",
    "omba kurejeshewa", "promo haikutumika", "kingine"
)
val SAFETY_CATS = listOf("uendeshaji hatari", "manyanyaso", "gari halilingani", "sijisikii salama", "ajali", "kingine")
