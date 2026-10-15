package com.example.ridepassenger2

import com.example.ridepassenger2.data.remote.LoginRequest
import com.example.ridepassenger2.data.remote.MeResponse
import com.example.ridepassenger2.data.remote.RegisterRequest
import com.example.ridepassenger2.data.remote.UserDto
import com.example.ridepassenger2.ui.common.NameRules
import com.example.ridepassenger2.ui.common.TzPhone
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFlowModelsTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun registrationRequestContainsOnlyAuthenticationFieldsAndRole() {
        val json = moshi.adapter(RegisterRequest::class.java).toJson(RegisterRequest("rider@example.com", "secret123"))

        assertTrue(json.contains("\"email\":\"rider@example.com\""))
        assertTrue(json.contains("\"password\":\"secret123\""))
        assertTrue(json.contains("\"role\":\"passenger\""))
        assertFalse(json.contains("\"name\""))
        assertFalse(json.contains("\"fullName\""))
        assertFalse(json.contains("\"phone\""))
    }

    @Test
    fun loginRequestUsesEmailAndPasswordOnly() {
        val json = moshi.adapter(LoginRequest::class.java).toJson(LoginRequest("rider@example.com", "secret123"))

        assertTrue(json.contains("\"email\":\"rider@example.com\""))
        assertTrue(json.contains("\"password\":\"secret123\""))
        assertFalse(json.contains("\"identifier\""))
        assertFalse(json.contains("\"phone\""))
    }

    @Test
    fun tanzaniaPhoneNumbersNormalizeToE164AndRejectMalformedInput() {
        assertEquals("+255712345678", TzPhone.normalize("0712 345 678"))
        assertEquals("+255712345678", TzPhone.normalize("+255 (712) 345-678"))
        assertEquals("+255712345678", TzPhone.normalize("712345678"))
        assertEquals("+255712345678", TzPhone.normalize("255712345678"))
        assertNull(TzPhone.normalize("0712 345 67"))
        assertNull(TzPhone.normalize("abc0712345678"))
    }

    @Test
    fun profileNameRulesAcceptUnicodeAndEnforceLengthAfterTrimming() {
        assertNull(NameRules.error("  Asha Juma  "))
        assertNull(NameRules.error("Amina Mwakyusa"))
        assertNull(NameRules.error("Asha O’Connor"))
        assertTrue(NameRules.error(" A ") != null)
        assertTrue(NameRules.error("a".repeat(51)) != null)
    }

    @Test
    fun profileResponseAllowsMissingNameAndPhone() {
        val user = moshi.adapter(UserDto::class.java).fromJson("""{"id":"user-1","email":"rider@example.com"}""")!!

        assertNull(user.name)
        assertNull(user.phone)
    }

    @Test
    fun profileCompletionFlagSupportsSnakeCaseAndCamelCaseResponses() {
        val adapter = moshi.adapter(MeResponse::class.java)

        val snake = adapter.fromJson("""{"user":{"id":"user-1"},"profile_completed":false}""")!!
        val camel = adapter.fromJson("""{"user":{"id":"user-1"},"profileCompleted":true}""")!!

        assertFalse(snake.isProfileComplete()!!)
        assertTrue(camel.isProfileComplete()!!)
    }
}
