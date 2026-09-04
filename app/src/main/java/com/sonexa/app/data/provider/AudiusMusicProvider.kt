package com.sonexa.app.data.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sonexa.app.data.model.ArtistAlbumSectionDto
import com.sonexa.app.data.model.ResolvedArtist
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AudiusMusicProvider : MusicProvider {
    override val providerId: String = "audius"
    override val displayName: String = "Audius"
    override val isEnabled: Boolean = true
    override val isConfigured: Boolean = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val host = "https://discoveryprovider.audius.co/v1"
    private val discoveryHosts = listOf(
        "https://api.audius.co/v1",
        "https://discoveryprovider.audius.co/v1",
        "https://audius-discovery-1.cultur3stake.com/v1",
        "https://audius-dp.amsterdam.creatorseed.com/v1",
        "https://discoveryprovider2.audius.co/v1",
        "https://discoveryprovider3.audius.co/v1",
        "https://dn-usa.audius.metadata.fyi/v1",
        "https://discoveryprovider.openplayer.org/v1"
    )
    private val appName = "Zynera"

    override suspend fun search(query: String, filter: String, limit: Int): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isBlank()) return@withContext Result.success(emptyList())

            val encoded = URLEncoder.encode(q, "UTF-8")
            for (host in discoveryHosts) {
                try {
                    val url = "$host/tracks/search?query=$encoded&app_name=$appName&limit=$limit"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Zynera-Android/1.0")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use

                        val bodyString = response.body?.string() ?: return@use
                        val json = gson.fromJson(bodyString, JsonObject::class.java)
                        val dataArray = json.getAsJsonArray("data") ?: return@use

                        val tracks = mutableListOf<TrackDto>()
                        for (element in dataArray) {
                            val obj = element.asJsonObject
                            val track = parseTrack(obj)
                            if (track != null) tracks.add(track)
                        }

                        if (tracks.isNotEmpty()) {
                            return@withContext Result.success(tracks)
                        }
                    }
                } catch (_: Exception) {
                    // Failover to next discovery node
                }
            }
            Result.success(emptyList())
        }

    /**
     * Resolves the canonical Audius artist profile by name.
     */
    suspend fun resolveArtist(artistName: String): Result<ResolvedArtist?> = withContext(Dispatchers.IO) {
        val q = artistName.trim()
        if (q.isBlank()) return@withContext Result.success(null)

        try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val url = "$host/users/search?query=$encoded&app_name=$appName"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Zynera-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.success(null)

                val bodyString = response.body?.string() ?: return@withContext Result.success(null)
                val json = gson.fromJson(bodyString, JsonObject::class.java)
                val dataArray = json.getAsJsonArray("data") ?: return@withContext Result.success(null)

                if (dataArray.size() == 0) return@withContext Result.success(null)

                // Pick the best match
                val best = dataArray[0].asJsonObject
                val userId = if (best.has("id")) best.get("id").asString else ""
                val name = if (best.has("name") && !best.get("name").isJsonNull) best.get("name").asString else q
                val bio = if (best.has("bio") && !best.get("bio").isJsonNull) best.get("bio").asString else ""
                val followers = if (best.has("follower_count") && !best.get("follower_count").isJsonNull) best.get("follower_count").asLong else 0L
                val isVerified = if (best.has("is_verified") && !best.get("is_verified").isJsonNull) best.get("is_verified").asBoolean else false

                var coverUrl = ""
                if (best.has("profile_picture") && best.get("profile_picture").isJsonObject) {
                    val pic = best.getAsJsonObject("profile_picture")
                    coverUrl = if (pic.has("480x480") && !pic.get("480x480").isJsonNull) {
                        pic.get("480x480").asString
                    } else if (pic.has("1000x1000") && !pic.get("1000x1000").isJsonNull) {
                        pic.get("1000x1000").asString
                    } else if (pic.has("150x150") && !pic.get("150x150").isJsonNull) {
                        pic.get("150x150").asString
                    } else ""
                }

                val resolved = ResolvedArtist(
                    canonicalName = name,
                    canonicalId = "art_audius_$userId",
                    providerIds = mapOf("audius" to userId),
                    bio = bio,
                    imageUrl = coverUrl,
                    followersCount = followers,
                    isVerified = isVerified,
                    confidence = 0.92
                )
                Result.success(resolved)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves an artist's full track catalog with pagination offset.
     */
    suspend fun getArtistTracks(userId: String, offset: Int = 0, limit: Int = 50): Result<List<TrackDto>> =
        withContext(Dispatchers.IO) {
            if (userId.isBlank()) return@withContext Result.success(emptyList())

            try {
                val url = "$host/users/$userId/tracks?offset=$offset&limit=$limit&app_name=$appName"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Zynera-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Audius artist tracks error: ${response.code}"))
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

    /**
     * Retrieves an artist's albums and playlists.
     */
    suspend fun getArtistAlbums(userId: String): Result<List<ArtistAlbumSectionDto>> =
        withContext(Dispatchers.IO) {
            if (userId.isBlank()) return@withContext Result.success(emptyList())

            try {
                val url = "$host/users/$userId/albums?app_name=$appName"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Zynera-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.success(emptyList())

                    val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
                    val json = gson.fromJson(bodyString, JsonObject::class.java)
                    val dataArray = json.getAsJsonArray("data") ?: return@withContext Result.success(emptyList())

                    val albums = mutableListOf<ArtistAlbumSectionDto>()
                    for (element in dataArray) {
                        val obj = element.asJsonObject
                        val albumId = if (obj.has("id")) obj.get("id").asString else ""
                        val title = if (obj.has("playlist_name")) obj.get("playlist_name").asString else "Album"
                        val count = if (obj.has("track_count")) obj.get("track_count").asInt else 0
                        val year = if (obj.has("created_at") && obj.get("created_at").asString.length >= 4) {
                            obj.get("created_at").asString.take(4)
                        } else "2024"

                        var artUrl = ""
                        if (obj.has("artwork") && obj.get("artwork").isJsonObject) {
                            val art = obj.getAsJsonObject("artwork")
                            artUrl = if (art.has("480x480") && !art.get("480x480").isJsonNull) art.get("480x480").asString else ""
                        }

                        if (albumId.isNotBlank()) {
                            albums.add(
                                ArtistAlbumSectionDto(
                                    id = "alb_audius_$albumId",
                                    title = title,
                                    year = year,
                                    coverUrl = artUrl,
                                    trackCount = count
                                )
                            )
                        }
                    }
                    Result.success(albums)
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
                .header("User-Agent", "Zynera-Android/1.0")
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

        val mood = if (obj.has("mood") && !obj.get("mood").isJsonNull) {
            obj.get("mood").asString
        } else "Chill"

        val durationSec = if (obj.has("duration") && !obj.get("duration").isJsonNull) {
            obj.get("duration").asLong
        } else 180L

        val tagsList = mutableListOf<String>()
        if (obj.has("tags") && !obj.get("tags").isJsonNull) {
            val rawTags = obj.get("tags").asString
            tagsList.addAll(rawTags.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }

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

        // Estimate energy from mood and genre
        val energyVal = when {
            genre.contains("Electronic", true) || genre.contains("EDM", true) || genre.contains("Rock", true) -> 0.85
            mood.contains("Energetic", true) || mood.contains("Party", true) -> 0.88
            mood.contains("Relaxing", true) || mood.contains("Calm", true) || mood.contains("Chill", true) -> 0.35
            mood.contains("Romantic", true) || mood.contains("Love", true) -> 0.50
            else -> 0.60
        }

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
            isOfficial = true,
            bpm = 118.0,
            energy = energyVal,
            mood = mood,
            moods = listOf(mood),
            genres = listOf(genre),
            tags = tagsList
        )
    }
}
