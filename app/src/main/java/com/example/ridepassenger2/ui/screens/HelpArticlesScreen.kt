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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RidaLightTheme

data class ArticleTopicItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

@Composable
fun HelpArticlesScreen(
    onBack: () -> Unit = {},
    onTopicSelect: (ArticleTopicItem) -> Unit = {},
    onChatSupport: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }

    val topics = listOf(
        ArticleTopicItem("About Rida", "Learn about our service", Icons.Outlined.HelpOutline),
        ArticleTopicItem("App and features", "Using the passenger app", Icons.Outlined.Settings),
        ArticleTopicItem("Account and data", "Profile and privacy", Icons.Outlined.PersonOutline),
        ArticleTopicItem("Payments and pricing", "Fares and payment options", Icons.Outlined.CreditCard),
        ArticleTopicItem("Trips and bookings", "Managing your journeys", Icons.Outlined.DirectionsCar),
        ArticleTopicItem("Safety and support", "Getting help on the road", Icons.Outlined.Shield),
    )

    val filteredTopics = if (searchQuery.isBlank()) {
        topics
    } else {
        topics.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

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
                        text = "Help articles",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = HelpTitleText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Find answers about using Rida.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HelpMutedText
                    )
                }
            }

            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search for a topic", fontSize = 15.sp, color = HelpMutedText)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = HelpMutedText
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = HelpCardBorder,
                        focusedBorderColor = HelpGreen,
                        cursorColor = HelpGreen
                    )
                )
            }

            // "BROWSE TOPICS" Section Label
            item {
                Text(
                    text = "BROWSE TOPICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = HelpMutedText,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Browse Topics List Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HelpCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        filteredTopics.forEachIndexed { index, topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) { onTopicSelect(topic) }
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = topic.icon,
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
                                        text = topic.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HelpTitleText
                                    )
                                    Text(
                                        text = topic.subtitle,
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
                            if (index < filteredTopics.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = HelpCardBorder.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }

            // "Can't find your answer?" Chat Support Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HelpGreen.copy(alpha = 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onChatSupport)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = HelpGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "Can't find your answer?",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HelpTitleText
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Chat with Rida support",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HelpGreen
                                )
                                Text(
                                    text = "→",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HelpGreen
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
fun HelpArticlesScreenPreview() {
    RidaLightTheme {
        HelpArticlesScreen()
    }
}
