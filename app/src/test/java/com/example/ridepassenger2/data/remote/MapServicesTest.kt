package com.example.ridepassenger2.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression test: Nominatim + OSRM responses must parse into our models.
 * (Without the Kotlin Moshi adapter every call threw and the UI silently
 * showed nothing.)
 */
class MapServicesTest {

    @Test
    fun nominatimFindsKariakoo() = runBlocking {
        val results = MapServiceFactory.nominatim.search(
            query = "Kariakoo, Dar es Salaam",
            limit = 6
        )
        assertTrue("expected Dar es Salaam results", results.isNotEmpty())
        assertTrue(results.any { it.displayName.contains("Dar es Salaam") })
    }

    @Test
    fun osrmRoutesKariakooToMwenge() = runBlocking {
        val resp = MapServiceFactory.osrm.route(
            coords = osrmCoords(39.269, -6.823, 39.22, -6.768)
        )
        assertEquals("Ok", resp.code)
        assertTrue("expected at least one route", resp.routes.isNotEmpty())
        val route = resp.routes.first()
        assertTrue("expected road geometry", route.geometry.coordinates.size >= 2)
    }
}
