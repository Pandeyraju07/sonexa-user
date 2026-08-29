package com.sonexa.app.data.repository

import com.sonexa.app.data.api.AiApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(private val apiService: AiApiService = RetrofitClient.aiApiService) {

    private val aggregationEngine = com.sonexa.app.data.provider.MusicAggregationEngine()

    suspend fun generateAiSignature(
        mood: String,
        prompt: String = "",
        detectedEmotion: String = ""
    ): Result<AiSignatureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.generateAiSignature(
                AiSignatureRequest(mood = mood, prompt = prompt, detectedEmotion = detectedEmotion)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                // If recommended tracks need multi-provider enrichment
                val enrichedTracks = if (body.recommendedTracks.isEmpty() && prompt.isNotBlank()) {
                    aggregationEngine.searchAll(prompt).tracks.take(5)
                } else {
                    body.recommendedTracks
                }
                Result.success(body.copy(recommendedTracks = enrichedTracks))
            } else {
                // Fallback to aggregation engine discovery based on prompt/mood
                val query = prompt.ifBlank { mood.ifBlank { "Chill Vibes" } }
                val discovered = aggregationEngine.searchAll(query).tracks.take(6)
                Result.success(
                    AiSignatureResponse(
                        success = true,
                        signatureId = "ai_${System.currentTimeMillis()}",
                        vibeTitle = "AI Discovery: $query",
                        aiGeneratedAudioUrl = "",
                        bpm = 115,
                        key = "A Minor",
                        recommendedTracks = discovered
                    )
                )
            }
        } catch (e: Exception) {
            // Graceful fallback to multi-provider discovery
            val query = prompt.ifBlank { mood.ifBlank { "Top Hits" } }
            val discovered = aggregationEngine.searchAll(query).tracks.take(6)
            Result.success(
                AiSignatureResponse(
                    success = true,
                    signatureId = "ai_fallback_${System.currentTimeMillis()}",
                    vibeTitle = "AI Discovery: $query",
                    bpm = 120,
                    key = "C Major",
                    recommendedTracks = discovered
                )
            )
        }
    }

    suspend fun chat(message: String): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.chat(AiChatRequest(message))
            val body = response.body()
            if (response.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception("AI chat failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
