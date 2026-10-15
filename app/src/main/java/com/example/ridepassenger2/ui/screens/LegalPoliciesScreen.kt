package com.example.ridepassenger2.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun LegalPoliciesScreen(
    onBack: () -> Unit = {},
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onCommunity: () -> Unit = {},
    onLicences: () -> Unit = {},
    onAccountDeletion: () -> Unit = {}
) {
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
                    text = "Legal & policies",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Important information about using Rida.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // DOCUMENTS
                Text(
                    text = "DOCUMENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                LegalItemCard(
                    icon = RidaIcons.Document,
                    title = "Terms of service",
                    subtitle = "Rules for using Rida",
                    onClick = onTerms
                )

                Spacer(Modifier.height(10.dp))

                LegalItemCard(
                    icon = RidaIcons.Verified,
                    title = "Privacy policy",
                    subtitle = "How we handle your information",
                    onClick = onPrivacy
                )

                Spacer(Modifier.height(10.dp))

                LegalItemCard(
                    icon = RidaIcons.Heart,
                    title = "Community guidelines",
                    subtitle = "Respectful and safe rides",
                    onClick = onCommunity
                )

                Spacer(Modifier.height(10.dp))

                LegalItemCard(
                    icon = RidaIcons.Info,
                    title = "Licences and notices",
                    subtitle = "Third-party software information",
                    onClick = onLicences
                )

                Spacer(Modifier.height(24.dp))

                // ACCOUNT & APP
                Text(
                    text = "ACCOUNT & APP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                LegalItemCard(
                    icon = RidaIcons.Account,
                    title = "Account deletion",
                    subtitle = "Learn about closing your Rida account",
                    onClick = onAccountDeletion
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Rida Passenger · Version 1.0",
                    fontSize = 12.5.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))
            }

            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun LegalItemCard(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
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
        RidaIcon(
            icon = RidaIcons.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF98A6A0),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LegalPoliciesScreenPreview() {
    RIDEPASSENGER2Theme {
        LegalPoliciesScreen()
    }
}
