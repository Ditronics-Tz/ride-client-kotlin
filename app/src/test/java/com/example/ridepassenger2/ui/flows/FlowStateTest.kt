package com.example.ridepassenger2.ui.flows

import com.example.ridepassenger2.ui.common.OtpState
import com.example.ridepassenger2.ui.common.SupportFormRules
import com.example.ridepassenger2.ui.common.TzEmail
import com.example.ridepassenger2.ui.common.TzMoney
import com.example.ridepassenger2.ui.common.TzPhone
import com.example.ridepassenger2.ui.common.otpEnter
import com.example.ridepassenger2.ui.common.otpSubmit
import com.example.ridepassenger2.ui.common.otpTick
import com.example.ridepassenger2.ui.payment.MmEvent
import com.example.ridepassenger2.ui.payment.MmSetupState
import com.example.ridepassenger2.ui.payment.MmVerifyPhase
import com.example.ridepassenger2.ui.payment.PayFilter
import com.example.ridepassenger2.ui.payment.PayStatus
import com.example.ridepassenger2.ui.payment.PaymentRecord
import com.example.ridepassenger2.ui.payment.filterHistory
import com.example.ridepassenger2.ui.payment.mmReduce
import com.example.ridepassenger2.ui.profile.passwordStrength
import com.example.ridepassenger2.ui.support.RequestFormState
import com.example.ridepassenger2.ui.support.TicketStatus
import com.example.ridepassenger2.ui.support.addAttachment
import com.example.ridepassenger2.ui.support.formValid
import com.example.ridepassenger2.ui.support.ticketChip
import org.junit.Assert.*
import org.junit.Test

class FlowStateTest {
    @Test fun phoneNormalise() {
        assertEquals("+255712345678", TzPhone.normalize("0712345678"))
        assertEquals("+255712345678", TzPhone.normalize("+255712345678"))
        assertEquals("+255712345678", TzPhone.normalize("712345678"))
        assertNull(TzPhone.normalize("123"))
    }
    @Test fun phoneProvider() {
        assertEquals("M-Pesa", TzPhone.providerOf("+255745000000"))
        assertEquals("Tigo Pesa", TzPhone.providerOf("+255710000000"))
        assertEquals("Airtel Money", TzPhone.providerOf("+255680000000"))
    }
    @Test fun phoneMismatch() {
        assertNotNull(TzPhone.validateForProvider("0710000000", "M-Pesa"))
        assertNull(TzPhone.validateForProvider("0745000000", "M-Pesa"))
    }
    @Test fun moneyWhole() {
        assertEquals("TSh 5,000", TzMoney.format(5000))
        assertEquals("TSh 0", TzMoney.format(-1))
    }
    @Test fun otpLockout() {
        var s = OtpState(code = "000000", attemptsLeft = 2)
        s = otpSubmit(s, "123456")
        assertEquals(1, s.attemptsLeft)
        s = otpSubmit(s.copy(code = "000000"), "123456")
        assertTrue(s.locked)
    }
    @Test fun otpEnterTick() {
        var s = OtpState(resendInSec = 2)
        s = otpTick(s)
        assertEquals(1, s.resendInSec)
        s = otpEnter(s, '5')
        assertEquals("5", s.code)
    }
    @Test fun emailValid() {
        assertNull(TzEmail.validate("juma@gmail.com"))
        assertNotNull(TzEmail.validate("juma@"))
        assertEquals("****@gmail.com", TzEmail.mask("juma@gmail.com"))
    }
    @Test fun mmTransitions() {
        var s = MmSetupState(provider = "M-Pesa", phoneRaw = "0745000000")
        s = mmReduce(s, MmEvent.ToConfirm)
        assertEquals(MmVerifyPhase.CONFIRM, s.phase)
        s = mmReduce(s, MmEvent.StartVerify)
        assertEquals(MmVerifyPhase.WAITING, s.phase)
        s = mmReduce(s, MmEvent.Declined)
        assertEquals(MmVerifyPhase.DECLINED, s.phase)
        s = mmReduce(s, MmEvent.Retry)
        assertEquals(MmVerifyPhase.WAITING, s.phase)
        s = mmReduce(s, MmEvent.Approved)
        assertEquals(MmVerifyPhase.SUCCESS, s.phase)
    }
    @Test fun mmResendCountdown() {
        var s = MmSetupState(phase = MmVerifyPhase.WAITING, resendInSec = 1)
        s = mmReduce(s, MmEvent.Tick)
        assertEquals(0, s.resendInSec)
        s = mmReduce(s, MmEvent.Resend)
        assertEquals(30, s.resendInSec)
    }
    @Test fun historyFilter() {
        val all = listOf(
            PaymentRecord("a", "x", "m", 1, PayStatus.PAID, "", "", ""),
            PaymentRecord("b", "x", "m", 1, PayStatus.FAILED, "", "", "")
        )
        assertEquals(1, filterHistory(all, PayFilter.PAID).size)
        assertEquals(2, filterHistory(all, PayFilter.ALL).size)
    }
    @Test fun supportForm() {
        val bad = RequestFormState(subject = "x", description = "fupi")
        assertFalse(formValid(bad))
        val ok = RequestFormState(subject = "Nimetozwa", description = "Nimetozwa mara mbili leo asubuhi kabisa.")
        assertTrue(formValid(ok))
        val capped = (0..5).fold(ok) { acc, i -> addAttachment(acc, "p$i.jpg") }
        assertEquals(SupportFormRules.MAX_ATTACH, capped.attachments.size)
        assertNotNull(capped.error)
    }
    @Test fun ticketMap() {
        assertEquals("resolved", ticketChip(TicketStatus.RESOLVED))
        assertEquals("open", ticketChip(TicketStatus.OPEN))
    }
    @Test fun pwStrength() {
        assertTrue(passwordStrength("Jina123!") >= 2)
        assertEquals(0, passwordStrength("abc"))
    }
}
