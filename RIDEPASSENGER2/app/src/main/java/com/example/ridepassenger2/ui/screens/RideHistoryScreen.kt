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
import com.example.ridepassenger2.data.mock.RideRepository

// Dark palette — locked to the home screen
private val ActPageBg = Color(0xFF0A0F14)
private val ActCardBg = Color(0xFF181F26)
private val ActBorder = Color(0xFF26313B)
private val ActPill = Color(0xFF1C252D)
private val ActTitle = Color.White
private val ActBody = Color(0xFFF3F4F6)
private val ActMuted = Color(0xFF9CA3AF)
private val ActFaint = Color(0xFF6B7884)
private val ActMint = Color(0xFF43D2A1)
private val ActMintBtn = Color(0xFF35C48E)
private val ActOnMint = Color(0xFF0C1014)

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
    // Live history — completed trips land here in real time.
    val items by RideRepository.history.collectAsState()

    // NOTE: Bottom bar is now provided by AppNavGraph Scaffold — do not duplicate here.
    // This screen is the "Activity" tab.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ActPageBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        // Logo row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(modifier = Modifier.width(4.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(ActMint))
                }
            }
            Text(text = "Rida", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ActTitle)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Activity", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ActTitle)
        Text(text = "Your past rides and shared journeys.", fontSize = 12.sp, color = ActMuted)
        Spacer(modifier = Modifier.height(14.dp))
        // Filter chips — green family, like home
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Completed", "Shared").forEach { tab ->
                val sel = filter == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (sel) ActMintBtn else ActPill)
                        .border(1.dp, if (sel) ActMintBtn else ActMint.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        .clickable { filter = tab }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(text = tab, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (sel) ActOnMint else ActMint)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ActCardBg)
                        .border(1.dp, ActBorder, RoundedCornerShape(16.dp))
                        .clickable { onRideClick(item) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ActPill)
                            .border(1.dp, ActBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.route, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ActTitle, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(ActMint.copy(alpha = 0.14f))
                                    .border(1.dp, ActMint.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "Completed", fontSize = 10.sp, color = ActMint, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(text = item.date, fontSize = 11.sp, color = ActMuted, modifier = Modifier.padding(top = 3.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(ActPill), contentAlignment = Alignment.Center) {
                                    Text(text = "👤", fontSize = 10.sp)
                                }
                                Text(text = item.people, fontSize = 11.sp, color = ActMuted)
                            }
                            Text(text = item.price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ActTitle)
                        }
                    }
                }
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
