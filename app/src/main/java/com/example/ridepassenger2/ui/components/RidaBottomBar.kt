package com.example.ridepassenger2.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val BarBg = Color.White
private val BarGreen = Color(0xFF008F64)
private val BarInactive = Color(0xFF667085)

/**
 * Bottom navigation bar matching the app theme. Active tab is brand green (#008F64).
 * Tabs: Home | Activity | Profile
 */
@Composable
fun RidaBottomBar(
    selected: String,
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val norm = when (selected) {
        "History" -> "Activity"
        else -> selected
    }

    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BarGreen,
        selectedTextColor = BarGreen,
        indicatorColor = BarGreen.copy(alpha = 0.12f),
        unselectedIconColor = BarInactive,
        unselectedTextColor = BarInactive
    )

    NavigationBar(
        modifier = modifier,
        containerColor = BarBg
    ) {
        NavigationBarItem(
            selected = norm == "Home",
            onClick = onHome,
            label = { Text("Home") },
            icon = {
                RidaIcon(
                    icon = RidaIcons.Home,
                    contentDescription = "Home"
                )
            },
            colors = colors
        )
        NavigationBarItem(
            selected = norm == "Activity",
            onClick = onActivity,
            label = { Text("Activity") },
            icon = {
                RidaIcon(
                    icon = RidaIcons.History,
                    contentDescription = "Activity"
                )
            },
            colors = colors
        )
        NavigationBarItem(
            selected = norm == "Profile",
            onClick = onProfile,
            label = { Text("Profile") },
            icon = {
                RidaIcon(
                    icon = RidaIcons.Account,
                    contentDescription = "Profile"
                )
            },
            colors = colors
        )
    }
}
