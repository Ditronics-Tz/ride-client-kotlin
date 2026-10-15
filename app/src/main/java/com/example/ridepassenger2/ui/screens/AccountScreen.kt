package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RidaLightTheme

@Composable
fun AccountScreen(
    onBack: () -> Unit = {},
    onVerifyIdentity: () -> Unit = {},
    onProfileSelect: () -> Unit = {},
    onPaymentSelect: () -> Unit = {},
    onSupportSelect: () -> Unit = {},
    onSafetySelect: () -> Unit = {},
    onSavedPlacesSelect: () -> Unit = {},
    onSettingsSelect: () -> Unit = {},
    passengerRating: String = "—",
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            // Title Header
            item {
                Text(
                    text = "Account",
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = HelpTitleText
                )
            }

            // "My account" Header Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HelpGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "My account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "Manage your Rida experience",
                                fontSize = 13.5.sp,
                                color = HelpMutedText
                            )
                            Text(
                                text = "★ $passengerRating passenger rating",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpGreen
                            )
                        }
                    }
                }
            }

            // "Ride with more confidence" Banner Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HelpGreen.copy(alpha = 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onVerifyIdentity)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = HelpGreen,
                            modifier = Modifier.size(28.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Ride with more confidence",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "Verify your identity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HelpGreen
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = HelpGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // "YOUR ACCOUNT" Section Header
            item {
                Text(
                    text = "YOUR ACCOUNT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // "YOUR ACCOUNT" Options List Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Profile
                        AccountOptionRow(
                            icon = Icons.Outlined.PersonOutline,
                            title = "Profile",
                            onClick = onProfileSelect
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Payment
                        AccountOptionRow(
                            icon = Icons.Outlined.CreditCard,
                            title = "Payment",
                            onClick = onPaymentSelect
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Support
                        AccountOptionRow(
                            icon = Icons.Outlined.HelpOutline,
                            title = "Support",
                            onClick = onSupportSelect
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Safety
                        AccountOptionRow(
                            icon = Icons.Outlined.Shield,
                            title = "Safety",
                            onClick = onSafetySelect
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Saved places
                        AccountOptionRow(
                            icon = Icons.Outlined.Place,
                            title = "Saved places",
                            onClick = onSavedPlacesSelect
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Settings
                        AccountOptionRow(
                            icon = Icons.Outlined.Settings,
                            title = "Settings",
                            onClick = onSettingsSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HelpGreen,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = HelpTitleText,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = HelpMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AccountScreenPreview() {
    RidaLightTheme {
        AccountScreen()
    }
}
