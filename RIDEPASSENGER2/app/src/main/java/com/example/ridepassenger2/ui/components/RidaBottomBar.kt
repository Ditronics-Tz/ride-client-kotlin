package com.example.ridepassenger2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// White home-theme pill — green stripes, same shape as jpeg but matching Home's white sheet
private val BarWhite = Color.White
private val BarBorderLight = Color(0xFFE5E7EB)
private val BarShadow = Color.Black.copy(alpha = 0.10f)
private val PillGreen = Color(0xFF0D5E3A) // requested green (was red #FF1530)
private val PillGreen2 = Color(0xFF237055)
private val PillGreenGlow = Color(0x2A0D5E3A)
private val InactiveGray = Color(0xFF8A9290)
private val InactiveLight = Color(0xFF9EA3A8)

private val LogoBgWhite = Color.White
private val LogoBorderLight = BarBorderLight
private val LogoGreen = PillGreen

@Composable
fun RidaBottomBar(
    selected: String, // "Home", "Activity"/"History", "Profile"
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onCenter: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    // restore previous items: Home | Activity | (center car) | Profile
    // selected mapping keeps History alias
    val norm = when (selected) {
        "History" -> "Activity"
        else -> selected
    }
    var pillW by remember { mutableStateOf(0f) }
    var pillH by remember { mutableStateOf(0f) }

    val activeIndex = when (norm) {
        "Home" -> 0
        "Activity" -> 1
        "Profile" -> 3 // skip center placeholder at 2
        else -> 0
    }
    val animIndex by animateFloatAsState(targetValue = activeIndex.toFloat(), animationSpec = tween(380), label = "notch")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(16.dp, RoundedCornerShape(34.dp), ambientColor = BarShadow, spotColor = BarShadow)
                .clip(RoundedCornerShape(34.dp))
                .background(BarWhite)
                .border(1.dp, BarBorderLight.copy(alpha = 0.9f), RoundedCornerShape(34.dp))
                .onGloballyPositioned { pillW = it.size.width.toFloat(); pillH = it.size.height.toFloat() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tabs row — Home is now first (P logo removed per request), others follow
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home
                PillTabWhite(label = "Home", active = norm == "Home", onClick = onHome, icon = { HomePillIconWhite(active = it) }, modifier = Modifier.weight(1f))
                // Activity
                PillTabWhite(label = "Activity", active = norm == "Activity", onClick = onActivity, icon = { ActivityPillIconWhite(active = it) }, modifier = Modifier.weight(1f))
                // Center placeholder for floating car (keeps spacing of previous bar)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    // floating green car — same as previous white bar's center
                    Box(
                        modifier = Modifier
                            .offset(y = (-10).dp)
                            .size(56.dp)
                            .shadow(10.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.18f), spotColor = PillGreen.copy(alpha = 0.22f))
                            .clip(CircleShape)
                            .background(PillGreen2)
                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            .clickable(onClick = onCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        CarIconWhite(tint = Color.White, iconSize = 22.dp)
                    }
                }
                // Profile (previous label, not Saved)
                PillTabWhite(label = "Profile", active = norm == "Profile", onClick = onProfile, icon = { SavedPillIconWhite(active = it) }, modifier = Modifier.weight(1f))
                // Note: label "Saved" matches jpeg, but onProfile still routes to Profile; keep "Profile" alias if needed:
                // If you prefer "Profile" text, change label back to "Profile"
            }
        }

        // Green stripe tracing bottom + curving up around active tab — was red
        if (pillW > 0f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = pillW
                val h = pillH
                val contentW = w - 12.dp.toPx()
                val tabW = contentW / 4f // 4 slots: Home | Activity | Center | Profile (Home now first, P removed)
                val activeCenterX = 6.dp.toPx() + tabW * animIndex + tabW / 2f

                val bottomY = h - 6.dp.toPx()
                val notchHalf = 34.dp.toPx()
                val hillHalf = 16.dp.toPx() // half width of hill plateau
                val hillTopY = 10.dp.toPx() // below ceiling (top is 0) — green stripe stays below pill top
                val domePeakY = 6.dp.toPx() // hill-like curvy peak, 4dp above plateau edges

                val path = Path().apply {
                    moveTo(18.dp.toPx(), bottomY)
                    lineTo(activeCenterX - notchHalf - 10.dp.toPx(), bottomY)
                    // left smooth ramp to hill edge — curvy, not angular
                    cubicTo(
                        activeCenterX - notchHalf * 0.52f, bottomY,
                        activeCenterX - hillHalf - 10.dp.toPx(), hillTopY,
                        activeCenterX - hillHalf, hillTopY
                    )
                    // hill-like curvy top — not flat, gentle dome with smooth edges
                    quadraticTo(
                        activeCenterX, domePeakY,
                        activeCenterX + hillHalf, hillTopY
                    )
                    // right smooth ramp down
                    cubicTo(
                        activeCenterX + hillHalf + 10.dp.toPx(), hillTopY,
                        activeCenterX + notchHalf * 0.52f, bottomY,
                        activeCenterX + notchHalf + 10.dp.toPx(), bottomY
                    )
                    lineTo(w - 18.dp.toPx(), bottomY)
                }
                // Fade logic: Home fades at start (down part before plateau), Profile fades at end
                val isHome = activeIndex == 0
                val isProfile = activeIndex == 3
                val solidBrush = when {
                    isHome -> Brush.horizontalGradient(
                        colors = listOf(PillGreen.copy(alpha = 0f), PillGreen, PillGreen),
                        startX = 0f,
                        endX = activeCenterX - 6.dp.toPx()
                    )
                    isProfile -> Brush.horizontalGradient(
                        colors = listOf(PillGreen, PillGreen, PillGreen.copy(alpha = 0f)),
                        startX = activeCenterX + 6.dp.toPx(),
                        endX = w
                    )
                    else -> null
                }
                val glowBrush = when {
                    isHome -> Brush.horizontalGradient(
                        colors = listOf(PillGreenGlow.copy(alpha = 0f), PillGreenGlow, PillGreenGlow),
                        startX = 0f,
                        endX = activeCenterX - 6.dp.toPx()
                    )
                    isProfile -> Brush.horizontalGradient(
                        colors = listOf(PillGreenGlow, PillGreenGlow, PillGreenGlow.copy(alpha = 0f)),
                        startX = activeCenterX + 6.dp.toPx(),
                        endX = w
                    )
                    else -> null
                }
                if (glowBrush != null) {
                    drawPath(path = path, brush = glowBrush, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                } else {
                    drawPath(path = path, color = PillGreenGlow, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                if (solidBrush != null) {
                    drawPath(path = path, brush = solidBrush, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                } else {
                    drawPath(path = path, color = PillGreen, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                // tiny green dot under active label (as jpeg but green)
                val dotY = h * 0.74f
                drawCircle(color = PillGreen, radius = 2.2.dp.toPx(), center = Offset(activeCenterX, dotY))
            }
        }
    }
}

@Composable
private fun PillTabWhite(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        icon(active)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            color = if (active) PillGreen else InactiveGray,
            letterSpacing = 0.1.sp
        )
    }
}

// White-theme icons — green when active, soft gray when inactive
@Composable
private fun HomePillIconWhite(active: Boolean, iconSize: Dp = 22.dp) {
    val tint = if (active) PillGreen else InactiveLight
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.42f)
            lineTo(w * 0.86f, h * 0.88f)
            lineTo(w * 0.14f, h * 0.88f)
            lineTo(w * 0.14f, h * 0.42f)
            close()
        }
        if (active) drawPath(path, color = tint)
        else drawPath(path, color = tint, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (!active) {
            val dw = w * 0.22f; val dh = h * 0.26f
            drawRect(color = tint, topLeft = Offset(w * 0.5f - dw / 2, h * 0.88f - dh), size = androidx.compose.ui.geometry.Size(dw, dh), style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun ActivityPillIconWhite(active: Boolean, iconSize: Dp = 22.dp) {
    val tint = if (active) PillGreen else InactiveLight
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height; val cx = w / 2; val cy = h / 2; val r = kotlin.math.min(w, h) * 0.36f; val stroke = 1.5.dp.toPx()
        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawLine(color = tint, start = Offset(cx, cy), end = Offset(cx, cy - r * 0.45f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(cx, cy), end = Offset(cx + r * 0.50f, cy), strokeWidth = stroke * 0.9f, cap = StrokeCap.Round)
        drawCircle(color = tint, radius = 1.2.dp.toPx(), center = Offset(cx, cy))
    }
}

@Composable
private fun SavedPillIconWhite(active: Boolean, iconSize: Dp = 22.dp) {
    val tint = if (active) PillGreen else InactiveLight
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height; val stroke = 1.5.dp.toPx(); val cx = w / 2
        drawCircle(color = tint, radius = w * 0.20f, center = Offset(cx, h * 0.34f), style = Stroke(width = stroke, cap = StrokeCap.Round))
        val p = Path().apply {
            moveTo(w * 0.18f, h * 0.88f)
            cubicTo(w * 0.22f, h * 0.62f, w * 0.30f, h * 0.56f, cx, h * 0.56f)
            cubicTo(w * 0.70f, h * 0.56f, w * 0.78f, h * 0.62f, w * 0.82f, h * 0.88f)
        }
        drawPath(p, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun CarIconWhite(tint: Color, iconSize: Dp) {
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.08f, h * 0.42f)
            lineTo(w * 0.22f, h * 0.28f)
            lineTo(w * 0.32f, h * 0.15f)
            lineTo(w * 0.68f, h * 0.15f)
            lineTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.92f, h * 0.42f)
            lineTo(w * 0.92f, h * 0.70f)
            lineTo(w * 0.08f, h * 0.70f)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color = tint, start = Offset(w * 0.42f, h * 0.18f), end = Offset(w * 0.42f, h * 0.40f), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.58f, h * 0.18f), end = Offset(w * 0.58f, h * 0.40f), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = tint, radius = w * 0.10f, center = Offset(w * 0.28f, h * 0.78f), style = Stroke(width = 1.6.dp.toPx()))
        drawCircle(color = tint, radius = w * 0.10f, center = Offset(w * 0.72f, h * 0.78f), style = Stroke(width = 1.6.dp.toPx()))
    }
}

// Compatibility shims — keep old 5-tab entry point if any screen still calls RidaPillBar
@Composable
fun RidaPillBar(
    selected: String,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCreate: () -> Unit,
    onInbox: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Map 5-tab to 3-tab white theme: Search→Home, Inbox→Activity, Saved→Profile, Create→Center
    val mapped = when (selected) {
        "Inbox" -> "Activity"
        "Saved" -> "Profile"
        "Search", "Create" -> "Home"
        else -> selected
    }
    RidaBottomBar(selected = mapped, onHome = onHome, onActivity = onInbox, onCenter = onCreate, onProfile = onSaved, modifier = modifier)
}
