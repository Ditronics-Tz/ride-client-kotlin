package com.example.ridepassenger2.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.R
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

private val RidaPrimary = Color(0xFF00C878)
private val RidaForest = Color(0xFF063F32)
private val RidaActionEnd = Color(0xFF008F64)
private val RidaInk = Color(0xFF111827)
private val RidaSecondary = Color(0xFF667085)
private val RidaMintWash = Color(0xFFF4FBF8)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onAlreadyHaveAccount: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LowerRoadAccent(modifier = Modifier.align(Alignment.BottomEnd))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Spacer(Modifier.height(14.dp))
                RidaLogo(modifier = Modifier.padding(top = 4.dp), green = RidaPrimary)
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Better rides.",
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = RidaInk,
                    letterSpacing = (-0.6).sp
                )
                Text(
                    text = "Together.",
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = RidaPrimary,
                    letterSpacing = (-0.6).sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Request a ride, share the journey, and move smarter.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = RidaSecondary
                )
            }

            // Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                RideIllustration(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }

            // Bottom Actions Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                GradientPrimaryButton(
                    text = "Get Started",
                    onClick = onGetStarted
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "I already have an account? ",
                        fontSize = 14.sp,
                        color = RidaSecondary
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RidaPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onAlreadyHaveAccount)
                            .heightIn(min = 48.dp)
                            .padding(vertical = 8.dp, horizontal = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GradientPrimaryButton(
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "primary-button-scale"
    )
    val shape = RoundedCornerShape(18.dp)
    val colors = if (enabled) listOf(RidaForest, RidaActionEnd) else listOf(RidaForest.copy(alpha = 0.55f), RidaActionEnd.copy(alpha = 0.55f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .shadow(if (enabled) 7.dp else 0.dp, shape, ambientColor = RidaPrimary.copy(alpha = 0.13f), spotColor = RidaPrimary.copy(alpha = 0.22f))
            .clip(shape)
            .background(Brush.horizontalGradient(colors))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(21.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RideIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_ride_illustration),
        contentDescription = "Rida ride illustration",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun LowerRoadAccent(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 168.dp, height = 142.dp)) {
        val curve = Path().apply {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            cubicTo(size.width * 0.32f, size.height * 0.72f, size.width * 0.55f, size.height * 0.78f, size.width, 0f)
            close()
        }
        drawPath(curve, RidaMintWash.copy(alpha = 0.9f))
        val roadMark = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.88f)
            cubicTo(size.width * 0.56f, size.height * 0.77f, size.width * 0.7f, size.height * 0.58f, size.width * 0.88f, size.height * 0.26f)
        }
        drawPath(
            roadMark,
            Color.White.copy(alpha = 0.85f),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())))
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreenPreview() {
    RIDEPASSENGER2Theme {
        OnboardingScreen(
            onGetStarted = {},
            onAlreadyHaveAccount = {}
        )
    }
}
