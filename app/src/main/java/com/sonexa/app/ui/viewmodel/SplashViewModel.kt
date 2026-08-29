package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.api.AppConfigApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.api.SplashConfigResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashUiState {
    object Loading : SplashUiState
    data class Success(val config: SplashConfigResponse) : SplashUiState
    data class Error(val message: String) : SplashUiState
}

class SplashViewModel(private val apiService: AppConfigApiService = RetrofitClient.appConfigApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAppConfig()
    }

    fun checkAppConfig() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            try {
                val response = apiService.getSplashConfig()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = SplashUiState.Success(response.body()!!)
                } else {
                    _uiState.value = SplashUiState.Error("Config check failed")
                }
            } catch (e: Exception) {
                _uiState.value = SplashUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}
