package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.ridepassenger2.data.remote.*
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import android.content.Intent
import android.net.Uri

@Composable
fun SharedRideScreen(back: () -> Unit) {
    val api = RidaApiClient.transport
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    val lock = remember { Mutex() }
    var actor by remember { mutableStateOf("") }
    var fees by remember { mutableStateOf<TransportFees?>(null) }
    var mine by remember { mutableStateOf(emptyList<SharedTrip>()) }
    var active by remember { mutableStateOf<SharedTrip?>(null) }
    var error by remember { mutableStateOf("") }
    var connection by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var history by rememberSaveable { mutableStateOf(false) }
    
    var area by remember { mutableStateOf<SharedPoint?>(null) }
    var nearby by remember { mutableStateOf(emptyList<SharedTrip>()) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var map by rememberSaveable { mutableStateOf(false) }
  
    suspend fun refresh() {
        if (actor.isEmpty()) actor = RidaApiClient.api.me().user.id
        fees = api.fees()
        mine = api.list("mine").trips
        active = mine.firstOrNull { it.status in listOf("waiting","in_progress") && it.my_seat?.status in listOf("held","confirmed") }
        val center = area
        nearby = if (center == null || active != null) emptyList() else api.list("nearby", center.lat, center.lng).trips
        connection = ""
    }
    LaunchedEffect(owner, area) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                try { lock.withLock { refresh() } }
                catch (e: Exception) { connection = transportError(e) }
                delay(2000)
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
            } catch (e: Exception) { error = transportError(e) }
            finally { busy = false }
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = back) { Text("Back") }
            TextButton(onClick = { history = !history }) { Text(if (history) "Current trip" else "My trips") }
        } }
        item { Text("Shared rides", style = MaterialTheme.typography.headlineSmall) }
        if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error) }
        if (connection.isNotBlank()) item { Text(connection) }
        if (history) {
            if (mine.isEmpty()) item { Text("No shared trips yet.") }
            mine.forEach { t -> item(key=t.id) { TripSummary(t) } }
        } else {
            val trip = active
            if (trip != null) {
                item { TripSummary(trip) }
                
                item {
                    val seat = trip.my_seat
                    if (seat?.status == "held") {
                        val seconds = (seat.expires - trip.server_time).coerceAtLeast(0)
                        Text("Hold: %d:%02d".format(seconds / 60, seconds % 60))
                        Text("Pickup code: ${trip.pin.orEmpty()}", style = MaterialTheme.typography.headlineSmall)
                        Text("Show this code to the driver at the pickup point to confirm your seat.")
                    } else Text("Seat ${seat?.status.orEmpty()}")
                    TextButton(onClick = {
                        val p = trip.pickup
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${p.lat},${p.lng}?q=${p.lat},${p.lng}"))
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                        else error = "No maps app is available. Pickup coordinates: ${p.lat}, ${p.lng}."
                    }) { Text("Directions to pickup") }
                    if (trip.status == "waiting") TextButton(enabled = !busy, onClick = {
                        perform("release/${trip.id}/${seat?.id}") { api.act(trip.id,"release",it) }
                    }) { Text("Release seat") }
                }
  
            } else {
                if (fees == null) item { Text("Loading trip settings…") }
                else if (fees?.enabled != true) item { Text("Ride sharing is closed for new bookings.") }
                else { 
                    item { PlaceField("Pickup area",area) { area = it; selected = null }; UseLocation { area = it; selected = null } }
                    area?.let { center ->
                        item {
                            Text("Within 10 km of ${center.address}")
                            TextButton(onClick = { map = !map }) { Text(if (map) "Show list" else "Show pickup map") }
                            if (map) PickupMap(center, nearby) { selected = it }
                        }
                        if (nearby.isEmpty()) item { Text("No bookable shared trips nearby.") }
                        nearby.filter { !map || it.id == selected }.forEach { t -> item(key=t.id) {
                            TripSummary(t)
                            Button(enabled = !busy && actor.isNotEmpty() && t.available > 0, onClick = {
                                perform("reserve/${t.id}") { api.act(t.id,"reserve",it) }
                            }) { Text("Hold one seat for 5 minutes") }
                        } }
                    }
   }
            }
        }
    }
}

@Composable
private fun TripSummary(t: SharedTrip) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("${t.pickup.address} → ${t.dropoff.address}", style = MaterialTheme.typography.titleMedium)
        Text("${t.vehicle} · TZS ${t.price} per seat · ${t.status}")
        Text("${t.available} available · ${t.held} held · ${t.confirmed} verified")
    }
}

@Composable
private fun PickupMap(center: SharedPoint, trips: List<SharedTrip>, select: (String) -> Unit) {
    val context = LocalContext.current
    val map = remember { MapView(context).apply {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
        setMultiTouchControls(true)
        controller.setZoom(13.0)
    } }
    LaunchedEffect(center) { map.controller.setCenter(GeoPoint(center.lat,center.lng)) }
    DisposableEffect(map) { map.onResume(); onDispose { map.onPause(); map.onDetach() } }
    AndroidView(factory = { map }, modifier = Modifier.fillMaxWidth().height(260.dp), update = {
        map.overlays.clear()
        trips.forEach { t -> map.overlays.add(Marker(map).apply {
            position = GeoPoint(t.pickup.lat,t.pickup.lng); title = t.pickup.address
            setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ -> select(t.id); true }
        }) }
        map.invalidate()
    })
    Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall)
}
