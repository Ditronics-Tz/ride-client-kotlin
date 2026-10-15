package com.example.ridepassenger2.data.repository

import org.osmdroid.util.GeoPoint

enum class RidePhase { IDLE, SEARCHING, DRIVER_FOUND, ARRIVING, IN_TRIP }

data class RideDriverInfo(
    val name: String,
    val rating: Double,
    val car: String,
    val plate: String,
    val phone: String? = null
)

data class ActiveRide(
    val rideId: String,
    val phase: RidePhase,
    val driver: RideDriverInfo?,
    val pickup: String,
    val destination: String,
    val price: String,
    val option: String,
    val etaMin: Int
)

data class ScheduledRideUi(
    val id: String,
    val pickup: String,
    val destination: String,
    val dateLabel: String,
    val timeLabel: String,
    val option: String,
    val price: String
)

data class RouteSnapshot(
    val origin: GeoPoint,
    val destination: GeoPoint,
    val destinationLabel: String,
    val points: List<GeoPoint> = emptyList(),
    val distanceM: Double? = null,
    val durationS: Double? = null
)
