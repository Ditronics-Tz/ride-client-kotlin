package com.example.ridepassenger2.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.data.repository.UserRepository
import com.example.ridepassenger2.data.repository.ProfilePhotoRepository
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.UserAvatar
import com.example.ridepassenger2.ui.theme.RidaLightTheme
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val session by SessionManager.observe(context).collectAsState(initial = SessionManager.Session())
    val trips by RideRepository.history.collectAsState()
    val scope = rememberCoroutineScope()
    var uploadingPhoto by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var lastPhoto by remember { mutableStateOf<Uri?>(null) }

    fun uploadPhoto(uri: Uri) {
        if (uploadingPhoto) return
        lastPhoto = uri
        photoError = null
        uploadingPhoto = true
        scope.launch {
            try {
                ProfilePhotoRepository.upload(context, uri)
                    .onSuccess { Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show() }
                    .onFailure { photoError = it.message }
            } finally {
                uploadingPhoto = false
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) uploadPhoto(uri)
    }

    LaunchedEffect(Unit) {
        UserRepository.refreshSessionFromApi(context)
        RideRepository.refreshHistory()
    }

    ProfileScreenContent(
        session = session,
        rideCount = trips.size,
        sharedRideCount = trips.count { it.isShared },
        uploadingPhoto = uploadingPhoto,
        photoError = photoError,
        onChoosePhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRetryPhoto = { lastPhoto?.let { uploadPhoto(it) } },
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
    uploadingPhoto: Boolean = false,
    photoError: String? = null,
    onChoosePhoto: () -> Unit = {},
    onRetryPhoto: () -> Unit = {},
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.displaySmall,
                color = HelpTitleText,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
                RidaIcon(
                    icon = RidaIcons.Settings,
                    contentDescription = "Settings",
                    tint = HelpTitleText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(key = "overview") {
                ProfileOverview(session, rideCount, sharedRideCount, uploadingPhoto, photoError, onChoosePhoto, onRetryPhoto)
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HelpCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFD92D20)
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    RidaIcon(RidaIcons.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Log out", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = HelpTitleText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, HelpCardBorder),
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
    uploadingPhoto: Boolean,
    photoError: String?,
    onChoosePhoto: () -> Unit,
    onRetryPhoto: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HelpCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp)
                        .clickable(enabled = !uploadingPhoto, role = Role.Button, onClickLabel = "Change profile photo", onClick = onChoosePhoto)
                ) {
                    UserAvatar(session.name, session.profilePictureUrl, Modifier.fillMaxSize(), "Profile photo", session.profilePictureVersion)
                    if (uploadingPhoto) {
                        Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.White.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        }
                    } else {
                        Surface(modifier = Modifier.size(24.dp).align(Alignment.BottomEnd), shape = CircleShape, color = Color.Black) {
                            Box(contentAlignment = Alignment.Center) {
                                RidaIcon(RidaIcons.Upload, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = session.name.ifBlank { "Name not added" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = HelpTitleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = session.email.ifBlank { "Email not added" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = HelpMutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = session.phone.ifBlank { "Phone not added" },
                        style = MaterialTheme.typography.bodySmall,
                        color = HelpMutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                TextButton(onClick = onChoosePhoto, enabled = !uploadingPhoto) {
                    Text(
                        text = if (uploadingPhoto) "Uploading photo…" else if (session.profilePictureUrl.isNullOrBlank()) "Upload photo" else "Change photo",
                        color = Color.Black
                    )
                }
                Text("JPG, PNG or WebP, up to 5 MB", style = MaterialTheme.typography.bodySmall, color = HelpMutedText)
                if (photoError != null) {
                    Text(
                        photoError, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB42318),
                        modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    TextButton(onClick = onRetryPhoto, enabled = !uploadingPhoto) { Text("Retry upload", color = Color.Black) }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = HelpCardBorder)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                ProfileStat(rideCount.toString(), "Rides taken", Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(36.dp).align(Alignment.CenterVertically), color = HelpCardBorder)
                ProfileStat(sharedRideCount.toString(), "Rides shared", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HelpTitleText,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = HelpMutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileOptionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp, end = 16.dp),
        color = HelpCardBorder.copy(alpha = 0.65f)
    )
}

@Composable
private fun ProfileOptionRow(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clickable(role = Role.Button, onClickLabel = title, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RidaIcon(icon, contentDescription = null, tint = HelpGreen, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = HelpTitleText)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HelpMutedText)
        }
        RidaIcon(
            icon = RidaIcons.ChevronRight,
            contentDescription = null,
            tint = HelpMutedText,
            modifier = Modifier.size(18.dp)
        )
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
