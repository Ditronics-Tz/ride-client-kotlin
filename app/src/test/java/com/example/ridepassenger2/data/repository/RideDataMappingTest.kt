package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.DriverInfoDto
import com.example.ridepassenger2.data.remote.PointDto
import com.example.ridepassenger2.data.remote.RideResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RideDataMappingTest {
    @Test
    fun rideWithoutArrivalDataHasNoInventedEta() {
        val response = RideResponse(
            id = "ride-1",
            status = "assigned",
            pickup = PointDto(-6.78, 39.20, "Pickup"),
            dropoff = PointDto(-6.80, 39.25, "Drop-off")
        )

        assertNull(response.toActiveRide("Standard").etaMin)
    }

    @Test
    fun missingDriverRatingStaysAbsent() {
        val driver = DriverInfoDto(id = "driver-1", name = "Driver")

        assertNull(driverFromDto(driver).rating)
    }

    @Test
    fun savedDriverRatingIsPreserved() {
        val driver = DriverInfoDto(id = "driver-1", name = "Driver", rating = 4.6)

        assertEquals(4.6, driverFromDto(driver).rating)
    }
}
