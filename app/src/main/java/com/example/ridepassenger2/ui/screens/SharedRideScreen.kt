package com.example.ridepassenger2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ridepassenger2.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun SharedRideScreen(back: () -> Unit) {
 val context=LocalContext.current
 val session by com.example.ridepassenger2.data.local.SessionManager.observe(context).collectAsState(initial=com.example.ridepassenger2.data.local.SessionManager.Session())
 key(session.loggedIn,session.sessionId) { if(session.loggedIn) SharedRideContent(back) else Text("Sign in to view shared rides.") }
}

@Composable
private fun SharedRideContent(back: () -> Unit) {
    val api = RidaApiClient.transport
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    val lock = remember { Mutex() }
    var position by remember { mutableStateOf<SharedPosition?>(null) }
    var actor by remember { mutableStateOf("") }
    var fees by remember { mutableStateOf<TransportFees?>(null) }
    var mine by remember { mutableStateOf(emptyList<SharedTrip>()) }
    var active by remember { mutableStateOf<SharedTrip?>(null) }
    var error by remember { mutableStateOf("") }
    var connection by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
 var cursor by remember { mutableLongStateOf(0) }
    var historyOffset by rememberSaveable {mutableIntStateOf(0)}
 var history by rememberSaveable { mutableStateOf(false) }

    var area by remember { mutableStateOf<SharedPoint?>(null) }
    var nearby by remember { mutableStateOf(emptyList<SharedTrip>()) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var nearbyOffset by rememberSaveable {mutableIntStateOf(0)}
 var destinationFilter by rememberSaveable { mutableStateOf("") }
 var vehicleFilter by rememberSaveable { mutableStateOf("") }
 var maxPrice by rememberSaveable { mutableStateOf("") }
 var radius by rememberSaveable { mutableDoubleStateOf(10.0) }
 var map by rememberSaveable { mutableStateOf(false) }

    suspend fun refresh() {
        if (actor.isEmpty()) actor = RidaApiClient.api.me().user.id
        fees = api.fees()
        val activePage=mergeTransportSnapshots(listOfNotNull(active)+mine,api.list("mine").trips)
 val page=if(history&&historyOffset>0)api.list("mine",offset=historyOffset).trips else activePage
 mine=mergeTransportSnapshots(mine,page)
        active = activePage.firstOrNull {
            it.isPassengerCommitment()
        }
        val center = area
        nearby =
            if (center == null || active != null) emptyList()
            else api.list("nearby", center.lat, center.lng,radius=radius,offset=nearbyOffset).trips
        position = active?.let { api.position(it.id).body() }
        connection = ""
    }
    LaunchedEffect(owner, area,radius,nearbyOffset,history,historyOffset) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                try {
                    lock.withLock { refresh() }
                } catch (e: Exception) {
                    position = null
                    connection = transportError(e)
                }
                try {
                    val trip = active
                    if (connection.isNotEmpty()) delay(2000)
                    else if (trip != null) api.waitForUpdate(tripId = trip.id,after=cursor).also { cursor=it.cursor }
                    else if (area != null) api.waitForUpdate(lat = area?.lat, lng = area?.lng,after=cursor,radius=radius).also { cursor=it.cursor }
                    else delay(2000)
                } catch (e: Exception) {
                    connection = transportError(e)
                    delay(2000)
                }
            }
        }
    }
    fun perform(signature: String, command: suspend (String) -> SharedTrip) {
        if (busy || actor.isBlank()) return
        busy = true
        error = ""
        scope.launch {
            try {
                lock.withLock {
                    val trip = rideCommand(context, actor, signature, command)
                    active = trip
                    refresh()
                }
            } catch (e: Exception) {
                error = transportError(e)
            } finally {
                busy = false
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("shared-trip-content").padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = back) { Text("Back") }
                TextButton(onClick = { history = !history }) {
                    Text(if (history) "Current trip" else "My trips")
                }
            }
        }
        item { Text("Shared rides", style = MaterialTheme.typography.headlineSmall) }
        if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error) }
        if (connection.isNotBlank()) item { Text(connection) }
        if (history) {
            if (mine.isEmpty()) item { Text("No shared trips yet.") }
            mine.forEach { t -> item(key = t.id) { TripSummary(t);SharedTripHelp(t); if(t.my_seat?.status=="completed") SharedTripRating(t) } }
 item {Row{TextButton(enabled=historyOffset>0,onClick={historyOffset=(historyOffset-50).coerceAtLeast(0)}){Text("Previous history")};TextButton(enabled=mine.size==50,onClick={historyOffset+=50}){Text("Next history")}}}
        } else {
            val trip = active
            if (trip != null) {
                item {
                    TripSummary(trip)
 SharedTripHelp(trip)
                    val point = position
                    if (point == null) Text("Live driver location unavailable")
                    else {
                        PickupMap(trip.pickup, listOf(trip), point) {}
                        Text("Driver location updated ${point.updated_at}")
                    }
                }

                item {
                    val seat = trip.my_seat
                    if (seat?.status == "held") {
                        val baseline =
                            remember(seat.id, trip.server_time) {
                                android.os.SystemClock.elapsedRealtime()
                            }
                        var seconds by
                            remember(seat.id, trip.server_time) {
                                mutableLongStateOf(
                                    (seat.expires - trip.server_time).coerceAtLeast(0)
                                )
                            }
                        LaunchedEffect(baseline, seat.expires) {
                            while (seconds > 0) {
                                delay(500)
                                seconds =
                                    remainingHoldSeconds(seat.expires,trip.server_time,baseline,android.os.SystemClock.elapsedRealtime())
                            }
                        }
                        if (seconds > 0) {
                            Text("Hold: %d:%02d".format(seconds / 60, seconds % 60))
                            Text(
                                "Pickup code: ${trip.pin.orEmpty()}",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                "Show this code to the driver at the pickup point to confirm your seat."
                            )
                        } else Text("Seat hold expired. Refreshing availability…")
                    } else Text("Seat ${seat?.status.orEmpty()}")
                    TextButton(
                        onClick = {
                            val p = trip.pickup
                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:${p.lat},${p.lng}?q=${p.lat},${p.lng}"),
                                )
                            if (intent.resolveActivity(context.packageManager) != null)
                                context.startActivity(intent)
                            else
                                error =
                                    "No maps app is available. Pickup coordinates: ${p.lat}, ${p.lng}."
                        }
                    ) {
                        Text("Directions to pickup")
                    }
                    if (trip.status == "waiting")
                        TextButton(
                            enabled = !busy,
                            onClick = {
                                perform("release/${trip.id}/${seat?.id}") {
                                    api.act(trip.id, "release", it)
                                }
                            },
                        ) {
                            Text("Release seat")
                        }
                }
            } else {
                if (fees == null) item { Text("Loading trip settings…") }
                else if (fees?.enabled != true)
                    item { Text("Ride sharing is closed for new bookings.") }
                else {
                    item {
                        PlaceField("Pickup area", area) {
                            area = it
 nearbyOffset=0;cursor=0
                            selected = null
                        }
                        UseLocation {
                            area = it
 nearbyOffset=0;cursor=0
                            selected = null
                        }
                    }
                    area?.let { center ->
                        item {
                            Text("Within ${radius.toInt()} km of ${center.address}")
 Row { listOf(5.0,10.0,25.0,50.0).forEach{km->TextButton(onClick={radius=km;cursor=0}){Text("${km.toInt()} km")}} }
 OutlinedTextField(destinationFilter,{destinationFilter=it},label={Text("Filter destination")})
 OutlinedTextField(vehicleFilter,{vehicleFilter=it},label={Text("Filter vehicle type")})
 OutlinedTextField(maxPrice,{maxPrice=it.filter(Char::isDigit)},label={Text("Maximum seat price (TZS)")})
                            TextButton(onClick = { map = !map }) {
                                Text(if (map) "Show list" else "Show pickup map")
                            }
                            if (map) PickupMap(center, nearby.filter{(destinationFilter.isBlank()||it.dropoff.address.contains(destinationFilter,true))&&(vehicleFilter.isBlank()||it.vehicle.contains(vehicleFilter,true))&&(maxPrice.toLongOrNull()?.let{max->it.price<=max}?:true)}) { selected = it }
                        }
                        item {Text("Showing up to 50 trips per page. Filters apply to this page.");Row{TextButton(enabled=nearbyOffset>0,onClick={nearbyOffset=(nearbyOffset-50).coerceAtLeast(0);selected=null}){Text("Previous trips")};TextButton(enabled=nearby.size==50,onClick={nearbyOffset+=50;selected=null}){Text("Next trips")}}}
                        if (nearby.isEmpty()) item { Text("No bookable shared trips nearby.") }
                        nearby
                            .filter { t-> destinationFilter.isBlank()||t.dropoff.address.contains(destinationFilter,true) }
 .filter { t-> vehicleFilter.isBlank()||t.vehicle.contains(vehicleFilter,true) }
 .filter { t-> maxPrice.toLongOrNull()?.let{t.price<=it}?:true }
 .filter { !map || selected==null || it.id == selected || nearby.find{t->t.id==selected}?.let{t->t.pickup.lat==it.pickup.lat&&t.pickup.lng==it.pickup.lng}==true }
                            .forEach { t ->
                                item(key = t.id) {
                                    TripSummary(t)
 Text("Arrive at pickup within five minutes. Your seat is confirmed only after the driver verifies your pickup code.")
                                    Button(
                                        enabled = !busy && actor.isNotEmpty() && t.available > 0,
                                        onClick = {
                                            perform("reserve/${t.id}") {
                                                api.act(t.id, "reserve", it)
                                            }
                                        },
                                    ) {
                                        Text("Hold one seat for 5 minutes")
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSummary(t: SharedTrip) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            "${t.pickup.address} → ${t.dropoff.address}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text("${t.vehicle} · TZS ${t.price} per seat · ${t.status}")
        Text("${t.available} available · ${t.held} held · ${t.confirmed} verified")
 Text("Pickup deadline: "+java.text.SimpleDateFormat("dd MMM HH:mm",java.util.Locale.getDefault()).apply{timeZone=java.util.TimeZone.getTimeZone("Africa/Dar_es_Salaam")}.format(java.util.Date(t.deadline*1000)))
 if(t.driver_name.isNotBlank()) Text("Driver: ${t.driver_name}")
 t.receipt?.let { r->
 Text("Trip receipt · Cash paid directly to driver")
 Text("Gross TZS ${r.gross} · Whole-trip fee TZS ${r.fee} · Driver net TZS ${r.net}")
 Text(if(r.status=="wallet_debited") "Platform fee posted to driver wallet" else "Platform fee receivable — collection pending")
 }
 t.my_seat?.takeIf{it.status=="completed"}?.let{Text("Your cash fare: TZS ${t.price}") }
    }
}

@Composable
private fun PickupMap(
    center: SharedPoint,
    trips: List<SharedTrip>,
    driver: SharedPosition? = null,
    select: (String) -> Unit,
) {
    val context = LocalContext.current
    val map = remember {
        MapView(context).apply {
            org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
            setMultiTouchControls(true)
            controller.setZoom(13.0)
        }
    }
    LaunchedEffect(center) { map.controller.setCenter(GeoPoint(center.lat, center.lng)) }
    DisposableEffect(map) {
        map.onResume()
        onDispose {
            map.onPause()
            map.onDetach()
        }
    }
    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxWidth().height(260.dp),
        update = {
            map.overlays.clear()
            trips.groupBy{Pair(it.pickup.lat,it.pickup.lng)}.values.forEach { group ->
 val t=group.first()
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(t.pickup.lat, t.pickup.lng)
                        title = if(group.size>1) "${group.size} trips: ${t.pickup.address}" else t.pickup.address
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            select(t.id)
                            true
                        }
                    }
                )
            }
            driver?.let { point ->
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(point.lat, point.lng)
                        title = "Driver"
                        setTextIcon("Driver")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                )
            }
            map.invalidate()
        },
    )
    Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall)
}
