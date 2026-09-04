package com.sonexa.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.local.AudioCacheManager
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.model.*
import com.sonexa.app.data.repository.AiRepository
import com.sonexa.app.data.repository.AppConfigRepository
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreViewModel(
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<HomeFeedResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<HomeFeedResponse>> = _uiState.asStateFlow()
    private val _genres = MutableStateFlow<List<GenreDto>>(emptyList())
    val genres: StateFlow<List<GenreDto>> = _genres.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            musicRepository.getHomeFeed().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
            musicRepository.getGenres().onSuccess { _genres.value = it.genres }
        }
    }
}

class LibraryViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<UserLibraryResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<UserLibraryResponse>> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow("Recents") // "Recents", "Recently Added", "Alphabetical", "Creator"
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    init {
        load()
    }

    fun load(context: android.content.Context? = null) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            
            // Query API library + dynamic fallbacks
            val libResult = userRepository.getUserLibrary()
            val apiLibrary = libResult.getOrNull()
            
            if (apiLibrary != null && (apiLibrary.playlists.isNotEmpty() || apiLibrary.savedAlbums.isNotEmpty() || apiLibrary.followedArtists.isNotEmpty())) {
                _uiState.value = CatalogUiState.Ready(apiLibrary)
                if (context != null && apiLibrary.playlists.isNotEmpty()) {
                    com.sonexa.app.data.local.UserPlaylistStore.syncFromApi(context, apiLibrary.playlists)
                }
            } else {
                // Fallback: Dynamically aggregate user created playlists, liked songs and live library
                try {
                    val localPlaylists = com.sonexa.app.data.local.UserPlaylistStore.playlists.value
                    val likedSongs = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                    val musicRepo = com.sonexa.app.data.repository.MusicRepository()
                    val homeFeedResult = musicRepo.getHomeFeed().getOrNull()
                    val dynamicAlbums = homeFeedResult?.popularAlbums.orEmpty().take(4)
                    val dynamicArtists = musicRepo.getArtists().getOrNull()?.artists.orEmpty().take(4)

                    val fallback = UserLibraryResponse(
                        success = true,
                        playlists = localPlaylists,
                        likedSongs = likedSongs,
                        likedCount = likedSongs.size,
                        savedAlbums = dynamicAlbums,
                        followedArtists = dynamicArtists,
                        recentHistory = emptyList()
                    )
                    _uiState.value = CatalogUiState.Ready(fallback)
                } catch (e: Exception) {
                    val localPlaylists = com.sonexa.app.data.local.UserPlaylistStore.playlists.value
                    val likedSongs = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                    val fallback = UserLibraryResponse(
                        success = true,
                        playlists = localPlaylists,
                        likedSongs = likedSongs,
                        likedCount = likedSongs.size,
                        savedAlbums = emptyList(),
                        followedArtists = emptyList(),
                        recentHistory = emptyList()
                    )
                    _uiState.value = CatalogUiState.Ready(fallback)
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun createPlaylist(
        context: android.content.Context,
        title: String,
        description: String = "",
        coverUrl: String = "",
        onCreated: (PlaylistDto) -> Unit = {}
    ) {
        viewModelScope.launch {
            val created = com.sonexa.app.data.local.UserPlaylistStore.createPlaylist(
                context = context,
                title = title,
                description = description,
                coverUrl = coverUrl
            )
            userRepository.createPlaylist(title, description, coverUrl)
            load(context)
            onCreated(created)
        }
    }

    fun deletePlaylist(context: android.content.Context, id: String) {
        viewModelScope.launch {
            com.sonexa.app.data.local.UserPlaylistStore.deletePlaylist(context, id)
            userRepository.deletePlaylist(id)
            load(context)
        }
    }

    fun togglePin(context: android.content.Context, id: String) {
        com.sonexa.app.data.local.UserPlaylistStore.togglePin(context, id)
    }
}

class AlbumDetailViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val saavnProvider: com.sonexa.app.data.provider.JioSaavnMusicProvider = com.sonexa.app.data.provider.JioSaavnMusicProvider()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<AlbumDetailResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<AlbumDetailResponse>> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            val serverResult = musicRepository.getAlbum(id.ifBlank { "alb_1" })
            if (serverResult.isSuccess && serverResult.getOrNull()?.tracks?.isNotEmpty() == true) {
                _uiState.value = CatalogUiState.Ready(serverResult.getOrNull()!!)
                return@launch
            }

            try {
                val tracks = saavnProvider.search(id.replace("alb_", "").replace("_", " ").ifBlank { "Top Hits" }, limit = 15).getOrDefault(emptyList())
                val album = AlbumDto(
                    id = id,
                    title = tracks.firstOrNull()?.album?.ifBlank { "Top Hits Album" } ?: "Featured Album",
                    artist = tracks.firstOrNull()?.artist ?: "Various Artists",
                    year = "2026",
                    coverUrl = tracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                    trackCount = tracks.size
                )
                _uiState.value = CatalogUiState.Ready(AlbumDetailResponse(true, album, tracks))
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load album")
            }
        }
    }
}

class ArtistProfileViewModel(
    private val catalogService: com.sonexa.app.data.provider.ArtistCatalogService = com.sonexa.app.data.provider.ArtistCatalogService()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<ArtistDetailResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<ArtistDetailResponse>> = _uiState.asStateFlow()

    private val _catalog = MutableStateFlow<ArtistCatalogResponse?>(null)
    val catalog: StateFlow<ArtistCatalogResponse?> = _catalog.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentArtistQuery: String = ""
    private var currentCursor: Int = 0

    fun load(idOrName: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            currentCursor = 0
            val searchQuery = idOrName.replace("art_", "").replace("_", " ").trim().ifBlank { "Arijit Singh" }
            currentArtistQuery = searchQuery

            try {
                val fullCatalog = catalogService.getFullArtistCatalog(searchQuery, cursor = 0, pageSize = 35)
                _catalog.value = fullCatalog

                val allArtistTracks = if (fullCatalog.allTracks.isNotEmpty()) {
                    fullCatalog.allTracks
                } else {
                    fullCatalog.popularTracks
                }

                _uiState.value = CatalogUiState.Ready(
                    ArtistDetailResponse(
                        success = true,
                        artist = fullCatalog.artist,
                        tracks = allArtistTracks
                    )
                )
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load artist")
            }
        }
    }

    fun loadMore() {
        val current = _catalog.value ?: return
        if (_isLoadingMore.value || !current.hasMore) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                currentCursor += 30
                val nextTracks = catalogService.getMoreTracks(currentArtistQuery, cursor = currentCursor, pageSize = 30)
                if (nextTracks.isNotEmpty()) {
                    val combinedTracks = (_catalog.value?.allTracks.orEmpty() + nextTracks).distinctBy { it.id }
                    val updatedCatalog = _catalog.value!!.copy(
                        allTracks = combinedTracks,
                        hasMore = nextTracks.size >= 15
                    )
                    _catalog.value = updatedCatalog
                    _uiState.value = CatalogUiState.Ready(
                        ArtistDetailResponse(
                            success = true,
                            artist = updatedCatalog.artist,
                            tracks = combinedTracks
                        )
                    )
                } else {
                    _catalog.value = _catalog.value?.copy(hasMore = false)
                }
            } catch (_: Exception) {}
            finally {
                _isLoadingMore.value = false
            }
        }
    }
}

class PlaylistDetailViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val saavnProvider: com.sonexa.app.data.provider.JioSaavnMusicProvider = com.sonexa.app.data.provider.JioSaavnMusicProvider()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<PlaylistDetailResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<PlaylistDetailResponse>> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading

            // Liked Songs playlist
            if (id == "pl_liked" || id.contains("liked", ignoreCase = true)) {
                val likedTracks = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                val effectiveTracks = if (likedTracks.isNotEmpty()) {
                    likedTracks
                } else {
                    saavnProvider.search("Top Romantic Hits", limit = 20).getOrDefault(emptyList()).map { it.copy(isLiked = true) }
                }

                val playlist = PlaylistDto(
                    id = "pl_liked",
                    title = "Liked Songs",
                    subtitle = "📌 Playlist • Liked Songs",
                    artworkType = "gradient",
                    coverUrl = effectiveTracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                    trackCount = effectiveTracks.size,
                    creatorName = "You",
                    isUserCreated = true,
                    isPinned = true
                )
                _uiState.value = CatalogUiState.Ready(PlaylistDetailResponse(true, playlist, effectiveTracks))
                return@launch
            }

            // Check if user-created custom playlist in local store
            val localPl = com.sonexa.app.data.local.UserPlaylistStore.getPlaylist(id)
            if (localPl != null) {
                val localTracks = com.sonexa.app.data.local.UserPlaylistStore.getTracks(id)
                // Also check backend API for synced tracks
                val apiResult = musicRepository.getPlaylist(id)
                val finalTracks = if (apiResult.isSuccess && apiResult.getOrNull()?.tracks?.isNotEmpty() == true) {
                    apiResult.getOrNull()!!.tracks
                } else {
                    localTracks
                }
                _uiState.value = CatalogUiState.Ready(
                    PlaylistDetailResponse(
                        success = true,
                        playlist = localPl.copy(trackCount = finalTracks.size),
                        tracks = finalTracks
                    )
                )
                return@launch
            }

            // Try backend API first
            val serverResult = musicRepository.getPlaylist(id)
            if (serverResult.isSuccess && serverResult.getOrNull()?.tracks?.isNotEmpty() == true) {
                _uiState.value = CatalogUiState.Ready(serverResult.getOrNull()!!)
                return@launch
            }

            // Fallback to Saavn provider
            try {
                val query = when {
                    id.contains("peace", true) -> "Peaceful Acoustic & Lo-Fi"
                    id.contains("bolly", true) -> "Bollywood Spicy Hits"
                    id.contains("workout", true) -> "High Energy Workout Hits"
                    id.contains("holly", true) -> "Hollywood Pop Hits"
                    id.contains("metro", true) -> "Metro In Dino"
                    id.contains("vaapas", true) -> "Main Vaapas Aaunga"
                    id.contains("10s", true) -> "2010s Bollywood Hits"
                    id == "pl_1" -> "Today's Top Hits"
                    id == "pl_2" -> "Viral Hits"
                    id == "pl_3" -> "Chill Lo-Fi"
                    id == "pl_4" -> "Party Hits"
                    else -> id.replace("pl_", "").replace("_", " ").ifBlank { "Trending Music" }
                }
                val tracks = saavnProvider.search(query, limit = 25).getOrDefault(emptyList())
                val playlist = PlaylistDto(
                    id = id,
                    title = query,
                    subtitle = "Curated automatically for your vibe",
                    artworkType = "gradient",
                    coverUrl = tracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                    trackCount = tracks.size,
                    creatorName = "Sonexa",
                    isUserCreated = false
                )
                _uiState.value = CatalogUiState.Ready(PlaylistDetailResponse(true, playlist, tracks))
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }

    fun addTrack(context: android.content.Context, playlistId: String, track: TrackDto) {
        viewModelScope.launch {
            com.sonexa.app.data.local.UserPlaylistStore.addTrack(context, playlistId, track)
            userRepository.addTrackToPlaylist(playlistId, track)
            load(playlistId)
        }
    }

    fun removeTrack(context: android.content.Context, playlistId: String, trackId: String) {
        viewModelScope.launch {
            com.sonexa.app.data.local.UserPlaylistStore.removeTrack(context, playlistId, trackId)
            userRepository.removeTrackFromPlaylist(playlistId, trackId)
            load(playlistId)
        }
    }

    fun updatePlaylist(
        context: android.content.Context,
        id: String,
        title: String,
        description: String = "",
        coverUrl: String = ""
    ) {
        viewModelScope.launch {
            com.sonexa.app.data.local.UserPlaylistStore.updatePlaylist(
                context = context,
                id = id,
                title = title,
                description = description,
                coverUrl = coverUrl
            )
            userRepository.updatePlaylist(
                id,
                UpdatePlaylistRequest(title = title, description = description, coverUrl = coverUrl)
            )
            load(id)
        }
    }

    fun deletePlaylist(
        context: android.content.Context,
        id: String,
        onDeleted: () -> Unit
    ) {
        viewModelScope.launch {
            com.sonexa.app.data.local.UserPlaylistStore.deletePlaylist(context, id)
            userRepository.deletePlaylist(id)
            onDeleted()
        }
    }
}

class PodcastViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val podcastProvider: com.sonexa.app.data.provider.PodcastProvider = com.sonexa.app.data.provider.PodcastProvider()
) : ViewModel() {
    private val _homeState = MutableStateFlow<CatalogUiState<PodcastHomeResponse>>(CatalogUiState.Loading)
    val homeState: StateFlow<CatalogUiState<PodcastHomeResponse>> = _homeState.asStateFlow()

    private val _uiState = MutableStateFlow<CatalogUiState<PodcastListResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<PodcastListResponse>> = _uiState.asStateFlow()

    private val _detail = MutableStateFlow<CatalogUiState<PodcastDetailResponse>>(CatalogUiState.Loading)
    val detail: StateFlow<CatalogUiState<PodcastDetailResponse>> = _detail.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("hindi")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Hindi (हिंदी)")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _followedPodcasts = MutableStateFlow<Set<String>>(setOf("pod_1542452346"))
    val followedPodcasts: StateFlow<Set<String>> = _followedPodcasts.asStateFlow()

    init {
        loadHomeFeed()
        load("Hindi (हिंदी)")
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _homeState.value = CatalogUiState.Loading
            podcastProvider.getPodcastHomeFeed().fold(
                onSuccess = { res -> _homeState.value = CatalogUiState.Ready(res) },
                onFailure = { e -> _homeState.value = CatalogUiState.Error(e.message ?: "Failed to load podcast home feed") }
            )
        }
    }

    fun load(category: String = "Hindi (हिंदी)") {
        _selectedCategory.value = category
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            podcastProvider.getPodcastsByCategory(category).fold(
                onSuccess = { list ->
                    _uiState.value = CatalogUiState.Ready(PodcastListResponse(true, list))
                    if (list.isNotEmpty() && _detail.value !is CatalogUiState.Ready) {
                        loadDetail(list.first().id)
                    }
                },
                onFailure = {
                    musicRepository.getPodcasts().fold(
                        onSuccess = { res -> _uiState.value = CatalogUiState.Ready(res) },
                        onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load podcasts") }
                    )
                }
            )
        }
    }

    fun selectLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        val langName = when (langCode.lowercase()) {
            "hindi" -> "Hindi (हिंदी)"
            "tamil" -> "Tamil (தமிழ்)"
            "telugu" -> "Telugu (తెలుగు)"
            "bengali" -> "Bengali (বাংলা)"
            "marathi" -> "Marathi (मराठी)"
            "punjabi" -> "Punjabi (ਪੰਜਾਬੀ)"
            "spanish" -> "Spanish (Español)"
            "german" -> "German (Deutsch)"
            "japanese" -> "Japanese (日本語)"
            else -> langCode.replaceFirstChar { it.uppercase() }
        }
        load(langName)
    }

    fun toggleFollow(podcastId: String) {
        val current = _followedPodcasts.value.toMutableSet()
        if (current.contains(podcastId)) {
            current.remove(podcastId)
        } else {
            current.add(podcastId)
        }
        _followedPodcasts.value = current
    }

    fun searchPodcasts(query: String) {
        if (query.isBlank()) {
            load(_selectedCategory.value)
            return
        }
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            podcastProvider.getPodcastsByCategory(query, 30).fold(
                onSuccess = { list -> _uiState.value = CatalogUiState.Ready(PodcastListResponse(true, list)) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Search failed") }
            )
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = CatalogUiState.Loading
            podcastProvider.getPodcastEpisodes(id).fold(
                onSuccess = { res -> _detail.value = CatalogUiState.Ready(res) },
                onFailure = {
                    musicRepository.getPodcast(id).fold(
                        onSuccess = { res -> _detail.value = CatalogUiState.Ready(res) },
                        onFailure = { e -> _detail.value = CatalogUiState.Error(e.message ?: "Failed to load episode details") }
                    )
                }
            )
        }
    }
}

class NotificationViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _allNotifications = MutableStateFlow<List<NotificationDto>>(emptyList())

    private val _uiState = MutableStateFlow<CatalogUiState<List<NotificationDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<NotificationDto>>> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredNotifications: StateFlow<List<NotificationDto>> = combine(
        _allNotifications, _selectedCategory
    ) { notifs, cat ->
        if (cat == "all") notifs else notifs.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getNotifications().fold(
                onSuccess = { res ->
                    val list = res.notifications
                    _allNotifications.value = list
                    _unreadCount.value = list.count { !it.read }
                    _uiState.value = CatalogUiState.Ready(list)
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            val updated = _allNotifications.value.map { if (it.id == id) it.copy(read = true) else it }
            _allNotifications.value = updated
            _unreadCount.value = updated.count { !it.read }
            userRepository.markNotificationRead(id)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val updated = _allNotifications.value.map { it.copy(read = true) }
            _allNotifications.value = updated
            _unreadCount.value = 0
            _uiState.value = CatalogUiState.Ready(updated)
            userRepository.markAllNotificationsRead()
        }
    }
}

class PremiumViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<PremiumResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<PremiumResponse>> = _uiState.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getPremium().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
    }

    fun subscribe(planId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            val result = userRepository.subscribe(planId)
            load()
            _busy.value = false
            onDone(result.isSuccess)
        }
    }

    fun redeemCoupon(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            val result = userRepository.redeemCoupon(code)
            if (result.isSuccess) {
                val res = result.getOrNull()
                load()
                _busy.value = false
                onResult(res?.success == true, res?.message ?: "Coupon applied")
            } else {
                _busy.value = false
                onResult(false, result.exceptionOrNull()?.message ?: "Invalid promo code")
            }
        }
    }

    fun cancelPremium(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            userRepository.cancelPremium()
            load()
            _busy.value = false
            onDone()
        }
    }
}

class ProfileHubViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<UserProfileDto>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<UserProfileDto>> = _uiState.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private var cachedBio: String? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getUserProfile().fold(
                onSuccess = {
                    val user = it.user ?: UserProfileDto(name = "Sonexa Listener")
                    val merged = if (user.bio.isBlank() && !cachedBio.isNullOrBlank()) {
                        user.copy(bio = cachedBio.orEmpty())
                    } else {
                        user
                    }
                    if (merged.bio.isNotBlank()) cachedBio = merged.bio
                    _uiState.value = CatalogUiState.Ready(merged)
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
    }

    fun updateProfile(name: String, bio: String? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            if (!bio.isNullOrBlank()) cachedBio = bio
            userRepository.updateProfile(name = name, bio = bio).fold(
                onSuccess = {
                    load()
                    onDone()
                },
                onFailure = { /* keep current; caller can toast */ }
            )
            _busy.value = false
        }
    }
}

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val appConfigRepository = AppConfigRepository()

    data class UiModel(
        val settings: Map<String, Any?> = emptyMap(),
        val profile: UserProfileDto? = null,
        val cacheBytes: Long = 0L,
        val availableLanguages: List<String> = emptyList(),
        val appVersion: String = "",
        val latestVersion: String = "",
        val saving: Boolean = false,
        val message: String? = null
    )

    private val _uiState = MutableStateFlow<CatalogUiState<UiModel>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<UiModel>> = _uiState.asStateFlow()

    private val sessionManager = SessionManager.getInstance(application)

    private val prefs = application.getSharedPreferences("sonexa_app_settings", Context.MODE_PRIVATE)

    init {
        load()
    }

    private fun getLocalSettings(): MutableMap<String, Any?> {
        val defaultMap = mutableMapOf<String, Any?>(
            "aiSensitivity" to "High",
            "aiVoiceModel" to "Sonexa Voice v2.4",
            "smartLyrics" to true,
            "audioQuality" to "High",
            "crossfade" to true,
            "normalizeVolume" to true,
            "gaplessPlayback" to true,
            "explicitContent" to true,
            "downloadQuality" to "High",
            "downloadOverWifiOnly" to true,
            "pushNotifications" to true,
            "friendActivity" to true,
            "newReleaseAlerts" to true,
            "theme" to "Dark",
            "accentStyle" to "Glassmorphism",
            "dataSharing" to true,
            "showActiveSessions" to true,
            "twoFactorEnabled" to false,
            "connectedDevices" to listOf("This Android phone", "Bluetooth earbuds")
        )
        prefs.all.forEach { (k, v) ->
            if (v != null) defaultMap[k] = v
        }
        return defaultMap
    }

    private fun saveLocalSettings(settings: Map<String, Any?>) {
        val editor = prefs.edit()
        settings.forEach { (k, v) ->
            when (v) {
                is Boolean -> editor.putBoolean(k, v)
                is String -> editor.putString(k, v)
                is Int -> editor.putInt(k, v)
                is Long -> editor.putLong(k, v)
                is Float -> editor.putFloat(k, v)
                is List<*> -> editor.putString(k, v.joinToString(","))
            }
        }
        editor.apply()
    }

    fun load() {
        val appCtx = getApplication<Application>()
        val localSettings = getLocalSettings()
        val versionName = try {
            val info = appCtx.packageManager.getPackageInfo(appCtx.packageName, 0)
            info.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
        val defaultLangs = listOf("English (India)", "Hindi", "Punjabi", "Tamil", "Telugu", "Spanish")

        // 1. Immediately emit Ready state from local storage for instant responsiveness
        _uiState.value = CatalogUiState.Ready(
            UiModel(
                settings = localSettings,
                profile = UserProfileDto(name = sessionManager.userName ?: "Music Lover", email = sessionManager.userEmail.orEmpty()),
                cacheBytes = AudioCacheManager.cacheSizeBytes(appCtx),
                availableLanguages = defaultLangs,
                appVersion = versionName,
                latestVersion = "1.0"
            )
        )

        // 2. Asynchronously sync with backend
        viewModelScope.launch {
            val cacheBytes = withContext(Dispatchers.IO) { AudioCacheManager.cacheSizeBytes(appCtx) }
            val settingsResult = userRepository.getSettings()
            val profileResult = userRepository.getUserProfile()
            val languagesResult = appConfigRepository.getLanguages()
            val updateResult = appConfigRepository.getAppUpdate()

            val remoteSettings = settingsResult.getOrNull()?.settings.orEmpty()
            val mergedSettings = localSettings.apply { putAll(remoteSettings) }
            saveLocalSettings(mergedSettings)

            val profile = profileResult.getOrNull()?.user
                ?: UserProfileDto(name = sessionManager.userName ?: "Music Lover", email = sessionManager.userEmail.orEmpty())
            val langs = languagesResult.getOrNull()?.languages?.map { it.name }?.ifEmpty { null }
                ?: defaultLangs
            val latest = updateResult.getOrNull()?.latestVersion.orEmpty().ifBlank { versionName }

            _uiState.value = CatalogUiState.Ready(
                UiModel(
                    settings = mergedSettings,
                    profile = profile,
                    cacheBytes = cacheBytes,
                    availableLanguages = langs,
                    appVersion = versionName,
                    latestVersion = latest
                )
            )
        }
    }

    fun clearMessage() {
        val ready = (_uiState.value as? CatalogUiState.Ready)?.data ?: return
        _uiState.value = CatalogUiState.Ready(ready.copy(message = null))
    }

    fun updateSettings(patch: Map<String, Any?>, successMessage: String = "Settings saved") {
        val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return
        // 1. Instant optimistic state update
        val merged = current.settings.toMutableMap().apply { putAll(patch) }
        if (patch.containsKey("languages")) {
            val list = (patch["languages"] as? List<*>)?.map { it.toString() }.orEmpty()
            sessionManager.preferredLanguages = list
            merged["language"] = list.joinToString(" • ")
        }
        _uiState.value = CatalogUiState.Ready(
            current.copy(settings = merged, saving = false, message = null)
        )
        // 2. Persist locally to SharedPreferences
        saveLocalSettings(merged)

        // 3. Asynchronously push to backend
        viewModelScope.launch {
            userRepository.updateSettings(patch).fold(
                onSuccess = {
                    // Confirmed on server
                },
                onFailure = {
                    // Keep optimistic local state
                }
            )
        }
    }

    fun updateToggle(key: String, value: Boolean) = updateSettings(mapOf(key to value))

    fun updateString(key: String, value: String) = updateSettings(mapOf(key to value))

    fun updateProfile(name: String, onDone: () -> Unit = {}) {
        val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return
        sessionManager.userName = name
        val updatedProfile = current.profile?.copy(name = name)
            ?: UserProfileDto(name = name, email = sessionManager.userEmail.orEmpty())
        _uiState.value = CatalogUiState.Ready(
            current.copy(
                profile = updatedProfile,
                saving = false,
                message = "Account updated"
            )
        )
        onDone()

        viewModelScope.launch {
            userRepository.updateProfile(name = name)
        }
    }

    fun saveLanguages(selected: List<String>) {
        val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return
        sessionManager.preferredLanguages = selected
        val patch = mapOf(
            "languages" to selected,
            "language" to selected.joinToString(" • ")
        )
        val merged = current.settings.toMutableMap().apply { putAll(patch) }
        _uiState.value = CatalogUiState.Ready(
            current.copy(
                settings = merged,
                saving = false,
                message = "Languages updated"
            )
        )
        saveLocalSettings(merged)

        viewModelScope.launch {
            appConfigRepository.saveLanguages(selected)
            userRepository.updateSettings(patch)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return@launch
            val cleared = withContext(Dispatchers.IO) {
                AudioCacheManager.clearCache(getApplication())
            }
            val remaining = withContext(Dispatchers.IO) {
                AudioCacheManager.cacheSizeBytes(getApplication())
            }
            _uiState.value = CatalogUiState.Ready(
                current.copy(
                    cacheBytes = remaining,
                    message = "Cleared ${AudioCacheManager.formatMb(cleared)} of cache"
                )
            )
        }
    }

    fun settingString(key: String, default: String = ""): String {
        val map = (_uiState.value as? CatalogUiState.Ready)?.data?.settings ?: return default
        return map[key]?.toString()?.takeIf { it.isNotBlank() } ?: default
    }

    fun settingBool(key: String, default: Boolean = false): Boolean {
        val map = (_uiState.value as? CatalogUiState.Ready)?.data?.settings ?: return default
        return when (val v = map[key]) {
            is Boolean -> v
            is Number -> v.toInt() != 0
            is String -> v.equals("true", ignoreCase = true)
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun settingStringList(key: String): List<String> {
        val map = (_uiState.value as? CatalogUiState.Ready)?.data?.settings ?: return emptyList()
        return when (val v = map[key]) {
            is List<*> -> v.mapNotNull { it?.toString() }
            is String -> v.split("•", ",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }
}

class PlayerViewModel(
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<QueueResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<QueueResponse>> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            musicRepository.getQueue().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
    }
}

class AiSignatureHubViewModel(
    private val aiRepository: AiRepository = AiRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<AiSignatureResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<AiSignatureResponse>> = _uiState.asStateFlow()
    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    fun generate(mood: String) {
        viewModelScope.launch {
            _generating.value = true
            _uiState.value = CatalogUiState.Loading
            aiRepository.generateAiSignature(mood).fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
            _generating.value = false
        }
    }
}

class LiveEventsViewModel(
    private val liveEventsProvider: com.sonexa.app.data.provider.LiveEventsProvider = com.sonexa.app.data.provider.LiveEventsProvider()
) : ViewModel() {
    private val _feedState = MutableStateFlow<CatalogUiState<LiveEventsFeedResponse>>(CatalogUiState.Loading)
    val feedState: StateFlow<CatalogUiState<LiveEventsFeedResponse>> = _feedState.asStateFlow()

    private val _detailState = MutableStateFlow<CatalogUiState<LiveEventDetailResponse>>(CatalogUiState.Loading)
    val detailState: StateFlow<CatalogUiState<LiveEventDetailResponse>> = _detailState.asStateFlow()

    private val _selectedCity = MutableStateFlow("All")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _remindedEvents = MutableStateFlow<Set<String>>(emptySet())
    val remindedEvents: StateFlow<Set<String>> = _remindedEvents.asStateFlow()

    init {
        loadFeed("All", "All")
    }

    fun loadFeed(city: String = _selectedCity.value, category: String = _selectedCategory.value) {
        _selectedCity.value = city
        _selectedCategory.value = category
        viewModelScope.launch {
            _feedState.value = CatalogUiState.Loading
            try {
                val feed = liveEventsProvider.getLiveEventsFeed(city, category)
                _feedState.value = CatalogUiState.Ready(feed)
            } catch (e: Exception) {
                _feedState.value = CatalogUiState.Error(e.message ?: "Failed to load live events")
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = CatalogUiState.Loading
            try {
                val detail = liveEventsProvider.getLiveEventDetail(id)
                _detailState.value = CatalogUiState.Ready(detail)
            } catch (e: Exception) {
                _detailState.value = CatalogUiState.Error(e.message ?: "Failed to load event details")
            }
        }
    }

    fun toggleReminder(id: String) {
        viewModelScope.launch {
            val isNowReminded = liveEventsProvider.toggleReminder(id)
            val current = _remindedEvents.value.toMutableSet()
            if (isNowReminded) current.add(id) else current.remove(id)
            _remindedEvents.value = current
        }
    }
}

class IPopViewModel(
    private val iPopProvider: com.sonexa.app.data.provider.IPopProvider = com.sonexa.app.data.provider.IPopProvider()
) : ViewModel() {
    private val _homeState = MutableStateFlow<CatalogUiState<IPopHomeResponse>>(CatalogUiState.Loading)
    val homeState: StateFlow<CatalogUiState<IPopHomeResponse>> = _homeState.asStateFlow()

    private val _playlistState = MutableStateFlow<CatalogUiState<IPopPlaylistDto>>(CatalogUiState.Loading)
    val playlistState: StateFlow<CatalogUiState<IPopPlaylistDto>> = _playlistState.asStateFlow()

    private val _selectedSubgenre = MutableStateFlow("All")
    val selectedSubgenre: StateFlow<String> = _selectedSubgenre.asStateFlow()

    init {
        loadHome("All")
    }

    fun loadHome(subgenre: String = _selectedSubgenre.value) {
        _selectedSubgenre.value = subgenre
        viewModelScope.launch {
            _homeState.value = CatalogUiState.Loading
            try {
                val feed = iPopProvider.getHomeFeed(subgenre)
                _homeState.value = CatalogUiState.Ready(feed)
            } catch (e: Exception) {
                _homeState.value = CatalogUiState.Error(e.message ?: "Failed to load I-Pop")
            }
        }
    }

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            _playlistState.value = CatalogUiState.Loading
            try {
                val pl = iPopProvider.getPlaylist(id)
                _playlistState.value = CatalogUiState.Ready(pl)
            } catch (e: Exception) {
                _playlistState.value = CatalogUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }
}
