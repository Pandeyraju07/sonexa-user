package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import java.util.Locale

/**
 * Production Search Ranking Engine with configurable multi-signal scoring
 * and exact-match priority (Original > Remix > Live > Cover > Instrumental).
 */
data class SearchRankingWeights(
    val exactTitleWeight: Double = 0.30,
    val artistWeight: Double = 0.18,
    val intentWeight: Double = 0.12,
    val aliasWeight: Double = 0.10,
    val transliterationWeight: Double = 0.08,
    val tokenFuzzyWeight: Double = 0.08,
    val languageWeight: Double = 0.05,
    val genreMoodWeight: Double = 0.04,
    val popularityWeight: Double = 0.03,
    val freshnessWeight: Double = 0.02
)

class SearchRankingEngine(
    var weights: SearchRankingWeights = SearchRankingWeights(),
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService()
) {

    fun rankSearchResults(
        tracks: List<TrackDto>,
        query: String,
        preferredLanguage: String = "Hindi",
        preferredGenre: String = "Pop",
        detectedIntentType: String = "GENERAL"
    ): List<TrackDto> {
        val qLower = query.lowercase(Locale.ROOT).trim()
        val userWantsRemix = qLower.contains("remix") || qLower.contains("mix")
        val userWantsLive = qLower.contains("live") || qLower.contains("concert")
        val userWantsCover = qLower.contains("cover")
        val userWantsInstrumental = qLower.contains("instrumental") || qLower.contains("karaoke")

        val scoredTracks = tracks.map { track ->
            val score = computeRelevanceScore(track, qLower, preferredLanguage, preferredGenre, detectedIntentType)
            
            // Apply exact match priority penalty for remixes/covers unless explicitly searched
            val titleLower = track.title.lowercase(Locale.ROOT)
            val penalty = when {
                !userWantsRemix && (titleLower.contains("remix") || titleLower.contains("club mix")) -> 0.15
                !userWantsLive && (titleLower.contains("live") || titleLower.contains("concert")) -> 0.12
                !userWantsCover && titleLower.contains("cover") -> 0.20
                !userWantsInstrumental && (titleLower.contains("instrumental") || titleLower.contains("karaoke")) -> 0.25
                else -> 0.0
            }
            val finalScore = (score - penalty).coerceIn(0.0, 1.0)

            val tier = when {
                finalScore >= 0.82 -> "EXACT_MATCH"
                finalScore >= 0.65 -> "STRONG_MATCH"
                finalScore >= 0.45 -> "RELATED"
                else -> "DISCOVERY"
            }
            Pair(track.copy(qualityTier = tier), finalScore)
        }

        return scoredTracks.sortedByDescending { it.second }.map { it.first }
    }

    private fun computeRelevanceScore(
        track: TrackDto,
        query: String,
        preferredLanguage: String,
        preferredGenre: String,
        detectedIntentType: String
    ): Double {
        val titleLower = track.title.lowercase(Locale.ROOT).trim()
        val artistLower = track.artist.lowercase(Locale.ROOT).trim()

        // 1. Exact Title Match (30%)
        val exactTitleScore = when {
            titleLower == query -> 1.0
            titleLower.startsWith(query) -> 0.85
            titleLower.contains(query) -> 0.70
            else -> 0.20
        }

        // 2. Artist Match (18%)
        val artistScore = when {
            artistLower == query -> 1.0
            artistLower.startsWith(query) || query.startsWith(artistLower) -> 0.85
            artistLower.contains(query) || query.contains(artistLower) -> 0.70
            else -> 0.20
        }

        // 3. Intent Match (12%)
        val intentScore = when (detectedIntentType) {
            "ARTIST" -> if (artistLower.contains(query) || query.contains(artistLower)) 1.0 else 0.40
            "TRACK" -> if (titleLower.contains(query) || query.contains(titleLower)) 1.0 else 0.40
            "MOOD" -> if (track.mood.contains(query, true)) 1.0 else 0.50
            else -> 0.60
        }

        // 4. Alias & Transliteration (10% + 8%)
        val aliasScore = if (track.tags.any { it.contains(query, true) }) 1.0 else 0.50
        val transliterationScore = 0.80 // Baseline matched by NLP pipeline

        // 5. Token & Fuzzy (8%)
        val tokenScore = if (query.split(" ").all { titleLower.contains(it) || artistLower.contains(it) }) 1.0 else 0.40

        // 6. Language & Genre/Mood (5% + 4%)
        val langScore = if (track.language.equals(preferredLanguage, ignoreCase = true)) 1.0 else 0.50
        val genreScore = if (track.genre.contains(preferredGenre, ignoreCase = true) || track.mood.contains(preferredGenre, ignoreCase = true)) 1.0 else 0.50

        // 7. Popularity & Freshness (3% + 2%)
        val popularityScore = if (track.isOfficial || track.isLiked) 0.95 else 0.70
        val freshnessScore = if (track.eraDecade == "2020s" || track.eraDecade == "Latest") 0.95 else 0.70

        val totalScore = (weights.exactTitleWeight * exactTitleScore) +
                (weights.artistWeight * artistScore) +
                (weights.intentWeight * intentScore) +
                (weights.aliasWeight * aliasScore) +
                (weights.transliterationWeight * transliterationScore) +
                (weights.tokenFuzzyWeight * tokenScore) +
                (weights.languageWeight * langScore) +
                (weights.genreMoodWeight * genreScore) +
                (weights.popularityWeight * popularityScore) +
                (weights.freshnessWeight * freshnessScore)

        return totalScore.coerceIn(0.0, 1.0)
    }
}

