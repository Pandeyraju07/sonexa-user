package com.sonexa.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserPlaylistStore {
    private val gson = com.sonexa.app.data.api.RetrofitClient.gson
    private val _playlists = MutableStateFlow<List<PlaylistDto>>(emptyList())
    val playlists: StateFlow<List<PlaylistDto>> = _playlists.asStateFlow()

    private val _playlistTracks = MutableStateFlow<Map<String, List<TrackDto>>>(emptyMap())
    val playlistTracks: StateFlow<Map<String, List<TrackDto>>> = _playlistTracks.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("sonexa_user_playlists", Context.MODE_PRIVATE)
        val plJson = prefs.getString("custom_playlists", null)
        val trJson = prefs.getString("playlist_tracks_map", null)

        if (!plJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<PlaylistDto>>() {}.type
                val list: List<PlaylistDto> = gson.fromJson(plJson, type)
                _playlists.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Seed default curated user starters
            val initial = listOf(
                PlaylistDto(
                    id = "pl_bolly",
                    title = "Bollywood spicy 🔥",
                    subtitle = "Playlist • Top Hits",
                    coverUrl = "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg",
                    trackCount = 12,
                    creatorName = "You",
                    isUserCreated = true,
                    isPinned = true
                ),
                PlaylistDto(
                    id = "pl_peace",
                    title = "Peace 🖤",
                    subtitle = "Playlist • Chill Vibe",
                    coverUrl = "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg",
                    trackCount = 8,
                    creatorName = "You",
                    isUserCreated = true
                ),
                PlaylistDto(
                    id = "pl_10s",
                    title = "<10s",
                    subtitle = "Playlist • 2010s Nostalgia",
                    coverUrl = "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg",
                    trackCount = 15,
                    creatorName = "You",
                    isUserCreated = true
                )
            )
            _playlists.value = initial
            persistPlaylists(context, initial)
        }

        if (!trJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<Map<String, List<TrackDto>>>() {}.type
                val map: Map<String, List<TrackDto>> = gson.fromJson(trJson, type)
                _playlistTracks.value = map.mapValues { entry -> entry.value.map { it.sanitized() } }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun syncFromApi(context: Context, apiPlaylists: List<PlaylistDto>) {
        if (apiPlaylists.isEmpty()) return
        val current = _playlists.value.toMutableList()
        val merged = mutableListOf<PlaylistDto>()

        // Add API playlists
        apiPlaylists.forEach { apiPl ->
            val existing = current.find { it.id == apiPl.id }
            if (existing != null) {
                merged.add(apiPl.copy(isPinned = existing.isPinned))
            } else {
                merged.add(apiPl)
            }
        }

        // Keep any local-only user playlists that haven't synced yet
        current.forEach { localPl ->
            if (merged.none { it.id == localPl.id }) {
                merged.add(localPl)
            }
        }

        _playlists.value = merged
        persistPlaylists(context, merged)
    }

    fun createPlaylist(
        context: Context,
        title: String,
        description: String = "",
        coverUrl: String = "",
        creatorName: String = "You"
    ): PlaylistDto {
        val newId = "pl_usr_" + System.currentTimeMillis()
        val effectiveCover = coverUrl.ifBlank {
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
        }
        val newPlaylist = PlaylistDto(
            id = newId,
            title = title.ifBlank { "My Playlist" },
            subtitle = if (description.isNotBlank()) description else "Playlist • $creatorName • 0 songs",
            artworkType = "custom",
            coverUrl = effectiveCover,
            trackCount = 0,
            creatorName = creatorName,
            isUserCreated = true,
            isPinned = false
        )

        val current = _playlists.value.toMutableList()
        current.add(0, newPlaylist)
        _playlists.value = current
        persistPlaylists(context, current)
        return newPlaylist
    }

    fun updatePlaylist(
        context: Context,
        id: String,
        title: String? = null,
        description: String? = null,
        coverUrl: String? = null,
        isPinned: Boolean? = null
    ) {
        val current = _playlists.value.map { pl ->
            if (pl.id == id) {
                pl.copy(
                    title = title?.takeIf { it.isNotBlank() } ?: pl.title,
                    subtitle = description ?: pl.subtitle,
                    coverUrl = coverUrl?.takeIf { it.isNotBlank() } ?: pl.coverUrl,
                    isPinned = isPinned ?: pl.isPinned
                )
            } else pl
        }
        _playlists.value = current
        persistPlaylists(context, current)
    }

    fun togglePin(context: Context, id: String) {
        val current = _playlists.value.map { pl ->
            if (pl.id == id) pl.copy(isPinned = !pl.isPinned) else pl
        }
        _playlists.value = current
        persistPlaylists(context, current)
    }

    fun deletePlaylist(context: Context, id: String) {
        val current = _playlists.value.filter { it.id != id }
        _playlists.value = current
        persistPlaylists(context, current)

        val tracksMap = _playlistTracks.value.toMutableMap()
        tracksMap.remove(id)
        _playlistTracks.value = tracksMap
        persistTracksMap(context, tracksMap)
    }

    fun addTrack(context: Context, playlistId: String, track: TrackDto): Boolean {
        val safeTrack = track.sanitized()
        val tracksMap = _playlistTracks.value.toMutableMap()
        val currentList = tracksMap[playlistId]?.toMutableList() ?: mutableListOf()
        if (currentList.any { it.id == safeTrack.id }) {
            return false // Already exists
        }
        currentList.add(safeTrack)
        tracksMap[playlistId] = currentList
        _playlistTracks.value = tracksMap
        persistTracksMap(context, tracksMap)

        // Update count in playlist header
        val currentPls = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                val newCount = currentList.size
                val updatedSub = "Playlist • ${pl.creatorName} • $newCount songs"
                pl.copy(trackCount = newCount, subtitle = updatedSub, coverUrl = pl.coverUrl.ifBlank { safeTrack.effectiveCoverUrl })
            } else pl
        }
        _playlists.value = currentPls
        persistPlaylists(context, currentPls)
        return true
    }

    fun removeTrack(context: Context, playlistId: String, trackId: String) {
        val tracksMap = _playlistTracks.value.toMutableMap()
        val currentList = tracksMap[playlistId]?.toMutableList() ?: return
        currentList.removeAll { it.id == trackId }
        tracksMap[playlistId] = currentList
        _playlistTracks.value = tracksMap
        persistTracksMap(context, tracksMap)

        // Update count
        val currentPls = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                val newCount = currentList.size
                val updatedSub = "Playlist • ${pl.creatorName} • $newCount songs"
                pl.copy(trackCount = newCount, subtitle = updatedSub)
            } else pl
        }
        _playlists.value = currentPls
        persistPlaylists(context, currentPls)
    }

    fun getTracks(playlistId: String): List<TrackDto> {
        return _playlistTracks.value[playlistId]?.map { it.sanitized() } ?: emptyList()
    }

    fun setTracks(context: Context, playlistId: String, tracks: List<TrackDto>) {
        val safeTracks = tracks.map { it.sanitized() }
        val tracksMap = _playlistTracks.value.toMutableMap()
        tracksMap[playlistId] = safeTracks
        _playlistTracks.value = tracksMap
        persistTracksMap(context, tracksMap)
    }

    fun getPlaylist(playlistId: String): PlaylistDto? {
        return _playlists.value.find { it.id == playlistId }
    }

    private fun persistPlaylists(context: Context, list: List<PlaylistDto>) {
        val prefs = context.getSharedPreferences("sonexa_user_playlists", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_playlists", gson.toJson(list)).apply()
    }

    private fun persistTracksMap(context: Context, map: Map<String, List<TrackDto>>) {
        val prefs = context.getSharedPreferences("sonexa_user_playlists", Context.MODE_PRIVATE)
        prefs.edit().putString("playlist_tracks_map", gson.toJson(map)).apply()
    }
}
