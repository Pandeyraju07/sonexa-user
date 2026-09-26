package com.sonexa.app.data.repository

import com.sonexa.app.data.api.RecommendationApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationRepository(
    private val api: RecommendationApiService = RetrofitClient.recommendationApiService
) {

    suspend fun getForYou(
        userId: String? = null,
        mood: String? = null,
        genre: String? = null,
        limit: Int = 20
    ): Result<List<RecommendationItemDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getForYouRecommendations(userId, mood, genre, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch recommendations: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDynamicQueue(
        currentSongId: String,
        userId: String? = null,
        sessionId: String? = null,
        skippedGenres: List<String> = emptyList(),
        limit: Int = 15
    ): Result<DynamicQueueDataDto> = withContext(Dispatchers.IO) {
        try {
            val skippedStr = if (skippedGenres.isNotEmpty()) skippedGenres.joinToString(",") else null
            val response = api.getDynamicQueue(currentSongId, userId, sessionId, skippedStr, limit)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception("Dynamic queue error: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAutoplay(
        currentSongId: String,
        userId: String? = null,
        sessionId: String? = null,
        limit: Int = 5
    ): Result<AutoplayResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAutoplay(currentSongId, userId, sessionId, limit)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Autoplay error: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSimilarSongs(songId: String, limit: Int = 10): Result<List<RecommendationItemDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getSimilarSongs(songId, limit)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception("Similar songs error: HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun trackPlaybackEvent(event: PlaybackEventRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.trackPlaybackEvent(event)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Event tracking failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            // Silently fail without interrupting user playback
            Result.failure(e)
        }
    }
}
