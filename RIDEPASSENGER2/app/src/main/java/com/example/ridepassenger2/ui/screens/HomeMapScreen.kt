package com.example.ridepassenger2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.data.remote.NominatimPlace
import com.example.ridepassenger2.data.remote.osrmCoords
import com.example.ridepassenger2.ui.map.RidaMapView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

// ---------------------------------------------------------------------------
// Palette — locked to RidaBottomBar (white sheet + green #0D5E3A / #237055)
// ---------------------------------------------------------------------------
private val BottomActiveGreen = Color(0xFF0D5E3A)
private val BottomCenterGreen = Color(0xFF237055)
private val BottomBorder = Color(0xFFE5E7EB)

private val LightPageBg = Color(0xFFF8FAFB)
private val CardBg = Color.White
private val CardBorder = BottomBorder
private val SheetBg = Color.White
private val PillBg = Color(0xFFF3F4F6)
private val PillInactive = Color(0xFF6B7280)
private val RequestGreen = BottomCenterGreen
private val RidaMintSoft = Color(0xFF10B981)

private val DarCenter = GeoPoint(-6.7924, 39.2083)

// ---------------------------------------------------------------------------
// Home — Bolt-style: live Google tiles + scrollable widget ABOVE the map
// ---------------------------------------------------------------------------
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    onProfile: () -> Unit = {},
    onWhereToClick: () -> Unit = {},
    onRequestRide: () -> Unit = {},
    onTabShare: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableStateOf("Ride") }
    var selectedOption by remember { mutableStateOf("Standard") }

    // Map state
    var useSatellite by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var destination by remember { mutableStateOf<GeoPoint?>(null) }
    var destinationLabel by remember { mutableStateOf<String?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var routeDistanceM by remember { mutableStateOf<Double?>(null) }
    var routeDurationS by remember { mutableStateOf<Double?>(null) }
    var mapCenter by remember { mutableStateOf(DarCenter) }
    var mapZoom by remember { mutableStateOf(13.8) }

    // Search (Nominatim) — same free examples as prompt
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NominatimPlace>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (!permissionState.status.isGranted) return
        fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                val gp = GeoPoint(loc.latitude, loc.longitude)
                currentLocation = gp; mapCenter = gp; mapZoom = 15.0
            } else {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { cur ->
                        if (cur != null) {
                            val gp = GeoPoint(cur.latitude, cur.longitude)
                            currentLocation = gp; mapCenter = gp; mapZoom = 15.0
                        }
                    }
            }
        }
    }

    LaunchedEffect(Unit) { if (!permissionState.status.isGranted) permissionState.launchPermissionRequest() }
    LaunchedEffect(permissionState.status.isGranted) { if (permissionState.status.isGranted) fetchLocation() }

    LaunchedEffect(query) {
        if (query.trim().length < 2) { searchResults = emptyList(); isSearching = false; return@LaunchedEffect }
        delay(520)
        isSearching = true
        try {
            val q = if (query.contains(",")) query else "$query, Dar es Salaam"
            searchResults = MapServiceFactory.nominatim.search(query = q, limit = 6)
        } catch (_: Exception) { searchResults = emptyList() } finally { isSearching = false }
    }

    LaunchedEffect(destination, currentLocation) {
        val dest = destination; val origin = currentLocation
        if (dest != null && origin != null) {
            try {
                val coords = osrmCoords(origin.longitude, origin.latitude, dest.longitude, dest.latitude)
                val resp = MapServiceFactory.osrm.route(coords = coords)
                val route = resp.routes.firstOrNull()
                if (route != null) {
                    routePoints = route.geometry.coordinates.map { (lon, lat) -> GeoPoint(lat, lon) }
                    routeDistanceM = route.distance; routeDurationS = route.duration
                    mapCenter = GeoPoint((origin.latitude + dest.latitude) / 2, (origin.longitude + dest.longitude) / 2)
                    mapZoom = when { route.distance < 3000 -> 15.0; route.distance < 8000 -> 13.8; route.distance < 15000 -> 12.8; else -> 11.5 }
                }
            } catch (_: Exception) { routePoints = emptyList() }
        } else if (dest == null) { routePoints = emptyList(); routeDistanceM = null; routeDurationS = null }
    }

    fun selectPlace(place: NominatimPlace) {
        val gp = GeoPoint(place.lat.toDouble(), place.lon.toDouble())
        destination = gp; destinationLabel = place.displayName.substringBefore(","); query = place.displayName.substringBefore(",")
        searchFocused = false; searchResults = emptyList(); keyboardController?.hide(); onWhereToClick()
    }

    val driverPins = remember(mapCenter) {
        val base = currentLocation ?: DarCenter
        listOf(
            GeoPoint(base.latitude + 0.008, base.longitude + 0.006),
            GeoPoint(base.latitude - 0.006, base.longitude + 0.009),
            GeoPoint(base.latitude + 0.004, base.longitude - 0.007),
        )
    }

    val etaText = routeDurationS?.let { "${(it / 60).toInt()} min" } ?: "10 min"
    val priceText = run {
        val distKm = (routeDistanceM ?: 4500.0) / 1000.0
        val fare = 2500 + (distKm * 450).toInt()
        "TZS ${"%,d".format(fare)}"
    }

    // Bolt-like: sheet always peeks (never fully hidden) so user never loses it, yet map stays clearly visible when sheet is collapsed
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded, skipHiddenState = true)
    )
    val sheetPeek = 232.dp

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = sheetPeek,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = SheetBg,
        sheetContentColor = Color(0xFF111827),
        sheetShadowElevation = 16.dp,
        sheetDragHandle = null, // we draw our own handle for pixel parity
        sheetSwipeEnabled = true,
        sheetContent = {
            // ---- SCROLLABLE WIDGET (Bolt home) ----
            // This entire Column scrolls vertically when expanded — categories, recents, ride options all live here
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Drag handle — Bolt: pull up to explore, drag down to see full map
                Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFE5E7EB)))
                Spacer(modifier = Modifier.height(10.dp))
                // Hint that sheet hides to reveal map
                Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "↓ drag down to see map", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Bolt-style category chips — not first? search card is now after these per request
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(listOf("Ride" to "🚗", "Share" to "👥", "Schedule" to "🕒", "Rent" to "🔑")) { (label, icon) ->
                        val active = selectedTab == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (active) BottomActiveGreen else PillBg)
                                .border(1.dp, if (active) BottomActiveGreen else CardBorder.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
                                .clickable {
                                    selectedTab = label
                                    if (label == "Share") onTabShare()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 12.sp)
                                Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else Color(0xFF374151))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Classic pill tabs (retain for consistency with earlier design)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(PillBg)
                        .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Ride", "Share").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSelected) CardBg else Color.Transparent)
                                    .then(if (isSelected) Modifier.shadow(2.dp, RoundedCornerShape(999.dp)).border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(999.dp)) else Modifier)
                                    .clickable { selectedTab = tab; if (tab == "Share") onTabShare() }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = tab, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, color = if (isSelected) Color(0xFF111827) else PillInactive)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // When NO destination

                // ── MOVED from top overlay → now inside scrollable widget (Bolt pattern) ──
                // Your location + Where to + quick chips — now scrolls with the sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.07f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(RidaMintSoft))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Your location", fontSize = 11.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                                Text(text = if (permissionState.status.isGranted) (currentLocation?.let { "%.4f, %.4f".format(it.latitude, it.longitude) } ?: "Locating…") else "Tap to enable GPS", fontSize = 12.5.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                            }
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PillBg).border(1.dp, CardBorder, CircleShape).clickable { if (!permissionState.status.isGranted) permissionState.launchPermissionRequest() else fetchLocation() }, contentAlignment = Alignment.Center) {
                                Text(text = "◎", color = BottomActiveGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) { Text(text = "⌖", fontSize = 10.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold) }
                            TextField(
                                value = query,
                                onValueChange = { query = it; searchFocused = true },
                                placeholder = { Text(text = "Where to? Mwenge, Kariakoo, Mlimani…", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { searchResults.firstOrNull()?.let { p -> val gp = GeoPoint(p.lat.toDouble(), p.lon.toDouble()); destination = gp; destinationLabel = p.displayName.substringBefore(","); query = p.displayName.substringBefore(","); searchFocused = false; searchResults = emptyList(); keyboardController?.hide() } }),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp, color = Color(0xFF111827))
                            )
                            if (query.isNotEmpty()) Box(modifier = Modifier.clip(CircleShape).background(PillBg).clickable { query = ""; destination = null; destinationLabel = null; routePoints = emptyList(); searchResults = emptyList() }.padding(6.dp)) { Text(text = "✕", fontSize = 10.sp, color = Color(0xFF6B7280)) }
                            else Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PillBg).border(1.dp, CardBorder, CircleShape).clickable { searchFocused = !searchFocused }, contentAlignment = Alignment.Center) { Text(text = "⇅", color = Color(0xFF6B7280), fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Mwenge", "Kariakoo", "Mlimani City", "DIT").forEach { hint ->
                                Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(PillBg).border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(999.dp)).clickable { query = hint; searchFocused = true }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                    Text(text = hint, fontSize = 11.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                // Nominatim results now live inside the sheet (scrolls with widget)
                AnimatedVisibility(visible = searchFocused && (isSearching || searchResults.isNotEmpty())) {
                    Box(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(CardBg).border(1.dp, CardBorder, RoundedCornerShape(16.dp))) {
                        if (isSearching) Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BottomActiveGreen)
                            Text(text = "Searching Nominatim…", fontSize = 12.sp, color = Color(0xFF6B7280))
                        } else LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                            items(searchResults) { place ->
                                Row(modifier = Modifier.fillMaxWidth().clickable { val gp = GeoPoint(place.lat.toDouble(), place.lon.toDouble()); destination = gp; destinationLabel = place.displayName.substringBefore(","); query = place.displayName.substringBefore(","); searchFocused = false; searchResults = emptyList(); keyboardController?.hide(); onWhereToClick() }.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(PillBg), contentAlignment = Alignment.Center) { Text(text = "📍", fontSize = 12.sp) }
                                    Column(modifier = Modifier.weight(1f)) { Text(text = place.displayName.substringBefore(","), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827)); Text(text = place.displayName, fontSize = 11.sp, color = Color(0xFF9CA3AF), maxLines = 1) }
                                }
                                if (place != searchResults.last()) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
                            }
                            item { Text(text = "Tiles: mt1.google.com/vt lyrs=m/y  •  Search: Nominatim  •  Route: OSRM", modifier = Modifier.padding(10.dp), fontSize = 9.sp, color = Color(0xFF9CA3AF)) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Recent + promo (Suggested removed per request)
                if (destination == null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(PillBg), contentAlignment = Alignment.Center) { Text(text = "🕒", fontSize = 10.sp) }
                            Text(text = "Recent", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                        Text(text = "Clear", fontSize = 11.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "Mlimani City → Kariakoo" to "Today • TZS 2,500",
                            "DIT → Mwenge" to "Yesterday • TZS 2,800",
                            "Kigamboni Ferry → Kariakoo" to "Sep 5 • TZS 4,200"
                        ).forEach { (route, meta) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBg)
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .clickable { query = route.substringAfter("→").trim(); searchFocused = true }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PillBg), contentAlignment = Alignment.Center) { Text(text = "🕒", fontSize = 12.sp) }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = route, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                    Text(text = meta, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                }
                                Text(text = "›", color = Color(0xFFD1D5DB), fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Promo — more Bolt: subtle gradient feel via two-tone + icon glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = BottomActiveGreen.copy(alpha = 0.18f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(BottomActiveGreen)
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .clickable { }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.16f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(text = "🎁", fontSize = 17.sp) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Share & save up to 40%", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Invite friends — ride credit instantly", color = Color.White.copy(alpha = 0.86f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Box(modifier = Modifier.shadow(2.dp, RoundedCornerShape(999.dp)).clip(RoundedCornerShape(999.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 7.dp)) {
                                Text(text = "Invite", color = BottomActiveGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // When destination selected: subtle route summary header inside sheet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BottomActiveGreen.copy(alpha = 0.08f))
                            .border(1.dp, BottomActiveGreen.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(BottomActiveGreen), contentAlignment = Alignment.Center) { Text(text = "→", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = destinationLabel ?: "Your trip", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF111827))
                                Text(text = "$etaText • ${routeDistanceM?.let { "%.1f km".format(it/1000)} ?: ""} • via OSRM", fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(BottomActiveGreen).clickable { destination = null; query = ""; routePoints = emptyList() }.padding(horizontal = 9.dp, vertical = 5.dp)) {
                                Text(text = "Change", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Choose a ride — only after destination chosen
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(BottomActiveGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Text(text = "🚗", fontSize = 11.sp) }
                        Text(text = "Choose a ride", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(PillBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = if (destination != null) "$etaText · ${routeDistanceM?.let { "%.1f km".format(it/1000)} ?: ""}" else "3 options", fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumRideOption(title = "Standard", subtitle = "1–4 seats · Shared rides", price = priceText, eta = etaText, badge = "Popular", selected = selectedOption == "Standard", onClick = { selectedOption = "Standard" })
                    PremiumRideOption(title = "Comfort", subtitle = "1–4 seats · Extra legroom", price = run { val d=(routeDistanceM?:4500.0)/1000; "TZS ${"%,d".format(3500 + (d*380).toInt())}" }, eta = run { val s=(routeDurationS?:720.0)+120; "${(s/60).toInt()} min" }, selected = selectedOption == "Comfort", onClick = { selectedOption = "Comfort" })
                    PremiumRideOption(title = "XL", subtitle = "1–6 seats · Group rides", price = run { val d=(routeDistanceM?:4500.0)/1000; "TZS ${"%,d".format(5000 + (d*420).toInt())}" }, eta = run { val s=(routeDurationS?:900.0)+180; "${(s/60).toInt()} min" }, selected = selectedOption == "XL", onClick = { selectedOption = "XL" })
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Bolt trust row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TrustItem("4.8 ★", "Drivers")
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(CardBorder))
                    TrustItem("2 min", "Avg pickup")
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(CardBorder))
                    TrustItem("24/7", "Support")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // CTA — sticks to bottom of scrollable sheet but scrolls with content (Bolt)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = RequestGreen.copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RequestGreen)
                        .clickable(onClick = onRequestRide),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Request Ride", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        Text(text = "→", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                } // ← close else: Choose a ride only after destination
                Spacer(modifier = Modifier.height(24.dp)) // breathing room for scroll
            }
        },
        content = {
            // Map + header overlay — BEHIND the scrollable sheet
            Box(modifier = Modifier.fillMaxSize().background(LightPageBg)) {
                RidaMapView(
                    modifier = Modifier.fillMaxSize(),
                    center = mapCenter,
                    zoom = mapZoom,
                    useGoogleTiles = true,
                    useSatellite = useSatellite,
                    currentLocation = currentLocation,
                    destination = destination,
                    routePoints = routePoints,
                    driverLocations = driverPins,
                    enableMyLocation = permissionState.status.isGranted
                )

                Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { repeat(2) { Box(modifier = Modifier.width(5.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(BottomCenterGreen)) } }
                            Text(text = "Rida", color = Color(0xFF111827), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(CardBg.copy(alpha = 0.94f))
                                    .border(1.dp, CardBorder, RoundedCornerShape(999.dp))
                                    .clickable { useSatellite = !useSatellite }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) { Text(text = if (useSatellite) "Satellite • lyrs=y" else "Road • lyrs=m", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151)) }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.06f))
                                    .clip(CircleShape)
                                    .background(CardBg)
                                    .border(1.dp, CardBorder, CircleShape)
                                    .clickable(onClick = onProfile),
                                contentAlignment = Alignment.Center
                            ) { Text(text = "DM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)) }
                        }
                    }
                    // Map controls hint — now the search card lives inside the scrollable sheet below
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(999.dp))
                            .background(CardBg.copy(alpha = 0.90f))
                            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
                            .clickable { scope.launch { sheetState.bottomSheetState.expand() } }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "↑", fontSize = 11.sp, color = BottomActiveGreen, fontWeight = FontWeight.Bold)
                        Text(text = "Where to?", fontSize = 11.5.sp, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                        Text(text = "• tap to search", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Re-center FAB — sits just above the peek so map stays usable even when sheet is collapsed
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 252.dp)
                        .size(44.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CardBg)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable { if (!permissionState.status.isGranted) permissionState.launchPermissionRequest() else fetchLocation() },
                    contentAlignment = Alignment.Center
                ) { Text(text = "◎", color = BottomActiveGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Bolt-style helpers
// ---------------------------------------------------------------------------
@Composable
private fun BoltPlaceCard(modifier: Modifier = Modifier, icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PillBg), contentAlignment = Alignment.Center) { Text(text = icon, fontSize = 13.sp) }
        Column {
            Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun TrustItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(text = label, fontSize = 10.5.sp, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun PremiumRideOption(title: String, subtitle: String, price: String, eta: String, badge: String? = null, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) BottomActiveGreen else CardBorder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (selected) 6.dp else 1.dp, shape = RoundedCornerShape(18.dp), ambientColor = if (selected) BottomActiveGreen.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(width = if (selected) 1.6.dp else 1.dp, color = border, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) BottomActiveGreen.copy(alpha = 0.10f) else PillBg).border(1.dp, if (selected) BottomActiveGreen.copy(alpha = 0.18f) else CardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                OptionCarIcon(active = selected)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    if (badge != null && selected) Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(BottomActiveGreen.copy(alpha = 0.10f)).border(1.dp, BottomActiveGreen.copy(alpha = 0.12f), RoundedCornerShape(999.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                        Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = BottomActiveGreen)
                    }
                }
                Text(text = subtitle, fontSize = 11.5.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 2.dp))
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = price, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (selected) BottomActiveGreen else Color(0xFFD1D5DB)))
                Text(text = eta, fontSize = 11.sp, color = if (selected) BottomActiveGreen else Color(0xFF9CA3AF), fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
            }
            if (selected) Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(BottomActiveGreen), contentAlignment = Alignment.Center) { Text(text = "✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun OptionCarIcon(active: Boolean, iconSize: Dp = 20.dp) {
    val tint = if (active) BottomActiveGreen else Color(0xFF9CA3AF)
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height; val stroke = 1.4.dp.toPx()
        val path = Path().apply { moveTo(w*0.08f, h*0.45f); lineTo(w*0.22f, h*0.30f); lineTo(w*0.32f, h*0.18f); lineTo(w*0.68f, h*0.18f); lineTo(w*0.78f, h*0.30f); lineTo(w*0.92f, h*0.45f); lineTo(w*0.92f, h*0.68f); lineTo(w*0.08f, h*0.68f); close() }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawLine(color = tint, start = Offset(w*0.42f, h*0.20f), end = Offset(w*0.42f, h*0.43f), strokeWidth = 1.1.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w*0.58f, h*0.20f), end = Offset(w*0.58f, h*0.43f), strokeWidth = 1.1.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = tint, radius = w*0.09f, center = Offset(w*0.28f, h*0.76f), style = Stroke(width = 1.4.dp.toPx()))
        drawCircle(color = tint, radius = w*0.09f, center = Offset(w*0.72f, h*0.76f), style = Stroke(width = 1.4.dp.toPx()))
    }
}
