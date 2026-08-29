package com.sonexa.app.audio.playback

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.StateFlow

data class EngineState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)

interface PlaybackProvider {
    val providerType: String
    val state: StateFlow<EngineState>

    fun play(track: TrackDto, startPositionMs: Long = 0L)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun stop()
    fun release()
}
