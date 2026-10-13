package com.example.ridepassenger2.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.ridepassenger2.data.local.AppPrefs
import com.example.ridepassenger2.data.local.AuthValidation
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.mock.RideRepository
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import kotlinx.coroutines.launch

/** One-shot handoff: Saved Places "Go here" → Home search box. */
object PendingSearch { var query: String? = null }

private val XBg = Color(0xFFF5F8F6)
private val XCard = Color.White
private val XBorder = Color(0xFFEEF2F0)
private val XPill = Color(0xFFE8F7F0)
private val XTitle = Color(0xFF111827)
private val XBody = Color(0xFF374151)
private val XMuted = Color(0xFF667085)
private val XMint = Color(0xFF00A86B)
private val XMintBtn = Color(0xFF008F64)
private val XOnMint = Color.White

@Composable
private fun AliveTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = XTitle,
                modifier = Modifier.size(22.dp)
            )
        }
        RidaLogo(green = XMint)
    }
}

@Composable
private fun AliveScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(XBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        AliveTopBar(title, onBack)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = XTitle, letterSpacing = (-0.5).sp)
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
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
// Payment methods — Light Mode selector, persisted via AppPrefs
// ------------------------------------------------------------------
@Composable
fun PaymentMethodsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by AppPrefs.payment(context).collectAsState(initial = "Cash")
    AliveScaffold(title = "Payment Methods", onBack = onBack) {
        Text(text = "How do you want to pay? Cash is default in Dar.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(16.dp))
        AppPrefs.PAYMENT_OPTIONS.forEach { method ->
            val sel = current == method
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (sel) 2.dp else 0.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(XCard)
                    .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(18.dp))
                    .clickable {
                        scope.launch {
                            AppPrefs.setPayment(context, method)
                            Toast.makeText(context, "$method set as default", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(XPill),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = paymentIcon(method), fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = method, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when (method) {
                            "Cash" -> "Pay the driver directly"
                            "Card" -> "Visa • Mastercard"
                            else -> "Mobile money • Tanzania"
                        },
                        fontSize = 12.5.sp, color = XMuted
                    )
                }
                if (sel) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(XMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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
        Text(text = "Book ahead — driver is assigned 15 min before pickup.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(16.dp))
        ScheduleField("Pickup", pickup) { pickup = it }
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Destination", destination, "Where to? e.g. Mlimani City") { destination = it }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { d ->
                Chip(text = d, selected = day == d) { day = d }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Time", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            times.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { t -> Chip(text = t, selected = time == t) { time = t } }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Ride type", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Standard", "Comfort", "XL").forEach { o -> Chip(text = o, selected = option == o) { option = o } }
        }
        Spacer(modifier = Modifier.height(20.dp))
        val price = when (option) { "Comfort" -> "TZS 4,200"; "XL" -> "TZS 5,800"; else -> "TZS 3,200" }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(XMintBtn)
                .clickable {
                    if (destination.isBlank()) {
                        Toast.makeText(context, "Enter a destination first", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    RideRepository.scheduleRide(pickup, destination, day, time, option, price)
                    Toast.makeText(context, "Scheduled $day $time • $price", Toast.LENGTH_LONG).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Confirm • $price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Upcoming (${scheduled.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(10.dp))
        if (scheduled.isEmpty()) {
            Text(text = "No scheduled rides yet.", fontSize = 13.sp, color = XMuted)
        } else {
            scheduled.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(XCard)
                        .border(1.dp, XBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${s.pickup} → ${s.destination}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = XTitle)
                        Spacer(Modifier.height(2.dp))
                        Text(text = "${s.dateLabel} • ${s.timeLabel} • ${s.option} • ${s.price}", fontSize = 12.sp, color = XMuted)
                    }
                    Text(
                        text = "Cancel",
                        color = Color(0xFFD92D20),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { RideRepository.cancelScheduled(s.id) }
                            .padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, placeholder: String = "", onChange: (String) -> Unit) {
    Column {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = XTitle)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            placeholder = { Text(text = placeholder.ifBlank { label }, fontSize = 13.sp, color = XMuted) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = XCard, unfocusedContainerColor = XCard,
                focusedTextColor = XTitle, unfocusedTextColor = XTitle,
                focusedBorderColor = XMint, unfocusedBorderColor = XBorder
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) XMintBtn else XPill)
            .border(1.dp, if (selected) XMintBtn else XMint.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else XMint)
    }
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
        Text(text = "A rider picks up your parcel and delivers it. Pay on delivery.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(16.dp))
        ScheduleField("Pickup", pickup) { pickup = it }
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Drop-off", dropoff, "Recipient address") { dropoff = it }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Parcel size", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Small" to "Envelope", "Medium" to "Box", "Large" to "Heavy").forEach { (s, d) ->
                val sel = size == s
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(XCard)
                        .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(16.dp))
                        .clickable { size = s }
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = s, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
                    Spacer(Modifier.height(2.dp))
                    Text(text = d, fontSize = 12.sp, color = XMuted)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ScheduleField("Recipient name", recipient, "Who receives?") { recipient = it }
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Recipient phone", phone, "+255 7XX XXX XXX") { phone = it; phoneError = null }
        if (phoneError != null) Text(text = phoneError!!, fontSize = 12.sp, color = Color(0xFFD92D20), modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(XMintBtn)
                .clickable {
                    phoneError = AuthValidation.identifierError(phone.ifBlank { " " })
                    if (dropoff.isBlank()) {
                        Toast.makeText(context, "Enter a drop-off first", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    if (phoneError != null) return@clickable
                    RideRepository.clearRoute()
                    RideRepository.requestRide(pickup.ifBlank { "Your location" }, dropoff, price, "Package • $size", 12)
                    Toast.makeText(context, "Courier requested • $price", Toast.LENGTH_LONG).show()
                    onSent()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Request Courier • $price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ------------------------------------------------------------------
// Safety — SOS, share trip, tips
// ------------------------------------------------------------------
@Composable
fun SafetyScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val ride by RideRepository.activeRide.collectAsState()
    AliveScaffold(title = "Safety", onBack = onBack) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFEF2F2))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🆘", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Emergency SOS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                Text(text = "Calls 112 immediately", fontSize = 13.sp, color = Color(0xFFB91C1C))
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDC2626))
                        .clickable {
                            try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri())) }
                            catch (_: Exception) { Toast.makeText(context, "Dial 112", Toast.LENGTH_SHORT).show() }
                        }
                        .padding(horizontal = 32.dp, vertical = 13.dp)
                ) {
                    Text(text = "Call 112 now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Safety tips", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(8.dp))
        listOf(
            "Check plate + driver photo before boarding.",
            "Share every night trip with family.",
            "Sit in the back seat and wear a seatbelt.",
            "Only pay the fare shown in the app."
        ).forEach { tip ->
            Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "✓", color = XMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = tip, fontSize = 13.sp, color = XBody)
            }
        }
    }
}

@Composable
private fun SafetyRow(icon: String, title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(XCard)
            .border(1.dp, XBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(XPill),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = XTitle)
            Spacer(Modifier.height(2.dp))
            Text(text = sub, fontSize = 12.5.sp, color = XMuted)
        }
        Text(text = "›", fontSize = 20.sp, color = XMuted)
    }
    Spacer(modifier = Modifier.height(10.dp))
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
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Phone", phone, "+255 7XX XXX XXX") { phone = it }
        Spacer(modifier = Modifier.height(12.dp))
        ScheduleField("Email", email, "you@example.com") { email = it }
        if (err != null) Text(text = err!!, fontSize = 12.sp, color = Color(0xFFD92D20), modifier = Modifier.padding(top = 6.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(XMintBtn)
                .clickable {
                    if (name.trim().length < 2) { err = "Enter your full name"; return@clickable }
                    scope.launch {
                        SessionManager.updateProfile(context, name.trim(), phone.trim(), email.trim())
                        Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Save changes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
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
        Text(text = "Seat preference", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
        Spacer(modifier = Modifier.height(10.dp))
        listOf("Any seat", "Window seat", "Front seat", "Quiet ride").forEach { s ->
            val sel = seat == s
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(XCard)
                    .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(16.dp))
                    .clickable { scope.launch { AppPrefs.setSeatPref(context, s) } }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = s, fontSize = 14.5.sp, color = XTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (sel) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(XMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Share live location with driver", fontSize = 14.5.sp, color = XTitle, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(text = "Faster pickup at crowded places", fontSize = 12.5.sp, color = XMuted)
            }
            Switch(
                checked = share,
                onCheckedChange = { scope.launch { AppPrefs.setShareLocation(context, it) } },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn)
            )
        }
    }
}

// ------------------------------------------------------------------
// Help + Contact
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(XCard)
                    .border(1.dp, XBorder, RoundedCornerShape(16.dp))
                    .clickable { open = if (open == i) -1 else i }
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = q, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = XTitle, modifier = Modifier.weight(1f))
                    Text(text = if (open == i) "−" else "+", color = XMint, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                if (open == i) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = a, fontSize = 13.sp, color = XBody, lineHeight = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ContactUsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    AliveScaffold(title = "Contact Us", onBack = onBack) {
        Text(text = "We're here 24/7 in Dar es Salaam.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(16.dp))
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
                fontSize = 13.5.sp
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Got it", color = XMintBtn, fontWeight = FontWeight.Bold) } }
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
                fontSize = 13.5.sp
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Got it", color = XMintBtn, fontWeight = FontWeight.Bold) } }
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PaymentMethodsScreenPreview() {
    RIDEPASSENGER2Theme {
        PaymentMethodsScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ScheduleRideScreenPreview() {
    RIDEPASSENGER2Theme {
        ScheduleRideScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PackageRideScreenPreview() {
    RIDEPASSENGER2Theme {
        PackageRideScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SafetyScreenPreview() {
    RIDEPASSENGER2Theme {
        SafetyScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PersonalInfoScreenPreview() {
    RIDEPASSENGER2Theme {
        PersonalInfoScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RidePreferencesScreenPreview() {
    RIDEPASSENGER2Theme {
        RidePreferencesScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HelpFaqScreenPreview() {
    RIDEPASSENGER2Theme {
        HelpFaqScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ContactUsScreenPreview() {
    RIDEPASSENGER2Theme {
        ContactUsScreen()
    }
}
