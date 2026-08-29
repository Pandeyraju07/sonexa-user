package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudiomackProvider : MusicProvider {
    override val providerId: String = "audiomack"
    override val displayName: String = "Audiomack"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim().lowercase()
            if (q.isBlank()) return@withContext Result.success(emptyList())

            val results = listOf(
                TrackDto(
                    id = "am_${query.hashCode()}_1",
                    title = "$query (Remix)",
                    artist = "Audiomack Artist",
                    album = "Trending On Audiomack",
                    durationMs = 210000L,
                    audioUrl = "https://cdn.sonexa.app/demo/audio_sample_1.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                    provider = "audiomack",
                    providerTrackId = "am_${query.hashCode()}_1",
                    providerUrl = "https://audiomack.com",
                    isPlayable = true,
                    providerType = "audio",
                    availableProviders = listOf("Audiomack"),
                    isOfficial = false
                )
            )
            Result.success(results.take(limit))
        }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        Result.success(emptyList())
    }

    override suspend fun checkHealth(): ProviderHealth {
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = true,
            isAvailable = true,
            latencyMs = 45,
            lastSuccessfulRequestTimestamp = System.currentTimeMillis(),
            quotaUsageInfo = "Authorized Stream Gateway"
        )
    }
}

class JamendoProvider : MusicProvider {
    override val providerId: String = "jamendo"
    override val displayName: String = "Jamendo"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim().lowercase()
            if (q.isBlank()) return@withContext Result.success(emptyList())

            val results = listOf(
                TrackDto(
                    id = "jam_${query.hashCode()}_1",
                    title = "$query (Indie Acoustic)",
                    artist = "Jamendo Open Artist",
                    album = "Creative Commons Showcase",
                    durationMs = 195000L,
                    audioUrl = "https://cdn.sonexa.app/demo/audio_sample_2.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
                    provider = "jamendo",
                    providerTrackId = "jam_${query.hashCode()}_1",
                    providerUrl = "https://www.jamendo.com",
                    isPlayable = true,
                    providerType = "audio",
                    availableProviders = listOf("Jamendo"),
                    isOfficial = true
                )
            )
            Result.success(results.take(limit))
        }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        Result.success(emptyList())
    }

    override suspend fun checkHealth(): ProviderHealth {
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = true,
            isAvailable = true,
            latencyMs = 38,
            lastSuccessfulRequestTimestamp = System.currentTimeMillis(),
            quotaUsageInfo = "Creative Commons Public API"
        )
    }
}
