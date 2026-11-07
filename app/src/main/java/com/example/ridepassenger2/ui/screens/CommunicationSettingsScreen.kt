package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import kotlinx.coroutines.launch

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val RidaGreen = Color(0xFF00A86B)
private val LightMint = Color(0xFFE8F7F0)

@Composable
fun CommunicationSettingsScreen(
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val editor = remember { com.example.ridepassenger2.data.repository.CommunicationPreferences(RidaApiClient.api, scope) }
    val state by editor.state.collectAsState()
    val enabled = !state.loading && !state.saving && state.error == null
    val rideUpdates = state.preferences.rideUpdates ?: true
    val tripMessages = state.preferences.tripMessages ?: true
    val offersPromotions = state.preferences.promotions ?: false
    val pushNotifications = state.preferences.pushEnabled ?: true
    val emailUpdates = state.preferences.emailEnabled ?: true
    val smsUpdates = state.preferences.smsEnabled ?: true
    LaunchedEffect(editor) { editor.refresh() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            if (state.loading || state.saving) Text(if (state.loading) "Loading preferences…" else "Saving preference…")
            state.error?.let { message ->
                Text(message)
                androidx.compose.material3.TextButton(onClick = { editor.refresh() }) { Text("Retry") }
            }
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.CenterStart
                ) {
                    RidaIcon(
                        icon = RidaIcons.Back,
                        contentDescription = "Back",
                        tint = InkDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                RidaLogo(green = RidaGreen)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Communication",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "How Rida keeps in touch with you.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // TRIP NOTIFICATIONS
                Text(
                    text = "TRIP NOTIFICATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                CommunicationCardRow(
                    icon = RidaIcons.Rides,
                    title = "Ride updates",
                    subtitle = "Driver assignment and arrival alerts",
                    checked = rideUpdates,
                    enabled = enabled,
                    onCheckedChange = {
                        editor.change("ride_updates", it)
                    }
                )

                Spacer(Modifier.height(10.dp))

                CommunicationCardRow(
                    icon = RidaIcons.Chat,
                    title = "Trip messages",
                    subtitle = "Driver and support messages",
                    checked = tripMessages,
                    enabled = enabled,
                    onCheckedChange = {
                        editor.change("trip_messages", it)
                    }
                )

                Spacer(Modifier.height(24.dp))

                // OFFERS
                Text(
                    text = "OFFERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                CommunicationCardRow(
                    icon = RidaIcons.Promotions,
                    title = "Offers and promotions",
                    subtitle = "Discounts and occasional updates",
                    checked = offersPromotions,
                    enabled = enabled,
                    onCheckedChange = {
                        editor.change("promotions", it)
                    }
                )

                Spacer(Modifier.height(24.dp))

                // PREFERRED CHANNELS
                Text(
                    text = "PREFERRED CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                ) {
                    SimpleChannelRow("Push notifications", pushNotifications, enabled) {
                        editor.change("push_enabled", it)
                    }
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    SimpleChannelRow("Email updates", emailUpdates, enabled) {
                        editor.change("email_enabled", it)
                    }
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    SimpleChannelRow("SMS updates", smsUpdates, enabled) {
                        editor.change("sms_enabled", it)
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CommunicationCardRow(
    icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LightMint),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = icon,
                contentDescription = title,
                tint = RidaGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = RidaGreen,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun SimpleChannelRow(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = RidaGreen,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun CommunicationSettingsScreenPreview() {
    RIDEPASSENGER2Theme {
        CommunicationSettingsScreen()
    }
}
