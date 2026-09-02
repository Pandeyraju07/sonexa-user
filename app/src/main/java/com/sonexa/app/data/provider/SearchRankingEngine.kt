package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import java.util.Locale

data class SearchRankingWeights(
    val textRelevanceWeight: Double = 0.30,
    val artistMatchWeight: Double = 0.20,
    val titleMatchWeight: Double = 0.10,
    val popularityWeight: Double = 0.10,
    val userAffinityWeight: Double = 0.10,
    val languageMatchWeight: Double = 0.05,
    val genreMatchWeight: Double = 0.05,
    val metadataQualityWeight: Double = 0.05,
    val streamReliabilityWeight: Double = 0.05
)

class SearchRankingEngine(
    private val weights: SearchRankingWeights = SearchRankingWeights(),
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService()
) {

    fun rankSearchResults(
        tracks: List<TrackDto>,
        query: String,
        preferredLanguage: String = "Hindi",
        preferredGenre: String = "Pop"
    ): List<TrackDto> {
        val qLower = query.lowercase(Locale.ROOT).trim()

        val scoredTracks = tracks.map { track ->
            val score = computeRelevanceScore(track, qLower, preferredLanguage, preferredGenre)
            val tier = when {
                score >= 0.82 -> "EXACT_MATCH"
                score >= 0.65 -> "STRONG_MATCH"
                score >= 0.45 -> "RELATED"
                else -> "DISCOVERY"
            }
            Pair(track.copy(qualityTier = tier), score)
        }

        return scoredTracks.sortedByDescending { it.second }.map { it.first }
    }

    private fun computeRelevanceScore(
        track: TrackDto,
        query: String,
        preferredLanguage: String,
        preferredGenre: String
    ): Double {
        val titleLower = track.title.lowercase(Locale.ROOT)
        val artistLower = track.artist.lowercase(Locale.ROOT)

        // 1. Text Relevance
        val textRel = when {
            titleLower == query || artistLower == query -> 1.0
            titleLower.startsWith(query) || artistLower.startsWith(query) -> 0.88
            titleLower.contains(query) || artistLower.contains(query) -> 0.75
            else -> 0.35
        }

        // 2. Artist Match
        val artistMatch = when {
            artistLower == query -> 1.0
            artistLower.contains(query) -> 0.85
            query.contains(artistLower) && artistLower.length > 3 -> 0.80
            else -> 0.20
        }

        // 3. Title Match
        val titleMatch = when {
            titleLower == query -> 1.0
            titleLower.contains(query) -> 0.80
            else -> 0.20
        }

        // 4. Popularity & Official Status
        val popularityScore = if (track.isOfficial) 0.95 else if (track.isLiked) 0.90 else 0.70

        // 5. Playability
        val playabilityScore = if (track.isPlayable && track.audioUrl.isNotBlank()) 1.0 else 0.40

        // 6. Metadata Quality
        val qualityScore = if (track.effectiveCoverUrl.isNotBlank() && track.durationMs > 0) 1.0 else 0.50

        // 7. Language & Genre affinities
        val langScore = if (track.language.equals(preferredLanguage, ignoreCase = true)) 1.0 else 0.50
        val genreScore = if (track.genre.contains(preferredGenre, ignoreCase = true)) 1.0 else 0.50

        val totalScore = (weights.textRelevanceWeight * textRel) +
                (weights.artistMatchWeight * artistMatch) +
                (weights.titleMatchWeight * titleMatch) +
                (weights.popularityWeight * popularityScore) +
                (weights.streamReliabilityWeight * playabilityScore) +
                (weights.metadataQualityWeight * qualityScore) +
                (weights.languageMatchWeight * langScore) +
                (weights.genreMatchWeight * genreScore)

        return totalScore.coerceIn(0.0, 1.0)
    }
}
