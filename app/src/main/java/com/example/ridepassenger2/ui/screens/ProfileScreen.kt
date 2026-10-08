package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.mock.RideRepository

// Dark palette — locked to the home screen
private val ProfPageBg = Color(0xFF0A0F14)
private val ProfCardBg = Color(0xFF181F26)
private val ProfBorder = Color(0xFF26313B)
private val ProfPill = Color(0xFF1C252D)
private val ProfTitle = Color.White
private val ProfBody = Color(0xFFF3F4F6)
private val ProfMuted = Color(0xFF9CA3AF)
private val ProfChevron = Color(0xFF3A4653)
private val ProfMint = Color(0xFF43D2A1)
private val ProfAvatarBg = Color(0xFF1E3A32)

@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    ProfileScreenInternal(onSettings = onSettings, onMenuClick = onMenuClick, onLogOut = onLogOut)
}

@Composable
private fun ProfileScreenInternal(
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    // Bottom bar is now provided by AppNavGraph Scaffold — do not duplicate here.
    val context = LocalContext.current
    val session by SessionManager.observe(context).collectAsState(initial = SessionManager.Session())
    val trips by RideRepository.history.collectAsState()
    val displayName = session.name.ifBlank { "Rida Rider" }
    val initials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "RR" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfPageBg)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(2) { Box(modifier = Modifier.width(4.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(ProfMint)) }
                }
                Text(text = "Rida", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ProfTitle)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ProfCardBg)
                    .border(1.dp, ProfBorder, CircleShape)
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚙", fontSize = 16.sp, color = ProfMuted)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Avatar section
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ProfAvatarBg)
                        .border(1.5.dp, ProfMint.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initials, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ProfTitle)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ProfTitle)
                Text(text = session.handle.ifBlank { "@rida_rider" }, fontSize = 12.sp, color = ProfMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Rides. People. A better way.", fontSize = 12.sp, color = ProfMuted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ProfCardBg)
                    .border(1.dp, ProfBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(trips.size.toString(), "Rides taken")
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(ProfBorder))
                StatItemWithStar("4.8", "Rating")
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(ProfBorder))
                StatItem(trips.size.toString(), "Rides shared")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Menu
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileMenuItem(icon = "👤", title = "Personal Information", subtitle = "Name, phone, email", onClick = { onMenuClick("personal") })
                ProfileMenuItem(icon = "💳", title = "Payment Methods", subtitle = "Cards & mobile money", onClick = { onMenuClick("payment") })
                ProfileMenuItem(icon = "📍", title = "Saved Addresses", subtitle = "Home, work, frequent places", onClick = { onMenuClick("addresses") })
                ProfileMenuItem(icon = "⚙", title = "Ride Preferences", subtitle = "Seat preferences, notifications", onClick = { onMenuClick("prefs") })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfCardBg)
                        .border(1.dp, ProfBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onLogOut)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "↗", fontSize = 16.sp, color = ProfMuted)
                    Text(text = "Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ProfBody)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Legacy overload — keep any old call sites compiling, delegate to internal
@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {},
    onHome: () -> Unit = {},
    onHistory: () -> Unit = {},
    onProfile: () -> Unit = {}
) = ProfileScreenInternal(onSettings, onMenuClick, onLogOut)

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ProfTitle)
        Text(text = label, fontSize = 11.sp, color = ProfMuted, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun StatItemWithStar(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ProfTitle)
            Text(text = "★", fontSize = 12.sp, color = ProfMint)
        }
        Text(text = label, fontSize = 11.sp, color = ProfMuted, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ProfileMenuItem(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProfCardBg)
            .border(1.dp, ProfBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(ProfPill), contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 15.sp)
            }
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ProfBody)
                Text(text = subtitle, fontSize = 11.sp, color = ProfMuted)
            }
        }
        Text(text = "›", fontSize = 18.sp, color = ProfChevron)
    }
}
