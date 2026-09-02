package com.sonexa.app.util

import android.content.Intent
import android.net.Uri
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.MusicAggregationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder

sealed class DeepLinkTarget {
    data class PlayTrack(val track: TrackDto) : DeepLinkTarget()
    data class OpenPlaylist(val playlistId: String, val title: String = "") : DeepLinkTarget()
    data class OpenAlbum(val albumId: String, val title: String = "", val artist: String = "") : DeepLinkTarget()
    data class OpenArtist(val artistId: String, val name: String = "") : DeepLinkTarget()
}

object DeepLinkEvents {
    private val _events = MutableSharedFlow<DeepLinkTarget>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    fun emit(target: DeepLinkTarget) {
        _events.tryEmit(target)
    }
}

object DeepLinkManager {

    private val aggregationEngine = MusicAggregationEngine()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Inspects an incoming Intent and extracts deep link targets.
     */
    fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        val data: Uri = intent.data ?: return false

        if (Intent.ACTION_VIEW != action && !isSonexaUri(data)) {
            return false
        }

        return parseUri(data)
    }

    private fun isSonexaUri(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()
        return scheme == "zynera" ||
                scheme == "sonexa" ||
                scheme == "com.sonexa.app" ||
                (host != null && (
                    host == "zynera.app" || host == "www.zynera.app" ||
                    host == "zynera.com" || host == "www.zynera.com" ||
                    host == "sonexa.app" || host == "www.sonexa.app" ||
                    host == "sonexa.com" || host == "www.sonexa.com"
                ))
    }

    /**
     * Parses the URI path and query params to construct the target.
     */
    fun parseUri(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        if (pathSegments.isEmpty()) return false

        val section = pathSegments[0].lowercase()

        when (section) {
            "track", "song" -> {
                val rawId = if (pathSegments.size > 1) pathSegments[1] else uri.getQueryParameter("id").orEmpty()
                val trackId = safeDecode(rawId)
                val title = safeDecode(uri.getQueryParameter("title").orEmpty())
                val artist = safeDecode(uri.getQueryParameter("artist").orEmpty())
                val album = safeDecode(uri.getQueryParameter("album").orEmpty())
                val coverUrl = safeDecode(uri.getQueryParameter("cover").orEmpty())
                val audioUrl = safeDecode(uri.getQueryParameter("audio").orEmpty())
                val provider = safeDecode(uri.getQueryParameter("provider").orEmpty())
                val videoId = safeDecode(uri.getQueryParameter("vid").orEmpty())

                if (title.isNotBlank() && artist.isNotBlank()) {
                    val track = TrackDto(
                        id = if (trackId.isNotBlank()) trackId else "tr_" + System.currentTimeMillis(),
                        title = title,
                        artist = artist,
                        album = album,
                        coverUrl = coverUrl,
                        audioUrl = audioUrl,
                        provider = if (provider.isNotBlank()) provider else "sonexa",
                        videoId = videoId,
                        isPlayable = true
                    )
                    DeepLinkEvents.emit(DeepLinkTarget.PlayTrack(track))
                    return true
                } else if (trackId.isNotBlank()) {
                    // Query catalog to resolve full track metadata asynchronously
                    scope.launch {
                        try {
                            val searchResult = aggregationEngine.searchAll(query = trackId, limit = 5)
                            val found = searchResult.tracks.firstOrNull { it.id == trackId }
                                ?: searchResult.tracks.firstOrNull()

                            if (found != null) {
                                DeepLinkEvents.emit(DeepLinkTarget.PlayTrack(found))
                            }
                        } catch (_: Exception) {}
                    }
                    return true
                }
            }
            "playlist" -> {
                val playlistId = safeDecode(if (pathSegments.size > 1) pathSegments[1] else uri.getQueryParameter("id").orEmpty())
                val title = safeDecode(uri.getQueryParameter("title").orEmpty())
                if (playlistId.isNotBlank()) {
                    DeepLinkEvents.emit(DeepLinkTarget.OpenPlaylist(playlistId, title))
                    return true
                }
            }
            "album" -> {
                val albumId = safeDecode(if (pathSegments.size > 1) pathSegments[1] else uri.getQueryParameter("id").orEmpty())
                val title = safeDecode(uri.getQueryParameter("title").orEmpty())
                val artist = safeDecode(uri.getQueryParameter("artist").orEmpty())
                if (albumId.isNotBlank()) {
                    DeepLinkEvents.emit(DeepLinkTarget.OpenAlbum(albumId, title, artist))
                    return true
                }
            }
            "artist" -> {
                val artistId = safeDecode(if (pathSegments.size > 1) pathSegments[1] else uri.getQueryParameter("id").orEmpty())
                val name = safeDecode(uri.getQueryParameter("name").orEmpty())
                if (artistId.isNotBlank() || name.isNotBlank()) {
                    DeepLinkEvents.emit(DeepLinkTarget.OpenArtist(artistId, name))
                    return true
                }
            }
        }

        return false
    }

    private fun safeDecode(value: String): String {
        return try {
            URLDecoder.decode(value, "UTF-8")
        } catch (_: Exception) {
            value
        }
    }
}
