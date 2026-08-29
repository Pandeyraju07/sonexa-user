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

    suspend fun getTrackLyrics(id: String, title: String = "", artist: String = ""): Result<LyricsResponse> = withContext(Dispatchers.IO) {
        // 1. Try local server first if available
        if (id.isNotBlank() && !id.startsWith("saavn_") && !id.startsWith("jam_")) {
            val serverResult = runCatching {
                val res = apiService.getTrackLyrics(id)
                if (res.isSuccessful && res.body() != null && (res.body()!!.lines.isNotEmpty() || res.body()!!.plainText.isNotBlank())) {
                    return@withContext Result.success(res.body()!!)
                }
            }
        }

        // 2. Fetch from global real-time synced lyrics provider (LRCLIB)
        val cleanTitle = title.replace(Regex("""\(.*?\)|\[.*?\]"""), "").trim()
        val cleanArtist = artist.split(",", "&", "feat.", "ft.").firstOrNull()?.trim() ?: artist.trim()

        if (cleanTitle.isNotBlank()) {
            val lrclibResult = fetchFromLrclib(cleanTitle, cleanArtist)
            if (lrclibResult != null) {
                return@withContext Result.success(lrclibResult)
            }

            // 3. Fallback to JioSaavn Official Lyrics API
            val saavnResult = fetchFromSaavnLyrics(cleanTitle, cleanArtist)
            if (saavnResult != null) {
                return@withContext Result.success(saavnResult)
            }
        }

        Result.failure(Exception("Lyrics not available for this track"))
    }

    private fun getJsonStringOrNull(json: com.google.gson.JsonObject, key: String): String? {
        return if (json.has(key) && !json.get(key).isJsonNull) {
            json.get(key).asString
        } else null
    }

    private fun fetchFromLrclib(title: String, artist: String): LyricsResponse? {
        try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle"

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "SonexaApp/1.0 (https://sonexa.app)")
                .build()

            val resp = client.newCall(req).execute()
            val body = resp.body?.string().orEmpty()
            if (resp.isSuccessful && body.isNotBlank()) {
                val json = JsonParser.parseString(body).asJsonObject
                val syncedLrc = getJsonStringOrNull(json, "syncedLyrics").orEmpty()
                val plainText = getJsonStringOrNull(json, "plainLyrics").orEmpty()

                if (syncedLrc.isNotBlank() || plainText.isNotBlank()) {
                    val parsedLines = if (syncedLrc.isNotBlank()) parseLrcLyrics(syncedLrc) else emptyList()
                    return LyricsResponse(
                        success = true,
                        trackId = title,
                        synced = parsedLines.isNotEmpty(),
                        lines = parsedLines,
                        plainText = plainText.ifBlank { parsedLines.joinToString("\n") { it.text } },
                        source = "LRCLIB Synced"
                    )
                }
            }

            // Fallback: search query
            val searchUrl = "https://lrclib.net/api/search?q=" + URLEncoder.encode("$title $artist", "UTF-8")
            val searchReq = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "SonexaApp/1.0 (https://sonexa.app)")
                .build()
            val searchResp = client.newCall(searchReq).execute()
            val searchBody = searchResp.body?.string().orEmpty()
            if (searchResp.isSuccessful && searchBody.isNotBlank()) {
                val array = JsonParser.parseString(searchBody).asJsonArray
                if (array.size() > 0) {
                    val first = array[0].asJsonObject
                    val syncedLrc = getJsonStringOrNull(first, "syncedLyrics").orEmpty()
                    val plainText = getJsonStringOrNull(first, "plainLyrics").orEmpty()
                    if (syncedLrc.isNotBlank() || plainText.isNotBlank()) {
                        val parsedLines = if (syncedLrc.isNotBlank()) parseLrcLyrics(syncedLrc) else emptyList()
                        return LyricsResponse(
                            success = true,
                            trackId = title,
                            synced = parsedLines.isNotEmpty(),
                            lines = parsedLines,
                            plainText = plainText.ifBlank { parsedLines.joinToString("\n") { it.text } },
                            source = "LRCLIB Search"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun fetchFromSaavnLyrics(title: String, artist: String): LyricsResponse? {
        try {
            val q = URLEncoder.encode("$title $artist", "UTF-8")
            val url = "https://www.jiosaavn.com/api.php?__call=search.getResults&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=5&p=1&q=$q"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string().orEmpty()
            if (resp.isSuccessful && body.isNotBlank()) {
                val root = JsonParser.parseString(body).asJsonObject
                val results = root.getAsJsonArray("results")
                if (results != null && results.size() > 0) {
                    for (i in 0 until results.size()) {
                        val obj = results[i].asJsonObject
                        val hasLyrics = getJsonStringOrNull(obj, "has_lyrics") == "true"
                        val lyricsId = getJsonStringOrNull(obj, "id").orEmpty()
                        if (hasLyrics && lyricsId.isNotBlank()) {
                            val lyricUrl = "https://www.jiosaavn.com/api.php?__call=lyrics.getLyrics&lyrics_id=$lyricsId&_format=json&ctx=web6dot0"
                            val lyricResp = client.newCall(Request.Builder().url(lyricUrl).build()).execute()
                            val lyricBody = lyricResp.body?.string().orEmpty()
                            if (lyricResp.isSuccessful && lyricBody.isNotBlank()) {
                                val lyricJson = JsonParser.parseString(lyricBody).asJsonObject
                                val lyricsHtml = getJsonStringOrNull(lyricJson, "lyrics").orEmpty()
                                val cleanLyrics = lyricsHtml.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n")
                                if (cleanLyrics.isNotBlank()) {
                                    val plainLines = cleanLyrics.lines().filter { it.isNotBlank() }.mapIndexed { idx, line ->
                                        LyricsLineDto(tMs = idx * 4000L, text = line.trim())
                                    }
                                    return LyricsResponse(
                                        success = true,
                                        trackId = title,
                                        synced = false,
                                        lines = plainLines,
                                        plainText = cleanLyrics,
                                        source = "JioSaavn Official Lyrics"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseLrcLyrics(lrcContent: String): List<LyricsLineDto> {
        val list = mutableListOf<LyricsLineDto>()
        val lines = lrcContent.split("\n")
        val regex = Regex("""\[(\d{2}):(\d{2})\.?(\d{2,3})?\]\s*(.*)""")
        for (line in lines) {
            val trimmed = line.trim()
            val match = regex.find(trimmed)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val fracStr = match.groupValues.getOrNull(3).orEmpty()
                val ms = when (fracStr.length) {
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10
                    3 -> fracStr.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = (min * 60 + sec) * 1000L + ms
                val text = match.groupValues[4].trim()
                if (text.isNotBlank()) {
                    list.add(LyricsLineDto(tMs = totalMs, text = text))
                }
            }
        }
        return list.sortedBy { it.tMs }
    }

    suspend fun getPodcasts(): Result<PodcastListResponse> = apiCall { apiService.getPodcasts() }
    suspend fun getPodcast(id: String): Result<PodcastDetailResponse> = apiCall { apiService.getPodcast(id) }

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
