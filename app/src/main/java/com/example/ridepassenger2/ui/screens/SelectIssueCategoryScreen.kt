package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
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
import com.example.ridepassenger2.ui.theme.RidaLightTheme

data class IssueCategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

@Composable
fun SelectIssueCategoryScreen(
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},
    onCategorySelect: (IssueCategoryItem) -> Unit = {},
) {
    val categories = listOf(
        IssueCategoryItem("Rides", "Bookings, drivers and trips", Icons.Outlined.DirectionsCar),
        IssueCategoryItem("Payments", "Charges and payment methods", Icons.Outlined.CreditCard),
        IssueCategoryItem("Account", "Profile, login and data", Icons.Outlined.PersonOutline),
        IssueCategoryItem("Safety", "Report a safety concern", Icons.Outlined.Shield),
        IssueCategoryItem("Promotions", "Offers and promo codes", Icons.Outlined.LocalOffer),
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        text = "Select an issue",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "What do you need assistance with?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // "CATEGORIES" Section Label
            item {
                Text(
                    text = "CATEGORIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Category Cards List
            items(categories.size) { index ->
                val category = categories[index]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onCategorySelect(category) }
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
                            Icon(
                                imageVector = category.icon,
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
                                text = category.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HelpTitleText
                            )
                            Text(
                                text = category.subtitle,
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
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SelectIssueCategoryScreenPreview() {
    RidaLightTheme {
        SelectIssueCategoryScreen()
    }
}
