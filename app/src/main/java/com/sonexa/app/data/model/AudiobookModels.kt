package com.sonexa.app.data.model

import com.google.gson.annotations.SerializedName

data class AudiobookChapterDto(
    @SerializedName("id") val id: String,
    @SerializedName("audiobookId") val audiobookId: String,
    @SerializedName("chapterNumber") val chapterNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("durationSec") val durationSec: Long,
    @SerializedName("streamUrl") val streamUrl: String,
    @SerializedName("summary") val summary: String? = null
) {
    val durationMs: Long
        get() = durationSec * 1000L

    val formattedDuration: String
        get() {
            val mins = durationSec / 60
            val secs = durationSec % 60
            return if (mins >= 60) {
                val hours = mins / 60
                val remMins = mins % 60
                "${hours}h ${remMins}m"
            } else {
                "${mins}m ${secs}s"
            }
        }
}

data class AudiobookDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("author") val author: String,
    @SerializedName("narrator") val narrator: String,
    @SerializedName("languageCode") val languageCode: String,
    @SerializedName("languageName") val languageName: String,
    @SerializedName("category") val category: String,
    @SerializedName("categories") val categories: List<String> = emptyList(),
    @SerializedName("coverUrl") val coverUrl: String,
    @SerializedName("totalDurationSec") val totalDurationSec: Long,
    @SerializedName("chapterCount") val chapterCount: Int,
    @SerializedName("isClassic") val isClassic: Boolean = false,
    @SerializedName("isPublicDomain") val isPublicDomain: Boolean = false,
    @SerializedName("rating") val rating: Float = 4.8f,
    @SerializedName("reviewsCount") val reviewsCount: Int = 0,
    @SerializedName("publicationYear") val publicationYear: Int = 0,
    @SerializedName("description") val description: String = "",
    @SerializedName("chapters") val chapters: List<AudiobookChapterDto> = emptyList(),
    @SerializedName("isSaved") val isSaved: Boolean = false
) {
    val totalDurationMs: Long
        get() = totalDurationSec * 1000L

    val formattedDuration: String
        get() {
            val hours = totalDurationSec / 3600
            val mins = (totalDurationSec % 3600) / 60
            return if (hours > 0) "${hours} hr ${mins} min" else "${mins} min"
        }

    /**
     * Converts a specific audiobook chapter into a TrackDto
     * so that AndroidX Media3 ExoPlayer, Notification, and Lock-Screen controls
     * work seamlessly with zero duplicated audio engines.
     */
    fun toTrackDto(chapterIndex: Int = 0): TrackDto {
        val chapter = chapters.getOrNull(chapterIndex)
            ?: AudiobookChapterDto(
                id = "${id}_ch1",
                audiobookId = id,
                chapterNumber = 1,
                title = "Chapter 1",
                durationSec = totalDurationSec / chapterCount.coerceAtLeast(1),
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            )

        return TrackDto(
            id = chapter.id,
            title = chapter.title,
            artist = "$author • Narrated by $narrator",
            album = title,
            coverUrl = coverUrl,
            audioUrl = chapter.streamUrl,
            durationMs = chapter.durationMs,
            provider = "audiobook",
            providerType = "audiobook_chapter",
            genres = listOf(category),
            language = languageName
        )
    }
}

data class AudiobookProgressDto(
    @SerializedName("audiobookId") val audiobookId: String,
    @SerializedName("currentChapterId") val currentChapterId: String = "",
    @SerializedName("currentChapterNumber") val currentChapterNumber: Int = 1,
    @SerializedName("chapterTitle") val chapterTitle: String = "",
    @SerializedName("positionSeconds") val positionSeconds: Long = 0L,
    @SerializedName("totalListenedSeconds") val totalListenedSeconds: Long = 0L,
    @SerializedName("completionPercentage") val completionPercentage: Int = 0,
    @SerializedName("lastListenedAt") val lastListenedAt: String = ""
) {
    val positionMs: Long
        get() = positionSeconds * 1000L

    val formattedPosition: String
        get() {
            val m = positionSeconds / 60
            val s = positionSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

data class AudiobookBookmarkDto(
    @SerializedName("id") val id: String,
    @SerializedName("audiobookId") val audiobookId: String,
    @SerializedName("chapterId") val chapterId: String = "",
    @SerializedName("chapterNumber") val chapterNumber: Int = 1,
    @SerializedName("positionSeconds") val positionSeconds: Long = 0L,
    @SerializedName("note") val note: String = "",
    @SerializedName("createdAt") val createdAt: String = ""
) {
    val positionMs: Long
        get() = positionSeconds * 1000L

    val formattedPosition: String
        get() {
            val m = positionSeconds / 60
            val s = positionSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

data class AudiobookCategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("bookCount") val bookCount: Int = 0
)

data class AudiobookLanguageDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("nativeName") val nativeName: String,
    @SerializedName("bookCount") val bookCount: Int = 0
)

data class AudiobookDetailResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: AudiobookDto? = null,
    @SerializedName("progress") val progress: AudiobookProgressDto? = null,
    @SerializedName("bookmarks") val bookmarks: List<AudiobookBookmarkDto> = emptyList(),
    @SerializedName("related") val related: List<AudiobookDto> = emptyList()
)

data class AudiobookListResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: List<AudiobookDto> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)
