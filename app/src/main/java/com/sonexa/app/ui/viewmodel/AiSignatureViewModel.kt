package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.model.AiSignatureResponse
import com.sonexa.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiSignatureUiState {
    object Idle : AiSignatureUiState
    object Generating : AiSignatureUiState
    data class Success(val response: AiSignatureResponse) : AiSignatureUiState
    data class Error(val message: String) : AiSignatureUiState
}

class AiSignatureViewModel(private val repository: AiRepository = AiRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<AiSignatureUiState>(AiSignatureUiState.Idle)
    val uiState: StateFlow<AiSignatureUiState> = _uiState.asStateFlow()

    fun generateSignature(mood: String, prompt: String, detectedEmotion: String = "") {
        viewModelScope.launch {
            _uiState.value = AiSignatureUiState.Generating
            val result = repository.generateAiSignature(mood, prompt, detectedEmotion)
            result.fold(
                onSuccess = { res ->
                    _uiState.value = AiSignatureUiState.Success(res)
                },
                onFailure = { err ->
                    _uiState.value = AiSignatureUiState.Error(err.localizedMessage ?: "AI Generation failed")
                }
            )
        }
    }
}
