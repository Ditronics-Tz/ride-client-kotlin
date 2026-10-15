package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.theme.RidaLightTheme

data class RideHistoryItem(
    val rideId: String? = null,
    val route: String,
    val date: String,
    val seatsShared: String,
    val people: String,
    val price: String,
    val isShared: Boolean = false,
    val rideStatus: String = "completed"
)

@Composable
fun RideHistoryScreen(onRideClick: (RideHistoryItem) -> Unit = {}) {
    var filter by rememberSaveable { mutableStateOf("All") }
    val rides by RideRepository.history.collectAsState()
    val loading by RideRepository.historyLoading.collectAsState()
    val loadError by RideRepository.historyError.collectAsState()

    LaunchedEffect(Unit) { RideRepository.refreshHistory() }

    RideHistoryScreenContent(
        rides = rides,
        filter = filter,
        loading = loading,
        hasError = loadError != null,
        onFilter = { filter = it },
        onRetry = { RideRepository.refreshHistory() },
        onRideClick = onRideClick
    )
}

@Composable
private fun RideHistoryScreenContent(
    rides: List<RideHistoryItem>,
    filter: String = "All",
    loading: Boolean = false,
    hasError: Boolean = false,
    onFilter: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    onRideClick: (RideHistoryItem) -> Unit = {}
) {
    val visible = when (filter) {
        "Shared" -> rides.filter { it.isShared }
        "Completed" -> rides.filter {
            it.rideStatus.equals("completed", ignoreCase = true) || it.rideStatus.equals("finished", ignoreCase = true)
        }
        else -> rides
    }

    Column(
        modifier = Modifier.fillMaxSize().background(HelpPageBg).statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Activity", style = MaterialTheme.typography.displaySmall, color = HelpTitleText)
            Text("Your past rides and shared journeys.", style = MaterialTheme.typography.bodyMedium, color = HelpMutedText)
        }

        Row(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                .horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Completed", "Shared").forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { onFilter(option) },
                    label = { Text(option, style = MaterialTheme.typography.labelLarge) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = HelpMutedText,
                        selectedContainerColor = HelpGreen,
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (filter == option) HelpGreen else HelpCardBorder)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hasError) {
                item(key = "error") {
                    HistoryMessage(
                        icon = RidaIcons.Warning,
                        title = "Couldn't load your rides",
                        message = "Check your connection and try again.",
                        onRetry = onRetry
                    )
                }
            }
            if (loading && rides.isEmpty()) {
                item(key = "loading") {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp), color = HelpGreen, strokeWidth = 2.dp)
                        Text("Loading your rides…", style = MaterialTheme.typography.bodyMedium, color = HelpMutedText)
                    }
                }
            } else if (visible.isEmpty() && !hasError) {
                item(key = "empty") {
                    val title = when (filter) {
                        "Completed" -> "No completed rides yet"
                        "Shared" -> "No shared rides yet"
                        else -> "No rides yet"
                    }
                    HistoryMessage(
                        icon = RidaIcons.Rides,
                        title = title,
                        message = if (filter == "All") {
                            "Take your first ride from Home. Your trips will appear here."
                        } else {
                            "Rides that match this filter will appear here."
                        }
                    )
                }
            } else {
                itemsIndexed(visible, key = { index, ride -> ride.rideId ?: "history-$index" }) { _, ride ->
                    RideHistoryCard(ride, onClick = { onRideClick(ride) })
                }
            }
        }
    }
}

@Composable
private fun RideHistoryCard(ride: RideHistoryItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().semantics { role = Role.Button },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HelpCardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RidaIcon(RidaIcons.Rides, null, Modifier.size(24.dp), HelpGreen)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        ride.seatsShared.ifBlank { "Ride" },
                        style = MaterialTheme.typography.titleMedium,
                        color = HelpTitleText
                    )
                    Text(
                        listOf(ride.date, if (ride.isShared) "Shared ride" else "")
                            .filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = HelpMutedText
                    )
                }
                RidaIcon(RidaIcons.ChevronRight, null, Modifier.size(18.dp), HelpMutedText)
            }

            val stops = ride.route.split("→", limit = 2).map { it.trim() }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HistoryRouteStop(RidaIcons.Pickup, stops.firstOrNull()?.ifBlank { "Pickup unavailable" }.orEmpty())
                stops.getOrNull(1)?.let {
                    HistoryRouteStop(RidaIcons.SavedPlace, it.ifBlank { "Drop-off unavailable" })
                }
            }

            if (ride.people.isNotBlank() && ride.people != "—") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RidaIcon(RidaIcons.Account, null, Modifier.size(18.dp), HelpMutedText)
                    Text(ride.people, style = MaterialTheme.typography.bodySmall, color = HelpMutedText, modifier = Modifier.weight(1f))
                }
            }

            HorizontalDivider(color = HelpCardBorder)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    HistoryStatus(ride.rideStatus)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Fare", style = MaterialTheme.typography.labelMedium, color = HelpMutedText)
                    Text(
                        ride.price.ifBlank { "—" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HelpTitleText,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRouteStop(icon: Int, address: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RidaIcon(icon, null, Modifier.size(18.dp), HelpGreen)
        Text(address, style = MaterialTheme.typography.bodyMedium, color = HelpTitleText, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HistoryStatus(status: String) {
    val (label, color) = when {
        status.equals("completed", true) || status.equals("finished", true) -> "Completed" to HelpGreen
        status.equals("cancelled", true) || status.equals("canceled", true) -> "Cancelled" to Color(0xFFD92D20)
        status.equals("failed", true) -> "Not completed" to Color(0xFFD92D20)
        status.equals("expired", true) -> "Expired" to HelpMutedText
        else -> "Status unavailable" to HelpMutedText
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.08f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HistoryMessage(icon: Int, title: String, message: String, onRetry: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HelpCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RidaIcon(icon, null, Modifier.size(32.dp), HelpGreen)
            Text(title, style = MaterialTheme.typography.headlineSmall, color = HelpTitleText, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = HelpMutedText, textAlign = TextAlign.Center)
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HelpGreen, contentColor = Color.White)
                ) {
                    Text("Try again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(name = "Ride history", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RideHistoryScreenPreview() {
    RidaLightTheme { RideHistoryScreenContent(emptyList()) }
}

@Preview(name = "Small screen, large text", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun SmallRideHistoryPreview() {
    RidaLightTheme { RideHistoryScreenContent(emptyList()) }
}

@Preview(name = "No rides", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EmptyRideHistoryPreview() {
    RidaLightTheme { RideHistoryScreenContent(emptyList()) }
}
