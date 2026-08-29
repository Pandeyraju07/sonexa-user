package com.sonexa.app.data.provider

import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IPopProvider {

    suspend fun getHomeFeed(subgenre: String? = null): IPopHomeResponse =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.getIPopFeed(subgenre)
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getFallbackHome(subgenre)
        }

    suspend fun getPlaylist(id: String): IPopPlaylistDto =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.getIPopPlaylist(id)
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val home = getFallbackHome(null)
            home.featuredPlaylists.find { it.id == id } ?: home.featuredPlaylists.first()
        }

    suspend fun getArtists(): List<IPopArtistDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.musicApiService.getIPopArtists()
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getFallbackHome(null).spotlightArtists
        }

    private fun getFallbackHome(subgenre: String?): IPopHomeResponse {
        val tracks = listOf(
            TrackDto(id = "ipop_1", title = "Maan Meri Jaan", artist = "King", album = "Champagne Talk", durationMs = 194000L, audioUrl = "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", coverUrl = "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", playsCount = "485M", isLiked = true, provider = "ipop", providerType = "audio"),
            TrackDto(id = "ipop_2", title = "cold/mess", artist = "Prateek Kuhad", album = "cold/mess EP", durationMs = 272000L, audioUrl = "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", playsCount = "192M", isLiked = true, provider = "ipop", providerType = "audio"),
            TrackDto(id = "ipop_3", title = "Husn", artist = "Anuv Jain", album = "Husn - Single", durationMs = 218000L, audioUrl = "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", coverUrl = "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", playsCount = "340M", isLiked = true, provider = "ipop", providerType = "audio"),
            TrackDto(id = "ipop_4", title = "With You", artist = "AP Dhillon", album = "With You", durationMs = 154000L, audioUrl = "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", coverUrl = "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", playsCount = "260M", isLiked = true, provider = "ipop", providerType = "audio"),
            TrackDto(id = "ipop_5", title = "Heeriye", artist = "Jasleen Royal, Arijit Singh", album = "Heeriye", durationMs = 195000L, audioUrl = "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", coverUrl = "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", playsCount = "520M", isLiked = true, provider = "ipop", providerType = "audio"),
            TrackDto(id = "ipop_6", title = "Liggi", artist = "Ritviz", album = "Liggi Single", durationMs = 182000L, audioUrl = "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", playsCount = "180M", isLiked = true, provider = "ipop", providerType = "audio")
        )

        val artists = listOf(
            IPopArtistDto("art_king", "King", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "12.4M", "Maan Meri Jaan", true),
            IPopArtistDto("art_prateek", "Prateek Kuhad", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", "4.8M", "cold/mess", true),
            IPopArtistDto("art_anuv", "Anuv Jain", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "8.2M", "Husn", true),
            IPopArtistDto("art_ap", "AP Dhillon", "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "14.1M", "With You", true),
            IPopArtistDto("art_jasleen", "Jasleen Royal", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "6.5M", "Heeriye", true)
        )

        val playlists = listOf(
            IPopPlaylistDto("pl_ipop_superhits", "I-Pop Superhits 2026", "The defining sound of modern Indian pop music.", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80", "🔥 TRENDING #1", tracks.size, tracks),
            IPopPlaylistDto("pl_indie_chill", "Indie India: Acoustic & Chill", "Soothing indie melodies and acoustic guitars.", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80", "🌿 VIBES", tracks.size, tracks),
            IPopPlaylistDto("pl_pop_punjabi", "Pop Punjabi Heat", "Banging basslines and modern pop synths.", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&q=80", "⚡ ENERGETIC", tracks.size, tracks)
        )

        return IPopHomeResponse(
            success = true,
            title = "Home of I-Pop",
            subtitle = "Discover the pulse of Indian Pop and Indie hits.",
            spotlightBannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&q=80",
            spotlightTitle = "I-Pop Superstars • 2026 Spotlight",
            spotlightSubtitle = "The golden era of Indian Pop music featuring King, Prateek Kuhad, Anuv Jain.",
            subgenres = listOf("All", "Indie Acoustic", "Desi Pop", "Late Night Beats", "Punjabi Pop", "Romantic I-Pop", "Hip-Hop Crossover"),
            trendingTracks = tracks,
            featuredPlaylists = playlists,
            spotlightArtists = artists,
            newReleases = tracks.take(4)
        )
    }
}
