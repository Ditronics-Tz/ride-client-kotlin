package com.example.ridepassenger2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.data.remote.PromoOfferDto
import com.example.ridepassenger2.data.remote.PromoValidationResponse
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.remote.ValidatePromoRequest
import com.example.ridepassenger2.data.repository.RideRepository
import com.example.ridepassenger2.ui.components.HomeIndicator
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.components.RidaLogo
import com.example.ridepassenger2.ui.theme.RIDEPASSENGER2Theme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val PageBg = Color(0xFFF5F8F6)
private val CardBg = Color.White
private val CardBorder = Color(0xFFEEF2F0)
private val InkDark = Color(0xFF111827)
private val TextMuted = Color(0xFF667085)
private val RidaGreen = Color(0xFF00A86B)
private val DarkForestGreen = Color(0xFF063F32)

@Composable
fun PromotionDetailsScreen(
    onBack: () -> Unit = {},
    onApply: (String) -> Unit = {},
    initialCode: String? = null,
    fare: Long? = null
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val liveFareQuote by RideRepository.quoteTzs.collectAsState()
    val effectiveFare = (fare ?: liveFareQuote ?: 1000L).coerceAtLeast(1000L)

    var isLoading by remember { mutableStateOf(true) }
    var availablePromos by remember { mutableStateOf<List<PromoOfferDto>>(emptyList()) }
    var selectedPromo by remember { mutableStateOf<PromoOfferDto?>(null) }
    var promoCodeInput by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    // Fetch live promotions from backend API
    LaunchedEffect(initialCode, effectiveFare) {
        try {
            isLoading = true
            validationMessage = null
            val response = RidaApiClient.api.getAvailablePromos(fare = effectiveFare)
            availablePromos = response.promos

            if (!initialCode.isNullOrBlank()) {
                val matched = availablePromos.firstOrNull { it.code.equals(initialCode, ignoreCase = true) }
                if (matched != null) {
                    selectedPromo = matched
                } else {
                    try {
                        val valResp = RidaApiClient.api.validatePromo(
                            ValidatePromoRequest(code = initialCode, fare = effectiveFare)
                        )
                        if (valResp.valid) {
                            selectedPromo = valResp.promo ?: PromoOfferDto(
                                code = initialCode,
                                description = valResp.message.orEmpty(),
                                discountTzs = valResp.discountAmount,
                                finalFare = valResp.finalFare
                            )
                            isSuccess = true
                            validationMessage = valResp.message ?: "Promo code valid!"
                        }
                    } catch (_: Exception) { }
                }
            } else if (availablePromos.isNotEmpty()) {
                selectedPromo = availablePromos.first()
            }
        } catch (_: Exception) {
            validationMessage = "Could not fetch promotions. Check your network."
        } finally {
            isLoading = false
        }
    }

    fun submitPromoValidation() {
        val code = promoCodeInput.trim().uppercase()
        if (code.isBlank()) {
            validationMessage = "Please enter a promo code"
            isSuccess = false
            return
        }
        keyboardController?.hide()
        scope.launch {
            try {
                isApplying = true
                validationMessage = null
                val response = RidaApiClient.api.validatePromo(
                    ValidatePromoRequest(code = code, fare = effectiveFare)
                )
                if (response.valid) {
                    isSuccess = true
                    selectedPromo = response.promo ?: PromoOfferDto(
                        code = code,
                        description = response.message ?: "Valid promo code",
                        discountTzs = response.discountAmount,
                        finalFare = response.finalFare
                    )
                    validationMessage = response.message ?: "Promo code successfully applied!"
                    onApply(code)
                } else {
                    isSuccess = false
                    validationMessage = response.message ?: "Invalid or expired promo code"
                }
            } catch (e: HttpException) {
                isSuccess = false
                val rawJson = e.response()?.errorBody()?.string()
                val parsedMsg = parsePromoErrorJson(rawJson)
                validationMessage = parsedMsg ?: "Validation error (${e.code()})"
            } catch (_: Exception) {
                isSuccess = false
                validationMessage = "Failed to validate promo code. Please check your connection."
            } finally {
                isApplying = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.CenterStart
                ) {
                    RidaIcon(
                        icon = RidaIcons.Back,
                        contentDescription = "Back",
                        tint = InkDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                RidaLogo(green = RidaGreen)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Promotions & Offers",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "View live discounts or validate promo codes from backend.",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBg)
                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = RidaGreen, modifier = Modifier.size(32.dp))
                            Text("Fetching promotions from server...", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                } else {
                    val promo = selectedPromo
                    if (promo != null) {
                        val discountDisplay = when {
                            promo.discountPercent != null -> "${promo.discountPercent}% off"
                            promo.discountTzs != null -> "TZS ${"%,d".format(promo.discountTzs)} off"
                            promo.discountAmount != null -> "TZS ${"%,d".format(promo.discountAmount)} off"
                            else -> promo.title ?: "Discount Offer"
                        }

                        // Hero Card with Real Backend Data
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkForestGreen)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RidaGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RidaIcon(
                                        icon = RidaIcons.Promotions,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "CODE: ${promo.code.uppercase()}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkForestGreen,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = discountDisplay,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = promo.description.ifBlank { promo.title ?: "Active backend promo offer" },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "OFFER DETAILS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7A88),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        // Real Offer Details Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(CardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            OfferDetailRow("Promo code", promo.code.uppercase())
                            if (promo.maxDiscount != null) {
                                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                OfferDetailRow("Maximum discount", "TZS ${"%,d".format(promo.maxDiscount)}")
                            }
                            if (promo.minFare != null) {
                                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                OfferDetailRow("Minimum fare required", "TZS ${"%,d".format(promo.minFare)}")
                            }
                            if (promo.finalFare != null) {
                                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                OfferDetailRow("Estimated final fare", "TZS ${"%,d".format(promo.finalFare)}")
                            }
                            if (!promo.expiresAt.isNullOrBlank()) {
                                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                OfferDetailRow("Expires at", promo.expiresAt.take(10))
                            }
                        }
                    } else {
                        // Empty State when no promos returned from backend
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkForestGreen)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RidaIcon(
                                icon = RidaIcons.Promotions,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Automatic Promos Active",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Enter a promo code below to check backend eligibility.",
                                fontSize = 13.5.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Available Backend Promos List Selector
                    if (availablePromos.size > 1) {
                        Text(
                            text = "AVAILABLE PROMOTIONS (${availablePromos.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7A88),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availablePromos.forEach { item ->
                                val isSelected = item.code.equals(selectedPromo?.code, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) RidaGreen.copy(alpha = 0.1f) else CardBg,
                                    border = BorderStroke(1.dp, if (isSelected) RidaGreen else CardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(role = Role.Button) {
                                            selectedPromo = item
                                            validationMessage = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = item.code.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                                            Text(text = item.description, fontSize = 12.sp, color = TextMuted)
                                        }
                                        if (isSelected) {
                                            RidaIcon(RidaIcons.Check, contentDescription = "Selected", tint = RidaGreen, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }

                    // Promo Code Validation Input Box
                    Text(
                        text = "VALIDATE PROMO CODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7A88),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBg)
                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = {
                                promoCodeInput = it
                                validationMessage = null
                            },
                            placeholder = { Text("Enter promo code (e.g. RIDA20)", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(color = InkDark, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitPromoValidation() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = InkDark,
                                unfocusedTextColor = InkDark,
                                focusedBorderColor = RidaGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = PageBg,
                                unfocusedContainerColor = PageBg,
                                cursorColor = RidaGreen
                            )
                        )

                        if (!validationMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = validationMessage.orEmpty(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) RidaGreen else Color(0xFFD32F2F)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                }
            }

            // Bottom Apply Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = RidaGreen.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(RidaGreen)
                    .clickable(enabled = !isApplying, onClick = { submitPromoValidation() }),
                contentAlignment = Alignment.Center
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = if (isSuccess) "Applied to Account" else "Validate & Apply Code",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HomeIndicator(color = Color.Black.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun parsePromoErrorJson(json: String?): String? {
    if (json.isNullOrBlank()) return null
    return try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PromoValidationResponse::class.java)
        val resp = adapter.fromJson(json)
        resp?.fields?.get("fare")
            ?: resp?.fields?.get("code")
            ?: resp?.error
            ?: resp?.message
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun OfferDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PromotionDetailsScreenPreview() {
    RIDEPASSENGER2Theme {
        PromotionDetailsScreen()
    }
}
