package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.local.AuthValidation
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.components.RidaPrimaryDarkButton
import com.example.ridepassenger2.ui.components.RidaTextField
import com.example.ridepassenger2.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var pwError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun doCreate() {
        nameError = AuthValidation.nameError(fullName)
        phoneError = AuthValidation.identifierError(phone)
        pwError = AuthValidation.passwordError(password)
        if (nameError != null || phoneError != null || pwError != null) return
        val handle = email.trim().ifBlank { phone.trim() }
        scope.launch {
            SessionManager.save(context, fullName.trim(), handle)
            onCreateAccount()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RidaLightSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            // Back button
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

            Spacer(modifier = Modifier.height(6.dp))
            RidaLogo(green = RidaMintAlt)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create your account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RidaDark,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Join a community that moves together.",
                fontSize = 13.5.sp,
                color = RidaMuted2,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(20.dp))

            RidaTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = null },
                placeholder = "Your full name",
                label = "Full Name",
                leading = { Text(text = "👤", fontSize = 14.sp, color = RidaPlaceholder) },
                background = RidaInputBg2,
                borderColor = RidaBorder2
            )
            if (nameError != null) Text(
                text = nameError!!, fontSize = 12.sp, color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            RidaTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = null },
                placeholder = "+255 7XX XXX XXX",
                label = "Phone Number",
                leading = { Text(text = "📞", fontSize = 14.sp, color = RidaPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                background = RidaInputBg2,
                borderColor = RidaBorder2
            )
            if (phoneError != null) Text(
                text = phoneError!!, fontSize = 12.sp, color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            RidaTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "you@example.com",
                label = "Email (optional)",
                leading = { Text(text = "✉", fontSize = 14.sp, color = RidaPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                background = RidaInputBg2,
                borderColor = RidaBorder2
            )
            Spacer(modifier = Modifier.height(12.dp))
            RidaTextField(
                value = password,
                onValueChange = { password = it; pwError = null },
                placeholder = "Create a password",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                background = RidaInputBg2,
                borderColor = RidaBorder2
            )
            if (pwError != null) Text(
                text = pwError!!, fontSize = 12.sp, color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))
            RidaPrimaryDarkButton(
                text = "Create Account",
                onClick = { doCreate() }
            )

            Spacer(modifier = Modifier.height(120.dp)) // space for footer
        }

        // Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(RidaLightSurface)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val annotated = buildAnnotatedString {
                append("By creating an account, you agree to our\n")
                pushStringAnnotation(tag = "terms", annotation = "terms")
                withStyle(style = SpanStyle(color = RidaMintAlt, fontWeight = FontWeight.Medium)) {
                    append("Terms of Service")
                }
                pop()
                append(" and ")
                pushStringAnnotation(tag = "privacy", annotation = "privacy")
                withStyle(style = SpanStyle(color = RidaMintAlt, fontWeight = FontWeight.Medium)) {
                    append("Privacy Policy")
                }
                pop()
            }
            Text(
                text = annotated,
                fontSize = 12.sp,
                color = Color(0xFF7A8783),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.clickable(enabled = false) {} // annotate clicks via separate? keep simple
            )
            Spacer(modifier = Modifier.height(12.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
