package com.sonexa.app.data.repository

import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.api.UserApiService
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val apiService: UserApiService = RetrofitClient.userApiService) {

    suspend fun getUserProfile(): Result<UserProfileApiResponse> = apiCall { apiService.getUserProfile() }

    suspend fun updateProfile(name: String? = null, bio: String? = null): Result<SimpleSuccessResponse> =
        apiCall {
            val body = mutableMapOf<String, String>()
            name?.let { body["name"] = it }
            bio?.let { body["bio"] = it }
            apiService.updateProfile(body)
        }

    suspend fun getUserLibrary(): Result<UserLibraryResponse> = apiCall { apiService.getUserLibrary() }

    suspend fun getUserPlaylists(): Result<UserPlaylistsResponse> = apiCall { apiService.getUserPlaylists() }

    suspend fun createPlaylist(title: String, description: String = "", coverUrl: String = ""): Result<PlaylistDto> =
        apiCall { apiService.createPlaylist(CreatePlaylistRequest(title, description, coverUrl)) }

    suspend fun updatePlaylist(id: String, request: UpdatePlaylistRequest): Result<PlaylistDto> =
        apiCall { apiService.updatePlaylist(id, request) }

    suspend fun deletePlaylist(id: String): Result<SimpleSuccessResponse> =
        apiCall { apiService.deletePlaylist(id) }

    suspend fun addTrackToPlaylist(id: String, track: TrackDto): Result<SimpleSuccessResponse> =
        apiCall {
            apiService.addTrackToPlaylist(
                id,
                AddTrackToPlaylistRequest(
                    trackId = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    audioUrl = track.audioUrl,
                    coverUrl = track.effectiveCoverUrl
                )
            )
        }

    suspend fun removeTrackFromPlaylist(id: String, trackId: String): Result<SimpleSuccessResponse> =
        apiCall { apiService.removeTrackFromPlaylist(id, trackId) }

    suspend fun toggleLikeSong(trackId: String): Result<ToggleLikeResponse> =
        apiCall { apiService.toggleLikeSong(ToggleLikeRequest(trackId)) }

    suspend fun getNotifications(): Result<NotificationListResponse> = apiCall { apiService.getNotifications() }

    suspend fun markAllNotificationsRead(): Result<SimpleSuccessResponse> =
        apiCall { apiService.markAllNotificationsRead() }

    suspend fun markNotificationRead(id: String): Result<SimpleSuccessResponse> =
        apiCall { apiService.markNotificationRead(id) }

    suspend fun getActiveSessions(): Result<ActiveSessionsResponse> =
        apiCall { apiService.getActiveSessions() }

    suspend fun revokeSession(sessionId: String): Result<SimpleSuccessResponse> =
        apiCall { apiService.revokeSession(sessionId) }

    suspend fun deleteAccount(): Result<SimpleSuccessResponse> =
        apiCall { apiService.deleteAccount() }

    suspend fun getSettings(): Result<SettingsResponse> = apiCall { apiService.getSettings() }

    suspend fun updateSettings(settings: Map<String, Any?>): Result<SimpleSuccessResponse> =
        apiCall { apiService.updateSettings(UpdateSettingsRequest(settings)) }

    suspend fun getPremium(): Result<PremiumResponse> = apiCall { apiService.getPremium() }

    suspend fun subscribe(planId: String): Result<SimpleSuccessResponse> =
        apiCall { apiService.subscribe(SubscribeRequest(planId)) }

    suspend fun redeemCoupon(code: String): Result<RedeemCouponResponse> =
        apiCall { apiService.redeemCoupon(RedeemCouponRequest(code)) }

    suspend fun cancelPremium(): Result<SimpleSuccessResponse> =
        apiCall { apiService.cancelPremium() }

    private suspend fun <T> apiCall(block: suspend () -> retrofit2.Response<T>): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                val response = block()
                val body = response.body()
                if (response.isSuccessful && body != null) Result.success(body)
                else Result.failure(Exception("Request failed (${response.code()})"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
