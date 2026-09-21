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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProfileGreen = Color(0xFF237055)
private val AvatarBg = Color(0xFF68BAA0)

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                    repeat(2) { Box(modifier = Modifier.width(4.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981))) }
                }
                Text(text = "Rida", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚙", fontSize = 18.sp, color = Color(0xFF374151))
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
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AvatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "DM", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Dadi Mwenge", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(text = "@dadi_mwenge", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Rides. People. A better way.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("12", "Rides taken")
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFFF3F4F6)))
                StatItemWithStar("4.8", "Rating")
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFFF3F4F6)))
                StatItem("2", "Rides shared")
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))

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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .clickable(onClick = onLogOut)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "↗", fontSize = 16.sp, color = Color(0xFF6B7280))
                    Text(text = "Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
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
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun StatItemWithStar(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(text = "★", fontSize = 12.sp, color = Color(0xFF10B981))
        }
        Text(text = label, fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ProfileMenuItem(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = icon, fontSize = 16.sp, color = Color(0xFF6B7280))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
        }
        Text(text = "›", fontSize = 18.sp, color = Color(0xFFD1D5DB))
    }
}
