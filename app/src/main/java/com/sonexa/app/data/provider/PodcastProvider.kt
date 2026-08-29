package com.sonexa.app.data.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sonexa.app.data.model.PodcastDetailResponse
import com.sonexa.app.data.model.PodcastDto
import com.sonexa.app.data.model.PodcastEpisodeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PodcastProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getPodcastsByCategory(category: String, limit: Int = 25): Result<List<PodcastDto>> =
        withContext(Dispatchers.IO) {
            try {
                val query = if (category.equals("all", ignoreCase = true)) "top podcast" else category
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://itunes.apple.com/search?term=$encoded&media=podcast&entity=podcast&limit=$limit"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "TuneFlow-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to fetch podcasts: ${response.code}"))
                    }

                    val body = response.body?.string().orEmpty()
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val results = json.getAsJsonArray("results") ?: return@withContext Result.success(emptyList())

                    val list = mutableListOf<PodcastDto>()
                    for (el in results) {
                        val obj = el.asJsonObject
                        val id = if (obj.has("collectionId")) obj.get("collectionId").asString else ""
                        val title = if (obj.has("collectionName")) obj.get("collectionName").asString else "Podcast Show"
                        val host = if (obj.has("artistName")) obj.get("artistName").asString else "Host"
                        val genre = if (obj.has("primaryGenreName")) obj.get("primaryGenreName").asString else "Podcasts"
                        var coverUrl = if (obj.has("artworkUrl600")) obj.get("artworkUrl600").asString else ""
                        if (coverUrl.isBlank() && obj.has("artworkUrl100")) {
                            coverUrl = obj.get("artworkUrl100").asString
                        }

                        if (id.isNotBlank()) {
                            list.add(
                                PodcastDto(
                                    id = "pod_$id",
                                    title = title,
                                    host = host,
                                    description = "Top show in $genre",
                                    coverUrl = coverUrl,
                                    category = genre
                                )
                            )
                        }
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPodcastEpisodes(podcastId: String, limit: Int = 25): Result<PodcastDetailResponse> =
        withContext(Dispatchers.IO) {
            try {
                val rawId = if (podcastId.startsWith("pod_")) podcastId.substring(4) else podcastId
                val url = "https://itunes.apple.com/lookup?id=$rawId&entity=podcastEpisode&limit=$limit"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "TuneFlow-Android/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Failed to lookup episodes: ${response.code}"))
                    }

                    val body = response.body?.string().orEmpty()
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val results = json.getAsJsonArray("results") ?: return@withContext Result.success(
                        PodcastDetailResponse(true, null, emptyList())
                    )

                    var show: PodcastDto? = null
                    val episodes = mutableListOf<PodcastEpisodeDto>()

                    for (el in results) {
                        val obj = el.asJsonObject
                        val wrapper = if (obj.has("wrapperType")) obj.get("wrapperType").asString else ""

                        if (wrapper == "track" && show == null) {
                            val id = "pod_" + if (obj.has("collectionId")) obj.get("collectionId").asString else rawId
                            val title = if (obj.has("collectionName")) obj.get("collectionName").asString else "Podcast Show"
                            val host = if (obj.has("artistName")) obj.get("artistName").asString else "Host"
                            val genre = if (obj.has("primaryGenreName")) obj.get("primaryGenreName").asString else "Podcasts"
                            var coverUrl = if (obj.has("artworkUrl600")) obj.get("artworkUrl600").asString else ""
                            if (coverUrl.isBlank() && obj.has("artworkUrl100")) {
                                coverUrl = obj.get("artworkUrl100").asString
                            }
                            show = PodcastDto(id, title, host, "Featured Show in $genre", coverUrl, genre)
                        } else if (wrapper == "podcastEpisode") {
                            val epId = "ep_" + if (obj.has("trackId")) obj.get("trackId").asString else "${System.currentTimeMillis()}"
                            val epTitle = if (obj.has("trackName")) obj.get("trackName").asString else "Episode"
                            val epDesc = if (obj.has("description")) obj.get("description").asString else ""
                            val durationMs = if (obj.has("trackTimeMillis")) obj.get("trackTimeMillis").asLong else 0L
                            val mins = durationMs / (1000 * 60)
                            val durLabel = if (mins > 60) "${mins / 60}h ${mins % 60}m" else "$mins min"
                            val audioUrl = if (obj.has("episodeUrl")) obj.get("episodeUrl").asString else ""
                            val epNum = if (obj.has("trackNumber")) obj.get("trackNumber").asInt else episodes.size + 1

                            if (audioUrl.isNotBlank()) {
                                episodes.add(
                                    PodcastEpisodeDto(
                                        id = epId,
                                        title = epTitle,
                                        description = epDesc,
                                        durationLabel = durLabel,
                                        audioUrl = audioUrl,
                                        episodeNumber = epNum
                                    )
                                )
                            }
                        }
                    }

                    Result.success(PodcastDetailResponse(true, show, episodes))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
