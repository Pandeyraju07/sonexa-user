package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/apple")
    suspend fun appleSignIn(@Body request: AppleSignInRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/refresh-token")
    fun refreshTokenBlocking(@Body request: RefreshTokenRequest): Call<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponseEnvelope<AuthDataPayload>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponseEnvelope<Void>>

    @POST("auth/logout")
    suspend fun logout(@Query("email") email: String? = null): Response<ApiResponseEnvelope<Void>>

    @DELETE("auth/delete-account")
    suspend fun deleteAccount(): Response<ApiResponseEnvelope<Void>>
}
