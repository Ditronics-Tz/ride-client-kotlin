package com.example.ridepassenger2.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.R
import com.example.ridepassenger2.data.local.AuthValidation
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme

private val RidaPrimary = Color(0xFF00C878)
private val RidaForest = Color(0xFF063F32)
private val RidaActionEnd = Color(0xFF008F64)
private val RidaInk = Color(0xFF111827)
private val RidaSecondary = Color(0xFF667085)
private val RidaHint = Color(0xFF98A6A0)
private val RidaMintWash = Color(0xFFF4FBF8)
private val RidaField = Color(0xFFFAFCFB)
private val RidaFieldBorder = Color(0xFFDCE8E3)

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendReset: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }

    fun doSendReset() {
        error = AuthValidation.identifierError(identifier)
        if (error == null) {
            sent = true
            onSendReset()
        }
    }

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            // Back button + Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    RidaIcon(
                        icon = RidaIcons.Back,
                        contentDescription = "Back",
                        tint = RidaInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                RidaLogo(green = RidaPrimary)
            }

            Spacer(Modifier.height(20.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val artworkWidth = (maxWidth * 0.43f).coerceAtMost(168.dp)
                if (maxWidth < 350.dp) {
                    Column {
                        ForgotPasswordCopy(modifier = Modifier.fillMaxWidth())
                        RideIllustration(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForgotPasswordCopy(modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        RideIllustration(
                            modifier = Modifier
                                .width(artworkWidth)
                                .height(126.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Email or Phone Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaInk
                )
                Spacer(Modifier.height(4.dp))
                AuthTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        error = null
                        sent = false
                    },
                    placeholder = "you@example.com or 07XXXXXXXX",
                    leadingIcon = {
                        RidaIcon(
                            icon = RidaIcons.Mail,
                            contentDescription = null,
                            tint = RidaHint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    onImeAction = ::doSendReset,
                    isError = error != null
                )
                AuthError(error)

                if (sent) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFECFDF5))
                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RidaIcon(RidaIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF065F46))
                                Text(text = "Reset link sent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Check ${identifier.trim()} for the reset link. It expires in 30 minutes.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF047857),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                GradientPrimaryButton(
                    text = if (sent) "Resend Link" else "Send Reset Link",
                    onClick = ::doSendReset
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remembered your password? ",
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
                        .clickable(onClick = onBackToSignIn)
                        .heightIn(min = 48.dp)
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordCopy(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Forgot password?",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = RidaInk,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "No worries. Enter your email or phone number and we'll send you a reset link.",
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = RidaSecondary
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    onImeAction: (() -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        placeholder = {
            Text(text = placeholder, color = RidaHint, fontSize = 13.sp, maxLines = 1)
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingContent,
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone = { onImeAction?.invoke() }),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RidaInk,
            unfocusedTextColor = RidaInk,
            focusedContainerColor = RidaField,
            unfocusedContainerColor = RidaField,
            focusedBorderColor = RidaPrimary,
            unfocusedBorderColor = RidaFieldBorder,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color(0xFFD92D20),
            cursorColor = RidaPrimary,
            focusedPlaceholderColor = RidaHint,
            unfocusedPlaceholderColor = RidaHint
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
    )
}

@Composable
private fun AuthError(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = Color(0xFFD92D20),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
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
        RidaIcon(
            icon = RidaIcons.Forward,
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
fun ForgotPasswordScreenPreview() {
    RIDEPASSENGER2Theme {
        ForgotPasswordScreen(
            onBack = {},
            onSendReset = {},
            onBackToSignIn = {}
        )
    }
}
