package com.example.ridepassenger2.data.local

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.ridepassenger2.data.remote.GoogleExchangeRequest
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

object GoogleAuthManager {

    const val WEB_CLIENT_ID = "173520268894-mupr409lfujnfl09os01cartf94vr0eh.apps.googleusercontent.com"
    const val DEBUG_SHA1 = "B4:D4:08:9B:12:A7:EB:51:29:2A:D9:16:76:39:14:14:F0:F3:BD:5F"

    data class GoogleUserResult(
        val idToken: String,
        val displayName: String,
        val email: String,
        val profilePictureUrl: String?
    )

    suspend fun signIn(context: Context): Result<GoogleUserResult> {
        val credentialManager = CredentialManager.create(context)

        var challengeId: String? = null
        var challengeNonce: String? = null

        try {
            val challengeResp = RidaApiClient.api.googleChallenge()
            challengeId = challengeResp.challengeId
            challengeNonce = challengeResp.nonce
        } catch (e: Exception) {
            Log.w("GoogleAuthManager", "Could not obtain backend Google challenge: ${e.message}")
        }

        val googleIdOptionBuilder = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)

        if (!challengeNonce.isNullOrBlank()) {
            googleIdOptionBuilder.setNonce(challengeNonce)
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOptionBuilder.build())
            .build()

        return try {
            val response = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                    val profilePic = googleIdTokenCredential.profilePictureUri?.toString()

                    var finalAccessToken: String? = null
                    var finalRefreshToken: String? = null

                    if (!challengeId.isNullOrBlank()) {
                        try {
                            val exchangeResp = RidaApiClient.api.googleExchange(
                                GoogleExchangeRequest(
                                    idToken = idToken,
                                    challengeId = challengeId
                                )
                            )
                            finalAccessToken = exchangeResp.getEffectiveAccessToken()
                            finalRefreshToken = exchangeResp.getEffectiveRefreshToken()
                        } catch (e: Exception) {
                            Log.e("GoogleAuthManager", "Backend Google exchange failed", e)
                        }
                    }

                    val userResult = GoogleUserResult(
                        idToken = finalAccessToken ?: idToken,
                        displayName = displayName,
                        email = email,
                        profilePictureUrl = profilePic
                    )

                    // Persist session locally
                    SessionManager.save(
                        context = context,
                        name = displayName,
                        handle = "@${email.substringBefore("@")}",
                        email = email,
                        accessToken = finalAccessToken,
                        refreshToken = finalRefreshToken,
                        profilePictureUrl = profilePic
                    )

                    Result.success(userResult)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleAuthManager", "Invalid ID token response", e)
                    Result.failure(e)
                }
            } else {
                Result.failure(IllegalStateException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuthManager", "User cancelled Google Sign-In")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "Credential error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }
}
