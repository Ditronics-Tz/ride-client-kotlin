package com.example.ridepassenger2.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RidaApi {

    // ---------------------------------------------------------------------------
    // Public Health & Auth
    // ---------------------------------------------------------------------------

    @GET("health")
    suspend fun getHealth(): Map<String, Any>

    @GET("ready")
    suspend fun getReady(): Response<Unit>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): SessionResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): SessionResponse

    @POST("auth/google/challenge")
    suspend fun googleChallenge(): GoogleChallengeResponse

    @POST("auth/google")
    suspend fun googleExchange(@Body request: GoogleExchangeRequest): SessionResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): SessionResponse

    // ---------------------------------------------------------------------------
    // User & Phone Verification
    // ---------------------------------------------------------------------------

    @GET("me")
    suspend fun me(): MeResponse

    @GET("auth/me")
    suspend fun authMe(): SessionResponse

    @POST("auth/logout")
    suspend fun logout(): Map<String, Any>

    @POST("auth/logout-all")
    suspend fun logoutAll(): Map<String, Any>

    @GET("auth/sessions")
    suspend fun getSessions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Map<String, Any>

    @DELETE("auth/sessions/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: String): Response<Unit>

    @POST("auth/phone/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): OtpResponse

    @POST("auth/phone/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): MeResponse

    // ---------------------------------------------------------------------------
    // Passenger Profile & Preferences
    // ---------------------------------------------------------------------------

    @GET("passenger/profile/me")
    suspend fun getProfile(): Map<String, Any>

    @PUT("passenger/profile/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Map<String, Any>

    @Multipart
    @POST("passenger/profile/me/image")
    suspend fun uploadProfileImage(
        @Part profilePicture: MultipartBody.Part
    ): Map<String, Any>

    @GET("passenger/preferences")
    suspend fun getPreferences(): PassengerPreferencesDto

    @PATCH("passenger/preferences")
    suspend fun updatePreferences(@Body preferences: Map<String, Any?>): PassengerPreferencesDto

    @GET("passenger/saved-places")
    suspend fun getSavedPlaces(): SavedPlacesResponse

    @POST("passenger/saved-places")
    suspend fun createSavedPlace(@Body place: SavedPlaceDto): SavedPlaceDto

    @PUT("passenger/saved-places/{id}")
    suspend fun updateSavedPlace(
        @Path("id") id: String,
        @Body place: SavedPlaceDto
    ): SavedPlaceDto

    @DELETE("passenger/saved-places/{id}")
    suspend fun deleteSavedPlace(@Path("id") id: String): Response<Unit>

    // ---------------------------------------------------------------------------
    // Rides & Fare Quotes
    // ---------------------------------------------------------------------------

    @POST("rides/quote")
    suspend fun getQuote(@Body request: RideQuoteRequest): RideQuoteResponse

    @POST("rides")
    suspend fun createRide(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: RideCreateRequest
    ): RideResponse

    @GET("rides")
    suspend fun getRides(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("status") status: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): RideListResponse

    @GET("rides/{rideId}")
    suspend fun getRide(@Path("rideId") rideId: String): RideResponse

    @GET("rides/{rideId}/driver-location")
    suspend fun getDriverLocation(@Path("rideId") rideId: String): Response<DriverLocationResponse>

    @POST("rides/{rideId}/cancel")
    suspend fun cancelRide(
        @Path("rideId") rideId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CancelRideRequest
    ): RideResponse

    @POST("rides/{rideId}/rating")
    suspend fun rateRide(
        @Path("rideId") rideId: String,
        @Body request: RateRideRequest
    ): Map<String, Any>

    // ---------------------------------------------------------------------------
    // Scheduled Rides
    // ---------------------------------------------------------------------------

    @POST("scheduled-rides")
    suspend fun createScheduledRide(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ScheduledRideRequest
    ): ScheduledRideResponse

    @GET("scheduled-rides")
    suspend fun getScheduledRides(
        @Query("status") status: String? = "upcoming",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ScheduledRideListResponse

    @GET("scheduled-rides/{scheduleId}")
    suspend fun getScheduledRide(@Path("scheduleId") scheduleId: String): ScheduledRideResponse

    @POST("scheduled-rides/{scheduleId}/cancel")
    suspend fun cancelScheduledRide(@Path("scheduleId") scheduleId: String): Map<String, Any>

    // ---------------------------------------------------------------------------
    // Support & Safety
    // ---------------------------------------------------------------------------

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body request: CreateTicketRequest): TicketDto

    @GET("support/tickets")
    suspend fun getSupportTickets(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TicketListResponse

    @GET("support/tickets/{ticketId}")
    suspend fun getSupportTicket(@Path("ticketId") ticketId: String): TicketDto

    @GET("support/tickets/{ticketId}/messages")
    suspend fun getTicketMessages(
        @Path("ticketId") ticketId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TicketMessageListResponse

    @POST("support/tickets/{ticketId}/messages")
    suspend fun sendTicketMessage(
        @Path("ticketId") ticketId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateTicketMessageRequest
    ): TicketMessageDto

    @GET("emergency-contacts")
    suspend fun getEmergencyContacts(): EmergencyContactsResponse

    @POST("emergency-contacts")
    suspend fun createEmergencyContact(@Body contact: EmergencyContactDto): EmergencyContactDto

    @PUT("emergency-contacts/{id}")
    suspend fun updateEmergencyContact(
        @Path("id") id: String,
        @Body contact: EmergencyContactDto
    ): EmergencyContactDto

    @DELETE("emergency-contacts/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: String): Response<Unit>

    @POST("rides/{rideId}/sos")
    suspend fun triggerSos(
        @Path("rideId") rideId: String,
        @Body request: SosRequest
    ): SosResponse

    @POST("rides/{rideId}/share")
    suspend fun shareRide(@Path("rideId") rideId: String): ShareLinkResponse

    // ---------------------------------------------------------------------------
    // Promos & Referrals
    // ---------------------------------------------------------------------------

    @GET("promos/available")
    suspend fun getAvailablePromos(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): PromoListResponse

    @POST("promos/validate")
    suspend fun validatePromo(@Body request: ValidatePromoRequest): PromoValidationResponse

    @GET("referral/me")
    suspend fun getReferral(): ReferralResponse

    @POST("referral/apply")
    suspend fun applyReferral(@Body request: ApplyReferralRequest): Map<String, Any>

    // ---------------------------------------------------------------------------
    // Account Privacy
    // ---------------------------------------------------------------------------

    @POST("account/deletion-request")
    suspend fun requestAccountDeletion(): DeletionRequestResponse

    @GET("account/deletion-request")
    suspend fun getAccountDeletionStatus(): DeletionRequestResponse

    @DELETE("account/deletion-request")
    suspend fun cancelAccountDeletion(): Response<Unit>

    @POST("account/export-request")
    suspend fun requestAccountExport(): ExportRequestResponse
}
