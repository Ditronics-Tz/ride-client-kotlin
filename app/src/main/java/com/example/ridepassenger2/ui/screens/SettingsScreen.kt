package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.local.AppPrefs
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RidaDarkTheme
import com.example.ridepassenger2.ui.theme.RidaLightTheme
import kotlinx.coroutines.launch

private val SettingsGreen = Color(0xFF008F64)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onLogOut: () -> Unit = {},
    onPersonal: () -> Unit = {},
    onPayment: () -> Unit = {},
    onAddresses: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onHelp: () -> Unit = {},
    onContact: () -> Unit = {}
) {
    var darkMode by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notifRides by AppPrefs.notifRides(context).collectAsState(initial = true)
    val notifPromos by AppPrefs.notifPromos(context).collectAsState(initial = false)

    val content: @Composable () -> Unit = {
        val pageColor = if (darkMode) Color(0xFF0A0F14) else Color(0xFFF4FBF8)
        val surfaceColor = if (darkMode) Color(0xFF181F26) else Color.White
        val borderColor = if (darkMode) Color(0xFF26313B) else Color(0xFFDCE8E3)
        val mutedColor = if (darkMode) Color(0xFFA6B4BF) else Color(0xFF667085)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                RidaLogo(
                    darkText = darkMode,
                    green = if (darkMode) Color(0xFF43D2A1) else SettingsGreen,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Settings",
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Manage your account and ride preferences.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedColor
                    )
                }
                item {
                    SettingsSection("Account", surfaceColor, borderColor, mutedColor) {
                        SettingsRow(Icons.Outlined.PersonOutline, "Personal information", "Name, phone and email", mutedColor, onPersonal)
                        SettingsDivider(borderColor)
                        SettingsRow(Icons.Outlined.CreditCard, "Payment methods", "Cards and mobile money", mutedColor, onPayment)
                        SettingsDivider(borderColor)
                        SettingsRow(Icons.Outlined.Place, "Saved addresses", "Home, work and favourite places", mutedColor, onAddresses)
                    }
                }
                item {
                    SettingsSection("Notifications", surfaceColor, borderColor, mutedColor) {
                        SettingsToggle(
                            icon = Icons.Outlined.DirectionsCar,
                            title = "Ride updates",
                            subtitle = "Driver matches and arrival updates",
                            checked = notifRides,
                            mutedColor = mutedColor,
                            onCheckedChange = { value ->
                                scope.launch { AppPrefs.setNotifRides(context, value) }
                            }
                        )
                        SettingsDivider(borderColor)
                        SettingsToggle(
                            icon = Icons.Outlined.LocalOffer,
                            title = "Offers and promos",
                            subtitle = "Discounts for your next journey",
                            checked = notifPromos,
                            mutedColor = mutedColor,
                            onCheckedChange = { value ->
                                scope.launch { AppPrefs.setNotifPromos(context, value) }
                            }
                        )
                        SettingsDivider(borderColor)
                        SettingsRow(Icons.Outlined.Tune, "Ride preferences", "Seat preferences and location sharing", mutedColor, onNotifications)
                    }
                }
                item {
                    SettingsSection("Appearance", surfaceColor, borderColor, mutedColor) {
                        SettingsToggle(
                            icon = Icons.Outlined.DarkMode,
                            title = "Dark mode",
                            subtitle = "Use a dark Settings screen",
                            checked = darkMode,
                            mutedColor = mutedColor,
                            onCheckedChange = { darkMode = it }
                        )
                    }
                }
                item {
                    SettingsSection("Support", surfaceColor, borderColor, mutedColor) {
                        SettingsRow(Icons.AutoMirrored.Outlined.HelpOutline, "Help and FAQ", "Find answers about riding with Rida", mutedColor, onHelp)
                        SettingsDivider(borderColor)
                        SettingsRow(Icons.Outlined.ChatBubbleOutline, "Contact us", "Talk to the Rida team", mutedColor, onContact)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onLogOut,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (darkMode) Color(0xFF69363B) else Color(0xFFF0D5D5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (darkMode) Color(0xFF2A1C23) else Color(0xFFFFF8F7),
                            contentColor = if (darkMode) Color(0xFFFFA3A3) else Color(0xFFB42318)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Log out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rida passenger • v1.0 alive (Oct 2026)",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                    }
                }
            }
        }
    }

    if (darkMode) RidaDarkTheme(content) else RidaLightTheme(content)
}

@Composable
private fun SettingsSection(
    title: String,
    surfaceColor: Color,
    borderColor: Color,
    mutedColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = mutedColor,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsDivider(color: Color) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = color.copy(alpha = 0.65f)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    mutedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIcon(icon)
        SettingsCopy(title, subtitle, mutedColor, Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    mutedColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIcon(icon)
        SettingsCopy(title, subtitle, mutedColor, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsGreen,
                checkedBorderColor = SettingsGreen,
                uncheckedThumbColor = mutedColor,
                uncheckedTrackColor = mutedColor.copy(alpha = 0.12f),
                uncheckedBorderColor = mutedColor.copy(alpha = 0.35f)
            )
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SettingsGreen.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (MaterialTheme.colorScheme.onSurface == Color.White) Color(0xFF43D2A1) else SettingsGreen,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SettingsCopy(title: String, subtitle: String, mutedColor: Color, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = mutedColor
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SettingsScreenPreview() {
    RidaLightTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun CompactSettingsScreenPreview() {
    RidaLightTheme {
        SettingsScreen()
    }
}
