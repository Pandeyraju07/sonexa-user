package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.api.AppConfigApiService
import com.sonexa.app.data.api.LanguagesCatalogResponse
import com.sonexa.app.data.api.MusicLanguageDto
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.api.SaveLanguagesRequest
import com.sonexa.app.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChooseLanguageUiState {
    object Loading : ChooseLanguageUiState
    data class Ready(
        val title: String,
        val subtitle: String,
        val minSelection: Int,
        val languages: List<MusicLanguageDto>
    ) : ChooseLanguageUiState

    data class Error(val message: String) : ChooseLanguageUiState
}

class ChooseLanguageViewModel(
    private val apiService: AppConfigApiService = RetrofitClient.appConfigApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChooseLanguageUiState>(ChooseLanguageUiState.Loading)
    val uiState: StateFlow<ChooseLanguageUiState> = _uiState.asStateFlow()

    private val _selectedLanguages = MutableStateFlow<List<String>>(emptyList())
    val selectedLanguages: StateFlow<List<String>> = _selectedLanguages.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            _uiState.value = ChooseLanguageUiState.Loading
            try {
                val response = apiService.getLanguages()
                val body = response.body()
                if (response.isSuccessful && body != null && body.languages.isNotEmpty()) {
                    applyCatalog(body)
                } else {
                    applyFallback()
                }
            } catch (_: Exception) {
                applyFallback()
            }
        }
    }

    fun toggleLanguage(name: String) {
        val current = _selectedLanguages.value.toMutableList()
        if (current.contains(name)) {
            val min = (_uiState.value as? ChooseLanguageUiState.Ready)?.minSelection ?: 1
            if (current.size > min) {
                current.remove(name)
            }
        } else {
            current.add(name)
        }
        _selectedLanguages.value = current
    }

    fun saveAndContinue(sessionManager: SessionManager, onSuccess: () -> Unit) {
        val selected = _selectedLanguages.value
        val min = (_uiState.value as? ChooseLanguageUiState.Ready)?.minSelection ?: 1
        if (selected.size < min) return

        viewModelScope.launch {
            _isSaving.value = true
            sessionManager.preferredLanguages = selected
            try {
                val response = apiService.saveLanguages(SaveLanguagesRequest(selected))
                // Navigate even if backend fails — preferences are already local
                if (!response.isSuccessful || response.body()?.success != true) {
                    // keep local save
                }
            } catch (_: Exception) {
                // Offline / mock path — local prefs already stored
            } finally {
                _isSaving.value = false
                onSuccess()
            }
        }
    }

    private fun applyCatalog(body: LanguagesCatalogResponse) {
        _uiState.value = ChooseLanguageUiState.Ready(
            title = body.title.ifBlank { "Choose Music Languages" },
            subtitle = body.subtitle.ifBlank { "Select languages you love to listen to" },
            minSelection = body.minSelection.coerceAtLeast(1),
            languages = body.languages
        )
        val defaults = body.defaultSelected.ifEmpty {
            listOfNotNull(body.languages.getOrNull(0)?.name, body.languages.getOrNull(1)?.name)
        }
        _selectedLanguages.value = defaults
    }

    private fun applyFallback() {
        val fallback = listOf(
            MusicLanguageDto("en", "English", "International"),
            MusicLanguageDto("hi", "Hindi", "हिंदी"),
            MusicLanguageDto("pa", "Punjabi", "ਪੰਜਾਬੀ"),
            MusicLanguageDto("ta", "Tamil", "தமிழ்"),
            MusicLanguageDto("te", "Telugu", "తెలుగు"),
            MusicLanguageDto("es", "Spanish", "Español"),
            MusicLanguageDto("ko", "K-Pop", "한국어"),
            MusicLanguageDto("fr", "French", "Français"),
            MusicLanguageDto("ja", "Japanese", "日本語"),
            MusicLanguageDto("de", "German", "Deutsch")
        )
        _uiState.value = ChooseLanguageUiState.Ready(
            title = "Choose Music Languages",
            subtitle = "Select languages you love to listen to",
            minSelection = 1,
            languages = fallback
        )
        _selectedLanguages.value = listOf("English", "Hindi")
    }
}
