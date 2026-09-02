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

    @GET("user/playlists")
    suspend fun getUserPlaylists(): Response<UserPlaylistsResponse>

    @POST("user/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): Response<PlaylistDto>

    @retrofit2.http.PUT("user/playlists/{id}")
    suspend fun updatePlaylist(
        @retrofit2.http.Path("id") id: String,
        @Body request: UpdatePlaylistRequest
    ): Response<PlaylistDto>

    @retrofit2.http.DELETE("user/playlists/{id}")
    suspend fun deletePlaylist(@retrofit2.http.Path("id") id: String): Response<SimpleSuccessResponse>

    @POST("user/playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @retrofit2.http.Path("id") id: String,
        @Body request: AddTrackToPlaylistRequest
    ): Response<SimpleSuccessResponse>

    @retrofit2.http.DELETE("user/playlists/{id}/tracks/{trackId}")
    suspend fun removeTrackFromPlaylist(
        @retrofit2.http.Path("id") id: String,
        @retrofit2.http.Path("trackId") trackId: String
    ): Response<SimpleSuccessResponse>

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

    @POST("user/premium/redeem")
    suspend fun redeemCoupon(@Body request: RedeemCouponRequest): Response<RedeemCouponResponse>

    @POST("user/premium/cancel")
    suspend fun cancelPremium(): Response<SimpleSuccessResponse>
}
