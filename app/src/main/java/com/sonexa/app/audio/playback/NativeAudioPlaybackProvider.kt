package com.sonexa.app.audio.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.sonexa.app.BuildConfig
import com.sonexa.app.audio.SonexaEqualizerEngine
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NativeAudioPlaybackProvider(
    context: Context,
    val equalizerEngine: SonexaEqualizerEngine = SonexaEqualizerEngine()
) : PlaybackProvider {

    override val providerType: String = "native_audio"

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(EngineState())
    override val state: StateFlow<EngineState> = _state.asStateFlow()

    var onTrackEnded: (() -> Unit)? = null
    var onSessionIdChanged: ((Int) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _state.update { it.copy(isBuffering = true) }
                    }
                    Player.STATE_READY -> {
                        val duration = player.duration.takeIf { it > 0 } ?: 0L
                        _state.update {
                            it.copy(
                                isBuffering = false,
                                durationMs = duration,
                                positionMs = player.currentPosition
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _state.update { it.copy(isPlaying = false, isBuffering = false) }
                        onTrackEnded?.invoke()
                    }
                    Player.STATE_IDLE -> {
                        _state.update { it.copy(isBuffering = false) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = error.message ?: "Native playback error"
                    )
                }
            }
        })

        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onAudioSessionIdChanged(
                eventTime: AnalyticsListener.EventTime,
                audioSessionId: Int
            ) {
                equalizerEngine.attach(audioSessionId)
                onSessionIdChanged?.invoke(audioSessionId)
            }
        })

        startProgressLoop()
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                delay(300)
                if (player.playbackState == Player.STATE_READY || player.isPlaying) {
                    val duration = player.duration.takeIf { it > 0 } ?: _state.value.durationMs
                    _state.update {
                        it.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0),
                            durationMs = duration,
                            isPlaying = player.isPlaying
                        )
                    }
                }
            }
        }
    }

    override fun play(track: TrackDto, startPositionMs: Long) {
        val url = resolveMediaUrl(track.audioUrl)
        if (url.isBlank()) {
            _state.update { it.copy(errorMessage = "No audio stream available for this track") }
            return
        }
        _state.update {
            it.copy(
                errorMessage = null,
                isBuffering = true,
                positionMs = startPositionMs,
                durationMs = track.durationMs
            )
        }
        player.stop()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.playWhenReady = true
    }

    override fun pause() {
        player.pause()
    }

    override fun resume() {
        player.play()
    }

    override fun seekTo(positionMs: Long) {
        val dur = player.duration.takeIf { it > 0 } ?: _state.value.durationMs
        val target = if (dur > 0) positionMs.coerceIn(0, dur) else positionMs
        player.seekTo(target)
        _state.update { it.copy(positionMs = target) }
    }

    override fun stop() {
        player.stop()
        _state.update { it.copy(isPlaying = false, isBuffering = false) }
    }

    override fun release() {
        progressJob?.cancel()
        equalizerEngine.release()
        player.release()
    }

    fun getAudioSessionId(): Int = player.audioSessionId

    companion object {
        fun resolveMediaUrl(raw: String?): String {
            val url = raw?.trim().orEmpty()
            if (url.isEmpty()) return ""
            val origin = apiOrigin()
            val mediaIndex = url.indexOf("/media/")
            if (mediaIndex >= 0) {
                return origin + url.substring(mediaIndex)
            }
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url
                    .replace("http://localhost:8080", origin)
                    .replace("https://localhost:8080", origin)
                    .replace("http://127.0.0.1:8080", origin)
                    .replace("https://127.0.0.1:8080", origin)
            }
            return origin + if (url.startsWith("/")) url else "/$url"
        }

        private fun apiOrigin(): String {
            val base = BuildConfig.API_BASE_URL.trimEnd('/')
            val apiIdx = base.indexOf("/api")
            return if (apiIdx > 0) base.substring(0, apiIdx) else base
        }
    }
}
