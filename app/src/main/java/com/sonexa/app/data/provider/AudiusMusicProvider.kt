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

class AudiusMusicProvider : MusicProvider {
    override val providerId: String = "audius"
    override val displayName: String = "Audius (TuneFlow)"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val host = "https://discoveryprovider.audius.co/v1"
    private val appName = "TuneFlow"

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isBlank()) return@withContext Result.success(emptyList())

            try {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val url = "$host/tracks/search?query=$encoded&app_name=$appName&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "TuneFlow-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Audius API error: ${response.code}"))
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

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$host/tracks/trending?app_name=$appName&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TuneFlow-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Audius trending error: ${response.code}"))
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
        val start = System.currentTimeMillis()
        val res = getTrending(1)
        val lat = System.currentTimeMillis() - start
        return ProviderHealth(
            providerId = providerId,
            displayName = displayName,
            isConfigured = true,
            isAvailable = res.isSuccess,
            latencyMs = lat,
            lastSuccessfulRequestTimestamp = if (res.isSuccess) System.currentTimeMillis() else null,
            errorMessage = res.exceptionOrNull()?.message
        )
    }

    private fun parseTrack(obj: JsonObject): TrackDto? {
        val id = if (obj.has("id") && !obj.get("id").isJsonNull) {
            obj.get("id").asString
        } else if (obj.has("track_id")) {
            obj.get("track_id").asString
        } else {
            return null
        }

        val title = if (obj.has("title") && !obj.get("title").isJsonNull) {
            obj.get("title").asString
        } else "Untitled Track"

        var artist = "Audius Artist"
        if (obj.has("user") && obj.get("user").isJsonObject) {
            val userObj = obj.getAsJsonObject("user")
            if (userObj.has("name") && !userObj.get("name").isJsonNull) {
                artist = userObj.get("name").asString
            } else if (userObj.has("handle")) {
                artist = userObj.get("handle").asString
            }
        }

        val genre = if (obj.has("genre") && !obj.get("genre").isJsonNull) {
            obj.get("genre").asString
        } else "Music"

        val durationSec = if (obj.has("duration") && !obj.get("duration").isJsonNull) {
            obj.get("duration").asLong
        } else 180L

        var coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
        if (obj.has("artwork") && obj.get("artwork").isJsonObject) {
            val art = obj.getAsJsonObject("artwork")
            if (art.has("480x480") && !art.get("480x480").isJsonNull) {
                coverUrl = art.get("480x480").asString
            } else if (art.has("1000x1000") && !art.get("1000x1000").isJsonNull) {
                coverUrl = art.get("1000x1000").asString
            } else if (art.has("150x150") && !art.get("150x150").isJsonNull) {
                coverUrl = art.get("150x150").asString
            }
        }

        val streamUrl = "$host/tracks/$id/stream?app_name=$appName"

        return TrackDto(
            id = id,
            title = title,
            artist = artist,
            album = genre,
            durationMs = durationSec * 1000L,
            audioUrl = streamUrl,
            coverUrl = coverUrl,
            provider = "audius",
            providerTrackId = id,
            providerUrl = streamUrl,
            isPlayable = true,
            providerType = "audio",
            availableProviders = listOf("Audius"),
            isOfficial = true
        )
    }
}
