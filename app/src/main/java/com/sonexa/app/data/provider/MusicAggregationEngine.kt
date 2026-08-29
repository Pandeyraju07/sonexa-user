package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale

data class UnifiedSearchResult(
    val tracks: List<TrackDto>,
    val providerCounts: Map<String, Int> = emptyMap(),
    val providerLatencies: Map<String, Long> = emptyMap(),
    val providerErrors: Map<String, String> = emptyMap()
)

class MusicAggregationEngine(
    val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    val jiosaavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    val sonexaProvider: SonexaNativeProvider = SonexaNativeProvider(),
    val jamendoProvider: JamendoProvider = JamendoProvider(),
    val audiomackProvider: AudiomackProvider = AudiomackProvider()
) {
    val allProviders: List<MusicProvider> = listOf(
        audiusProvider,
        jiosaavnProvider,
        sonexaProvider,
        jamendoProvider,
        audiomackProvider
    )

    suspend fun searchAll(
        query: String,
        selectedCategory: ProviderCategory = ProviderCategory.ALL,
        limit: Int = 30
    ): UnifiedSearchResult = coroutineScope {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@coroutineScope UnifiedSearchResult(emptyList())
        }

        // Filter providers based on category tab
        val targetProviders = when (selectedCategory) {
            ProviderCategory.ALL -> allProviders.filter { it.isEnabled }
            ProviderCategory.AUDIUS -> listOf(audiusProvider)
            ProviderCategory.JIOSAAVN -> listOf(jiosaavnProvider)
            ProviderCategory.SONEXA -> listOf(sonexaProvider)
            ProviderCategory.JAMENDO -> listOf(jamendoProvider)
            ProviderCategory.AUDIOMACK -> listOf(audiomackProvider)
        }

        val counts = mutableMapOf<String, Int>()
        val latencies = mutableMapOf<String, Long>()
        val errors = mutableMapOf<String, String>()

        // Execute searches concurrently across providers
        val deferred = targetProviders.map { provider ->
            async {
                val start = System.currentTimeMillis()
                val res = provider.search(trimmed, limit = limit)
                val duration = System.currentTimeMillis() - start
                latencies[provider.providerId] = duration

                res.fold(
                    onSuccess = { list ->
                        counts[provider.providerId] = list.size
                        Pair(provider.providerId, list)
                    },
                    onFailure = { err ->
                        errors[provider.providerId] = err.message ?: "Search failed"
                        Pair(provider.providerId, emptyList())
                    }
                )
            }
        }

        val results = deferred.awaitAll()
        val allRawTracks = results.flatMap { it.second }

        // Deduplicate and aggregate across providers
        val aggregatedTracks = aggregateAndDeduplicate(allRawTracks, trimmed)

        // Rank by exact match, playability, official status
        val ranked = rankTracks(aggregatedTracks, trimmed)

        UnifiedSearchResult(
            tracks = ranked.take(limit),
            providerCounts = counts,
            providerLatencies = latencies,
            providerErrors = errors
        )
    }

    suspend fun getTrendingUnified(limit: Int = 20): List<TrackDto> = coroutineScope {
        val deferredSaavn = async { jiosaavnProvider.getTrending(limit) }
        val deferredSonexa = async { sonexaProvider.getTrending(limit) }

        val saavnList = deferredSaavn.await().getOrDefault(emptyList())
        val sonexaList = deferredSonexa.await().getOrDefault(emptyList())

        val combined = mutableListOf<TrackDto>()
        // Interleave / merge trending tracks
        val maxLen = maxOf(saavnList.size, sonexaList.size)
        for (i in 0 until maxLen) {
            if (i < saavnList.size) combined.add(saavnList[i])
            if (i < sonexaList.size) combined.add(sonexaList[i])
        }
        aggregateAndDeduplicate(combined, "").take(limit)
    }

    suspend fun checkAllHealth(): List<ProviderHealth> = coroutineScope {
        allProviders.map { async { it.checkHealth() } }.awaitAll()
    }

    private fun aggregateAndDeduplicate(tracks: List<TrackDto>, query: String): List<TrackDto> {
        val grouped = mutableMapOf<String, MutableList<TrackDto>>()

        for (track in tracks) {
            val key = normalizeTrackKey(track.title, track.artist)
            grouped.getOrPut(key) { mutableListOf() }.add(track)
        }

        val aggregated = mutableListOf<TrackDto>()
        for ((_, group) in grouped) {
            if (group.size == 1) {
                aggregated.add(group[0])
            } else {
                // Multi-provider match: determine primary provider (e.g. prioritize authorized stream or official video)
                val primary = group.firstOrNull { it.isOfficial && it.isPlayable }
                    ?: group.firstOrNull { it.isPlayable }
                    ?: group.first()

                val allProviderNames = group.map { it.provider.replaceFirstChar { c -> c.uppercase() } }.distinct()
                val merged = primary.copy(
                    availableProviders = allProviderNames
                )
                aggregated.add(merged)
            }
        }
        return aggregated
    }

    private fun normalizeTrackKey(title: String, artist: String): String {
        val cleanTitle = title.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
        val cleanArtist = artist.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
        return "$cleanTitle::$cleanArtist"
    }

    private fun rankTracks(tracks: List<TrackDto>, query: String): List<TrackDto> {
        val q = query.lowercase(Locale.ROOT).trim()
        return tracks.sortedWith(
            compareByDescending<TrackDto> { it.title.lowercase(Locale.ROOT).trim() == q } // Exact title
                .thenByDescending { it.artist.lowercase(Locale.ROOT).trim() == q } // Exact artist
                .thenByDescending { it.isOfficial }
                .thenByDescending { it.isPlayable }
                .thenByDescending { it.availableProviders.size > 1 } // Multi-source availability
                .thenByDescending { it.title.lowercase(Locale.ROOT).contains(q) }
        )
    }
}
