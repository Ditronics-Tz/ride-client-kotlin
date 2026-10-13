package com.example.ridepassenger2.ui.support

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val RidaGreen = Color(0xFF00A86B)
private val DarkForestGreen = Color(0xFF063F32)
private val LightMint = Color(0xFFE8F7F0)

@Composable
private fun SupportTopBar(onBack: () -> Unit) {
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = InkDark,
                modifier = Modifier.size(22.dp)
            )
        }
        RidaLogo(green = RidaGreen)
    }
}

// ==================================================================
// SCREEN 1: Safety Center (Image 1)
// ==================================================================
@Composable
fun SafetyCenterScreen(
    onBack: () -> Unit = {},
    onEmergencyOptions: () -> Unit = {},
    onTrustedContacts: () -> Unit = {},
    onShareTrip: () -> Unit = {},
    onSafetyTips: () -> Unit = {}
) {
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
            SupportTopBar(onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Safety Center",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Support and protection for every trip.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Urgent Assistance Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkForestGreen)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RidaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Need urgent assistance?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "If you are in immediate danger, contact local emergency services.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .clickable(onClick = onEmergencyOptions)
                            .padding(horizontal = 18.dp, vertical = 11.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Emergency help options",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = DarkForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "SAFETY FEATURES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(12.dp))

                SafetyFeatureCard(
                    icon = Icons.Outlined.PersonOutline,
                    title = "Trusted contacts",
                    subtitle = "Share trip details with people you trust",
                    onClick = onTrustedContacts
                )

                Spacer(Modifier.height(10.dp))

                SafetyFeatureCard(
                    icon = Icons.Outlined.Route,
                    title = "Share my trip",
                    subtitle = "Send your live trip link securely",
                    onClick = onShareTrip
                )

                Spacer(Modifier.height(10.dp))

                SafetyFeatureCard(
                    icon = Icons.Outlined.Shield,
                    title = "Ride safety tips",
                    subtitle = "Learn how to stay safe on the road",
                    onClick = onSafetyTips
                )

                Spacer(Modifier.height(24.dp))

                // Check Driver Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightMint)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Check",
                        tint = RidaGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Check your driver and vehicle before pickup.",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SafetyFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LightMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = RidaGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF98A6A0),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==================================================================
// SCREEN 2: My Support Cases (Image 2)
// ==================================================================
@Composable
fun MySupportCasesScreen(
    onBack: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onCaseClick: (caseId: String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All cases") }

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
            SupportTopBar(onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "My support cases",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Track conversations and resolutions.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Filter Chips Row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("All cases", "Open", "Resolved").forEach { filter ->
                        val sel = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (sel) RidaGreen else CardBg)
                                .border(1.dp, if (sel) RidaGreen else CardBorder, RoundedCornerShape(999.dp))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 18.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sel) Color.White else TextMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Support Case 1
                SupportCaseCard(
                    caseId = "#RD-1042",
                    badgeText = "In review",
                    badgeIsGreen = true,
                    title = "Unrecognized fare charge",
                    subtitle = "Updated today · Awaiting reply",
                    onClick = { onCaseClick("RD-1042") }
                )

                Spacer(Modifier.height(12.dp))

                // Support Case 2
                SupportCaseCard(
                    caseId = "#RD-1033",
                    badgeText = "Open",
                    badgeIsGreen = true,
                    title = "Driver didn't arrive",
                    subtitle = "Updated yesterday · In progress",
                    onClick = { onCaseClick("RD-1033") }
                )

                Spacer(Modifier.height(12.dp))

                // Support Case 3
                SupportCaseCard(
                    caseId = "#RD-1008",
                    badgeText = "Resolved",
                    badgeIsGreen = false,
                    title = "Item left in vehicle",
                    subtitle = "Closed on 2 Oct · Resolved",
                    onClick = { onCaseClick("RD-1008") }
                )

                Spacer(Modifier.height(28.dp))
            }

            // Create Support Request Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(RidaGreen)
                    .clickable(onClick = onCreateRequest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create support request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SupportCaseCard(
    caseId: String,
    badgeText: String,
    badgeIsGreen: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    tint = RidaGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = caseId,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (badgeIsGreen) LightMint else Color(0xFFF3F4F6))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badgeIsGreen) RidaGreen else Color(0xFF6B7280)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF98A6A0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==================================================================
// SCREEN 3: Report an Issue (Image 3)
// ==================================================================
@Composable
fun ReportIssueScreen(
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf<String?>(null) }

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
            SupportTopBar(onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Report an issue",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tell us what happened. We're here to help.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Issue category
                Text(
                    text = "Issue category",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { selectedCategory = it },
                    placeholder = {
                        Text(
                            text = "Select issue type",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.FormatListBulleted,
                            contentDescription = "Category",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
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

                // Subject
                Text(
                    text = "Subject",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = {
                        Text(
                            text = "Give your issue a short title",
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

                // Description
                Text(
                    text = "Description",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            text = "Explain what happened. Include trip details or other information that may help our team.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 20.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
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

                Spacer(Modifier.height(24.dp))

                // ATTACH EVIDENCE Section
                Text(
                    text = "ATTACH EVIDENCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                        .clickable { attachmentName = "receipt_screenshot.png" }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = "Attach",
                            tint = RidaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attachmentName ?: "Add photo or document",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Optional · JPG, PNG or PDF",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Please do not share passwords or full card details.",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(28.dp))
            }

            // Submit Request Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(RidaGreen)
                    .clickable {
                        if (subject.isBlank() && description.isBlank()) {
                            Toast.makeText(context, "Please enter a subject or description", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        onSubmit()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Submit request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ==================================================================
// SCREEN 4: Case Details (Image 4)
// ==================================================================
@Composable
fun CaseDetailsScreen(
    caseId: String = "RD-1042",
    onBack: () -> Unit = {},
    onReply: () -> Unit = {}
) {
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
            SupportTopBar(onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Case details",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your request and support updates.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Case #$caseId",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(LightMint)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "In review",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RidaGreen
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Unrecognized fare charge",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Submitted 10 Oct · Payment related",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "TIMELINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(10.dp))

                // Timeline Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    TimelineItem(
                        icon = Icons.Outlined.Check,
                        isGreenIcon = true,
                        title = "Request submitted",
                        subtitle = "10 Oct · 09:41",
                        showLine = true
                    )

                    TimelineItem(
                        icon = Icons.Outlined.PersonOutline,
                        isGreenIcon = true,
                        title = "Assigned to support",
                        subtitle = "10 Oct · 09:45",
                        showLine = true
                    )

                    TimelineItem(
                        icon = Icons.Outlined.Schedule,
                        isGreenIcon = false,
                        title = "Awaiting an update",
                        subtitle = "We will notify you when there is a reply",
                        showLine = false
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightMint)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = "Chat",
                        tint = RidaGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "You can add more information any time.",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            // Reply Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(RidaGreen)
                    .clickable(onClick = onReply),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reply to support",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TimelineItem(
    icon: ImageVector,
    isGreenIcon: Boolean,
    title: String,
    subtitle: String,
    showLine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isGreenIcon) LightMint else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGreenIcon) RidaGreen else Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(32.dp)
                        .background(Color(0xFFEEF2F0))
                )
            }
        }

        Column(modifier = Modifier.padding(top = 4.dp, bottom = if (showLine) 12.dp else 0.dp)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }
    }
}

// Legacy Aliases for backwards compatibility with FlowNavGraphs.kt
@Composable
fun SupportHomeScreen(
    query: String = "",
    onQuery: (String) -> Unit = {},
    tickets: List<Ticket> = emptyList(),
    onContact: () -> Unit = {},
    onTopic: (Topic) -> Unit = {},
    onTicket: (String) -> Unit = {},
    onCall112: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    SafetyCenterScreen(
        onBack = onBack,
        onEmergencyOptions = onCall112,
        onTrustedContacts = onContact,
        onShareTrip = onContact,
        onSafetyTips = onContact
    )
}

@Composable
fun MyRequestsScreen(
    tickets: List<Ticket> = emptyList(),
    filter: TicketStatus? = null,
    onFilter: (TicketStatus?) -> Unit = {},
    onOpen: (String) -> Unit = {},
    onBack: () -> Unit = {},
    loading: Boolean = false
) {
    MySupportCasesScreen(
        onBack = onBack,
        onCreateRequest = { onOpen("new") },
        onCaseClick = onOpen
    )
}

@Composable
fun RequestFormScreen(
    state: RequestFormState = RequestFormState(),
    onSubject: (String) -> Unit = {},
    onDesc: (String) -> Unit = {},
    onAttach: () -> Unit = {},
    onRemove: (String) -> Unit = {},
    onContact: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    ReportIssueScreen(
        onBack = onBack,
        onSubmit = onSubmit
    )
}

@Composable
fun RequestDetailScreen(
    ticket: Ticket? = null,
    msgs: List<TicketMsg> = emptyList(),
    reply: String = "",
    onReply: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onResolve: () -> Unit = {},
    rating: Int = 0,
    onRate: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    loading: Boolean = false,
    error: String? = null
) {
    CaseDetailsScreen(
        caseId = ticket?.id ?: "RD-1042",
        onBack = onBack,
        onReply = onSend
    )
}

@Composable
fun TopicScreen(
    topic: Topic = Topic.OTHER,
    onSub: (String) -> Unit = {},
    onBack: () -> Unit = {},
    prefilledTrip: String? = null
) {
    SafetyCenterScreen(onBack = onBack)
}

@Composable
fun SelfHelpScreen(
    answer: String = "",
    onSolved: () -> Unit = {},
    onNeedHelp: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    SafetyCenterScreen(onBack = onBack)
}

@Composable
fun ConfirmScreen(
    ticketId: String = "RD-1042",
    summary: String = "",
    onView: () -> Unit = {},
    onHome: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    CaseDetailsScreen(caseId = ticketId, onBack = onBack, onReply = onView)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PrevSafetyCenterScreen() {
    RIDEPASSENGER2Theme {
        SafetyCenterScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PrevMySupportCasesScreen() {
    RIDEPASSENGER2Theme {
        MySupportCasesScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PrevReportIssueScreen() {
    RIDEPASSENGER2Theme {
        ReportIssueScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PrevCaseDetailsScreen() {
    RIDEPASSENGER2Theme {
        CaseDetailsScreen()
    }
}
