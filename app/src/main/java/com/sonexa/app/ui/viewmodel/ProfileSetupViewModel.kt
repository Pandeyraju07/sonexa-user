package com.sonexa.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.api.AppConfigApiService
import com.sonexa.app.data.api.ProfileCreateRequest
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileSetupUiState {
    object Idle : ProfileSetupUiState
    object Loading : ProfileSetupUiState
    data class Success(val message: String) : ProfileSetupUiState
    data class Error(val message: String) : ProfileSetupUiState
}

class ProfileSetupViewModel(
    application: Application,
    private val apiService: AppConfigApiService = RetrofitClient.appConfigApiService
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val _uiState = MutableStateFlow<ProfileSetupUiState>(ProfileSetupUiState.Idle)
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun createProfile(displayName: String, handle: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ProfileSetupUiState.Loading
            if (displayName.isNotBlank()) {
                sessionManager.userName = displayName.trim()
            }
            try {
                val formattedHandle = if (handle.isNotBlank()) {
                    if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}"
                } else "@zynera_user"
                val response = apiService.createProfile(ProfileCreateRequest(displayName.trim(), formattedHandle))
                if (response.isSuccessful) {
                    _uiState.value = ProfileSetupUiState.Success(response.body()?.message ?: "Profile created")
                    onSuccess()
                } else {
                    _uiState.value = ProfileSetupUiState.Error("Failed to save profile")
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = ProfileSetupUiState.Error(e.localizedMessage ?: "Error saving profile")
                onSuccess()
            }
        }
    }
}
