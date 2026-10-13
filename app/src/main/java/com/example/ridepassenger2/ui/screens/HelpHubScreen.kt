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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Shield
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
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RidaLightTheme

@Composable
fun HelpHubScreen(
    onBack: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    onRecentTripClick: () -> Unit = {},
    onHelpArticlesSelect: () -> Unit = {},
    onMyCasesSelect: () -> Unit = {},
    onSafetyAssistanceSelect: () -> Unit = {},
    recentTripTitle: String = "Gerezani BRT",
    recentTripSubtitle: String = "Dar es Salaam · Cancelled",
    recentTripDate: String = "22 Aug · 14:43",
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
            // Title & Subtitle Header
            item {
                Column {
                    Text(
                        text = "How can we help?",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "We're here for every step of your ride.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // "Need a hand?" Support Banner Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HelpGreen.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(30.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Need a hand?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HelpTitleText
                                )
                                Text(
                                    text = "Our support team is ready to help.",
                                    fontSize = 13.5.sp,
                                    color = HelpMutedText
                                )
                            }
                        }

                        Button(
                            onClick = onContactSupport,
                            colors = ButtonDefaults.buttonColors(containerColor = HelpGreen),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Contact support",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "→",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // "RECENT TRIP" Section Header
            item {
                Text(
                    text = "RECENT TRIP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Recent Trip Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onRecentTripClick)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(HelpGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsCar,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = recentTripTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = recentTripSubtitle,
                                fontSize = 13.sp,
                                color = HelpMutedText
                            )
                            Text(
                                text = recentTripDate,
                                fontSize = 13.sp,
                                color = HelpMutedText
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = HelpMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // "MORE WAYS TO GET HELP" Section Header
            item {
                Text(
                    text = "MORE WAYS TO GET HELP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // More Ways Options Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Option 1: Help articles
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = onHelpArticlesSelect)
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Help articles",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HelpTitleText
                                )
                                Text(
                                    text = "Browse common questions",
                                    fontSize = 13.sp,
                                    color = HelpMutedText
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = HelpMutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Option 2: My cases
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = onMyCasesSelect)
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "My cases",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HelpTitleText
                                )
                                Text(
                                    text = "Track support requests",
                                    fontSize = 13.sp,
                                    color = HelpMutedText
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = HelpMutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HelpCardBorder.copy(alpha = 0.65f)
                        )

                        // Option 3: Safety assistance
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = onSafetyAssistanceSelect)
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = HelpGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Safety assistance",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HelpTitleText
                                )
                                Text(
                                    text = "Report a safety concern",
                                    fontSize = 13.sp,
                                    color = HelpMutedText
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = HelpMutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HelpHubScreenPreview() {
    RidaLightTheme {
        HelpHubScreen()
    }
}
