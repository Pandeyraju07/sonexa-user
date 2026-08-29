package com.sonexa.app.data.api

import com.google.gson.annotations.SerializedName
import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class SplashConfigResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("appName") val appName: String = "",
    @SerializedName("version") val version: String = "",
    @SerializedName("minSupportedVersion") val minSupportedVersion: String = "",
    @SerializedName("forceUpdate") val forceUpdate: Boolean = false,
    @SerializedName("maintenanceMode") val maintenanceMode: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class OnboardingSlideDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = ""
)

data class OnboardingResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("slides") val slides: List<OnboardingSlideDto> = emptyList()
)

data class ProfileCreateRequest(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("handle") val handle: String
)

data class MusicLanguageDto(
    @SerializedName("code") val code: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("nativeName") val nativeName: String = ""
)

data class LanguagesCatalogResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("title") val title: String = "Choose Music Languages",
    @SerializedName("subtitle") val subtitle: String = "Select languages you love to listen to",
    @SerializedName("minSelection") val minSelection: Int = 1,
    @SerializedName("defaultSelected") val defaultSelected: List<String> = emptyList(),
    @SerializedName("languages") val languages: List<MusicLanguageDto> = emptyList()
)

data class SaveLanguagesRequest(
    @SerializedName("languages") val languages: List<String>
)

data class SaveLanguagesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("languages") val languages: List<String> = emptyList(),
    @SerializedName("items") val items: List<String> = emptyList(),
    @SerializedName("count") val count: Int = 0
)

data class PermissionPrefsRequest(
    @SerializedName("notificationsEnabled") val notificationsEnabled: Boolean = false,
    @SerializedName("downloadsEnabled") val downloadsEnabled: Boolean = false
)

data class PermissionPrefsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("notificationsEnabled") val notificationsEnabled: Boolean = false,
    @SerializedName("downloadsEnabled") val downloadsEnabled: Boolean = false
)

interface AppConfigApiService {
    @GET("config/splash")
    suspend fun getSplashConfig(): Response<SplashConfigResponse>

    @GET("config/onboarding")
    suspend fun getOnboardingSlides(): Response<OnboardingResponse>

    @GET("config/languages")
    suspend fun getLanguages(): Response<LanguagesCatalogResponse>

    @GET("config/app-update")
    suspend fun getAppUpdate(): Response<AppUpdateResponse>

    @GET("config/permissions")
    suspend fun getPermissions(): Response<PermissionsConfigResponse>

    @POST("profile-setup/create")
    suspend fun createProfile(@Body request: ProfileCreateRequest): Response<SplashConfigResponse>

    @GET("profile-setup/genres")
    suspend fun getSetupGenres(): Response<GenreListResponse>

    @POST("profile-setup/genres")
    suspend fun saveGenres(@Body request: SaveListRequest): Response<SaveListResponse>

    @GET("profile-setup/artists")
    suspend fun getSetupArtists(): Response<ArtistListResponse>

    @POST("profile-setup/artists")
    suspend fun saveArtists(@Body request: SaveListRequest): Response<SaveListResponse>

    @GET("profile-setup/moods")
    suspend fun getSetupMoods(): Response<MoodListResponse>

    @POST("profile-setup/moods")
    suspend fun saveMoods(@Body request: SaveListRequest): Response<SaveListResponse>

    @POST("profile-setup/languages")
    suspend fun saveLanguages(@Body request: SaveLanguagesRequest): Response<SaveLanguagesResponse>

    @GET("profile-setup/permissions")
    suspend fun getPermissionPrefs(): Response<PermissionPrefsResponse>

    @POST("profile-setup/permissions")
    suspend fun savePermissionPrefs(@Body request: PermissionPrefsRequest): Response<PermissionPrefsResponse>
}
