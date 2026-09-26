package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecommendationApiService {

    @GET("recommendations/home")
    suspend fun getHomeRecommendations(
        @Query("userId") userId: String? = null,
        @Query("mood") mood: String? = null,
        @Query("language") language: String? = null
    ): Response<Map<String, Any>>

    @GET("recommendations/for-you")
    suspend fun getForYouRecommendations(
        @Query("userId") userId: String? = null,
        @Query("mood") mood: String? = null,
        @Query("genre") genre: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<RecommendationListResponse>

    @GET("recommendations/queue")
    suspend fun getDynamicQueue(
        @Query("currentSongId") currentSongId: String,
        @Query("userId") userId: String? = null,
        @Query("sessionId") sessionId: String? = null,
        @Query("skippedGenres") skippedGenres: String? = null,
        @Query("limit") limit: Int = 15
    ): Response<DynamicQueueResponseDto>

    @GET("recommendations/autoplay")
    suspend fun getAutoplay(
        @Query("currentSongId") currentSongId: String,
        @Query("userId") userId: String? = null,
        @Query("sessionId") sessionId: String? = null,
        @Query("limit") limit: Int = 5
    ): Response<AutoplayResponseDto>

    @GET("recommendations/similar/{songId}")
    suspend fun getSimilarSongs(
        @Path("songId") songId: String,
        @Query("limit") limit: Int = 10
    ): Response<RecommendationListResponse>

    @POST("events/playback")
    suspend fun trackPlaybackEvent(
        @Body event: PlaybackEventRequest
    ): Response<Map<String, Any>>
}
