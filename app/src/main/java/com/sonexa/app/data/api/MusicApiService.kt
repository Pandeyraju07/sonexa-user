package com.sonexa.app.data.api

import com.sonexa.app.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApiService {
    @GET("home")
    suspend fun getDynamicHomeFeed(): Response<HomeDynamicFeedResponse>

    @GET("music/home")
    suspend fun getHomeFeed(): Response<HomeFeedResponse>

    @retrofit2.http.POST("events")
    suspend fun trackEvent(@retrofit2.http.Body event: Map<String, Any>): Response<Void>

    @GET("music/trending")
    suspend fun getTrending(): Response<TrendingResponse>

    @GET("music/search")
    suspend fun searchMusic(@Query("q") query: String): Response<SearchResponse>

    @GET("music/tracks/{id}")
    suspend fun getTrack(@Path("id") id: String): Response<TrackDetailResponse>

    @GET("music/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Response<AlbumDetailResponse>

    @GET("music/playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): Response<PlaylistDetailResponse>

    @GET("music/artists/{id}")
    suspend fun getArtist(@Path("id") id: String): Response<ArtistDetailResponse>

    @GET("music/genres")
    suspend fun getGenres(): Response<GenreListResponse>

    @GET("music/artists")
    suspend fun getArtists(): Response<ArtistListResponse>

    @GET("music/moods")
    suspend fun getMoods(): Response<MoodListResponse>

    @GET("music/queue")
    suspend fun getQueue(): Response<QueueResponse>

    @GET("music/tracks/{id}/lyrics")
    suspend fun getTrackLyrics(@Path("id") id: String): Response<LyricsResponse>

    @GET("music/lyrics")
    suspend fun getTrackLyricsByQuery(@Query("trackId") trackId: String): Response<LyricsResponse>

    @GET("podcasts")
    suspend fun getPodcasts(): Response<PodcastListResponse>

    @GET("podcasts/{id}")
    suspend fun getPodcast(@Path("id") id: String): Response<PodcastDetailResponse>
}
