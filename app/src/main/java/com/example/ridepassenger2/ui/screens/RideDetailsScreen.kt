package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.net.toUri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.mock.RidePhase
import com.example.ridepassenger2.data.mock.RideRepository
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.map.RidaMapView
import org.osmdroid.util.GeoPoint

private val DetailsBg = Color(0xFF0C1014)
private val SheetBg = Color(0xFF101418)
private val SheetBorder = Color(0xFF1C252D)
private val MintRoute = Color(0xFF43D2A1)
private val MintDot = Color(0xFF4ADE80)

@Composable
fun RideDetailsScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val liveRide by RideRepository.activeRide.collectAsState()
    val selected by RideRepository.selectedHistory.collectAsState()
    val justCompleted by RideRepository.lastCompleted.collectAsState()
    val snapshot by RideRepository.routeSnapshot.collectAsState()
    val driverPin by RideRepository.driverPin.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    // Precedence: live ride → just-completed receipt → tapped history → empty.
    val receipt = if (liveRide == null) justCompleted else null
    // History fallback: when opened from Activity, show that trip.
    val histParts = selected?.route?.split("→")?.map { it.trim() }.orEmpty()
    val showTitle = liveRide?.option ?: receipt?.seatsShared ?: selected?.seatsShared ?: "Standard"
    val showSubtitle = liveRide?.driver?.let { "${it.name} ★ ${it.rating} • ${it.car}" }
        ?: receipt?.let { "${it.date} • ${it.people}" }
        ?: selected?.let { "${it.date} • ${it.people}" } ?: "Shared ride • 1-4 seats"
    val showPrice = liveRide?.price ?: receipt?.price ?: selected?.price ?: "TZS 2,500"
    val showEta = liveRide?.let { "${it.etaMin} min" } ?: "12 min"
    val pickupLabel = liveRide?.pickup
        ?: receipt?.route?.substringBefore("→")?.trim().orEmpty().ifBlank { "Your location" }
        ?: histParts.getOrNull(0)?.ifBlank { "Kariakoo" } ?: "Kariakoo"
    val dropoffLabel = liveRide?.destination
        ?: receipt?.route?.substringAfter("→")?.trim().orEmpty().ifBlank { "Destination" }
        ?: histParts.getOrNull(1)?.ifBlank { "Mwenge" } ?: "Mwenge"

    // History has no coordinates — geocode the drop-off once so its map is real.
    var histDest by remember(selected) { mutableStateOf<GeoPoint?>(null) }
    LaunchedEffect(selected) {
        histDest = null
        val target = if (liveRide == null && receipt == null) selected else null
        if (target != null) {
            try {
                val q = target.route.substringAfter("→").trim().ifBlank { target.route }
                val places = MapServiceFactory.nominatim.search(query = "$q, Dar es Salaam", limit = 1)
                places.firstOrNull()?.let { histDest = GeoPoint(it.lat.toDouble(), it.lon.toDouble()) }
            } catch (_: Exception) { }
        }
    }

    // Live map inputs: snapshot route for live/receipt, geocoded pin for history.
    val useSnapshot = (liveRide != null || receipt != null) && snapshot != null
    val mapCenter = when {
        useSnapshot -> GeoPoint(
            (snapshot!!.origin.latitude + snapshot!!.destination.latitude) / 2,
            (snapshot!!.origin.longitude + snapshot!!.destination.longitude) / 2
        )
        histDest != null -> histDest!!
        else -> GeoPoint(-6.7924, 39.2083)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailsBg)
    ) {
        // Live map — OSRM road route + moving driver pin, Bolt-style night tiles.
        RidaMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter),
            center = mapCenter,
            zoom = if (useSnapshot && (snapshot!!.distanceM ?: 9999.0) < 3000) 15.0 else 13.5,
            useGoogleTiles = false,
            useSatellite = false,
            useDarkTiles = true,
            currentLocation = snapshot?.origin,
            destination = if (useSnapshot) snapshot!!.destination else histDest,
            routePoints = if (useSnapshot) snapshot!!.points else emptyList(),
            driverLocations = listOfNotNull(if (liveRide != null) driverPin else null)
        )

        // Top bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "‹", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                }
                Text(text = "Ride Details", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        // Bottom sheet
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(SheetBg)
                .border(1.dp, SheetBorder, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF181F26))
                            .border(1.dp, Color(0xFF26313B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", fontSize = 20.sp)
                    }
                    Column {
                        Text(text = showTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = showSubtitle, color = Color(0xFF7B8893), fontSize = 12.sp)
                    }
                }

                // Live phase banner — the trip's heartbeat. Actions fast-forward the engine.
                val phase = liveRide?.phase
                if (phase != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    when (phase) {
                        RidePhase.SEARCHING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF141B20)).border(1.dp, Color(0xFF1E2730), RoundedCornerShape(14.dp))
                                    .padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = MintRoute)
                                Column {
                                    Text(text = "Finding your driver…", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Usually under a minute", color = Color(0xFF6E7B86), fontSize = 11.5.sp)
                                }
                            }
                        }
                        RidePhase.DRIVER_FOUND -> {
                            PhaseBanner(
                                dot = MintDot,
                                title = "Driver found — heading to pickup",
                                subtitle = liveRide?.driver?.let { "${it.plate} • arriving in ~${liveRide?.etaMin} min" } ?: ""
                            )
                        }
                        RidePhase.ARRIVING -> {
                            PhaseBanner(
                                dot = MintDot,
                                title = "Driver arriving now",
                                subtitle = "Meet at $pickupLabel • ${liveRide?.driver?.plate ?: ""}"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(MintRoute)
                                    .clickable(onClick = { RideRepository.markInTrip() })
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(text = "I'm in — start trip", color = Color(0xFF0C1014), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        }
                        RidePhase.IN_TRIP -> {
                            PhaseBanner(
                                dot = MintRoute,
                                title = "You're on your way",
                                subtitle = "Heading to $dropoffLabel"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(MintRoute)
                                    .clickable(onClick = { RideRepository.completeRide() })
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(text = "Complete trip • $showPrice", color = Color(0xFF0C1014), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        }
                        else -> Unit
                    }
                }

                // Receipt banner for a just-finished trip.
                if (receipt != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F2A1F)).border(1.dp, MintRoute.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MintRoute), contentAlignment = Alignment.Center) {
                            Text(text = "✓", color = Color(0xFF0C1014), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Trip completed • $showPrice", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Saved to Activity. Asante for riding Rida!", color = Color(0xFF6E7B86), fontSize = 11.5.sp)
                        }
                    }
                }

                // Driver actions — call / message / share when a live driver exists
                if (liveRide?.driver != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141B20)).border(1.dp, Color(0xFF1E2730), RoundedCornerShape(12.dp))
                                .clickable {
                                    try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, "tel:+255700000000".toUri())) }
                                    catch (_: Exception) { android.widget.Toast.makeText(context, "Calling driver…", android.widget.Toast.LENGTH_SHORT).show() }
                                }.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "📞 Call", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141B20)).border(1.dp, Color(0xFF1E2730), RoundedCornerShape(12.dp))
                                .clickable {
                                    try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_SENDTO, "smsto:+255700000000".toUri())) }
                                    catch (_: Exception) { android.widget.Toast.makeText(context, "Messaging driver…", android.widget.Toast.LENGTH_SHORT).show() }
                                }.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "💬 Message", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141B20)).border(1.dp, Color(0xFF1E2730), RoundedCornerShape(12.dp))
                                .clickable {
                                    val r = liveRide
                                    val text = if (r != null) "I'm in a Rida ${r.option}: ${r.pickup} → ${r.destination}, driver ${r.driver?.name ?: ""} ${r.driver?.plate ?: ""}." else "My Rida trip."
                                    try { context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, text) }, "Share trip")) }
                                    catch (_: Exception) { }
                                }.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "📤 Share", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF182128)))

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "◷", color = Color(0xFF8695A2), fontSize = 14.sp)
                            Text(text = showEta, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(text = if (liveRide != null) "Pickup time" else "Duration", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column {
                        Text(text = showPrice, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Total fare", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "👥", color = Color(0xFF8695A2), fontSize = 14.sp)
                            Text(text = "2/3", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(text = "Seats filled", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stops — live pickup/destination or the tapped history route
                Column {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintDot))
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF33404D)))
                        }
                        Column {
                            Text(text = pickupLabel, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Pickup (exact location)", color = Color(0xFF6F7D89), fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
                        Column {
                            Text(text = dropoffLabel, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Drop-off (approx.)", color = Color(0xFF6F7D89), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141B20))
                        .border(1.dp, Color(0xFF1E2730), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF182622)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍃", fontSize = 16.sp, color = MintDot)
                    }
                    Column {
                        Text(text = "You're sharing this ride", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Good for your wallet. Better for the planet.", color = Color(0xFF6E7B86), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (liveRide != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161C22))
                            .border(1.dp, Color(0xFF232D36), RoundedCornerShape(12.dp))
                            .clickable(onClick = { RideRepository.cancelRide(); RideRepository.selectHistory(null); onCancel() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Cancel Ride", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                } else if (receipt != null) {
                    // Receipt: ride again re-requests the same route, Done files it away.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintRoute)
                            .clickable(onClick = {
                                RideRepository.consumeCompleted()
                                RideRepository.selectHistory(null)
                                RideRepository.requestRide(pickupLabel, dropoffLabel, showPrice, showTitle, 10)
                                onCancel()
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Ride again • $showPrice", color = Color(0xFF0C1014), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161C22))
                            .border(1.dp, Color(0xFF232D36), RoundedCornerShape(12.dp))
                            .clickable(onClick = { RideRepository.consumeCompleted(); RideRepository.selectHistory(null); onCancel() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Done", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                } else if (selected != null) {
                    // History view: Book again re-requests the same route, Close clears selection.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintRoute)
                            .clickable(onClick = {
                                RideRepository.selectHistory(null)
                                // No coordinates for old history rows — drop any stale
                                // route so the engine + map don't show the wrong trip.
                                RideRepository.clearRoute()
                                RideRepository.requestRide(pickupLabel, dropoffLabel, showPrice, showTitle, 10)
                                onCancel()
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Book again • $showPrice", color = Color(0xFF0C1014), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161C22))
                            .border(1.dp, Color(0xFF232D36), RoundedCornerShape(12.dp))
                            .clickable(onClick = { RideRepository.selectHistory(null); onCancel() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Close", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                } else {
                    // Empty: no live ride, no receipt, no history selection.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintRoute)
                            .clickable(onClick = { onCancel() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Find a ride", color = Color(0xFF0C1014), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    HomeIndicator(color = Color.White.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun PhaseBanner(dot: Color, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141B20)).border(1.dp, Color(0xFF1E2730), RoundedCornerShape(14.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dot))
        Column {
            Text(text = title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) Text(text = subtitle, color = Color(0xFF6E7B86), fontSize = 11.5.sp)
        }
    }
}
