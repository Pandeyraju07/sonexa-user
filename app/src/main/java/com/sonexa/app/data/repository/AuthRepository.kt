package com.sonexa.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.sonexa.app.data.api.AuthApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository(
    private val apiService: AuthApiService = RetrofitClient.authApiService,
    private val sessionManager: SessionManager? = null
) {

    private val gson = Gson()

    companion object {
        fun create(context: Context): AuthRepository {
            val session = SessionManager.getInstance(context)
            RetrofitClient.init(session)
            return AuthRepository(sessionManager = session)
        }
    }

    suspend fun login(email: String, pass: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email.trim(), pass))
            parseAuthResponse(response, persistSession = true)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun register(
        email: String,
        name: String,
        pass: String,
        phone: String? = null
    ): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(email.trim(), name.trim(), pass, phone?.trim()))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                sessionManager?.pendingOtpEmail = email.trim().lowercase()
                val body = response.body()!!
                val delivered = body.data?.emailDelivered ?: true
                Result.success(
                    GenericApiResponse(
                        success = true,
                        message = body.message ?: "Verification OTP sent to ${email.trim().lowercase()}",
                        otp = null,
                        emailDelivered = delivered
                    )
                )
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun sendOtp(email: String, purpose: String = "REGISTER"): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendOtp(SendOtpRequest(email.trim(), purpose))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                sessionManager?.pendingOtpEmail = email.trim().lowercase()
                val body = response.body()!!
                val delivered = body.data?.emailDelivered ?: true
                Result.success(
                    GenericApiResponse(
                        success = true,
                        message = body.message ?: "Verification OTP sent to ${email.trim().lowercase()}",
                        otp = null,
                        emailDelivered = delivered
                    )
                )
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun verifyOtp(email: String, otp: String, purpose: String = "REGISTER"): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(OtpVerifyRequest(email.trim(), otp.trim(), purpose))
            parseAuthResponse(response, persistSession = true)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun googleSignIn(
        idToken: String,
        email: String? = null,
        name: String? = null,
        profilePicUrl: String? = null
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.googleSignIn(GoogleSignInRequest(idToken, email, name, profilePicUrl))
            parseAuthResponse(response, persistSession = true)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun appleSignIn(
        identityToken: String,
        email: String? = null,
        name: String? = null
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.appleSignIn(AppleSignInRequest(identityToken, email, name))
            parseAuthResponse(response, persistSession = true)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            parseAuthResponse(response, persistSession = true)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun forgotPassword(email: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                val body = response.body()!!
                Result.success(
                    GenericApiResponse(
                        success = true,
                        message = body.message ?: "Password reset OTP sent to ${email.trim().lowercase()}",
                        otp = body.data?.otp,
                        emailDelivered = body.data?.emailDelivered ?: true
                    )
                )
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPass: String): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetPassword(ResetPasswordRequest(email.trim(), otp.trim(), newPass))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(GenericApiResponse(true, response.body()!!.message))
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    suspend fun logout(email: String? = null): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            apiService.logout(email)
            sessionManager?.clearSession()
            Result.success(GenericApiResponse(true, "Logged out successfully"))
        } catch (e: Exception) {
            sessionManager?.clearSession()
            Result.success(GenericApiResponse(true, "Logged out successfully"))
        }
    }

    suspend fun deleteAccount(): Result<GenericApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful) {
                sessionManager?.clearSession()
                Result.success(GenericApiResponse(true, "Account deleted successfully"))
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyNetworkError(e), e))
        }
    }

    fun getPendingOtpEmail(): String? = sessionManager?.pendingOtpEmail

    private fun parseAuthResponse(
        response: Response<ApiResponseEnvelope<AuthDataPayload>>,
        persistSession: Boolean
    ): Result<LoginResponse> {
        val body = response.body()
        if (response.isSuccessful && body != null && body.success) {
            val payload = body.data
            val loginResponse = LoginResponse(
                success = true,
                message = body.message,
                token = payload?.token,
                refreshToken = payload?.refreshToken,
                user = payload?.user,
                dataPayload = payload
            )
            if (persistSession && !payload?.token.isNullOrBlank()) {
                sessionManager?.saveSession(
                    accessToken = payload?.token,
                    refreshToken = payload?.refreshToken,
                    userId = payload?.user?.id,
                    email = payload?.user?.email,
                    name = payload?.user?.name
                )
                sessionManager?.pendingOtpEmail = null
            }
            return Result.success(loginResponse)
        }
        return Result.failure(Exception(parseErrorMessage(response)))
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val envelope = gson.fromJson(errorBody, ApiResponseEnvelope::class.java)
                envelope?.error?.errDesc ?: envelope?.message ?: "Request failed (${response.code()})"
            } else {
                val bodyMessage = (response.body() as? ApiResponseEnvelope<*>)?.let {
                    it.error?.errDesc ?: it.message
                }
                bodyMessage ?: response.message().ifEmpty { "Request failed (${response.code()})" }
            }
        } catch (e: Exception) {
            "Request failed (${response.code()})"
        }
    }

    private fun friendlyNetworkError(e: Exception): String {
        return when (e) {
            is ConnectException, is UnknownHostException ->
                "Cannot reach Zynera server. Check that the backend is running and network connection is active."
            is SocketTimeoutException ->
                "Server took too long to respond. Please try again."
            is IOException ->
                e.localizedMessage?.takeIf { it.isNotBlank() } ?: "Network error. Please try again."
            else -> e.localizedMessage?.takeIf { it.isNotBlank() } ?: "Something went wrong"
        }
    }
}
