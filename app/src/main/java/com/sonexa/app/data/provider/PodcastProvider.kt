package com.sonexa.app.data.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sonexa.app.data.model.*
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

    fun getLanguages(): List<PodcastLanguageDto> = listOf(
        PodcastLanguageDto("all", "All", "All", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", 9500),
        PodcastLanguageDto("hindi", "Hindi", "हिन्दी", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", 1420),
        PodcastLanguageDto("english", "English", "English", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", 5200),
        PodcastLanguageDto("tamil", "Tamil", "தமிழ்", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", 860),
        PodcastLanguageDto("telugu", "Telugu", "తెలుగు", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400", 790),
        PodcastLanguageDto("bengali", "Bengali", "বাংলা", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400", 640),
        PodcastLanguageDto("marathi", "Marathi", "मराठी", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", 580),
        PodcastLanguageDto("punjabi", "Punjabi", "ਪੰਜਾਬੀ", "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=400", 610),
        PodcastLanguageDto("spanish", "Spanish", "Español", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400", 1850),
        PodcastLanguageDto("german", "German", "Deutsch", "https://images.unsplash.com/photo-1445985543469-433ecba627a0?w=400", 920),
        PodcastLanguageDto("japanese", "Japanese", "日本語", "https://images.unsplash.com/photo-1528164344705-475426879c0d?w=400", 780),
        PodcastLanguageDto("arabic", "Arabic", "العربية", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400", 670)
    )

    fun getCategories(): List<PodcastCategoryDto> = listOf(
        PodcastCategoryDto("comedy", "Comedy", "🎙️", "#D97706", "#78350F", "#D97706"),
        PodcastCategoryDto("news", "News", "📰", "#0EA5E9", "#0C4A6E", "#0EA5E9"),
        PodcastCategoryDto("business", "Business", "💼", "#059669", "#064E3B", "#059669"),
        PodcastCategoryDto("education", "Education", "🧠", "#8B5CF6", "#4C1D95", "#8B5CF6"),
        PodcastCategoryDto("technology", "Technology", "🚀", "#2563EB", "#1E3A8A", "#2563EB"),
        PodcastCategoryDto("relationships", "Relationships", "❤️", "#EC4899", "#831843", "#EC4899"),
        PodcastCategoryDto("motivation", "Motivation", "🔥", "#F97316", "#7C2D12", "#F97316"),
        PodcastCategoryDto("true_crime", "True Crime", "🔎", "#DC2626", "#7F1D1D", "#DC2626"),
        PodcastCategoryDto("stories", "Stories", "📚", "#10B981", "#064E3B", "#10B981"),
        PodcastCategoryDto("entertainment", "Entertainment", "🎬", "#6366F1", "#312E81", "#6366F1"),
        PodcastCategoryDto("wellness", "Wellness", "🧘", "#14B8A6", "#134E4A", "#14B8A6"),
        PodcastCategoryDto("finance", "Finance", "💰", "#F59E0B", "#78350F", "#F59E0B"),
        PodcastCategoryDto("science", "Science", "🧪", "#A855F7", "#581C87", "#A855F7"),
        PodcastCategoryDto("sports", "Sports", "🏏", "#3B82F6", "#1D4ED8", "#3B82F6")
    )

    suspend fun getPodcastHomeFeed(): Result<PodcastHomeResponse> = withContext(Dispatchers.IO) {
        try {
            val hindiShows = getPodcastsByCategory("the ranveer show hindi audio pitara", 8).getOrDefault(emptyList())
            val popularShows = getPodcastsByCategory("top podcasts", 8).getOrDefault(emptyList())
            val trending = (hindiShows + popularShows).distinctBy { it.id }

            // Get a realistic continue listening episode from top Hindi show
            val continueList = mutableListOf<PodcastEpisodeDto>()
            if (hindiShows.isNotEmpty()) {
                val epRes = getPodcastEpisodes(hindiShows[0].id, limit = 1).getOrNull()
                val topEp = epRes?.episodes?.firstOrNull()
                if (topEp != null) {
                    continueList.add(topEp.copy(progressPercent = 42, durationLabel = "34 min remaining"))
                }
            }

            Result.success(
                PodcastHomeResponse(
                    success = true,
                    continueListening = continueList,
                    languages = getLanguages(),
                    trendingPodcasts = trending,
                    madeForYou = hindiShows,
                    popularShows = popularShows,
                    categories = getCategories()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPodcastsByCategory(category: String, limit: Int = 25): Result<List<PodcastDto>> =
        withContext(Dispatchers.IO) {
            try {
                val query = when {
                    category.contains("hindi", ignoreCase = true) && category.contains("story", ignoreCase = true) -> "hindi stories kahaniyan podcast"
                    category.contains("hindi", ignoreCase = true) && category.contains("crime", ignoreCase = true) -> "desi crime indian true crime hindi"
                    category.contains("hindi", ignoreCase = true) -> "the ranveer show hindi audio pitara hindi podcast"
                    category.equals("all", ignoreCase = true) -> "top popular podcast hindi english"
                    category.equals("technology", ignoreCase = true) -> "technology tech podcast"
                    category.equals("business", ignoreCase = true) -> "business finance entrepreneurship podcast"
                    category.equals("comedy", ignoreCase = true) -> "comedy funny podcast"
                    category.equals("true crime", ignoreCase = true) -> "true crime mysteries podcast"
                    category.equals("health", ignoreCase = true) -> "health fitness mental wellbeing podcast"
                    category.equals("science", ignoreCase = true) -> "science discovery podcast"
                    category.equals("news", ignoreCase = true) -> "news current affairs daily podcast"
                    else -> "$category podcast"
                }

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
                        var title = if (obj.has("collectionName")) obj.get("collectionName").asString else "Podcast Show"
                        title = title.replace("?????", "हिंदी")
                        val host = if (obj.has("artistName")) obj.get("artistName").asString else "Host"
                        val genre = if (obj.has("primaryGenreName")) obj.get("primaryGenreName").asString else "Podcasts"
                        var coverUrl = if (obj.has("artworkUrl600")) obj.get("artworkUrl600").asString else ""
                        if (coverUrl.isBlank() && obj.has("artworkUrl100")) {
                            coverUrl = obj.get("artworkUrl100").asString
                        }
                        val trackCount = if (obj.has("trackCount")) obj.get("trackCount").asInt else 25

                        if (id.isNotBlank()) {
                            list.add(
                                PodcastDto(
                                    id = "pod_$id",
                                    title = title,
                                    host = host,
                                    description = "Top show in $genre",
                                    coverUrl = coverUrl,
                                    category = genre,
                                    language = detectLanguage(title + " " + host),
                                    followerCount = "185K",
                                    episodeCount = trackCount,
                                    isFollowed = false
                                )
                            )
                        }
                    }

                    // Guarantee TRS Hindi on Hindi query
                    if (category.contains("hindi", ignoreCase = true) && list.none { it.id.contains("1542452346") }) {
                        list.add(
                            0,
                            PodcastDto(
                                id = "pod_1542452346",
                                title = "The Ranveer Show (TRS हिंदी)",
                                host = "BeerBiceps (Ranveer Allahbadia)",
                                description = "India's biggest Hindi podcast with incredible guests & spiritual wisdom",
                                coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Podcasts126/v4/4a/12/f9/4a12f915-0557-0a2a-281b-5e60d2ecb3fb/mza_16382103562699898858.jpg/600x600bb.jpg",
                                category = "Society & Culture",
                                language = "Hindi",
                                followerCount = "2.4M",
                                episodeCount = 320,
                                isFollowed = true
                            )
                        )
                    }

                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPodcastEpisodes(podcastId: String, limit: Int = 30): Result<PodcastDetailResponse> =
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
                            var title = if (obj.has("collectionName")) obj.get("collectionName").asString else "Podcast Show"
                            title = title.replace("?????", "हिंदी")
                            val host = if (obj.has("artistName")) obj.get("artistName").asString else "Host"
                            val genre = if (obj.has("primaryGenreName")) obj.get("primaryGenreName").asString else "Podcasts"
                            var coverUrl = if (obj.has("artworkUrl600")) obj.get("artworkUrl600").asString else ""
                            if (coverUrl.isBlank() && obj.has("artworkUrl100")) {
                                coverUrl = obj.get("artworkUrl100").asString
                            }
                            val trackCount = if (obj.has("trackCount")) obj.get("trackCount").asInt else 30
                            show = PodcastDto(id, title, host, "Featured Show in $genre", coverUrl, genre, detectLanguage(title + " " + host), "250K", trackCount, false)
                        } else if (wrapper == "podcastEpisode") {
                            val epId = "ep_" + if (obj.has("trackId")) obj.get("trackId").asString else "${System.currentTimeMillis()}"
                            val epTitle = if (obj.has("trackName")) obj.get("trackName").asString else "Episode"
                            val epDesc = if (obj.has("description")) obj.get("description").asString else ""
                            val durationMs = if (obj.has("trackTimeMillis")) obj.get("trackTimeMillis").asLong else 1800000L
                            val mins = durationMs / (1000 * 60)
                            val durLabel = if (mins > 60) "${mins / 60}h ${mins % 60}m" else "$mins min"
                            val audioUrl = if (obj.has("episodeUrl")) obj.get("episodeUrl").asString else ""
                            val epNum = if (obj.has("trackNumber")) obj.get("trackNumber").asInt else episodes.size + 1
                            var pubDate = if (obj.has("releaseDate")) obj.get("releaseDate").asString else "Recently added"
                            if (pubDate.length >= 10) pubDate = pubDate.substring(0, 10)
                            var epCover = if (obj.has("artworkUrl600")) obj.get("artworkUrl600").asString else ""
                            if (epCover.isBlank() && obj.has("artworkUrl160")) {
                                epCover = obj.get("artworkUrl160").asString
                            }

                            val chapters = generateSampleChapters(durationMs)

                            if (audioUrl.isNotBlank()) {
                                episodes.add(
                                    PodcastEpisodeDto(
                                        id = epId,
                                        podcastId = "pod_$rawId",
                                        title = epTitle,
                                        description = epDesc,
                                        durationLabel = durLabel,
                                        durationMs = durationMs,
                                        audioUrl = audioUrl,
                                        coverUrl = epCover,
                                        episodeNumber = epNum,
                                        publishedAt = pubDate,
                                        progressPercent = 0,
                                        isPlayed = false,
                                        isDownloaded = false,
                                        chapters = chapters
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

    private fun detectLanguage(text: String): String {
        val lower = text.lowercase()
        if (lower.contains("hindi") || lower.contains("kahani") || lower.contains("bharat") || lower.contains("desi") || lower.contains("ranveer")) return "Hindi"
        if (lower.contains("tamil") || lower.contains("chennai")) return "Tamil"
        if (lower.contains("telugu") || lower.contains("hyderabad")) return "Telugu"
        if (lower.contains("punjabi") || lower.contains("moose")) return "Punjabi"
        if (lower.contains("marathi") || lower.contains("pune") || lower.contains("mumbai")) return "Marathi"
        if (lower.contains("bengali") || lower.contains("kolkata")) return "Bengali"
        if (lower.contains("spanish") || lower.contains("espanol")) return "Spanish"
        if (lower.contains("deutsch") || lower.contains("german")) return "German"
        if (lower.contains("japanese")) return "Japanese"
        return "English"
    }

    private fun generateSampleChapters(durationMs: Long): List<PodcastChapterDto> {
        val totalSec = durationMs / 1000
        val effective = if (totalSec <= 120) 1800 else totalSec

        val c1 = 0L
        val c2 = (effective / 5).coerceAtLeast(180L)
        val c3 = (effective / 2).coerceAtLeast(600L)
        val c4 = ((effective * 3) / 4).coerceAtLeast(1200L)

        return listOf(
            PodcastChapterDto("00:00 Introduction & Context", c1, c2),
            PodcastChapterDto("04:00 Deep Dive & Core Discussion", c2, c3),
            PodcastChapterDto("12:00 Guest Insights & Stories", c3, c4),
            PodcastChapterDto("22:00 Key Takeaways & Conclusion", c4, effective)
        )
    }
}
