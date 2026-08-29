package com.sonexa.app.audio.playback

import android.content.Context
import com.sonexa.app.audio.SonexaEqualizerEngine
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaybackManager(
    context: Context,
    val equalizerEngine: SonexaEqualizerEngine = SonexaEqualizerEngine()
) {
    val nativeProvider = NativeAudioPlaybackProvider(context, equalizerEngine)
    val youtubeProvider = YouTubePlaybackProvider()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _activeProviderType = MutableStateFlow("native_audio")
    val activeProviderType: StateFlow<String> = _activeProviderType.asStateFlow()

    var onTrackEnded: (() -> Unit)? = null

    init {
        nativeProvider.onTrackEnded = { onTrackEnded?.invoke() }
        youtubeProvider.onTrackEnded = { onTrackEnded?.invoke() }
    }

    val engineState: StateFlow<EngineState> = combine(
        _activeProviderType,
        nativeProvider.state,
        youtubeProvider.state
    ) { type, nativeState, ytState ->
        if (type == "youtube_video") ytState else nativeState
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = EngineState()
    )

    fun play(track: TrackDto, startPositionMs: Long = 0L) {
        val hasDirectAudio = track.audioUrl.isNotBlank()
        if (hasDirectAudio) {
            // High-performance native audio playback (ExoPlayer with hardware acceleration)
            _activeProviderType.value = "native_audio"
            youtubeProvider.stop()
            nativeProvider.play(track, startPositionMs)
        } else if (track.isYouTube && track.effectiveVideoId.isNotBlank()) {
            // Fallback for YouTube video tracks only if no direct audio stream exists
            _activeProviderType.value = "youtube_video"
            nativeProvider.stop()
            youtubeProvider.play(track, startPositionMs)
        } else {
            _activeProviderType.value = "native_audio"
            youtubeProvider.stop()
            nativeProvider.play(track, startPositionMs)
        }
    }

    fun togglePlayPause(currentTrack: TrackDto?) {
        val currentIsPlaying = engineState.value.isPlaying
        if (currentIsPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        if (_activeProviderType.value == "youtube_video") {
            youtubeProvider.pause()
        } else {
            nativeProvider.pause()
        }
    }

    fun resume() {
        if (_activeProviderType.value == "youtube_video") {
            youtubeProvider.resume()
        } else {
            nativeProvider.resume()
        }
    }

    fun seekTo(positionMs: Long) {
        if (_activeProviderType.value == "youtube_video") {
            youtubeProvider.seekTo(positionMs)
        } else {
            nativeProvider.seekTo(positionMs)
        }
    }

    fun stop() {
        nativeProvider.stop()
        youtubeProvider.stop()
    }

    fun release() {
        nativeProvider.release()
        youtubeProvider.release()
    }
}
