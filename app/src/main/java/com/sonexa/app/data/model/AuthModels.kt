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

data class CheckEmailRequest(
    @SerializedName("email") val email: String
)

data class CheckEmailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("exists") val exists: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CheckEmailData? = null
) {
    val isRegistered: Boolean
        get() = exists || (data?.exists == true)
}

data class CheckEmailData(
    @SerializedName("email") val email: String? = null,
    @SerializedName("exists") val exists: Boolean = false
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

data class TokenPayload(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long? = null
)

data class UserProfileDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("handle") val handle: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("profilePicUrl") val profilePicUrl: String = "",
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("isEmailVerified") val isEmailVerified: Boolean = false,
    @SerializedName("followersCount") val followersCount: Int = 0,
    @SerializedName("followingCount") val followingCount: Int = 0
) {
    val resolvedId: String
        get() = id.ifBlank { userId ?: mongoId ?: "" }
}

data class AuthDataPayload(
    @SerializedName("token") val token: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("tokens") val tokens: TokenPayload? = null,
    @SerializedName("user") val user: UserProfileDto? = null,
    @SerializedName("otpSent") val otpSent: Boolean = false,
    @SerializedName("otp") val otp: String? = null,
    @SerializedName("emailDelivered") val emailDelivered: Boolean? = null
) {
    val resolvedToken: String?
        get() = token
            ?: accessToken
            ?: tokens?.accessToken
            ?: tokens?.token

    val resolvedRefreshToken: String?
        get() = refreshToken
            ?: tokens?.refreshToken
}

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
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("tokens") val tokens: TokenPayload? = null,
    @SerializedName("user") val user: UserProfileDto? = null,
    @SerializedName("data") val dataPayload: AuthDataPayload? = null
) {
    val resolvedToken: String?
        get() = token
            ?: accessToken
            ?: tokens?.accessToken
            ?: tokens?.token
            ?: dataPayload?.resolvedToken

    val resolvedRefreshToken: String?
        get() = refreshToken
            ?: tokens?.refreshToken
            ?: dataPayload?.resolvedRefreshToken

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
