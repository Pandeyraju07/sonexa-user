package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto

enum class ProviderCategory(val id: String, val title: String) {
    ALL("all", "All"),
    AUDIUS("audius", "Audius (TuneFlow)"),
    JIOSAAVN("jiosaavn", "Top Songs"),
    SONEXA("sonexa", "Sonexa"),
    JAMENDO("jamendo", "Jamendo"),
    AUDIOMACK("audiomack", "Audiomack")
}

data class ProviderHealth(
    val providerId: String,
    val displayName: String,
    val isConfigured: Boolean,
    val isAvailable: Boolean = true,
    val latencyMs: Long = 0,
    val lastSuccessfulRequestTimestamp: Long? = null,
    val errorMessage: String? = null,
    val quotaUsageInfo: String? = null
)

interface MusicProvider {
    val providerId: String
    val displayName: String
    val isEnabled: Boolean
    val isConfigured: Boolean

    suspend fun search(query: String, filter: String = "All", limit: Int = 20): Result<List<TrackDto>>
    suspend fun getTrending(limit: Int = 20): Result<List<TrackDto>>
    suspend fun checkHealth(): ProviderHealth
}
