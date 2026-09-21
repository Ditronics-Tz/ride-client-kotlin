package com.example.ridepassenger2.ui.map

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Compose wrapper around osmdroid MapView.
 * - Supports Google tiles (lyrs=m / y) + OSM fallback
 * - Draws current location, destination marker, and OSRM route polyline
 * - Lightweight: no fragment, direct AndroidView lifecycle
 */
@Composable
fun RidaMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(-6.7924, 39.2083), // Dar es Salaam fallback
    zoom: Double = 13.5,
    useGoogleTiles: Boolean = true,
    useSatellite: Boolean = false,
    currentLocation: GeoPoint? = null,
    destination: GeoPoint? = null,
    routePoints: List<GeoPoint> = emptyList(),
    driverLocations: List<GeoPoint> = emptyList(),
    onMapReady: ((MapView) -> Unit)? = null,
    enableMyLocation: Boolean = false
) {
    val context = LocalContext.current

    // osmdroid needs a user-agent; set once
    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(
                when {
                    useSatellite -> GoogleTileSources.GoogleSatellite
                    useGoogleTiles -> GoogleTileSources.GoogleRoad
                    else -> TileSourceFactory.MAPNIK
                }
            )
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(center)
            // crisp tiles on high-dpi
            isTilesScaledToDpi = true
        }
    }

    // Reflect tile-source toggles without recreating view
    LaunchedEffect(useGoogleTiles, useSatellite) {
        mapView.setTileSource(
            when {
                useSatellite -> GoogleTileSources.GoogleSatellite
                useGoogleTiles -> GoogleTileSources.GoogleRoad
                else -> TileSourceFactory.MAPNIK
            }
        )
        mapView.invalidate()
    }

    // Keep center/zoom in sync
    LaunchedEffect(center, zoom) {
        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(center)
    }

    // Overlays: location + route + markers — rebuilt on each param change
    LaunchedEffect(currentLocation, destination, routePoints, driverLocations, enableMyLocation) {
        mapView.overlays.clear()

        // MyLocation overlay (blue dot + accuracy ring) when enabled and permission granted
        if (enableMyLocation) {
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                enableMyLocation()
                enableFollowLocation()
            }
            mapView.overlays.add(myLocationOverlay)
        }

        // Route polyline (OSRM)
        if (routePoints.size >= 2) {
            val polyline = Polyline().apply {
                setPoints(routePoints)
                outlinePaint.apply {
                    color = AndroidColor.parseColor("#0D5E3A")
                    strokeWidth = 10f
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                }
                // white halo underneath for contrast on both light/dark tiles
            }
            // halo polyline (slightly wider, white, drawn first)
            val halo = Polyline().apply {
                setPoints(routePoints)
                outlinePaint.apply {
                    color = AndroidColor.WHITE
                    strokeWidth = 16f
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                    alpha = 200
                }
            }
            mapView.overlays.add(halo)
            mapView.overlays.add(polyline)
        }

        // Destination marker
        destination?.let { dest ->
            val marker = Marker(mapView).apply {
                position = dest
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Destination"
                // use built-in icon; could swap for custom Rida pin
            }
            mapView.overlays.add(marker)
        }

        // Current location custom marker (when MyLocation overlay not used, or as extra)
        if (currentLocation != null && !enableMyLocation) {
            val me = Marker(mapView).apply {
                position = currentLocation
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "You"
            }
            mapView.overlays.add(me)
        }

        // Driver pins
        driverLocations.forEach { d ->
            val pin = Marker(mapView).apply {
                position = d
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Driver"
            }
            mapView.overlays.add(pin)
        }

        mapView.invalidate()
        onMapReady?.invoke(mapView)
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { /* handled by LaunchedEffects */ }
    )

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }
}

// Helper to move map smoothly
fun MapView.animateTo(geoPoint: GeoPoint, zoom: Double = 15.0) {
    controller.animateTo(geoPoint)
    controller.setZoom(zoom)
    invalidate()
}
