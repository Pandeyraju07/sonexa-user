package com.sonexa.app.data.repository

import com.sonexa.app.data.api.*
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(
    private val apiService: AppConfigApiService = RetrofitClient.appConfigApiService
) {
    suspend fun getSplashConfig(): Result<SplashConfigResponse> = apiCall { apiService.getSplashConfig() }
    suspend fun getOnboardingSlides(): Result<OnboardingResponse> = apiCall { apiService.getOnboardingSlides() }
    suspend fun getLanguages(): Result<LanguagesCatalogResponse> = apiCall { apiService.getLanguages() }
    suspend fun getAppUpdate(): Result<AppUpdateResponse> = apiCall { apiService.getAppUpdate() }
    suspend fun getPermissions(): Result<PermissionsConfigResponse> = apiCall { apiService.getPermissions() }
    suspend fun createProfile(displayName: String, handle: String): Result<SplashConfigResponse> =
        apiCall { apiService.createProfile(ProfileCreateRequest(displayName, handle)) }

    suspend fun getGenres(): Result<GenreListResponse> = apiCall { apiService.getSetupGenres() }
    suspend fun saveGenres(names: List<String>): Result<SaveListResponse> =
        apiCall { apiService.saveGenres(SaveListRequest(items = names, genres = names)) }

    suspend fun getArtists(): Result<ArtistListResponse> = apiCall { apiService.getSetupArtists() }
    suspend fun saveArtists(names: List<String>): Result<SaveListResponse> =
        apiCall { apiService.saveArtists(SaveListRequest(items = names, artists = names)) }

    suspend fun getMoods(): Result<MoodListResponse> = apiCall { apiService.getSetupMoods() }
    suspend fun saveMoods(names: List<String>): Result<SaveListResponse> =
        apiCall { apiService.saveMoods(SaveListRequest(items = names, moods = names)) }

    suspend fun saveLanguages(languages: List<String>): Result<SaveLanguagesResponse> =
        apiCall { apiService.saveLanguages(SaveLanguagesRequest(languages)) }

    suspend fun getPermissionPrefs(): Result<PermissionPrefsResponse> =
        apiCall { apiService.getPermissionPrefs() }

    suspend fun savePermissionPrefs(
        notificationsEnabled: Boolean,
        downloadsEnabled: Boolean
    ): Result<PermissionPrefsResponse> =
        apiCall {
            apiService.savePermissionPrefs(
                PermissionPrefsRequest(notificationsEnabled, downloadsEnabled)
            )
        }

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
