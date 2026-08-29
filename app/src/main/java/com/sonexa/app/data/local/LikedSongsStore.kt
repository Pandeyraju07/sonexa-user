package com.sonexa.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LikedSongsStore {
    private val gson = Gson()
    private val _likedSongs = MutableStateFlow<List<TrackDto>>(emptyList())
    val likedSongs: StateFlow<List<TrackDto>> = _likedSongs.asStateFlow()

    fun init(context: android.content.Context) {
        val prefs = context.getSharedPreferences("sonexa_liked_songs", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString("liked_tracks", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<TrackDto>>() {}.type
                val list: List<TrackDto> = gson.fromJson(json, type)
                _likedSongs.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isLiked(trackId: String): Boolean {
        return _likedSongs.value.any { it.id == trackId }
    }

    fun toggleLike(context: android.content.Context, track: TrackDto): Boolean {
        val current = _likedSongs.value.toMutableList()
        val exists = current.any { it.id == track.id }
        val nowLiked = if (exists) {
            current.removeAll { it.id == track.id }
            false
        } else {
            current.add(0, track.copy(isLiked = true))
            true
        }
        _likedSongs.value = current

        // Persist to SharedPreferences
        val prefs = context.getSharedPreferences("sonexa_liked_songs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("liked_tracks", gson.toJson(current)).apply()

        return nowLiked
    }

    fun getLikedTracks(): List<TrackDto> = _likedSongs.value
}
