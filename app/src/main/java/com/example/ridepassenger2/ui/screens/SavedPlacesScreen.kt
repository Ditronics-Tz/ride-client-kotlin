package com.example.ridepassenger2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.local.AppPrefs
import com.example.ridepassenger2.data.remote.MapServiceFactory
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.remote.SavedPlaceDto
import androidx.compose.runtime.LaunchedEffect
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import kotlinx.coroutines.launch

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val IconBg = Color(0xFFE8F7F0)
private val RidaGreen = Color(0xFF00A86B)
private val RidaButtonGreen = Color(0xFF008F64)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val SectionTitleColor = Color(0xFF6B7A88)

data class SavedPlaceItem(
    val id: String,
    val title: String,
    val address: String
)

private suspend fun geocodeLatLng(address: String): Pair<Double, Double> {
    val q = if (address.contains(",")) address else "$address, Dar es Salaam, Tanzania"
    val hit = MapServiceFactory.nominatim.search(query = q, limit = 1).firstOrNull()
    return if (hit != null) Pair(hit.lat.toDouble(), hit.lon.toDouble()) else Pair(-6.7924, 39.2083)
}

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit = {},
    onUsePlace: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val homeAddress by AppPrefs.homeQuery(context).collectAsState(initial = null)
    val workAddress by AppPrefs.workQuery(context).collectAsState(initial = null)

    var editingType by remember { mutableStateOf<String?>(null) } // "home", "work", "new", or custom place ID
    var editTitleDraft by remember { mutableStateOf("") }
    var editAddressDraft by remember { mutableStateOf("") }

    var customPlaces by remember { mutableStateOf<List<SavedPlaceItem>>(emptyList()) }
    var placesLoading by remember { mutableStateOf(true) }
    var homePlaceId by remember { mutableStateOf<String?>(null) }
    var workPlaceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        placesLoading = true
        try {
            val resp = RidaApiClient.api.getSavedPlaces()
            resp.places.firstOrNull { it.kind == "home" }?.let {
                homePlaceId = it.id
                AppPrefs.setHome(context, it.address)
            }
            resp.places.firstOrNull { it.kind == "work" }?.let {
                workPlaceId = it.id
                AppPrefs.setWork(context, it.address)
            }
            customPlaces = resp.places
                .filter { it.kind == "custom" && !it.id.isNullOrBlank() }
                .map { SavedPlaceItem(it.id!!, it.label, it.address) }
        } catch (_: Exception) {
            customPlaces = emptyList()
        } finally {
            placesLoading = false
        }
    }

    fun openEditShortcut(type: String, currentAddress: String?) {
        editingType = type
        editTitleDraft = if (type == "home") "Home" else "Work"
        editAddressDraft = currentAddress ?: ""
    }

    fun openEditCustom(item: SavedPlaceItem) {
        editingType = item.id
        editTitleDraft = item.title
        editAddressDraft = item.address
    }

    fun openAddNew() {
        editingType = "new"
        editTitleDraft = ""
        editAddressDraft = ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(10.dp))
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        RidaIcon(
                            icon = RidaIcons.Back,
                            contentDescription = "Back",
                            tint = InkDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    RidaLogo(green = RidaGreen)
                }

                Spacer(Modifier.height(20.dp))

                // Header
                Text(
                    text = "Saved places",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your favourite destinations, one tap away.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(24.dp))

                // QUICK SHORTCUTS Section
                Text(
                    text = "QUICK SHORTCUTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SectionTitleColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(12.dp))

                // Home Card
                SavedShortcutCard(
                    icon = RidaIcons.Home,
                    title = "Home",
                    subtitle = homeAddress ?: "Choose where you live",
                    onClick = {
                        if (!homeAddress.isNullOrBlank()) {
                            onUsePlace(homeAddress!!)
                        } else {
                            openEditShortcut("home", homeAddress)
                        }
                    },
                    onEditClick = { openEditShortcut("home", homeAddress) }
                )

                Spacer(Modifier.height(10.dp))

                // Work Card
                SavedShortcutCard(
                    icon = RidaIcons.Work,
                    title = "Work",
                    subtitle = workAddress ?: "Save your workplace",
                    onClick = {
                        if (!workAddress.isNullOrBlank()) {
                            onUsePlace(workAddress!!)
                        } else {
                            openEditShortcut("work", workAddress)
                        }
                    },
                    onEditClick = { openEditShortcut("work", workAddress) }
                )

                Spacer(Modifier.height(24.dp))

                // YOUR PLACES Section
                Text(
                    text = "YOUR PLACES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SectionTitleColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(12.dp))

                if (placesLoading && customPlaces.isEmpty()) {
                    Text(text = "Loading saved places…", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(10.dp))
                } else if (!placesLoading && customPlaces.isEmpty()) {
                    Text(text = "No saved places yet.", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(10.dp))
                }
                customPlaces.forEach { place ->
                    CustomPlaceCard(
                        item = place,
                        onClick = { onUsePlace("${place.title}, ${place.address}") },
                        onEditClick = { openEditCustom(place) }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(14.dp))

                // Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RidaIcon(
                        icon = RidaIcons.Map,
                        contentDescription = null,
                        tint = RidaGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Your trips, made easier",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Save your most visited places to book faster and avoid typing addresses.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF047857),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Bottom Add Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaButtonGreen.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(RidaButtonGreen)
                        .clickable(onClick = ::openAddNew)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add new place",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    RidaIcon(
                        icon = RidaIcons.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }

    // Edit/Add Screen overlay
    if (editingType != null) {
        AddAddressScreen(
            initialLabel = editTitleDraft,
            initialAddress = editAddressDraft,
            onBack = { editingType = null },
            onSave = { label, address ->
                scope.launch {
                    try {
                        val (lat, lng) = geocodeLatLng(address)
                        when (editingType) {
                            "home" -> {
                                AppPrefs.setHome(context, address)
                                val dto = SavedPlaceDto(
                                    id = homePlaceId,
                                    kind = "home",
                                    label = label.ifBlank { "Home" },
                                    address = address,
                                    lat = lat,
                                    lng = lng
                                )
                                if (homePlaceId != null) {
                                    RidaApiClient.api.updateSavedPlace(homePlaceId!!, dto)
                                } else {
                                    val created = RidaApiClient.api.createSavedPlace(dto)
                                    homePlaceId = created.id
                                }
                            }
                            "work" -> {
                                AppPrefs.setWork(context, address)
                                val dto = SavedPlaceDto(
                                    id = workPlaceId,
                                    kind = "work",
                                    label = label.ifBlank { "Work" },
                                    address = address,
                                    lat = lat,
                                    lng = lng
                                )
                                if (workPlaceId != null) {
                                    RidaApiClient.api.updateSavedPlace(workPlaceId!!, dto)
                                } else {
                                    val created = RidaApiClient.api.createSavedPlace(dto)
                                    workPlaceId = created.id
                                }
                            }
                            "new" -> {
                                RidaApiClient.api.createSavedPlace(
                                    SavedPlaceDto(
                                        kind = "custom",
                                        label = label,
                                        address = address,
                                        lat = lat,
                                        lng = lng
                                    )
                                )
                            }
                            else -> {
                                val id = editingType
                                if (id != null) {
                                    RidaApiClient.api.updateSavedPlace(
                                        id,
                                        SavedPlaceDto(
                                            id = id,
                                            kind = "custom",
                                            label = label,
                                            address = address,
                                            lat = lat,
                                            lng = lng
                                        )
                                    )
                                }
                            }
                        }
                        try {
                            val resp = RidaApiClient.api.getSavedPlaces()
                            customPlaces = resp.places
                                .filter { it.kind == "custom" && !it.id.isNullOrBlank() }
                                .map { SavedPlaceItem(it.id!!, it.label, it.address) }
                        } catch (_: Exception) { }
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Could not save place", Toast.LENGTH_LONG).show()
                    }
                    editingType = null
                }
            }
        )
    }
}

@Composable
private fun SavedShortcutCard(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(IconBg),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = icon,
                contentDescription = title,
                tint = RidaGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
        RidaIcon(
            icon = RidaIcons.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF98A6A0),
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onEditClick)
        )
    }
}

@Composable
private fun CustomPlaceCard(
    item: SavedPlaceItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(IconBg),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = RidaIcons.SavedPlace,
                contentDescription = item.title,
                tint = RidaGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.address,
                fontSize = 13.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onEditClick)
                .padding(4.dp)
        ) {
            RidaIcon(
                icon = RidaIcons.Edit,
                contentDescription = "Edit place",
                tint = Color(0xFF98A6A0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SavedPlacesScreenPreview() {
    RIDEPASSENGER2Theme {
        SavedPlacesScreen()
    }
}
