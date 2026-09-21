package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ridepassenger2.ui.components.HomeIndicator

private val ShareMint = Color(0xFF35C48E)

data class ShareRideItem(val time: String, val seatsTaken: Int, val price: String, val full: Boolean = false)

@Composable
fun ShareRideScreen(
    onBack: () -> Unit = {},
    onJoin: (ShareRideItem) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val rides = remember {
        listOf(
            ShareRideItem("10:15 AM", 2, "TZS 2,500"),
            ShareRideItem("11:00 AM", 1, "TZS 2,800"),
            ShareRideItem("12:30 PM", 3, "TZS 2,400", full = true),
            ShareRideItem("2:00 PM", 1, "TZS 2,600")
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "‹", fontSize = 24.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Share Ride", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Split the cost. Fill the seats.\nChoose a ride going your way and join others.",
                fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Route card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF7F8F9))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShareMint))
                        Text(text = "Kariakoo", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF111827)))
                        Text(text = "Mwenge", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                    }
                }
                Text(text = "⇅", color = Color(0xFF9CA3AF), fontSize = 16.sp, modifier = Modifier.clickable {})
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filters
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Today", "Tomorrow").forEach { f ->
                    val selected = selectedFilter == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) Color.Black else Color(0xFFF3F4F6))
                            .clickable { selectedFilter = f }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(rides) { item ->
                    ShareRideCard(item = item, onJoin = { onJoin(item) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HomeIndicator(color = Color.Black)
        }
    }
}

@Composable
private fun ShareRideCard(item: ShareRideItem, onJoin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚗", fontSize = 18.sp)
            }
            Column {
                Text(text = item.time, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(text = "Kariakoo → Mwenge", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                    Text(text = "👤", fontSize = 10.sp)
                    Text(text = "${item.seatsTaken}/3 seats", fontSize = 11.sp, color = Color(0xFF6B7280))
                }
                Text(text = item.price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (item.full) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Full", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ShareMint)
                    .clickable(onClick = onJoin)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Join", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
