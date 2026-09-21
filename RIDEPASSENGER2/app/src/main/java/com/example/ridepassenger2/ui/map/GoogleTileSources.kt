package com.example.ridepassenger2.ui.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

/**
 * Google tile providers — same URLs from the Flutter proposal, now for Android osmdroid.
 *   Road Map:         https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}
 *   Satellite+Roads:  https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}
 *
 * Note: these endpoints are not an official public API. They work for MVP / university
 * projects but may be restricted for production. Fallback is OSM tiles.
 */
object GoogleTileSources {

    // Road map (lyrs=m)
    val GoogleRoad = object : OnlineTileSourceBase(
        "GoogleRoad", 0, 20, 256, "",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=m&x=",
            "https://mt1.google.com/vt/lyrs=m&x=",
            "https://mt2.google.com/vt/lyrs=m&x=",
            "https://mt3.google.com/vt/lyrs=m&x="
        )
    ) {
        override fun getTileURLString(pTileIndex: Long): String {
            return baseUrl + MapTileIndex.getX(pTileIndex) + "&y=" + MapTileIndex.getY(pTileIndex) + "&z=" + MapTileIndex.getZoom(pTileIndex)
        }
    }

    // Satellite + roads overlay (lyrs=y = satellite with labels)
    val GoogleSatellite = object : OnlineTileSourceBase(
        "GoogleSat", 0, 20, 256, "",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=y&x=",
            "https://mt1.google.com/vt/lyrs=y&x=",
            "https://mt2.google.com/vt/lyrs=y&x=",
            "https://mt3.google.com/vt/lyrs=y&x="
        )
    ) {
        override fun getTileURLString(pTileIndex: Long): String {
            return baseUrl + MapTileIndex.getX(pTileIndex) + "&y=" + MapTileIndex.getY(pTileIndex) + "&z=" + MapTileIndex.getZoom(pTileIndex)
        }
    }

    // Pure satellite (lyrs=s) — optional
    val GoogleSatOnly = object : OnlineTileSourceBase(
        "GoogleSatOnly", 0, 20, 256, "",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=s&x=",
            "https://mt1.google.com/vt/lyrs=s&x=",
            "https://mt2.google.com/vt/lyrs=s&x=",
            "https://mt3.google.com/vt/lyrs=s&x="
        )
    ) {
        override fun getTileURLString(pTileIndex: Long): String {
            return baseUrl + MapTileIndex.getX(pTileIndex) + "&y=" + MapTileIndex.getY(pTileIndex) + "&z=" + MapTileIndex.getZoom(pTileIndex)
        }
    }

    // Road map at night — same Google URLs, transformed on-device by NightTiles.
    // Cached under its own name so night + day tiles never mix.
    val GoogleRoadNight = object : OnlineTileSourceBase(
        "GoogleRoadNight", 0, 20, 256, "",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=m&x=",
            "https://mt1.google.com/vt/lyrs=m&x=",
            "https://mt2.google.com/vt/lyrs=m&x=",
            "https://mt3.google.com/vt/lyrs=m&x="
        )
    ) {
        override fun getTileURLString(pTileIndex: Long): String {
            return baseUrl + MapTileIndex.getX(pTileIndex) + "&y=" + MapTileIndex.getY(pTileIndex) + "&z=" + MapTileIndex.getZoom(pTileIndex)
        }
    }
}
