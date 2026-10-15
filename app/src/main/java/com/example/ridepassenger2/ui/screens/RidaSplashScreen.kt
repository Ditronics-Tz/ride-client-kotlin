package com.example.ridepassenger2.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.R
import com.example.ridepassenger2.ui.theme.PoppinsFontFamily

private val SplashGreen = Color(0xFF00B978)
private val SplashMuted = Color(0xFF667B73)

@Composable
fun RidaSplashScreen() {
    val transition = rememberInfiniteTransition(label = "rida-splash")
    val dotScales = (0..2).map { index ->
        val scale by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 560, delayMillis = index * 140),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loading-dot-$index"
        )
        scale
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val logoFrameWidth = maxWidth.coerceAtMost(420.dp)
                val sourceWidth = logoFrameWidth * 1.52f
                Image(
                    painter = painterResource(R.drawable.rida_splash_logo),
                    contentDescription = "Rida",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .requiredWidth(sourceWidth)
                        .requiredHeight(sourceWidth / 1.5f)
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "Getting Rida ready",
                color = SplashMuted,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dotScales.forEach { scale ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(SplashGreen, androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
