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
fun SelectRideIssueScreen(
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},
    onIssueSelect: (String) -> Unit = {},
) {
    val issues = listOf(
        "I have a suggestion",
        "I lost an item",
        "I have an unknown charge",
        "I was charged more than expected",
        "I was charged twice",
        "I couldn't book a ride",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title & Subtitle Header
            item {
                Column {
                    Text(
                        text = "Select a ride issue",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tell us what's going on.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // "COMMON RIDE ISSUES" Section Label
            item {
                Text(
                    text = "COMMON RIDE ISSUES",
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
fun SelectRideIssueScreenPreview() {
    RidaLightTheme {
        SelectRideIssueScreen()
    }
}
