package com.example.ridepassenger2.ui.profile

import com.example.ridepassenger2.ui.common.NameRules
import com.example.ridepassenger2.ui.common.OtpState
import com.example.ridepassenger2.ui.common.TzEmail
import com.example.ridepassenger2.ui.common.TzPhone
import com.example.ridepassenger2.ui.common.otpSubmit

/** Profile-editing pure state. */

// Edit name
data class NameEditState(val name: String = "", val saving: Boolean = false, val error: String? = null, val dirty: Boolean = false)
fun nameChange(s: NameEditState, v: String) = s.copy(name = v, dirty = true, error = null)
fun nameValidate(s: NameEditState): NameEditState {
    val e = NameRules.error(s.name)
    return if (e != null) s.copy(error = e) else s.copy(error = null, saving = true)
}

// Change phone
enum class PhoneStep { REAUTH, ENTER_NEW, OTP, SUCCESS }
data class PhoneChangeState(
    val step: PhoneStep = PhoneStep.REAUTH,
    val password: String = "",
    val googleOnly: Boolean = false,
    val newRaw: String = "",
    val newError: String? = null,
    val otp: OtpState = OtpState(resendInSec = 30),
    val oldMasked: String = "+255 7** *** 100"
)
fun phoneEnterNew(s: PhoneChangeState, v: String) = s.copy(newRaw = v, newError = null)
fun phoneValidateNew(s: PhoneChangeState): PhoneChangeState {
    val n = TzPhone.normalize(s.newRaw)
    if (n == null) return s.copy(newError = "Namba si sahihi")
    if (n == "+255712000001") return s.copy(newError = "Namba tayari inatumika")
    return s.copy(newError = null, step = PhoneStep.OTP, otp = OtpState(resendInSec = 30))
}
fun phoneSubmitOtp(s: PhoneChangeState, expected: String): PhoneChangeState {
    val next = otpSubmit(s.otp, expected)
    return if (next.error == null && s.otp.code == expected) s.copy(otp = next, step = PhoneStep.SUCCESS)
    else s.copy(otp = next)
}

// Change email
enum class EmailStep { REAUTH, ENTER_NEW, SENT, SUCCESS, EXPIRED }
data class EmailChangeState(
    val step: EmailStep = EmailStep.REAUTH,
    val password: String = "",
    val newEmail: String = "",
    val error: String? = null,
    val resendInSec: Int = 60
)
fun emailValidate(s: EmailChangeState): EmailChangeState {
    val e = TzEmail.validate(s.newEmail)
    return if (e != null) s.copy(error = e) else s.copy(error = null, step = EmailStep.SENT, resendInSec = 60)
}

// Password strength 0..3
fun passwordStrength(pw: String): Int {
    var s = 0
    if (pw.length >= 8) s++
    if (pw.any { it.isDigit() } && pw.any { it.isLetter() }) s++
    if (pw.any { !it.isLetterOrDigit() }) s++
    return s
}
