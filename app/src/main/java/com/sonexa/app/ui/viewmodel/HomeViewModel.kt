package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.model.*
import com.sonexa.app.data.provider.MusicAggregationEngine
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val continueListening: List<TrackDto>,
        val trendingNow: List<TrackDto>,
        val popularAlbums: List<AlbumDto>,
        val madeForYou: List<PlaylistDto>,
        val recommendedArtists: List<ArtistDto> = emptyList(),
        val moods: List<MoodDto> = emptyList(),
        val podcasts: List<PodcastDto> = emptyList(),
        val userDisplayName: String = ""
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel @JvmOverloads constructor(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            val feedDeferred = async { musicRepository.getHomeFeed() }
            val artistsDeferred = async { musicRepository.getArtists() }
            val moodsDeferred = async { musicRepository.getMoods() }
            val podcastsDeferred = async { musicRepository.getPodcasts() }
            val profileDeferred = async { userRepository.getUserProfile() }

            val feedResult = feedDeferred.await()
            val serverFeed = feedResult.getOrNull()

            if (serverFeed != null && (serverFeed.trendingNow.isNotEmpty() || serverFeed.continueListening.isNotEmpty())) {
                val artists = artistsDeferred.await().getOrNull()?.artists.orEmpty()
                val moods = moodsDeferred.await().getOrNull()?.moods.orEmpty()
                val podcasts = podcastsDeferred.await().getOrNull()?.podcasts.orEmpty()
                val profile = profileDeferred.await().getOrNull()?.user
                val name = profile?.name.orEmpty().ifBlank { "Music Lover" }

                _uiState.value = HomeUiState.Success(
                    continueListening = serverFeed.continueListening.map { it.sanitized() },
                    trendingNow = serverFeed.trendingNow.map { it.sanitized() },
                    popularAlbums = serverFeed.popularAlbums,
                    madeForYou = serverFeed.madeForYou,
                    recommendedArtists = artists,
                    moods = moods,
                    podcasts = podcasts,
                    userDisplayName = name
                )
            } else {
                // Fallback: Dynamically generate complete rich Home feed from active aggregation providers
                try {
                    val trendingTracksDeferred = async { aggregationEngine.getTrendingUnified(20) }
                    val popHitsDeferred = async { aggregationEngine.searchAll("Bollywood Hits", limit = 15).tracks }
                    val artistsResultDeferred = async { musicRepository.getArtists() }
                    val moodsResultDeferred = async { musicRepository.getMoods() }
                    val podcastsResultDeferred = async { musicRepository.getPodcasts() }

                    val trendingTracks = trendingTracksDeferred.await().map { it.sanitized() }
                    val popHits = popHitsDeferred.await().map { it.sanitized() }
                    val artistsFromApi = artistsResultDeferred.await().getOrNull()?.artists.orEmpty()
                    val moodsFromApi = moodsResultDeferred.await().getOrNull()?.moods.orEmpty()
                    val podcastsFromApi = podcastsResultDeferred.await().getOrNull()?.podcasts.orEmpty()

                    val continueListening = if (popHits.isNotEmpty()) popHits.take(6) else trendingTracks.take(6)
                    val trendingNow = if (trendingTracks.isNotEmpty()) trendingTracks else popHits

                    // Derive dynamic albums from top tracks
                    val dynamicAlbums = (trendingNow + popHits).map { track ->
                        AlbumDto(
                            id = "alb_${track.id}",
                            title = track.album?.ifBlank { track.title } ?: track.title,
                            artist = track.artist,
                            year = "2026",
                            coverUrl = track.effectiveCoverUrl,
                            trackCount = 10
                        )
                    }.distinctBy { it.title }.take(8)

                    // Derive dynamic playlists
                    val dynamicPlaylists = listOf(
                        PlaylistDto("pl_1", "Today's Top Hits", "Most played tracks right now", "gradient", continueListening.firstOrNull()?.effectiveCoverUrl.orEmpty()),
                        PlaylistDto("pl_2", "Viral Hits 2026", "Trending sounds & anthems", "gradient", trendingNow.getOrNull(1)?.effectiveCoverUrl.orEmpty()),
                        PlaylistDto("pl_3", "Deep Focus & Chill", "Ambient beats to zone in", "gradient", popHits.getOrNull(2)?.effectiveCoverUrl.orEmpty()),
                        PlaylistDto("pl_4", "Mega Hit Mix", "Your daily curated mix", "gradient", trendingNow.firstOrNull()?.effectiveCoverUrl.orEmpty())
                    )

                    // Derive dynamic artists if API artists is empty
                    val resolvedArtists = if (artistsFromApi.isNotEmpty()) {
                        artistsFromApi
                    } else {
                        (trendingNow + popHits).map { track ->
                            val cleanArtist = track.artist.split(",", "&", "feat.").first().trim()
                            ArtistDto(
                                id = "art_${cleanArtist.hashCode()}",
                                name = cleanArtist,
                                genre = "Top Artist",
                                imageUrl = track.effectiveCoverUrl,
                                followersCount = 1250000,
                                verified = true
                            )
                        }.distinctBy { it.name }.take(8)
                    }

                    val defaultMoods = if (moodsFromApi.isNotEmpty()) moodsFromApi else listOf(
                        MoodDto("1", "Chill", "headphones", "#8B5CF6"),
                        MoodDto("2", "Workout", "flash_on", "#EC4899"),
                        MoodDto("3", "Focus", "lightbulb", "#3B82F6"),
                        MoodDto("4", "Party", "celebration", "#F59E0B"),
                        MoodDto("5", "Romance", "favorite", "#EF4444")
                    )

                    _uiState.value = HomeUiState.Success(
                        continueListening = continueListening,
                        trendingNow = trendingNow,
                        popularAlbums = dynamicAlbums,
                        madeForYou = dynamicPlaylists,
                        recommendedArtists = resolvedArtists,
                        moods = defaultMoods,
                        podcasts = podcastsFromApi,
                        userDisplayName = "Music Lover"
                    )
                } catch (e: Exception) {
                    _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load music feed")
                }
            }
        }
    }

    fun toggleLikeSong(trackId: String) {
        viewModelScope.launch {
            userRepository.toggleLikeSong(trackId)
        }
    }
}
