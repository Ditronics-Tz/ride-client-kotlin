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
import com.example.ridepassenger2.ui.theme.RidaLightTheme

@Composable
fun HelpCenterScreen(
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},
    onRideHelpSelect: () -> Unit = {},
    onSomethingElseSelect: () -> Unit = {},
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
            TextButton(onClick = onExit) {
                Text(
                    text = "Exit",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HelpGreen
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Subtitle Header
            item {
                Column {
                    Text(
                        text = "How can we help you?",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Choose what you need support with.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // Green Info Banner Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HelpGreen.copy(alpha = 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RidaIcon(
                            icon = RidaIcons.Help,
                            contentDescription = null,
                            tint = HelpGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "You're in the right place",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "Find the right team for your concern.",
                                fontSize = 13.5.sp,
                                color = HelpMutedText
                            )
                        }
                    }
                }
            }

            // "GET SUPPORT" Section Label
            item {
                Text(
                    text = "GET SUPPORT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Option 1: "I need help with a ride"
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onRideHelpSelect)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HelpGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            RidaIcon(
                                icon = RidaIcons.Rides,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "I need help with a ride",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "Recent trips, drivers and fares",
                                fontSize = 13.sp,
                                color = HelpMutedText
                            )
                        }

                        RidaIcon(
                            icon = RidaIcons.ChevronRight,
                            contentDescription = null,
                            tint = HelpMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Option 2: "Something else"
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onSomethingElseSelect)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HelpGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            RidaIcon(
                                icon = RidaIcons.Chat,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Something else",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "Account, payment or other issues",
                                fontSize = 13.sp,
                                color = HelpMutedText
                            )
                        }

                        RidaIcon(
                            icon = RidaIcons.ChevronRight,
                            contentDescription = null,
                            tint = HelpMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Footer note
            item {
                Text(
                    text = "Most questions can be resolved right here.",
                    fontSize = 14.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HelpCenterScreenPreview() {
    RidaLightTheme {
        HelpCenterScreen()
    }
}
