package com.example.ridepassenger2.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.repository.RideDriverInfo
import com.example.ridepassenger2.data.repository.RidePhase
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.map.RidaMapView
import com.example.ridepassenger2.ui.theme.RidaLightTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

private val RideError = Color(0xFFD92D20)

@Composable
fun RideDetailsScreen(onBack: () -> Unit = {}, onCancel: () -> Unit = {}) {
    val liveRide by RideRepository.activeRide.collectAsState()
    val selected by RideRepository.selectedHistory.collectAsState()
    val justCompleted by RideRepository.lastCompleted.collectAsState()
    val snapshot by RideRepository.routeSnapshot.collectAsState()
    val driverPin by RideRepository.driverPin.collectAsState()
    val ui = RideDetailsUiState.from(liveRide, justCompleted, selected)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val driverPhone = ui.activeRide?.driver?.phone?.filter { it.isDigit() || it == '+' }
    var isSharing by remember { mutableStateOf(false) }
    var historyDestination by remember(selected) { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(ui.historyItem, ui.activeRide?.rideId, ui.isReceipt) {
        historyDestination = null
        if (ui.activeRide == null && !ui.isReceipt && ui.historyItem != null) {
            try {
                val query = ui.historyItem.route.substringAfter("→").trim()
                    .ifBlank { ui.historyItem.route }
                val places = MapServiceFactory.nominatim.search(query = "$query, Dar es Salaam", limit = 1)
                places.firstOrNull()?.let {
                    historyDestination = GeoPoint(it.lat.toDouble(), it.lon.toDouble())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep the city map when this historical address cannot be geocoded.
            }
        }
    }

    val visibleSnapshot = snapshot?.takeIf { ui.activeRide != null || ui.isReceipt }
    val mapCenter = visibleSnapshot?.let {
        GeoPoint(
            (it.origin.latitude + it.destination.latitude) / 2,
            (it.origin.longitude + it.destination.longitude) / 2
        )
    } ?: historyDestination ?: GeoPoint(-6.7924, 39.2083)

    fun contactDriver(action: String, scheme: String) {
        val phone = driverPhone?.takeIf { it.length >= 9 } ?: return
        try {
            context.startActivity(Intent(action, "$scheme:$phone".toUri()))
        } catch (_: Exception) {
            Toast.makeText(context, "No app is available to contact your driver.", Toast.LENGTH_SHORT).show()
        }
    }

    RideDetailsScreenContent(
        ui = ui,
        canContactDriver = driverPhone?.let { it.length >= 9 } == true,
        isSharing = isSharing,
        onBack = onBack,
        onCall = { contactDriver(Intent.ACTION_DIAL, "tel") },
        onMessage = { contactDriver(Intent.ACTION_SENDTO, "smsto") },
        onShare = {
            val ride = ui.activeRide
            if (ride != null && !isSharing) {
                scope.launch {
                    isSharing = true
                    try {
                        val text = try {
                            "Track my Rida trip: ${RidaApiClient.api.shareRide(ride.rideId).shareUrl}"
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            "I'm in a Rida ${ride.option}: ${ride.pickup} → ${ride.destination}."
                        }
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                },
                                "Share trip"
                            )
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        Toast.makeText(context, "Unable to open trip sharing.", Toast.LENGTH_SHORT).show()
                    } finally {
                        isSharing = false
                    }
                }
            }
        },
        onPrimary = {
            when {
                ui.activeRide != null -> {
                    RideRepository.cancelRide()
                    RideRepository.selectHistory(null)
                }
                ui.isReceipt -> {
                    RideRepository.consumeCompleted()
                    RideRepository.selectHistory(null)
                    RideRepository.requestRide(ui.pickup, ui.destination, ui.price, ui.title)
                }
                ui.historyItem != null -> {
                    RideRepository.selectHistory(null)
                    RideRepository.clearRoute()
                    RideRepository.requestRide(ui.pickup, ui.destination, ui.price, ui.title)
                }
            }
            onCancel()
        },
        onSecondary = {
            if (ui.isReceipt) RideRepository.consumeCompleted()
            RideRepository.selectHistory(null)
            onCancel()
        },
        mapContent = { modifier ->
            RidaMapView(
                modifier = modifier,
                center = mapCenter,
                zoom = if ((visibleSnapshot?.distanceM ?: 9999.0) < 3000) 15.0 else 13.5,
                useGoogleTiles = false,
                useSatellite = false,
                useDarkTiles = false,
                currentLocation = visibleSnapshot?.origin,
                destination = visibleSnapshot?.destination ?: historyDestination,
                routePoints = visibleSnapshot?.points.orEmpty(),
                driverLocations = listOfNotNull(driverPin.takeIf { ui.activeRide != null })
            )
        }
    )
}

@Composable
private fun RideDetailsScreenContent(
    ui: RideDetailsUiState,
    canContactDriver: Boolean = false,
    isSharing: Boolean = false,
    onBack: () -> Unit = {},
    onCall: () -> Unit = {},
    onMessage: () -> Unit = {},
    onShare: () -> Unit = {},
    onPrimary: () -> Unit = {},
    onSecondary: () -> Unit = {},
    mapContent: @Composable (Modifier) -> Unit = { RidaMapView(modifier = it, useGoogleTiles = false) }
) {
    Column(
        modifier = Modifier.fillMaxSize().background(HelpPageBg).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                RidaIcon(RidaIcons.Back, "Back", Modifier.size(24.dp), HelpTitleText)
            }
            Text(
                text = "Ride details",
                style = MaterialTheme.typography.displaySmall,
                color = HelpTitleText,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (ui.isEmpty) {
                item(key = "empty") {
                    RideEmptyState(Modifier.fillParentMaxSize())
                }
            } else {
                item(key = "map") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, HelpCardBorder)
                    ) {
                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            mapContent(Modifier.fillMaxWidth().height((maxWidth * 0.55f).coerceIn(140.dp, 220.dp)))
                        }
                    }
                }
                item(key = "summary") { RideSummary(ui) }
                val driver = ui.activeRide?.driver
                if (driver != null) {
                    item(key = "driver") {
                        RideSection("Your driver") {
                            RideDriverCard(driver, canContactDriver, isSharing, onCall, onMessage, onShare)
                        }
                    }
                }
                item(key = "route") {
                    RideSection("Your route") {
                        RideCard {
                            Column(Modifier.padding(16.dp)) {
                                RideStop("Pickup", ui.pickup, RidaIcons.Pickup, HelpGreen, connect = true)
                                RideStop("Drop-off", ui.destination, RidaIcons.SavedPlace, HelpTitleText)
                            }
                        }
                    }
                }
                if (ui.isShared) {
                    item(key = "sharing") {
                        RideNotice(
                            icon = RidaIcons.Leaf,
                            title = if (ui.activeRide != null) "You're sharing this ride" else "Shared ride",
                            subtitle = if (ui.activeRide != null) {
                                "Good for your wallet. Better for the planet."
                            } else {
                                "This booking used a shared ride option."
                            }
                        )
                    }
                }
            }
        }

        RideDetailsActions(ui, onPrimary, onSecondary)
    }
}

@Composable
private fun RideCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HelpCardBorder),
        content = content
    )
}

@Composable
private fun RideSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = HelpTitleText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun RideSummary(ui: RideDetailsUiState) {
    RideCard {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(HelpGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    RidaIcon(RidaIcons.Rides, null, Modifier.size(24.dp), HelpGreen)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ui.title, style = MaterialTheme.typography.headlineSmall, color = HelpTitleText)
                    val subtitle = ui.historyItem?.let {
                        listOf(it.date, it.people).filter { value -> value.isNotBlank() && value != "—" }
                            .joinToString(" • ")
                    } ?: if (ui.isShared) "Shared ride" else "Your current ride"
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HelpMutedText)
                    }
                }
            }

            RideStatus(ui)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = HelpCardBorder)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RideMetric(ui.price, if (ui.activeRide != null) "Fare" else "Total fare", Modifier.weight(1f))
                ui.activeRide?.etaMin?.takeIf { it > 0 }?.let { eta ->
                    VerticalDivider(Modifier.height(36.dp).align(Alignment.CenterVertically), color = HelpCardBorder)
                    RideMetric("$eta min", "ETA", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RideMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HelpTitleText,
            textAlign = TextAlign.Center
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = HelpMutedText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RideStatus(ui: RideDetailsUiState) {
    val ride = ui.activeRide
    val phase = ride?.phase
    val status = ui.historyItem?.rideStatus?.lowercase()
    val title: String
    val subtitle: String
    var icon = RidaIcons.Navigation
    var isError = false
    when {
        phase == RidePhase.SEARCHING -> {
            title = "Finding your driver…"
            subtitle = "Your driver's details will appear here."
        }
        phase == RidePhase.DRIVER_FOUND -> {
            title = "Your driver is on the way"
            subtitle = listOfNotNull(
                ride?.driver?.plate,
                "Heading to ${ui.pickup}"
            ).filter { it.isNotBlank() && it != "—" }.joinToString(" • ")
        }
        phase == RidePhase.ARRIVING -> {
            title = "Driver arriving now"
            subtitle = "Meet at ${ui.pickup}"
            icon = RidaIcons.Pickup
        }
        phase == RidePhase.IN_TRIP -> {
            title = "You're on your way"
            subtitle = "Heading to ${ui.destination}"
            icon = RidaIcons.Route
        }
        ui.isReceipt || status == "completed" || status == "finished" -> {
            title = "Trip completed"
            subtitle = "Saved to Activity. Asante for riding Rida!"
            icon = RidaIcons.Check
        }
        status == "cancelled" || status == "canceled" -> {
            title = "Ride cancelled"
            subtitle = "This ride was cancelled."
            icon = RidaIcons.Close
            isError = true
        }
        status == "failed" || status == "expired" -> {
            title = "Ride not completed"
            subtitle = "This ride did not complete."
            icon = RidaIcons.Info
            isError = true
        }
        else -> return
    }
    Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        RideNotice(icon, title, subtitle, loading = phase == RidePhase.SEARCHING, isError = isError)
    }
}

@Composable
private fun RideNotice(icon: Int, title: String, subtitle: String, loading: Boolean = false, isError: Boolean = false) {
    val accent = if (isError) RideError else HelpGreen
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(14.dp),
        color = if (isError) RideError.copy(alpha = 0.05f) else Color(0xFFEEF5F2),
        border = BorderStroke(1.dp, if (isError) RideError.copy(alpha = 0.2f) else HelpCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(24.dp), color = accent, strokeWidth = 2.dp)
            } else {
                RidaIcon(icon, null, Modifier.size(24.dp), accent)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = HelpTitleText)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HelpMutedText)
            }
        }
    }
}

@Composable
private fun RideDriverCard(
    driver: RideDriverInfo,
    canContactDriver: Boolean,
    isSharing: Boolean,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onShare: () -> Unit
) {
    RideCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RidaIcon(RidaIcons.Account, null, Modifier.size(32.dp), HelpGreen)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = driver.name.ifBlank { "Your driver" },
                        style = MaterialTheme.typography.titleMedium,
                        color = HelpTitleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val vehicle = listOf(driver.car, driver.plate).filter { it.isNotBlank() && it != "—" }
                        .joinToString(" • ").ifBlank { "Vehicle details pending" }
                    Text(vehicle, style = MaterialTheme.typography.bodySmall, color = HelpMutedText)
                    driver.rating?.takeIf { it > 0 }?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            RidaIcon(RidaIcons.Star, null, Modifier.size(14.dp), HelpGreen)
                            Text(rating.toString(), style = MaterialTheme.typography.labelMedium, color = HelpMutedText)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DriverAction("Call", RidaIcons.Phone, canContactDriver, Modifier.weight(1f).fillMaxHeight(), onClick = onCall)
                DriverAction("Message", RidaIcons.Chat, canContactDriver, Modifier.weight(1f).fillMaxHeight(), onClick = onMessage)
                DriverAction("Share", RidaIcons.Share, !isSharing, Modifier.weight(1f).fillMaxHeight(), isSharing, onShare)
            }
        }
    }
}

@Composable
private fun DriverAction(
    label: String,
    icon: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HelpCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = HelpTitleText,
            disabledContentColor = HelpMutedText.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = HelpGreen, strokeWidth = 2.dp)
            else RidaIcon(icon, null, Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RideStop(label: String, address: String, icon: Int, tint: Color, connect: Boolean = false) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.width(24.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            RidaIcon(icon, null, Modifier.size(24.dp), tint)
            if (connect) Box(Modifier.padding(top = 6.dp).width(1.dp).weight(1f).background(HelpCardBorder))
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (connect) 20.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = HelpMutedText)
            Text(address, style = MaterialTheme.typography.titleSmall, color = HelpTitleText)
        }
    }
}

@Composable
private fun RideDetailsActions(ui: RideDetailsUiState, onPrimary: () -> Unit, onSecondary: () -> Unit) {
    Surface(color = HelpPageBg) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalDivider(color = HelpCardBorder)
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val buttonModifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                val label = when {
                    ui.activeRide != null -> "Cancel ride"
                    ui.isReceipt -> "Ride again • ${ui.price}"
                    ui.historyItem != null -> "Book again • ${ui.price}"
                    else -> "Find a ride"
                }
                if (ui.activeRide != null) {
                    OutlinedButton(
                        onClick = onPrimary,
                        modifier = buttonModifier,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, HelpCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = RideError),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                } else {
                    Button(
                        onClick = onPrimary,
                        modifier = buttonModifier,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HelpGreen, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                }
                if (ui.historyItem != null) {
                    TextButton(
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = HelpTitleText)
                    ) {
                        Text(if (ui.isReceipt) "Done" else "Close", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun RideEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RidaIcon(RidaIcons.Rides, null, Modifier.size(40.dp), HelpGreen)
        Text("No ride selected", style = MaterialTheme.typography.headlineSmall, color = HelpTitleText)
        Text(
            text = "Your active ride or a trip from Activity will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = HelpMutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "No selected ride", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RideDetailsScreenPreview() {
    RidaLightTheme {
        RideDetailsScreenContent(RideDetailsUiState())
    }
}

@Preview(name = "Small screen, large text", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun SmallRideDetailsPreview() {
    RidaLightTheme {
        RideDetailsScreenContent(RideDetailsUiState())
    }
}

@Preview(name = "No selected ride", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EmptyRideDetailsPreview() {
    RidaLightTheme { RideDetailsScreenContent(RideDetailsUiState()) }
}
