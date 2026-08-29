package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.MusicAggregationEngine
import com.sonexa.app.data.provider.ProviderCategory
import com.sonexa.app.data.provider.UnifiedSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecentSearchItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val type: String, // "song", "artist", "album", "playlist"
    val isSaved: Boolean = false,
    val track: TrackDto? = null
)

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(
        val tracks: List<TrackDto>,
        val topArtist: ArtistDto? = null,
        val matchingPlaylists: List<PlaylistDto> = emptyList(),
        val matchingAlbums: List<AlbumDto> = emptyList(),
        val artists: List<String> = emptyList(),
        val providerCounts: Map<String, Int> = emptyMap(),
        val providerLatencies: Map<String, Long> = emptyMap(),
        val activeCategory: ProviderCategory = ProviderCategory.ALL
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProviderCategory.ALL)
    val selectedCategory: StateFlow<ProviderCategory> = _selectedCategory.asStateFlow()

    private val _recents = MutableStateFlow<List<RecentSearchItem>>(
        listOf(
            RecentSearchItem("rec_1", "Love Exit", "Song • Jind Universe", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "song"),
            RecentSearchItem("rec_2", "Darmiyaan (From \"Musaf...\")", "Song • Rekha Bhardwaj, Raghav...", "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "song"),
            RecentSearchItem("rec_3", "Barsaat Lagdi Ae", "Song • Darshan Raval, Simran...", "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "song"),
            RecentSearchItem("rec_4", "Neelkamal Singh", "Artist", "https://c.saavncdn.com/artists/Neelkamal_Singh_000_20220616084050_500x500.jpg", "artist"),
            RecentSearchItem("rec_5", "Dooron Dooron", "Single • Paresh Pahuja, Shiv Ta...", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "song"),
            RecentSearchItem("rec_6", "Main Vaapas Aaunga", "Album • A.R. Rahman, Irshad K...", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "album", isSaved = true),
            RecentSearchItem("rec_7", "Chand Mera Dil", "Album • Sachin-Jigar, Amitabh...", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "album"),
            RecentSearchItem("rec_8", "Tujhko - From \"Cocktail 2\"", "Song • Pritam, Arijit Singh, Suni...", "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "song", isSaved = true),
            RecentSearchItem("rec_9", "Ye Baarish", "Song • Darshan Raval", "https://c.saavncdn.com/530/Ye-Baarish-Hindi-2023-20230628172810-500x500.jpg", "song", isSaved = true),
            RecentSearchItem("rec_10", "Halki Si Barsaat", "Song • Saaj Bhatt", "https://c.saavncdn.com/393/Halki-Si-Barsaat-Hindi-2022-20220608143808-500x500.jpg", "song", isSaved = true)
        )
    )
    val recents: StateFlow<List<RecentSearchItem>> = _recents.asStateFlow()

    private var currentQuery: String = ""
    private var searchJob: Job? = null

    fun removeRecent(id: String) {
        _recents.value = _recents.value.filter { it.id != id }
    }

    fun clearAllRecents() {
        _recents.value = emptyList()
    }

    fun addRecentTrack(track: TrackDto) {
        val newItem = RecentSearchItem(
            id = track.id,
            title = track.title,
            subtitle = "Song • ${track.artist}",
            imageUrl = track.effectiveCoverUrl,
            type = "song",
            isSaved = track.isLiked,
            track = track
        )
        _recents.value = listOf(newItem) + _recents.value.filter { it.id != track.id }.take(15)
    }

    fun onCategorySelected(category: ProviderCategory) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        if (currentQuery.isNotBlank()) {
            executeSearch(currentQuery, category)
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // Debounce user typing
            executeSearch(query, _selectedCategory.value)
        }
    }

    fun searchCategoryDirect(categoryQuery: String) {
        currentQuery = categoryQuery
        executeSearch(categoryQuery, ProviderCategory.ALL)
    }

    private fun executeSearch(query: String, category: ProviderCategory) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                // Strip "playlist", "songs", "all songs" for broader singer/track resolution
                val cleanedQuery = query.replace("playlist", "", ignoreCase = true)
                    .replace("all songs", "", ignoreCase = true)
                    .replace("songs", "", ignoreCase = true)
                    .trim()
                    .ifBlank { query.trim() }

                val unified: UnifiedSearchResult = aggregationEngine.searchAll(
                    query = cleanedQuery,
                    selectedCategory = category,
                    limit = 40
                )

                val tracks = unified.tracks
                val distinctArtists = tracks.map { it.artist.split(",", "&", "feat.").first().trim() }.distinct()

                // Check if search matches an artist directly
                var topArtist: ArtistDto? = null
                val matchedArtistName = distinctArtists.firstOrNull {
                    it.contains(cleanedQuery, ignoreCase = true) || cleanedQuery.contains(it, ignoreCase = true)
                }

                if (matchedArtistName != null) {
                    val artistTrack = tracks.firstOrNull { it.artist.contains(matchedArtistName, ignoreCase = true) }
                    topArtist = ArtistDto(
                        id = "art_${matchedArtistName.lowercase().replace(" ", "_")}",
                        name = matchedArtistName,
                        genre = "Artist",
                        bio = "Top songs and latest releases from $matchedArtistName on Sonexa.",
                        imageUrl = artistTrack?.effectiveCoverUrl.orEmpty(),
                        followersCount = 4250000,
                        verified = true
                    )
                }

                // Create matching playlists/albums dynamically
                val playlists = listOf(
                    PlaylistDto(
                        id = "pl_${cleanedQuery.lowercase().replace(" ", "_")}",
                        title = "$cleanedQuery - Best Hits",
                        subtitle = "Curated automatically for your vibe",
                        artworkType = "gradient",
                        coverUrl = tracks.firstOrNull()?.effectiveCoverUrl.orEmpty()
                    ),
                    PlaylistDto(
                        id = "pl_radio_${cleanedQuery.lowercase().replace(" ", "_")}",
                        title = "$cleanedQuery Radio",
                        subtitle = "With related songs & similar artists",
                        artworkType = "gradient",
                        coverUrl = tracks.getOrNull(1)?.effectiveCoverUrl.orEmpty()
                    )
                )

                val albums = tracks.mapNotNull { it.album }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(4)
                    .mapIndexed { idx, albName ->
                        AlbumDto(
                            id = "alb_${albName.lowercase().replace(" ", "_")}",
                            title = albName,
                            artist = tracks.firstOrNull { it.album == albName }?.artist ?: cleanedQuery,
                            year = "2024",
                            coverUrl = tracks.firstOrNull { it.album == albName }?.effectiveCoverUrl.orEmpty()
                        )
                    }

                _uiState.value = SearchUiState.Success(
                    tracks = tracks,
                    topArtist = topArtist,
                    matchingPlaylists = playlists,
                    matchingAlbums = albums,
                    artists = distinctArtists.take(6),
                    providerCounts = unified.providerCounts,
                    providerLatencies = unified.providerLatencies,
                    activeCategory = category
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.localizedMessage ?: "Search failed")
            }
        }
    }
}
