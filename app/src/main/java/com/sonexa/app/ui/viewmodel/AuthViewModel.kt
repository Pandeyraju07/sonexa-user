package com.sonexa.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.model.UserProfileDto
import com.sonexa.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(
        val message: String,
        val user: UserProfileDto? = null,
        val otp: String? = null,
        val emailDelivered: Boolean = true
    ) : AuthUiState
    data class Error(val errorMessage: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository.create(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfileDto?>(null)
    val currentUser: StateFlow<UserProfileDto?> = _currentUser.asStateFlow()

    private val _pendingOtpEmail = MutableStateFlow(repository.getPendingOtpEmail().orEmpty())
    val pendingOtpEmail: StateFlow<String> = _pendingOtpEmail.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { response ->
                    val user = response.resolvedUser
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(response.message ?: "Login Successful", user)
                    onSuccess()
                },
                onFailure = { error ->
                    val message = error.localizedMessage ?: "Login failed"
                    _uiState.value = AuthUiState.Error(message)
                    if (message.contains("verify your email", ignoreCase = true) ||
                        message.contains("OTP has been sent", ignoreCase = true)
                    ) {
                        _pendingOtpEmail.value = email.trim().lowercase()
                    }
                }
            )
        }
    }

    fun register(email: String, name: String, pass: String, phone: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.register(email, name, pass, phone)
            result.fold(
                onSuccess = { response ->
                    _pendingOtpEmail.value = email.trim().lowercase()
                    _uiState.value = AuthUiState.Success(
                        message = response.message
                            ?: "Verification OTP sent to ${email.trim().lowercase()}",
                        otp = response.otp,
                        emailDelivered = response.emailDelivered != false
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Registration failed")
                }
            )
        }
    }

    fun sendOtp(email: String, purpose: String = "REGISTER", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.sendOtp(email, purpose)
            result.fold(
                onSuccess = { response ->
                    _pendingOtpEmail.value = email.trim().lowercase()
                    _uiState.value = AuthUiState.Success(
                        message = response.message
                            ?: "Verification OTP sent to ${email.trim().lowercase()}",
                        otp = response.otp,
                        emailDelivered = response.emailDelivered != false
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to send OTP")
                }
            )
        }
    }

    fun verifyOtp(email: String, otp: String, purpose: String = "REGISTER", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.verifyOtp(email, otp, purpose)
            result.fold(
                onSuccess = { response ->
                    val user = response.resolvedUser
                    _currentUser.value = user
                    _pendingOtpEmail.value = ""
                    _uiState.value = AuthUiState.Success(response.message ?: "OTP Verified", user)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Verification failed")
                }
            )
        }
    }

    fun googleSignIn(
        idToken: String,
        email: String? = null,
        name: String? = null,
        profilePicUrl: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.googleSignIn(idToken, email, name, profilePicUrl)
            result.fold(
                onSuccess = { response ->
                    val user = response.resolvedUser
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(response.message ?: "Google Sign In Successful", user)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Google Sign In failed")
                }
            )
        }
    }

    fun appleSignIn(
        identityToken: String,
        email: String? = null,
        name: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.appleSignIn(identityToken, email, name)
            result.fold(
                onSuccess = { response ->
                    val user = response.resolvedUser
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(response.message ?: "Apple Sign In Successful", user)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Apple Sign In failed")
                }
            )
        }
    }

    fun forgotPassword(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.forgotPassword(email)
            result.fold(
                onSuccess = { response ->
                    _pendingOtpEmail.value = email.trim().lowercase()
                    _uiState.value = AuthUiState.Success(
                        message = response.message
                            ?: "Password reset OTP sent to ${email.trim().lowercase()}",
                        otp = response.otp,
                        emailDelivered = response.emailDelivered != false
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to request password reset")
                }
            )
        }
    }

    fun resetPassword(email: String, otp: String, newPass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.resetPassword(email, otp, newPass)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = AuthUiState.Success(response.message ?: "Password reset successful")
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to reset password")
                }
            )
        }
    }

    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout(_currentUser.value?.email)
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
            onSuccess()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
