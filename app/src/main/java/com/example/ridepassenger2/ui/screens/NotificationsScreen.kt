package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.remote.InboxItem
import com.example.ridepassenger2.data.repository.NotificationRepository
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.theme.RidaLightTheme
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    onNavigateToPromo: (String) -> Unit = {},
    onNavigateToRide: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notifications by NotificationRepository.notifications.collectAsState()
    val unreadCount by NotificationRepository.unreadCount.collectAsState()
    val isLoading by NotificationRepository.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        NotificationRepository.refreshInbox(context)
    }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Promos" -> notifications.filter { it.kind == "promo" }
            "Rides" -> notifications.filter { it.kind == "ride" }
            else -> notifications
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
                RidaIcon(
                    icon = RidaIcons.Back,
                    contentDescription = "Back",
                    tint = HelpTitleText
                )
            }

            Text(
                text = "Notifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HelpTitleText
            )

            if (unreadCount > 0) {
                TextButton(
                    onClick = {
                        scope.launch { NotificationRepository.markAllRead() }
                    }
                ) {
                    Text(
                        text = "Mark all read",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HelpGreen
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Promos", "Rides")
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HelpGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = HelpTitleText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = HelpCardBorder,
                        selectedBorderColor = HelpGreen
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading && notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HelpGreen)
                    }
                }
            } else if (filteredNotifications.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, HelpCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(HelpGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                RidaIcon(
                                    icon = RidaIcons.Notifications,
                                    contentDescription = null,
                                    tint = HelpGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "No notifications yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HelpTitleText
                            )
                            Text(
                                text = "We'll notify you when you receive new ride updates, promo codes or account alerts.",
                                fontSize = 13.5.sp,
                                color = HelpMutedText,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = {
                            scope.launch { NotificationRepository.markRead(item.id) }
                            val promoCode = item.promoCode
                            val rideId = item.rideId
                            if (!promoCode.isNullOrBlank()) {
                                onNavigateToPromo(promoCode)
                            } else if (!rideId.isNullOrBlank()) {
                                onNavigateToRide(rideId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: InboxItem,
    onClick: () -> Unit
) {
    val isUnread = !item.isRead
    val iconRes = when (item.kind) {
        "promo" -> RidaIcons.Promotions
        "ride" -> RidaIcons.Rides
        else -> RidaIcons.Notifications
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnread) Color.White else Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, if (isUnread) HelpGreen.copy(alpha = 0.4f) else HelpCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HelpGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                RidaIcon(
                    icon = iconRes,
                    contentDescription = null,
                    tint = HelpGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title.ifBlank { "Notification" },
                        fontSize = 15.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = HelpTitleText,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HelpGreen)
                        )
                    }
                }

                Text(
                    text = item.body,
                    fontSize = 13.5.sp,
                    color = if (isUnread) HelpTitleText else HelpMutedText,
                    lineHeight = 18.sp
                )

                if (!item.createdAt.isNullOrBlank()) {
                    Text(
                        text = item.createdAt.take(10),
                        fontSize = 11.5.sp,
                        color = HelpMutedText
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun NotificationsScreenPreview() {
    RidaLightTheme {
        NotificationsScreen()
    }
}
