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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun PaymentMethodsScreen(
    onBack: () -> Unit = {},
    onAddMethod: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by AppPrefs.payment(context).collectAsState(initial = "Cash")

    AliveScaffold(title = "Payment methods", onBack = onBack) {
        Text(text = "Choose how to pay for your rides.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(20.dp))

        // DEFAULT PAYMENT
        Text(
            text = "DEFAULT PAYMENT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Cash Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .clickable {
                    scope.launch { AppPrefs.setPayment(context, "Cash") }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💵", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Cash", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Pay the driver after your ride", fontSize = 12.5.sp, color = XMuted)
            }
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

        Spacer(modifier = Modifier.height(24.dp))

        // OTHER PAYMENT METHODS
        Text(
            text = "OTHER PAYMENT METHODS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Mobile money
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .clickable {
                    scope.launch { AppPrefs.setPayment(context, "M-Pesa") }
                    Toast.makeText(context, "Mobile money selected", Toast.LENGTH_SHORT).show()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📱", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Mobile money", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "M-Pesa, Airtel Money, Mixx by Yas", fontSize = 12.5.sp, color = XMuted)
            }
            Text(text = "›", fontSize = 22.sp, color = Color(0xFF98A6A0))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Debit or credit card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .clickable {
                    scope.launch { AppPrefs.setPayment(context, "Card") }
                    Toast.makeText(context, "Card selected", Toast.LENGTH_SHORT).show()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💳", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Debit or credit card", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Visa and Mastercard", fontSize = 12.5.sp, color = XMuted)
            }
            Text(text = "›", fontSize = 22.sp, color = Color(0xFF98A6A0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Payments made simpler Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(XPill)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = XMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(
                    text = "Payments made simpler",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = XTitle
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Your details remain protected. Method availability may vary.",
                    fontSize = 12.5.sp,
                    color = XMuted,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Add payment method Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(XMintBtn)
                .clickable(onClick = onAddMethod),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add payment method +",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ------------------------------------------------------------------
// Schedule — Advance booking with rich cards
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
        Text(
            text = "Book ahead for stress-free pickups across Dar es Salaam.",
            fontSize = 13.5.sp,
            color = XMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Location Route Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(XCard)
                .padding(16.dp)
        ) {
            // Pickup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(XMint)
                )
                OutlinedTextField(
                    value = pickup,
                    onValueChange = { pickup = it },
                    placeholder = { Text("Pickup address", fontSize = 13.5.sp, color = XMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAFCFB),
                        unfocusedContainerColor = Color(0xFFFAFCFB),
                        focusedTextColor = XTitle,
                        unfocusedTextColor = XTitle,
                        focusedBorderColor = XMint,
                        unfocusedBorderColor = XBorder
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .border(2.dp, XMint, CircleShape)
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    placeholder = { Text("Where to? e.g. Mlimani City", fontSize = 13.5.sp, color = XMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAFCFB),
                        unfocusedContainerColor = Color(0xFFFAFCFB),
                        focusedTextColor = XTitle,
                        unfocusedTextColor = XTitle,
                        focusedBorderColor = XMint,
                        unfocusedBorderColor = XBorder
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Date Selection
        Text(
            text = "SELECT DATE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            days.forEach { d ->
                val sel = day == d
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (sel) XPill else XCard)
                        .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(16.dp))
                        .clickable { day = d }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = d,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) XMint else XTitle
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time Selection
        Text(
            text = "SELECT TIME",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            times.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { t ->
                        val sel = time == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (sel) XPill else XCard)
                                .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(14.dp))
                                .clickable { time = t }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sel) XMint else XTitle
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Vehicle Option
        Text(
            text = "CHOOSE RIDE TYPE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("Standard", "1–4 seats • Popular choice", "TZS 3,200"),
                Triple("Comfort", "1–4 seats • Extra legroom", "TZS 4,200"),
                Triple("XL", "1–6 seats • Group rides", "TZS 5,800")
            ).forEach { (opt, desc, prc) ->
                val sel = option == opt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (sel) 2.dp else 0.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(XCard)
                        .border(if (sel) 1.8.dp else 1.dp, if (sel) XMint else XBorder, RoundedCornerShape(18.dp))
                        .clickable { option = opt }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(XPill),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚗", fontSize = 18.sp)
                        }
                        Column {
                            Text(text = opt, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                            Spacer(Modifier.height(2.dp))
                            Text(text = desc, fontSize = 12.sp, color = XMuted)
                        }
                    }
                    Text(text = prc, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XMint)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val price = when (option) { "Comfort" -> "TZS 4,200"; "XL" -> "TZS 5,800"; else -> "TZS 3,200" }
        // Gradient Confirm Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF063F32), Color(0xFF008F64))
                    )
                )
                .clickable {
                    if (destination.isBlank()) {
                        Toast.makeText(context, "Enter a destination first", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    RideRepository.scheduleRide(pickup, destination, day, time, option, price)
                    Toast.makeText(context, "Scheduled $day $time • $price", Toast.LENGTH_LONG).show()
                }
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Confirm Schedule • $price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Upcoming Scheduled Rides
        Text(
            text = "UPCOMING SCHEDULED RIDES (${scheduled.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (scheduled.isEmpty()) {
            Text(text = "No scheduled rides yet.", fontSize = 13.sp, color = XMuted)
        } else {
            scheduled.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(XCard)
                        .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${s.pickup} → ${s.destination}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = XTitle)
                        Spacer(Modifier.height(4.dp))
                        Text(text = "${s.dateLabel} • ${s.timeLabel} • ${s.option} • ${s.price}", fontSize = 12.sp, color = XMuted)
                    }
                    Text(
                        text = "Cancel",
                        color = Color(0xFFD92D20),
                        fontSize = 13.sp,
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

// ------------------------------------------------------------------
// Safety — SOS, share trip, tips
// ------------------------------------------------------------------
@Composable
fun SafetyScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val ride by RideRepository.activeRide.collectAsState()
    AliveScaffold(title = "Safety & Protection", onBack = onBack) {
        Text(
            text = "24/7 emergency assistance, trip sharing, and rider guidelines.",
            fontSize = 13.5.sp,
            color = XMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Emergency SOS Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFEF2F2))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Emergency SOS (112)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Instantly connects you to local emergency services in Tanzania.",
                fontSize = 12.5.sp,
                color = Color(0xFFB91C1C),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFDC2626))
                    .clickable {
                        try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri())) }
                        catch (_: Exception) { Toast.makeText(context, "Dial 112", Toast.LENGTH_SHORT).show() }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Call 112 Emergency", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SAFETY TOOLS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        SafetyToolCard(
            icon = "📤",
            title = "Share Live Location",
            subtitle = "Send real-time ride tracking to family or friends",
            onClick = {
                val text = if (ride != null) "I'm in a Rida ${ride!!.option}: ${ride!!.pickup} → ${ride!!.destination}, driver ${ride!!.driver?.name ?: "assigning"} ${ride!!.driver?.plate ?: ""}, fare ${ride!!.price}."
                else "I'm using Rida passenger app in Dar es Salaam."
                try {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                    }, "Share trip"))
                } catch (_: Exception) { Toast.makeText(context, "Copy: $text", Toast.LENGTH_LONG).show() }
            }
        )

        SafetyToolCard(
            icon = "📞",
            title = "24/7 Support Hotline",
            subtitle = "Call 0800 750 000 for instant app support",
            onClick = {
                try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:0800750000".toUri())) }
                catch (_: Exception) { Toast.makeText(context, "Call 0800 750 000", Toast.LENGTH_SHORT).show() }
            }
        )

        SafetyToolCard(
            icon = "👥",
            title = "Trusted Emergency Contacts",
            subtitle = "Speed-dial your primary emergency contact",
            onClick = {
                try { context.startActivity(Intent(Intent.ACTION_DIAL)) }
                catch (_: Exception) { }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RIDER SAFETY GUIDELINES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                "Verify license plate & driver photo before boarding.",
                "Share live trip tracking for night rides.",
                "Sit in the rear seat whenever possible.",
                "Only pay the fare displayed in the Rida app."
            ).forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(XPill),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", color = XMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = tip, fontSize = 13.sp, color = XBody, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun SafetyToolCard(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(XCard)
            .border(1.dp, XBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(XPill),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.5.sp, color = XMuted)
        }
        Text(text = "›", fontSize = 22.sp, color = Color(0xFF98A6A0))
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
fun RidePreferencesScreen(
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val context = LocalContext.current
    var quietRide by remember { mutableStateOf(false) }
    var extraPickupTime by remember { mutableStateOf(true) }
    var shareTripStatus by remember { mutableStateOf(true) }
    var preciseLocation by remember { mutableStateOf(true) }

    AliveScaffold(title = "Ride preferences", onBack = onBack) {
        Text(text = "Make every journey feel more like yours.", fontSize = 13.5.sp, color = XMuted)
        Spacer(modifier = Modifier.height(20.dp))

        // COMFORT PREFERENCES
        Text(
            text = "COMFORT PREFERENCES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Quiet ride
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    tint = XMint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Quiet ride", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Prefer minimal conversation", fontSize = 12.5.sp, color = XMuted)
            }
            Switch(
                checked = quietRide,
                onCheckedChange = { quietRide = it },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Extra pickup time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = XMint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Extra pickup time", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Allow time to reach the vehicle", fontSize = 12.5.sp, color = XMuted)
            }
            Switch(
                checked = extraPickupTime,
                onCheckedChange = { extraPickupTime = it },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LOCATION & SAFETY
        Text(
            text = "LOCATION & SAFETY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7A88),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Share trip status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = XMint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Share trip status", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Make live trip sharing easier", fontSize = 12.5.sp, color = XMuted)
            }
            Switch(
                checked = shareTripStatus,
                onCheckedChange = { shareTripStatus = it },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Precise pickup location
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XCard)
                .border(1.dp, XBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(XPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = XMint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Precise pickup location", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = XTitle)
                Spacer(Modifier.height(2.dp))
                Text(text = "Use your accurate location when booking", fontSize = 12.5.sp, color = XMuted)
            }
            Switch(
                checked = preciseLocation,
                onCheckedChange = { preciseLocation = it },
                colors = SwitchDefaults.colors(checkedTrackColor = XMintBtn)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Banner Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XPill)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Preferences",
                tint = XMint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Settings are preferences, not guarantees.",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = XTitle
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Save preferences Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = XMintBtn.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(XMintBtn)
                .clickable {
                    Toast.makeText(context, "Preferences saved", Toast.LENGTH_SHORT).show()
                    onSave()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save preferences",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
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
        SafetyToolCard("📞", "Call support", "0800 750 000 • free") {
            try { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:0800750000".toUri())) } catch (_: Exception) { }
        }
        SafetyToolCard("✉", "Email us", "support@rida.co.tz") {
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:support@rida.co.tz".toUri()))
            } catch (_: Exception) { Toast.makeText(context, "support@rida.co.tz", Toast.LENGTH_LONG).show() }
        }
        SafetyToolCard("💬", "WhatsApp", "+255 700 000 000") {
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
