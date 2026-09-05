package com.sonexa.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LikedSongsStore {
    private val gson = com.sonexa.app.data.api.RetrofitClient.gson
    private val _likedSongs = MutableStateFlow<List<TrackDto>>(emptyList())
    val likedSongs: StateFlow<List<TrackDto>> = _likedSongs.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("sonexa_liked_songs", Context.MODE_PRIVATE)
        val json = prefs.getString("liked_tracks", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<TrackDto>>() {}.type
                val list: List<TrackDto> = gson.fromJson(json, type)
                _likedSongs.value = list.map { it.sanitized() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isLiked(trackId: String?): Boolean {
        if (trackId.isNullOrBlank()) return false
        return _likedSongs.value.any { it.id == trackId }
    }

    fun withLikedStatus(track: TrackDto?): TrackDto? {
        if (track == null) return null
        val safeTrack = track.sanitized()
        return safeTrack.copySafe(isLiked = isLiked(safeTrack.id))
    }

    fun withLikedStatus(tracks: List<TrackDto>): List<TrackDto> {
        return tracks.mapNotNull { track ->
            val safe = track.sanitized()
            safe.copySafe(isLiked = isLiked(safe.id)) 
        }
    }

    fun toggleLike(context: Context, track: TrackDto): Boolean {
        val safe = track.sanitized()
        val current = _likedSongs.value.toMutableList()
        val exists = current.any { it.id == safe.id }
        val nowLiked = if (exists) {
            current.removeAll { it.id == safe.id }
            false
        } else {
            current.add(0, safe.copySafe(isLiked = true))
            true
        }
        _likedSongs.value = current

        // Persist to SharedPreferences
        val prefs = context.getSharedPreferences("sonexa_liked_songs", Context.MODE_PRIVATE)
        prefs.edit().putString("liked_tracks", gson.toJson(current)).apply()

        return nowLiked
    }

    fun getLikedTracks(): List<TrackDto> = _likedSongs.value
}