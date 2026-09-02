package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SonexaNativeProvider(
    private val musicRepository: MusicRepository = MusicRepository()
) : MusicProvider {

    override val providerId: String = "zynera"
    override val displayName: String = "Zynera Catalog"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    private var lastLatencyMs: Long = 0
    private var lastSuccessTimestamp: Long? = null
    private var lastError: String? = null

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            val result = musicRepository.searchMusic(query)
            lastLatencyMs = System.currentTimeMillis() - start

            result.fold(
                onSuccess = { res ->
                    lastSuccessTimestamp = System.currentTimeMillis()
                    lastError = null
                    val normalized = res.tracks.take(limit).map { track ->
                        track.copy(
                            provider = "sonexa",
                            providerTrackId = track.id,
                            isPlayable = track.audioUrl.isNotBlank(),
                            providerType = "audio",
                            availableProviders = listOf("Sonexa"),
                            isOfficial = true
                        )
                    }
                    Result.success(normalized)
                },
                onFailure = { err ->
                    lastError = err.message
                    Result.failure(err)
                }
            )
        }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val result = musicRepository.getTrending()
        lastLatencyMs = System.currentTimeMillis() - start

        result.fold(
            onSuccess = { res ->
                lastSuccessTimestamp = System.currentTimeMillis()
                lastError = null
                val normalized = res.tracks.take(limit).map { track ->
                    track.copy(
                        provider = "sonexa",
                        providerTrackId = track.id,
                        isPlayable = track.audioUrl.isNotBlank(),
                        providerType = "audio",
                        availableProviders = listOf("Sonexa"),
                        isOfficial = true
                    )
                }
                Result.success(normalized)
            },
            onFailure = { err ->
                lastError = err.message
                Result.failure(err)
            }
        )
    }

    override suspend fun checkHealth(): ProviderHealth {
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = true,
            isAvailable = lastError == null,
            latencyMs = lastLatencyMs,
            lastSuccessfulRequestTimestamp = lastSuccessTimestamp,
            errorMessage = lastError,
            quotaUsageInfo = "Unlimited (Internal Backend API)"
        )
    }
}
