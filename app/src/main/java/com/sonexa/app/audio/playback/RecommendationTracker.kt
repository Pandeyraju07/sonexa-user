package com.sonexa.app.audio.playback

import com.sonexa.app.data.model.PlaybackEventRequest
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.RecommendationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecommendationTracker(
    private val repository: RecommendationRepository = RecommendationRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var activeTrackId: String? = null
    private var activeStartTimeMs: Long = 0L
    private var reached25: Boolean = false
    private var reached50: Boolean = false
    private var reached75: Boolean = false
    private var reachedCompleted: Boolean = false
    private var totalDurationMs: Long = 0L

    fun onTrackStarted(track: TrackDto, sessionId: String? = null) {
        activeTrackId = track.id
        activeStartTimeMs = System.currentTimeMillis()
        reached25 = false
        reached50 = false
        reached75 = false
        reachedCompleted = false
        totalDurationMs = track.durationMs ?: 0L

        dispatch("SONG_PLAY_STARTED", track, 0L, totalDurationMs, sessionId)
    }

    fun onProgressTick(track: TrackDto, positionMs: Long, durationMs: Long, sessionId: String? = null) {
        if (track.id != activeTrackId || durationMs <= 0) return
        totalDurationMs = durationMs

        val fraction = positionMs.toFloat() / durationMs.toFloat()

        if (fraction >= 0.25f && !reached25) {
            reached25 = true
            dispatch("SONG_25_PERCENT", track, positionMs, durationMs, sessionId)
        }
        if (fraction >= 0.50f && !reached50) {
            reached50 = true
            dispatch("SONG_50_PERCENT", track, positionMs, durationMs, sessionId)
        }
        if (fraction >= 0.75f && !reached75) {
            reached75 = true
            dispatch("SONG_75_PERCENT", track, positionMs, durationMs, sessionId)
        }
        if (fraction >= 0.85f && !reachedCompleted) {
            reachedCompleted = true
            dispatch("SONG_COMPLETED", track, positionMs, durationMs, sessionId)
        }
    }

    fun onTrackSkipped(track: TrackDto, positionMs: Long, sessionId: String? = null) {
        if (reachedCompleted) return // If already finished, skip next is not an early skip penalty
        val listenTimeMs = System.currentTimeMillis() - activeStartTimeMs

        val eventType = if (positionMs < 10000L || listenTimeMs < 10000L) {
            "EARLY_SKIP"
        } else {
            "SONG_SKIPPED"
        }

        dispatch(eventType, track, positionMs, totalDurationMs, sessionId)
    }

    fun onTrackReplayed(track: TrackDto, sessionId: String? = null) {
        dispatch("SONG_REPLAYED", track, 0L, totalDurationMs, sessionId)
    }

    fun onTrackLiked(track: TrackDto, isLiked: Boolean, sessionId: String? = null) {
        val eventType = if (isLiked) "SONG_LIKED" else "SONG_UNLIKED"
        dispatch(eventType, track, 0L, totalDurationMs, sessionId)
    }

    private fun dispatch(
        eventType: String,
        track: TrackDto,
        positionMs: Long,
        durationMs: Long,
        sessionId: String?
    ) {
        scope.launch {
            repository.trackPlaybackEvent(
                PlaybackEventRequest(
                    sessionId = sessionId,
                    songId = track.id,
                    artistId = track.artist,
                    albumId = track.album,
                    position = positionMs,
                    duration = durationMs,
                    eventType = eventType
                )
            )
        }
    }
}
