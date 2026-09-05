package com.sonexa.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String? = null
)

data class SendOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("purpose") val purpose: String? = "REGISTER"
)

data class OtpVerifyRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("purpose") val purpose: String? = "REGISTER"
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String = "",
    @SerializedName("newPassword") val newPassword: String
)

data class GoogleSignInRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("profilePicUrl") val profilePicUrl: String? = null
)

data class AppleSignInRequest(
    @SerializedName("identityToken") val identityToken: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class UserProfileDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("handle") val handle: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("profilePicUrl") val profilePicUrl: String = "",
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("isEmailVerified") val isEmailVerified: Boolean = false,
    @SerializedName("followersCount") val followersCount: Int = 0,
    @SerializedName("followingCount") val followingCount: Int = 0
)

data class AuthDataPayload(
    @SerializedName("token") val token: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: UserProfileDto? = null,
    @SerializedName("otpSent") val otpSent: Boolean = false,
    @SerializedName("otp") val otp: String? = null,
    @SerializedName("emailDelivered") val emailDelivered: Boolean? = null
)

data class ErrorEnvelope(
    @SerializedName("errCode") val errCode: String? = null,
    @SerializedName("errDesc") val errDesc: String? = null,
    @SerializedName("field") val field: String? = null
)

data class ApiResponseEnvelope<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: ErrorEnvelope? = null
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: UserProfileDto? = null,
    @SerializedName("data") val dataPayload: AuthDataPayload? = null
) {
    val resolvedToken: String?
        get() = token ?: dataPayload?.token

    val resolvedUser: UserProfileDto?
        get() = user ?: dataPayload?.user
}

data class GenericApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: ErrorEnvelope? = null,
    @SerializedName("otp") val otp: String? = null,
    @SerializedName("emailDelivered") val emailDelivered: Boolean? = null
)
