package com.sonexa.app.data.provider

import com.sonexa.app.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.Locale

class ArtistCatalogService(
    private val artistResolver: ArtistResolver = ArtistResolver(),
    private val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    private val saavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    private val jamendoProvider: JamendoProvider = JamendoProvider(),
    private val deduplicationService: TrackDeduplicationService = TrackDeduplicationService(),
    private val searchRankingEngine: SearchRankingEngine = SearchRankingEngine()
) {

    suspend fun getFullArtistCatalog(
        artistQuery: String,
        cursor: Int = 0,
        pageSize: Int = 30
    ): ArtistCatalogResponse = coroutineScope {
        // 1. Resolve Canonical Artist Profile
        val resolved = artistResolver.resolve(artistQuery)
        val canonicalName = resolved.canonicalName

        // 2. Fetch Multi-Tier Catalogs Concurrently
        val directSaavnDeferred = async {
            saavnProvider.search(canonicalName, limit = 50).getOrDefault(emptyList())
        }

        val audiusUserId = resolved.providerIds["audius"].orEmpty()
        val audiusTracksDeferred = async {
            if (audiusUserId.isNotBlank()) {
                audiusProvider.getArtistTracks(audiusUserId, offset = cursor, limit = 50).getOrDefault(emptyList())
            } else {
                audiusProvider.search(canonicalName, limit = 30).getOrDefault(emptyList())
            }
        }

        val audiusAlbumsDeferred = async {
            if (audiusUserId.isNotBlank()) {
                audiusProvider.getArtistAlbums(audiusUserId).getOrDefault(emptyList())
            } else emptyList()
        }

        val collabDeferred = async {
            saavnProvider.search("$canonicalName Duet Hits", limit = 20).getOrDefault(emptyList())
        }

        val remixDeferred = async {
            saavnProvider.search("$canonicalName Remix", limit = 15).getOrDefault(emptyList())
        }

        val jamendoDeferred = async {
            jamendoProvider.search(canonicalName, limit = 15).getOrDefault(emptyList())
        }

        val saavnTracks = directSaavnDeferred.await()
        val audiusTracks = audiusTracksDeferred.await()
        val audiusAlbums = audiusAlbumsDeferred.await()
        val collabTracks = collabDeferred.await()
        val remixTracks = remixDeferred.await()
        val jamendoTracks = jamendoDeferred.await()

        // 3. Aggregate all candidate tracks
        val allRaw = (saavnTracks + audiusTracks + collabTracks + remixTracks + jamendoTracks)
        val deduplicated = deduplicationService.deduplicate(allRaw)
        val rankedAll = searchRankingEngine.rankSearchResults(deduplicated, canonicalName)

        // 4. Categorize by version and section
        val popular = rankedAll.filter { it.versionType == "Original" }.take(15)
        val singles = rankedAll.filter { it.album.contains("Single", true) || it.album.isBlank() }.take(10)
        val collaborations = rankedAll.filter {
            it.artist.contains(",") || it.artist.contains("&") || it.artist.contains("feat", true)
        }.take(10)
        val remixes = rankedAll.filter { it.versionType in listOf("Remix", "Acoustic", "Live", "Lo-Fi") }.take(10)

        // 5. Build dynamic albums list
        val albumGroups = rankedAll.filter { it.album.isNotBlank() && !it.album.contains("Single", true) }
            .groupBy { it.album }

        val albumsList = mutableListOf<ArtistAlbumSectionDto>()
        albumGroups.forEach { (albName, trList) ->
            albumsList.add(
                ArtistAlbumSectionDto(
                    id = "alb_" + albName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "_"),
                    title = albName,
                    year = "2024",
                    coverUrl = trList.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                    trackCount = trList.size,
                    tracks = trList
                )
            )
        }
        if (audiusAlbums.isNotEmpty()) {
            albumsList.addAll(audiusAlbums)
        }

        // 6. Build related artists DTOs
        val relatedArtistDtos = resolved.relatedArtists.map { relName ->
            ArtistDto(
                id = "art_" + relName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "_"),
                name = relName,
                genre = "Similar Artist",
                bio = "Listeners who enjoy $canonicalName also listen to $relName",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                followersCount = 3500000,
                verified = true
            )
        }

        // 7. Paginated response window
        val paginatedSlice = rankedAll.drop(cursor).take(pageSize)
        val nextCursorVal = if (cursor + pageSize < rankedAll.size) (cursor + pageSize).toString() else null
        val hasMoreVal = nextCursorVal != null

        val artistDto = ArtistDto(
            id = resolved.canonicalId,
            name = resolved.canonicalName,
            genre = resolved.genres.firstOrNull() ?: "Top Global Artist",
            bio = resolved.bio,
            imageUrl = resolved.imageUrl,
            followersCount = resolved.followersCount.toInt(),
            verified = resolved.isVerified
        )

        ArtistCatalogResponse(
            artist = artistDto,
            popularTracks = if (popular.isNotEmpty()) popular else paginatedSlice.take(10),
            albums = albumsList.take(8),
            singlesAndEps = singles,
            collaborations = collaborations,
            remixes = remixes,
            relatedArtists = relatedArtistDtos,
            allTracks = paginatedSlice,
            nextCursor = nextCursorVal,
            hasMore = hasMoreVal
        )
    }

    suspend fun getMoreTracks(
        artistName: String,
        cursor: Int = 0,
        pageSize: Int = 30
    ): List<TrackDto> = coroutineScope {
        val resolved = artistResolver.resolve(artistName)
        val tracksDeferred = async { saavnProvider.search(resolved.canonicalName, limit = cursor + pageSize + 20).getOrDefault(emptyList()) }
        val audiusDeferred = async { audiusProvider.search(resolved.canonicalName, limit = 30).getOrDefault(emptyList()) }

        val combined = (tracksDeferred.await() + audiusDeferred.await())
        val deduplicated = deduplicationService.deduplicate(combined)
        val ranked = searchRankingEngine.rankSearchResults(deduplicated, resolved.canonicalName)

        ranked.drop(cursor).take(pageSize)
    }
}
