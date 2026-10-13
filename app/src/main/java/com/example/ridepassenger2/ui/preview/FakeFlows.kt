package com.example.ridepassenger2.ui.preview

import com.example.ridepassenger2.ui.payment.MethodsUiState
import com.example.ridepassenger2.ui.payment.PayMethod
import com.example.ridepassenger2.ui.payment.PayStatus
import com.example.ridepassenger2.ui.payment.PaymentRecord
import com.example.ridepassenger2.ui.support.Ticket
import com.example.ridepassenger2.ui.support.TicketMsg
import com.example.ridepassenger2.ui.support.TicketStatus
import com.example.ridepassenger2.ui.support.Topic

/** Fake data + fake repository impls. Preview/debug only — never wired to network. */
object FakePayments {
    fun methods() = listOf(
        PayMethod("cash", "cash", "Cash", "Mlipie dereva", true),
        PayMethod("mpesa", "mm", "M-Pesa", "+255 7** *** 123", false),
        PayMethod("tigo", "mm", "Tigo Pesa", "+255 6** *** 456", false)
    )
    fun methodsState() = MethodsUiState.Content(methods())
    fun history() = listOf(
        PaymentRecord("p1", "Kariakoo → Mlimani", "Oktoba", 5000, PayStatus.PAID, "M-Pesa ••123", "Leo 08:24 • Africa/Dar_es_Salaam", "REF-88231", 500),
        PaymentRecord("p2", "Mbezi → Kariakoo", "Oktoba", 3800, PayStatus.PENDING, "Cash", "Jana 17:12", "REF-88200"),
        PaymentRecord("p3", "Upanga → Kariakoo", "Septemba", 4500, PayStatus.FAILED, "Tigo ••456", "Sep 28", "REF-87999"),
        PaymentRecord("p4", "Mikocheni → Kariakoo", "Septemba", 2800, PayStatus.REFUNDED, "M-Pesa ••123", "Sep 20", "REF-87710")
    )
    /** Canned failure cases for the debug flow runner toggles. */
    fun verifyFailure(kind: String) = when (kind) {
        "declined" -> "Umekataa ombi"
        "timeout" -> "Muda umeisha"
        else -> "Hitilafu ya mtandao. Jaribu tena"
    }
}

object FakeProfile {
    fun sessions() = listOf("Tecno Spark • Dar", "Samsung A12 • Arusha")
}

object FakeSupport {
    fun tickets() = listOf(
        Ticket("R-1042", "Nimetozwa mara mbili", Topic.PAYMENTS, TicketStatus.IN_REVIEW, "leo"),
        Ticket("R-1039", "OTP haikufika", Topic.ACCOUNT, TicketStatus.WAITING_FOR_YOU, "jana"),
        Ticket("R-1031", "Nauli si sahihi", Topic.PAYMENTS, TicketStatus.RESOLVED, "Sep 30")
    )
    fun thread() = listOf(
        TicketMsg(true, "Nimetozwa mara mbili leo."),
        TicketMsg(false, "Pole. Tunaangalia malipo yako."),
        TicketMsg(false, "Tiketi imepokelewa na timu.", system = true)
    )
}

/** Repository interfaces (main) with fake impls (preview). Real backend comes later. */
interface PayRepo {
    fun methods(): List<PayMethod>
    fun history(): List<PaymentRecord>
}
class FakePayRepo : PayRepo {
    override fun methods() = FakePayments.methods()
    override fun history() = FakePayments.history()
}
