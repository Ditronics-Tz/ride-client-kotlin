package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.PointDto
import com.example.ridepassenger2.data.remote.RidaApi
import com.example.ridepassenger2.data.remote.SavedPlaceDto

/** Resolves a real address and commits a shortcut cache only after server acknowledgement. */
internal class SavedPlaceWriter(
    private val api: RidaApi,
    private val resolve: suspend (String) -> PointDto?,
    private val cacheShortcut: suspend (String, String) -> Unit
) {
    suspend fun save(kind: String, id: String?, label: String, address: String): SavedPlaceDto {
        require(address.isNotBlank()) { "Enter an address" }
        val point = requireNotNull(resolve(address)) { "Address not found. Enter a more specific location." }
        val input = SavedPlaceDto(id = id, kind = kind, label = label, address = address,
            lat = point.lat, lng = point.lng)
        val saved = if (id == null) api.createSavedPlace(input) else api.updateSavedPlace(id, input)
        if (kind in setOf("home", "work")) cacheShortcut(kind, saved.address)
        return saved
    }
}
