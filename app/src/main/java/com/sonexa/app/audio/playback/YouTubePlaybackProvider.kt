package com.sonexa.app.audio.playback

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class YouTubePlaybackProvider : PlaybackProvider {

    override val providerType: String = "youtube_video"

    private val _state = MutableStateFlow(EngineState())
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    enum class CommandType {
        LOAD_VIDEO,
        PLAY,
        PAUSE,
        SEEK,
        STOP
    }

    data class YouTubeCommand(
        val type: CommandType,
        val videoId: String = "",
        val startSeconds: Float = 0f
    )

    private val _commands = MutableSharedFlow<YouTubeCommand>(extraBufferCapacity = 10)
    val commands: SharedFlow<YouTubeCommand> = _commands.asSharedFlow()

    private val _currentVideoId = MutableStateFlow<String?>(null)
    val currentVideoId: StateFlow<String?> = _currentVideoId.asStateFlow()

    var onTrackEnded: (() -> Unit)? = null

    override fun play(track: TrackDto, startPositionMs: Long) {
        val videoId = track.effectiveVideoId
        if (videoId.isBlank()) {
            _state.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "Invalid YouTube video ID"
                )
            }
            return
        }

        _currentVideoId.value = videoId
        _state.update {
            it.copy(
                errorMessage = null,
                isBuffering = true,
                positionMs = startPositionMs,
                durationMs = track.durationMs
            )
        }

        val startSeconds = (startPositionMs / 1000f).coerceAtLeast(0f)
        _commands.tryEmit(
            YouTubeCommand(
                type = CommandType.LOAD_VIDEO,
                videoId = videoId,
                startSeconds = startSeconds
            )
        )
    }

    override fun pause() {
        _commands.tryEmit(YouTubeCommand(type = CommandType.PAUSE))
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        _commands.tryEmit(YouTubeCommand(type = CommandType.PLAY))
        _state.update { it.copy(isPlaying = true) }
    }

    override fun seekTo(positionMs: Long) {
        val seconds = (positionMs / 1000f).coerceAtLeast(0f)
        _commands.tryEmit(YouTubeCommand(type = CommandType.SEEK, startSeconds = seconds))
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun stop() {
        _commands.tryEmit(YouTubeCommand(type = CommandType.STOP))
        _state.update { it.copy(isPlaying = false, isBuffering = false) }
        _currentVideoId.value = null
    }

    override fun release() {
        stop()
    }

    // Callbacks from YouTube IFrame WebView bridge
    fun onPlayerReady(durationSeconds: Float) {
        _state.update {
            it.copy(
                durationMs = (durationSeconds * 1000).toLong(),
                isBuffering = false
            )
        }
    }

    fun onPlayerStateChange(stateInt: Int) {
        when (stateInt) {
            // YouTube IFrame Player State Constants:
            // -1 = UNSTARTED, 0 = ENDED, 1 = PLAYING, 2 = PAUSED, 3 = BUFFERING, 5 = CUED
            0 -> {
                _state.update { it.copy(isPlaying = false, isBuffering = false) }
                onTrackEnded?.invoke()
            }
            1 -> {
                _state.update { it.copy(isPlaying = true, isBuffering = false) }
            }
            2 -> {
                _state.update { it.copy(isPlaying = false, isBuffering = false) }
            }
            3 -> {
                _state.update { it.copy(isBuffering = true) }
            }
            5 -> {
                _state.update { it.copy(isBuffering = false) }
            }
        }
    }

    fun onTimeUpdate(currentSeconds: Float, durationSeconds: Float) {
        _state.update {
            it.copy(
                positionMs = (currentSeconds * 1000).toLong().coerceAtLeast(0L),
                durationMs = if (durationSeconds > 0) (durationSeconds * 1000).toLong() else it.durationMs
            )
        }
    }

    fun onPlayerError(errorCode: Int, description: String) {
        val msg = when (errorCode) {
            2 -> "Invalid YouTube video parameter ($errorCode)"
            5 -> "HTML5 player error on YouTube ($errorCode)"
            100 -> "YouTube video not found or removed ($errorCode)"
            101, 150 -> "YouTube video embedding not allowed by owner ($errorCode)"
            else -> "YouTube Player error: $description ($errorCode)"
        }
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = msg
            )
        }
    }
}
