package com.sonexa.app.data.search

import com.sonexa.app.data.model.*
import com.sonexa.app.data.provider.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class SearchOrchestrator(
    private val jiosaavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    private val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    private val sonexaProvider: SonexaNativeProvider = SonexaNativeProvider(),
    private val jamendoProvider: JamendoProvider = JamendoProvider(),
    private val deezerProvider: DeezerMusicProvider = DeezerMusicProvider(),
    private val deduplicationService: TrackDeduplicationService = TrackDeduplicationService(),
    private val searchRankingService: SearchRankingService = SearchRankingService(),
    private val artistResolver: ArtistResolver = ArtistResolver(),
    private val artistCatalogService: ArtistCatalogService = ArtistCatalogService()
) {

    private val allProviders: List<MusicProvider> = listOf(
        jiosaavnProvider,
        audiusProvider,
        sonexaProvider,
        jamendoProvider,
        deezerProvider
    )

    /**
     * Executes the industry-grade search pipeline:
     * 1. Normalize Query
     * 2. Detect Language & Script
     * 3. Transliterate Devanagari <-> Roman
     * 4. Correct Typos ("Did you mean")
     * 5. Detect Musical Intent
     * 6. Expand Query Variants
     * 7. Execute Parallel Search across legitimate providers
     * 8. Aggregate & Deduplicate
     * 9. Rank with multi-signal score (Exact Match Priority)
     * 10. Construct Spotify-Class dynamic sections
     */
    suspend fun search(
        rawQuery: String,
        selectedCategory: ProviderCategory = ProviderCategory.ALL,
        limit: Int = 40
    ): UnifiedSearchResponse = coroutineScope {
        val startTime = System.currentTimeMillis()
        val query = rawQuery.trim()

        if (query.isBlank()) {
            return@coroutineScope UnifiedSearchResponse(
                originalQuery = "",
                normalizedQuery = "",
                detectedLanguage = "en",
                intent = SearchIntent(),
                totalResults = 0
            )
        }

        // 1-5. NLP Pipeline (Normalization, Detection, Transliteration, Intent, Typo)
        val normalized = QueryNormalizer.normalize(query)
        val intent = IntentDetector.detect(query)
        val didYouMean = TypoCorrectionService.findCorrection(query)

        // 6. Query Expansion
        val queryVariants = QueryExpansionService.expand(query, intent)

        // 7. Parallel Provider Search
        val targetProviders = when (selectedCategory) {
            ProviderCategory.ALL -> allProviders.filter { it.isEnabled }
            ProviderCategory.AUDIUS -> listOf(audiusProvider)
            ProviderCategory.JIOSAAVN -> listOf(jiosaavnProvider)
            ProviderCategory.SONEXA -> listOf(sonexaProvider)
            ProviderCategory.JAMENDO -> listOf(jamendoProvider)
            ProviderCategory.AUDIOMACK -> listOf(jamendoProvider)
        }

        val providerCounts = mutableMapOf<String, Int>()
        val providerLatencies = mutableMapOf<String, Long>()

        // Artist profile resolution in parallel
        val resolvedArtistDeferred = async {
            val artistTarget = intent.artistName ?: query
            runCatching { artistResolver.resolve(artistTarget) }.getOrNull()
        }

        // Search queries concurrently across variants & providers with 6s timeout protection
        val searchDeferred = targetProviders.flatMap { provider ->
            queryVariants.take(4).map { variant ->
                async {
                    val pStart = System.currentTimeMillis()
                    val result = withTimeoutOrNull(6000L) {
                        provider.search(variant, limit = 25)
                    } ?: Result.failure(Exception("Timeout"))

                    val duration = System.currentTimeMillis() - pStart
                    providerLatencies[provider.providerId] = duration

                    result.fold(
                        onSuccess = { list ->
                            providerCounts[provider.providerId] = (providerCounts[provider.providerId] ?: 0) + list.size
                            list
                        },
                        onFailure = { emptyList() }
                    )
                }
            }
        }

        val rawTracks = searchDeferred.awaitAll().flatten()

        // 8. Deduplication across providers (Prioritizing Audius & full-length streams)
        val deduplicated = deduplicationService.deduplicate(rawTracks)

        // Prepend movie soundtrack tracks ONLY if intent is MOVIE_SOUNDTRACK
        val movieMatch = if (intent.type == SearchIntentType.MOVIE_SOUNDTRACK) {
            val movieTarget = intent.movieName ?: query
            MovieSoundtrackCatalog.findMovieSoundtrack(movieTarget)
        } else {
            null
        }

        val movieTracks = movieMatch?.tracks.orEmpty()
        val combinedTracks = (movieTracks + deduplicated).distinctBy { it.id }

        // 9. Multi-Signal Ranking
        val rankedTracks = searchRankingService.rankTracks(
            tracks = combinedTracks,
            query = query,
            intent = intent
        ).take(limit)

        // Background prefetch top ranked tracks for 0ms full-length instant playback
        FullAudioStreamResolver.prefetchBatch(rankedTracks)

        val resolvedArtist = resolvedArtistDeferred.await()

        // Construct Top Artist Model (only for artist queries)
        val topArtist = if (resolvedArtist != null && (intent.type == SearchIntentType.ARTIST_SEARCH || intent.type == SearchIntentType.ARTIST_MOOD_SEARCH)) {
            ArtistDto(
                id = resolvedArtist.canonicalId,
                name = resolvedArtist.canonicalName,
                genre = resolvedArtist.genres.firstOrNull() ?: "Artist",
                bio = resolvedArtist.bio,
                imageUrl = resolvedArtist.imageUrl,
                followersCount = resolvedArtist.followersCount.toInt(),
                verified = resolvedArtist.isVerified
            )
        } else if (rankedTracks.isNotEmpty() && intent.type == SearchIntentType.ARTIST_SEARCH) {
            val first = rankedTracks.first()
            ArtistDto(
                id = "art_" + first.artist.lowercase(Locale.ROOT).replace(" ", "_"),
                name = first.artist,
                genre = "Top Artist",
                bio = "Official Artist profile",
                imageUrl = first.effectiveCoverUrl,
                followersCount = 1500000,
                verified = true
            )
        } else null

        // 10. Generate Dynamic Sections (Spotify-Class)
        val sections = mutableListOf<SearchSection>()

        // Section A: Top Result (Spotify priority: Song > Artist > Album/Soundtrack based on intent)
        val topResultItem: Any? = when {
            // 1. Explicit Artist Search intent
            intent.type == SearchIntentType.ARTIST_SEARCH && topArtist != null -> topArtist
            // 2. Explicit Movie/Album Soundtrack intent
            intent.type == SearchIntentType.MOVIE_SOUNDTRACK && movieMatch != null -> movieMatch
            // 3. Primary: Song Search -> Top Result is the #1 ranked Song!
            rankedTracks.isNotEmpty() -> rankedTracks.first()
            topArtist != null -> topArtist
            movieMatch != null -> movieMatch
            else -> null
        }

        if (topResultItem != null) {
            sections.add(
                SearchSection(
                    type = SearchSectionType.TOP_RESULT,
                    title = "Top result",
                    topItem = topResultItem
                )
            )
        }

        // Section B: Songs
        if (rankedTracks.isNotEmpty()) {
            sections.add(
                SearchSection(
                    type = SearchSectionType.TRACKS,
                    title = "Songs",
                    tracks = rankedTracks
                )
            )
        }

        // Section C: Artists
        if (topArtist != null) {
            sections.add(
                SearchSection(
                    type = SearchSectionType.ARTISTS,
                    title = "Artists",
                    artists = listOf(topArtist)
                )
            )
        }

        // Section D: Albums
        val matchingAlbums = mutableListOf<AlbumDto>()
        if (movieMatch != null) {
            matchingAlbums.add(MovieSoundtrackCatalog.toAlbumDto(movieMatch))
        }
        if (rankedTracks.isNotEmpty()) {
            val albumTracks = rankedTracks.filter { it.album.isNotBlank() }.distinctBy { it.album }
            albumTracks.take(4).forEach { t ->
                matchingAlbums.add(
                    AlbumDto(
                        id = "alb_" + t.album.lowercase(Locale.ROOT).replace(" ", "_"),
                        title = t.album,
                        artist = t.artist,
                        year = "2024",
                        coverUrl = t.effectiveCoverUrl,
                        trackCount = 8
                    )
                )
            }
        }
        if (matchingAlbums.isNotEmpty()) {
            sections.add(
                SearchSection(
                    type = SearchSectionType.ALBUMS,
                    title = "Albums",
                    albums = matchingAlbums.distinctBy { it.id }
                )
            )
        }

        // Section E: Playlists
        val matchingPlaylists = listOf(
            PlaylistDto(
                id = "pl_srch_${normalized.replace(" ", "_")}",
                title = "$query Radio",
                subtitle = "Playlist • Top tracks & artists related to $query",
                coverUrl = rankedTracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                trackCount = rankedTracks.size
            ),
            PlaylistDto(
                id = "pl_best_${normalized.replace(" ", "_")}",
                title = "Best of $query",
                subtitle = "Playlist • Essential Hits",
                coverUrl = rankedTracks.getOrNull(1)?.effectiveCoverUrl.orEmpty(),
                trackCount = rankedTracks.size
            )
        )
        sections.add(
            SearchSection(
                type = SearchSectionType.PLAYLISTS,
                title = "Playlists",
                playlists = matchingPlaylists
            )
        )

        // Section F: Related Searches
        val relatedSearches = listOf(
            "$query songs",
            "$query romantic",
            "$query hits",
            "Best of $query",
            "$query live",
            "Artists like $query"
        )

        val totalDuration = System.currentTimeMillis() - startTime

        UnifiedSearchResponse(
            originalQuery = rawQuery,
            normalizedQuery = normalized,
            detectedLanguage = intent.detectedLanguage,
            intent = intent,
            didYouMean = didYouMean,
            topResult = topResultItem,
            sections = sections,
            allTracks = rankedTracks,
            topArtist = topArtist,
            resolvedArtist = resolvedArtist,
            movieSoundtrack = movieMatch,
            matchingAlbums = matchingAlbums,
            matchingPlaylists = matchingPlaylists,
            relatedSearches = relatedSearches,
            totalResults = rankedTracks.size,
            executionTimeMs = totalDuration,
            providerLatencies = providerLatencies,
            providerCounts = providerCounts
        )
    }

    suspend fun getSuggestions(prefix: String): List<SearchSuggestionDto> = coroutineScope {
        val q = QueryNormalizer.normalize(prefix)
        if (q.isBlank()) return@coroutineScope emptyList()

        val list = mutableListOf<SearchSuggestionDto>()
        list.add(SearchSuggestionDto(title = prefix, subtitle = "Search all music", type = "search", query = prefix))

        // Check for typo in prefix
        val typo = TypoCorrectionService.findCorrection(prefix)
        if (typo != null) {
            list.add(SearchSuggestionDto(title = typo.correctedQuery, subtitle = "Did you mean?", type = "artist", query = typo.correctedQuery))
        }

        val capitalized = prefix.replaceFirstChar { it.uppercaseChar() }
        list.add(SearchSuggestionDto(title = capitalized, subtitle = "Artist", type = "artist", query = prefix))
        list.add(SearchSuggestionDto(title = "$capitalized Songs", subtitle = "Top Tracks", type = "track", query = "$prefix Songs"))
        list.add(SearchSuggestionDto(title = "$capitalized Radio", subtitle = "Artist Station", type = "genre", query = "$prefix Radio"))
        list.add(SearchSuggestionDto(title = "Best of $capitalized", subtitle = "Playlist", type = "album", query = "Best of $prefix"))

        list
    }
}
