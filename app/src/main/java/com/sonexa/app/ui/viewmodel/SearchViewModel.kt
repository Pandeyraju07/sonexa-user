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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrowseCategoryItem(
    val id: String,
    val title: String,
    val colorHex: Long,
    val imageUrl: String,
    val query: String
)

data class DiscoverItem(
    val id: String,
    val tag: String,
    val title: String,
    val imageUrl: String,
    val query: String,
    val track: TrackDto? = null
)

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

    // 1. Dynamic Hero Categories (2x2 Grid)
    private val _heroCategories = MutableStateFlow<List<BrowseCategoryItem>>(
        listOf(
            BrowseCategoryItem("hero_music", "Music", 0xFFE1336E, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Songs"),
            BrowseCategoryItem("hero_podcasts", "Podcasts", 0xFF006450, "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=300", "Podcasts"),
            BrowseCategoryItem("hero_events", "Live\nEvents", 0xFF7358FF, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300", "Live Concert"),
            BrowseCategoryItem("hero_ipop", "Home of\nI-Pop", 0xFF1E3264, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Indian Pop Hits")
        )
    )
    val heroCategories: StateFlow<List<BrowseCategoryItem>> = _heroCategories.asStateFlow()

    // 2. Dynamic "Discover something new" Items
    private val _discoverItems = MutableStateFlow<List<DiscoverItem>>(
        listOf(
            DiscoverItem("disc_1", "#hindi pop", "Saregama Open Stage Covers", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Hindi Pop"),
            DiscoverItem("disc_2", "#falak", "Falak Live & Acoustic", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "Falak"),
            DiscoverItem("disc_3", "#pop", "Pop Global Sensation", "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Global Pop Hits"),
            DiscoverItem("disc_4", "#punjabi", "Punjabi Wave 2026", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Punjabi Top Hits"),
            DiscoverItem("disc_5", "#bollywood", "Bollywood Melodies", "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Bollywood Hits"),
            DiscoverItem("disc_6", "#lofi", "Midnight Hindi Lo-Fi", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Hindi Lo-Fi Chill")
        )
    )
    val discoverItems: StateFlow<List<DiscoverItem>> = _discoverItems.asStateFlow()

    // 3. Dynamic "Browse all" Categories (2-Column Grid)
    private val _browseAllCategories = MutableStateFlow<List<BrowseCategoryItem>>(
        listOf(
            BrowseCategoryItem("cat_made_for_you", "Made\nFor You", 0xFF8C67AC, "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Made For You Mix"),
            BrowseCategoryItem("cat_upcoming", "Upcoming\nreleases", 0xFF007F5F, "https://c.saavncdn.com/530/Ye-Baarish-Hindi-2023-20230628172810-500x500.jpg", "New Releases 2026"),
            BrowseCategoryItem("cat_new_releases", "New\nReleases", 0xFF477D32, "https://c.saavncdn.com/393/Halki-Si-Barsaat-Hindi-2022-20220608143808-500x500.jpg", "Latest Songs 2026"),
            BrowseCategoryItem("cat_monsoon", "Rain &\nMonsoon", 0xFF1E5BB0, "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Monsoon Rain Songs"),
            BrowseCategoryItem("cat_bollywood", "Bollywood", 0xFFE76F51, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Bollywood Hits"),
            BrowseCategoryItem("cat_punjabi", "Punjabi", 0xFFF4A261, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Punjabi Top Hits"),
            BrowseCategoryItem("cat_bhojpuri", "Bhojpuri", 0xFFE63946, "https://c.saavncdn.com/artists/Neelkamal_Singh_000_20220616084050_500x500.jpg", "Bhojpuri Hits"),
            BrowseCategoryItem("cat_pop", "Pop", 0xFFD81159, "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Global Pop"),
            BrowseCategoryItem("cat_romance", "Romance", 0xFF8338EC, "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "Romantic Hits"),
            BrowseCategoryItem("cat_chill", "Chill &\nLo-Fi", 0xFF2A9D8F, "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Lo-Fi Beats"),
            BrowseCategoryItem("cat_workout", "Workout", 0xFFD90429, "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Workout Energetic"),
            BrowseCategoryItem("cat_devotional", "Devotional", 0xFFFFB703, "https://c.saavncdn.com/artists/Satinder_Sartaaj_500x500.jpg", "Bhakti Songs")
        )
    )
    val browseAllCategories: StateFlow<List<BrowseCategoryItem>> = _browseAllCategories.asStateFlow()

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

    init {
        loadDynamicCategoriesAndDiscover()
    }

    private fun loadDynamicCategoriesAndDiscover() {
        viewModelScope.launch {
            try {
                // Fetch real live trending data from API
                val trendingDeferred = async { aggregationEngine.jiosaavnProvider.getTrending(20) }
                val hindiPopDeferred = async { aggregationEngine.jiosaavnProvider.search("Hindi Pop", limit = 10) }
                val punjabiDeferred = async { aggregationEngine.jiosaavnProvider.search("Punjabi Top Hits", limit = 10) }

                val trending = trendingDeferred.await().getOrDefault(emptyList())
                val hindiPop = hindiPopDeferred.await().getOrDefault(emptyList())
                val punjabi = punjabiDeferred.await().getOrDefault(emptyList())

                if (trending.isNotEmpty()) {
                    // Update Hero category covers dynamically with real API covers
                    val musicCover = trending.firstOrNull()?.effectiveCoverUrl ?: _heroCategories.value[0].imageUrl
                    val podcastCover = _heroCategories.value[1].imageUrl
                    val eventCover = trending.getOrNull(2)?.effectiveCoverUrl ?: _heroCategories.value[2].imageUrl
                    val ipopCover = hindiPop.firstOrNull()?.effectiveCoverUrl ?: _heroCategories.value[3].imageUrl

                    _heroCategories.value = listOf(
                        _heroCategories.value[0].copy(imageUrl = musicCover),
                        _heroCategories.value[1].copy(imageUrl = podcastCover),
                        _heroCategories.value[2].copy(imageUrl = eventCover),
                        _heroCategories.value[3].copy(imageUrl = ipopCover)
                    )

                    // Update Discover items with live API tracks & covers
                    val dynamicDiscover = mutableListOf<DiscoverItem>()
                    hindiPop.firstOrNull()?.let {
                        dynamicDiscover.add(DiscoverItem("disc_1", "#hindi pop", it.title, it.effectiveCoverUrl, "Hindi Pop", it))
                    }
                    trending.getOrNull(1)?.let {
                        dynamicDiscover.add(DiscoverItem("disc_2", "#falak", it.title, it.effectiveCoverUrl, it.artist, it))
                    }
                    trending.getOrNull(3)?.let {
                        dynamicDiscover.add(DiscoverItem("disc_3", "#pop", it.title, it.effectiveCoverUrl, "Pop Hits", it))
                    }
                    punjabi.firstOrNull()?.let {
                        dynamicDiscover.add(DiscoverItem("disc_4", "#punjabi", it.title, it.effectiveCoverUrl, "Punjabi Hits", it))
                    }
                    trending.getOrNull(4)?.let {
                        dynamicDiscover.add(DiscoverItem("disc_5", "#bollywood", it.title, it.effectiveCoverUrl, "Bollywood", it))
                    }
                    trending.getOrNull(5)?.let {
                        dynamicDiscover.add(DiscoverItem("disc_6", "#lofi", it.title, it.effectiveCoverUrl, "Lo-Fi Chill", it))
                    }

                    if (dynamicDiscover.isNotEmpty()) {
                        _discoverItems.value = dynamicDiscover
                    }
                }
            } catch (_: Exception) {
                // Keep default high-res curated items if network is unavailable
            }
        }
    }

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

    fun playCategoryOrTagDirect(
        query: String,
        fallbackTitle: String,
        onReadyToPlay: (List<TrackDto>, Int, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cleaned = query.replace("#", "").trim()
                val result = aggregationEngine.searchAll(cleaned, limit = 25)
                val tracks = result.tracks
                if (tracks.isNotEmpty()) {
                    onReadyToPlay(tracks, 0, "$fallbackTitle ($cleaned)")
                } else {
                    // Fallback to direct search
                    searchCategoryDirect(cleaned)
                }
            } catch (e: Exception) {
                searchCategoryDirect(query)
            }
        }
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
                            year = "2026",
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

