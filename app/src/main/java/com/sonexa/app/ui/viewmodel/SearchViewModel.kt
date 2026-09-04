package com.sonexa.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.*
import com.sonexa.app.data.provider.MusicAggregationEngine
import com.sonexa.app.data.provider.ProviderCategory
import com.sonexa.app.data.provider.UnifiedSearchResult
import com.sonexa.app.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        val response: com.sonexa.app.data.search.UnifiedSearchResponse,
        val tracks: List<TrackDto>,
        val movieSoundtrack: com.sonexa.app.data.provider.MovieSoundtrack? = null,
        val topArtist: ArtistDto? = null,
        val resolvedArtist: ResolvedArtist? = null,
        val artistCatalog: ArtistCatalogResponse? = null,
        val matchingPlaylists: List<PlaylistDto> = emptyList(),
        val matchingAlbums: List<AlbumDto> = emptyList(),
        val artists: List<String> = emptyList(),
        val providerCounts: Map<String, Int> = emptyMap(),
        val providerLatencies: Map<String, Long> = emptyMap(),
        val activeCategory: ProviderCategory = ProviderCategory.ALL,
        val hasMoreTracks: Boolean = true,
        val isLoadingMore: Boolean = false,
        val suggestions: List<SearchSuggestionDto> = emptyList(),
        val didYouMean: com.sonexa.app.data.search.DidYouMeanSuggestion? = null
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine(),
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val gson = com.sonexa.app.data.api.RetrofitClient.gson
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProviderCategory.ALL)
    val selectedCategory: StateFlow<ProviderCategory> = _selectedCategory.asStateFlow()

    private val defaultHeroCategories = listOf(
        BrowseCategoryItem("hero_music", "Music", 0xFFE1336E, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Bollywood Songs"),
        BrowseCategoryItem("hero_podcasts", "Podcasts", 0xFF006450, "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=500", "Top Podcasts"),
        BrowseCategoryItem("hero_events", "Live\nEvents", 0xFF7358FF, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500", "Live Concerts"),
        BrowseCategoryItem("hero_ipop", "Home of\nI-Pop", 0xFF1E3264, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Indian Pop Hits")
    )

    private val defaultDiscoverItems = listOf(
        DiscoverItem("disc_hindipop", "#hindipop", "Hindi Pop", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Hindi Pop Hits"),
        DiscoverItem("disc_bollywood", "#bollywood", "Bollywood", "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Bollywood Hits"),
        DiscoverItem("disc_punjabi", "#punjabi", "Punjabi Wave", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Top Punjabi Hits"),
        DiscoverItem("disc_lofi", "#lofi", "Lo-Fi Chill", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Lo-Fi Beats"),
        DiscoverItem("disc_romantic", "#romance", "Romantic Melodies", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "Romantic Hindi Songs"),
        DiscoverItem("disc_acoustic", "#acoustic", "Acoustic Live", "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=500", "Acoustic Hindi Hits"),
        DiscoverItem("disc_party", "#party", "Club Anthem", "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Bollywood Party Hits"),
        DiscoverItem("disc_trending", "#trending", "Trending India", "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "Top Trending Songs")
    )

    private val defaultBrowseAll = listOf(
        BrowseCategoryItem("cat_made_for_you", "Made\nFor You", 0xFF8C67AC, "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Made For You"),
        BrowseCategoryItem("cat_new_releases", "New\nReleases", 0xFFE8115B, "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "New Releases"),
        BrowseCategoryItem("cat_hindi", "Hindi", 0xFFE91429, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Hindi Songs"),
        BrowseCategoryItem("cat_punjabi", "Punjabi", 0xFFB02897, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Top Punjabi Hits"),
        BrowseCategoryItem("cat_charts", "Charts", 0xFF8D67AB, "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "Top 50 India"),
        BrowseCategoryItem("cat_lofi", "Lo-Fi\nChill", 0xFF1E3264, "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Lo-Fi Beats"),
        BrowseCategoryItem("cat_party", "Party &\nDance", 0xFF503750, "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Bollywood Party"),
        BrowseCategoryItem("cat_romance", "Romance", 0xFFE8115B, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Romantic Hindi Songs"),
        BrowseCategoryItem("cat_bhakti", "Devotional", 0xFF477D95, "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Bhakti Songs"),
        BrowseCategoryItem("cat_workout", "Workout", 0xFF777777, "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500", "Workout Motivation")
    )

    private val _heroCategories = MutableStateFlow<List<BrowseCategoryItem>>(defaultHeroCategories)
    val heroCategories: StateFlow<List<BrowseCategoryItem>> = _heroCategories.asStateFlow()

    private val _discoverItems = MutableStateFlow<List<DiscoverItem>>(defaultDiscoverItems)
    val discoverItems: StateFlow<List<DiscoverItem>> = _discoverItems.asStateFlow()

    private val _browseAllCategories = MutableStateFlow<List<BrowseCategoryItem>>(defaultBrowseAll)
    val browseAllCategories: StateFlow<List<BrowseCategoryItem>> = _browseAllCategories.asStateFlow()

    private val _recents = MutableStateFlow<List<RecentSearchItem>>(emptyList())
    val recents: StateFlow<List<RecentSearchItem>> = _recents.asStateFlow()

    private val _suggestions = MutableStateFlow<List<SearchSuggestionDto>>(emptyList())
    val suggestions: StateFlow<List<SearchSuggestionDto>> = _suggestions.asStateFlow()

    private var currentQuery: String = ""
    private var currentCursor: Int = 0
    private var searchJob: Job? = null
    private var appContext: Context? = null

    init {
        loadDynamicCategories()
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        loadPersistedRecents(context)
    }

    private fun loadPersistedRecents(context: Context) {
        val prefs = context.getSharedPreferences("sonexa_search_recents", Context.MODE_PRIVATE)
        val json = prefs.getString("recent_items", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<RecentSearchItem>>() {}.type
                val list: List<RecentSearchItem> = gson.fromJson(json, type)
                _recents.value = list.map { it.copy(track = it.track?.sanitized()) }
            } catch (_: Exception) {}
        }
    }

    private fun persistRecents(items: List<RecentSearchItem>) {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("sonexa_search_recents", Context.MODE_PRIVATE)
            val cleanItems = items.map { it.copy(track = it.track?.sanitized()) }
            prefs.edit().putString("recent_items", gson.toJson(cleanItems)).apply()
        }
    }

    fun loadDynamicCategories() {
        viewModelScope.launch {
            // 1. Try Backend API first
            val categoriesResult = musicRepository.getSearchCategories()
            if (categoriesResult.isSuccess && categoriesResult.getOrNull()?.heroCategories?.isNotEmpty() == true) {
                val data = categoriesResult.getOrNull()!!
                _heroCategories.value = data.heroCategories.map {
                    BrowseCategoryItem(it.id, it.title, it.colorHex, it.imageUrl.ifBlank { "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg" }, it.query)
                }
                _discoverItems.value = data.discoverTags.mapIndexed { idx, it ->
                    val fallbackCover = defaultDiscoverItems.getOrNull(idx)?.imageUrl ?: "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg"
                    DiscoverItem(it.id, it.tag, it.title, it.imageUrl.ifBlank { fallbackCover }, it.query)
                }
                _browseAllCategories.value = data.browseCategories.map {
                    BrowseCategoryItem(it.id, it.title, it.colorHex, it.imageUrl.ifBlank { "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg" }, it.query)
                }
                return@launch
            }

            // 2. Dynamic Streaming Aggregation Engine Fallback
            try {
                val trending = aggregationEngine.jiosaavnProvider.getTrending(20).getOrDefault(emptyList())
                val hindiPop = aggregationEngine.jiosaavnProvider.search("Hindi Pop Hits", limit = 10).getOrDefault(emptyList())

                val musicCover = trending.firstOrNull()?.effectiveCoverUrl?.takeIf { it.isNotBlank() } ?: defaultHeroCategories[0].imageUrl
                val eventCover = trending.getOrNull(2)?.effectiveCoverUrl?.takeIf { it.isNotBlank() } ?: defaultHeroCategories[2].imageUrl
                val ipopCover = hindiPop.firstOrNull()?.effectiveCoverUrl?.takeIf { it.isNotBlank() } ?: defaultHeroCategories[3].imageUrl

                _heroCategories.value = listOf(
                    BrowseCategoryItem("hero_music", "Music", 0xFFE1336E, musicCover, "Top Bollywood Songs"),
                    BrowseCategoryItem("hero_podcasts", "Podcasts", 0xFF006450, "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=500", "Top Podcasts"),
                    BrowseCategoryItem("hero_events", "Live\nEvents", 0xFF7358FF, eventCover, "Live Concerts"),
                    BrowseCategoryItem("hero_ipop", "Home of\nI-Pop", 0xFF1E3264, ipopCover, "Indian Pop Hits")
                )

                val updatedDiscover = defaultDiscoverItems.mapIndexed { index, defaultItem ->
                    val matchingTrack = when (index) {
                        0 -> hindiPop.firstOrNull()
                        1 -> trending.firstOrNull()
                        2 -> trending.getOrNull(1)
                        3 -> trending.getOrNull(3)
                        else -> trending.getOrNull(index)
                    }
                    val cover = matchingTrack?.effectiveCoverUrl?.takeIf { it.isNotBlank() } ?: defaultItem.imageUrl
                    defaultItem.copy(
                        title = matchingTrack?.title ?: defaultItem.title,
                        imageUrl = cover,
                        track = matchingTrack
                    )
                }
                _discoverItems.value = updatedDiscover
            } catch (_: Exception) {}
        }
    }

    fun removeRecent(id: String) {
        val updated = _recents.value.filter { it.id != id }
        _recents.value = updated
        persistRecents(updated)
    }

    fun clearAllRecents() {
        _recents.value = emptyList()
        persistRecents(emptyList())
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
        val updated = listOf(newItem) + _recents.value.filter { it.id != track.id }.take(15)
        _recents.value = updated
        persistRecents(updated)
    }

    fun addRecentQuery(title: String, subtitle: String = "Search", imageUrl: String = "", type: String = "search") {
        val newItem = RecentSearchItem(
            id = "rec_" + title.lowercase().replace(" ", "_"),
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl,
            type = type
        )
        val updated = listOf(newItem) + _recents.value.filter { !it.title.equals(title, ignoreCase = true) }.take(15)
        _recents.value = updated
        persistRecents(updated)
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
            _suggestions.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            // Instant debounced autocomplete suggestions
            val suggestList = aggregationEngine.getSearchSuggestions(query)
            _suggestions.value = suggestList

            delay(250) // Debounce typing
            executeSearch(query, _selectedCategory.value)
        }
    }

    fun playCategoryOrTagDirect(
        query: String,
        title: String,
        onPlay: (List<TrackDto>, Int, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = aggregationEngine.searchAll(query = query, limit = 20)
            if (result.tracks.isNotEmpty()) {
                onPlay(result.tracks, 0, title)
            }
        }
    }

    fun loadMoreTracks() {
        val currentState = _uiState.value as? SearchUiState.Success ?: return
        if (currentState.isLoadingMore || !currentState.hasMoreTracks || currentQuery.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                if (it is SearchUiState.Success) it.copy(isLoadingMore = true) else it
            }

            try {
                currentCursor += 25
                val moreTracks = aggregationEngine.artistCatalogService.getMoreTracks(
                    artistName = currentQuery,
                    cursor = currentCursor,
                    pageSize = 25
                )

                if (moreTracks.isEmpty()) {
                    _uiState.update {
                        if (it is SearchUiState.Success) it.copy(isLoadingMore = false, hasMoreTracks = false) else it
                    }
                } else {
                    _uiState.update {
                        if (it is SearchUiState.Success) {
                            val combined = (it.tracks + moreTracks).distinctBy { t -> t.id }
                            it.copy(
                                tracks = combined,
                                isLoadingMore = false,
                                hasMoreTracks = moreTracks.size >= 10
                            )
                        } else it
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    if (it is SearchUiState.Success) it.copy(isLoadingMore = false) else it
                }
            }
        }
    }

    private fun executeSearch(query: String, category: ProviderCategory) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            currentCursor = 0

            try {
                // Execute deep orchestration pipeline (NLP, Devanagari transliteration, typo correction, parallel search, ranking)
                val searchResponse = aggregationEngine.searchUnifiedDeep(
                    query = query,
                    selectedCategory = category,
                    limit = 40
                )

                val mergedTracks = searchResponse.allTracks
                val movieMatch = searchResponse.movieSoundtrack
                val topArtist = searchResponse.topArtist
                val resolvedArtist = searchResponse.resolvedArtist

                // If query matches an artist strongly, retrieve rich discography catalog
                var artistCatalog: ArtistCatalogResponse? = null
                if (topArtist != null) {
                    val isArtistQuery = topArtist.name.contains(query, ignoreCase = true) ||
                            query.contains(topArtist.name, ignoreCase = true)
                    if (isArtistQuery) {
                        artistCatalog = runCatching {
                            aggregationEngine.getArtistFullCatalog(topArtist.name, cursor = 0, pageSize = 30)
                        }.getOrNull()
                    }
                }

                _uiState.value = SearchUiState.Success(
                    response = searchResponse,
                    tracks = mergedTracks,
                    movieSoundtrack = movieMatch,
                    topArtist = topArtist,
                    resolvedArtist = resolvedArtist,
                    artistCatalog = artistCatalog,
                    matchingPlaylists = searchResponse.matchingPlaylists,
                    matchingAlbums = searchResponse.matchingAlbums,
                    artists = mergedTracks.map { it.artist }.distinct().take(6),
                    providerCounts = searchResponse.providerCounts,
                    providerLatencies = searchResponse.providerLatencies,
                    activeCategory = category,
                    hasMoreTracks = mergedTracks.size >= 15,
                    isLoadingMore = false,
                    suggestions = _suggestions.value,
                    didYouMean = searchResponse.didYouMean
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed. Please try again.")
            }
        }
    }
}
