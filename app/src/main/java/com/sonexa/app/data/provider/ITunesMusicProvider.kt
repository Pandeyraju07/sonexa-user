package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Deprecated / Removed: iTunes Search API has been completely removed in favor of
 * JioSaavn (for official Hindi, Bollywood, and Indian Regional catalog) and Audius.
 */
@Deprecated("Removed in favor of JioSaavn and Audius providers")
class ITunesMusicProvider : MusicProvider {
    override val providerId: String = "itunes"
    override val displayName: String = "iTunes (Removed)"
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
            errorMessage = "Removed: iTunes API is no longer active"
        )
    }
}
