package com.example.ridepassenger2.ui.payment

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.common.ChipTone
import com.example.ridepassenger2.ui.common.FlowRow
import com.example.ridepassenger2.ui.common.FlowScaffold
import com.example.ridepassenger2.ui.common.RidaField
import com.example.ridepassenger2.ui.common.RidaPrimaryButton
import com.example.ridepassenger2.ui.common.StatusChip
import com.example.ridepassenger2.ui.common.TzPhone
import com.example.ridepassenger2.ui.preview.FakePayments
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

// ---- 1. Methods list (overview PaymentMethodsScreen already existed in AliveScreens.kt) ----
@Composable
fun PayMethodsFlowScreen(state: MethodsUiState, onBack: () -> Unit, onAdd: () -> Unit, onSetDefault: (String) -> Unit, onRemove: (String) -> Unit) {
    PayMethodsContent(state, onBack, onAdd, onSetDefault, onRemove)
}

@Composable
private fun PayMethodsContent(state: MethodsUiState, onBack: () -> Unit, onAdd: () -> Unit, onSetDefault: (String) -> Unit, onRemove: (String) -> Unit) {
    var confirmRemove by remember { mutableStateOf<String?>(null) }
    FlowScaffold(title = "Njia za malipo", onBack = onBack) {
        when (state) {
            MethodsUiState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.semantics { contentDescription = "Inapakia" })
            }
            MethodsUiState.Empty -> {
                Text(text = "Huna njia ya malipo", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(text = "Weka namba ya simu kulipa haraka.", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp)); RidaPrimaryButton(text = "Weka", onClick = onAdd)
            }
            is MethodsUiState.Error -> {
                Text(text = "Imeshindikana kupakia. Jaribu tena.", color = Color(0xFFB91C1C))
                Spacer(Modifier.height(12.dp)); RidaPrimaryButton(text = "Jaribu tena", onClick = onAdd)
            }
            is MethodsUiState.Content -> {
                state.methods.forEach { m ->
                    FlowRow(
                        title = "${m.label} ${if (m.isDefault) "• Chaguo" else ""}",
                        subtitle = m.masked, onClick = { onSetDefault(m.id) },
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (m.isDefault) StatusChip("Chaguo", ChipTone.OK)
                                TextButton(onClick = { confirmRemove = m.id }, modifier = Modifier.heightIn(min = 48.dp)) {
                                    Text("Ondoa")
                                }
                            }
                        }
                    ); Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(8.dp)); RidaPrimaryButton(text = "Weka njia", onClick = onAdd)
            }
        }
    }
    if (confirmRemove != null) AlertDialog(
        onDismissRequest = { confirmRemove = null },
        title = { Text("Ondoa njia?") },
        text = { Text("Hutaweza kulipa nayo tena.") },
        confirmButton = { TextButton(onClick = { onRemove(confirmRemove!!); confirmRemove = null }) { Text("Ondoa") } },
        dismissButton = { TextButton(onClick = { confirmRemove = null }) { Text("Ghairi") } }
    )
}

// ---- 2/3. Add + mobile-money setup ----
@Composable
fun AddMethodScreen(state: MmSetupState, onEvent: (MmEvent) -> Unit, onBack: () -> Unit, onDone: () -> Unit) {
    AddMethodContent(state, onEvent, onBack, onDone)
}

@Composable
private fun AddMethodContent(state: MmSetupState, onEvent: (MmEvent) -> Unit, onBack: () -> Unit, onDone: () -> Unit) {
    FlowScaffold(title = "Weka malipo", onBack = onBack) {
        when (state.phase) {
            MmVerifyPhase.ENTER, MmVerifyPhase.CONFIRM -> {
                Text(text = "Chagua mtandao", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MmProviders.ALL.forEach { p ->
                        FilterChip(
                            selected = state.provider == p, onClick = { onEvent(MmEvent.PickProvider(p)) },
                            label = { Text(p, fontSize = 12.sp) }, modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                RidaField(value = state.phoneRaw, onValueChange = { onEvent(MmEvent.EnterPhone(it)) },
                    label = "Namba ya simu", placeholder = "0712 345 678", error = state.phoneError)
                Spacer(Modifier.height(8.dp))
                if (state.phase == MmVerifyPhase.CONFIRM) {
                    val n = TzPhone.normalize(state.phoneRaw) ?: state.phoneRaw
                    Text(text = "Tutathibitisha ${TzPhone.mask(n)}", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                RidaPrimaryButton(
                    text = if (state.phase == MmVerifyPhase.ENTER) "Endelea" else "Thibitisha namba",
                    onClick = { if (state.phase == MmVerifyPhase.ENTER) onEvent(MmEvent.ToConfirm) else onEvent(MmEvent.StartVerify) }
                )
            }
            MmVerifyPhase.WAITING -> {
                Text(text = "Tumetuma ombi kwenye simu yako. Ikubali kuthibitisha.", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                Text(text = if (state.resendInSec > 0) "Tuma tena baada ya ${state.resendInSec}s" else "Unga ombi jipya",
                    fontSize = 12.sp, color = Color.Gray)
            }
            MmVerifyPhase.APPROVED, MmVerifyPhase.SUCCESS -> {
                StatusChip("Imekubaliwa", ChipTone.OK); Spacer(Modifier.height(8.dp))
                Text(text = "Njia imewekwa. Uko tayari kulipa.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp)); RidaPrimaryButton(text = "Maliza", onClick = onDone)
            }
            MmVerifyPhase.DECLINED, MmVerifyPhase.TIMED_OUT, MmVerifyPhase.FAILED -> {
                StatusChip(state.failReason ?: "Imeshindikana", ChipTone.BAD); Spacer(Modifier.height(8.dp))
                Text(text = "Hatukuweza kukamilisha. Jaribu tena.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                RidaPrimaryButton(text = "Jaribu tena", onClick = { onEvent(MmEvent.Retry) })
            }
        }
    }
}

// ---- 4. History ----
@Composable
fun PayHistoryScreen(records: List<PaymentRecord>, filter: PayFilter, onFilter: (PayFilter) -> Unit, onOpen: (String) -> Unit, onBack: () -> Unit, loadingMore: Boolean = false, refreshing: Boolean = false) {
    FlowScaffold(title = "Malipo yangu", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PayFilter.values().forEach { f ->
                FilterChip(selected = filter == f, onClick = { onFilter(f) },
                    label = { Text(f.name.lowercase(), fontSize = 12.sp) }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (refreshing) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        val shown = filterHistory(records, filter)
        if (shown.isEmpty()) {
            Text(text = "Hakuna malipo hapa", fontWeight = FontWeight.Bold)
            Text(text = "Malipo yako yataonekana hapa.", fontSize = 13.sp, color = Color.Gray)
        } else {
            var lastMonth = ""
            shown.forEach { r ->
                if (r.month != lastMonth) { lastMonth = r.month; Text(text = r.month, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp)) }
                FlowRow(title = "${r.route} • ${r.amountLabel}", subtitle = "${r.time} • ${r.ref}",
                    onClick = { onOpen(r.id) },
                    trailing = {
                        StatusChip(
                            when (r.status) { PayStatus.PAID -> "paid"; PayStatus.PENDING -> "pending"; PayStatus.FAILED -> "failed"; PayStatus.REFUNDED -> "refunded" },
                            when (r.status) { PayStatus.PAID -> ChipTone.OK; PayStatus.PENDING -> ChipTone.WARN; PayStatus.FAILED -> ChipTone.BAD; PayStatus.REFUNDED -> ChipTone.INFO }
                        )
                    })
                Spacer(Modifier.height(8.dp))
            }
            if (loadingMore) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

// ---- 5. Detail / receipt ----
@Composable
fun PayDetailScreen(rec: PaymentRecord?, onBack: () -> Unit, onReport: (String) -> Unit) {
    FlowScaffold(title = "Risiti", onBack = onBack) {
        if (rec == null) { Text("Haikupatikana. Jaribu tena.", color = Color(0xFFB91C1C)); return@FlowScaffold }
        Text(text = rec.route, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = "${rec.time} • ${rec.ref}", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(10.dp))
        DetailLine("Kiasi", rec.amountLabel)
        DetailLine("Njia", rec.methodMasked)
        if (rec.discountTzs > 0) DetailLine("Punguzo", "- ${com.example.ridepassenger2.ui.common.TzMoney.format(rec.discountTzs)}")
        Spacer(Modifier.height(10.dp))
        Text(text = "Hatua", fontWeight = FontWeight.SemiBold)
        listOf("Imeombwa", "Imethibitishwa", "Imelipwa").forEach { s -> Text(text = "• $s", fontSize = 13.sp) }
        Spacer(Modifier.height(14.dp))
        RidaPrimaryButton(text = "Ripoti tatizo la malipo", onClick = { onReport(rec.id) })
    }
}

@Composable private fun DetailLine(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = k, fontSize = 13.sp, color = Color.Gray); Text(text = v, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---- 6. Pay for a trip ----
@Composable
fun PayTripScreen(state: PayTripState, amountLabel: String, onPick: (String) -> Unit, onPay: () -> Unit, onRetry: () -> Unit, onCash: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Lipa safari $amountLabel", onBack = onBack) {
        when (state.phase) {
            PayTripPhase.PICK -> {
                FlowRow(title = "Cash", subtitle = "Mlipie dereva", onClick = { onPick("cash") }); Spacer(Modifier.height(8.dp))
                FlowRow(title = "M-Pesa • +255 7** *** 123", subtitle = "Thibitisha kwa simu", onClick = { onPick("m-pesa") })
                Spacer(Modifier.height(12.dp)); RidaPrimaryButton(text = "Lipa sasa", onClick = onPay)
            }
            PayTripPhase.WAITING -> {
                Text(text = "Subiri idhini ya simu...", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp)); Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            PayTripPhase.FAILED -> {
                StatusChip(state.error ?: "Imeshindikana", ChipTone.BAD); Spacer(Modifier.height(8.dp))
                RidaPrimaryButton(text = "Jaribu tena", onClick = onRetry); Spacer(Modifier.height(8.dp))
                FlowRow(title = "Badili kwa Cash", subtitle = "Lipa dereva", onClick = onCash)
            }
            PayTripPhase.SUCCESS -> {
                StatusChip("Imelipwa", ChipTone.OK); Spacer(Modifier.height(8.dp))
                Text(text = "Asante! Safari imelipwa.", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---- Previews: normal/loading/empty/error/success + light/dark + fontscale ----
@Preview(showBackground = true) @Composable fun PrevPayMethods() { RIDEPASSENGER2Theme { PayMethodsFlowScreen(MethodsUiState.Content(FakePayments.methods()), {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPayMethodsEmpty() { RIDEPASSENGER2Theme { PayMethodsFlowScreen(MethodsUiState.Empty, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPayMethodsLoading() { RIDEPASSENGER2Theme { PayMethodsFlowScreen(MethodsUiState.Loading, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPayMethodsError() { RIDEPASSENGER2Theme { PayMethodsFlowScreen(MethodsUiState.Error("network"), {}, {}, {}, {}) } }
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES) @Composable fun PrevPayMethodsDark() { RIDEPASSENGER2Theme(darkTheme = true) { PayMethodsFlowScreen(MethodsUiState.Content(FakePayments.methods()), {}, {}, {}, {}) } }
@Preview(showBackground = true, fontScale = 1.5f) @Composable fun PrevPayMethodsFont() { RIDEPASSENGER2Theme { PayMethodsFlowScreen(MethodsUiState.Content(FakePayments.methods()), {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevAddEnter() { RIDEPASSENGER2Theme { AddMethodScreen(MmSetupState(), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevAddWaiting() { RIDEPASSENGER2Theme { AddMethodScreen(MmSetupState(phase = MmVerifyPhase.WAITING, resendInSec = 12), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevAddSuccess() { RIDEPASSENGER2Theme { AddMethodScreen(MmSetupState(phase = MmVerifyPhase.SUCCESS), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevAddFailed() { RIDEPASSENGER2Theme { AddMethodScreen(MmSetupState(phase = MmVerifyPhase.DECLINED, failReason = "Umekataa ombi"), {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevHistory() { RIDEPASSENGER2Theme { PayHistoryScreen(FakePayments.history(), PayFilter.ALL, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevHistoryEmpty() { RIDEPASSENGER2Theme { PayHistoryScreen(emptyList(), PayFilter.PAID, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevDetail() { RIDEPASSENGER2Theme { Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { PayDetailScreen(FakePayments.history().first(), {}, {}) } } }
@Preview(showBackground = true) @Composable fun PrevPayTrip() { RIDEPASSENGER2Theme { PayTripScreen(PayTripState(), "TSh 5,000", {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevPayTripFailed() { RIDEPASSENGER2Theme { PayTripScreen(PayTripState(phase = PayTripPhase.FAILED, error = "Muamala umeshindikana"), "TSh 5,000", {}, {}, {}, {}, {}) } }
