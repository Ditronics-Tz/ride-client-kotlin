package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator

private val DetailsBg = Color(0xFF0C1014)
private val SheetBg = Color(0xFF101418)
private val SheetBorder = Color(0xFF1C252D)
private val MintRoute = Color(0xFF43D2A1)
private val MintDot = Color(0xFF4ADE80)

@Composable
fun RideDetailsScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailsBg)
    ) {
        // Map
        RideRouteMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
        )

        // Top bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "‹", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                }
                Text(text = "Ride Details", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        // Bottom sheet
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(SheetBg)
                .border(1.dp, SheetBorder, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF181F26))
                            .border(1.dp, Color(0xFF26313B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", fontSize = 20.sp)
                    }
                    Column {
                        Text(text = "Standard", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Shared ride • 1-4 seats", color = Color(0xFF7B8893), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF182128)))

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "◷", color = Color(0xFF8695A2), fontSize = 14.sp)
                            Text(text = "12 min", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(text = "Pickup time", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column {
                        Text(text = "TZS 2,500", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Total fare", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "👥", color = Color(0xFF8695A2), fontSize = 14.sp)
                            Text(text = "2/3", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(text = "Seats filled", color = Color(0xFF6B7884), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stops
                Column {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintDot))
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF33404D)))
                        }
                        Column {
                            Text(text = "Kariakoo", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Pickup (exact location)", color = Color(0xFF6F7D89), fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
                        Column {
                            Text(text = "Mwenge", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Drop-off (approx.)", color = Color(0xFF6F7D89), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141B20))
                        .border(1.dp, Color(0xFF1E2730), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF182622)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍃", fontSize = 16.sp, color = MintDot)
                    }
                    Column {
                        Text(text = "You're sharing this ride", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Good for your wallet. Better for the planet.", color = Color(0xFF6E7B86), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161C22))
                        .border(1.dp, Color(0xFF232D36), RoundedCornerShape(12.dp))
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cancel Ride", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    HomeIndicator(color = Color.White.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun RideRouteMap(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color(0xFF0A1015))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Secondary roads
            val pathBg1 = Path().apply {
                moveTo(-0.05f*w, 0.25f*h)
                cubicTo(0.20f*w, 0.35f*h, 0.46f*w, 0.28f*h, 1.07f*w, 0.46f*h)
            }
            drawPath(pathBg1, color = Color(0xFF151C22), style = Stroke(width = 14f, cap = StrokeCap.Round))
            val pathBg2 = Path().apply {
                moveTo(0.25f*w, -0.05f*h)
                cubicTo(0.30f*w, 0.46f*h, 0.18f*w, 1.06f*h, 0.18f*w, 1.06f*h)
            }
            drawPath(pathBg2, color = Color(0xFF141B21), style = Stroke(width = 10f))
            val pathBg3 = Path().apply {
                moveTo(0.40f*w, 0f)
                lineTo(0.66f*w, h)
            }
            drawPath(pathBg3, color = Color(0xFF18222A), style = Stroke(width = 8f))
            // Glow
            val route = Path().apply {
                moveTo(0.17f*w, 0.26f*h)
                cubicTo(0.19f*w, 0.37f*h, 0.28f*w, 0.50f*h, 0.37f*w, 0.50f*h)
                cubicTo(0.46f*w, 0.50f*h, 0.58f*w, 0.46f*h, 0.63f*w, 0.58f*h)
                cubicTo(0.66f*w, 0.67f*h, 0.71f*w, 0.78f*h, 0.79f*w, 0.75f*h)
            }
            drawPath(route, color = MintRoute.copy(alpha = 0.2f), style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(route, color = MintRoute, style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        // Labels
        Text(text = "Kariakoo", color = Color(0xFF4B5965), fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 90.dp, end = 60.dp))
        Text(text = "Mwenge", color = Color(0xFF4B5965), fontSize = 11.sp, modifier = Modifier.align(Alignment.Center).offset(x = (-60).dp, y = 30.dp))
        // Origin dot
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 55.dp, y = 80.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(MintDot)
                .border(2.dp, Color(0xFF0A1015), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
        }
        // Destination dot
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 298.dp, y = 235.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFF0A1015), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF0A1015)))
        }
        // Car
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 40.dp, y = -10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 22.dp, height = 38.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF0C151B))
                    .border(1.dp, Color(0xFF25323A), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(16.dp, 10.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                    Box(modifier = Modifier.size(13.dp, 5.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF0F171E)))
                    Box(modifier = Modifier.size(13.dp, 4.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF0F171E)))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MintRoute))
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MintRoute))
                    }
                }
            }
        }
    }
}
