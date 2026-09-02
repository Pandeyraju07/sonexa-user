package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JioSaavn unofficial web endpoints and stream-URL decryption are not licensed
 * for production use. The provider stays in the aggregation list as disabled so
 * search/playback fail closed instead of returning unauthorized streams.
 */
class JioSaavnMusicProvider : MusicProvider {
    override val providerId: String = "jiosaavn"
    override val displayName: String = "JioSaavn"
    override val isEnabled: Boolean = false
    override val isConfigured: Boolean = false

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) { Result.success(emptyList()) }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) { Result.success(emptyList()) }

    override suspend fun checkHealth(): ProviderHealth {
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = false,
            isAvailable = false,
            errorMessage = "Disabled: no licensed JioSaavn integration"
        )
    }
}
