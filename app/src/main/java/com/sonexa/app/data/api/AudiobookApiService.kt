package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AudiobookApiService {

    @GET("audiobooks")
    suspend fun getAudiobooks(
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("search") search: String? = null,
        @Query("featured") featured: Boolean? = null
    ): Response<AudiobookListResponse>

    @GET("audiobooks/categories")
    suspend fun getCategories(): Response<ApiResponse<List<AudiobookCategoryDto>>>

    @GET("audiobooks/languages")
    suspend fun getLanguages(): Response<ApiResponse<List<AudiobookLanguageDto>>>

    @GET("audiobooks/continue-listening")
    suspend fun getContinueListening(): Response<ApiResponse<List<AudiobookDto>>>

    @GET("audiobooks/{id}")
    suspend fun getAudiobookDetail(
        @Path("id") id: String
    ): Response<AudiobookDetailResponse>

    @GET("audiobooks/{id}/chapters")
    suspend fun getChapters(
        @Path("id") id: String
    ): Response<ApiResponse<List<AudiobookChapterDto>>>

    @GET("audiobooks/{id}/progress")
    suspend fun getProgress(
        @Path("id") id: String
    ): Response<ApiResponse<AudiobookProgressDto>>

    @PUT("audiobooks/{id}/progress")
    suspend fun updateProgress(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<ApiResponse<AudiobookProgressDto>>

    @GET("audiobooks/{id}/bookmarks")
    suspend fun getBookmarks(
        @Path("id") id: String
    ): Response<ApiResponse<List<AudiobookBookmarkDto>>>

    @POST("audiobooks/{id}/bookmarks")
    suspend fun createBookmark(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<ApiResponse<AudiobookBookmarkDto>>

    @DELETE("audiobooks/{id}/bookmarks/{bookmarkId}")
    suspend fun deleteBookmark(
        @Path("id") id: String,
        @Path("bookmarkId") bookmarkId: String
    ): Response<ApiResponse<Map<String, Any>>>
}
