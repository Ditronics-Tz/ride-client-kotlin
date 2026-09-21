package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.*
import com.example.ridepassenger2.ui.theme.*

@Composable
fun SignInScreen(
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onGoogle: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(14.dp))
            // Status bar is system; we just show content under

            RidaLogo(modifier = Modifier.padding(top = 6.dp))

            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Welcome back",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = RidaTitleDark,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to continue your journey.",
                fontSize = 13.5.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            RidaTextField(
                value = identifier,
                onValueChange = { identifier = it },
                placeholder = "you@example.com or 07XXXXXXXX",
                label = "Email or Phone Number",
                leading = { Text(text = "✉", fontSize = 16.sp, color = RidaPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(14.dp))

            RidaTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                label = "Password",
                leading = { Text(text = "🔒", fontSize = 14.sp, color = RidaPlaceholder) },
                trailing = {
                    Text(
                        text = if (passwordVisible) "🙈" else "👁",
                        fontSize = 16.sp,
                        color = RidaPlaceholder,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(4.dp)
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { rememberMe = !rememberMe }
                        .padding(vertical = 4.dp)
                ) {
                    // Custom checkbox
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (rememberMe) RidaMintDark else Color.Transparent)
                            .then(
                                if (!rememberMe) Modifier.background(
                                    Color.White
                                ).let {
                                    Modifier
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rememberMe) {
                            Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Transparent)
                                )
                            }
                        }
                        if (!rememberMe) {
                            // border
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    // Better border for unchecked
                    if (!rememberMe) {
                        // overlay border effect via background? simple box border
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remember me",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }

                Text(
                    text = "Forgot password?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaMintDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onForgotPassword)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            RidaPrimaryDarkButton(
                text = "Sign In",
                onClick = onSignIn
            )

            Spacer(modifier = Modifier.height(18.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(18.dp))
            RidaGoogleButton(onClick = onGoogle)

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 8.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Sign Up",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaMintDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onSignUp)
                        .padding(horizontal = 2.dp)
                )
            }
            HomeIndicator(color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
