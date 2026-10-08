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
import org.osmdroid.util.GeoPoint

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

    private val _selectedHistory = MutableStateFlow<RideHistoryItem?>(null)
    val selectedHistory: StateFlow<RideHistoryItem?> = _selectedHistory.asStateFlow()

    fun selectHistory(item: RideHistoryItem?) {
        _selectedHistory.value = item
    }

    /**
     * Live geography for the active ride: OSRM route + endpoints saved by Home
     * when the road route is calculated. Details screen renders this — no fake map.
     */
    data class RouteSnapshot(
        val origin: GeoPoint,
        val destination: GeoPoint,
        val destinationLabel: String,
        val points: List<GeoPoint> = emptyList(),
        val distanceM: Double? = null,
        val durationS: Double? = null
    )

    private val _routeSnapshot = MutableStateFlow<RouteSnapshot?>(null)
    val routeSnapshot: StateFlow<RouteSnapshot?> = _routeSnapshot.asStateFlow()

    fun saveRoute(
        origin: GeoPoint,
        destination: GeoPoint,
        destinationLabel: String,
        points: List<GeoPoint> = emptyList(),
        distanceM: Double? = null,
        durationS: Double? = null
    ) {
        _routeSnapshot.value = RouteSnapshot(origin, destination, destinationLabel, points, distanceM, durationS)
    }

    fun clearRoute() {
        _routeSnapshot.value = null
    }

    /** Mock GPS: driver position, driven by the engine below. Any screen can observe. */
    private val _driverPin = MutableStateFlow<GeoPoint?>(null)
    val driverPin: StateFlow<GeoPoint?> = _driverPin.asStateFlow()

    data class ScheduledRide(
        val id: Long = System.currentTimeMillis(),
        val pickup: String,
        val destination: String,
        val dateLabel: String,
        val timeLabel: String,
        val option: String,
        val price: String
    )

    private val _scheduled = MutableStateFlow<List<ScheduledRide>>(emptyList())
    val scheduled: StateFlow<List<ScheduledRide>> = _scheduled.asStateFlow()

    fun scheduleRide(
        pickup: String,
        destination: String,
        dateLabel: String,
        timeLabel: String,
        option: String,
        price: String
    ) {
        _scheduled.value = _scheduled.value + ScheduledRide(
            pickup = pickup.ifBlank { "Your location" },
            destination = destination.ifBlank { "Destination" },
            dateLabel = dateLabel,
            timeLabel = timeLabel,
            option = option,
            price = price
        )
    }

    fun cancelScheduled(id: Long) {
        _scheduled.value = _scheduled.value.filterNot { it.id == id }
    }

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
        _driverPin.value = null
        runEngine()
    }

    /**
     * Simulation engine — runs in the repo scope so EVERY screen stays live.
     * (Previously this ticked inside Home's composition and froze on Details.)
     * Manual actions (markInTrip/completeRide/cancelRide) just fast-forward it;
     * every step re-checks the phase and bails out when the user intervenes.
     */
    private fun runEngine() {
        scope.launch {
            delay(4500)
            val current = _activeRide.value
            if (current == null || current.phase != RidePhase.SEARCHING) return@launch
            _activeRide.value = current.copy(
                phase = RidePhase.DRIVER_FOUND,
                driver = drivers.random()
            )

            // Drive toward pickup (mock GPS). No snapshot (e.g. Package) → skip to arriving.
            val pickup = _routeSnapshot.value?.origin
            if (pickup != null) {
                var pos = GeoPoint(pickup.latitude + 0.012, pickup.longitude + 0.012)
                _driverPin.value = pos
                repeat(6) {
                    delay(2000)
                    if (_activeRide.value?.phase != RidePhase.DRIVER_FOUND) return@launch
                    pos = GeoPoint(
                        pos.latitude + (pickup.latitude - pos.latitude) * 0.25,
                        pos.longitude + (pickup.longitude - pos.longitude) * 0.25
                    )
                    _driverPin.value = pos
                }
            } else {
                delay(6000)
                if (_activeRide.value?.phase != RidePhase.DRIVER_FOUND) return@launch
            }
            markArriving()

            repeat(3) {
                delay(2000)
                if (_activeRide.value?.phase != RidePhase.ARRIVING) return@launch
            }
            // Auto-start the trip only if the rider hasn't already (Details has a button).
            if (_activeRide.value?.phase == RidePhase.ARRIVING) markInTrip()

            // Follow the road route, then finish.
            val pts = _routeSnapshot.value?.points.orEmpty()
            if (pts.size >= 2) {
                val step = (pts.size / 6).coerceAtLeast(1)
                var i = 0
                while (i < pts.size) {
                    delay(2000)
                    if (_activeRide.value?.phase != RidePhase.IN_TRIP) return@launch
                    _driverPin.value = pts[i]
                    i += step
                }
            } else {
                delay(10000)
                if (_activeRide.value?.phase != RidePhase.IN_TRIP) return@launch
            }
            completeRide()
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
        val shared = ride.option.equals("Shared", ignoreCase = true) ||
            ride.option.contains("shar", ignoreCase = true)
        val record = RideHistoryItem(
            route = "${ride.pickup} → ${ride.destination}",
            date = "Just now • ${ride.option}",
            seatsShared = ride.option,
            people = "You + driver",
            price = ride.price,
            isShared = shared
        )
        _history.value = listOf(record) + _history.value
        _lastCompleted.value = record
        _activeRide.value = null
        _driverPin.value = null
    }

    fun cancelRide() {
        _activeRide.value = null
        _driverPin.value = null
    }

    fun consumeCompleted() {
        _lastCompleted.value = null
    }
}
