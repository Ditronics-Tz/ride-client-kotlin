package com.example.ridepassenger2.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.widget.Toast
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
import com.example.ridepassenger2.data.mock.ActiveRide
import com.example.ridepassenger2.data.mock.RidePhase
import com.example.ridepassenger2.data.mock.RideRepository
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
import org.osmdroid.util.GeoPoint

// ---------------------------------------------------------------------------
// Palette — dark home, matched to reference (dark map + dark sheet + mint)
// ---------------------------------------------------------------------------
private val DarkPageBg = Color(0xFF0A0F14)
private val DarkSheetBg = Color(0xFF101418)
private val DarkCardBg = Color(0xFF181F26)
private val DarkCardBg2 = Color(0xFF151C22)
private val DarkBorder = Color(0xFF26313B)
private val DarkDivider = Color(0xFF232D36)
private val DarkPill = Color(0xFF1C252D)

private val DarkTitle = Color.White
private val DarkBody = Color(0xFFF3F4F6)
private val DarkMuted = Color(0xFF9CA3AF)
private val DarkFaint = Color(0xFF6B7884)
private val DarkChevron = Color(0xFF3A4653)

private val Mint = Color(0xFF43D2A1)
private val MintSoft = Color(0xFF35C48E)
private val OnMintDark = Color(0xFF0C1014)

private val DarCenter = GeoPoint(-6.7924, 39.2083)

// ---------------------------------------------------------------------------
// Home — dark, matched to reference: dark CARTO tiles + dark sheet
// ---------------------------------------------------------------------------
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    onProfile: () -> Unit = {},
    onRequestRide: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableStateOf("Ride") }
    var selectedOption by remember { mutableStateOf("Standard") }

    // Map state
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var destination by remember { mutableStateOf<GeoPoint?>(null) }
    var destinationLabel by remember { mutableStateOf<String?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var routeDistanceM by remember { mutableStateOf<Double?>(null) }
    var routeDurationS by remember { mutableStateOf<Double?>(null) }
    var mapCenter by remember { mutableStateOf(DarCenter) }
    var mapZoom by remember { mutableStateOf(13.8) }

    // Search (Nominatim)
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NominatimPlace>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf(false) }

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
        if (query.trim().length < 2) { searchResults = emptyList(); isSearching = false; searchError = false; return@LaunchedEffect }
        delay(520)
        isSearching = true
        searchError = false
        try {
            val q = if (query.contains(",")) query else "$query, Dar es Salaam"
            searchResults = MapServiceFactory.nominatim.search(query = q, limit = 6)
        } catch (_: Exception) { searchResults = emptyList(); searchError = true } finally { isSearching = false }
    }

    // Road-following mint route (OSRM fastest). Falls back to the map center
    // as origin so a route still draws before the GPS fix lands.
    LaunchedEffect(destination, currentLocation) {
        val dest = destination
        if (dest == null) {
            routePoints = emptyList(); routeDistanceM = null; routeDurationS = null
            return@LaunchedEffect
        }
        val origin = currentLocation ?: mapCenter
        try {
            val coords = osrmCoords(origin.longitude, origin.latitude, dest.longitude, dest.latitude)
            val resp = MapServiceFactory.osrm.route(coords = coords)
            val route = resp.routes.firstOrNull()
            if (route != null && route.geometry.coordinates.size >= 2) {
                routePoints = route.geometry.coordinates.map { (lon, lat) -> GeoPoint(lat, lon) }
                routeDistanceM = route.distance; routeDurationS = route.duration
                mapCenter = GeoPoint((origin.latitude + dest.latitude) / 2, (origin.longitude + dest.longitude) / 2)
                mapZoom = when { route.distance < 3000 -> 15.0; route.distance < 8000 -> 13.8; route.distance < 15000 -> 12.8; else -> 11.5 }
            } else {
                routePoints = emptyList()
                Toast.makeText(context, "No road route found to that pin", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            routePoints = emptyList()
            Toast.makeText(context, "Couldn't calculate the route — check connection", Toast.LENGTH_SHORT).show()
        }
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

    // ---- Live mock ride ----
    val activeRide by RideRepository.activeRide.collectAsState()
    val justCompleted by RideRepository.lastCompleted.collectAsState()
    var driverPin by remember { mutableStateOf<GeoPoint?>(null) }
    val busy = activeRide != null

    fun lerp(a: GeoPoint, b: GeoPoint, t: Double) = GeoPoint(
        a.latitude + (b.latitude - a.latitude) * t,
        a.longitude + (b.longitude - a.longitude) * t
    )

    // Driver movement + phase advancement (mock GPS, ticks every ~2s)
    LaunchedEffect(activeRide?.phase) {
        val phase = activeRide?.phase ?: run { driverPin = null; return@LaunchedEffect }
        val pickup = currentLocation ?: return@LaunchedEffect
        when (phase) {
            RidePhase.DRIVER_FOUND -> {
                var pos = GeoPoint(pickup.latitude + 0.012, pickup.longitude + 0.012)
                driverPin = pos
                repeat(6) {
                    delay(2000)
                    if (RideRepository.activeRide.value?.phase != RidePhase.DRIVER_FOUND) return@LaunchedEffect
                    pos = lerp(pos, pickup, 0.25)
                    driverPin = pos
                }
                RideRepository.markArriving()
            }
            RidePhase.ARRIVING -> {
                repeat(3) {
                    delay(2000)
                    if (RideRepository.activeRide.value?.phase != RidePhase.ARRIVING) return@LaunchedEffect
                }
                RideRepository.markInTrip()
                onRequestRide()
            }
            RidePhase.IN_TRIP -> {
                val pts = routePoints
                if (pts.size >= 2) {
                    val step = (pts.size / 6).coerceAtLeast(1)
                    var i = 0
                    while (i < pts.size) {
                        delay(2000)
                        if (RideRepository.activeRide.value?.phase != RidePhase.IN_TRIP) return@LaunchedEffect
                        driverPin = pts[i]
                        i += step
                    }
                } else {
                    delay(10000)
                    if (RideRepository.activeRide.value?.phase != RidePhase.IN_TRIP) return@LaunchedEffect
                }
                RideRepository.completeRide()
            }
            else -> Unit
        }
    }

    // Completion toast (fires when returning from the ride screen too)
    LaunchedEffect(justCompleted) {
        justCompleted?.let {
            Toast.makeText(context, "Trip completed • ${it.price} • saved to Activity", Toast.LENGTH_LONG).show()
            RideRepository.consumeCompleted()
        }
    }

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded, skipHiddenState = true)
    )
    val sheetPeek = 232.dp

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = sheetPeek,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = DarkSheetBg,
        sheetContentColor = DarkTitle,
        sheetShadowElevation = 16.dp,
        sheetDragHandle = null,
        sheetSwipeEnabled = true,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Drag handle
                Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFF2A3540)))
                Spacer(modifier = Modifier.height(12.dp))

                // Bolt category chips — green family to match the home design.
                // Active = solid green, inactive = dark with green outline + green text.
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(listOf("Ride" to "🚗", "Share" to "👥", "Schedule" to "🕒", "Package" to "📦")) { (label, icon) ->
                        val active = selectedTab == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (active) MintSoft else DarkPill)
                                .border(1.dp, if (active) MintSoft else Mint.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                                .clickable {
                                    when (label) {
                                        "Share" -> { selectedTab = label }
                                        "Schedule", "Package" -> Toast.makeText(context, "$label coming soon", Toast.LENGTH_SHORT).show()
                                        else -> selectedTab = label
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 12.sp)
                                Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (active) OnMintDark else Mint)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (destination == null) {
                    // Saved places — Bolt Home/Work shortcuts
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SavedPlaceCard(icon = "⌂", title = "Home", subtitle = "Set home", modifier = Modifier.weight(1f), onClick = { Toast.makeText(context, "Saved places coming soon", Toast.LENGTH_SHORT).show() })
                        SavedPlaceCard(icon = "💼", title = "Work", subtitle = "Set work", modifier = Modifier.weight(1f), onClick = { Toast.makeText(context, "Saved places coming soon", Toast.LENGTH_SHORT).show() })
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Recent
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(DarkPill), contentAlignment = Alignment.Center) { Text(text = "🕒", fontSize = 10.sp) }
                            Text(text = "Recent", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
                        }
                        Text(text = "Clear", fontSize = 11.sp, color = DarkMuted, fontWeight = FontWeight.Medium)
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
                                    .background(DarkCardBg)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { query = route.substringAfter("→").trim(); searchFocused = true }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkPill), contentAlignment = Alignment.Center) { Text(text = "🕒", fontSize = 12.sp) }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = route, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = DarkBody)
                                    Text(text = meta, fontSize = 11.sp, color = DarkMuted)
                                }
                                Text(text = "›", color = DarkChevron, fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Payment method — Bolt sheet row
                    PaymentRow(onChange = { Toast.makeText(context, "Payments coming soon", Toast.LENGTH_SHORT).show() })
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // Route summary header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Mint.copy(alpha = 0.10f))
                            .border(1.dp, Mint.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) { Text(text = "→", color = OnMintDark, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = destinationLabel ?: "Your trip", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DarkTitle)
                                Text(text = "$etaText • ${routeDistanceM?.let { "%.1f km".format(it / 1000) } ?: ""} • via OSRM", fontSize = 11.sp, color = DarkMuted)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Mint).clickable { destination = null; query = ""; routePoints = emptyList() }.padding(horizontal = 9.dp, vertical = 5.dp)) {
                                Text(text = "Change", color = OnMintDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Live ride status replaces the options while a ride is active
                    val ride = activeRide
                    if (ride == null) {
                    if (selectedTab == "Share") {
                        SharedRidesSection { item ->
                            driverPin = null
                            RideRepository.requestRide(
                                pickup = "Your location",
                                destination = destinationLabel ?: "Destination",
                                price = item.price,
                                option = "Shared",
                                etaMin = routeDurationS?.div(60)?.toInt() ?: 10
                            )
                        }
                    } else {
                    // Choose a ride
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Mint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Text(text = "🚗", fontSize = 11.sp) }
                            Text(text = "Choose a ride", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(DarkPill).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(text = "$etaText · ${routeDistanceM?.let { "%.1f km".format(it / 1000) } ?: ""}", fontSize = 11.sp, color = DarkMuted, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumRideOption(title = "Standard", subtitle = "1–4 seats · Shared rides", price = priceText, eta = etaText, badge = "Popular", selected = selectedOption == "Standard", onClick = { selectedOption = "Standard" })
                        PremiumRideOption(title = "Comfort", subtitle = "1–4 seats · Extra legroom", price = run { val d = (routeDistanceM ?: 4500.0) / 1000; "TZS ${"%,d".format(3500 + (d * 380).toInt())}" }, eta = run { val s = (routeDurationS ?: 720.0) + 120; "${(s / 60).toInt()} min" }, selected = selectedOption == "Comfort", onClick = { selectedOption = "Comfort" })
                        PremiumRideOption(title = "XL", subtitle = "1–6 seats · Group rides", price = run { val d = (routeDistanceM ?: 4500.0) / 1000; "TZS ${"%,d".format(5000 + (d * 420).toInt())}" }, eta = run { val s = (routeDurationS ?: 900.0) + 180; "${(s / 60).toInt()} min" }, selected = selectedOption == "XL", onClick = { selectedOption = "XL" })
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Trust row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardBg2)
                            .border(1.dp, DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TrustItem("4.8 ★", "Drivers")
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkBorder))
                        TrustItem("2 min", "Avg pickup")
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkBorder))
                        TrustItem("24/7", "Support")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Payment method — Bolt sheet row
                    PaymentRow(onChange = { Toast.makeText(context, "Payments coming soon", Toast.LENGTH_SHORT).show() })
                    Spacer(modifier = Modifier.height(16.dp))

                    // CTA — mint with dark text (reference)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Mint.copy(alpha = 0.30f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MintSoft)
                            .clickable(onClick = {
                                driverPin = null
                                RideRepository.requestRide(
                                    pickup = "Your location",
                                    destination = destinationLabel ?: "Destination",
                                    price = priceText,
                                    option = selectedOption,
                                    etaMin = routeDurationS?.div(60)?.toInt() ?: 10
                                )
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Request Ride", color = OnMintDark, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                    }
                    } else {
                        RideStatusCard(
                            phase = ride.phase,
                            ride = ride,
                            onCancel = { driverPin = null; RideRepository.cancelRide() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        content = {
            // Map + header + search overlay — dark
            Box(modifier = Modifier.fillMaxSize().background(DarkPageBg)) {
                RidaMapView(
                    modifier = Modifier.fillMaxSize(),
                    center = mapCenter,
                    zoom = mapZoom,
                    useGoogleTiles = false,
                    useSatellite = false,
                    useDarkTiles = true,
                    currentLocation = currentLocation,
                    destination = destination,
                    routePoints = routePoints,
                    driverLocations = if (driverPin != null && busy) listOf(driverPin!!) else driverPins,
                    enableMyLocation = permissionState.status.isGranted
                )

                Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Compact top bar — Rida + location + search in ONE slim card,
                    // leaving maximum room for the map underneath.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.4f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkCardBg2.copy(alpha = 0.96f))
                            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { repeat(2) { Box(modifier = Modifier.width(4.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(Mint)) } }
                                    Text(text = "Rida", color = DarkTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Your location", fontSize = 10.sp, color = DarkMuted, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = if (permissionState.status.isGranted) (currentLocation?.let { "%.4f, %.4f".format(it.latitude, it.longitude) } ?: "Locating…") else "Tap to enable GPS",
                                            fontSize = 11.5.sp,
                                            color = DarkBody,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DarkCardBg)
                                            .border(1.dp, DarkBorder, CircleShape)
                                            .clickable(onClick = onProfile),
                                        contentAlignment = Alignment.Center
                                    ) { Text(text = "DM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkTitle) }
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkDivider))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(MintSoft))
                                TextField(
                                    value = query,
                                    onValueChange = { query = it; searchFocused = true },
                                    placeholder = { Text(text = "Where to?", fontSize = 13.sp, color = DarkFaint) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { searchResults.firstOrNull()?.let { p -> val gp = GeoPoint(p.lat.toDouble(), p.lon.toDouble()); destination = gp; destinationLabel = p.displayName.substringBefore(","); query = p.displayName.substringBefore(","); searchFocused = false; searchResults = emptyList(); keyboardController?.hide() } }),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = DarkTitle,
                                        unfocusedTextColor = DarkTitle,
                                        cursorColor = Mint
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp, color = DarkTitle)
                                )
                                if (query.isNotEmpty()) Box(modifier = Modifier.clip(CircleShape).background(DarkPill).clickable { query = ""; destination = null; destinationLabel = null; routePoints = emptyList(); searchResults = emptyList() }.padding(6.dp)) { Text(text = "✕", fontSize = 10.sp, color = DarkMuted) }
                                else Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(DarkPill).border(1.dp, DarkBorder, CircleShape).clickable { if (!permissionState.status.isGranted) permissionState.launchPermissionRequest() else fetchLocation() }, contentAlignment = Alignment.Center) { Text(text = "◎", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }

                    // Nominatim results — dark dropdown under the search card
                    AnimatedVisibility(visible = searchFocused && (isSearching || searchError || searchResults.isNotEmpty())) {
                        Box(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(DarkCardBg2).border(1.dp, DarkBorder, RoundedCornerShape(16.dp))) {
                            if (isSearching) Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Mint)
                                Text(text = "Searching Nominatim…", fontSize = 12.sp, color = DarkMuted)
                            } else if (searchError) Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "⚠", fontSize = 14.sp)
                                Text(text = "Couldn't search — check your connection and retry", fontSize = 12.sp, color = DarkMuted)
                            } else LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                items(searchResults) { place ->
                                    Row(modifier = Modifier.fillMaxWidth().clickable { val gp = GeoPoint(place.lat.toDouble(), place.lon.toDouble()); destination = gp; destinationLabel = place.displayName.substringBefore(","); query = place.displayName.substringBefore(","); searchFocused = false; searchResults = emptyList(); keyboardController?.hide() }.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(DarkPill), contentAlignment = Alignment.Center) { Text(text = "📍", fontSize = 12.sp) }
                                        Column(modifier = Modifier.weight(1f)) { Text(text = place.displayName.substringBefore(","), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTitle); Text(text = place.displayName, fontSize = 11.sp, color = DarkMuted, maxLines = 1) }
                                    }
                                    if (place != searchResults.last()) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkDivider))
                                }
                                item { Text(text = "Tiles: Google (night) • Search: Nominatim • Route: OSRM", modifier = Modifier.padding(10.dp), fontSize = 9.sp, color = DarkFaint) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Safety + re-center rail — Bolt-style right side
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 252.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(DarkCardBg)
                            .border(1.dp, DarkBorder, CircleShape)
                            .clickable { Toast.makeText(context, "Safety toolkit coming soon", Toast.LENGTH_SHORT).show() },
                        contentAlignment = Alignment.Center
                    ) { Text(text = "🛡", fontSize = 16.sp) }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(DarkCardBg)
                            .border(1.dp, DarkBorder, CircleShape)
                            .clickable { if (!permissionState.status.isGranted) permissionState.launchPermissionRequest() else fetchLocation() },
                        contentAlignment = Alignment.Center
                    ) { Text(text = "◎", color = Mint, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Dark helpers
// ---------------------------------------------------------------------------
data class ShareRideItem(val time: String, val seatsTaken: Int, val price: String, val full: Boolean = false)

@Composable
private fun SharedRidesSection(onJoin: (ShareRideItem) -> Unit) {
    val rides = remember {
        listOf(
            ShareRideItem("10:15 AM", 2, "TZS 2,500"),
            ShareRideItem("11:00 AM", 1, "TZS 2,800"),
            ShareRideItem("12:30 PM", 3, "TZS 2,400", full = true),
            ShareRideItem("2:00 PM", 1, "TZS 2,600")
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Mint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Text(text = "👥", fontSize = 11.sp) }
            Text(text = "Shared rides", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(DarkPill).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(text = "Split the cost", fontSize = 11.sp, color = DarkMuted, fontWeight = FontWeight.Medium)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rides.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCardBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkPill)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", fontSize = 18.sp)
                    }
                    Column {
                        Text(text = item.time, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                            Text(text = "👤", fontSize = 10.sp)
                            Text(text = "${item.seatsTaken}/3 seats", fontSize = 11.sp, color = DarkMuted)
                        }
                        Text(text = item.price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTitle, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (item.full) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(DarkPill)
                            .border(1.dp, DarkBorder, RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Full", fontSize = 12.sp, color = DarkMuted)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MintSoft)
                            .clickable(onClick = { onJoin(item) })
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Join", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnMintDark)
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun RideStatusCard(phase: RidePhase, ride: ActiveRide, onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkCardBg)
            .border(1.6.dp, Mint.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (phase) {
                RidePhase.SEARCHING -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp, color = Mint)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Finding your driver…", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
                            Text(text = "Usually under a minute", fontSize = 11.5.sp, color = DarkMuted)
                        }
                    }
                }
                RidePhase.DRIVER_FOUND, RidePhase.ARRIVING -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Mint.copy(alpha = 0.14f)).border(1.dp, Mint.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(text = ride.driver?.name?.firstOrNull()?.toString() ?: "D", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Mint)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (phase == RidePhase.ARRIVING) "Driver arriving now" else "Driver found!",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTitle
                            )
                            Text(
                                text = "${ride.driver?.name ?: ""} ★ ${ride.driver?.rating ?: ""}",
                                fontSize = 12.sp, color = DarkBody, fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${ride.driver?.car ?: ""} • ${ride.driver?.plate ?: ""}",
                                fontSize = 11.sp, color = DarkMuted
                            )
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Mint.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(text = "${ride.etaMin} min", fontSize = 11.sp, color = Mint, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                RidePhase.IN_TRIP -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                            Text(text = "→", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnMintDark)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "You're on your way", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
                            Text(text = "Heading to ${ride.destination}", fontSize = 11.5.sp, color = DarkMuted)
                        }
                    }
                }
                else -> Unit
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCancel)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Cancel ride", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkMuted)
            }
        }
    }
}

@Composable
private fun TrustItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
        Text(text = label, fontSize = 10.5.sp, color = DarkMuted)
    }
}

@Composable
private fun SavedPlaceCard(icon: String, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkPill), contentAlignment = Alignment.Center) { Text(text = icon, fontSize = 13.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = DarkBody)
            Text(text = subtitle, fontSize = 11.sp, color = DarkMuted)
        }
    }
}

@Composable
private fun PaymentRow(onChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardBg2)
            .border(1.dp, DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable(onClick = onChange)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkPill), contentAlignment = Alignment.Center) { Text(text = "💵", fontSize = 13.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Cash", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = DarkBody)
            Text(text = "Default payment", fontSize = 11.sp, color = DarkMuted)
        }
        Text(text = "Change ›", fontSize = 11.5.sp, color = Mint, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PremiumRideOption(title: String, subtitle: String, price: String, eta: String, badge: String? = null, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) Mint else DarkBorder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (selected) 6.dp else 1.dp, shape = RoundedCornerShape(18.dp), ambientColor = if (selected) Mint.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(18.dp))
            .background(DarkCardBg)
            .border(width = if (selected) 1.6.dp else 1.dp, color = border, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) Mint.copy(alpha = 0.14f) else DarkPill).border(1.dp, if (selected) Mint.copy(alpha = 0.25f) else DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                OptionCarIcon(active = selected)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTitle)
                    if (badge != null && selected) Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Mint.copy(alpha = 0.14f)).border(1.dp, Mint.copy(alpha = 0.20f), RoundedCornerShape(999.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                        Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Mint)
                    }
                }
                Text(text = subtitle, fontSize = 11.5.sp, color = DarkMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = price, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DarkTitle)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (selected) Mint else DarkChevron))
                Text(text = eta, fontSize = 11.sp, color = if (selected) Mint else DarkMuted, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
            }
            if (selected) Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) { Text(text = "✓", color = OnMintDark, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun OptionCarIcon(active: Boolean, iconSize: Dp = 20.dp) {
    val tint = if (active) Mint else DarkFaint
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height; val stroke = 1.4.dp.toPx()
        val path = Path().apply { moveTo(w * 0.08f, h * 0.45f); lineTo(w * 0.22f, h * 0.30f); lineTo(w * 0.32f, h * 0.18f); lineTo(w * 0.68f, h * 0.18f); lineTo(w * 0.78f, h * 0.30f); lineTo(w * 0.92f, h * 0.45f); lineTo(w * 0.92f, h * 0.68f); lineTo(w * 0.08f, h * 0.68f); close() }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawLine(color = tint, start = Offset(w * 0.42f, h * 0.20f), end = Offset(w * 0.42f, h * 0.43f), strokeWidth = 1.1.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.58f, h * 0.20f), end = Offset(w * 0.58f, h * 0.43f), strokeWidth = 1.1.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = tint, radius = w * 0.09f, center = Offset(w * 0.28f, h * 0.76f), style = Stroke(width = 1.4.dp.toPx()))
        drawCircle(color = tint, radius = w * 0.09f, center = Offset(w * 0.72f, h * 0.76f), style = Stroke(width = 1.4.dp.toPx()))
    }
}
