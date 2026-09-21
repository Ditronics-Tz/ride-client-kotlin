package com.example.ridepassenger2.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Default bottom bar — plain Material3, no custom designs.
 * Tabs: Home | Activity | Ride (center action) | Profile
 */
@Composable
fun RidaBottomBar(
    selected: String,
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onCenter: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val norm = when (selected) {
        "History" -> "Activity"
        else -> selected
    }

    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = norm == "Home",
            onClick = onHome,
            label = { Text("Home") },
            icon = {
                Icon(
                    imageVector = if (norm == "Home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            }
        )
        NavigationBarItem(
            selected = norm == "Activity",
            onClick = onActivity,
            label = { Text("Activity") },
            icon = {
                Icon(
                    imageVector = if (norm == "Activity") Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "Activity"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onCenter,
            label = { Text("Ride") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = "Request a ride"
                )
            }
        )
        NavigationBarItem(
            selected = norm == "Profile",
            onClick = onProfile,
            label = { Text("Profile") },
            icon = {
                Icon(
                    imageVector = if (norm == "Profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            }
        )
    }
}
