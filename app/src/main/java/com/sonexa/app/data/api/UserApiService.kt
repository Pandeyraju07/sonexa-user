package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfileApiResponse>

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<SimpleSuccessResponse>

    @GET("user/library")
    suspend fun getUserLibrary(): Response<UserLibraryResponse>

    @POST("user/like")
    suspend fun toggleLikeSong(@Body request: ToggleLikeRequest): Response<ToggleLikeResponse>

    @GET("user/notifications")
    suspend fun getNotifications(): Response<NotificationListResponse>

    @GET("user/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("user/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<SimpleSuccessResponse>

    @GET("user/premium")
    suspend fun getPremium(): Response<PremiumResponse>

    @POST("user/premium/subscribe")
    suspend fun subscribe(@Body request: SubscribeRequest): Response<SimpleSuccessResponse>
}
