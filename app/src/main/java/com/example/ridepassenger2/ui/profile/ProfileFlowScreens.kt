package com.example.ridepassenger2.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.common.ChipTone
import com.example.ridepassenger2.ui.common.FlowRow
import com.example.ridepassenger2.ui.common.FlowScaffold
import com.example.ridepassenger2.ui.common.MapPlaceholder
import com.example.ridepassenger2.ui.common.OtpState
import com.example.ridepassenger2.ui.common.RidaField
import com.example.ridepassenger2.ui.common.RidaPrimaryButton
import com.example.ridepassenger2.ui.common.StatusChip
import com.example.ridepassenger2.ui.common.VerifyBanner
import com.example.ridepassenger2.ui.common.VerifyChip
import com.example.ridepassenger2.ui.common.VerifyState
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.preview.FakeProfile
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

// ---- Edit name ----
@Composable
fun EditNameScreen(state: NameEditState, onChange: (String) -> Unit, onSave: () -> Unit, onBack: () -> Unit) {
    var showDiscard by remember { mutableStateOf(false) }
    val tryBack = { if (state.dirty && !state.saving) showDiscard = true else onBack() }
    FlowScaffold(title = "Hariri jina", onBack = tryBack) {
        RidaField(value = state.name, onValueChange = onChange, label = "Jina kamili", placeholder = "Juma K.", error = state.error)
        Spacer(Modifier.height(12.dp))
        RidaPrimaryButton(text = "Hifadhi", onClick = onSave, loading = state.saving)
    }
    if (showDiscard) AlertDialog(
        onDismissRequest = { showDiscard = false },
        title = { Text("Ondoa mabadiliko?") },
        text = { Text("Mabadiliko ambayo hujahifadhi yatapotea.") },
        confirmButton = { TextButton(onClick = { showDiscard = false; onBack() }) { Text("Ondoa") } },
        dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Endelea") } }
    )
}

// ---- Change phone ----
@Composable
fun ChangePhoneScreen(state: PhoneChangeState, onPassword: (String) -> Unit, onReauth: () -> Unit, onNew: (String) -> Unit, onNext: () -> Unit, onOtp: (String) -> Unit, onVerify: () -> Unit, onResend: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Badili namba", onBack = onBack) {
        when (state.step) {
            PhoneStep.REAUTH -> {
                Text("Thibitisha ni wewe", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (state.googleOnly) Text("Akaunti ya Google: ingia tena na Google.", fontSize = 13.sp)
                else RidaField(value = state.password, onValueChange = onPassword, label = "Nenosiri", placeholder = "••••••••")
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Endelea", onClick = onReauth)
            }
            PhoneStep.ENTER_NEW -> {
                RidaField(value = state.newRaw, onValueChange = onNew, label = "Namba mpya", placeholder = "0712 345 678", error = state.newError)
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Tuma kodi", onClick = onNext)
            }
            PhoneStep.OTP -> {
                Text("Tumetuma kodi kwenye namba MPYA", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                OtpBoxes(code = state.otp.code, onDigit = onOtp)
                if (state.otp.error != null) Text(state.otp.error!!, color = Color(0xFFB91C1C), fontSize = 12.sp)
                Text("Majaribio: ${state.otp.attemptsLeft}", fontSize = 12.sp, color = Color.Gray)
                Text(if (state.otp.resendInSec > 0) "Tuma tena ${state.otp.resendInSec}s" else "Tuma tena", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Thibitisha", onClick = onVerify)
                if (state.otp.locked) VerifyBanner("Umefungiwa kwa majaribio mengi. Jaribu baadaye.", ChipTone.BAD)
            }
            PhoneStep.SUCCESS -> {
                StatusChip("Imekamilika", ChipTone.OK); Spacer(Modifier.height(8.dp))
                Text("Namba imebadilishwa. ${state.oldMasked} si halali tena.", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun OtpBoxes(code: String, onDigit: (String) -> Unit) {
    // Simplified single field for low-end + tests; 6-box UI is visual only in preview.
    RidaField(value = code, onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onDigit(it) }, label = "Kodi (tarakimu 6)", placeholder = "123456")
}

// ---- Change email ----
@Composable
fun ChangeEmailScreen(state: EmailChangeState, onPassword: (String) -> Unit, onReauth: () -> Unit, onNew: (String) -> Unit, onNext: () -> Unit, onResend: () -> Unit, onChangeEmail: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Badili barua pepe", onBack = onBack) {
        when (state.step) {
            EmailStep.REAUTH -> {
                Text("Thibitisha ni wewe", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
                RidaField(value = state.password, onValueChange = onPassword, label = "Nenosiri", placeholder = "••••••••")
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Endelea", onClick = onReauth)
            }
            EmailStep.ENTER_NEW -> {
                RidaField(value = state.newEmail, onValueChange = onNew, label = "Barua pepe mpya", placeholder = "jina@gmail.com", error = state.error)
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Tuma kiungo", onClick = onNext)
            }
            EmailStep.SENT -> {
                Text("Angalia barua pepe yako", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                Text("Tumetuma kiungo. Fungua ili kuthibitisha.", fontSize = 13.sp)
                Text(if (state.resendInSec > 0) "Tuma tena ${state.resendInSec}s" else "Tuma tena", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton(text = "Tuma tena", onClick = onResend)
            }
            EmailStep.SUCCESS -> { StatusChip("Imethibitishwa", ChipTone.OK) }
            EmailStep.EXPIRED -> {
                StatusChip("Kiungo kimeisha muda", ChipTone.BAD); Spacer(Modifier.height(8.dp))
                RidaPrimaryButton(text = "Badili barua pepe", onClick = onChangeEmail)
            }
        }
    }
}

// ---- Verification rows on profile overview ----
@Composable
fun ProfileVerifyRows(phone: VerifyState, email: VerifyState, onEditPhone: () -> Unit, onEditEmail: () -> Unit) {
    if (email == VerifyState.PENDING) VerifyBanner("Barua pepe haijathibitishwa. Angalia inbox yako.")
    Spacer(Modifier.height(8.dp))
    FlowRow(title = "Simu • +255 7** *** 123", subtitle = "Namba ya akaunti", onClick = onEditPhone, trailing = { VerifyChip(phone) })
    Spacer(Modifier.height(8.dp))
    FlowRow(title = "Barua pepe • ****@gmail.com", subtitle = "Kwa risiti na arifa", onClick = onEditEmail, trailing = { VerifyChip(email) })
}

// ---- Photo ----
@Composable
fun PhotoChangeScreen(progress: Float?, error: String?, onPick: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Picha ya wasifu", onBack = onBack) {
        Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8ECE9)), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RidaIcon(RidaIcons.Account, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
                Text("Picha (hakiki)", color = Color.Gray)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        if (error != null) { Text(error, color = Color(0xFFB91C1C), fontSize = 12.sp); Spacer(Modifier.height(8.dp)); RidaPrimaryButton("Jaribu tena", onRetry) }
        else { Spacer(Modifier.height(8.dp)); RidaPrimaryButton("Chagua / Piga picha", onPick) }
    }
}

// ---- Security ----
@Composable
fun SecurityScreen(
    pw: String,
    confirm: String,
    onPw: (String) -> Unit,
    onConfirm: (String) -> Unit,
    devices: List<String> = emptyList(),
    onSavePw: () -> Unit,
    onLogoutDevice: (String) -> Unit,
    onLogoutAll: () -> Unit,
    onBack: () -> Unit
) {
    val mismatch = confirm.isNotEmpty() && pw != confirm
    FlowScaffold(title = "Usalama", onBack = onBack) {
        RidaField(value = pw, onValueChange = onPw, label = "Nenosiri jipya", placeholder = "••••••••")
        val s = passwordStrength(pw)
        Text("Nguvu: ${"●".repeat(s)}${"○".repeat(3 - s)}", fontSize = 12.sp, color = Color.Gray)
        RidaField(value = confirm, onValueChange = onConfirm, label = "Thibitisha", placeholder = "••••••••", error = if (mismatch) "Havilingani" else null)
        Spacer(Modifier.height(8.dp)); RidaPrimaryButton("Badili nenosiri", onSavePw, enabled = !mismatch && pw.length >= 8)
        Spacer(Modifier.height(14.dp)); Text("Vifaa vilivyoingia", fontWeight = FontWeight.Bold)
        if (devices.isEmpty()) {
            Text("No active sessions found.", fontSize = 13.sp, color = Color.Gray)
        } else {
            devices.forEach { sess ->
                FlowRow(title = sess, subtitle = "Ilitumika hivi karibuni", onClick = { onLogoutDevice(sess) }); Spacer(Modifier.height(8.dp))
            }
        }
        TextButton(onClick = onLogoutAll, modifier = Modifier.heightIn(min = 48.dp)) { Text("Toka vifaa vyote") }
    }
}

// ---- Delete account ----
@Composable
fun DeleteAccountScreen(step: Int, onNext: () -> Unit, onConfirm: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Futa akaunti", onBack = onBack) {
        when (step) {
            0 -> {
                Text("Kitakachofutwa: wasifu, safari, malipo. Kipindi cha kusubiri: siku 30. Unaweza kughairi ndani ya siku 30.", fontSize = 13.sp)
                Spacer(Modifier.height(12.dp)); RidaPrimaryButton("Endelea", onNext)
            }
            else -> {
                Text("Thibitisha ni wewe kuendelea.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp)); RidaField(value = "", onValueChange = {}, label = "Nenosiri", placeholder = "••••••••")
                Spacer(Modifier.height(10.dp)); RidaPrimaryButton("Thibitisha kufuta", onConfirm)
            }
        }
    }
}

@Preview(showBackground = true) @Composable fun PrevEditName() { RIDEPASSENGER2Theme { EditNameScreen(NameEditState("Juma K.", dirty = true), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevEditNameError() { RIDEPASSENGER2Theme { EditNameScreen(NameEditState("J", error = "Jina fupi sana", dirty = true), {}, {}, {}) } }
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES) @Composable fun PrevEditNameDark() { RIDEPASSENGER2Theme(darkTheme = true) { EditNameScreen(NameEditState("Juma"), {}, {}, {}) } }
@Preview(showBackground = true, fontScale = 1.5f) @Composable fun PrevEditNameFont() { RIDEPASSENGER2Theme { EditNameScreen(NameEditState("Juma K."), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPhoneOtp() { RIDEPASSENGER2Theme { ChangePhoneScreen(PhoneChangeState(step = PhoneStep.OTP, otp = OtpState(code = "123", attemptsLeft = 3)), {}, {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPhoneLocked() { RIDEPASSENGER2Theme { ChangePhoneScreen(PhoneChangeState(step = PhoneStep.OTP, otp = OtpState(locked = true, attemptsLeft = 0, error = "Umefungiwa")), {}, {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPhoneSuccess() { RIDEPASSENGER2Theme { ChangePhoneScreen(PhoneChangeState(step = PhoneStep.SUCCESS), {}, {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevEmailSent() { RIDEPASSENGER2Theme { ChangeEmailScreen(EmailChangeState(step = EmailStep.SENT, resendInSec = 20), {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevEmailExpired() { RIDEPASSENGER2Theme { ChangeEmailScreen(EmailChangeState(step = EmailStep.EXPIRED), {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevVerifyRows() { RIDEPASSENGER2Theme { Column(Modifier.fillMaxSize().padding(16.dp)) { ProfileVerifyRows(VerifyState.VERIFIED, VerifyState.PENDING, {}, {}) } } }
@Preview(showBackground = true) @Composable fun PrevSecurity() { RIDEPASSENGER2Theme { SecurityScreen("Jina123!", "Jina123", {}, {}, emptyList(), {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevDelete() { RIDEPASSENGER2Theme { DeleteAccountScreen(0, {}, {}, {}) } }
