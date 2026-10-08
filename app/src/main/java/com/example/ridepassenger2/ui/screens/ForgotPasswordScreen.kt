package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.components.RidaPrimaryDarkButton
import com.example.ridepassenger2.ui.components.RidaTextField
import com.example.ridepassenger2.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendReset: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", fontSize = 20.sp, color = RidaTitleDark, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            RidaLogo(green = RidaGreen28)

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Forgot your password?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RidaTitleDark,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No worries. Enter your email or phone\nnumber and we'll send you a reset link.",
                fontSize = 13.5.sp,
                color = Color(0xFF6B7280),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            RidaTextField(
                value = identifier,
                onValueChange = { identifier = it; error = null; sent = false },
                placeholder = "you@example.com or 07XXXXXXXX",
                label = "Email or Phone Number",
                leading = { Text(text = "✉", fontSize = 16.sp, color = RidaPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                background = RidaInputBg,
                borderColor = RidaBorderLight
            )
            if (error != null) Text(
                text = error!!, fontSize = 12.sp, color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 6.dp)
            )

            if (sent) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = "✓ Reset link sent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check ${identifier.trim()} for the reset link. It expires in 30 minutes.",
                            fontSize = 12.5.sp, color = Color(0xFF047857), lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            RidaPrimaryDarkButton(
                text = if (sent) "Resend Link" else "Send Reset Link",
                onClick = {
                    error = com.example.ridepassenger2.data.local.AuthValidation.identifierError(identifier)
                    if (error == null) {
                        sent = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to Sign In",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackToSignIn)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            HomeIndicator(color = Color.Black)
        }
    }
}
