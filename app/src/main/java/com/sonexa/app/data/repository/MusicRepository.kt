package com.sonexa.app.data.repository

import com.google.gson.JsonParser
import com.sonexa.app.data.api.MusicApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class MusicRepository(
    private val apiService: MusicApiService = RetrofitClient.musicApiService,
    private val client: OkHttpClient = RetrofitClient.okHttpClient
) {

    suspend fun getDynamicHomeFeed(): Result<HomeDynamicFeedResponse> = apiCall { apiService.getDynamicHomeFeed() }
    suspend fun getHomeFeed(): Result<HomeFeedResponse> = apiCall { apiService.getHomeFeed() }

    suspend fun trackEvent(eventType: String, trackId: String? = null, positionMs: Long = 0, durationMs: Long = 0) {
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = mutableMapOf<String, Any>(
                    "eventType" to eventType,
                    "positionMs" to positionMs,
                    "durationMs" to durationMs
                )
                if (trackId != null) payload["trackId"] = trackId
                apiService.trackEvent(payload)
            }
        }
    }
    suspend fun getTrending(): Result<TrendingResponse> = apiCall { apiService.getTrending() }
    suspend fun searchMusic(query: String): Result<SearchResponse> = apiCall { apiService.searchMusic(query) }
    suspend fun getTrack(id: String): Result<TrackDetailResponse> = apiCall { apiService.getTrack(id) }
    suspend fun getAlbum(id: String): Result<AlbumDetailResponse> = apiCall { apiService.getAlbum(id) }
    suspend fun getPlaylist(id: String): Result<PlaylistDetailResponse> = apiCall { apiService.getPlaylist(id) }
    suspend fun getArtist(id: String): Result<ArtistDetailResponse> = apiCall { apiService.getArtist(id) }
    suspend fun getGenres(): Result<GenreListResponse> = apiCall { apiService.getGenres() }
    suspend fun getArtists(): Result<ArtistListResponse> = apiCall { apiService.getArtists() }
    suspend fun getMoods(): Result<MoodListResponse> = apiCall { apiService.getMoods() }
    suspend fun getQueue(): Result<QueueResponse> = apiCall { apiService.getQueue() }

    private val freeLyricsProvider = com.sonexa.app.data.lyrics.FreeLyricsProvider(client)

    suspend fun getTrackLyrics(id: String, title: String = "", artist: String = ""): Result<LyricsResponse> = withContext(Dispatchers.IO) {
        // 1. Try local server first if non-thirdparty ID
        if (id.isNotBlank() && !id.startsWith("saavn_") && !id.startsWith("jam_") && !id.startsWith("yt_")) {
            val serverResult = runCatching {
                val res = apiService.getTrackLyrics(id)
                if (res.isSuccessful && res.body() != null && (res.body()!!.lines.isNotEmpty() || res.body()!!.plainText.isNotBlank())) {
                    return@withContext Result.success(res.body()!!)
                }
            }
        }

        // 2. Fetch using multi-tier FreeLyricsProvider (LRCLIB Synced + JioSaavn + Lyrics.ovh)
        val lyrics = freeLyricsProvider.getLyrics(trackId = id, rawTitle = title, rawArtist = artist)
        if (lyrics != null && (lyrics.lines.isNotEmpty() || lyrics.plainText.isNotBlank())) {
            return@withContext Result.success(lyrics)
        }

        Result.failure(Exception("Lyrics not available for this track"))
    }

    suspend fun getPodcasts(): Result<PodcastListResponse> = apiCall { apiService.getPodcasts() }
    suspend fun getPodcast(id: String): Result<PodcastDetailResponse> = apiCall { apiService.getPodcast(id) }

    suspend fun getSearchCategories(): Result<SearchCategoriesResponse> = apiCall { apiService.getSearchCategories() }

    suspend fun generateAiSignature(mood: String, genre: String, prompt: String): Result<AiSignatureResponse> =
        apiCall { apiService.generateAiSignature(AiSignatureRequest(mood, genre, prompt)) }

    suspend fun chatWithAi(message: String): Result<AiChatResponse> =
        apiCall { apiService.chatWithAi(AiChatRequest(message)) }

    private suspend fun <T> apiCall(block: suspend () -> retrofit2.Response<T>): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                val response = block()
                val body = response.body()
                if (response.isSuccessful && body != null) Result.success(body)
                else Result.failure(Exception("Request failed (${response.code()})"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
