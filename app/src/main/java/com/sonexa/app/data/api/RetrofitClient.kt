package com.sonexa.app.data.api

import com.sonexa.app.BuildConfig
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object RetrofitClient {
    private val baseUrl: String
        get() {
            val configured = BuildConfig.API_BASE_URL.trim()
            return if (configured.endsWith("/")) configured else "$configured/"
        }

    @Volatile
    private var sessionManager: SessionManager? = null
    private val refreshing = AtomicBoolean(false)

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val requestIdInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("X-Request-Id", java.util.UUID.randomUUID().toString())
            .build()
        chain.proceed(request)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        if (isAuthPath(original)) {
            return@Interceptor chain.proceed(original)
        }
        val token = sessionManager?.accessToken
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val tokenAuthenticator = Authenticator { _: Route?, response: Response ->
        if (responseCount(response) >= 2) return@Authenticator null
        if (isAuthPath(response.request)) return@Authenticator null
        val refresh = sessionManager?.refreshToken ?: return@Authenticator null
        synchronized(this) {
            if (!refreshing.compareAndSet(false, true)) {
                return@synchronized null
            }
            try {
                val newAccess = refreshAccessToken(refresh) ?: return@synchronized null
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            } finally {
                refreshing.set(false)
            }
        }
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(requestIdInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    val gson: com.google.gson.Gson by lazy {
        com.google.gson.GsonBuilder()
            .registerTypeAdapter(com.sonexa.app.data.model.TrackDto::class.java, SafeTrackDtoDeserializer())
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val appConfigApiService: AppConfigApiService by lazy {
        retrofit.create(AppConfigApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val musicApiService: MusicApiService by lazy {
        retrofit.create(MusicApiService::class.java)
    }

    val aiApiService: AiApiService by lazy {
        retrofit.create(AiApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    private fun isAuthPath(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/auth/login")
                || path.contains("/auth/register")
                || path.contains("/auth/refresh-token")
                || path.contains("/auth/send-otp")
                || path.contains("/auth/verify-otp")
                || path.contains("/auth/forgot-password")
                || path.contains("/auth/reset-password")
                || path.contains("/auth/google")
                || path.contains("/auth/apple")
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val refreshClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val refreshRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = refreshRetrofit.create(AuthApiService::class.java)
            val response = service.refreshTokenBlocking(RefreshTokenRequest(refreshToken)).execute()
            val payload = response.body()?.data
            val access = payload?.token
            if (response.isSuccessful && !access.isNullOrBlank()) {
                sessionManager?.updateTokens(access, payload.refreshToken ?: refreshToken)
                access
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
