package com.sonexa.app.data.api

import com.sonexa.app.BuildConfig
import com.sonexa.app.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val baseUrl: String
        get() {
            val configured = BuildConfig.API_BASE_URL.trim()
            return if (configured.endsWith("/")) configured else "$configured/"
        }

    @Volatile
    private var sessionManager: SessionManager? = null

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Avoid logging request bodies (passwords / tokens) in Logcat
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
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

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        // Real backend only — mock fallback was hiding admin-uploaded tracks
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
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
}
