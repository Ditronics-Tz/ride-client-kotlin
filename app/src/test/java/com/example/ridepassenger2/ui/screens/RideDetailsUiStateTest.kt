package com.example.ridepassenger2.ui.screens

import com.example.ridepassenger2.data.repository.ActiveRide
import com.example.ridepassenger2.data.repository.RidePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideDetailsUiStateTest {
    private fun history(route: String = "  Mlimani City  →  Posta  ", shared: Boolean = false) = RideHistoryItem(
        route = route,
        date = "15 Oct, 14:30",
        seatsShared = "Standard",
        people = "Driver: Juma",
        price = "TZS 12,000",
        isShared = shared
    )

    @Test
    fun historyUsesSavedAddressesForItineraryAndRebooking() {
        val state = RideDetailsUiState.from(null, null, history())

        assertEquals("Mlimani City", state.pickup)
        assertEquals("Posta", state.destination)
        assertFalse(state.isReceipt)
        assertFalse(state.isEmpty)
    }

    @Test
    fun liveRideTakesPrecedenceOverReceiptAndSelectedHistory() {
        val live = ActiveRide("live", RidePhase.SEARCHING, null, "Kariakoo", "Masaki", "TZS 9,000", "Standard", 5)
        val state = RideDetailsUiState.from(live, history(), history("Ubungo → Airport"))

        assertEquals("Kariakoo", state.pickup)
        assertEquals("Masaki", state.destination)
        assertEquals("TZS 9,000", state.price)
        assertEquals(null, state.historyItem)
        assertFalse(state.isReceipt)
    }

    @Test
    fun justCompletedRideKeepsReceiptActionsAndItsOwnAddresses() {
        val state = RideDetailsUiState.from(null, history(), history("Ubungo → Airport"))

        assertTrue(state.isReceipt)
        assertEquals("Mlimani City", state.pickup)
        assertEquals("Posta", state.destination)
    }

    @Test
    fun missingRideProducesAnEmptyState() {
        val state = RideDetailsUiState.from(null, null, null)

        assertTrue(state.isEmpty)
        assertFalse(state.isReceipt)
        assertFalse(state.isShared)
    }

    @Test
    fun partialHistoryRouteDoesNotUsePickupAsDestination() {
        val state = RideDetailsUiState.from(null, null, history("Kariakoo"))

        assertEquals("Kariakoo", state.pickup)
        assertEquals("Destination", state.destination)
    }

    @Test
    fun sharingNoticeDependsOnActualRideType() {
        assertFalse(RideDetailsUiState.from(null, null, history()).isShared)
        assertTrue(RideDetailsUiState.from(null, null, history(shared = true)).isShared)
        val pooled = ActiveRide("live", RidePhase.SEARCHING, null, "A", "B", "TZS 5,000", "Pool", 5)
        assertTrue(RideDetailsUiState.from(pooled, null, null).isShared)
    }
}
