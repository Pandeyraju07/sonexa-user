package com.sonexa.app.data.local

import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.model.UserEventRequestDto
import com.sonexa.app.data.repository.AiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object UserEventTracker {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val aiRepository = AiRepository()

    fun logPlayStarted(track: TrackDto, userKey: String = "guest_user") {
        logEvent(
            eventType = "PLAY_STARTED",
            track = track,
            userKey = userKey
        )
    }

    fun logPlay30Seconds(track: TrackDto, userKey: String = "guest_user") {
        logEvent(
            eventType = "PLAY_30_SECONDS",
            track = track,
            userKey = userKey
        )
    }

    fun logPlayCompleted(track: TrackDto, userKey: String = "guest_user") {
        logEvent(
            eventType = "PLAY_COMPLETED",
            track = track,
            userKey = userKey
        )
    }

    fun logSkip(track: TrackDto, userKey: String = "guest_user") {
        logEvent(
            eventType = "SKIP",
            track = track,
            userKey = userKey
        )
    }

    fun logLike(track: TrackDto, userKey: String = "guest_user") {
        logEvent(
            eventType = "LIKE",
            track = track,
            userKey = userKey
        )
    }

    fun logChangeVibe(vibe: String, userKey: String = "guest_user") {
        scope.launch {
            aiRepository.recordEvent(
                UserEventRequestDto(
                    userKey = userKey,
                    eventType = "CHANGE_VIBE",
                    metadataJson = "{\"vibe\":\"$vibe\"}"
                )
            )
        }
    }

    fun logVoiceSearch(transcript: String, userKey: String = "guest_user") {
        scope.launch {
            aiRepository.recordEvent(
                UserEventRequestDto(
                    userKey = userKey,
                    eventType = "VOICE_SEARCH",
                    metadataJson = "{\"transcript\":\"$transcript\"}"
                )
            )
        }
    }

    private fun logEvent(eventType: String, track: TrackDto, userKey: String) {
        scope.launch {
            aiRepository.recordEvent(
                UserEventRequestDto(
                    userKey = userKey,
                    eventType = eventType,
                    trackId = track.id,
                    trackTitle = track.title,
                    artist = track.artist,
                    genre = track.album,
                    language = "Hindi"
                )
            )
        }
    }
}