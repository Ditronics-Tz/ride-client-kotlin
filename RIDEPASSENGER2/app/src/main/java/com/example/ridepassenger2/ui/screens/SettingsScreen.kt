package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator

private val SettingsGreen = Color(0xFF3C9E7D)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", fontSize = 20.sp, color = Color(0xFF111827))
                }
                Text(text = "Settings", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                SettingsSection(title = "Account") {
                    SettingsRow(icon = "👤", title = "Personal Information", subtitle = "Name, phone, email")
                    SettingsRow(icon = "💳", title = "Payment Methods", subtitle = "Cards & mobile money")
                    SettingsRow(icon = "📍", title = "Saved Addresses", subtitle = "Home, work, frequent places")
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "App Preferences") {
                    SettingsRow(icon = "🔔", title = "Notifications", subtitle = "Ride updates, messages, offers")
                    SettingsRow(icon = "🛡", title = "Privacy & Security", subtitle = "Data, location, account")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "🌙", fontSize = 16.sp)
                            Column {
                                Text(text = "Dark Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                Text(text = "System setting", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                            }
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { darkMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SettingsGreen, uncheckedTrackColor = Color(0xFFE5E7EB))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Support") {
                    SettingsRow(icon = "❓", title = "Help & FAQ", subtitle = "Get answers to common questions")
                    SettingsRow(icon = "💬", title = "Contact Us", subtitle = "We're here to help")
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDF2F2))
                        .clickable(onClick = onLogOut)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "↗", color = Color(0xFFEF4444), fontSize = 14.sp)
                        Text(text = "Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HomeIndicator(color = Color.Black.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937), modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsRow(icon: String, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = icon, fontSize = 16.sp, color = Color(0xFF374151))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
        }
        Text(text = "›", fontSize = 16.sp, color = Color(0xFFD1D5DB))
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
}
