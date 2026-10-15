package com.example.ridepassenger2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val RidaGreen = Color(0xFF00A86B)
private val LightMint = Color(0xFFE8F7F0)

@Composable
fun AddAddressScreen(
    initialLabel: String = "",
    initialAddress: String = "",
    onBack: () -> Unit = {},
    onSave: (label: String, address: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var locationLabel by remember(initialLabel) { mutableStateOf(initialLabel) }
    var addressDetails by remember(initialAddress) { mutableStateOf(initialAddress) }

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
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                // Title Header
                Text(
                    text = "Add an address",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Search or position the pin precisely.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search location or landmark",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        RidaIcon(
                            icon = RidaIcons.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(18.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        focusedBorderColor = RidaGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Interactive Map View Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE2EFE8))
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Stylized Map Graphics
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Grid Road Lines
                        val roadColor = Color(0xFFD3E4DB)
                        val gridStepX = width / 5f
                        val gridStepY = height / 4f

                        for (i in 1..4) {
                            drawLine(
                                color = roadColor,
                                start = Offset(gridStepX * i, 0f),
                                end = Offset(gridStepX * i, height),
                                strokeWidth = 14f
                            )
                        }
                        for (i in 1..3) {
                            drawLine(
                                color = roadColor,
                                start = Offset(0f, gridStepY * i),
                                end = Offset(width, gridStepY * i),
                                strokeWidth = 14f
                            )
                        }

                        // Building Blocks
                        val blockColor = Color.White.copy(alpha = 0.85f)
                        val greenBlockColor = Color(0xFFC3E3D3)

                        drawRoundRect(
                            color = blockColor,
                            topLeft = Offset(width * 0.38f, height * 0.15f),
                            size = Size(width * 0.22f, height * 0.2f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = greenBlockColor,
                            topLeft = Offset(width * 0.73f, height * 0.14f),
                            size = Size(width * 0.14f, height * 0.22f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = blockColor,
                            topLeft = Offset(width * 0.07f, height * 0.28f),
                            size = Size(width * 0.15f, height * 0.12f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = blockColor,
                            topLeft = Offset(width * 0.72f, height * 0.58f),
                            size = Size(width * 0.19f, height * 0.22f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = blockColor,
                            topLeft = Offset(width * 0.16f, height * 0.65f),
                            size = Size(width * 0.18f, height * 0.18f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                    }

                    // Map Pin Center Marker
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(LightMint),
                        contentAlignment = Alignment.Center
                    ) {
                        RidaIcon(
                            icon = RidaIcons.SavedPlace,
                            contentDescription = "Pin",
                            tint = RidaGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Floating Pill Overlay: "Drag pin to adjust"
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .shadow(2.dp, RoundedCornerShape(999.dp))
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Drag pin to adjust",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF063F32)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Location label
                Text(
                    text = "Location label",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = locationLabel,
                    onValueChange = { locationLabel = it },
                    placeholder = {
                        Text(
                            text = "Home, Work or Custom",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        focusedBorderColor = RidaGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(18.dp))

                // Address details
                Text(
                    text = "Address details",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = addressDetails,
                    onValueChange = { addressDetails = it },
                    placeholder = {
                        Text(
                            text = "Building, street or nearby landmark",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = InkDark,
                        unfocusedTextColor = InkDark,
                        focusedBorderColor = RidaGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(28.dp))

                // Save Address Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(RidaGreen)
                        .clickable {
                            if (addressDetails.isBlank() && searchQuery.isBlank()) {
                                Toast.makeText(context, "Please enter an address or search location", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val finalAddress = addressDetails.ifBlank { searchQuery }
                            val finalLabel = locationLabel.ifBlank { "Saved Place" }
                            onSave(finalLabel, finalAddress)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save address",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AddAddressScreenPreview() {
    RIDEPASSENGER2Theme {
        AddAddressScreen()
    }
}
