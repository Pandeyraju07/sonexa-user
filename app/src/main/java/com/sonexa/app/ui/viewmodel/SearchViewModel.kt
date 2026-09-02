package com.sonexa.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.TrackDto
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
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine(),
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProviderCategory.ALL)
    val selectedCategory: StateFlow<ProviderCategory> = _selectedCategory.asStateFlow()

    private val _heroCategories = MutableStateFlow<List<BrowseCategoryItem>>(emptyList())
    val heroCategories: StateFlow<List<BrowseCategoryItem>> = _heroCategories.asStateFlow()

    private val _discoverItems = MutableStateFlow<List<DiscoverItem>>(emptyList())
    val discoverItems: StateFlow<List<DiscoverItem>> = _discoverItems.asStateFlow()

    private val _browseAllCategories = MutableStateFlow<List<BrowseCategoryItem>>(emptyList())
    val browseAllCategories: StateFlow<List<BrowseCategoryItem>> = _browseAllCategories.asStateFlow()

    private val _recents = MutableStateFlow<List<RecentSearchItem>>(emptyList())
    val recents: StateFlow<List<RecentSearchItem>> = _recents.asStateFlow()

    private var currentQuery: String = ""
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
                _recents.value = list
            } catch (_: Exception) {}
        }
    }

    private fun persistRecents(items: List<RecentSearchItem>) {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("sonexa_search_recents", Context.MODE_PRIVATE)
            prefs.edit().putString("recent_items", gson.toJson(items)).apply()
        }
    }

    fun loadDynamicCategories() {
        viewModelScope.launch {
            // 1. Try Backend API first
            val categoriesResult = musicRepository.getSearchCategories()
            if (categoriesResult.isSuccess && categoriesResult.getOrNull()?.heroCategories?.isNotEmpty() == true) {
                val data = categoriesResult.getOrNull()!!
                _heroCategories.value = data.heroCategories.map {
                    BrowseCategoryItem(it.id, it.title, it.colorHex, it.imageUrl, it.query)
                }
                _discoverItems.value = data.discoverTags.map {
                    DiscoverItem(it.id, it.tag, it.title, it.imageUrl, it.query)
                }
                _browseAllCategories.value = data.browseCategories.map {
                    BrowseCategoryItem(it.id, it.title, it.colorHex, it.imageUrl, it.query)
                }
                return@launch
            }

            // 2. Dynamic Streaming Aggregation Engine Fallback
            try {
                val trending = aggregationEngine.jiosaavnProvider.getTrending(20).getOrDefault(emptyList())
                val hindiPop = aggregationEngine.jiosaavnProvider.search("Hindi Pop Hits", limit = 10).getOrDefault(emptyList())

                val musicCover = trending.firstOrNull()?.effectiveCoverUrl ?: "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg"
                val eventCover = trending.getOrNull(2)?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300"
                val ipopCover = hindiPop.firstOrNull()?.effectiveCoverUrl ?: "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg"

                _heroCategories.value = listOf(
                    BrowseCategoryItem("hero_music", "Music", 0xFFE1336E, musicCover, "Top Bollywood Songs"),
                    BrowseCategoryItem("hero_podcasts", "Podcasts", 0xFF006450, "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=300", "Top Podcasts"),
                    BrowseCategoryItem("hero_events", "Live\nEvents", 0xFF7358FF, eventCover, "Live Concerts"),
                    BrowseCategoryItem("hero_ipop", "Home of\nI-Pop", 0xFF1E3264, ipopCover, "Indian Pop Hits")
                )

                val dynamicDiscover = mutableListOf<DiscoverItem>()
                trending.take(6).forEachIndexed { index, tr ->
                    val tag = when (index) {
                        0 -> "#hindipop"
                        1 -> "#bollywood"
                        2 -> "#punjabi"
                        3 -> "#lofi"
                        4 -> "#acoustic"
                        else -> "#trending"
                    }
                    dynamicDiscover.add(DiscoverItem("disc_$index", tag, tr.title, tr.effectiveCoverUrl, tr.title, tr))
                }
                if (dynamicDiscover.isNotEmpty()) {
                    _discoverItems.value = dynamicDiscover
                }

                _browseAllCategories.value = listOf(
                    BrowseCategoryItem("cat_made_for_you", "Made\nFor You", 0xFF8C67AC, "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Made For You"),
                    BrowseCategoryItem("cat_new_releases", "New\nReleases", 0xFFE8115B, "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "New Releases"),
                    BrowseCategoryItem("cat_hindi", "Hindi", 0xFFE91429, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Hindi Songs"),
                    BrowseCategoryItem("cat_punjabi", "Punjabi", 0xFFB02897, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Top Punjabi Hits"),
                    BrowseCategoryItem("cat_charts", "Charts", 0xFF8D67AB, "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "Top 50 India"),
                    BrowseCategoryItem("cat_lofi", "Lo-Fi\nChill", 0xFF1E3264, "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Lo-Fi Beats"),
                    BrowseCategoryItem("cat_party", "Party &\nDance", 0xFF503750, "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Bollywood Party"),
                    BrowseCategoryItem("cat_romance", "Romance", 0xFFE8115B, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Romantic Hindi Songs"),
                    BrowseCategoryItem("cat_bhakti", "Devotional", 0xFF477D95, "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Bhakti Songs"),
                    BrowseCategoryItem("cat_workout", "Workout", 0xFF777777, "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=300", "Workout Motivation")
                )
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
            return
        }

        searchJob = viewModelScope.launch {
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

    private fun executeSearch(query: String, category: ProviderCategory) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            try {
                // 1. Search using Unified Aggregation Engine
                val result: UnifiedSearchResult = aggregationEngine.searchAll(
                    query = query,
                    selectedCategory = category,
                    limit = 35
                )

                // 2. Also query backend catalog for native tracks & albums
                val backendSearch = musicRepository.searchMusic(query).getOrNull()
                val nativeTracks = backendSearch?.tracks.orEmpty()
                val matchingAlbums = backendSearch?.albums.orEmpty()

                // Merge native tracks on top of streaming tracks
                val mergedTracks = (nativeTracks + result.tracks).distinctBy { it.id }

                val topArtist = if (mergedTracks.isNotEmpty()) {
                    val first = mergedTracks.first()
                    ArtistDto(
                        id = "art_" + first.artist.lowercase().replace(" ", "_"),
                        name = first.artist,
                        genre = "Top Artist",
                        bio = "Top matching artist for $query",
                        imageUrl = first.effectiveCoverUrl,
                        followersCount = 1250000,
                        verified = true
                    )
                } else null

                // Construct matching dynamic playlists
                val matchingPlaylists = listOf(
                    PlaylistDto(
                        id = "pl_srch_${query.lowercase().replace(" ", "_")}",
                        title = "$query Radio",
                        subtitle = "Playlist • Top tracks & artists related to $query",
                        coverUrl = mergedTracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                        trackCount = mergedTracks.size
                    ),
                    PlaylistDto(
                        id = "pl_best_${query.lowercase().replace(" ", "_")}",
                        title = "Best of $query",
                        subtitle = "Playlist • Essential Hits",
                        coverUrl = mergedTracks.getOrNull(1)?.effectiveCoverUrl.orEmpty(),
                        trackCount = mergedTracks.size
                    )
                )

                _uiState.value = SearchUiState.Success(
                    tracks = mergedTracks,
                    topArtist = topArtist,
                    matchingPlaylists = matchingPlaylists,
                    matchingAlbums = matchingAlbums,
                    artists = mergedTracks.map { it.artist }.distinct().take(5),
                    providerCounts = result.providerCounts,
                    providerLatencies = result.providerLatencies,
                    activeCategory = category
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed. Please try again.")
            }
        }
    }
}
