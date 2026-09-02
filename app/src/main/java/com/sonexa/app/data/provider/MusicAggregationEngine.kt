package com.sonexa.app.data.provider

import com.sonexa.app.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

data class UnifiedSearchResult(
    val tracks: List<TrackDto>,
    val resolvedArtist: ResolvedArtist? = null,
    val artistCatalog: ArtistCatalogResponse? = null,
    val providerCounts: Map<String, Int> = emptyMap(),
    val providerLatencies: Map<String, Long> = emptyMap(),
    val providerErrors: Map<String, String> = emptyMap()
)

class MusicAggregationEngine(
    val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    val jiosaavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    val sonexaProvider: SonexaNativeProvider = SonexaNativeProvider(),
    val jamendoProvider: JamendoProvider = JamendoProvider(),
    val deduplicationService: TrackDeduplicationService = TrackDeduplicationService(),
    val searchRankingEngine: SearchRankingEngine = SearchRankingEngine(),
    val artistResolver: ArtistResolver = ArtistResolver(),
    val artistCatalogService: ArtistCatalogService = ArtistCatalogService(),
    val recommendationEngine: HybridRecommendationEngine = HybridRecommendationEngine(),
    val understandingService: TrackUnderstandingService = TrackUnderstandingService()
) {
    val allProviders: List<MusicProvider> = listOf(
        audiusProvider,
        jiosaavnProvider,
        sonexaProvider,
        jamendoProvider
    )

    suspend fun searchAll(
        query: String,
        selectedCategory: ProviderCategory = ProviderCategory.ALL,
        limit: Int = 35
    ): UnifiedSearchResult = coroutineScope {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@coroutineScope UnifiedSearchResult(emptyList())
        }

        // 1. Resolve Artist candidate in parallel
        val resolvedArtistDeferred = async {
            runCatching { artistResolver.resolve(trimmed) }.getOrNull()
        }

        // 2. Filter providers based on category tab
        val targetProviders = when (selectedCategory) {
            ProviderCategory.ALL -> allProviders.filter { it.isEnabled }
            ProviderCategory.AUDIUS -> listOf(audiusProvider)
            ProviderCategory.JIOSAAVN -> listOf(jiosaavnProvider)
            ProviderCategory.SONEXA -> listOf(sonexaProvider)
            ProviderCategory.JAMENDO -> listOf(jamendoProvider)
            ProviderCategory.AUDIOMACK -> listOf(jamendoProvider) // mapped to legal open provider
        }

        val counts = mutableMapOf<String, Int>()
        val latencies = mutableMapOf<String, Long>()
        val errors = mutableMapOf<String, String>()

        // 3. Execute searches concurrently across providers with 8s timeout
        val deferred = targetProviders.map { provider ->
            async {
                val start = System.currentTimeMillis()
                val res = withTimeoutOrNull(8000L) {
                    provider.search(trimmed, limit = limit)
                } ?: Result.failure(Exception("Timeout"))

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

        // 4. Intelligent Deduplication across providers
        val deduplicated = deduplicationService.deduplicate(allRawTracks)

        // 5. Multi-Signal Search Ranking
        val ranked = searchRankingEngine.rankSearchResults(deduplicated, trimmed)

        val resolved = resolvedArtistDeferred.await()

        UnifiedSearchResult(
            tracks = ranked.take(limit),
            resolvedArtist = resolved,
            providerCounts = counts,
            providerLatencies = latencies,
            providerErrors = errors
        )
    }

    suspend fun getArtistFullCatalog(artistQuery: String, cursor: Int = 0, pageSize: Int = 30): ArtistCatalogResponse {
        return artistCatalogService.getFullArtistCatalog(artistQuery, cursor, pageSize)
    }

    suspend fun getSearchSuggestions(prefix: String): List<SearchSuggestionDto> = coroutineScope {
        val q = prefix.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) return@coroutineScope emptyList()

        val list = mutableListOf<SearchSuggestionDto>()
        list.add(SearchSuggestionDto(title = prefix, subtitle = "Search all music", type = "search", query = prefix))

        // Artist suggestion
        list.add(SearchSuggestionDto(title = prefix.replaceFirstChar { it.uppercase() }, subtitle = "Artist", type = "artist", query = prefix))
        list.add(SearchSuggestionDto(title = "$prefix Songs", subtitle = "Top Tracks", type = "track", query = prefix))
        list.add(SearchSuggestionDto(title = "$prefix Radio", subtitle = "Artist Station", type = "genre", query = "$prefix Radio"))
        list.add(SearchSuggestionDto(title = "Best of $prefix", subtitle = "Playlist", type = "album", query = "Best of $prefix"))

        list
    }

    suspend fun getTrendingUnified(limit: Int = 20): List<TrackDto> = coroutineScope {
        val deferredSaavn = async { jiosaavnProvider.getTrending(limit) }
        val deferredAudius = async { audiusProvider.getTrending(limit) }
        val deferredSonexa = async { sonexaProvider.getTrending(limit) }

        val saavnList = deferredSaavn.await().getOrDefault(emptyList())
        val audiusList = deferredAudius.await().getOrDefault(emptyList())
        val sonexaList = deferredSonexa.await().getOrDefault(emptyList())

        val combined = (saavnList + audiusList + sonexaList)
        val deduplicated = deduplicationService.deduplicate(combined)
        deduplicated.take(limit)
    }

    suspend fun checkAllHealth(): List<ProviderHealth> = coroutineScope {
        allProviders.map { async { it.checkHealth() } }.awaitAll()
    }
}
