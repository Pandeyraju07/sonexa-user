package com.sonexa.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.audio.SonexaEqualizerEngine
import com.sonexa.app.audio.playback.PlaybackManager
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val track: TrackDto? = null,
    val queue: List<TrackDto> = emptyList(),
    val queueIndex: Int = 0,
    val sourceTitle: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerRemainingMs: Long? = null,
    val playbackSpeed: Float = 1.0f,
    val connectedDevice: String = "This device",
    val deviceConnected: Boolean = true,
    val equalizer: SonexaEqualizerEngine.Snapshot = SonexaEqualizerEngine.Snapshot(),
    val errorMessage: String? = null,
    val activeProviderType: String = "native_audio",
    val isYouTubeMode: Boolean = false
)

enum class RepeatMode { OFF, ALL, ONE }

class PlaybackViewModel : AndroidViewModel {

    private val musicRepository = MusicRepository()
    private val userRepository = UserRepository()
    val equalizerEngine = SonexaEqualizerEngine()
    val playbackManager: PlaybackManager

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var sleepJob: Job? = null
    private var originalQueue: List<TrackDto> = emptyList()

    constructor(application: Application) : super(application) {
        playbackManager = PlaybackManager(application, equalizerEngine)

        playbackManager.onTrackEnded = {
            onTrackEnded()
        }

        playbackManager.nativeProvider.onSessionIdChanged = { sessionId ->
            bindEqualizer(sessionId)
        }

        // Collect engine state from active provider (native or YouTube)
        viewModelScope.launch {
            playbackManager.engineState.collectLatest { engine ->
                _uiState.update { current ->
                    current.copy(
                        isPlaying = engine.isPlaying,
                        positionMs = engine.positionMs,
                        durationMs = if (engine.durationMs > 0) engine.durationMs else current.durationMs,
                        errorMessage = engine.errorMessage ?: current.errorMessage
                    )
                }
            }
        }

        // Collect active provider type
        viewModelScope.launch {
            playbackManager.activeProviderType.collectLatest { providerType ->
                _uiState.update { current ->
                    current.copy(
                        activeProviderType = providerType,
                        isYouTubeMode = providerType == "youtube_video"
                    )
                }
            }
        }

        refreshDeviceFromSettings()
        loadServerQueueHint()
        bindEqualizer()
    }

    fun play(track: TrackDto, sourceTitle: String = track.album) {
        playQueue(listOf(track), 0, sourceTitle.ifBlank { track.album })
    }

    fun playQueue(tracks: List<TrackDto>, startIndex: Int = 0, sourceTitle: String = "") {
        if (tracks.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No tracks to play") }
            return
        }
        originalQueue = tracks
        val index = startIndex.coerceIn(0, tracks.lastIndex)
        val ordered = if (_uiState.value.shuffle && tracks.size > 1) {
            buildShuffledQueue(tracks, index)
        } else {
            tracks
        }
        val playIndex = if (_uiState.value.shuffle && tracks.size > 1) {
            ordered.indexOfFirst { it.id == tracks[index].id }.coerceAtLeast(0)
        } else {
            index
        }
        _uiState.update {
            it.copy(
                queue = ordered,
                queueIndex = playIndex,
                sourceTitle = sourceTitle.ifBlank { ordered.getOrNull(playIndex)?.album.orEmpty() }
            )
        }
        startTrackAt(playIndex)
    }

    fun togglePlayPause() {
        val currentTrack = _uiState.value.track ?: return
        playbackManager.togglePlayPause(currentTrack)
    }

    fun skipNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return
        val next = when {
            state.queueIndex < state.queue.lastIndex -> state.queueIndex + 1
            state.repeatMode == RepeatMode.ALL -> 0
            else -> return
        }
        startTrackAt(next)
    }

    fun skipPrevious() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return
        if (state.positionMs > 3000) {
            playbackManager.seekTo(0)
            return
        }
        val prev = when {
            state.queueIndex > 0 -> state.queueIndex - 1
            state.repeatMode == RepeatMode.ALL -> state.queue.lastIndex
            else -> {
                playbackManager.seekTo(0)
                return
            }
        }
        startTrackAt(prev)
    }

    fun toggleShuffle() {
        val enabling = !_uiState.value.shuffle
        val current = _uiState.value.track
        val base = if (originalQueue.isNotEmpty()) originalQueue else _uiState.value.queue
        if (base.isEmpty()) {
            _uiState.update { it.copy(shuffle = enabling) }
            return
        }
        val newQueue = if (enabling) {
            buildShuffledQueue(base, base.indexOfFirst { it.id == current?.id }.coerceAtLeast(0))
        } else {
            base
        }
        val newIndex = newQueue.indexOfFirst { it.id == current?.id }.coerceAtLeast(0)
        _uiState.update { it.copy(shuffle = enabling, queue = newQueue, queueIndex = newIndex) }
    }

    fun cycleRepeatMode() {
        val next = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.update { it.copy(repeatMode = next) }
    }

    fun toggleLike(customTrack: TrackDto? = null) {
        val track = customTrack ?: _uiState.value.track ?: return
        val isNowLiked = com.sonexa.app.data.local.LikedSongsStore.toggleLike(getApplication(), track)

        // Optimistic update for instant Spotify-like responsiveness
        _uiState.update { state ->
            state.copy(
                track = if (state.track?.id == track.id) state.track?.copy(isLiked = isNowLiked) else state.track,
                queue = state.queue.map {
                    if (it.id == track.id) it.copy(isLiked = isNowLiked) else it
                }
            )
        }

        viewModelScope.launch {
            userRepository.toggleLikeSong(track.id).onFailure {
                // Keep local like state smoothly without intrusive error toasts
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        _uiState.update { it.copy(playbackSpeed = clamped) }
        playbackManager.setPlaybackSpeed(clamped)
    }

    fun seekToMs(positionMs: Long) {
        val dur = _uiState.value.durationMs
        val target = if (dur > 0) positionMs.coerceIn(0L, dur) else positionMs.coerceAtLeast(0L)
        playbackManager.seekTo(target)
        _uiState.update { it.copy(positionMs = target) }
    }

    fun seekFraction(fraction: Float) {
        val dur = _uiState.value.durationMs
        if (dur > 0) {
            val target = (dur * fraction.coerceIn(0f, 1f)).toLong()
            seekToMs(target)
        }
    }

    fun seekForward30() {
        val current = _uiState.value.positionMs
        val dur = _uiState.value.durationMs
        val target = (current + 30_000L).coerceAtMost(if (dur > 0) dur else Long.MAX_VALUE)
        seekToMs(target)
    }

    fun seekBackward10() {
        val current = _uiState.value.positionMs
        val target = (current - 10_000L).coerceAtLeast(0L)
        seekToMs(target)
    }

    fun seekToChapter(startTimeSeconds: Long) {
        seekToMs(startTimeSeconds * 1000L)
    }

    fun setSleepTimerMinutes(minutes: Int?) {
        sleepJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _uiState.update { it.copy(sleepTimerRemainingMs = null) }
            return
        }
        var remaining = minutes * 60_000L
        _uiState.update { it.copy(sleepTimerRemainingMs = remaining) }
        sleepJob = viewModelScope.launch {
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _uiState.update { it.copy(sleepTimerRemainingMs = remaining.coerceAtLeast(0)) }
            }
            playbackManager.pause()
            _uiState.update { it.copy(sleepTimerRemainingMs = null) }
        }
    }

    fun playFromQueueIndex(index: Int) {
        if (index !in _uiState.value.queue.indices) return
        startTrackAt(index)
    }

    fun refreshDeviceFromSettings() {
        viewModelScope.launch {
            userRepository.getSettings().onSuccess { res ->
                val devices = (res.settings["connectedDevices"] as? List<*>)
                    ?.mapNotNull { it?.toString() }
                    .orEmpty()
                val device = devices.firstOrNull().orEmpty().ifBlank { "This device" }
                _uiState.update {
                    it.copy(
                        connectedDevice = device,
                        deviceConnected = devices.isNotEmpty() || device == "This device"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        _uiState.update { it.copy(equalizer = equalizerEngine.setEnabled(enabled)) }
        persistEqualizer()
    }

    fun setEqualizerBand(index: Int, level: Float) {
        _uiState.update { it.copy(equalizer = equalizerEngine.setBandLevel(index, level)) }
        persistEqualizer()
    }

    fun setBassBoost(level: Float) {
        _uiState.update { it.copy(equalizer = equalizerEngine.setBassBoost(level)) }
        persistEqualizer()
    }

    fun setVirtualizer(level: Float) {
        _uiState.update { it.copy(equalizer = equalizerEngine.setVirtualizer(level)) }
        persistEqualizer()
    }

    fun applyEqualizerPreset(name: String) {
        bindEqualizer()
        _uiState.update { it.copy(equalizer = equalizerEngine.applyPreset(name)) }
        persistEqualizer()
    }

    fun resetEqualizer() {
        _uiState.update { it.copy(equalizer = equalizerEngine.reset()) }
        persistEqualizer()
    }

    private fun bindEqualizer(sessionId: Int = playbackManager.nativeProvider.getAudioSessionId()) {
        val snap = equalizerEngine.attach(sessionId)
        _uiState.update { it.copy(equalizer = snap) }
    }

    private fun persistEqualizer() {
        val eq = _uiState.value.equalizer
        viewModelScope.launch {
            userRepository.updateSettings(
                mapOf(
                    "eqEnabled" to eq.enabled,
                    "eqPreset" to eq.presetName,
                    "eqBass" to eq.bassBoost,
                    "eqVirtual" to eq.virtualizer,
                    "eqBands" to eq.bands.map { it.level }
                )
            )
        }
    }

    private fun loadServerQueueHint() {
        viewModelScope.launch {
            musicRepository.getQueue().onSuccess { res ->
                if (_uiState.value.queue.isEmpty() && res.queue.isNotEmpty()) {
                    originalQueue = res.queue
                    _uiState.update {
                        it.copy(
                            queue = res.queue,
                            sourceTitle = res.nowPlaying?.album.orEmpty()
                        )
                    }
                }
            }
        }
    }

    private fun onTrackEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                playbackManager.seekTo(0)
                playbackManager.resume()
            }
            RepeatMode.ALL, RepeatMode.OFF -> skipNext()
        }
    }

    private fun startTrackAt(index: Int) {
        val queue = _uiState.value.queue
        if (index !in queue.indices) return
        val track = queue[index]

        _uiState.update {
            it.copy(
                track = track,
                queueIndex = index,
                errorMessage = null,
                positionMs = 0,
                durationMs = (track.durationMs ?: 0L).coerceAtLeast(0),
                sourceTitle = it.sourceTitle.ifBlank { track.album.orEmpty() },
                isYouTubeMode = track.isYouTube && track.audioUrl.isNullOrBlank()
            )
        }

        playbackManager.play(track, 0L)
    }

    private fun buildShuffledQueue(tracks: List<TrackDto>, preferIndex: Int): List<TrackDto> {
        if (tracks.size <= 1) return tracks
        val preferred = tracks.getOrNull(preferIndex) ?: tracks.first()
        val rest = tracks.filter { it.id != preferred.id }.shuffled()
        return listOf(preferred) + rest
    }

    override fun onCleared() {
        sleepJob?.cancel()
        playbackManager.release()
        super.onCleared()
    }

    companion object {
        val Factory: androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: androidx.lifecycle.viewmodel.CreationExtras
                ): T {
                    val app = extras[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        ?: throw IllegalStateException("Application is required to create PlaybackViewModel")
                    return PlaybackViewModel(app) as T
                }
            }

        fun resolveMediaUrl(raw: String?): String =
            com.sonexa.app.audio.playback.NativeAudioPlaybackProvider.resolveMediaUrl(raw)
    }
}
