package com.example.ridepassenger2.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.ridepassenger2.data.local.AppPrefs
import com.example.ridepassenger2.data.local.AuthValidation
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.mock.RideRepository
import com.example.ridepassenger2.ui.components.HomeIndicator
import kotlinx.coroutines.launch

private val XBg = Color(0xFF0A0F14)
private val XCard = Color(0xFF181F26)

/** One-shot handoff: Saved Places "Go here" → Home search box. */
object PendingSearch { var query: String? = null }
private val XBorder = Color(0xFF26313B)
private val XPill = Color(0xFF1C252D)
private val XTitle = Color.White
private val XBody = Color(0xFFF3F4F6)
private val XMuted = Color(0xFF9CA3AF)
private val XMint = Color(0xFF43D2A1)
private val XMintBtn = Color(0xFF35C48E)
private val XOnMint = Color(0xFF0C1014)

@Composable
private fun AliveTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack).padding(6.dp),
            contentAlignment = Alignment.Center
        ) { Text(text = "←", fontSize = 20.sp, color = XTitle) }
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = XTitle)
    }
}

@Composable
private fun AliveScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(XBg).statusBarsPadding().padding(horizontal = 20.dp)
    ) {
        AliveTopBar(title, onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            HomeIndicator(color = Color.White.copy(alpha = 0.3f))
        }
    }
}

private fun paymentIcon(method: String): String = when (method) {
    "M-Pesa" -> "📱"
    "Tigo Pesa" -> "📲"
    "HaloPesa" -> "💳"
    "Card" -> "💼"
    else -> "💵"
}

// ------------------------------------------------------------------
// Payment methods — Bolt-style selector, persisted via AppPrefs
// ------------------------------------------------------------------
@Composable
fun PaymentMethodsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by AppPrefs.payment(context).collectAsState(initial = "Cash")
    AliveScaffold(title = "Payment Methods", onBack = onBack) {
        Text(text = "How do you want to pay? Cash is default in Dar.", fontSize = 12.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(14.dp))
        AppPrefs.PAYMENT_OPTIONS.forEach { method ->
            val sel = current == method
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(if (sel) XCard else XCard.copy(alpha = 0.6f))
                    .border(1.4.dp, if (sel) XMint else XBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        scope.launch {
                            AppPrefs.setPayment(context, method)
                            Toast.makeText(context, "$method set as default", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(XPill), contentAlignment = Alignment.Center) {
                    Text(text = paymentIcon(method), fontSize = 16.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = method, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = XTitle)
                    Text(
                        text = when (method) {
                            "Cash" -> "Pay the driver directly"
                            "Card" -> "Visa • Mastercard"
                            else -> "Mobile money • Tanzania"
                        },
                        fontSize = 11.5.sp, color = XMuted
                    )
                }
                if (sel) Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(XMint), contentAlignment = Alignment.Center) {
                    Text(text = "✓", color = XOnMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(text = "Mobile money is mock for now — driver confirms on arrival, like Farasi.", fontSize = 11.sp, color = XMuted)
    }
}

// ------------------------------------------------------------------
// Saved places — Home / Work backed by AppPrefs, tap to use
// ------------------------------------------------------------------
@Composable
fun SavedPlacesScreen(onBack: () -> Unit = {}, onUsePlace: (String) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val home by AppPrefs.homeQuery(context).collectAsState(initial = null)
    val work by AppPrefs.workQuery(context).collectAsState(initial = null)
    var editing by remember { mutableStateOf<String?>(null) } // "home" | "work"
    var draft by remember { mutableStateOf("") }

    fun openEdit(which: String, current: String?) {
        editing = which
        draft = current ?: ""
    }

    AliveScaffold(title = "Saved Places", onBack = onBack) {
        Text(text = "One tap to go home or to work — like Bolt.", fontSize = 12.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(14.dp))
        SavedPlaceRow(
            icon = "⌂", title = "Home", value = home,
            onUse = { home?.let(onUsePlace) },
            onEdit = { openEdit("home", home) },
            onClear = { scope.launch { AppPrefs.clearPlace(context, true) } }
        )
        Spacer(modifier = Modifier.height(10.dp))
        SavedPlaceRow(
            icon = "💼", title = "Work", value = work,
            onUse = { work?.let(onUsePlace) },
            onEdit = { openEdit("work", work) },
            onClear = { scope.launch { AppPrefs.clearPlace(context, false) } }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Tip: search a place on Home first, then save it here with the same name.", fontSize = 11.sp, color = XMuted)
    }

    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(text = if (editing == "home") "Set Home" else "Set Work") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(text = "e.g. Mbezi Beach, Dar es Salaam") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XMint)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = draft.trim()
                    if (v.length < 2) {
                        Toast.makeText(context, "Type a place name first", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    scope.launch {
                        if (editing == "home") AppPrefs.setHome(context, v) else AppPrefs.setWork(context, v)
                        editing = null
                    }
                }) { Text(text = "Save", color = XMint) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(text = "Cancel") } }
        )
    }
}

@Composable
private fun SavedPlaceRow(
    icon: String, title: String, value: String?,
    onUse: () -> Unit, onEdit: () -> Unit, onClear: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(XCard).border(1.dp, XBorder, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(XPill), contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Text(text = value ?: "Not set — tap Edit", fontSize = 12.sp, color = XMuted)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (value != null) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(XMintBtn).clickable(onClick = onUse).padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Go here", color = XOnMint, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, XBorder, RoundedCornerShape(10.dp)).clickable(onClick = onClear).padding(horizontal = 14.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Clear", color = XMuted, fontSize = 12.sp)
                }
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).border(1.dp, XBorder, RoundedCornerShape(10.dp)).clickable(onClick = onEdit).padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                Text(text = if (value == null) "Set" else "Edit", color = XTitle, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ------------------------------------------------------------------
// Schedule — Farasi-style advance booking, stored in RideRepository
// ------------------------------------------------------------------
@Composable
fun ScheduleRideScreen(onBack: () -> Unit = {}, prefillDestination: String = "") {
    val context = LocalContext.current
    var pickup by remember { mutableStateOf("Your location") }
    var destination by remember { mutableStateOf(prefillDestination) }
    var day by remember { mutableStateOf("Today") }
    var time by remember { mutableStateOf("10:00 AM") }
    var option by remember { mutableStateOf("Standard") }
    val scheduled by RideRepository.scheduled.collectAsState()

    val days = listOf("Today", "Tomorrow", "Saturday")
    val times = listOf("8:00 AM", "10:00 AM", "12:30 PM", "5:00 PM", "7:30 PM")

    AliveScaffold(title = "Schedule a Ride", onBack = onBack) {
        Text(text = "Book ahead — driver is assigned 15 min before pickup.", fontSize = 12.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(14.dp))
        ScheduleField("Pickup", pickup) { pickup = it }
        Spacer(modifier = Modifier.height(10.dp))
        ScheduleField("Destination", destination, "Where to? e.g. Mlimani City") { destination = it }
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Day", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { d ->
                Chip(text = d, selected = day == d) { day = d }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        @Composable fun TimeGrid() {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                times.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t -> Chip(text = t, selected = time == t) { time = t } }
                    }
                }
            }
        }
        TimeGrid()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Ride type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Standard", "Comfort", "XL").forEach { o -> Chip(text = o, selected = option == o) { option = o } }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val price = when (option) { "Comfort" -> "TZS 4,200"; "XL" -> "TZS 5,800"; else -> "TZS 3,200" }
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(XMintBtn)
                .clickable {
                    if (destination.isBlank()) {
                        Toast.makeText(context, "Enter a destination first", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    RideRepository.scheduleRide(pickup, destination, day, time, option, price)
                    Toast.makeText(context, "Scheduled $day $time • $price", Toast.LENGTH_LONG).show()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) { Text(text = "Confirm • $price", color = XOnMint, fontWeight = FontWeight.Bold, fontSize = 14.sp) }

        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "Upcoming (${scheduled.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        if (scheduled.isEmpty()) {
            Text(text = "No scheduled rides yet.", fontSize = 12.sp, color = XMuted)
        } else {
            scheduled.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(XCard).border(1.dp, XBorder, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${s.pickup} → ${s.destination}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = XTitle)
                        Text(text = "${s.dateLabel} • ${s.timeLabel} • ${s.option} • ${s.price}", fontSize = 11.sp, color = XMuted)
                    }
                    Text(text = "Cancel", color = XMint, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { RideRepository.cancelScheduled(s.id) }.padding(6.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, placeholder: String = "", onChange: (String) -> Unit) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = XMuted)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            placeholder = { Text(text = placeholder.ifBlank { label }, fontSize = 13.sp) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = XCard, unfocusedContainerColor = XCard,
                focusedTextColor = XTitle, unfocusedTextColor = XTitle,
                focusedBorderColor = XMint, unfocusedBorderColor = XBorder
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (selected) XMintBtn else XPill)
            .border(1.dp, if (selected) XMintBtn else XMint.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)
    ) { Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) XOnMint else XMint) }
}

// ------------------------------------------------------------------
// Package — send a parcel, Bolt-style
// ------------------------------------------------------------------
@Composable
fun PackageRideScreen(onBack: () -> Unit = {}, onSent: () -> Unit = {}) {
    val context = LocalContext.current
    var pickup by remember { mutableStateOf("Your location") }
    var dropoff by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("Small") }
    var recipient by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val price = when (size) { "Medium" -> "TZS 4,500"; "Large" -> "TZS 7,000"; else -> "TZS 3,000" }

    AliveScaffold(title = "Send a Package", onBack = onBack) {
        Text(text = "A rider picks up your parcel and delivers it. Pay on delivery.", fontSize = 12.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(14.dp))
        ScheduleField("Pickup", pickup) { pickup = it }
        Spacer(modifier = Modifier.height(10.dp))
        ScheduleField("Drop-off", dropoff, "Recipient address") { dropoff = it }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Parcel size", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Small" to "Envelope", "Medium" to "Box", "Large" to "Heavy").forEach { (s, d) ->
                val sel = size == s
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(XCard).border(if (sel) 1.6.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(12.dp))
                        .clickable { size = s }.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = s, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
                    Text(text = d, fontSize = 11.sp, color = XMuted)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Recipient name", recipient, "Who receives?") { recipient = it }
        Spacer(modifier = Modifier.height(10.dp))
        ScheduleField("Recipient phone", phone, "+255 7XX XXX XXX") { phone = it; phoneError = null }
        if (phoneError != null) Text(text = phoneError!!, fontSize = 12.sp, color = Color(0xFFF87171), modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(XMintBtn)
                .clickable {
                    phoneError = AuthValidation.identifierError(phone.ifBlank { " " })
                    if (dropoff.isBlank()) {
                        Toast.makeText(context, "Enter a drop-off first", Toast.LENGTH_SHORT).show(); return@clickable
                    }
                    if (phoneError != null) return@clickable
                    RideRepository.clearRoute() // no road route for parcels — engine skips GPS animation
                    RideRepository.requestRide(pickup.ifBlank { "Your location" }, dropoff, price, "Package • $size", 12)
                    Toast.makeText(context, "Courier requested • $price", Toast.LENGTH_LONG).show()
                    onSent()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) { Text(text = "Request Courier • $price", color = XOnMint, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
    }
}

// ------------------------------------------------------------------
// Safety — SOS, share trip, tips (no more "coming soon")
// ------------------------------------------------------------------
@Composable
fun SafetyScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val ride by RideRepository.activeRide.collectAsState()
    AliveScaffold(title = "Safety", onBack = onBack) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF3B0A0A))
                .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🆘", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Emergency SOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Calls 112 immediately", fontSize = 12.sp, color = Color(0xFFFCA5A5))
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFDC2626))
                        .clickable {
                            try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri())) }
                            catch (_: Exception) { Toast.makeText(context, "Dial 112", Toast.LENGTH_SHORT).show() }
                        }
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                ) { Text(text = "Call 112 now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SafetyRow("📤", "Share my trip", "Send route + driver to family") {
            val text = if (ride != null) "I'm in a Rida ${ride!!.option}: ${ride!!.pickup} → ${ride!!.destination}, driver ${ride!!.driver?.name ?: "assigning"} ${ride!!.driver?.plate ?: ""}, fare ${ride!!.price}."
            else "I'm using Rida passenger app in Dar es Salaam."
            try {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                }, "Share trip"))
            } catch (_: Exception) { Toast.makeText(context, "Copy: $text", Toast.LENGTH_LONG).show() }
        }
        SafetyRow("📞", "Call Rida support", "24/7 • 0800 750 000") {
            try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:0800750000".toUri())) }
            catch (_: Exception) { Toast.makeText(context, "Call 0800 750 000", Toast.LENGTH_SHORT).show() }
        }
        SafetyRow("👥", "Emergency contact", "Call your person") {
            try { context.startActivity(Intent(Intent.ACTION_DIAL)) }
            catch (_: Exception) { }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Safety tips", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(6.dp))
        listOf(
            "Check plate + driver photo before boarding.",
            "Share every night trip with family.",
            "Sit in the back seat and wear a seatbelt.",
            "Only pay the fare shown in the app."
        ).forEach {
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "✓", color = XMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = it, fontSize = 12.5.sp, color = XBody)
            }
        }
    }
}

@Composable
private fun SafetyRow(icon: String, title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(XCard).border(1.dp, XBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(XPill), contentAlignment = Alignment.Center) {
            Text(text = icon, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = XBody)
            Text(text = sub, fontSize = 11.5.sp, color = XMuted)
        }
        Text(text = "›", fontSize = 18.sp, color = XMuted)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ------------------------------------------------------------------
// Personal info — edits the DataStore session shown on Profile
// ------------------------------------------------------------------
@Composable
fun PersonalInfoScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by SessionManager.observe(context).collectAsState(initial = SessionManager.Session())
    var name by remember(session.name) { mutableStateOf(session.name) }
    var phone by remember(session.phone) { mutableStateOf(session.phone) }
    var email by remember(session.email) { mutableStateOf(session.email) }
    var err by remember { mutableStateOf<String?>(null) }

    AliveScaffold(title = "Personal Information", onBack = onBack) {
        ScheduleField("Full name", name) { name = it }
        Spacer(modifier = Modifier.height(10.dp))
        ScheduleField("Phone", phone, "+255 7XX XXX XXX") { phone = it }
        Spacer(modifier = Modifier.height(10.dp))
        ScheduleField("Email", email, "you@example.com") { email = it }
        if (err != null) Text(text = err!!, fontSize = 12.sp, color = Color(0xFFF87171), modifier = Modifier.padding(top = 6.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(XMintBtn)
                .clickable {
                    if (name.trim().length < 2) { err = "Enter your full name"; return@clickable }
                    scope.launch {
                        SessionManager.updateProfile(context, name.trim(), phone.trim(), email.trim())
                        Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) { Text(text = "Save changes", color = XOnMint, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
    }
}

// ------------------------------------------------------------------
// Ride preferences — seat + sharing, persisted
// ------------------------------------------------------------------
@Composable
fun RidePreferencesScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seat by AppPrefs.seatPref(context).collectAsState(initial = "Any seat")
    val share by AppPrefs.shareLocation(context).collectAsState(initial = true)

    AliveScaffold(title = "Ride Preferences", onBack = onBack) {
        Text(text = "Seat preference", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        listOf("Any seat", "Window seat", "Front seat", "Quiet ride").forEach { s ->
            val sel = seat == s
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(XCard).border(if (sel) 1.6.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(12.dp))
                    .clickable { scope.launch { AppPrefs.setSeatPref(context, s) } }.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = s, fontSize = 13.5.sp, color = XTitle, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (sel) Text(text = "✓", color = XMint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(XCard).border(1.dp, XBorder, RoundedCornerShape(12.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Share live location with driver", fontSize = 13.5.sp, color = XTitle, fontWeight = FontWeight.Medium)
                Text(text = "Faster pickup at crowded places", fontSize = 11.5.sp, color = XMuted)
            }
            Switch(checked = share, onCheckedChange = { scope.launch { AppPrefs.setShareLocation(context, it) } },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn))
        }
    }
}

// ------------------------------------------------------------------
// Help + Contact — real content, real intents
// ------------------------------------------------------------------
@Composable
fun HelpFaqScreen(onBack: () -> Unit = {}) {
    var open by remember { mutableStateOf(-1) }
    val faqs = listOf(
        "How do I request a ride?" to "Type your destination on Home, pick Standard/Comfort/XL or a Shared ride, then tap Request Ride. Your driver appears in seconds.",
        "How is the fare calculated?" to "Base TZS 2,500 + distance via OSRM road routing. Comfort/XL add legroom and space. Shared splits the cost.",
        "How do I pay?" to "Cash by default. Set M-Pesa, Tigo Pesa, HaloPesa or Card in Payment Methods — driver confirms mobile money on arrival.",
        "Can I schedule or send a package?" to "Yes. Use Schedule on Home to book ahead, and Package to send parcels across Dar.",
        "How do I stay safe?" to "Open the shield button on Home: SOS calls 112, share your trip, and always check plate + driver photo."
    )
    AliveScaffold(title = "Help & FAQ", onBack = onBack) {
        faqs.forEachIndexed { i, (q, a) ->
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(XCard).border(1.dp, XBorder, RoundedCornerShape(12.dp))
                    .clickable { open = if (open == i) -1 else i }.padding(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = q, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = XTitle, modifier = Modifier.weight(1f))
                    Text(text = if (open == i) "−" else "+", color = XMint, fontWeight = FontWeight.Bold)
                }
                if (open == i) Text(text = a, fontSize = 12.5.sp, color = XBody, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ContactUsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    AliveScaffold(title = "Contact Us", onBack = onBack) {
        Text(text = "We're here 24/7 in Dar es Salaam.", fontSize = 12.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(14.dp))
        SafetyRow("📞", "Call support", "0800 750 000 • free") {
            try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:0800750000".toUri())) } catch (_: Exception) { }
        }
        SafetyRow("✉", "Email us", "support@rida.co.tz") {
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:support@rida.co.tz".toUri()))
            } catch (_: Exception) { Toast.makeText(context, "support@rida.co.tz", Toast.LENGTH_LONG).show() }
        }
        SafetyRow("💬", "WhatsApp", "+255 700 000 000") {
            Toast.makeText(context, "WhatsApp: +255 700 000 000", Toast.LENGTH_LONG).show()
        }
    }
}

// ------------------------------------------------------------------
// Terms + Privacy dialogs for Create Account
// ------------------------------------------------------------------
@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Terms of Service", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "Rida is a ride-sharing marketplace. Fares are estimates from road routing and confirmed by the driver. " +
                    "Riders must wear seatbelts, respect drivers, and pay the shown fare. " +
                    "Scheduled rides are assigned ~15 min before pickup. " +
                    "Misuse, fraud or unsafe behaviour leads to suspension. Student MVP — $0 stack.",
                fontSize = 13.sp
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Got it", color = XMintBtn) } }
    )
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Privacy Policy", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "We store your profile on-device (DataStore), your location only while finding trips, " +
                    "and search queries with Nominatim/OSM. We never sell data. " +
                    "Share-location and notifications can be switched off in Ride Preferences and Settings.",
                fontSize = 13.sp
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Got it", color = XMintBtn) } }
    )
}
