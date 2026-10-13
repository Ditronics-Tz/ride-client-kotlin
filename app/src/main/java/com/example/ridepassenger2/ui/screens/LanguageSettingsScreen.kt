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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
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
private val LightMint = Color(0xFFE8F7F0)

@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit = {},
    onSaveLanguage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("English") }

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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                Text(
                    text = "Language",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Choose the language you prefer.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // APP LANGUAGE
                Text(
                    text = "APP LANGUAGE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A88),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))

                // English Card
                LanguageOptionCard(
                    title = "English",
                    subtitle = "Currently selected",
                    selected = selectedLanguage == "English",
                    onClick = { selectedLanguage = "English" }
                )

                Spacer(Modifier.height(10.dp))

                // Kiswahili Card
                LanguageOptionCard(
                    title = "Kiswahili",
                    subtitle = "Lugha ya Kiswahili",
                    selected = selectedLanguage == "Kiswahili",
                    onClick = { selectedLanguage = "Kiswahili" }
                )

                Spacer(Modifier.height(24.dp))

                // Info Banner Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(LightMint)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = RidaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "About language preferences",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Some support articles and messages may still appear in English while translation expands.",
                            fontSize = 12.5.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            // Save Language Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(RidaGreen)
                    .clickable {
                        Toast.makeText(context, "Language saved as $selectedLanguage", Toast.LENGTH_SHORT).show()
                        onSaveLanguage(selectedLanguage)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save language",
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
private fun LanguageOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
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
                imageVector = Icons.Outlined.Language,
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
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(RidaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6))
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LanguageSettingsScreenPreview() {
    RIDEPASSENGER2Theme {
        LanguageSettingsScreen()
    }
}
