package com.example.ridepassenger2.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType

/**
 * Regression test: Nominatim + OSRM responses must parse into our models.
 * (Without the Kotlin Moshi adapter every call threw and the UI silently
 * showed nothing.)
 */
class MapServicesTest {
    // The normal suite exercises the production Retrofit/Moshi parser deterministically.
    // Set RIDA_LIVE_MAP_TESTS=1 to also exercise the real public services.
    private fun <T> service(type: Class<T>, json: String, live: T): T {
        if (System.getenv("RIDA_LIVE_MAP_TESTS") == "1") return live
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(json.toResponseBody("application/json".toMediaType())).build()
        }.build()
        return MapServiceFactory.service("https://map.test/", type, client)
    }


    @Test
    fun nominatimFindsKariakoo() = runBlocking {
        val results = service(NominatimApi::class.java,
            """[{"place_id":1,"display_name":"Kariakoo, Dar es Salaam","lat":"-6.823","lon":"39.269"}]""",
            MapServiceFactory.nominatim).search(
            query = "Kariakoo, Dar es Salaam",
            limit = 6
        )
        assertTrue("expected Dar es Salaam results", results.isNotEmpty())
        assertTrue(results.any { it.displayName.contains("Dar es Salaam") })
    }

    @Test
    fun osrmRoutesKariakooToMwenge() = runBlocking {
        val resp = service(OsrmApi::class.java,
            """{"code":"Ok","routes":[{"distance":100.0,"duration":60.0,"geometry":{"coordinates":[[39.269,-6.823],[39.22,-6.768]]}}]}""",
            MapServiceFactory.osrm).route(
            coords = osrmCoords(39.269, -6.823, 39.22, -6.768)
        )
        assertEquals("Ok", resp.code)
        assertTrue("expected at least one route", resp.routes.isNotEmpty())
        val route = resp.routes.first()
        assertTrue("expected road geometry", route.geometry.coordinates.size >= 2)
    }
}
