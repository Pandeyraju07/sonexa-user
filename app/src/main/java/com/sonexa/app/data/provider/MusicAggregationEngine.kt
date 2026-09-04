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
    val jiosaavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    val sonexaProvider: SonexaNativeProvider = SonexaNativeProvider(),
    val jamendoProvider: JamendoProvider = JamendoProvider(),
    val deezerProvider: DeezerMusicProvider = DeezerMusicProvider(),
    val deduplicationService: TrackDeduplicationService = TrackDeduplicationService(),
    val searchRankingEngine: SearchRankingEngine = SearchRankingEngine(),
    val artistResolver: ArtistResolver = ArtistResolver(),
    val artistCatalogService: ArtistCatalogService = ArtistCatalogService(),
    val recommendationEngine: HybridRecommendationEngine = HybridRecommendationEngine(),
    val understandingService: TrackUnderstandingService = TrackUnderstandingService(),
    val searchOrchestrator: com.sonexa.app.data.search.SearchOrchestrator = com.sonexa.app.data.search.SearchOrchestrator(
        jiosaavnProvider = jiosaavnProvider,
        audiusProvider = audiusProvider,
        sonexaProvider = sonexaProvider,
        jamendoProvider = jamendoProvider,
        deezerProvider = deezerProvider,
        deduplicationService = deduplicationService,
        artistResolver = artistResolver,
        artistCatalogService = artistCatalogService
    )
) {
    val allProviders: List<MusicProvider> = listOf(
        jiosaavnProvider,
        audiusProvider,
        sonexaProvider,
        jamendoProvider,
        deezerProvider
    )

    suspend fun searchAll(
        query: String,
        selectedCategory: ProviderCategory = ProviderCategory.ALL,
        limit: Int = 35
    ): UnifiedSearchResult = coroutineScope {
        val orchestrated = searchOrchestrator.search(query, selectedCategory, limit)
        UnifiedSearchResult(
            tracks = orchestrated.allTracks,
            resolvedArtist = orchestrated.resolvedArtist,
            providerCounts = orchestrated.providerCounts,
            providerLatencies = orchestrated.providerLatencies
        )
    }

    suspend fun searchUnifiedDeep(
        query: String,
        selectedCategory: ProviderCategory = ProviderCategory.ALL,
        limit: Int = 40
    ): com.sonexa.app.data.search.UnifiedSearchResponse {
        return searchOrchestrator.search(query, selectedCategory, limit)
    }

    suspend fun getArtistFullCatalog(artistQuery: String, cursor: Int = 0, pageSize: Int = 30): ArtistCatalogResponse {
        return artistCatalogService.getFullArtistCatalog(artistQuery, cursor, pageSize)
    }

    suspend fun getSearchSuggestions(prefix: String): List<SearchSuggestionDto> {
        return searchOrchestrator.getSuggestions(prefix)
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
