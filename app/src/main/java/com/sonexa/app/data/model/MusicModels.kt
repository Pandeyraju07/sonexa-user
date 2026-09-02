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
    @SerializedName("isOfficial") val isOfficial: Boolean = false,
    @SerializedName("bpm") val bpm: Double = 110.0,
    @SerializedName("energy") val energy: Double = 0.55,
    @SerializedName("mood") val mood: String = "Chill",
    @SerializedName("moods") val moods: List<String> = emptyList(),
    @SerializedName("genres") val genres: List<String> = emptyList(),
    @SerializedName("language") val language: String = "Hindi",
    @SerializedName("eraDecade") val eraDecade: String = "2020s",
    @SerializedName("acousticness") val acousticness: Double = 0.45,
    @SerializedName("danceability") val danceability: Double = 0.60,
    @SerializedName("isInstrumental") val isInstrumental: Boolean = false,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("versionType") val versionType: String = "Original",
    @SerializedName("recommendationReason") val recommendationReason: String = "",
    @SerializedName("qualityTier") val qualityTier: String = "EXACT_MATCH"
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

    val genre: String
        get() = genres.firstOrNull()?.ifBlank { album } ?: album
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
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("trackCount") val trackCount: Int = 0,
    @SerializedName("creatorName") val creatorName: String = "Sonexa",
    @SerializedName("isUserCreated") val isUserCreated: Boolean = false,
    @SerializedName("isPinned") val isPinned: Boolean = false
)

data class CreatePlaylistRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("isPrivate") val isPrivate: Boolean = false
)

data class UpdatePlaylistRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("isPrivate") val isPrivate: Boolean? = null,
    @SerializedName("isPinned") val isPinned: Boolean? = null
)

data class AddTrackToPlaylistRequest(
    @SerializedName("trackId") val trackId: String,
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("album") val album: String = "",
    @SerializedName("durationMs") val durationMs: Long = 0L,
    @SerializedName("audioUrl") val audioUrl: String = "",
    @SerializedName("coverUrl") val coverUrl: String = ""
)

data class UserPlaylistsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("playlists") val playlists: List<PlaylistDto> = emptyList()
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
    @SerializedName("read") val read: Boolean = false,
    @SerializedName("category") val category: String = "music" // music, social, ai, system
)

data class ActiveSessionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("device") val device: String = "Unknown Device",
    @SerializedName("location") val location: String = "",
    @SerializedName("lastActive") val lastActive: String = "",
    @SerializedName("isCurrent") val isCurrent: Boolean = false,
    @SerializedName("platform") val platform: String = "android"
)

data class ActiveSessionsResponse(
    @SerializedName("sessions") val sessions: List<ActiveSessionDto> = emptyList()
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
    @SerializedName("playlists") val playlists: List<PlaylistDto> = emptyList(),
    @SerializedName("likedSongs") val likedSongs: List<TrackDto> = emptyList(),
    @SerializedName("likedCount") val likedCount: Int = 0,
    @SerializedName("savedAlbums") val savedAlbums: List<AlbumDto> = emptyList(),
    @SerializedName("followedArtists") val followedArtists: List<ArtistDto> = emptyList(),
    @SerializedName("recentHistory") val recentHistory: List<TrackDto> = emptyList()
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
    @SerializedName("period") val period: String = "per month",
    @SerializedName("description") val description: String = "",
    @SerializedName("badge") val badge: String = "",
    @SerializedName("color1") val color1: String = "#6B3CE9",
    @SerializedName("color2") val color2: String = "#9825DD",
    @SerializedName("features") val features: List<String> = emptyList()
)

data class SubscribeRequest(
    @SerializedName("planId") val planId: String
)

data class RedeemCouponRequest(
    @SerializedName("code") val code: String
)

data class RedeemCouponResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("planName") val planName: String = ""
)

data class BrowseCategoryDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("colorHex") val colorHex: Long = 0xFF8C67AC,
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("query") val query: String = ""
)

data class DiscoverTagDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("tag") val tag: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("query") val query: String = ""
)

data class SearchCategoriesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("heroCategories") val heroCategories: List<BrowseCategoryDto> = emptyList(),
    @SerializedName("discoverTags") val discoverTags: List<DiscoverTagDto> = emptyList(),
    @SerializedName("browseCategories") val browseCategories: List<BrowseCategoryDto> = emptyList()
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

// LIVE EVENTS MODELS
data class EventSetlistTrackDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("audioUrl") val audioUrl: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("durationLabel") val durationLabel: String = "3:30",
    @SerializedName("durationMs") val durationMs: Long = 210000L
)

data class EventTicketTierDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("perks") val perks: List<String> = emptyList(),
    @SerializedName("isAvailable") val isAvailable: Boolean = true
)

data class LiveEventDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("artistName") val artistName: String = "",
    @SerializedName("artistImageUrl") val artistImageUrl: String = "",
    @SerializedName("bannerUrl") val bannerUrl: String = "",
    @SerializedName("venue") val venue: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("priceStarting") val priceStarting: String = "",
    @SerializedName("status") val status: String = "UPCOMING",
    @SerializedName("category") val category: String = "Stadium Tour",
    @SerializedName("bookingUrl") val bookingUrl: String = "",
    @SerializedName("isReminderSet") val isReminderSet: Boolean = false,
    @SerializedName("lineup") val lineup: List<String> = emptyList(),
    @SerializedName("setlist") val setlist: List<EventSetlistTrackDto> = emptyList()
)

data class LiveEventsFeedResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("title") val title: String = "Live Concerts & Tours",
    @SerializedName("cities") val cities: List<String> = emptyList(),
    @SerializedName("categories") val categories: List<String> = emptyList(),
    @SerializedName("featuredTours") val featuredTours: List<LiveEventDto> = emptyList(),
    @SerializedName("events") val events: List<LiveEventDto> = emptyList()
)

data class LiveEventDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("event") val event: LiveEventDto? = null,
    @SerializedName("ticketTiers") val ticketTiers: List<EventTicketTierDto> = emptyList(),
    @SerializedName("nearbyEvents") val nearbyEvents: List<LiveEventDto> = emptyList()
)

// HOME OF I-POP MODELS
data class IPopArtistDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("followers") val followers: String = "",
    @SerializedName("topSongTitle") val topSongTitle: String = "",
    @SerializedName("isVerified") val isVerified: Boolean = true
)

data class IPopPlaylistDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("coverUrl") val coverUrl: String = "",
    @SerializedName("badge") val badge: String = "🔥 TRENDING",
    @SerializedName("trackCount") val trackCount: Int = 0,
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class IPopHomeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("title") val title: String = "Home of I-Pop",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("spotlightBannerUrl") val spotlightBannerUrl: String = "",
    @SerializedName("spotlightTitle") val spotlightTitle: String = "",
    @SerializedName("spotlightSubtitle") val spotlightSubtitle: String = "",
    @SerializedName("subgenres") val subgenres: List<String> = emptyList(),
    @SerializedName("trendingTracks") val trendingTracks: List<TrackDto> = emptyList(),
    @SerializedName("featuredPlaylists") val featuredPlaylists: List<IPopPlaylistDto> = emptyList(),
    @SerializedName("spotlightArtists") val spotlightArtists: List<IPopArtistDto> = emptyList(),
    @SerializedName("newReleases") val newReleases: List<TrackDto> = emptyList()
)

// ==========================================
// SIGNATURE AI & VOICE MUSIC MODELS
// ==========================================

data class MusicIntentDto(
    @SerializedName("intentType") val intentType: String = "PLAY_MUSIC",
    @SerializedName("query") val query: String = "",
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("track") val track: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("genres") val genres: List<String> = emptyList(),
    @SerializedName("languages") val languages: List<String> = emptyList(),
    @SerializedName("moods") val moods: List<String> = emptyList(),
    @SerializedName("energy") val energy: Double? = null,
    @SerializedName("durationMinutes") val durationMinutes: Int? = null,
    @SerializedName("activity") val activity: String? = null,
    @SerializedName("era") val era: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("confidence") val confidence: Double = 0.90
)

data class IntentParseRequestDto(
    @SerializedName("text") val text: String = "",
    @SerializedName("userKey") val userKey: String = "guest_user",
    @SerializedName("currentTrackId") val currentTrackId: String? = null
)

data class ChangeVibeRequestDto(
    @SerializedName("userKey") val userKey: String = "guest_user",
    @SerializedName("vibe") val vibe: String = "MORE_ENERGETIC",
    @SerializedName("currentQueue") val currentQueue: List<TrackDto> = emptyList(),
    @SerializedName("currentTrack") val currentTrack: TrackDto? = null
)

data class ChangeVibeResponseDto(
    @SerializedName("newVibe") val newVibe: String = "",
    @SerializedName("targetEnergy") val targetEnergy: Double = 0.5,
    @SerializedName("reorderedQueue") val reorderedQueue: List<TrackDto> = emptyList(),
    @SerializedName("explanation") val explanation: String = ""
)

data class FixQueueRequestDto(
    @SerializedName("userKey") val userKey: String = "guest_user",
    @SerializedName("queue") val queue: List<TrackDto> = emptyList()
)

data class FixQueueResponseDto(
    @SerializedName("balancedQueue") val balancedQueue: List<TrackDto> = emptyList(),
    @SerializedName("removedDuplicatesCount") val removedDuplicatesCount: Int = 0,
    @SerializedName("balanceSummary") val balanceSummary: String = ""
)

data class MusicJourneyPhaseItemDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("startMinute") val startMinute: Int = 0,
    @SerializedName("endMinute") val endMinute: Int = 20,
    @SerializedName("targetEnergy") val targetEnergy: Double = 0.5,
    @SerializedName("mood") val mood: String = "Calm",
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class MusicJourneyResponseDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("theme") val theme: String = "",
    @SerializedName("totalDurationMinutes") val totalDurationMinutes: Int = 60,
    @SerializedName("phases") val phases: List<MusicJourneyPhaseItemDto> = emptyList(),
    @SerializedName("allTracks") val allTracks: List<TrackDto> = emptyList()
)

data class MusicDnaResponseDto(
    @SerializedName("personality") val personality: String = "Explorer",
    @SerializedName("energy") val energy: Int = 72,
    @SerializedName("discovery") val discovery: Int = 64,
    @SerializedName("nostalgia") val nostalgia: Int = 81,
    @SerializedName("romance") val romance: Int = 58,
    @SerializedName("mainstream") val mainstream: Int = 42,
    @SerializedName("topGenres") val topGenres: Map<String, Double> = emptyMap(),
    @SerializedName("topLanguages") val topLanguages: Map<String, Double> = emptyMap(),
    @SerializedName("topArtists") val topArtists: Map<String, Double> = emptyMap(),
    @SerializedName("summaryText") val summaryText: String = ""
)

data class ListeningInsightsResponseDto(
    @SerializedName("totalMinutes") val totalMinutes: Int = 3640,
    @SerializedName("topArtists") val topArtists: List<String> = emptyList(),
    @SerializedName("topGenres") val topGenres: List<String> = emptyList(),
    @SerializedName("topLanguages") val topLanguages: List<String> = emptyList(),
    @SerializedName("peakListeningHour") val peakListeningHour: String = "10 PM - 1 AM",
    @SerializedName("skipRate") val skipRate: Double = 0.08,
    @SerializedName("completionRate") val completionRate: Double = 0.92,
    @SerializedName("discoveryRate") val discoveryRate: Double = 0.48,
    @SerializedName("favoriteMood") val favoriteMood: String = "Romantic"
)

data class PredictionItemDto(
    @SerializedName("track") val track: TrackDto = TrackDto(),
    @SerializedName("matchScore") val matchScore: Double = 0.90,
    @SerializedName("reasons") val reasons: List<String> = emptyList()
)

data class WhyThisSongResponseDto(
    @SerializedName("trackId") val trackId: String = "",
    @SerializedName("trackTitle") val trackTitle: String = "",
    @SerializedName("reasons") val reasons: List<String> = emptyList(),
    @SerializedName("affinityScore") val affinityScore: Double = 0.89
)

data class VoiceSearchRequestDto(
    @SerializedName("userKey") val userKey: String = "guest_user",
    @SerializedName("transcript") val transcript: String = "",
    @SerializedName("language") val language: String = "en"
)

data class VoiceSearchResponseDto(
    @SerializedName("transcript") val transcript: String = "",
    @SerializedName("intent") val intent: MusicIntentDto = MusicIntentDto(),
    @SerializedName("feedbackMessage") val feedbackMessage: String = "",
    @SerializedName("tracks") val tracks: List<TrackDto> = emptyList()
)

data class NextTrackDecisionDto(
    @SerializedName("track") val track: TrackDto? = null,
    @SerializedName("reason") val reason: String = "",
    @SerializedName("confidence") val confidence: Double = 0.85
)

data class UserEventRequestDto(
    @SerializedName("userKey") val userKey: String = "guest_user",
    @SerializedName("eventType") val eventType: String = "PLAY_STARTED",
    @SerializedName("trackId") val trackId: String? = null,
    @SerializedName("trackTitle") val trackTitle: String? = null,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("energy") val energy: Double? = null,
    @SerializedName("metadataJson") val metadataJson: String? = null
)

// ==========================================
// 🔍 MULTI-STAGE ARTIST DISCOVERY & INTELLIGENCE
// ==========================================

data class ResolvedArtist(
    val canonicalName: String,
    val canonicalId: String,
    val providerIds: Map<String, String> = emptyMap(),
    val aliases: List<String> = emptyList(),
    val confidence: Double = 1.0,
    val bio: String = "",
    val imageUrl: String = "",
    val genres: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val relatedArtists: List<String> = emptyList(),
    val followersCount: Long = 0,
    val isVerified: Boolean = false
)

data class ArtistAlbumSectionDto(
    val id: String,
    val title: String,
    val year: String,
    val coverUrl: String,
    val trackCount: Int,
    val tracks: List<TrackDto> = emptyList()
)

data class ArtistCatalogResponse(
    val artist: ArtistDto,
    val popularTracks: List<TrackDto> = emptyList(),
    val albums: List<ArtistAlbumSectionDto> = emptyList(),
    val singlesAndEps: List<TrackDto> = emptyList(),
    val collaborations: List<TrackDto> = emptyList(),
    val remixes: List<TrackDto> = emptyList(),
    val relatedArtists: List<ArtistDto> = emptyList(),
    val allTracks: List<TrackDto> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

data class SearchSuggestionDto(
    val title: String,
    val subtitle: String,
    val type: String, // "artist", "track", "album", "genre", "mood", "language"
    val imageUrl: String = "",
    val query: String = title
)

data class TrackUnderstandingProfile(
    val trackId: String,
    val title: String,
    val artist: String,
    val primaryGenre: String,
    val subgenres: List<String> = emptyList(),
    val canonicalMood: String,
    val moods: List<String> = emptyList(),
    val normalizedEnergy: Double, // 0.0 to 1.0
    val language: String,
    val eraDecade: String,
    val tempoBpm: Double,
    val isRomantic: Boolean,
    val acousticness: Double,
    val danceability: Double,
    val isInstrumental: Boolean,
    val tags: List<String> = emptyList(),
    val confidence: Double = 0.90
)

