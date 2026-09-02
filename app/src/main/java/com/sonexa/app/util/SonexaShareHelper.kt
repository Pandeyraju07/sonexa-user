package com.sonexa.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.sonexa.app.data.model.TrackDto
import java.net.URLEncoder

object SonexaShareHelper {

    private const val BASE_WEB_URL = "https://zynera.app"

    /**
     * Generates a standard, clickable URL for any track with embedded metadata.
     */
    fun generateTrackShareUrl(track: TrackDto): String {
        val encodedId = try {
            URLEncoder.encode(track.id, "UTF-8")
        } catch (_: Exception) {
            track.id
        }
        val builder = Uri.parse("$BASE_WEB_URL/track/$encodedId").buildUpon()

        if (track.title.isNotBlank()) builder.appendQueryParameter("title", track.title)
        if (track.artist.isNotBlank()) builder.appendQueryParameter("artist", track.artist)
        if (track.album.isNotBlank()) builder.appendQueryParameter("album", track.album)
        if (track.effectiveCoverUrl.isNotBlank()) builder.appendQueryParameter("cover", track.effectiveCoverUrl)
        if (track.audioUrl.isNotBlank()) builder.appendQueryParameter("audio", track.audioUrl)
        if (track.provider.isNotBlank()) builder.appendQueryParameter("provider", track.provider)
        if (track.effectiveVideoId.isNotBlank()) builder.appendQueryParameter("vid", track.effectiveVideoId)

        return builder.build().toString()
    }

    /**
     * Shares a song with an attractive message and a guaranteed clickable link.
     */
    fun shareTrack(context: Context, track: TrackDto) {
        try {
            val url = generateTrackShareUrl(track)
            val shareText = "🎵 Listen to \"${track.title}\" by ${track.artist} on Zynera:\n$url\n\nExperience high-fidelity 320kbps audio & AI vibe flow on Zynera."

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "🎵 ${track.title} - ${track.artist}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Track"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a playlist with a clickable link.
     */
    fun sharePlaylist(context: Context, playlistId: String, title: String) {
        try {
            val encodedId = try { URLEncoder.encode(playlistId, "UTF-8") } catch (_: Exception) { playlistId }
            val url = "$BASE_WEB_URL/playlist/$encodedId?title=${URLEncoder.encode(title, "UTF-8")}"
            val shareText = "🎶 Check out the playlist \"$title\" on Zynera:\n$url"

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "🎶 Playlist: $title")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Playlist"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares an album with a clickable link.
     */
    fun shareAlbum(context: Context, albumId: String, title: String, artist: String) {
        try {
            val encodedId = try { URLEncoder.encode(albumId, "UTF-8") } catch (_: Exception) { albumId }
            val url = "$BASE_WEB_URL/album/$encodedId?title=${URLEncoder.encode(title, "UTF-8")}&artist=${URLEncoder.encode(artist, "UTF-8")}"
            val shareText = "💿 Listen to the album \"$title\" by $artist on Zynera:\n$url"

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "💿 Album: $title - $artist")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Album"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares an artist profile with a clickable link.
     */
    fun shareArtist(context: Context, artistId: String, artistName: String) {
        try {
            val encodedId = try { URLEncoder.encode(artistId, "UTF-8") } catch (_: Exception) { artistId }
            val url = "$BASE_WEB_URL/artist/$encodedId?name=${URLEncoder.encode(artistName, "UTF-8")}"
            val shareText = "🎤 Explore all songs and albums by $artistName on Zynera:\n$url"

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "🎤 Artist: $artistName")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Artist"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }
}
