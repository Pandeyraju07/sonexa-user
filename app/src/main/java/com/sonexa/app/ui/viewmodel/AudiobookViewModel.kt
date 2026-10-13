package com.sonexa.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.*
import com.sonexa.app.data.repository.AudiobookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudiobookUiState(
    val audiobooks: List<AudiobookDto> = emptyList(),
    val continueListening: List<AudiobookDto> = emptyList(),
    val categories: List<AudiobookCategoryDto> = emptyList(),
    val languages: List<AudiobookLanguageDto> = emptyList(),
    val selectedCategory: String = "All",
    val selectedLanguage: String = "All",
    val selectedAudiobook: AudiobookDto? = null,
    val currentProgress: AudiobookProgressDto? = null,
    val bookmarks: List<AudiobookBookmarkDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AudiobookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudiobookRepository(application)

    private val _uiState = MutableStateFlow(AudiobookUiState())
    val uiState: StateFlow<AudiobookUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val catsResult = repository.getCategories()
            val langsResult = repository.getLanguages()
            val contResult = repository.getContinueListening()
            val booksResult = repository.getAudiobooks(
                category = _uiState.value.selectedCategory,
                language = _uiState.value.selectedLanguage
            )

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    categories = catsResult.getOrDefault(AudiobookRepository.bundledCategories()),
                    languages = langsResult.getOrDefault(AudiobookRepository.bundledLanguages()),
                    continueListening = contResult.getOrDefault(emptyList()),
                    audiobooks = booksResult.getOrDefault(AudiobookRepository.bundledAudiobooks())
                )
            }
        }
    }

    fun selectCategory(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
        viewModelScope.launch {
            val result = repository.getAudiobooks(
                category = category,
                language = _uiState.value.selectedLanguage
            )
            _uiState.update {
                it.copy(
                    audiobooks = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        if (_uiState.value.selectedLanguage == languageCode) return
        _uiState.update { it.copy(selectedLanguage = languageCode, isLoading = true) }
        viewModelScope.launch {
            val result = repository.getAudiobooks(
                category = _uiState.value.selectedCategory,
                language = languageCode
            )
            _uiState.update {
                it.copy(
                    audiobooks = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            }
        }
    }

    fun loadAudiobookDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val detailResult = repository.getAudiobookDetail(id)
            val book = detailResult.getOrNull()
            val prog = AudiobookStore.getProgress(id)
            val bms = AudiobookStore.getBookmarks(id)

            _uiState.update {
                it.copy(
                    selectedAudiobook = book,
                    currentProgress = prog,
                    bookmarks = bms,
                    isLoading = false
                )
            }
        }
    }

    fun toggleSave(audiobookId: String) {
        val nowSaved = repository.toggleSave(audiobookId)
        _uiState.update { state ->
            val updatedList = state.audiobooks.map {
                if (it.id == audiobookId) it.copy(isSaved = nowSaved) else it
            }
            val updatedSelected = if (state.selectedAudiobook?.id == audiobookId) {
                state.selectedAudiobook.copy(isSaved = nowSaved)
            } else state.selectedAudiobook

            state.copy(
                audiobooks = updatedList,
                selectedAudiobook = updatedSelected
            )
        }
    }

    fun addBookmark(
        audiobookId: String,
        chapterId: String,
        chapterNumber: Int,
        positionSeconds: Long,
        note: String
    ) {
        viewModelScope.launch {
            val newBm = repository.addBookmark(audiobookId, chapterId, chapterNumber, positionSeconds, note)
            _uiState.update { state ->
                val list = listOf(newBm) + state.bookmarks
                state.copy(bookmarks = list)
            }
        }
    }

    fun removeBookmark(audiobookId: String, bookmarkId: String) {
        viewModelScope.launch {
            repository.removeBookmark(audiobookId, bookmarkId)
            _uiState.update { state ->
                state.copy(bookmarks = state.bookmarks.filter { it.id != bookmarkId })
            }
        }
    }
}
