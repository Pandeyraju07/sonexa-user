package com.sonexa.app.data.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Free, official Deezer Search API provider.
 * Free legal audio preview streams (MP3), rich album covers, and global catalog search.
 */
class DeezerMusicProvider : MusicProvider {

    override val providerId: String = "deezer"
    override val displayName: String = "Deezer (Open Preview)"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = com.sonexa.app.data.api.RetrofitClient.gson
    private val apiBase = "https://api.deezer.com"

    private var lastLatencyMs: Long = 0
    private var lastSuccessTimestamp: Long? = null
    private var lastError: String? = null

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isBlank()) return@withContext Result.success(emptyList())

            val start = System.currentTimeMillis()
            try {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val url = "$apiBase/search?q=$encoded&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/115.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    lastLatencyMs = System.currentTimeMillis() - start
                    if (!response.isSuccessful) {
                        lastError = "HTTP ${response.code}"
                        return@withContext Result.failure(Exception("Deezer API error: ${response.code}"))
                    }

                    val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
                    val json = gson.fromJson(bodyString, JsonObject::class.java)
                    val dataArray = json.getAsJsonArray("data") ?: return@withContext Result.success(emptyList())

                    val tracks = mutableListOf<TrackDto>()
                    for (element in dataArray) {
                        val obj = element.asJsonObject
                        val track = parseTrack(obj)
                        if (track != null) tracks.add(track)
                    }

                    lastSuccessTimestamp = System.currentTimeMillis()
                    lastError = null
                    Result.success(tracks)
                }
            } catch (e: Exception) {
                lastLatencyMs = System.currentTimeMillis() - start
                lastError = e.message
                Result.failure(e)
            }
        }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val url = "$apiBase/chart/0/tracks?limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/115.0")
                .build()

            client.newCall(request).execute().use { response ->
                lastLatencyMs = System.currentTimeMillis() - start
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Deezer Chart error: ${response.code}"))
                }

                val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
                val json = gson.fromJson(bodyString, JsonObject::class.java)
                val dataArray = json.getAsJsonArray("data") ?: return@withContext Result.success(emptyList())

                val tracks = mutableListOf<TrackDto>()
                for (element in dataArray) {
                    val obj = element.asJsonObject
                    val track = parseTrack(obj)
                    if (track != null) tracks.add(track)
                }

                Result.success(tracks)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkHealth(): ProviderHealth {
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = true,
            isAvailable = lastError == null,
            latencyMs = lastLatencyMs,
            lastSuccessfulRequestTimestamp = lastSuccessTimestamp,
            errorMessage = lastError,
            quotaUsageInfo = "Free Open API"
        )
    }

    private fun parseTrack(obj: JsonObject): TrackDto? {
        val trackId = if (obj.has("id") && !obj.get("id").isJsonNull) {
            obj.get("id").asString
        } else return null

        val title = if (obj.has("title") && !obj.get("title").isJsonNull) {
            obj.get("title").asString
        } else "Unknown Track"

        var artistName = "Unknown Artist"
        if (obj.has("artist") && obj.get("artist").isJsonObject) {
            val artistObj = obj.getAsJsonObject("artist")
            if (artistObj.has("name") && !artistObj.get("name").isJsonNull) {
                artistName = artistObj.get("name").asString
            }
        }

        var albumTitle = title
        var coverUrl = ""
        if (obj.has("album") && obj.get("album").isJsonObject) {
            val albumObj = obj.getAsJsonObject("album")
            if (albumObj.has("title") && !albumObj.get("title").isJsonNull) {
                albumTitle = albumObj.get("title").asString
            }
            if (albumObj.has("cover_big") && !albumObj.get("cover_big").isJsonNull) {
                coverUrl = albumObj.get("cover_big").asString
            } else if (albumObj.has("cover_medium") && !albumObj.get("cover_medium").isJsonNull) {
                coverUrl = albumObj.get("cover_medium").asString
            }
        }

        val previewUrl = if (obj.has("preview") && !obj.get("preview").isJsonNull) {
            obj.get("preview").asString
        } else ""

        val durationSec = if (obj.has("duration") && !obj.get("duration").isJsonNull) {
            obj.get("duration").asLong
        } else 30L

        return TrackDto(
            id = "deezer_$trackId",
            title = title,
            artist = artistName,
            album = albumTitle,
            durationMs = durationSec * 1000L,
            audioUrl = previewUrl,
            coverUrl = coverUrl,
            provider = "deezer",
            providerTrackId = trackId,
            isPlayable = previewUrl.isNotBlank(),
            providerType = "audio",
            availableProviders = listOf("Deezer", "Zynera"),
            isOfficial = true,
            qualityTier = "EXACT_MATCH"
        )
    }
}
