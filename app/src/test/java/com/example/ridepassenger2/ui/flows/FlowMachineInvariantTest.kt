package com.example.ridepassenger2.ui.flows

import com.example.ridepassenger2.ui.common.*
import com.example.ridepassenger2.ui.payment.*
import com.example.ridepassenger2.ui.profile.*
import org.junit.Assert.*
import org.junit.Test

class FlowMachineInvariantTest {
    @Test fun approvalBeforeVerificationCannotCreatePaymentSuccess() {
        val initial = MmSetupState()
        assertEquals(initial, mmReduce(initial, MmEvent.Approved))
    }
    @Test fun delayedDeclineCannotUndoSuccessfulVerification() {
        val success = MmSetupState(phase = MmVerifyPhase.SUCCESS)
        assertEquals(success, mmReduce(success, MmEvent.Declined))
    }
    @Test fun emptyOtpCannotVerifyPhone() {
        val state = PhoneChangeState(step = PhoneStep.OTP)
        assertNotEquals(PhoneStep.SUCCESS, phoneSubmitOtp(state, "").step)
    }
    @Test fun correctOtpCannotSkipReauthenticationAndPhoneEntry() {
        val state = PhoneChangeState(otp = OtpState(code = "123456"))
        assertNotEquals(PhoneStep.SUCCESS, phoneSubmitOtp(state, "123456").step)
    }
}
