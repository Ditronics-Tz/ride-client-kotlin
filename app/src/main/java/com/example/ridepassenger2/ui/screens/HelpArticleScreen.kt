package com.example.ridepassenger2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val RidaGreen = Color(0xFF00A86B)
private val LightMint = Color(0xFFE8F7F0)

@Composable
fun HelpArticleScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var helpfulFeedback by remember { mutableStateOf<Boolean?>(null) }

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
                    text = "Help article",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Rida support • Ride booking",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Article Header Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightMint),
                        contentAlignment = Alignment.Center
                    ) {
                        RidaIcon(
                            icon = RidaIcons.HelpArticles,
                            contentDescription = null,
                            tint = RidaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "How do I change my pickup location?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Updated October 2026",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "STEP-BY-STEP GUIDE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(12.dp))

                // Guide Step 1
                Text(
                    text = "Before a driver is assigned",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Open your booking details, tap the pickup address, and select a new pickup location on the map.",
                    fontSize = 13.5.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(18.dp))

                // Guide Step 2
                Text(
                    text = "After a driver is assigned",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Depending on your trip status, location edits may be limited. Contact your driver through the app if you need assistance.",
                    fontSize = 13.5.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                // Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(LightMint)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        RidaIcon(
                            icon = RidaIcons.Info,
                            contentDescription = "Info",
                            tint = RidaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Remember",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Changing pickup points may affect time and fare.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Was this article helpful?
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Was this article helpful?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(999.dp))
                                .clickable {
                                    helpfulFeedback = true
                                    Toast.makeText(context, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 20.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "Yes",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RidaGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(999.dp))
                                .clickable {
                                    helpfulFeedback = false
                                    Toast.makeText(context, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 20.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "No",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HelpArticleScreenPreview() {
    RIDEPASSENGER2Theme {
        HelpArticleScreen()
    }
}
