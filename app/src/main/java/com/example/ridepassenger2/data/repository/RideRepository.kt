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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
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
object RideRepository : PassengerRideRepository()

open class PassengerRideRepository(
    private val apiProvider: () -> com.example.ridepassenger2.data.remote.RidaApi = { RidaApiClient.api },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val newKey: () -> String = { java.util.UUID.randomUUID().toString() },
    private val resolvePoint: suspend (String) -> PointDto? = ::geocodeRidePoint,
    private val now: () -> Instant = Instant::now,
    private val callbackDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Main
) {

    private val workScope = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job]))
    private val rideLock = Mutex()
    private val quoteRevision = AtomicLong()
    private val historyRevision = AtomicLong()
    private val scheduledRevision = AtomicLong()
    private var pendingCreate: Pair<RideCreateRequest, String>? = null
    private val cancelKeys = mutableMapOf<String, String>()
    private val _rideActionPending = MutableStateFlow(false)
    val rideActionPending: StateFlow<Boolean> = _rideActionPending.asStateFlow()

    /** Invalidates work and private state at every logout, including authenticator expiry. */
    @Synchronized fun reset() {
        workScope.coroutineContext.cancelChildren()
        quoteRevision.incrementAndGet()
        historyRevision.incrementAndGet()
        scheduledRevision.incrementAndGet()
        stopPolling()
        activeRideId = null
        pendingCreate = null
        cancelKeys.clear()
        _activeRide.value = null
        _history.value = emptyList()
        _selectedHistory.value = null
        _lastCompleted.value = null
        _driverPin.value = null
        _scheduled.value = emptyList()
        _routeSnapshot.value = null
        _quoteTzs.value = null
        _historyLoading.value = false
        _historyError.value = null
        _rideActionError.value = null
        _rideActionPending.value = false
        _schedulePending.value = false
        pendingSchedule = null
    }

    @Synchronized private fun reserveAction(): Boolean {
        if (_rideActionPending.value) return false
        _rideActionPending.value = true
        return true
    }

    /** Check cancellation and publish under the same monitor as reset. A cancelled
     * task must not publish or attach a new polling child after logout finishes. */
    private suspend fun <T> publish(block: () -> T): T {
        val job = currentCoroutineContext()[Job]
        return synchronized(this) {
            job?.ensureActive()
            block()
        }
    }

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

    @Synchronized fun selectHistory(item: RideHistoryItem?) {
        _selectedHistory.value = item
    }

    @Synchronized fun saveRoute(
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

    @Synchronized fun clearRoute() {
        quoteRevision.incrementAndGet()
        _routeSnapshot.value = null
        _quoteTzs.value = null
    }

    @Synchronized fun refreshQuote(origin: GeoPoint, destination: GeoPoint) {
        val revision = quoteRevision.incrementAndGet()
        _quoteTzs.value = null
        workScope.launch {
            try {
                val resp = apiProvider().getQuote(
                    RideQuoteRequest(
                        pickup = PointDto(origin.latitude, origin.longitude, "Pickup"),
                        dropoff = PointDto(destination.latitude, destination.longitude, _routeSnapshot.value?.destinationLabel ?: "Destination")
                    )
                )
                currentCoroutineContext().ensureActive()
                publish { if (quoteRevision.get() == revision) _quoteTzs.value = resp.estimatedFareTzs }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                publish { if (quoteRevision.get() == revision) _quoteTzs.value = null }
            }
        }
    }

    fun isBusy(): Boolean {
        val phase = _activeRide.value?.phase
        return _rideActionPending.value || phase == RidePhase.SEARCHING || phase == RidePhase.DRIVER_FOUND ||
            phase == RidePhase.ARRIVING || phase == RidePhase.IN_TRIP
    }

    @Synchronized fun refreshHistory() {
        val revision = historyRevision.incrementAndGet()
        workScope.launch {
            if (!publish {
                if (historyRevision.get() != revision) false else {
                    _historyLoading.value = true
                    _historyError.value = null
                    true
                }
            }) return@launch
            try {
                val resp = apiProvider().getRides(limit = 50, offset = 0)
                currentCoroutineContext().ensureActive()
                val items = resp.rides.filter { it.isTerminal() }.map { it.toHistoryItem() }
                publish { if (historyRevision.get() == revision) _history.value = items }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                publish { if (historyRevision.get() == revision) _historyError.value = e.message ?: "Could not load ride history" }
            } finally {
                if (currentCoroutineContext().isActive) publish {
                    if (historyRevision.get() == revision) _historyLoading.value = false
                }
            }
        }
    }

    @Synchronized fun restoreActiveRideIfAny() {
        workScope.launch {
            try {
                rideLock.withLock {
                    val resp = apiProvider().getRides(limit = 20, offset = 0)
                    currentCoroutineContext().ensureActive()
                    val ride = resp.rides.firstOrNull { !it.isTerminal() } ?: return@withLock
                    publish {
                        activeRideId = ride.id
                        lastOptionLabel = ride.vehicleType ?: "Standard"
                        applyRideResponse(ride)
                        startPolling(ride.id)
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { }
        }
    }

    @Synchronized fun openRideDetails(id: String, onLoaded: (fromHistory: Boolean) -> Unit) {
        workScope.launch {
            try {
                val ride = rideLock.withLock {
                    apiProvider().getRide(id).also { response ->
                        currentCoroutineContext().ensureActive()
                        publish {
                            if (response.isTerminal()) _selectedHistory.value = response.toHistoryItem()
                            else {
                                activeRideId = response.id
                                lastOptionLabel = response.vehicleType ?: "Standard"
                                applyRideResponse(response)
                                startPolling(response.id)
                            }
                        }
                    }
                }
                withContext(callbackDispatcher) { publish { onLoaded(ride.isTerminal()) } }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                currentCoroutineContext().ensureActive()
                publish { _rideActionError.value = error.message ?: "Could not open this ride. Retry." }
            }
        }
    }

    @Synchronized fun refreshScheduled() {
        val revision = scheduledRevision.incrementAndGet()
        workScope.launch {
            try {
                val resp = apiProvider().getScheduledRides(status = "upcoming")
                currentCoroutineContext().ensureActive()
                val items = resp.schedules.map { it.toUi() }
                publish { if (scheduledRevision.get() == revision) _scheduled.value = items }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                publish { if (scheduledRevision.get() == revision) _rideActionError.value = "Could not refresh upcoming rides. Retry." }
            }
        }
    }

    @Synchronized fun requestRide(
        pickup: String, destination: String, price: String, option: String,
        onSuccess: () -> Unit = {}
    ) {
        if (isBusy() || !reserveAction()) return
        val snapshot = _routeSnapshot.value
        workScope.launch {
            publish { _rideActionError.value = null }
            try {
                val pickupPoint = snapshot?.let { PointDto(it.origin.latitude, it.origin.longitude, pickup) }
                    ?: requireNotNull(resolvePoint(pickup)) { "Pickup could not be located. Select it on the map." }
                val dropoffPoint = snapshot?.let { PointDto(it.destination.latitude, it.destination.longitude, destination) }
                    ?: requireNotNull(resolvePoint(destination)) { "Destination could not be located. Select it on the map." }
                val req = RideCreateRequest(pickupPoint, dropoffPoint, mapOptionToVehicle(option))
                val key = publish {
                    (pendingCreate?.takeIf { it.first == req }?.second ?: newKey()).also { pendingCreate = req to it }
                }
                rideLock.withLock {
                    val ride = apiProvider().createRide(key, req)
                    currentCoroutineContext().ensureActive()
                    publish {
                        pendingCreate = null
                        lastOptionLabel = option
                        activeRideId = ride.id
                        applyRideResponse(ride)
                        if (!ride.isTerminal()) startPolling(ride.id)
                    }
                    withContext(callbackDispatcher) { publish { onSuccess() } }
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                publish { _rideActionError.value = e.message ?: "Could not request ride" }
            } finally {
                if (currentCoroutineContext().isActive) publish { _rideActionPending.value = false }
            }
        }
    }

    private val _schedulePending = MutableStateFlow(false)
    val schedulePending: StateFlow<Boolean> = _schedulePending.asStateFlow()
    private var pendingSchedule: Pair<ScheduledRideRequest, String>? = null

    @Synchronized fun scheduleRide(
        pickup: String, destination: String, dateLabel: String, timeLabel: String,
        option: String, price: String, onSuccess: () -> Unit = {}
    ) {
        synchronized(this) {
            if (_schedulePending.value) return
            _schedulePending.value = true
        }
        workScope.launch {
            publish { _rideActionError.value = null }
            try {
                val pickupPoint = requireNotNull(resolvePoint(pickup)) { "Pickup could not be located. Enter a specific address." }
                val dropPoint = requireNotNull(resolvePoint(destination)) { "Destination could not be located." }
                val scheduledFor = buildScheduledIso(dateLabel, timeLabel)
                require(Instant.parse(scheduledFor).isAfter(now())) { "Choose a future pickup time." }
                val req = ScheduledRideRequest(pickupPoint, dropPoint, mapOptionToVehicle(option), scheduledFor)
                val key = publish {
                    (pendingSchedule?.takeIf { it.first == req }?.second ?: newKey()).also { pendingSchedule = req to it }
                }
                apiProvider().createScheduledRide(key, req)
                currentCoroutineContext().ensureActive()
                publish {
                    pendingSchedule = null
                    refreshScheduled()
                }
                withContext(callbackDispatcher) { publish { onSuccess() } }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                publish { _rideActionError.value = e.message ?: "Could not schedule ride" }
            } finally {
                if (currentCoroutineContext().isActive) publish { _schedulePending.value = false }
            }
        }
    }

    @Synchronized fun cancelScheduled(id: String) {
        workScope.launch {
            try {
                apiProvider().cancelScheduledRide(id)
                publish { refreshScheduled() }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                publish { _rideActionError.value = e.message ?: "Could not cancel schedule" }
            }
        }
    }

    @Synchronized fun cancelRide(onSuccess: () -> Unit = {}) {
        val id = activeRideId ?: _activeRide.value?.rideId ?: return
        if (_activeRide.value?.phase == RidePhase.IN_TRIP) {
            _rideActionError.value = "An ongoing trip cannot be cancelled. Contact support if you need help."
            return
        }
        if (!reserveAction()) return
        val key = cancelKeys.getOrPut(id) { newKey() }
        workScope.launch {
            publish { _rideActionError.value = null }
            try {
                rideLock.withLock {
                    val ride = apiProvider().cancelRide(id, key, CancelRideRequest())
                    currentCoroutineContext().ensureActive()
                    publish {
                        applyRideResponse(ride)
                        if (ride.isTerminal()) cancelKeys.remove(id)
                        else _rideActionError.value = "Cancellation has not been confirmed. Retry."
                    }
                    if (ride.isTerminal()) withContext(callbackDispatcher) { publish { onSuccess() } }
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                publish { _rideActionError.value = e.message ?: "Could not cancel ride. Retry." }
            } finally {
                if (currentCoroutineContext().isActive) publish { _rideActionPending.value = false }
            }
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

    @Synchronized fun consumeCompleted() {
        _lastCompleted.value = null
    }

    private fun applyRideResponse(ride: RideResponse) {
        val active = ride.toActiveRide(lastOptionLabel)
        if (ride.isTerminal()) {
            if (ride.status.lowercase(Locale.US) in setOf("completed", "finished")) {
                _lastCompleted.value = ride.toHistoryItem()
            }
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
        pollJob = workScope.launch {
            while (true) {
                delay(4000)
                try {
                    rideLock.withLock {
                        if (activeRideId != rideId) return@launch
                        val ride = apiProvider().getRide(rideId)
                        currentCoroutineContext().ensureActive()
                        publish { applyRideResponse(ride) }
                        if (!ride.isTerminal() && ride.driver != null) {
                            val locResp = apiProvider().getDriverLocation(rideId)
                            currentCoroutineContext().ensureActive()
                            publish {
                                if (locResp.isSuccessful && activeRideId == rideId) {
                                    locResp.body()?.let { _driverPin.value = GeoPoint(it.lat, it.lng) }
                                }
                            }
                        }
                    }
                } catch (cancelled: CancellationException) { throw cancelled
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
        val now = now().atZone(zone)
        val day = when (dateLabel.lowercase(Locale.US)) {
            "today" -> now.toLocalDate()
            "tomorrow" -> now.toLocalDate().plusDays(1)
            else -> now.toLocalDate().plusDays(2)
        }
        val time = timeLabel.trim()
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val localTime = java.time.LocalTime.parse(time.uppercase(Locale.US), formatter)
        val zdt = day.atTime(localTime).atZone(zone)
        return zdt.toInstant().toString()
    }
}

private suspend fun geocodeRidePoint(query: String): PointDto? {
        val q = if (query.contains(",")) query else "$query, Dar es Salaam, Tanzania"
        val places = MapServiceFactory.nominatim.search(query = q, limit = 1)
        val p = places.firstOrNull() ?: return null
        return PointDto(p.lat.toDouble(), p.lon.toDouble(), query)
    }
