package com.sonexa.app.data.model

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("album") val album: String = "",
    @SerializedName("durationMs") val durationMs: Long = 0,
    @SerializedName("audioUrl") val audioUrl: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("playsCount") val playsCount: String = "",
    @SerializedName("isLiked") val isLiked: Boolean = false,
    @SerializedName("provider") val provider: String = "sonexa",
    @SerializedName("providerTrackId") val providerTrackId: String = "",
    @SerializedName("videoId") val videoId: String = "",
    @SerializedName("providerUrl") val providerUrl: String = "",
    @SerializedName("isPlayable") val isPlayable: Boolean = true,
    @SerializedName("providerType") val providerType: String = "audio",
    @SerializedName("availability") val availability: String = "AVAILABLE",
    @SerializedName("availableProviders") val availableProviders: List<String> = emptyList(),
    @SerializedName("channelTitle") val channelTitle: String = "",
    @SerializedName("isOfficial") val isOfficial: Boolean = false
) {
    val isYouTube: Boolean
        get() = provider?.equals("youtube", ignoreCase = true) == true ||
                providerType?.equals("youtube_video", ignoreCase = true) == true ||
                !videoId.isNullOrBlank() ||
                (!providerTrackId.isNullOrBlank() && provider?.equals("youtube", ignoreCase = true) == true)

    val effectiveVideoId: String
        get() {
            val vid = videoId.orEmpty()
            val prov = provider.orEmpty()
            val pid = providerTrackId.orEmpty()
            return if (vid.isNotBlank()) vid else if (prov.equals("youtube", ignoreCase = true)) pid else ""
        }

    val effectiveCoverUrl: String
        get() {
            val cover = coverUrl.orEmpty()
            if (cover.isNotBlank()) return cover
            val vid = effectiveVideoId
            return if (vid.isNotBlank()) "https://img.youtube.com/vi/$vid/hqdefault.jpg" else ""
        }
}

data class AlbumDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("year") val year: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("trackCount") val trackCount: Int = 0
)

data class PlaylistDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("artworkType") val artworkType: String = "",
    @SerializedName("coverUrl") val coverUrl: String = ""
)

data class ArtistDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("genre") val genre: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("color1") val color1: String = "#6B3CE9",
    @SerializedName("color2") val color2: String = "#9825DD",
    @SerializedName("followersCount") val followersCount: Int = 0,
    @SerializedName("verified") val verified: Boolean = false
)

data class GenreDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("color1") val color1: String = "#6B3CE9",
    @SerializedName("color2") val color2: String = "#9825DD",
    @SerializedName("imageUrl") val imageUrl: String = ""
)

data class MoodDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("iconKey") val iconKey: String = "",
    @SerializedName("colorHex") val colorHex: String = "#8B5CF6"
)

data class PodcastChapterDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("startTimeSeconds") val startTimeSeconds: Long = 0,
    @SerializedName("endTimeSeconds") val endTimeSeconds: Long = 0
)

data class PodcastLanguageDto(
    @SerializedName("code") val code: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("nativeName") val nativeName: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("showCount") val showCount: Int = 0
)

data class PodcastCategoryDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("icon") val icon: String = "🎙️",
    @SerializedName("colorHex") val colorHex: String = "#7C3AED",
    @SerializedName("gradientFrom") val gradientFrom: String = "#2E1065",
    @SerializedName("gradientTo") val gradientTo: String = "#0F172A"
)

data class PodcastDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("host") val host: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("category") val category: String = "General",
    @SerializedName("language") val language: String = "Hindi",
    @SerializedName("followerCount") val followerCount: String = "150K",
    @SerializedName("episodeCount") val episodeCount: Int = 25,
    @SerializedName("isFollowed") val isFollowed: Boolean = false
)

data class PodcastEpisodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("podcastId") val podcastId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("durationLabel") val durationLabel: String = "30 min",
    @SerializedName("durationMs") val durationMs: Long = 1800000L,
    @SerializedName("audioUrl") val audioUrl: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("episodeNumber") val episodeNumber: Int = 1,
    @SerializedName("publishedAt") val publishedAt: String = "Recently added",
    @SerializedName("progressPercent") val progressPercent: Int = 0,
    @SerializedName("isPlayed") val isPlayed: Boolean = false,
    @SerializedName("isDownloaded") val isDownloaded: Boolean = false,
    @SerializedName("chapters") val chapters: List<PodcastChapterDto> = emptyList()
)

data class PodcastHomeResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("continueListening") val continueListening: List<PodcastEpisodeDto> = emptyList(),
    @SerializedName("languages") val languages: List<PodcastLanguageDto> = emptyList(),
    @SerializedName("trendingPodcasts") val trendingPodcasts: List<PodcastDto> = emptyList(),
    @SerializedName("madeForYou") val madeForYou: List<PodcastDto> = emptyList(),
    @SerializedName("popularShows") val popularShows: List<PodcastDto> = emptyList(),
    @SerializedName("categories") val categories: List<PodcastCategoryDto> = emptyList()
)

data class NotificationDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("iconKey") val iconKey: String = "",
    @SerializedName("colorHex") val colorHex: String = "#E534B2",
    @SerializedName("timeAgo") val timeAgo: String = "",
    @SerializedName("read") val read: Boolean = false
)

data class HomeDynamicSectionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("sectionKey") val sectionKey: String = "",
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String = "TRACK_CAROUSEL", // QUICK_ACCESS, TRACK_CAROUSEL, PLAYLIST_CAROUSEL, ARTIST_CAROUSEL, MOOD_GRID, CONTINUE_LISTENING
    @SerializedName("position") val position: Int = 0,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList(),
    @SerializedName("playlists") val playlists: List<PlaylistDto> = emptyList(),
    @SerializedName("artists") val artists: List<ArtistDto> = emptyList()
)

data class HomeDynamicFeedResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("greeting") val greeting: String = "",
    @SerializedName("sections") val sections: List<HomeDynamicSectionDto> = emptyList()
)

data class HomeFeedResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("continueListening") val continueListening: List<TrackDto> = emptyList(),
    @SerializedName("trendingNow") val trendingNow: List<TrackDto> = emptyList(),
    @SerializedName("popularAlbums") val popularAlbums: List<AlbumDto> = emptyList(),
    @SerializedName("madeForYou") val madeForYou: List<PlaylistDto> = emptyList(),
    @SerializedName("sections") val sections: List<HomeDynamicSectionDto> = emptyList()
)

data class TrendingResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class SearchResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList(),
    @SerializedName("albums") val albums: List<AlbumDto> = emptyList(),
    @SerializedName("artists") val artists: List<String> = emptyList()
)

data class TrackDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("track") val track: TrackDto? = null
)

data class AlbumDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("album") val album: AlbumDto? = null,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class PlaylistDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("playlist") val playlist: PlaylistDto? = null,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class ArtistDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("artist") val artist: ArtistDto? = null,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList(),
    @SerializedName("albums") val albums: List<AlbumDto> = emptyList()
)

data class QueueResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("nowPlaying") val nowPlaying: TrackDto? = null,
    @SerializedName("queue") val queue: List<TrackDto> = emptyList(),
    @SerializedName("sourceTitle") val sourceTitle: String = ""
)

data class LyricsLineDto(
    @SerializedName("tMs") val tMs: Long = 0,
    @SerializedName("text") val text: String = ""
)

data class LyricsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("trackId") val trackId: String = "",
    @SerializedName("synced") val synced: Boolean = false,
    @SerializedName("lines") val lines: List<LyricsLineDto> = emptyList(),
    @SerializedName("plainText") val plainText: String = "",
    @SerializedName("source") val source: String = ""
)

data class GenreListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("genres") val genres: List<GenreDto> = emptyList()
)

data class ArtistListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("artists") val artists: List<ArtistDto> = emptyList()
)

data class MoodListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("moods") val moods: List<MoodDto> = emptyList()
)

data class PodcastListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("podcasts") val podcasts: List<PodcastDto> = emptyList()
)

data class PodcastDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("podcast") val podcast: PodcastDto? = null,
    @SerializedName("episodes") val episodes: List<PodcastEpisodeDto> = emptyList()
)

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("notifications") val notifications: List<NotificationDto> = emptyList()
)

data class UserProfileApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user") val user: UserProfileDto? = null
)

data class UserLibraryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("likedSongs") val likedSongs: List<TrackDto> = emptyList(),
    @SerializedName("savedAlbums") val savedAlbums: List<AlbumDto> = emptyList()
)

data class ToggleLikeRequest(
    @SerializedName("trackId") val trackId: String
)

data class ToggleLikeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("trackId") val trackId: String = "",
    @SerializedName("isLiked") val isLiked: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class SimpleSuccessResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class SaveListRequest(
    @SerializedName("items") val items: List<String> = emptyList(),
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("artists") val artists: List<String>? = null,
    @SerializedName("moods") val moods: List<String>? = null
)

data class SaveListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("items") val items: List<String> = emptyList(),
    @SerializedName("count") val count: Int = 0
)

data class PremiumResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("plans") val plans: List<PremiumPlanDto> = emptyList(),
    @SerializedName("benefits") val benefits: List<String> = emptyList()
)

data class PremiumPlanDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: String = "",
    @SerializedName("description") val description: String = ""
)

data class SubscribeRequest(
    @SerializedName("planId") val planId: String
)

data class SettingsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("settings") val settings: Map<String, Any?> = emptyMap()
)

data class UpdateSettingsRequest(
    @SerializedName("settings") val settings: Map<String, Any?>
)

data class AppUpdateResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("updateAvailable") val updateAvailable: Boolean = false,
    @SerializedName("forceUpdate") val forceUpdate: Boolean = false,
    @SerializedName("latestVersion") val latestVersion: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("storeUrl") val storeUrl: String = ""
)

data class PermissionsConfigResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("notifications") val notifications: Map<String, Any?> = emptyMap(),
    @SerializedName("downloads") val downloads: Map<String, Any?> = emptyMap()
)

data class AiSignatureRequest(
    @SerializedName("mood") val mood: String = "",
    @SerializedName("prompt") val prompt: String = "",
    @SerializedName("detectedEmotion") val detectedEmotion: String = ""
)

data class AiSignatureResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("signatureId") val signatureId: String = "",
    @SerializedName("vibeTitle") val vibeTitle: String = "",
    @SerializedName("aiGeneratedAudioUrl") val aiGeneratedAudioUrl: String = "",
    @SerializedName("bpm") val bpm: Int = 120,
    @SerializedName("key") val key: String = "C Major",
    @SerializedName("recommendedTracks") val recommendedTracks: List<TrackDto> = emptyList()
)

data class AiChatRequest(
    @SerializedName("message") val message: String = ""
)

data class AiChatResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("reply") val reply: String = ""
)
