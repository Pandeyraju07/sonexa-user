package com.sonexa.app.audio.playback

import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.RecommendationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoplayManager(
    private val repository: RecommendationRepository = RecommendationRepository()
) {
    private var isFetchingAutoplay: Boolean = false

    suspend fun fetchNextAutoplayTracks(
        currentTrack: TrackDto,
        sessionId: String? = null,
        limit: Int = 5,
        fallbackPool: List<TrackDto> = emptyList()
    ): List<TrackDto> = withContext(Dispatchers.IO) {
        if (isFetchingAutoplay) return@withContext emptyList()
        isFetchingAutoplay = true

        try {
            val result = repository.getAutoplay(
                currentSongId = currentTrack.id,
                sessionId = sessionId,
                limit = limit
            )

            val recs = result.getOrNull()?.recommendations?.map { it.song.sanitized() }

            if (!recs.isNullOrEmpty()) {
                recs
            } else {
                // Graceful fallback to local fallback pool if recommendation API returns empty or fails
                fallbackPool.filter { it.id != currentTrack.id }.take(limit)
            }
        } catch (e: Exception) {
            // Never let playback stop due to network/API failure
            fallbackPool.filter { it.id != currentTrack.id }.take(limit)
        } finally {
            isFetchingAutoplay = false
        }
    }
}
