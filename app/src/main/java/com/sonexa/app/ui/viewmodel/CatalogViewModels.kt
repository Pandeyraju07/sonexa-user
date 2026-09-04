package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.api.OnboardingSlideDto
import com.sonexa.app.data.model.*
import com.sonexa.app.data.repository.AppConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CatalogUiState<out T> {
    data object Loading : CatalogUiState<Nothing>
    data class Ready<T>(val data: T) : CatalogUiState<T>
    data class Error(val message: String) : CatalogUiState<Nothing>
}

class OnboardingViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<List<OnboardingSlideDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<OnboardingSlideDto>>> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repo.getOnboardingSlides().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it.slides) },
                onFailure = {
                    _uiState.value = CatalogUiState.Ready(
                        listOf(
                            OnboardingSlideDto("AI Personal DJ", "Music adapted to your mood in real-time"),
                            OnboardingSlideDto("Lossless Audio", "Studio-quality sound with spatial audio"),
                            OnboardingSlideDto("Smart Discovery", "Discover emerging tracks with Zynera AI")
                        )
                    )
                }
            )
        }
    }
}

class GenreSelectionViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<List<GenreDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<GenreDto>>> = _uiState.asStateFlow()
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repo.getGenres().fold(
                onSuccess = {
                    _uiState.value = CatalogUiState.Ready(it.genres)
                    if (_selected.value.isEmpty() && it.genres.isNotEmpty()) {
                        _selected.value = it.genres.take(3).map { g -> g.name }.toSet()
                    }
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load genres") }
            )
        }
    }

    fun toggle(name: String) {
        val cur = _selected.value.toMutableSet()
        if (!cur.add(name)) {
            if (cur.size > 1) cur.remove(name)
        }
        _selected.value = cur
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            repo.saveGenres(_selected.value.toList())
            _saving.value = false
            onDone()
        }
    }
}

class ArtistSelectionViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<List<ArtistDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<ArtistDto>>> = _uiState.asStateFlow()
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repo.getArtists().fold(
                onSuccess = {
                    _uiState.value = CatalogUiState.Ready(it.artists)
                    if (_selected.value.isEmpty() && it.artists.isNotEmpty()) {
                        _selected.value = it.artists.take(3).map { a -> a.name }.toSet()
                    }
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load artists") }
            )
        }
    }

    fun toggle(name: String) {
        val cur = _selected.value.toMutableSet()
        if (!cur.add(name)) {
            if (cur.size > 1) cur.remove(name)
        }
        _selected.value = cur
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            repo.saveArtists(_selected.value.toList())
            _saving.value = false
            onDone()
        }
    }
}

class MoodSelectionViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<List<MoodDto>>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<List<MoodDto>>> = _uiState.asStateFlow()
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repo.getMoods().fold(
                onSuccess = {
                    _uiState.value = CatalogUiState.Ready(it.moods)
                    if (_selected.value.isEmpty() && it.moods.isNotEmpty()) {
                        _selected.value = it.moods.take(2).map { m -> m.name }.toSet()
                    }
                },
                onFailure = { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load moods") }
            )
        }
    }

    fun toggle(name: String) {
        val cur = _selected.value.toMutableSet()
        if (!cur.add(name)) {
            if (cur.size > 1) cur.remove(name)
        }
        _selected.value = cur
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            repo.saveMoods(_selected.value.toList())
            _saving.value = false
            onDone()
        }
    }
}

class AppUpdateViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState<AppUpdateResponse>>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState<AppUpdateResponse>> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repo.getAppUpdate().fold(
                onSuccess = { _uiState.value = CatalogUiState.Ready(it) },
                onFailure = {
                    _uiState.value = CatalogUiState.Ready(
                        AppUpdateResponse(true, false, false, "1.0.0", "You're on the latest version")
                    )
                }
            )
        }
    }
}

class PermissionsOnboardingViewModel(
    private val repo: AppConfigRepository = AppConfigRepository()
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun saveNotifications(enabled: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            val current = repo.getPermissionPrefs().getOrNull()
            repo.savePermissionPrefs(
                notificationsEnabled = enabled,
                downloadsEnabled = current?.downloadsEnabled == true
            )
            _saving.value = false
            onDone()
        }
    }

    fun saveDownloads(enabled: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            val current = repo.getPermissionPrefs().getOrNull()
            repo.savePermissionPrefs(
                notificationsEnabled = current?.notificationsEnabled == true,
                downloadsEnabled = enabled
            )
            _saving.value = false
            onDone()
        }
    }
}