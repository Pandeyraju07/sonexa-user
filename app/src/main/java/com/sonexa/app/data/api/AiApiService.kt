package com.sonexa.app.data.api

import com.sonexa.app.data.model.AiChatRequest
import com.sonexa.app.data.model.AiChatResponse
import com.sonexa.app.data.model.AiSignatureRequest
import com.sonexa.app.data.model.AiSignatureResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("ai/signature")
    suspend fun generateAiSignature(@Body request: AiSignatureRequest): Response<AiSignatureResponse>

    @POST("ai/chat")
    suspend fun chat(@Body request: AiChatRequest): Response<AiChatResponse>
}
