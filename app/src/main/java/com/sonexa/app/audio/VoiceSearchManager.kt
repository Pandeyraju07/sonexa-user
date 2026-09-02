package com.sonexa.app.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class VoiceSearchUiState {
    object Idle : VoiceSearchUiState()
    object RequestingPermission : VoiceSearchUiState()
    data class Listening(val partialText: String = "") : VoiceSearchUiState()
    data class Processing(val finalText: String) : VoiceSearchUiState()
    data class Success(val transcript: String) : VoiceSearchUiState()
    data class Error(val message: String, val canRetry: Boolean = true, val needsSystemDialogFallback: Boolean = false) : VoiceSearchUiState()
}

class VoiceSearchManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _uiState = MutableStateFlow<VoiceSearchUiState>(VoiceSearchUiState.Idle)
    val uiState: StateFlow<VoiceSearchUiState> = _uiState.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private var isListening = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languageCode: String = "en-IN") {
        mainHandler.post {
            try {
                destroyInternal()

                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _uiState.value = VoiceSearchUiState.Error(
                        message = "Device speech service unavailable. Tap below to use system voice dialog.",
                        needsSystemDialogFallback = true
                    )
                    return@post
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        _uiState.value = VoiceSearchUiState.Listening("")
                    }

                    override fun onBeginningOfSpeech() {
                        _uiState.value = VoiceSearchUiState.Listening("")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                        _rmsLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        val current = _uiState.value
                        if (current is VoiceSearchUiState.Listening && current.partialText.isNotBlank()) {
                            _uiState.value = VoiceSearchUiState.Processing(current.partialText)
                        }
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        _rmsLevel.value = 0f
                        val (message, needsFallback) = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error" to false
                            SpeechRecognizer.ERROR_CLIENT -> "Voice service busy. Tap below to search." to true
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required" to false
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue during recognition" to false
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Voice network timeout" to false
                            SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't catch that. Please speak again." to false
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy. Tap to retry." to true
                            SpeechRecognizer.ERROR_SERVER -> "Server recognition error. Tap to retry." to true
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to speak." to false
                            else -> "Recognition issue ($error). Tap to retry." to true
                        }
                        _uiState.value = VoiceSearchUiState.Error(
                            message = message,
                            canRetry = true,
                            needsSystemDialogFallback = needsFallback
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        _rmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val transcript = matches?.firstOrNull()?.trim() ?: ""
                        if (transcript.isNotBlank()) {
                            _uiState.value = VoiceSearchUiState.Success(transcript)
                        } else {
                            _uiState.value = VoiceSearchUiState.Error("No speech detected. Please try again.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _uiState.value = VoiceSearchUiState.Listening(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
                _uiState.value = VoiceSearchUiState.Listening("")
            } catch (e: Exception) {
                _uiState.value = VoiceSearchUiState.Error(
                    message = "Voice search issue: ${e.message ?: "Please try system voice."}",
                    needsSystemDialogFallback = true
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            if (isListening) {
                try {
                    speechRecognizer?.stopListening()
                } catch (_: Exception) {}
                isListening = false
            }
        }
    }

    fun reset() {
        stopListening()
        _uiState.value = VoiceSearchUiState.Idle
        _rmsLevel.value = 0f
    }

    private fun destroyInternal() {
        stopListening()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    fun destroy() {
        mainHandler.post {
            destroyInternal()
        }
    }
}