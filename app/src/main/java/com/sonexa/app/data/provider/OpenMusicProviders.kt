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

class JamendoProvider : MusicProvider {
    override val providerId: String = "jamendo"
    override val displayName: String = "Jamendo (Creative Commons)"
    override val isEnabled: Boolean
        get() = isConfigured
    private val clientId: String
        get() = com.sonexa.app.BuildConfig.JAMENDO_CLIENT_ID.trim().ifBlank { "563f1052" }

    override val isConfigured: Boolean
        get() = true

    // Licensing and deployment compliance configuration
    var streamingAllowed: Boolean = true
    var metadataAllowed: Boolean = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val apiBase = "https://api.jamendo.com/v3.0"

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isBlank()) return@withContext Result.success(emptyList())
            if (!isConfigured || !metadataAllowed) return@withContext Result.success(emptyList())

            try {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val url = "$apiBase/tracks/?client_id=$clientId&format=json&namesearch=$encoded&include=musicinfo+licenses+stats&audioformat=mp32&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Zynera-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Jamendo API error: ${response.code}"))
                    }

                    val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
                    val json = gson.fromJson(bodyString, JsonObject::class.java)
                    val resultsArray = json.getAsJsonArray("results") ?: return@withContext Result.success(emptyList())

                    val tracks = mutableListOf<TrackDto>()
                    for (element in resultsArray) {
                        val obj = element.asJsonObject
                        val track = parseJamendoTrack(obj)
                        if (track != null) tracks.add(track)
                    }
                    Result.success(tracks)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTrending(limit: Int): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        if (!isConfigured || !metadataAllowed) return@withContext Result.success(emptyList())
        try {
            val url = "$apiBase/tracks/?client_id=$clientId&format=json&boost=popularity_total&include=musicinfo+licenses+stats&audioformat=mp32&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Zynera-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Jamendo trending error: ${response.code}"))
                }

                val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
                val json = gson.fromJson(bodyString, JsonObject::class.java)
                val resultsArray = json.getAsJsonArray("results") ?: return@withContext Result.success(emptyList())

                val tracks = mutableListOf<TrackDto>()
                for (element in resultsArray) {
                    val obj = element.asJsonObject
                    val track = parseJamendoTrack(obj)
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
            quotaUsageInfo = "Creative Commons 3.0 API",
            errorMessage = res.exceptionOrNull()?.message
        )
    }

    private fun parseJamendoTrack(obj: JsonObject): TrackDto? {
        val id = if (obj.has("id")) obj.get("id").asString else return null
        val name = if (obj.has("name") && !obj.get("name").isJsonNull) obj.get("name").asString else "Untitled Track"
        val artist = if (obj.has("artist_name") && !obj.get("artist_name").isJsonNull) obj.get("artist_name").asString else "Jamendo Artist"
        val album = if (obj.has("album_name") && !obj.get("album_name").isJsonNull) obj.get("album_name").asString else "Single"
        val durationSec = if (obj.has("duration") && !obj.get("duration").isJsonNull) obj.get("duration").asLong else 180L
        val image = if (obj.has("image") && !obj.get("image").isJsonNull) obj.get("image").asString else ""
        val audio = if (obj.has("audio") && !obj.get("audio").isJsonNull && streamingAllowed) obj.get("audio").asString else ""
        val shareUrl = if (obj.has("shareurl") && !obj.get("shareurl").isJsonNull) obj.get("shareurl").asString else "https://www.jamendo.com/track/$id"

        var bpmVal = 115.0
        var genreVal = "Indie"
        val tagsList = mutableListOf<String>()

        if (obj.has("musicinfo") && obj.get("musicinfo").isJsonObject) {
            val info = obj.getAsJsonObject("musicinfo")
            if (info.has("speed") && !info.get("speed").isJsonNull) {
                val speed = info.get("speed").asString
                bpmVal = when (speed.lowercase()) {
                    "very slow" -> 70.0
                    "slow" -> 90.0
                    "medium" -> 115.0
                    "fast" -> 135.0
                    "very fast" -> 155.0
                    else -> 115.0
                }
            }
            if (info.has("tags") && info.get("tags").isJsonObject) {
                val tagsObj = info.getAsJsonObject("tags")
                if (tagsObj.has("genres") && tagsObj.get("genres").isJsonArray) {
                    val genresArr = tagsObj.getAsJsonArray("genres")
                    if (genresArr.size() > 0) genreVal = genresArr[0].asString
                }
            }
        }

        val energyVal = if (bpmVal > 130) 0.85 else if (bpmVal < 85) 0.35 else 0.60

        return TrackDto(
            id = "jam_$id",
            title = name,
            artist = artist,
            album = album,
            durationMs = durationSec * 1000L,
            audioUrl = audio,
            coverUrl = image.ifBlank { "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500" },
            provider = "jamendo",
            providerTrackId = id,
            providerUrl = shareUrl,
            isPlayable = audio.isNotBlank() && streamingAllowed,
            providerType = "audio",
            availableProviders = listOf("Jamendo"),
            isOfficial = true,
            bpm = bpmVal,
            energy = energyVal,
            mood = if (energyVal > 0.7) "Energetic" else "Chill",
            genres = listOf(genreVal),
            tags = tagsList
        )
    }
}
