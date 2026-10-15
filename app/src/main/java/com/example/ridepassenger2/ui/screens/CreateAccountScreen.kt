package com.example.ridepassenger2.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.ridepassenger2.R
import com.example.ridepassenger2.data.local.AuthValidation
import com.example.ridepassenger2.data.local.GoogleAuthManager
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.remote.RegisterRequest
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.repository.UserRepository
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import kotlinx.coroutines.launch

private val RidaPrimary = Color(0xFF00C878)
private val RidaForest = Color(0xFF063F32)
private val RidaActionEnd = Color(0xFF008F64)
private val RidaInk = Color(0xFF111827)
private val RidaSecondary = Color(0xFF667085)
private val RidaHint = Color(0xFF98A6A0)
private val RidaMintWash = Color(0xFFF4FBF8)
private val RidaField = Color(0xFFFAFCFB)
private val RidaFieldBorder = Color(0xFFDCE8E3)
private val RidaDivider = Color(0xFFE5EAE7)

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onGoogle: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var pwError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun doCreate() {
        if (isLoading) return
        nameError = AuthValidation.nameError(fullName)
        phoneError = AuthValidation.identifierError(phone)
        pwError = AuthValidation.passwordError(password)
        if (nameError != null || phoneError != null || pwError != null) return
        val handle = email.trim().ifBlank { phone.trim() }
        scope.launch {
            isLoading = true
            formError = null
            try {
                val regReq = RegisterRequest(
                    name = fullName.trim(),
                    email = email.trim().ifBlank { "${phone.trim().replace(" ", "")}@rida.tz" },
                    password = password
                )
                val sessionResp = RidaApiClient.api.register(regReq)
                val user = sessionResp.user
                SessionManager.save(
                    context = context,
                    name = user.name.ifBlank { fullName.trim() },
                    handle = handle,
                    phone = user.phone ?: phone.trim(),
                    email = user.email ?: email.trim(),
                    accessToken = sessionResp.getEffectiveAccessToken(),
                    refreshToken = sessionResp.getEffectiveRefreshToken()
                )
                UserRepository.refreshSessionFromApi(context)
                onCreateAccount()
            } catch (e: Exception) {
                formError = e.message ?: "Could not create account. Try again."
            } finally {
                isLoading = false
            }
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
            // Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RidaLogo(green = RidaPrimary)
            }

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val artworkWidth = (maxWidth * 0.43f).coerceAtMost(168.dp)
                if (maxWidth < 350.dp) {
                    Column {
                        CreateAccountCopy(modifier = Modifier.fillMaxWidth())
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
                        CreateAccountCopy(modifier = Modifier.weight(1f))
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
                    text = "Full Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaInk
                )
                Spacer(Modifier.height(4.dp))
                AuthTextField(
                    value = fullName,
                    onValueChange = { fullName = it; nameError = null },
                    placeholder = "Your full name",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = RidaHint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = nameError != null
                )
                AuthError(nameError)

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Phone Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaInk
                )
                Spacer(Modifier.height(4.dp))
                AuthTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    placeholder = "+255 7XX XXX XXX",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = RidaHint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    isError = phoneError != null
                )
                AuthError(phoneError)

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Email ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaInk
                )
                Spacer(Modifier.height(4.dp))
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = RidaHint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RidaInk
                )
                Spacer(Modifier.height(4.dp))
                AuthTextField(
                    value = password,
                    onValueChange = { password = it; pwError = null },
                    placeholder = "Create a password",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = RidaHint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = RidaSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    onImeAction = ::doCreate,
                    isError = pwError != null
                )
                AuthError(pwError)
                AuthError(formError)

                Spacer(Modifier.height(14.dp))
                GradientPrimaryButton(
                    text = "Create Account",
                    enabled = !isLoading,
                    loading = isLoading,
                    onClick = ::doCreate
                )

                Spacer(Modifier.height(12.dp))
                AuthOrDivider()
                Spacer(Modifier.height(12.dp))
                GoogleSignInButton(
                    onClick = {
                        if (isLoading) return@GoogleSignInButton
                        scope.launch {
                            isLoading = true
                            try {
                                val result = GoogleAuthManager.signIn(context)
                                if (result.isSuccess) {
                                    val googleUser = result.getOrNull()!!
                                    SessionManager.saveGoogleSession(context, googleUser)
                                    onGoogle()
                                } else {
                                    val err = result.exceptionOrNull()
                                    if (err !is GetCredentialCancellationException) {
                                        Toast.makeText(
                                            context,
                                            "Google Sign-In failed: ${err?.localizedMessage ?: "Unknown error"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "By creating an account, you agree to our",
                        fontSize = 12.sp,
                        color = RidaSecondary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terms of Service",
                            fontSize = 12.sp,
                            color = RidaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    showTerms = true
                                    onTerms()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(text = " and ", fontSize = 12.sp, color = RidaSecondary)
                        Text(
                            text = "Privacy Policy",
                            fontSize = 12.sp,
                            color = RidaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    showPrivacy = true
                                    onPrivacy()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
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
                    text = "Already have an account? ",
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
                        .clickable(onClick = onBack)
                        .heightIn(min = 48.dp)
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                )
            }
        }
    }
    if (showTerms) TermsDialog(onDismiss = { showTerms = false })
    if (showPrivacy) PrivacyDialog(onDismiss = { showPrivacy = false })
}

@Composable
private fun CreateAccountCopy(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Create account",
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
            color = RidaInk,
            letterSpacing = (-0.6).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Join a community that moves together.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
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
private fun AuthOrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = RidaDivider)
        Text(
            text = "OR",
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 13.sp,
            color = RidaSecondary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = RidaDivider)
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = RidaInk,
            disabledContentColor = RidaSecondary
        ),
        border = BorderStroke(1.dp, RidaFieldBorder)
    ) {
        GoogleMark(Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoogleMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.google_g),
        contentDescription = "Google",
        modifier = modifier
    )
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
fun CreateAccountScreenPreview() {
    RIDEPASSENGER2Theme {
        CreateAccountScreen(
            onBack = {},
            onCreateAccount = {}
        )
    }
}
