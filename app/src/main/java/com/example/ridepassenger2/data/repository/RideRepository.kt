package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.CancelRideRequest
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.data.remote.PointDto
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.remote.RideCreateRequest
import com.example.ridepassenger2.data.remote.RideQuoteRequest
import com.example.ridepassenger2.data.remote.RideResponse
import com.example.ridepassenger2.data.remote.ScheduledRideRequest
import com.example.ridepassenger2.ui.screens.RideHistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Passenger ride state backed by Rida API. Route geometry still comes from OSRM on-device.
 */
object RideRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollJob: Job? = null
    private var activeRideId: String? = null
    private var lastOptionLabel: String = "Standard"

    private val _activeRide = MutableStateFlow<ActiveRide?>(null)
    val activeRide: StateFlow<ActiveRide?> = _activeRide.asStateFlow()

    private val _history = MutableStateFlow<List<RideHistoryItem>>(emptyList())
    val history: StateFlow<List<RideHistoryItem>> = _history.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading.asStateFlow()

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError.asStateFlow()

    private val _lastCompleted = MutableStateFlow<RideHistoryItem?>(null)
    val lastCompleted: StateFlow<RideHistoryItem?> = _lastCompleted.asStateFlow()

    private val _selectedHistory = MutableStateFlow<RideHistoryItem?>(null)
    val selectedHistory: StateFlow<RideHistoryItem?> = _selectedHistory.asStateFlow()

    private val _routeSnapshot = MutableStateFlow<RouteSnapshot?>(null)
    val routeSnapshot: StateFlow<RouteSnapshot?> = _routeSnapshot.asStateFlow()

    private val _driverPin = MutableStateFlow<GeoPoint?>(null)
    val driverPin: StateFlow<GeoPoint?> = _driverPin.asStateFlow()

    private val _scheduled = MutableStateFlow<List<ScheduledRideUi>>(emptyList())
    val scheduled: StateFlow<List<ScheduledRideUi>> = _scheduled.asStateFlow()

    private val _quoteTzs = MutableStateFlow<Long?>(null)
    val quoteTzs: StateFlow<Long?> = _quoteTzs.asStateFlow()

    private val _rideActionError = MutableStateFlow<String?>(null)
    val rideActionError: StateFlow<String?> = _rideActionError.asStateFlow()

    fun selectHistory(item: RideHistoryItem?) {
        _selectedHistory.value = item
    }

    fun saveRoute(
        origin: GeoPoint,
        destination: GeoPoint,
        destinationLabel: String,
        points: List<GeoPoint> = emptyList(),
        distanceM: Double? = null,
        durationS: Double? = null
    ) {
        _routeSnapshot.value = RouteSnapshot(origin, destination, destinationLabel, points, distanceM, durationS)
        refreshQuote(origin, destination)
    }

    fun clearRoute() {
        _routeSnapshot.value = null
        _quoteTzs.value = null
    }

    fun refreshQuote(origin: GeoPoint, destination: GeoPoint) {
        scope.launch {
            try {
                val resp = RidaApiClient.api.getQuote(
                    RideQuoteRequest(
                        pickup = PointDto(origin.latitude, origin.longitude, "Pickup"),
                        dropoff = PointDto(destination.latitude, destination.longitude, _routeSnapshot.value?.destinationLabel ?: "Destination")
                    )
                )
                _quoteTzs.value = resp.estimatedFareTzs
            } catch (_: Exception) {
                _quoteTzs.value = null
            }
        }
    }

    fun isBusy(): Boolean {
        val phase = _activeRide.value?.phase
        return phase == RidePhase.SEARCHING || phase == RidePhase.DRIVER_FOUND ||
            phase == RidePhase.ARRIVING || phase == RidePhase.IN_TRIP
    }

    fun refreshHistory() {
        scope.launch {
            _historyLoading.value = true
            _historyError.value = null
            try {
                val resp = RidaApiClient.api.getRides(limit = 50, offset = 0)
                _history.value = resp.rides
                    .filter { it.isTerminal() }
                    .map { it.toHistoryItem() }
            } catch (e: Exception) {
                _historyError.value = e.message ?: "Could not load ride history"
            } finally {
                _historyLoading.value = false
            }
        }
    }

    fun restoreActiveRideIfAny() {
        scope.launch {
            try {
                val resp = RidaApiClient.api.getRides(limit = 20, offset = 0)
                val ride = resp.rides.firstOrNull { !it.isTerminal() } ?: return@launch
                activeRideId = ride.id
                lastOptionLabel = ride.vehicleType ?: "Standard"
                val eta = (_routeSnapshot.value?.durationS?.div(60)?.toInt() ?: 10).coerceAtLeast(1)
                applyRideResponse(ride, eta)
                startPolling(ride.id)
            } catch (_: Exception) { }
        }
    }

    fun refreshScheduled() {
        scope.launch {
            try {
                val resp = RidaApiClient.api.getScheduledRides(status = "upcoming")
                _scheduled.value = resp.schedules.map { it.toUi() }
            } catch (_: Exception) {
                _scheduled.value = emptyList()
            }
        }
    }

    fun requestRide(pickup: String, destination: String, price: String, option: String, etaMin: Int) {
        if (isBusy()) return
        val snap = _routeSnapshot.value
        if (snap == null) {
            _rideActionError.value = "Route not ready. Select a destination on the map first."
            return
        }
        lastOptionLabel = option
        scope.launch {
            _rideActionError.value = null
            try {
                val vehicleType = mapOptionToVehicle(option)
                val req = RideCreateRequest(
                    pickup = PointDto(snap.origin.latitude, snap.origin.longitude, pickup.ifBlank { "Pickup" }),
                    dropoff = PointDto(snap.destination.latitude, snap.destination.longitude, destination.ifBlank { snap.destinationLabel }),
                    vehicleType = vehicleType
                )
                val ride = RidaApiClient.api.createRide(RidaApiClient.generateIdempotencyKey(), req)
                activeRideId = ride.id
                applyRideResponse(ride, etaMin)
                startPolling(ride.id)
            } catch (e: Exception) {
                _rideActionError.value = e.message ?: "Could not request ride"
            }
        }
    }

    fun scheduleRide(
        pickup: String,
        destination: String,
        dateLabel: String,
        timeLabel: String,
        option: String,
        price: String
    ) {
        scope.launch {
            _rideActionError.value = null
            try {
                val pickupPoint = geocodePoint(pickup.ifBlank { "Dar es Salaam" }) ?: return@launch
                val dropPoint = geocodePoint(destination) ?: return@launch
                val scheduledFor = buildScheduledIso(dateLabel, timeLabel)
                val req = ScheduledRideRequest(
                    pickup = pickupPoint,
                    dropoff = dropPoint,
                    vehicleType = mapOptionToVehicle(option),
                    scheduledFor = scheduledFor
                )
                RidaApiClient.api.createScheduledRide(RidaApiClient.generateIdempotencyKey(), req)
                refreshScheduled()
            } catch (e: Exception) {
                _rideActionError.value = e.message ?: "Could not schedule ride"
            }
        }
    }

    fun cancelScheduled(id: String) {
        scope.launch {
            try {
                RidaApiClient.api.cancelScheduledRide(id)
                refreshScheduled()
            } catch (e: Exception) {
                _rideActionError.value = e.message ?: "Could not cancel schedule"
            }
        }
    }

    fun cancelRide() {
        val id = activeRideId ?: _activeRide.value?.rideId
        scope.launch {
            if (id != null) {
                try {
                    RidaApiClient.api.cancelRide(
                        id,
                        RidaApiClient.generateIdempotencyKey(),
                        CancelRideRequest()
                    )
                } catch (_: Exception) { }
            }
            stopPolling()
            _activeRide.value = null
            _driverPin.value = null
            activeRideId = null
            refreshHistory()
        }
    }

    fun completeRide() {
        // Trip completion is server-driven; refresh when polling detects terminal state.
        refreshHistory()
    }

    fun markInTrip() {
        // No passenger endpoint to force in-trip; state comes from backend polling.
    }

    fun markArriving() {
        // No-op — backend drives phases.
    }

    fun consumeCompleted() {
        _lastCompleted.value = null
    }

    private fun applyRideResponse(ride: RideResponse, etaMin: Int) {
        val active = ride.toActiveRide(etaMin, lastOptionLabel)
        if (ride.status.lowercase(Locale.US) in setOf("completed", "finished")) {
            val item = ride.toHistoryItem()
            _lastCompleted.value = item
            _activeRide.value = null
            _driverPin.value = null
            activeRideId = null
            stopPolling()
            refreshHistory()
        } else {
            _activeRide.value = active
        }
    }

    private fun startPolling(rideId: String) {
        stopPolling()
        pollJob = scope.launch {
            while (true) {
                delay(4000)
                try {
                    val ride = RidaApiClient.api.getRide(rideId)
                    val eta = (_routeSnapshot.value?.durationS?.div(60)?.toInt() ?: 10).coerceAtLeast(1)
                    if (ride.isTerminal()) {
                        if (ride.status.lowercase(Locale.US) in setOf("completed", "finished")) {
                            _lastCompleted.value = ride.toHistoryItem()
                        }
                        _activeRide.value = null
                        _driverPin.value = null
                        activeRideId = null
                        stopPolling()
                        refreshHistory()
                        break
                    } else {
                        applyRideResponse(ride, eta)
                    }
                    if (ride.driver != null) {
                        val locResp = RidaApiClient.api.getDriverLocation(rideId)
                        if (locResp.isSuccessful) {
                            locResp.body()?.let { loc ->
                                _driverPin.value = GeoPoint(loc.lat, loc.lng)
                            }
                        }
                    }
                } catch (_: Exception) {
                    delay(2000)
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private suspend fun geocodePoint(query: String): PointDto? {
        val q = if (query.contains(",")) query else "$query, Dar es Salaam, Tanzania"
        val places = MapServiceFactory.nominatim.search(query = q, limit = 1)
        val p = places.firstOrNull() ?: return null
        return PointDto(p.lat.toDouble(), p.lon.toDouble(), query)
    }

    private fun mapOptionToVehicle(option: String): String {
        val o = option.lowercase(Locale.US)
        return when {
            o.contains("xl") || o.contains("6 seat") -> "van"
            o.contains("comfort") -> "comfort"
            o.contains("package") -> "car"
            o.contains("share") -> "shared"
            else -> "car"
        }
    }

    private fun buildScheduledIso(dateLabel: String, timeLabel: String): String {
        val zone = ZoneId.of("Africa/Dar_es_Salaam")
        val now = Instant.now().atZone(zone)
        val day = when (dateLabel.lowercase(Locale.US)) {
            "today" -> now.toLocalDate()
            "tomorrow" -> now.toLocalDate().plusDays(1)
            else -> now.toLocalDate().plusDays(2)
        }
        val time = timeLabel.trim()
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val localTime = try {
            java.time.LocalTime.parse(time.uppercase(Locale.US), formatter)
        } catch (_: Exception) {
            java.time.LocalTime.of(10, 0)
        }
        val zdt = day.atTime(localTime).atZone(zone)
        return zdt.toInstant().toString()
    }
}
