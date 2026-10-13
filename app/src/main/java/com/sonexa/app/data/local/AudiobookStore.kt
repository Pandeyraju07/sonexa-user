package com.sonexa.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.AudiobookBookmarkDto
import com.sonexa.app.data.model.AudiobookDto
import com.sonexa.app.data.model.AudiobookProgressDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AudiobookStore {

    private const val PREFS_NAME = "zynera_audiobook_prefs"
    private const val KEY_PROGRESS_MAP = "audiobook_progress_map"
    private const val KEY_BOOKMARKS_MAP = "audiobook_bookmarks_map"
    private const val KEY_SAVED_IDS = "saved_audiobook_ids"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val _progressMap = MutableStateFlow<Map<String, AudiobookProgressDto>>(emptyMap())
    val progressMap: StateFlow<Map<String, AudiobookProgressDto>> = _progressMap.asStateFlow()

    private val _bookmarksMap = MutableStateFlow<Map<String, List<AudiobookBookmarkDto>>>(emptyMap())
    val bookmarksMap: StateFlow<Map<String, List<AudiobookBookmarkDto>>> = _bookmarksMap.asStateFlow()

    private val _savedAudiobookIds = MutableStateFlow<Set<String>>(emptySet())
    val savedAudiobookIds: StateFlow<Set<String>> = _savedAudiobookIds.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        // Load cached progress
        val progressJson = sp.getString(KEY_PROGRESS_MAP, null)
        if (!progressJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<Map<String, AudiobookProgressDto>>() {}.type
                val map: Map<String, AudiobookProgressDto> = gson.fromJson(progressJson, type) ?: emptyMap()
                _progressMap.value = map
            } catch (_: Exception) {}
        }

        // Load cached bookmarks
        val bookmarksJson = sp.getString(KEY_BOOKMARKS_MAP, null)
        if (!bookmarksJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<Map<String, List<AudiobookBookmarkDto>>>() {}.type
                val map: Map<String, List<AudiobookBookmarkDto>> = gson.fromJson(bookmarksJson, type) ?: emptyMap()
                _bookmarksMap.value = map
            } catch (_: Exception) {}
        }

        // Load saved IDs
        val savedSet = sp.getStringSet(KEY_SAVED_IDS, emptySet()) ?: emptySet()
        _savedAudiobookIds.value = savedSet.toSet()
    }

    fun getProgress(audiobookId: String): AudiobookProgressDto? {
        return _progressMap.value[audiobookId]
    }

    fun saveProgress(
        audiobookId: String,
        chapterId: String,
        chapterNumber: Int,
        chapterTitle: String,
        positionSeconds: Long,
        totalDurationSeconds: Long
    ) {
        val totalListened = ((chapterNumber - 1).coerceAtLeast(0) * (totalDurationSeconds / 4.coerceAtLeast(1))) + positionSeconds
        val percent = if (totalDurationSeconds > 0) {
            ((totalListened.toFloat() / totalDurationSeconds.toFloat()) * 100f).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val updated = AudiobookProgressDto(
            audiobookId = audiobookId,
            currentChapterId = chapterId,
            currentChapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            positionSeconds = positionSeconds,
            totalListenedSeconds = totalListened,
            completionPercentage = percent,
            lastListenedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )

        _progressMap.update { current ->
            current + (audiobookId to updated)
        }
        persistProgress()
    }

    fun getBookmarks(audiobookId: String): List<AudiobookBookmarkDto> {
        return _bookmarksMap.value[audiobookId] ?: emptyList()
    }

    fun addBookmark(
        audiobookId: String,
        chapterId: String,
        chapterNumber: Int,
        positionSeconds: Long,
        note: String
    ): AudiobookBookmarkDto {
        val newBookmark = AudiobookBookmarkDto(
            id = "bm_${System.currentTimeMillis()}",
            audiobookId = audiobookId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            positionSeconds = positionSeconds,
            note = note.trim(),
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )

        _bookmarksMap.update { current ->
            val list = current[audiobookId].orEmpty().toMutableList()
            list.add(0, newBookmark)
            current + (audiobookId to list)
        }
        persistBookmarks()
        return newBookmark
    }

    fun removeBookmark(audiobookId: String, bookmarkId: String) {
        _bookmarksMap.update { current ->
            val list = current[audiobookId].orEmpty().filter { it.id != bookmarkId }
            current + (audiobookId to list)
        }
        persistBookmarks()
    }

    fun toggleSaveAudiobook(audiobookId: String): Boolean {
        val currentSet = _savedAudiobookIds.value
        val isSaved = currentSet.contains(audiobookId)
        val newSet = if (isSaved) currentSet - audiobookId else currentSet + audiobookId
        _savedAudiobookIds.value = newSet
        prefs?.edit()?.putStringSet(KEY_SAVED_IDS, newSet)?.apply()
        return !isSaved
    }

    fun isSaved(audiobookId: String): Boolean {
        return _savedAudiobookIds.value.contains(audiobookId)
    }

    private fun persistProgress() {
        val json = gson.toJson(_progressMap.value)
        prefs?.edit()?.putString(KEY_PROGRESS_MAP, json)?.apply()
    }

    private fun persistBookmarks() {
        val json = gson.toJson(_bookmarksMap.value)
        prefs?.edit()?.putString(KEY_BOOKMARKS_MAP, json)?.apply()
    }
}
