package com.example.ridepassenger2.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---------------------------------------------------------------------------
// Auth & Session Models
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String? = "passenger"
)

@JsonClass(generateAdapter = false)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = false)
data class GoogleChallengeResponse(
    val challengeId: String,
    val nonce: String,
    val expiresAt: String
)

@JsonClass(generateAdapter = false)
data class GoogleExchangeRequest(
    val idToken: String,
    val challengeId: String
)

@JsonClass(generateAdapter = false)
data class RefreshRequest(
    val refreshToken: String
)

@JsonClass(generateAdapter = false)
data class UserDto(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val status: String? = null,
    @param:Json(name = "is_active") val isActive: Boolean? = true,
    @param:Json(name = "phone_verified_at") val phoneVerifiedAt: String? = null,
    @param:Json(name = "suspended_until") val suspendedUntil: String? = null,
    val profileCompleted: Boolean? = null,
    @param:Json(name = "profile_completed") val profileCompletedSnake: Boolean? = null
) {
    fun isProfileComplete(): Boolean? = profileCompleted ?: profileCompletedSnake
}

@JsonClass(generateAdapter = false)
data class SessionResponse(
    val user: UserDto,
    val accessToken: String? = null,
    val expiresIn: Long? = null,
    val refreshToken: String? = null,
    val sessionId: String? = null,
    val onboardingRequired: List<String>? = emptyList(),
    val restricted: Boolean? = false,
    val message: String? = null,
    val profileCompleted: Boolean? = null,
    @param:Json(name = "profile_completed") val profileCompletedSnake: Boolean? = null,
    // snake_case aliases for compatibility
    val token: String? = null,
    @param:Json(name = "access_token") val accessTokenSnake: String? = null,
    @param:Json(name = "refresh_token") val refreshTokenSnake: String? = null,
    @param:Json(name = "expires_in") val expiresInSnake: Long? = null
) {
    fun getEffectiveAccessToken(): String = accessToken ?: accessTokenSnake ?: token ?: ""
    fun getEffectiveRefreshToken(): String = refreshToken ?: refreshTokenSnake ?: ""
    fun isProfileComplete(): Boolean? = profileCompleted ?: profileCompletedSnake ?: user.isProfileComplete()
}

@JsonClass(generateAdapter = false)
data class MeResponse(
    val user: UserDto,
    val onboardingRequired: List<String>? = emptyList(),
    val profileCompleted: Boolean? = null,
    @param:Json(name = "profile_completed") val profileCompletedSnake: Boolean? = null
) {
    fun isProfileComplete(): Boolean? = profileCompleted ?: profileCompletedSnake ?: user.isProfileComplete()
}

// ---------------------------------------------------------------------------
// OTP & Phone Verification
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class RequestOtpRequest(
    val phone: String
)

@JsonClass(generateAdapter = false)
data class VerifyOtpRequest(
    val code: String
)

@JsonClass(generateAdapter = false)
data class OtpResponse(
    val message: String? = null,
    @param:Json(name = "expires_in") val expiresIn: Int? = null
)

// ---------------------------------------------------------------------------
// Profile & Preferences
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class UpdateProfileRequest(
    @param:Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = false)
data class PassengerPreferencesDto(
    @param:Json(name = "quiet_ride") val quietRide: Boolean? = false,
    @param:Json(name = "extra_pickup_time") val extraPickupTime: Boolean? = false,
    @param:Json(name = "share_trip_status") val shareTripStatus: Boolean? = false,
    @param:Json(name = "precise_location") val preciseLocation: Boolean? = true,
    @param:Json(name = "ride_updates") val rideUpdates: Boolean? = true,
    @param:Json(name = "trip_messages") val tripMessages: Boolean? = true,
    @param:Json(name = "promotions") val promotions: Boolean? = false,
    @param:Json(name = "email_enabled") val emailEnabled: Boolean? = true,
    @param:Json(name = "sms_enabled") val smsEnabled: Boolean? = true,
    @param:Json(name = "push_enabled") val pushEnabled: Boolean? = true,
    val language: String? = "en"
)

@JsonClass(generateAdapter = false)
data class SavedPlaceDto(
    val id: String? = null,
    val kind: String, // home, work, custom
    val label: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

@JsonClass(generateAdapter = false)
data class SavedPlacesResponse(
    val places: List<SavedPlaceDto> = emptyList()
)

// ---------------------------------------------------------------------------
// Rides & Fare Quotes
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class PointDto(
    val lat: Double,
    val lng: Double,
    val address: String
)

@JsonClass(generateAdapter = false)
data class RideQuoteRequest(
    val pickup: PointDto,
    val dropoff: PointDto,
    @param:Json(name = "vehicle_type") val vehicleType: String = "car"
)

@JsonClass(generateAdapter = false)
data class RideQuoteResponse(
    @param:Json(name = "estimated_fare_tzs") val estimatedFareTzs: Long,
    @param:Json(name = "estimated_distance_meters") val estimatedDistanceMeters: Long,
    @param:Json(name = "estimated_duration_seconds") val estimatedDurationSeconds: Long,
    @param:Json(name = "distance_basis") val distanceBasis: String? = "straight_line",
    val currency: String = "TZS",
    @param:Json(name = "payment_method") val paymentMethod: String = "cash"
)

@JsonClass(generateAdapter = false)
data class RideCreateRequest(
    val pickup: PointDto,
    val dropoff: PointDto,
    @param:Json(name = "vehicle_type") val vehicleType: String = "car"
)

@JsonClass(generateAdapter = false)
data class DriverInfoDto(
    val id: String,
    val name: String,
    val phone: String? = null,
    @param:Json(name = "vehicle_type") val vehicleType: String? = null,
    @param:Json(name = "plate_number") val plateNumber: String? = null,
    val rating: Double? = null
)

@JsonClass(generateAdapter = false)
data class RideResponse(
    val id: String,
    val status: String,
    val passenger: UserDto? = null,
    val driver: DriverInfoDto? = null,
    val pickup: PointDto,
    val dropoff: PointDto,
    @param:Json(name = "vehicle_type") val vehicleType: String? = "car",
    @param:Json(name = "estimated_fare") val estimatedFare: Long? = null,
    @param:Json(name = "final_fare") val finalFare: Long? = null,
    @param:Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class RideListResponse(
    val rides: List<RideResponse> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0
)

@JsonClass(generateAdapter = false)
data class DriverLocationResponse(
    val lat: Double,
    val lng: Double,
    val bearing: Float? = null,
    @param:Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = false)
data class CancelRideRequest(
    val reason: String = "Passenger changed plans"
)

@JsonClass(generateAdapter = false)
data class RateRideRequest(
    val score: Int,
    val comment: String? = null
)

// ---------------------------------------------------------------------------
// Scheduled Rides
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class ScheduledRideRequest(
    val pickup: PointDto,
    val dropoff: PointDto,
    @param:Json(name = "vehicle_type") val vehicleType: String = "car",
    @param:Json(name = "scheduled_for") val scheduledFor: String // RFC3339
)

@JsonClass(generateAdapter = false)
data class ScheduledRideResponse(
    val id: String,
    val status: String, // upcoming, dispatched, cancelled, failed
    val pickup: PointDto,
    val dropoff: PointDto,
    @param:Json(name = "vehicle_type") val vehicleType: String = "car",
    @param:Json(name = "scheduled_for") val scheduledFor: String,
    @param:Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class ScheduledRideListResponse(
    val schedules: List<ScheduledRideResponse> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0
)

// ---------------------------------------------------------------------------
// Support, Emergency & SOS
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class CreateTicketRequest(
    val subject: String,
    val description: String,
    val category: String = "ride"
)

@JsonClass(generateAdapter = false)
data class TicketDto(
    val id: String,
    val subject: String,
    val description: String,
    val category: String,
    val status: String,
    @param:Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class TicketListResponse(
    val tickets: List<TicketDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0
)

@JsonClass(generateAdapter = false)
data class CreateTicketMessageRequest(
    val body: String
)

@JsonClass(generateAdapter = false)
data class TicketMessageDto(
    val id: String,
    val body: String,
    val sender: String? = null,
    @param:Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class TicketMessageListResponse(
    val messages: List<TicketMessageDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0
)

@JsonClass(generateAdapter = false)
data class EmergencyContactDto(
    val id: String? = null,
    val name: String,
    val phone: String,
    val relationship: String? = "Family"
)

@JsonClass(generateAdapter = false)
data class EmergencyContactsResponse(
    val contacts: List<EmergencyContactDto> = emptyList()
)

@JsonClass(generateAdapter = false)
data class SosRequest(
    val lat: Double? = null,
    val lng: Double? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = false)
data class SosResponse(
    val id: String,
    val status: String, // active, resolved
    @param:Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class ShareLinkResponse(
    val token: String,
    @param:Json(name = "share_url") val shareUrl: String
)

// ---------------------------------------------------------------------------
// Promos & Referral
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class PromoOfferDto(
    val code: String,
    val description: String,
    @param:Json(name = "discount_percent") val discountPercent: Int? = null,
    @param:Json(name = "discount_tzs") val discountTzs: Long? = null,
    @param:Json(name = "expires_at") val expiresAt: String? = null
)

@JsonClass(generateAdapter = false)
data class PromoListResponse(
    val promos: List<PromoOfferDto> = emptyList()
)

@JsonClass(generateAdapter = false)
data class ValidatePromoRequest(
    val code: String
)

@JsonClass(generateAdapter = false)
data class PromoValidationResponse(
    val valid: Boolean,
    val code: String,
    @param:Json(name = "discount_amount") val discountAmount: Long? = 0,
    val message: String? = null
)

@JsonClass(generateAdapter = false)
data class ReferralResponse(
    val code: String,
    @param:Json(name = "share_text") val shareText: String? = null,
    @param:Json(name = "total_referred") val totalReferred: Int = 0,
    @param:Json(name = "total_earned_tzs") val totalEarnedTzs: Long = 0
)

@JsonClass(generateAdapter = false)
data class ApplyReferralRequest(
    val code: String
)

// ---------------------------------------------------------------------------
// Account Privacy
// ---------------------------------------------------------------------------

@JsonClass(generateAdapter = false)
data class DeletionRequestResponse(
    val id: String? = null,
    val status: String, // pending, processing, completed, cancelled
    @param:Json(name = "requested_at") val requestedAt: String? = null,
    @param:Json(name = "scheduled_for") val scheduledFor: String? = null
)

@JsonClass(generateAdapter = false)
data class ExportRequestResponse(
    val id: String,
    val status: String, // pending, processing, ready, expired
    @param:Json(name = "download_url") val downloadUrl: String? = null,
    @param:Json(name = "expires_at") val expiresAt: String? = null
)
