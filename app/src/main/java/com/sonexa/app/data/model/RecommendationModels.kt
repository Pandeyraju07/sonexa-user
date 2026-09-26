package com.sonexa.app.data.model

import com.google.gson.annotations.SerializedName

data class RecommendationItemDto(
    @SerializedName("song")
    val song: TrackDto,
    @SerializedName("score")
    val score: Float = 0.0f,
    @SerializedName("reason")
    val reason: String = ""
)

data class RecommendationListResponse(
    @SerializedName("success")
    val success: Boolean = true,
    @SerializedName("data")
    val data: List<RecommendationItemDto> = emptyList()
)

data class AutoplayResponseDto(
    @SerializedName("currentSong")
    val currentSong: TrackDto? = null,
    @SerializedName("recommendations")
    val recommendations: List<RecommendationItemDto> = emptyList()
)

data class DynamicQueueDataDto(
    @SerializedName("currentSong")
    val currentSong: TrackDto? = null,
    @SerializedName("queue")
    val queue: List<TrackDto> = emptyList(),
    @SerializedName("adaptedContext")
    val adaptedContext: Map<String, Any>? = null
)

data class DynamicQueueResponseDto(
    @SerializedName("success")
    val success: Boolean = true,
    @SerializedName("data")
    val data: DynamicQueueDataDto? = null
)

data class PlaybackEventRequest(
    @SerializedName("eventId")
    val eventId: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("sessionId")
    val sessionId: String? = null,
    @SerializedName("songId")
    val songId: String? = null,
    @SerializedName("artistId")
    val artistId: String? = null,
    @SerializedName("albumId")
    val albumId: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("position")
    val position: Long = 0L,
    @SerializedName("duration")
    val duration: Long = 0L,
    @SerializedName("eventType")
    val eventType: String = "SONG_PLAY_STARTED",
    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

sealed interface RecommendationUiState {
    object Idle : RecommendationUiState
    object Loading : RecommendationUiState
    data class Success(
        val items: List<RecommendationItemDto>,
        val contextBadge: String = ""
    ) : RecommendationUiState
    data class Error(val message: String) : RecommendationUiState
}
