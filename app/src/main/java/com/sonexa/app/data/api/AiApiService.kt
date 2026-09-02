package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AiApiService {
    @POST("ai/signature")
    suspend fun generateAiSignature(@Body request: AiSignatureRequest): Response<AiSignatureResponse>

    @POST("ai/chat")
    suspend fun chat(@Body request: AiChatRequest): Response<AiChatResponse>

    @POST("ai/intent")
    suspend fun parseIntent(@Body request: IntentParseRequestDto): Response<ApiEnvelope<MusicIntentDto>>

    @POST("ai/change-vibe")
    suspend fun changeVibe(@Body request: ChangeVibeRequestDto): Response<ApiEnvelope<ChangeVibeResponseDto>>

    @POST("ai/fix-queue")
    suspend fun fixQueue(@Body request: FixQueueRequestDto): Response<ApiEnvelope<FixQueueResponseDto>>

    @POST("ai/music-journey")
    suspend fun createJourney(
        @Query("userKey") userKey: String = "guest_user",
        @Query("theme") theme: String = "CALM_TO_ENERGETIC",
        @Query("duration") duration: Int = 60
    ): Response<ApiEnvelope<MusicJourneyResponseDto>>

    @POST("ai/dj/next")
    suspend fun djNext(
        @Query("userKey") userKey: String = "guest_user",
        @Body currentTrack: TrackDto? = null
    ): Response<ApiEnvelope<NextTrackDecisionDto>>

    @POST("ai/playlist")
    suspend fun generateAiPlaylist(@Body request: IntentParseRequestDto): Response<ApiEnvelope<List<TrackDto>>>

    @POST("voice/search")
    suspend fun voiceSearch(@Body request: VoiceSearchRequestDto): Response<ApiEnvelope<VoiceSearchResponseDto>>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Query("userKey") userKey: String = "guest_user",
        @Query("limit") limit: Int = 20
    ): Response<ApiEnvelope<List<TrackDto>>>

    @GET("recommendations/daily-mix")
    suspend fun getDailyMix(@Query("userKey") userKey: String = "guest_user"): Response<ApiEnvelope<List<TrackDto>>>

    @GET("recommendations/surprise")
    suspend fun getSurprise(@Query("userKey") userKey: String = "guest_user"): Response<ApiEnvelope<List<TrackDto>>>

    @GET("recommendations/predictions")
    suspend fun getPredictions(@Query("userKey") userKey: String = "guest_user"): Response<ApiEnvelope<List<PredictionItemDto>>>

    @GET("recommendations/why/{trackId}")
    suspend fun getWhyThisSong(
        @Path("trackId") trackId: String,
        @Query("userKey") userKey: String = "guest_user"
    ): Response<ApiEnvelope<WhyThisSongResponseDto>>

    @GET("recommendations/mood")
    suspend fun getMoodSession(
        @Query("userKey") userKey: String = "guest_user",
        @Query("mood") mood: String = "Chill",
        @Query("energy") energy: Double? = null
    ): Response<ApiEnvelope<List<TrackDto>>>

    @GET("recommendations/energy")
    suspend fun getEnergySession(
        @Query("userKey") userKey: String = "guest_user",
        @Query("level") level: Double = 0.5
    ): Response<ApiEnvelope<List<TrackDto>>>

    @GET("me/music-dna")
    suspend fun getMusicDna(@Query("userKey") userKey: String = "guest_user"): Response<ApiEnvelope<MusicDnaResponseDto>>

    @GET("me/listening-insights")
    suspend fun getListeningInsights(@Query("userKey") userKey: String = "guest_user"): Response<ApiEnvelope<ListeningInsightsResponseDto>>

    @POST("events")
    suspend fun recordEvent(@Body event: UserEventRequestDto): Response<ApiEnvelope<String>>
}

data class ApiEnvelope<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null
)
