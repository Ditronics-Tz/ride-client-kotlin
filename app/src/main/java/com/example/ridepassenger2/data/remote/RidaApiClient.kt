package com.example.ridepassenger2.data.remote

import android.content.Context
import com.example.ridepassenger2.data.local.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

object RidaApiClient {

    val BASE_URL = com.example.ridepassenger2.BuildConfig.API_BASE_URL

    private var appContext: Context? = null
    private var deviceId: String = UUID.randomUUID().toString()
    private val refreshMutex = Mutex()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences("rida_device_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("device_id", null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
        }
        deviceId = id ?: UUID.randomUUID().toString()
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    private val headersInterceptor = Interceptor { chain ->
        val origRequest = chain.request()
        val builder = origRequest.newBuilder()
            .header("Accept", origRequest.header("Accept") ?: "application/json")
            .header("X-Device-ID", deviceId)
            .header("X-Device-Platform", "android")

        // Attach bearer token if available and request doesn't already have one
        if (origRequest.header("Authorization") == null) {
            val token = appContext?.let { ctx ->
                runBlocking {
                    SessionManager.observe(ctx).firstOrNull()?.accessToken
                }
            }
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        chain.proceed(builder.build())
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            // Prevent infinite retry loop if refresh itself returns 401
            if (response.request.url.encodedPath.contains("auth/refresh")) {
                return null
            }

            if (response.priorResponse?.code == 401) return null
            val ctx = appContext ?: return null

            return runBlocking {
                refreshMutex.withLock {
                    val currentSession = SessionManager.observe(ctx).firstOrNull()
                    val latest = currentSession?.accessToken
                    if (!latest.isNullOrBlank() && response.request.header("Authorization") != "Bearer $latest") {
                        return@withLock response.request.newBuilder().header("Authorization", "Bearer $latest").build()
                    }
                    val refreshToken = currentSession?.refreshToken

                    if (refreshToken.isNullOrBlank()) {
                        return@withLock null
                    }

                    try {
                        val unauthClient = OkHttpClient.Builder().build()
                        val refreshRetrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(unauthClient)
                            .addConverterFactory(MoshiConverterFactory.create(moshi))
                            .build()
                        val authApi = refreshRetrofit.create(RidaApi::class.java)

                        val refreshResponse = authApi.refresh(RefreshRequest(refreshToken))
                        val newAccess = refreshResponse.getEffectiveAccessToken()
                        val newRefresh = refreshResponse.getEffectiveRefreshToken()

                        if (newAccess.isNotBlank()) {
                            SessionManager.updateTokens(ctx, newAccess, newRefresh)
                            return@withLock response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        } else {
                            SessionManager.clear(ctx)
                            null
                        }
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 401 && SessionManager.observe(ctx).firstOrNull()?.refreshToken == refreshToken) {
                            SessionManager.clear(ctx)
                        }
                        null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val publicPhotoClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followSslRedirects(false)
            .build()
    }

    /** Backend photos use the same bearer token and refresh handling as API calls. */
    fun profilePhotoCall(url: String): Call {
        val request = Request.Builder().url(url).header("Accept", "image/*").build()
        val backend = BASE_URL.toHttpUrl()
        val sameBackend = request.url.scheme == "https" &&
            request.url.host == backend.host && request.url.port == backend.port
        return (if (sameBackend) okHttpClient else publicPhotoClient).newCall(request)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    }

    val api: RidaApi by lazy { retrofit.create(RidaApi::class.java) }
    val transport: TransportApi by lazy { retrofit.create(TransportApi::class.java) }

    fun generateIdempotencyKey(): String = UUID.randomUUID().toString()
}
