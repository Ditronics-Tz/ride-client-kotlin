package com.example.ridepassenger2.data.mock

import com.example.ridepassenger2.ui.screens.RideHistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Mock backend (frontend-only mode): drivers, live ride state machine and
 * recorded history. Screens observe the flows; timers + user actions advance
 * the phases. No network, no keys, $0.
 */
enum class RidePhase { IDLE, SEARCHING, DRIVER_FOUND, ARRIVING, IN_TRIP }

data class MockDriver(
    val name: String,
    val rating: Double,
    val car: String,
    val plate: String
)

data class ActiveRide(
    val phase: RidePhase,
    val driver: MockDriver?,
    val pickup: String,
    val destination: String,
    val price: String,
    val option: String,
    val etaMin: Int
)

object RideRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val drivers = listOf(
        MockDriver("Juma K.", 4.9, "Toyota Corolla • White", "T 123 ABC"),
        MockDriver("Amina S.", 4.8, "Honda Fit • Silver", "T 456 DEF"),
        MockDriver("Baraka M.", 4.9, "Suzuki Swift • Red", "T 789 GHI"),
        MockDriver("Neema J.", 4.7, "Nissan Note • Blue", "T 321 JKL")
    )

    private val _activeRide = MutableStateFlow<ActiveRide?>(null)
    val activeRide: StateFlow<ActiveRide?> = _activeRide.asStateFlow()

    private val _history = MutableStateFlow(
        listOf(
            RideHistoryItem("Kariakoo → Mlimani City", "Today, 8:24 AM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,500"),
            RideHistoryItem("Mbezi → Kariakoo", "Yesterday, 5:12 PM • 3 seats shared", "3 seats shared", "You + 2", "TZS 3,800"),
            RideHistoryItem("Kijitonyama → Mlimani City", "Sep 5, 2025, 7:45 AM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,200"),
            RideHistoryItem("Kariakoo → Upanga", "Sep 4, 2025, 6:20 PM • 4 seats shared", "4 seats shared", "You + 3", "TZS 4,500"),
            RideHistoryItem("Mikocheni → Kariakoo", "Sep 2, 2025, 5:10 PM • 2 seats shared", "2 seats shared", "You + 1", "TZS 2,800")
        )
    )
    val history: StateFlow<List<RideHistoryItem>> = _history.asStateFlow()

    private val _lastCompleted = MutableStateFlow<RideHistoryItem?>(null)
    val lastCompleted: StateFlow<RideHistoryItem?> = _lastCompleted.asStateFlow()

    fun isBusy(): Boolean {
        val phase = _activeRide.value?.phase
        return phase == RidePhase.SEARCHING || phase == RidePhase.DRIVER_FOUND ||
            phase == RidePhase.ARRIVING || phase == RidePhase.IN_TRIP
    }

    /** Passenger taps Request → matching starts, driver assigned after a beat. */
    fun requestRide(pickup: String, destination: String, price: String, option: String, etaMin: Int) {
        if (isBusy()) return
        _activeRide.value = ActiveRide(
            phase = RidePhase.SEARCHING,
            driver = null,
            pickup = pickup,
            destination = destination,
            price = price,
            option = option,
            etaMin = etaMin
        )
        scope.launch {
            delay(4500)
            val current = _activeRide.value
            if (current != null && current.phase == RidePhase.SEARCHING) {
                _activeRide.value = current.copy(
                    phase = RidePhase.DRIVER_FOUND,
                    driver = drivers.random()
                )
            }
        }
    }

    fun markArriving() {
        _activeRide.value?.let {
            if (it.phase == RidePhase.DRIVER_FOUND) _activeRide.value = it.copy(phase = RidePhase.ARRIVING)
        }
    }

    fun markInTrip() {
        _activeRide.value?.let {
            if (it.phase == RidePhase.ARRIVING) _activeRide.value = it.copy(phase = RidePhase.IN_TRIP)
        }
    }

    /** Trip finished → recorded at the top of Activity, ride cleared. */
    fun completeRide() {
        val ride = _activeRide.value ?: return
        val record = RideHistoryItem(
            route = "${ride.pickup} → ${ride.destination}",
            date = "Just now • ${ride.option}",
            seatsShared = ride.option,
            people = "You + driver",
            price = ride.price
        )
        _history.value = listOf(record) + _history.value
        _lastCompleted.value = record
        _activeRide.value = null
    }

    fun cancelRide() {
        _activeRide.value = null
    }

    fun consumeCompleted() {
        _lastCompleted.value = null
    }
}
