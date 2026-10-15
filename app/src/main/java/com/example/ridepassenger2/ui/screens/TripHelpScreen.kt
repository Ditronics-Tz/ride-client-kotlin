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

internal val HelpGreen = Color(0xFF008F64)
internal val HelpPageBg = Color(0xFFF4FBF8)
internal val HelpCardBorder = Color(0xFFDCE8E3)
internal val HelpMutedText = Color(0xFF667085)
internal val HelpTitleText = Color(0xFF101828)
internal val HelpStatusOrange = Color(0xFFC25E00)

@Composable
fun TripHelpScreen(
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},
    onIssueSelect: (String) -> Unit = {},
    tripTitle: String = "Gerezani BRT Terminal",
    tripSubtitle: String = "Dar es Salaam · 22 Aug, 14:43",
    tripStatus: String = "Cancelled",
) {
    val issues = listOf(
        "I was wrongly charged a cancellation fee",
        "My payment hold wasn't released",
        "My driver refused to pick me up",
        "My driver didn't show up",
        "The driver was rude or abusive",
        "My pickup took longer than expected",
        "Something else",
    )

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
            horizontalArrangement = Arrangement.End
        ) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title & Subtitle Header
            item {
                Column {
                    Text(
                        text = "Get help with a trip",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Select the issue that best describes it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // Selected Trip Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
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
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tripTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = tripSubtitle,
                                fontSize = 13.sp,
                                color = HelpMutedText
                            )
                            Text(
                                text = tripStatus,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpStatusOrange
                            )
                        }
                    }
                }
            }

            // "WHAT HAPPENED?" Section Label
            item {
                Text(
                    text = "WHAT HAPPENED?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Issue Options List
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        issues.forEachIndexed { index, issue ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) { onIssueSelect(issue) }
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = issue,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HelpTitleText,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(12.dp))
                                RidaIcon(
                                    icon = RidaIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = HelpMutedText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (index < issues.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = HelpCardBorder.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TripHelpScreenPreview() {
    RidaLightTheme {
        TripHelpScreen()
    }
}
