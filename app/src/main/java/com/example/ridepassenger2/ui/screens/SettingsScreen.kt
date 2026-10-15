package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RidaLightTheme

private val LogoutRed = Color(0xFFD92D20)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onLogOut: () -> Unit = {},
    onCommunication: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onRidePreferences: () -> Unit = {},
    onCalendar: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onSignInSecurity: () -> Unit = {},
    onLegal: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HelpPageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                RidaIcon(
                    icon = RidaIcons.Back,
                    contentDescription = "Back",
                    tint = HelpTitleText
                )
            }
            RidaLogo(
                green = HelpGreen,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title & Subtitle Header
            item {
                Column {
                    Text(
                        text = "Settings",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Your account, preferences and privacy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // "PREFERENCES" Section Header
            item {
                Text(
                    text = "PREFERENCES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Preferences Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Communication
                        SettingsOptionRow(
                            icon = RidaIcons.Sound,
                            title = "Communication",
                            subtitle = "Notifications and updates",
                            onClick = onCommunication
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Language
                        SettingsOptionRow(
                            icon = RidaIcons.Language,
                            title = "Language",
                            subtitle = "English",
                            onClick = onLanguage
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Ride preferences
                        SettingsOptionRow(
                            icon = RidaIcons.Rides,
                            title = "Ride preferences",
                            subtitle = "Customize your rides",
                            onClick = onRidePreferences
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Calendar
                        SettingsOptionRow(
                            icon = RidaIcons.Calendar,
                            title = "Calendar",
                            subtitle = "Smart ride suggestions",
                            onClick = onCalendar
                        )
                    }
                }
            }

            // "SECURITY & PRIVACY" Section Header
            item {
                Text(
                    text = "SECURITY & PRIVACY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Security & Privacy Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Privacy
                        SettingsOptionRow(
                            icon = RidaIcons.Safety,
                            title = "Privacy",
                            subtitle = null,
                            onClick = onPrivacy
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Sign in & security
                        SettingsOptionRow(
                            icon = RidaIcons.Lock,
                            title = "Sign in & security",
                            subtitle = null,
                            onClick = onSignInSecurity
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Legal
                        SettingsOptionRow(
                            icon = RidaIcons.HelpArticles,
                            title = "Legal",
                            subtitle = null,
                            onClick = onLegal
                        )
                    }
                }
            }

            // "Log out" Button Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onLogOut)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RidaIcon(
                            icon = RidaIcons.Logout,
                            contentDescription = null,
                            tint = LogoutRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Log out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LogoutRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    icon: Int,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HelpGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = icon,
                contentDescription = null,
                tint = HelpGreen,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HelpTitleText
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = HelpMutedText
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        RidaIcon(
            icon = RidaIcons.ChevronRight,
            contentDescription = null,
            tint = HelpMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SettingsScreenPreview() {
    RidaLightTheme {
        SettingsScreen()
    }
}
