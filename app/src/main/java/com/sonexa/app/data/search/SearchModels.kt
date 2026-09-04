package com.sonexa.app.data.search

import com.google.gson.annotations.SerializedName
import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.ResolvedArtist
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.MovieSoundtrack

enum class SearchSectionType {
    TOP_RESULT,
    TRACKS,
    ARTISTS,
    ALBUMS,
    PLAYLISTS,
    MOVIE_SOUNDTRACK,
    RELATED_SEARCHES,
    DISCOVERY_SUGGESTIONS
}

enum class SearchIntentType {
    TRACK_SEARCH,
    ARTIST_SEARCH,
    ALBUM_SEARCH,
    PLAYLIST_SEARCH,
    MOVIE_SOUNDTRACK,
    GENRE_SEARCH,
    MOOD_SEARCH,
    ERA_SEARCH,
    LANGUAGE_SEARCH,
    ARTIST_MOOD_SEARCH,
    SIMILAR_TRACK_SEARCH,
    GENERAL_MUSIC_SEARCH
}

data class SearchIntent(
    val type: SearchIntentType = SearchIntentType.GENERAL_MUSIC_SEARCH,
    val query: String = "",
    val normalizedQuery: String = "",
    val detectedLanguage: String = "en",
    val isDevanagari: Boolean = false,
    val transliteratedQuery: String? = null,
    val artistName: String? = null,
    val trackTitle: String? = null,
    val movieName: String? = null,
    val mood: String? = null,
    val genre: String? = null,
    val era: String? = null,
    val confidence: Double = 0.85
)

data class DidYouMeanSuggestion(
    val originalQuery: String,
    val correctedQuery: String,
    val confidence: Double = 0.90,
    val reason: String = "Typo correction"
)

data class SearchSection(
    val type: SearchSectionType,
    val title: String,
    val subtitle: String? = null,
    val tracks: List<TrackDto> = emptyList(),
    val artists: List<ArtistDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
    val playlists: List<PlaylistDto> = emptyList(),
    val topItem: Any? = null, // Can be TrackDto, ArtistDto, AlbumDto, or MovieSoundtrack
    val relatedQueries: List<String> = emptyList()
)

data class UnifiedSearchResponse(
    val originalQuery: String,
    val normalizedQuery: String,
    val detectedLanguage: String,
    val intent: SearchIntent,
    val didYouMean: DidYouMeanSuggestion? = null,
    val topResult: Any? = null,
    val sections: List<SearchSection> = emptyList(),
    val allTracks: List<TrackDto> = emptyList(),
    val topArtist: ArtistDto? = null,
    val resolvedArtist: ResolvedArtist? = null,
    val movieSoundtrack: MovieSoundtrack? = null,
    val matchingAlbums: List<AlbumDto> = emptyList(),
    val matchingPlaylists: List<PlaylistDto> = emptyList(),
    val relatedSearches: List<String> = emptyList(),
    val totalResults: Int = 0,
    val executionTimeMs: Long = 0L,
    val providerLatencies: Map<String, Long> = emptyMap(),
    val providerCounts: Map<String, Int> = emptyMap()
)

data class SearchAnalyticsEvent(
    val eventType: String, // SEARCH_SUBMITTED, RESULT_CLICKED, TRACK_PLAYED, SUGGESTION_CLICKED, VOICE_SEARCH
    val query: String,
    val normalizedQuery: String,
    val intentType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val clickedItemId: String? = null,
    val clickedItemType: String? = null,
    val position: Int = 0,
    val latencyMs: Long = 0L
)
