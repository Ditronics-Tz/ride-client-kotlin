package com.example.ridepassenger2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BarGreen = Color(0xFF008F64)
private val BarInactive = Color(0xFF667085)
private val BarShape = RoundedCornerShape(40.dp)

/**
 * Floating glass pill. Active tab is brand green (#008F64).
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 12.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, BarShape, ambientColor = Color(0x330D2921), spotColor = Color(0x330D2921))
                .clip(BarShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.96f),
                            Color(0xFFF5FAF8).copy(alpha = 0.90f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White, Color(0xFFB9D7CC).copy(alpha = 0.65f))
                    ),
                    shape = BarShape
                ),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
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
}
