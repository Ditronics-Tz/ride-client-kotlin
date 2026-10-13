package com.example.ridepassenger2.ui.support

import android.content.res.Configuration
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ridepassenger2.ui.common.SupportFormRules
import com.example.ridepassenger2.ui.common.VerifyBanner
import com.example.ridepassenger2.ui.preview.FakeSupport
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

@Composable
fun SupportHomeScreen(query: String, onQuery: (String) -> Unit, tickets: List<Ticket>, onContact: () -> Unit, onTopic: (Topic) -> Unit, onTicket: (String) -> Unit, onCall112: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Msaada", onBack = onBack) {
        RidaField(value = query, onValueChange = onQuery, label = "Tafuta msaada", placeholder = "mf. malipo, OTP")
        Spacer(Modifier.height(10.dp))
        RidaPrimaryButton("Wasiliana na msaada", onContact)
        Spacer(Modifier.height(12.dp))
        VerifyBanner("Dharura? Piga 112 sasa.", ChipTone.BAD)
        // Note: app only dials 112; no monitoring implied.
        Spacer(Modifier.height(6.dp))
        RidaPrimaryButton("Piga 112", onCall112)
        Spacer(Modifier.height(12.dp))
        Text("Mada", fontWeight = FontWeight.Bold)
        Topic.values().forEach { t ->
            Spacer(Modifier.height(8.dp))
            FlowRow(title = t.name.lowercase().replaceFirstChar { it.uppercase() }, onClick = { onTopic(t) })
        }
        Spacer(Modifier.height(12.dp)); Text("Maombi ya hivi karibuni", fontWeight = FontWeight.Bold)
        if (tickets.isEmpty()) Text("Hakuna maombi.", fontSize = 13.sp, color = Color.Gray)
        tickets.take(3).forEach { tk ->
            Spacer(Modifier.height(8.dp))
            FlowRow(title = "#${tk.id} ${tk.subject}", subtitle = ticketChip(tk.status), onClick = { onTicket(tk.id) })
        }
    }
}

@Composable
fun TopicScreen(topic: Topic, onSub: (String) -> Unit, onBack: () -> Unit, prefilledTrip: String? = null) {
    FlowScaffold(title = topic.name.lowercase(), onBack = onBack) {
        if (topic == Topic.SAFETY) {
            VerifyBanner("Uko hatarini sasa? Piga 112.", ChipTone.BAD)
            Spacer(Modifier.height(8.dp))
            Text("Timu yetu itapitia ripoti yako. (Muda wa kushughulikia: ~masaa ${SupportConfig.REVIEW_HOURS}.)", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
        }
        if (prefilledTrip != null) { Text("Safari: $prefilledTrip", fontSize = 12.sp); Spacer(Modifier.height(8.dp)) }
        val subs = when (topic) {
            Topic.ACCOUNT -> ACCOUNT_SUBS; Topic.PAYMENTS -> PAY_PROBLEMS
            Topic.SAFETY -> SAFETY_CATS; Topic.COMPLAINTS -> listOf("dereva", "nauli", "app", "kingine")
            Topic.OTHER -> listOf("andika ujumbe")
        }
        subs.forEach { s -> FlowRow(title = s, onClick = { onSub(s) }); Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun SelfHelpScreen(answer: String, onSolved: () -> Unit, onNeedHelp: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Msaada wa haraka", onBack = onBack) {
        Text(answer, fontSize = 14.sp); Spacer(Modifier.height(14.dp))
        RidaPrimaryButton("Imenisaidia", onSolved); Spacer(Modifier.height(8.dp))
        FlowRow(title = "Bado nahitaji msaada", onClick = onNeedHelp)
    }
}

@Composable
fun RequestFormScreen(state: RequestFormState, onSubject: (String) -> Unit, onDesc: (String) -> Unit, onAttach: () -> Unit, onRemove: (String) -> Unit, onContact: (String) -> Unit, onSubmit: () -> Unit, onBack: () -> Unit) {
    val errs = formErrors(state)
    FlowScaffold(title = "Tuma ombi", onBack = onBack) {
        RidaField(value = state.subject, onValueChange = onSubject, label = "Kichwa", placeholder = "mf. Nimetozwa mara mbili", error = errs["subject"])
        Spacer(Modifier.height(8.dp))
        RidaField(value = state.description, onValueChange = onDesc, label = "Maelezo (${state.description.length}/${SupportFormRules.MAX_DESC})", placeholder = "Eleza kilichotokea...")
        if (errs["description"] != null) Text(errs["description"]!!, color = Color(0xFFB91C1C), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text("Viambatisho (${state.attachments.size}/3)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        state.attachments.forEach { a -> FlowRow(title = a, onClick = { onRemove(a) }); Spacer(Modifier.height(6.dp)) }
        if (errs["attachments"] != null) Text(errs["attachments"]!!, color = Color(0xFFB91C1C), fontSize = 12.sp)
        RidaPrimaryButton("Weka picha (hadi 5MB)", onAttach)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("app", "simu").forEach { c ->
                FilterChip(selected = state.contact == c, onClick = { onContact(c) }, label = { Text(if (c == "app") "Ndani ya app" else "Simu") }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (state.error != null) { Text(state.error!!, color = Color(0xFFB91C1C), fontSize = 12.sp); Spacer(Modifier.height(6.dp)) }
        if (state.draftSaved) Text("Rasimu imehifadhiwa.", fontSize = 12.sp, color = Color.Gray)
        RidaPrimaryButton("Tuma", onSubmit, loading = state.submitting, enabled = formValid(state))
    }
}

@Composable
fun ConfirmScreen(ticketId: String, summary: String, onView: () -> Unit, onHome: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(title = "Imetumwa", onBack = onBack) {
        StatusChip("Tiketi #$ticketId", ChipTone.OK); Spacer(Modifier.height(8.dp))
        Text(summary, fontSize = 14.sp)
        Text("Tutakujulisha ndani ya app. Tutapitia na kukujibu.", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp)); RidaPrimaryButton("Angalia ombi", onView); Spacer(Modifier.height(8.dp))
        FlowRow(title = "Rudi mwanzo", onClick = onHome)
    }
}

@Composable
fun MyRequestsScreen(tickets: List<Ticket>, filter: TicketStatus?, onFilter: (TicketStatus?) -> Unit, onOpen: (String) -> Unit, onBack: () -> Unit, loading: Boolean = false) {
    FlowScaffold(title = "Maombi yangu", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { onFilter(null) }, label = { Text("zote") }, modifier = Modifier.heightIn(min = 48.dp))
            TicketStatus.values().forEach { s ->
                FilterChip(selected = filter == s, onClick = { onFilter(s) }, label = { Text(ticketChip(s), fontSize = 11.sp) }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (loading) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@FlowScaffold }
        val shown = if (filter == null) tickets else tickets.filter { it.status == filter }
        if (shown.isEmpty()) { Text("Hakuna maombi.", fontWeight = FontWeight.Bold); Text("Maombi yako yataonekana hapa.", fontSize = 13.sp, color = Color.Gray) }
        else shown.forEach { tk ->
            FlowRow(title = "#${tk.id} ${tk.subject}", subtitle = "${ticketChip(tk.status)} • ${tk.updated}", onClick = { onOpen(tk.id) }); Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun RequestDetailScreen(ticket: Ticket?, msgs: List<TicketMsg>, reply: String, onReply: (String) -> Unit, onSend: () -> Unit, onResolve: () -> Unit, rating: Int, onRate: (Int) -> Unit, onBack: () -> Unit, loading: Boolean = false, error: String? = null) {
    FlowScaffold(title = "Ombi #${ticket?.id ?: "-"}", onBack = onBack) {
        if (loading) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@FlowScaffold }
        if (error != null) { Text(error, color = Color(0xFFB91C1C)); return@FlowScaffold }
        if (ticket == null) { Text("Haikupatikana."); return@FlowScaffold }
        StatusChip(ticketChip(ticket.status), if (ticket.status == TicketStatus.RESOLVED) ChipTone.OK else ChipTone.INFO)
        Spacer(Modifier.height(8.dp)); Text(ticket.subject, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        msgs.forEach { m ->
            val bg = if (m.system) Color(0xFFF1F5F9) else if (m.fromUser) Color(0xFFE6F6EE) else Color.White
            Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(m.text, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(10.dp))
        if (ticket.status == TicketStatus.RESOLVED || ticket.status == TicketStatus.CLOSED) {
            Text("Kadiria msaada (1-5)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { i ->
                    FilterChip(selected = rating == i, onClick = { onRate(i) }, label = { Text("$i") }, modifier = Modifier.heightIn(min = 48.dp))
                }
            }
        } else {
            RidaField(value = reply, onValueChange = onReply, label = "Jibu", placeholder = "Andika jibu...")
            Spacer(Modifier.height(8.dp)); RidaPrimaryButton("Tuma jibu", onSend)
            Spacer(Modifier.height(8.dp)); FlowRow(title = "Weka alama imetatuliwa", onClick = onResolve)
        }
    }
}

@Preview(showBackground = true) @Composable fun PrevSupportHome() { RIDEPASSENGER2Theme { SupportHomeScreen("", {}, FakeSupport.tickets(), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevSupportHomeEmpty() { RIDEPASSENGER2Theme { SupportHomeScreen("", {}, emptyList(), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES) @Composable fun PrevSupportHomeDark() { RIDEPASSENGER2Theme(darkTheme = true) { SupportHomeScreen("", {}, FakeSupport.tickets(), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true, fontScale = 1.5f) @Composable fun PrevSupportHomeFont() { RIDEPASSENGER2Theme { SupportHomeScreen("", {}, FakeSupport.tickets(), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevTopic() { RIDEPASSENGER2Theme { TopicScreen(Topic.ACCOUNT, {}, {}, null) } }
@Preview(showBackground = true) @Composable fun PrevTopicSafety() { RIDEPASSENGER2Theme { TopicScreen(Topic.SAFETY, {}, {}, "Kariakoo → Mlimani") } }
@Preview(showBackground = true) @Composable fun PrevForm() { RIDEPASSENGER2Theme { RequestFormScreen(RequestFormState(subject = "Nimetozwa", description = "Nimetozwa mara mbili leo asubuhi kwenye safari ya Kariakoo."), {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevFormError() { RIDEPASSENGER2Theme { RequestFormScreen(RequestFormState(subject = "x", description = "fupi", error = "Imeshindikana kutuma. Jaribu tena."), {}, {}, {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevConfirm() { RIDEPASSENGER2Theme { ConfirmScreen("R-1042", "Ombi kuhusu malipo.", {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevMyReq() { RIDEPASSENGER2Theme { MyRequestsScreen(FakeSupport.tickets(), null, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevMyReqEmpty() { RIDEPASSENGER2Theme { MyRequestsScreen(emptyList(), null, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevReqDetail() { RIDEPASSENGER2Theme { RequestDetailScreen(FakeSupport.tickets().first(), FakeSupport.thread(), "", {}, {}, {}, 0, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevReqResolved() { RIDEPASSENGER2Theme { RequestDetailScreen(FakeSupport.tickets().first().copy(status = TicketStatus.RESOLVED), FakeSupport.thread(), "", {}, {}, {}, 5, {}, {}) } }
@Preview(showBackground = true) @Composable fun PrevReqLoading() { RIDEPASSENGER2Theme { RequestDetailScreen(null, emptyList(), "", {}, {}, {}, 0, {}, {}, loading = true) } }
@Preview(showBackground = true) @Composable fun PrevReqError() { RIDEPASSENGER2Theme { RequestDetailScreen(null, emptyList(), "", {}, {}, {}, 0, {}, {}, error = "Imeshindikana kupakia. Jaribu tena.") } }
