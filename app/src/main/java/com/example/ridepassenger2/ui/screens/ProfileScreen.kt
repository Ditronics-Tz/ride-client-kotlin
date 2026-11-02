package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.data.repository.UserRepository
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.components.UserAvatar
import com.example.ridepassenger2.ui.theme.RidaLightTheme

@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val session by SessionManager.observe(context).collectAsState(initial = SessionManager.Session())
    val trips by RideRepository.history.collectAsState()

    LaunchedEffect(Unit) {
        UserRepository.refreshSessionFromApi(context)
        RideRepository.refreshHistory()
    }

    ProfileScreenContent(
        session = session,
        rideCount = trips.size,
        sharedRideCount = trips.count { it.isShared },
        onSettings = onSettings,
        onMenuClick = onMenuClick,
        onLogOut = onLogOut
    )
}

@Composable
private fun ProfileScreenContent(
    session: SessionManager.Session,
    rideCount: Int,
    sharedRideCount: Int,
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HelpPageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = HelpTitleText,
                modifier = Modifier.weight(1f)
            )
            RidaLogo(green = HelpGreen)
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "overview") {
                ProfileOverview(
                    session = session,
                    rideCount = rideCount,
                    sharedRideCount = sharedRideCount,
                    onOpenPersonalInfo = { onMenuClick("personal") }
                )
            }

            item(key = "preferences") {
                ProfileMenuSection(title = "Account & preferences") {
                    ProfileOptionRow(
                        icon = RidaIcons.EditProfile,
                        title = "Personal information",
                        subtitle = "Name, phone and email",
                        onClick = { onMenuClick("personal") }
                    )
                    ProfileOptionDivider()
                    ProfileOptionRow(
                        icon = RidaIcons.Payment,
                        title = "Payment methods",
                        subtitle = "Cards and mobile money",
                        onClick = { onMenuClick("payment") }
                    )
                    ProfileOptionDivider()
                    ProfileOptionRow(
                        icon = RidaIcons.Promotions,
                        title = "Promotions & discounts",
                        subtitle = "Offers, promo codes and rewards",
                        onClick = { onMenuClick("promotions") }
                    )
                    ProfileOptionDivider()
                    ProfileOptionRow(
                        icon = RidaIcons.SavedPlace,
                        title = "Saved addresses",
                        subtitle = "Home, work and favourite places",
                        onClick = { onMenuClick("addresses") }
                    )
                    ProfileOptionDivider()
                    ProfileOptionRow(
                        icon = RidaIcons.Settings,
                        title = "Preferences",
                        subtitle = "Language, notifications and privacy",
                        onClick = {
                            onMenuClick("preferences")
                            onSettings()
                        }
                    )
                }
            }

            item(key = "help") {
                ProfileMenuSection(title = "Help & safety") {
                    ProfileOptionRow(
                        icon = RidaIcons.Support,
                        title = "Support",
                        subtitle = "Get help with your rides",
                        onClick = { onMenuClick("support") }
                    )
                    ProfileOptionDivider()
                    ProfileOptionRow(
                        icon = RidaIcons.Safety,
                        title = "Safety",
                        subtitle = "Safety tools and assistance",
                        onClick = { onMenuClick("safety") }
                    )
                }
            }

            item(key = "logout") {
                OutlinedButton(
                    onClick = onLogOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDA29B).copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFF1F0),
                        contentColor = Color(0xFFD92D20)
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    RidaIcon(
                        icon = RidaIcons.Logout,
                        contentDescription = null,
                        tint = Color(0xFFD92D20),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HelpTitleText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, HelpCardBorder),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ProfileOverview(
    session: SessionManager.Session,
    rideCount: Int,
    sharedRideCount: Int,
    onOpenPersonalInfo: () -> Unit = {}
) {
    val isProfileIncomplete = session.name.isBlank() || session.email.isBlank() || session.phone.isBlank()

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HelpCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = "Edit personal information", onClick = onOpenPersonalInfo)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 2.dp,
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        UserAvatar(
                            name = session.name,
                            photoUrl = session.profilePictureUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "Profile photo",
                            photoVersion = session.profilePictureVersion,
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = session.name.ifBlank { "Name not added" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HelpTitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ProfileContactDetail(RidaIcons.Mail, session.email.ifBlank { "Email not added" })
                    ProfileContactDetail(RidaIcons.Phone, session.phone.ifBlank { "Phone not added" })
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFFECF8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    RidaIcon(
                        icon = RidaIcons.Edit,
                        contentDescription = null,
                        tint = HelpGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isProfileIncomplete) {
                HorizontalDivider(color = HelpCardBorder.copy(alpha = 0.7f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RidaIcon(
                        icon = RidaIcons.Info,
                        contentDescription = null,
                        tint = HelpGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Complete your profile",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HelpGreen
                    )
                }
            }

            HorizontalDivider(color = HelpCardBorder.copy(alpha = 0.7f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatCard(
                    icon = RidaIcons.Rides,
                    value = rideCount.toString(),
                    label = "Rides taken",
                    modifier = Modifier.weight(1f)
                )
                Box(Modifier.width(1.dp).height(32.dp).background(HelpCardBorder))
                ProfileStatCard(
                    icon = RidaIcons.Share,
                    value = sharedRideCount.toString(),
                    label = "Rides shared",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileContactDetail(icon: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        RidaIcon(icon, contentDescription = null, tint = HelpGreen, modifier = Modifier.size(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF436858),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileStatCard(
    icon: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RidaIcon(icon, contentDescription = null, tint = HelpGreen, modifier = Modifier.size(15.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HelpTitleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF436858))
    }
}

@Composable
private fun ProfileOptionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
        color = HelpCardBorder.copy(alpha = 0.5f)
    )
}

@Composable
private fun ProfileOptionRow(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(role = Role.Button, onClickLabel = title, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(HelpGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = icon,
                contentDescription = null,
                tint = HelpGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HelpTitleText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = HelpMutedText
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFF2F4F7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            RidaIcon(
                icon = RidaIcons.ChevronRight,
                contentDescription = null,
                tint = HelpMutedText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(name = "Empty profile", showBackground = true, widthDp = 390, heightDp = 760)
@Composable
fun ProfileScreenPreview() {
    RidaLightTheme {
        ProfileScreenContent(
            session = SessionManager.Session(),
            rideCount = 0,
            sharedRideCount = 0
        )
    }
}

@Preview(name = "Incomplete profile", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun IncompleteProfilePreview() {
    RidaLightTheme {
        ProfileScreenContent(SessionManager.Session(), 0, 0)
    }
}

@Preview(name = "Small screen, large text", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun SmallProfilePreview() {
    RidaLightTheme {
        ProfileScreenContent(
            session = SessionManager.Session(),
            rideCount = 0,
            sharedRideCount = 0
        )
    }
}
