package com.sonexa.app.ui.viewmodel

import android.app.Application
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getUserLibrary().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
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
    private val saavnProvider: com.sonexa.app.data.provider.JioSaavnMusicProvider = com.sonexa.app.data.provider.JioSaavnMusicProvider()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<ArtistDetailResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<ArtistDetailResponse>> = _uiState.asStateFlow()

    fun load(idOrName: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            val searchQuery = idOrName.replace("art_", "").replace("_", " ").trim().ifBlank { "Arijit Singh" }

            try {
                val tracks = saavnProvider.search(searchQuery, limit = 35).getOrDefault(emptyList())
                val cleanArtistName = if (searchQuery.contains("Top", ignoreCase = true) || searchQuery.contains("Trending", ignoreCase = true)) {
                    tracks.firstOrNull()?.artist?.split(",", "&")?.first()?.trim() ?: searchQuery
                } else {
                    searchQuery
                }
                val artist = ArtistDto(
                    id = "art_${cleanArtistName.lowercase().replace(" ", "_")}",
                    name = cleanArtistName,
                    genre = "Top Global Artist",
                    bio = "Discover the top hits, trending tracks, and latest releases from $cleanArtistName on Sonexa.",
                    imageUrl = tracks.firstOrNull()?.effectiveCoverUrl.orEmpty(),
                    followersCount = 4850000,
                    verified = true
                )
                _uiState.value = CatalogUiState.Ready(ArtistDetailResponse(true, artist, tracks))
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load artist")
            }
        }
    }
}

class PlaylistDetailViewModel(
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
                    coverUrl = effectiveTracks.firstOrNull()?.effectiveCoverUrl.orEmpty()
                )
                _uiState.value = CatalogUiState.Ready(PlaylistDetailResponse(true, playlist, effectiveTracks))
                return@launch
            }

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
                    coverUrl = tracks.firstOrNull()?.effectiveCoverUrl.orEmpty()
                )
                _uiState.value = CatalogUiState.Ready(PlaylistDetailResponse(true, playlist, tracks))
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }
}

class PodcastViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val podcastProvider: com.sonexa.app.data.provider.PodcastProvider = com.sonexa.app.data.provider.PodcastProvider()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<PodcastListResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<PodcastListResponse>> = _uiState.asStateFlow()
    private val _detail = MutableStateFlow<CatalogUiState<PodcastDetailResponse>>(CatalogUiState.Loading)
    val detail: StateFlow<CatalogUiState<PodcastDetailResponse>> = _detail.asStateFlow()
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init { load("All") }

    fun load(category: String = "All") {
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
    private val _uiState = MutableStateFlow<CatalogUiState<List<NotificationDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<NotificationDto>>> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getNotifications().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it.notifications) },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
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

    fun subscribe(planId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            userRepository.subscribe(planId)
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

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            userRepository.getUserProfile().fold(
                onSuccess = {
                    _uiState.value = CatalogUiState.Ready(it.user ?: UserProfileDto(name = "Sonexa Listener"))
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed") }
            )
        }
    }

    fun updateProfile(name: String, bio: String? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
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

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            val appCtx = getApplication<Application>()
            val cacheBytes = withContext(Dispatchers.IO) { AudioCacheManager.cacheSizeBytes(appCtx) }
            val settingsResult = userRepository.getSettings()
            val profileResult = userRepository.getUserProfile()
            val languagesResult = appConfigRepository.getLanguages()
            val updateResult = appConfigRepository.getAppUpdate()

            settingsResult.fold(
                onSuccess = { settingsResp ->
                    val settings = settingsResp.settings.toMutableMap()
                    val profile = profileResult.getOrNull()?.user
                    val langs = languagesResult.getOrNull()?.languages?.map { it.name }?.ifEmpty { null }
                        ?: listOf("English (India)", "Hindi", "Punjabi", "Tamil", "Telugu", "Spanish")
                    val versionName = try {
                        val info = appCtx.packageManager.getPackageInfo(appCtx.packageName, 0)
                        info.versionName ?: "1.0"
                    } catch (_: Exception) {
                        "1.0"
                    }
                    val latest = updateResult.getOrNull()?.latestVersion.orEmpty()
                    _uiState.value = CatalogUiState.Ready(
                        UiModel(
                            settings = settings,
                            profile = profile,
                            cacheBytes = cacheBytes,
                            availableLanguages = langs,
                            appVersion = versionName,
                            latestVersion = latest
                        )
                    )
                },
                onFailure = { e ->
                    _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load settings")
                }
            )
        }
    }

    fun clearMessage() {
        val ready = (_uiState.value as? CatalogUiState.Ready)?.data ?: return
        _uiState.value = CatalogUiState.Ready(ready.copy(message = null))
    }

    fun updateSettings(patch: Map<String, Any?>, successMessage: String = "Settings saved") {
        viewModelScope.launch {
            val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return@launch
            _uiState.value = CatalogUiState.Ready(current.copy(saving = true, message = null))
            userRepository.updateSettings(patch).fold(
                onSuccess = {
                    val merged = current.settings.toMutableMap().apply { putAll(patch) }
                    if (patch.containsKey("languages")) {
                        val list = (patch["languages"] as? List<*>)?.map { it.toString() }.orEmpty()
                        sessionManager.preferredLanguages = list
                        merged["language"] = list.joinToString(" • ")
                    }
                    _uiState.value = CatalogUiState.Ready(
                        current.copy(settings = merged, saving = false, message = successMessage)
                    )
                },
                onFailure = { e ->
                    _uiState.value = CatalogUiState.Ready(
                        current.copy(saving = false, message = e.message ?: "Failed to save settings")
                    )
                }
            )
        }
    }

    fun updateToggle(key: String, value: Boolean) = updateSettings(mapOf(key to value))

    fun updateString(key: String, value: String) = updateSettings(mapOf(key to value))

    fun updateProfile(name: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return@launch
            _uiState.value = CatalogUiState.Ready(current.copy(saving = true))
            userRepository.updateProfile(name = name).fold(
                onSuccess = {
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
                },
                onFailure = { e ->
                    _uiState.value = CatalogUiState.Ready(
                        current.copy(saving = false, message = e.message ?: "Profile update failed")
                    )
                }
            )
        }
    }

    fun saveLanguages(selected: List<String>) {
        viewModelScope.launch {
            val current = (_uiState.value as? CatalogUiState.Ready)?.data ?: return@launch
            _uiState.value = CatalogUiState.Ready(current.copy(saving = true, message = null))
            appConfigRepository.saveLanguages(selected)
            val patch = mapOf(
                "languages" to selected,
                "language" to selected.joinToString(" • ")
            )
            userRepository.updateSettings(patch).fold(
                onSuccess = {
                    sessionManager.preferredLanguages = selected
                    val merged = current.settings.toMutableMap().apply { putAll(patch) }
                    _uiState.value = CatalogUiState.Ready(
                        current.copy(
                            settings = merged,
                            saving = false,
                            message = "Languages updated"
                        )
                    )
                },
                onFailure = { e ->
                    _uiState.value = CatalogUiState.Ready(
                        current.copy(saving = false, message = e.message ?: "Failed to save languages")
                    )
                }
            )
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
