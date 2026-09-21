package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.data.remote.osrmCoords
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.map.RidaMapView
import org.osmdroid.util.GeoPoint

// Dark palette — locked to the home screen
private val ShrPageBg = Color(0xFF0A0F14)
private val ShrCardBg = Color(0xFF181F26)
private val ShrBorder = Color(0xFF26313B)
private val ShrPill = Color(0xFF1C252D)
private val ShrTitle = Color.White
private val ShrBody = Color(0xFFF3F4F6)
private val ShrMuted = Color(0xFF9CA3AF)
private val ShrChevron = Color(0xFF3A4653)
private val ShrMint = Color(0xFF43D2A1)
private val ShrMintBtn = Color(0xFF35C48E)
private val ShrOnMint = Color(0xFF0C1014)

// District anchors for the shared Kariakoo → Mwenge route (OSRM draws the rest)
private val KariakooPt = GeoPoint(-6.8230, 39.2690)
private val MwengePt = GeoPoint(-6.7680, 39.2200)

data class ShareRideItem(val time: String, val seatsTaken: Int, val price: String, val full: Boolean = false)

@Composable
fun ShareRideScreen(
    onBack: () -> Unit = {},
    onJoin: (ShareRideItem) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    val rides = remember {
        listOf(
            ShareRideItem("10:15 AM", 2, "TZS 2,500"),
            ShareRideItem("11:00 AM", 1, "TZS 2,800"),
            ShareRideItem("12:30 PM", 3, "TZS 2,400", full = true),
            ShareRideItem("2:00 PM", 1, "TZS 2,600")
        )
    }

    // Real OSRM route for the shared corridor
    LaunchedEffect(Unit) {
        try {
            val coords = osrmCoords(KariakooPt.longitude, KariakooPt.latitude, MwengePt.longitude, MwengePt.latitude)
            val route = MapServiceFactory.osrm.route(coords = coords).routes.firstOrNull()
            if (route != null) {
                routePoints = route.geometry.coordinates.map { (lon, lat) -> GeoPoint(lat, lon) }
            }
        } catch (_: Exception) { routePoints = emptyList() }
    }
    val mapCenter = remember {
        GeoPoint(
            (KariakooPt.latitude + MwengePt.latitude) / 2,
            (KariakooPt.longitude + MwengePt.longitude) / 2
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(ShrPageBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
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
                Text(text = "‹", fontSize = 24.sp, color = ShrTitle)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Share Ride", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ShrTitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Split the cost. Fill the seats.\nChoose a ride going your way and join others.",
                fontSize = 13.sp, color = ShrMuted, lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Live night map of the shared corridor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A0F14))
                    .border(1.dp, ShrBorder, RoundedCornerShape(20.dp))
            ) {
                RidaMapView(
                    modifier = Modifier.fillMaxSize(),
                    center = mapCenter,
                    zoom = 12.5,
                    useGoogleTiles = false,
                    useSatellite = false,
                    useDarkTiles = true,
                    currentLocation = KariakooPt,
                    destination = MwengePt,
                    routePoints = routePoints,
                    enableMyLocation = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Route card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ShrCardBg)
                    .border(1.dp, ShrBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShrMint))
                        Text(text = "Kariakoo", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ShrBody)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ShrTitle))
                        Text(text = "Mwenge", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ShrBody)
                    }
                }
                Text(text = "⇅", color = ShrMuted, fontSize = 16.sp, modifier = Modifier.clickable {})
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filters — green family
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Today", "Tomorrow").forEach { f ->
                    val selected = selectedFilter == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) ShrMintBtn else ShrPill)
                            .border(1.dp, if (selected) ShrMintBtn else ShrMint.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                            .clickable { selectedFilter = f }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) ShrOnMint else ShrMint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rides.forEach { item ->
                    ShareRideCard(item = item, onJoin = { onJoin(item) })
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ShrPageBg)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HomeIndicator(color = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun ShareRideCard(item: ShareRideItem, onJoin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ShrCardBg)
            .border(1.dp, ShrBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShrPill)
                    .border(1.dp, ShrBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚗", fontSize = 18.sp)
            }
            Column {
                Text(text = item.time, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShrTitle)
                Text(text = "Kariakoo → Mwenge", fontSize = 11.sp, color = ShrMuted)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                    Text(text = "👤", fontSize = 10.sp)
                    Text(text = "${item.seatsTaken}/3 seats", fontSize = 11.sp, color = ShrMuted)
                }
                Text(text = item.price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShrTitle, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (item.full) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ShrPill)
                    .border(1.dp, ShrBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Full", fontSize = 12.sp, color = ShrMuted)
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ShrMintBtn)
                    .clickable(onClick = onJoin)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Join", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShrOnMint)
            }
        }
    }
}
