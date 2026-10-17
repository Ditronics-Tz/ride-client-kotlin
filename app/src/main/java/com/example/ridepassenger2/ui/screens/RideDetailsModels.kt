package com.example.ridepassenger2.ui.screens

import com.example.ridepassenger2.data.repository.ActiveRide

/** Presentation data shared by the itinerary, summary and rebooking actions. */
internal data class RideDetailsUiState(
    val activeRide: ActiveRide? = null,
    val historyItem: RideHistoryItem? = null,
    val isReceipt: Boolean = false
) {
    val isEmpty: Boolean get() = activeRide == null && historyItem == null
    val title: String
        get() = activeRide?.option?.takeIf { it.isNotBlank() }
            ?: historyItem?.seatsShared?.takeIf { it.isNotBlank() } ?: "Ride details"
    val price: String
        get() = activeRide?.price?.takeIf { it.isNotBlank() }
            ?: historyItem?.price?.takeIf { it.isNotBlank() } ?: "—"
    val pickup: String
        get() = activeRide?.pickup?.takeIf { it.isNotBlank() }
            ?: historyItem?.route?.substringBefore("→")?.trim()?.takeIf { it.isNotBlank() }
            ?: "Pickup"
    val destination: String
        get() = activeRide?.destination?.takeIf { it.isNotBlank() }
            ?: historyItem?.route?.takeIf { "→" in it }?.substringAfter("→")?.trim()?.takeIf { it.isNotBlank() }
            ?: "Destination"
    val isShared: Boolean
        get() = activeRide?.option?.let {
            it.contains("share", ignoreCase = true) || it.contains("pool", ignoreCase = true)
        } ?: (historyItem?.isShared == true)

    companion object {
        fun from(
            liveRide: ActiveRide?,
            justCompleted: RideHistoryItem?,
            selectedHistory: RideHistoryItem?,
            showSelectedHistory: Boolean = false
        ): RideDetailsUiState = when {
            showSelectedHistory -> RideDetailsUiState(historyItem = selectedHistory)
            liveRide != null -> RideDetailsUiState(activeRide = liveRide)
            justCompleted != null -> RideDetailsUiState(historyItem = justCompleted, isReceipt = true)
            else -> RideDetailsUiState(historyItem = selectedHistory)
        }
    }
}
