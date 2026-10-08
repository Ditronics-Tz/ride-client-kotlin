package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaLogoLargeDark
import com.example.ridepassenger2.ui.components.RidaMintButton
import com.example.ridepassenger2.ui.theme.*

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onAlreadyHaveAccount: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RidaOnboardingBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                RidaLogoLargeDark()

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Better rides.",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.8).sp
                )
                Text(
                    text = "Together.",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = RidaMint,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.8).sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Request a ride, share the journey, and\nmove smarter.",
                    fontSize = 14.sp,
                    color = RidaTextMutedOnboarding,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Center illustration area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                OnboardingIllustration(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
            }

            // Bottom actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RidaMintButton(
                    text = "Get Started",
                    onClick = onGetStarted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "I already have an account",
                    fontSize = 13.sp,
                    color = RidaTextMutedOnboarding,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAlreadyHaveAccount)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                HomeIndicator(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Glow behind moon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-32).dp, y = 24.dp)
                .size(96.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(RidaMint.copy(alpha = 0.28f), Color.Transparent),
                        radius = 200f
                    )
                )
        )
        // Moon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 36.dp)
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(RidaMint)
        )

        // City skyline silhouettes (very subtle)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 80.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = listOf(48, 72, 36, 60, 84, 42, 60)
            val colors = listOf(Color(0xFF182A32), Color(0xFF1B2F38), Color(0xFF182A32), Color(0xFF16272E), Color(0xFF203742), Color(0xFF182A32), Color(0xFF172830))
            heights.forEachIndexed { i, h ->
                Box(
                    modifier = Modifier
                        .width(when (i) {1-> 18.dp; 4->16.dp; 6->22.dp; else->14.dp})
                        .height(h.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(colors[i].copy(alpha = 0.35f))
                )
            }
        }

        // Hills & road drawn with Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Back hill
            val backHill = Path().apply {
                moveTo(w * 0.28f, h * 0.58f)
                cubicTo(w * 0.5f, h * 0.44f, w * 0.75f, h * 0.48f, w, h * 0.66f)
                lineTo(w, h)
                lineTo(w * 0.28f, h)
                close()
            }
            drawPath(
                path = backHill,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF14262C), Color(0xFF0B1316)),
                    start = Offset(w * 0.5f, h * 0.3f),
                    end = Offset(w * 0.5f, h)
                )
            )

            // Fore hill left
            val foreHill = Path().apply {
                moveTo(0f, h * 0.42f)
                cubicTo(w * 0.24f, h * 0.46f, w * 0.35f, h * 0.65f, w * 0.4f, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = foreHill,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1F3B3F), Color(0xFF0E1B1F)),
                    start = Offset(w * 0.2f, h * 0.42f),
                    end = Offset(w * 0.2f, h)
                )
            )

            // Road
            val road = Path().apply {
                moveTo(w * 0.9f, h * 0.56f)
                cubicTo(w * 0.65f, h * 0.57f, w * 0.42f, h * 0.66f, 0f, h)
                lineTo(w, h)
                lineTo(w, h * 0.59f)
                close()
            }
            drawPath(
                path = road,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF19252B), Color(0xFF070C0E)),
                    start = Offset(w * 0.6f, h * 0.48f),
                    end = Offset(w * 0.3f, h)
                )
            )

            // Neon edge
            val neon = Path().apply {
                moveTo(w * 0.94f, h * 0.58f)
                cubicTo(w * 0.66f, h * 0.59f, w * 0.43f, h * 0.68f, 0f, h)
            }
            drawPath(
                path = neon,
                color = RidaMint.copy(alpha = 0.55f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Dashed center line (approx)
            val dashPath = Path().apply {
                moveTo(w * 0.78f, h * 0.60f)
                cubicTo(w * 0.59f, h * 0.63f, w * 0.41f, h * 0.72f, 0f, h)
            }
            drawPath(
                path = dashPath,
                color = Color(0xFF3D5660).copy(alpha = 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }

        // Car (simple white car with wheels) - positioned on road
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp, x = (-8).dp)
        ) {
            CarMini()
        }
    }
}

@Composable
private fun CarMini() {
    Box(modifier = Modifier.size(width = 104.dp, height = 48.dp)) {
        // shadow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
                .size(width = 84.dp, height = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF05080A).copy(alpha = 0.85f))
        )
        // body lower
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp)
                .size(width = 88.dp, height = 18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF4F7F8))
        )
        // cabin
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 6.dp)
                .size(width = 62.dp, height = 14.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 6.dp))
                .background(Color(0xFFE2E8EB))
        )
        // windows (two small black rects)
        Row(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 8.dp).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(18.dp, 10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1B282F)))
            Box(modifier = Modifier.size(22.dp, 10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1B282F)))
        }
        // headlight mint glow
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 2.dp, y = 4.dp)
                .size(6.dp, 8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(RidaMint)
        )
        // taillight
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 0.dp, y = 6.dp)
                .size(4.dp, 6.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFFFF453A))
        )
        // wheels
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 12.dp, y = 0.dp)
                .size(16.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF10171A))
                .padding(3.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFFD3DDE1))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-12).dp, y = 0.dp)
                .size(16.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF10171A))
                .padding(3.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFFD3DDE1))
        )
        // headlight beam cone (faint)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 28.dp, y = 6.dp)
                .size(width = 36.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(RidaMint.copy(alpha = 0.18f))
        )
    }
}
