package com.example.ridepassenger2.ui.payment

import com.example.ridepassenger2.ui.common.TzMoney
import com.example.ridepassenger2.ui.common.TzPhone

/** Passenger payment domain. Pure state holders — unit-testable, no Android. */

object MmProviders {
    // Keep provider list in one config.
    val ALL = listOf("M-Pesa", "Tigo Pesa", "Airtel Money", "Halopesa")
}

data class PayMethod(val id: String, val kind: String, val label: String, val masked: String, val isDefault: Boolean)

sealed interface MethodsUiState {
    data class Content(val methods: List<PayMethod>) : MethodsUiState
    data object Loading : MethodsUiState
    data object Empty : MethodsUiState
    data class Error(val message: String) : MethodsUiState
}

fun defaultMethod(methods: List<PayMethod>): PayMethod? = methods.firstOrNull { it.isDefault } ?: methods.firstOrNull()

// --- Mobile-money verification state machine ---
enum class MmVerifyPhase { ENTER, CONFIRM, WAITING, APPROVED, DECLINED, TIMED_OUT, SUCCESS, FAILED }

data class MmSetupState(
    val provider: String = "M-Pesa",
    val phoneRaw: String = "",
    val phoneError: String? = null,
    val phase: MmVerifyPhase = MmVerifyPhase.ENTER,
    val resendInSec: Int = 0,
    val failReason: String? = null
)

sealed interface MmEvent {
    data class PickProvider(val p: String) : MmEvent
    data class EnterPhone(val v: String) : MmEvent
    data object ToConfirm : MmEvent
    data object StartVerify : MmEvent
    data object Approved : MmEvent
    data object Declined : MmEvent
    data object TimedOut : MmEvent
    data object Retry : MmEvent
    data object Resend : MmEvent
    data object Tick : MmEvent
}

fun mmReduce(s: MmSetupState, e: MmEvent): MmSetupState {
    val editing = s.phase in setOf(MmVerifyPhase.ENTER, MmVerifyPhase.CONFIRM)
    val failed = s.phase in setOf(MmVerifyPhase.DECLINED, MmVerifyPhase.TIMED_OUT, MmVerifyPhase.FAILED)
    return when (e) {
        is MmEvent.PickProvider -> if (editing) s.copy(provider = e.p, phoneError = null, phase = MmVerifyPhase.ENTER) else s
        is MmEvent.EnterPhone -> if (editing) s.copy(phoneRaw = e.v, phoneError = null, phase = MmVerifyPhase.ENTER) else s
        MmEvent.ToConfirm -> if (s.phase != MmVerifyPhase.ENTER) s else {
            val error = TzPhone.validateForProvider(s.phoneRaw, s.provider)
            if (error != null) s.copy(phoneError = error) else s.copy(phase = MmVerifyPhase.CONFIRM)
        }
        MmEvent.StartVerify -> if (s.phase == MmVerifyPhase.CONFIRM)
            s.copy(phase = MmVerifyPhase.WAITING, resendInSec = 30, failReason = null) else s
        MmEvent.Approved -> if (s.phase == MmVerifyPhase.WAITING) s.copy(phase = MmVerifyPhase.SUCCESS) else s
        MmEvent.Declined -> if (s.phase == MmVerifyPhase.WAITING)
            s.copy(phase = MmVerifyPhase.DECLINED, failReason = "Umekataa ombi") else s
        MmEvent.TimedOut -> if (s.phase == MmVerifyPhase.WAITING)
            s.copy(phase = MmVerifyPhase.TIMED_OUT, failReason = "Muda umeisha") else s
        MmEvent.Retry -> if (failed) s.copy(phase = MmVerifyPhase.WAITING, resendInSec = 30, failReason = null) else s
        MmEvent.Resend -> if ((failed || s.phase == MmVerifyPhase.WAITING) && s.resendInSec <= 0)
            s.copy(phase = MmVerifyPhase.WAITING, resendInSec = 30, failReason = null) else s
        MmEvent.Tick -> if (s.resendInSec > 0) s.copy(resendInSec = s.resendInSec - 1) else s
    }
}

// --- History ---
enum class PayStatus { PAID, PENDING, FAILED, REFUNDED }
enum class PayFilter { ALL, PAID, FAILED, REFUNDED }

data class PaymentRecord(
    val id: String, val route: String, val month: String,
    val amountTzs: Int, val status: PayStatus, val methodMasked: String,
    val time: String, val ref: String, val discountTzs: Int = 0
) {
    val amountLabel: String get() = TzMoney.format(amountTzs)
}

fun filterHistory(all: List<PaymentRecord>, f: PayFilter): List<PaymentRecord> = when (f) {
    PayFilter.ALL -> all
    PayFilter.PAID -> all.filter { it.status == PayStatus.PAID }
    PayFilter.FAILED -> all.filter { it.status == PayStatus.FAILED }
    PayFilter.REFUNDED -> all.filter { it.status == PayStatus.REFUNDED }
}

// --- Pay for a trip ---
enum class PayTripPhase { PICK, WAITING, FAILED, SUCCESS }

data class PayTripState(
    val methodId: String = "cash",
    val phase: PayTripPhase = PayTripPhase.PICK,
    val error: String? = null
)

fun ticketStatusLabel(open: Boolean, inReview: Boolean, resolved: Boolean): String = when {
    resolved -> "resolved"; inReview -> "in review"; open -> "open"; else -> "closed"
}
