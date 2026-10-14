package com.sonexa.app.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Lifecycle-safe Android TextToSpeech wrapper for Zynera Voice Assistant.
 * Provides spoken confirmations (e.g. "Playing Kesariya", "Playback paused", "Sleep timer set").
 */
class ZyneraTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("ZyneraTtsManager", "Failed to instantiate TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.let { engine ->
                // Try to set Indian English or fallback to US
                val inLocale = Locale("en", "IN")
                val res = engine.setLanguage(inLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.language = Locale.US
                }
                engine.setSpeechRate(1.05f)
                engine.setPitch(1.0f)
            }
            pendingSpeech?.let {
                speak(it)
                pendingSpeech = null
            }
        } else {
            Log.w("ZyneraTtsManager", "TTS initialization failed with code: $status")
            isInitialized = false
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (text.isBlank()) return
        if (!isInitialized) {
            pendingSpeech = text
            return
        }

        try {
            val utteranceId = "zynera_utt_${System.currentTimeMillis()}"
            if (onDone != null) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                    override fun onError(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                })
            }
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.85f)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e("ZyneraTtsManager", "Error speaking utterance: $text", e)
            onDone?.invoke()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (_: Exception) {}
    }
}
