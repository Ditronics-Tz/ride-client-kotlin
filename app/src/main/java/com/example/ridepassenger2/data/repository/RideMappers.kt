package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.DriverInfoDto
import com.example.ridepassenger2.data.remote.RideResponse
import com.example.ridepassenger2.data.remote.ScheduledRideResponse
import com.example.ridepassenger2.ui.screens.RideHistoryItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val darZone = ZoneId.of("Africa/Dar_es_Salaam")

fun RideResponse.isTerminal(): Boolean {
    val s = status.lowercase(Locale.US)
    return s in setOf("completed", "cancelled", "canceled", "failed", "expired")
}

fun mapStatusToPhase(status: String): RidePhase {
    val s = status.lowercase(Locale.US)
    return when {
        s in setOf("pending", "requested", "searching", "matching", "created", "queued") -> RidePhase.SEARCHING
        s in setOf("accepted", "assigned", "matched", "driver_assigned", "driver_found") -> RidePhase.DRIVER_FOUND
        s in setOf("arriving", "driver_arriving", "at_pickup", "pickup") -> RidePhase.ARRIVING
        s in setOf("in_progress", "ongoing", "started", "in_trip", "en_route") -> RidePhase.IN_TRIP
        s in setOf("completed", "finished") -> RidePhase.IDLE
        else -> RidePhase.SEARCHING
    }
}

fun formatFareTzs(amount: Long?): String =
    amount?.let { "TZS ${"%,d".format(it)}" } ?: "—"

fun formatRideTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(darZone)
        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.US)
        fmt.format(zdt)
    } catch (_: Exception) {
        iso
    }
}

fun isSharedVehicle(vehicleType: String?): Boolean {
    val vt = vehicleType?.lowercase(Locale.US) ?: return false
    return vt.contains("share") || vt.contains("pool")
}

fun RideResponse.toHistoryItem(): RideHistoryItem {
    val route = "${pickup.address} → ${dropoff.address}"
    val fare = finalFare ?: estimatedFare
    val shared = isSharedVehicle(vehicleType)
    val driverName = driver?.name
    return RideHistoryItem(
        rideId = id,
        route = route,
        date = formatRideTimestamp(createdAt),
        seatsShared = vehicleType?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            ?: "Standard",
        people = driverName?.let { "Driver: $it" } ?: "—",
        price = formatFareTzs(fare),
        isShared = shared,
        rideStatus = status.lowercase(Locale.US)
    )
}

fun driverFromDto(d: DriverInfoDto): RideDriverInfo {
    val car = buildString {
        d.vehicleType?.let { append(it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.US) else c.toString() }) }
    }.ifBlank { "Vehicle" }
    return RideDriverInfo(
        name = d.name,
        rating = d.rating ?: 0.0,
        car = car,
        plate = d.plateNumber ?: "—",
        phone = d.phone
    )
}

fun RideResponse.toActiveRide(etaMin: Int, optionLabel: String): ActiveRide {
    val fare = finalFare ?: estimatedFare
    return ActiveRide(
        rideId = id,
        phase = mapStatusToPhase(status),
        driver = driver?.let { driverFromDto(it) },
        pickup = pickup.address,
        destination = dropoff.address,
        price = formatFareTzs(fare),
        option = optionLabel.ifBlank { vehicleType ?: "Standard" },
        etaMin = etaMin
    )
}

fun ScheduledRideResponse.toUi(): ScheduledRideUi {
    val scheduled = formatRideTimestamp(scheduledFor)
    return ScheduledRideUi(
        id = id,
        pickup = pickup.address,
        destination = dropoff.address,
        dateLabel = scheduled.substringBefore(",").ifBlank { scheduled },
        timeLabel = scheduled.substringAfter(",", scheduled).trim().ifBlank { "—" },
        option = vehicleType?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            ?: "Standard",
        price = "—"
    )
}

fun mapTicketStatus(raw: String): com.example.ridepassenger2.ui.support.TicketStatus {
    return when (raw.lowercase(Locale.US)) {
        "open", "new" -> com.example.ridepassenger2.ui.support.TicketStatus.OPEN
        "in_review", "in_review", "reviewing" -> com.example.ridepassenger2.ui.support.TicketStatus.IN_REVIEW
        "waiting_for_you", "waiting", "needs_info" -> com.example.ridepassenger2.ui.support.TicketStatus.WAITING_FOR_YOU
        "resolved", "done" -> com.example.ridepassenger2.ui.support.TicketStatus.RESOLVED
        "closed" -> com.example.ridepassenger2.ui.support.TicketStatus.CLOSED
        else -> com.example.ridepassenger2.ui.support.TicketStatus.OPEN
    }
}

fun mapTicketTopic(category: String): com.example.ridepassenger2.ui.support.Topic {
    return when (category.lowercase(Locale.US)) {
        "payment", "payments" -> com.example.ridepassenger2.ui.support.Topic.PAYMENTS
        "safety" -> com.example.ridepassenger2.ui.support.Topic.SAFETY
        "complaint", "complaints" -> com.example.ridepassenger2.ui.support.Topic.COMPLAINTS
        "account" -> com.example.ridepassenger2.ui.support.Topic.ACCOUNT
        else -> com.example.ridepassenger2.ui.support.Topic.OTHER
    }
}
