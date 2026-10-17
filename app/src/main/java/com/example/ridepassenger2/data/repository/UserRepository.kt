package com.example.ridepassenger2.data.repository

import android.content.Context
import com.example.ridepassenger2.data.local.SessionManager
import com.example.ridepassenger2.data.remote.RidaApiClient
import com.example.ridepassenger2.data.remote.UpdateProfileRequest
import com.example.ridepassenger2.data.remote.RidaApiErrors
import kotlinx.coroutines.flow.first

object UserRepository {

    suspend fun refreshSessionFromApi(context: Context) {
        try {
            val me = RidaApiClient.api.me()
            val user = me.user
            val handle = user.email?.substringBefore("@")?.let { "@$it" }
                ?: user.phone?.let { "@${it.takeLast(4)}" }
                ?: ""
            SessionManager.save(
                context = context,
                name = user.name.orEmpty(),
                handle = handle,
                phone = user.phone ?: "",
                email = user.email ?: "",
                profileCompleted = me.isProfileComplete()
            )
        } catch (_: Exception) { }

        try {
            val profile = RidaApiClient.api.getProfile()
            ProfilePhotoContract.photoUrl(profile, RidaApiClient.BASE_URL)?.let {
                SessionManager.updateProfilePicture(context, it)
            }
            val fullName = (profile["full_name"] as? String)
                ?: (profile["name"] as? String)
            val phone = profile["phone"] as? String
            val email = profile["email"] as? String
            if (!fullName.isNullOrBlank() || !phone.isNullOrBlank() || !email.isNullOrBlank()) {
                val current = SessionManager.observe(context).first()
                SessionManager.updateProfile(
                    context,
                    fullName ?: current.name,
                    phone ?: current.phone,
                    email ?: current.email
                )
            }
        } catch (_: Exception) { }
    }

    suspend fun updateProfileOnServer(context: Context, fullName: String): Result<Unit> {
        return try {
            RidaApiClient.api.updateProfile(UpdateProfileRequest(fullName = fullName))
            val current = SessionManager.observe(context).first()
            SessionManager.updateProfile(context, fullName, current.phone, current.email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException(RidaApiErrors.message(e, "Could not save your profile. Try again."), e))
        }
    }
}
