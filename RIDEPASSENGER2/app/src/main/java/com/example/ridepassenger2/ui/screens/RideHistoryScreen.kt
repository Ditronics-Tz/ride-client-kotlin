package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HistoryEmerald = Color(0xFF238965)
private val PillActive = Color(0xFF0D221C)

data class RideHistoryItem(
    val route: String,
    val date: String,
    val seatsShared: String,
    val people: String,
    val price: String
)

@Composable
fun RideHistoryScreen(
    onRideClick: (RideHistoryItem) -> Unit = {}
) {
    // Back-compat constructor — old callers may still pass onHome/onHistory/onProfile.
    // We keep overload below.
    RideHistoryScreenInternal(onRideClick = onRideClick)
}

@Composable
private fun RideHistoryScreenInternal(
    onRideClick: (RideHistoryItem) -> Unit = {}
) {
    var filter by remember { mutableStateOf("All") }
    val items = remember {
        listOf(
            RideHistoryItem("Kariakoo → Mlimani City", "Today, 8:24 AM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,500"),
            RideHistoryItem("Mbezi → Kariakoo", "Yesterday, 5:12 PM • 3 seats shared", "3 seats shared", "You + 2", "TZS 3,800"),
            RideHistoryItem("Kijitonyama → Mlimani City", "Sep 5, 2025, 7:45 AM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,200"),
            RideHistoryItem("Kariakoo → Upanga", "Sep 4, 2025, 6:20 PM • 4 seats shared", "4 seats shared", "You + 3", "TZS 4,500"),
            RideHistoryItem("Mikocheni → Kariakoo", "Sep 2, 2025, 5:10 PM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,800")
        )
    }

    // NOTE: Bottom bar is now provided by AppNavGraph Scaffold — do not duplicate here.
    // This screen is the "Activity" tab.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        // Logo row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(modifier = Modifier.width(4.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(HistoryEmerald))
                }
            }
            Text(text = "Rida", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Activity", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
        Text(text = "Your past rides and shared journeys.", fontSize = 12.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Completed", "Shared").forEach { tab ->
                val sel = filter == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (sel) PillActive else Color(0xFFF1F4F3))
                        .clickable { filter = tab }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(text = tab, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (sel) Color.White else Color(0xFF6B7280))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRideClick(item) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", fontSize = 14.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.route, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFE3F3EC))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "Completed", fontSize = 10.sp, color = HistoryEmerald, fontWeight = FontWeight.Medium)
                            }
                        }
                        Text(text = item.date, fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "👤", fontSize = 10.sp)
                                Text(text = item.people, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                            }
                            Text(text = item.price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// Deprecated overload for any legacy call sites still using bottom-bar callbacks (onHistory alias)
@Composable
fun RideHistoryScreen(
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onRideClick: (RideHistoryItem) -> Unit = {}
) = RideHistoryScreenInternal(onRideClick = onRideClick)
