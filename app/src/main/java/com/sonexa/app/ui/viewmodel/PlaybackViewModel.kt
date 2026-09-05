package com.sonexa.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.audio.SonexaEqualizerEngine
import com.sonexa.app.audio.playback.PlaybackManager
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.data.repository.UserRepository
import com.sonexa.app.data.model.AudioQuality
import com.sonexa.app.data.local.SessionManager
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
    val isYouTubeMode: Boolean = false,
    val audioQuality: AudioQuality = AudioQuality.LOSSLESS
)

enum class RepeatMode { OFF, ALL, ONE }

class PlaybackViewModel : AndroidViewModel {

    private val musicRepository = MusicRepository()
    private val userRepository = UserRepository()
    private val sessionManager: SessionManager
    val playbackManager: PlaybackManager
    val equalizerEngine: SonexaEqualizerEngine
        get() = playbackManager.equalizerEngine

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()
    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private var sleepJob: Job? = null
    private var originalQueue: List<TrackDto> = emptyList()

    constructor(application: Application) : super(application) {
        sessionManager = SessionManager.getInstance(application)
        val initialQuality = AudioQuality.fromKey(sessionManager.audioQuality)
        _uiState.update { it.copy(audioQuality = initialQuality) }
        playbackManager = (application as com.sonexa.app.SonexaApp).playbackManager

        playbackManager.onTrackEnded = {
            onTrackEnded()
        }

        playbackManager.onTrackError = { errorMsg ->
            viewModelScope.launch {
                val currentTrack = _uiState.value.track
                if (currentTrack != null) {
                    com.sonexa.app.data.provider.FullAudioStreamResolver.invalidate(currentTrack)
                    if (currentTrack.effectiveVideoId.isNotBlank() && !_uiState.value.isYouTubeMode) {
                        _uiState.update { it.copy(isYouTubeMode = true, errorMessage = null) }
                        playbackManager.play(currentTrack.copy(provider = "youtube", providerType = "youtube_video"))
                    }
                }
            }
        }

        playbackManager.nativeProvider.onSessionIdChanged = { sessionId ->
            bindEqualizer(sessionId)
        }

        // Collect engine state from active provider (native or YouTube).
        // Position ticks stay on elapsedMs so the full player is not recomposed 3x/sec.
        viewModelScope.launch {
            playbackManager.engineState.collectLatest { engine ->
                _elapsedMs.value = engine.positionMs
                _uiState.update { current ->
                    val activeTrack = current.track
                    val trackMetadataDuration = activeTrack?.durationMs ?: 0L
                    // Never downgrade a full-length track duration to a 30-sec preview duration!
                    val duration = if (trackMetadataDuration > 30000L && engine.durationMs in 1..31000L) {
                        trackMetadataDuration
                    } else if (engine.durationMs > 0) {
                        engine.durationMs
                    } else if (trackMetadataDuration > 0) {
                        trackMetadataDuration
                    } else {
                        current.durationMs
                    }

                    val error = engine.errorMessage ?: current.errorMessage
                    if (current.isPlaying == engine.isPlaying &&
                        current.durationMs == duration &&
                        current.errorMessage == error
                    ) {
                        current
                    } else {
                        current.copy(
                            isPlaying = engine.isPlaying,
                            durationMs = duration,
                            errorMessage = error,
                            positionMs = engine.positionMs
                        )
                    }
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

        // Live Reactive Liked Songs Synchronization
        viewModelScope.launch {
            com.sonexa.app.data.local.LikedSongsStore.likedSongs.collectLatest { likedList ->
                val likedSet = likedList.map { it.id }.toSet()
                _uiState.update { state ->
                    val currentTrack = state.track?.sanitized()
                    val updatedTrack = if (currentTrack != null) {
                        currentTrack.copySafe(isLiked = likedSet.contains(currentTrack.id))
                    } else null
                    val updatedQueue = state.queue.map {
                        val safe = it.sanitized()
                        safe.copySafe(isLiked = likedSet.contains(safe.id))
                    }
                    state.copy(track = updatedTrack, queue = updatedQueue)
                }
            }
        }

        refreshDeviceFromSettings()
        loadServerQueueHint()
        bindEqualizer()
    }

    fun play(track: TrackDto, sourceTitle: String = track.album) {
        val mappedTrack = com.sonexa.app.data.local.LikedSongsStore.withLikedStatus(track) ?: track
        playQueue(listOf(mappedTrack), 0, sourceTitle.ifBlank { mappedTrack.album })
    }

    fun playTrack(track: TrackDto?, queue: List<TrackDto> = emptyList(), sourceTitle: String = "") {
        if (track == null) return
        val effectiveQueue = if (queue.isNotEmpty()) queue else listOf(track)
        val idx = effectiveQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playQueue(effectiveQueue, idx, sourceTitle.ifBlank { track.album })
    }

    fun playQueue(tracks: List<TrackDto>, startIndex: Int = 0, sourceTitle: String = "") {
        if (tracks.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No tracks to play") }
            return
        }
        val mappedTracks = com.sonexa.app.data.local.LikedSongsStore.withLikedStatus(tracks)
        originalQueue = mappedTracks
        val index = startIndex.coerceIn(0, mappedTracks.lastIndex)
        val ordered = if (_uiState.value.shuffle && mappedTracks.size > 1) {
            buildShuffledQueue(mappedTracks, index)
        } else {
            mappedTracks
        }
        val playIndex = if (_uiState.value.shuffle && mappedTracks.size > 1) {
            ordered.indexOfFirst { it.id == mappedTracks[index].id }.coerceAtLeast(0)
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
        if (state.positionMs > 3000 || _elapsedMs.value > 3000) {
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
                track = if (state.track?.id == track.id) state.track?.copySafe(isLiked = isNowLiked) else state.track,
                queue = state.queue.map {
                    if (it.id == track.id) it.copySafe(isLiked = isNowLiked) else it
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

    fun setAudioQuality(quality: AudioQuality) {
        sessionManager.audioQuality = quality.key
        _uiState.update { it.copy(audioQuality = quality) }
        viewModelScope.launch {
            userRepository.updateSettings(mapOf("audioQuality" to quality.key))
        }
        val currentTrack = _uiState.value.track
        if (currentTrack != null && !_uiState.value.isYouTubeMode && currentTrack.audioUrl.isNotBlank()) {
            val adaptedUrl = com.sonexa.app.data.provider.FullAudioStreamResolver.applyAudioQuality(currentTrack.audioUrl, quality)
            if (adaptedUrl != currentTrack.audioUrl) {
                val currentPos = playbackManager.engineState.value.positionMs.coerceAtLeast(0L)
                val updatedTrack = currentTrack.copySafe(audioUrl = adaptedUrl)
                _uiState.update { state ->
                    state.copy(
                        track = updatedTrack,
                        queue = state.queue.map { if (it.id == updatedTrack.id) updatedTrack else it }
                    )
                }
                playbackManager.play(updatedTrack, currentPos)
            }
        }
    }

    fun seekToMs(positionMs: Long) {
        val dur = _uiState.value.durationMs
        val target = if (dur > 0) positionMs.coerceIn(0L, dur) else positionMs.coerceAtLeast(0L)
        playbackManager.seekTo(target)
        _elapsedMs.value = target
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
        val current = _elapsedMs.value.takeIf { it > 0 } ?: _uiState.value.positionMs
        val dur = _uiState.value.durationMs
        val target = (current + 10_000L).coerceAtMost(if (dur > 0) dur else Long.MAX_VALUE)
        seekToMs(target)
    }

    fun seekBackward10() {
        val current = _elapsedMs.value.takeIf { it > 0 } ?: _uiState.value.positionMs
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

    fun stopPlayback() {
        sleepJob?.cancel()
        playbackManager.stop()
        _elapsedMs.value = 0L
        _uiState.update {
            it.copy(
                track = null,
                queue = emptyList(),
                queueIndex = 0,
                isPlaying = false,
                positionMs = 0L,
                durationMs = 0L,
                sourceTitle = "",
                sleepTimerRemainingMs = null,
                isYouTubeMode = false
            )
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
                _elapsedMs.value = 0L
                playbackManager.resume()
            }
            RepeatMode.ALL, RepeatMode.OFF -> skipNext()
        }
    }

    private fun startTrackAt(index: Int) {
        val queue = _uiState.value.queue
        if (index !in queue.indices) return
        val rawTrack = queue[index]
        val track = com.sonexa.app.data.local.LikedSongsStore.withLikedStatus(rawTrack) ?: rawTrack

        _elapsedMs.value = 0L
        _uiState.update {
            it.copy(
                track = track,
                queueIndex = index,
                errorMessage = null,
                positionMs = 0,
                durationMs = (track.durationMs ?: 0L).coerceAtLeast(0),
                sourceTitle = it.sourceTitle.ifBlank { track.album.orEmpty() },
                isYouTubeMode = false
            )
        }

        viewModelScope.launch {
            val currentQuality = _uiState.value.audioQuality
            val cachedStream = com.sonexa.app.data.provider.FullAudioStreamResolver.getCachedStream(track)
                ?.let { com.sonexa.app.data.provider.FullAudioStreamResolver.applyAudioQuality(it, currentQuality) }
            val rawAudio = com.sonexa.app.data.provider.FullAudioStreamResolver.applyAudioQuality(track.audioUrl, currentQuality)
            val isInitialPreview = com.sonexa.app.data.provider.FullAudioStreamResolver.isAudioPreview(rawAudio, track.provider)

            if (!cachedStream.isNullOrBlank()) {
                val fullTrack = track.copySafe(audioUrl = cachedStream, isPlayable = true)
                _uiState.update { current ->
                    if (current.track?.id == track.id) current.copy(track = fullTrack) else current
                }
                playbackManager.play(fullTrack, 0L)
            } else if (!isInitialPreview && rawAudio.isNotBlank()) {
                val fullTrack = track.copySafe(audioUrl = rawAudio, isPlayable = true)
                playbackManager.play(fullTrack, 0L)
            } else if (track.isYouTube && track.effectiveVideoId.isNotBlank()) {
                playbackManager.play(track, 0L)
            } else {
                // If track has a preview URL, begin playing immediately so user hears sound instantly
                if (rawAudio.isNotBlank()) {
                    playbackManager.play(track.copySafe(audioUrl = rawAudio), 0L)
                }

                // Simultaneously resolve full-length stream
                val resolvedStream = com.sonexa.app.data.provider.FullAudioStreamResolver.resolveFullStreamUrl(track)
                val fullStream = com.sonexa.app.data.provider.FullAudioStreamResolver.applyAudioQuality(resolvedStream, currentQuality)
                if (fullStream.isNotBlank() && fullStream != rawAudio) {
                    val currentPos = playbackManager.engineState.value.positionMs.coerceAtLeast(0L)
                    val fullTrack = track.copySafe(audioUrl = fullStream, isPlayable = true)
                    _uiState.update { current ->
                        if (current.track?.id == track.id) current.copy(track = fullTrack) else current
                    }
                    playbackManager.play(fullTrack, currentPos)
                } else if (rawAudio.isBlank() && fullStream.isNotBlank()) {
                    val fullTrack = track.copySafe(audioUrl = fullStream, isPlayable = true)
                    _uiState.update { current ->
                        if (current.track?.id == track.id) current.copy(track = fullTrack) else current
                    }
                    playbackManager.play(fullTrack, 0L)
                }
            }

            // Prefetch upcoming tracks in queue for instant switching
            com.sonexa.app.data.provider.FullAudioStreamResolver.prefetch(queue.getOrNull(index + 1))
            com.sonexa.app.data.provider.FullAudioStreamResolver.prefetch(queue.getOrNull(index + 2))
        }
    }

    private fun buildShuffledQueue(tracks: List<TrackDto>, preferIndex: Int): List<TrackDto> {
        if (tracks.size <= 1) return tracks
        val preferred = tracks.getOrNull(preferIndex) ?: tracks.first()
        val rest = tracks.filter { it.id != preferred.id }.shuffled()
        return listOf(preferred) + rest
    }

    override fun onCleared() {
        sleepJob?.cancel()
        playbackManager.onTrackEnded = null
        playbackManager.onTrackError = null
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
