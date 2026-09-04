package com.sonexa.app.data.search

import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.TrackUnderstandingService
import java.util.Locale

data class SearchRankingConfig(
    val exactMatchWeight: Double = 0.30,
    val titleMatchWeight: Double = 0.18,
    val artistMatchWeight: Double = 0.15,
    val intentMatchWeight: Double = 0.10,
    val transliterationWeight: Double = 0.08,
    val languageMatchWeight: Double = 0.06,
    val fuzzySimilarityWeight: Double = 0.05,
    val personalizationWeight: Double = 0.04,
    val popularityWeight: Double = 0.02,
    val freshnessWeight: Double = 0.02
)

class SearchRankingService(
    private val config: SearchRankingConfig = SearchRankingConfig(),
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService()
) {

    fun rankTracks(
        tracks: List<TrackDto>,
        query: String,
        intent: SearchIntent,
        preferredLanguage: String = "Hindi",
        preferredGenre: String = "Pop"
    ): List<TrackDto> {
        val qNorm = QueryNormalizer.normalize(query)
        val qClean = QueryNormalizer.stripStopWords(qNorm)
        val transliterated = intent.transliteratedQuery?.let { QueryNormalizer.normalize(it) }

        val scoredTracks = tracks.map { track ->
            val score = calculateScore(track, qNorm, qClean, transliterated, intent, preferredLanguage, preferredGenre)
            val titleNorm = QueryNormalizer.normalize(track.title)
            val isExact = (titleNorm == qNorm || titleNorm == qClean || (intent.trackTitle != null && titleNorm == intent.trackTitle)) &&
                    track.versionType == "Original" &&
                    !titleNorm.contains("remix") &&
                    !titleNorm.contains("cover")

            val tier = when {
                isExact || score >= 0.80 -> "EXACT_MATCH"
                score >= 0.60 -> "STRONG_MATCH"
                score >= 0.40 -> "RELATED"
                else -> "DISCOVERY"
            }
            Pair(track.copy(qualityTier = tier), score)
        }

        // Sort descending by calculated multi-signal score
        return scoredTracks.sortedByDescending { it.second }.map { it.first }
    }

    private fun calculateScore(
        track: TrackDto,
        qNorm: String,
        qClean: String,
        transliterated: String?,
        intent: SearchIntent,
        preferredLanguage: String,
        preferredGenre: String
    ): Double {
        val titleNorm = QueryNormalizer.normalize(track.title)
        val artistNorm = QueryNormalizer.normalize(track.artist)
        val albumNorm = QueryNormalizer.normalize(track.album)

        // 1. Exact Match (30%) - Original tracks with exact title match score 1.0; remixes/covers get penalized
        val isExactTitle = titleNorm == qNorm || titleNorm == qClean || (intent.trackTitle != null && titleNorm == intent.trackTitle)
        val isExactArtist = artistNorm == qNorm || artistNorm == qClean
        val isVersion = track.versionType != "Original" ||
                titleNorm.contains("remix") ||
                titleNorm.contains("cover") ||
                titleNorm.contains("instrumental") ||
                titleNorm.contains("live") ||
                titleNorm.contains("reverb") ||
                titleNorm.contains("lofi") ||
                titleNorm.contains("slowed")

        val exactScore = when {
            isExactTitle && !isVersion -> 1.0
            isExactTitle && isVersion -> 0.50
            isExactArtist -> 0.95
            titleNorm.startsWith(qNorm) || titleNorm.startsWith(qClean) -> if (isVersion) 0.50 else 0.85
            else -> 0.20
        }

        // 2. Title Match (18%)
        val titleScore = when {
            titleNorm == qNorm || (intent.trackTitle != null && titleNorm == intent.trackTitle) -> if (isVersion) 0.60 else 1.0
            titleNorm.contains(qNorm) || (qClean.isNotBlank() && titleNorm.contains(qClean)) -> 0.80
            else -> 0.10
        }

        // 3. Artist Match (15%)
        val artistScore = when {
            artistNorm == qNorm || (intent.artistName != null && artistNorm.contains(intent.artistName)) -> 1.0
            artistNorm.contains(qNorm) || (qClean.isNotBlank() && artistNorm.contains(qClean)) -> 0.85
            else -> 0.10
        }

        // 4. Intent Match (10%)
        val intentScore = when (intent.type) {
            SearchIntentType.TRACK_SEARCH -> {
                if (titleNorm == intent.trackTitle || titleNorm.contains(intent.trackTitle.orEmpty())) {
                    if (isVersion) 0.60 else 1.0
                } else 0.50
            }
            SearchIntentType.ARTIST_MOOD_SEARCH -> {
                val matchesArtist = intent.artistName != null && artistNorm.contains(intent.artistName)
                val matchesMood = intent.mood != null && (track.mood.equals(intent.mood, ignoreCase = true) || track.tags.any { it.equals(intent.mood, ignoreCase = true) })
                if (matchesArtist && matchesMood) 1.0 else if (matchesArtist) 0.80 else 0.40
            }
            SearchIntentType.MOVIE_SOUNDTRACK -> {
                val movie = intent.movieName.orEmpty().lowercase(Locale.ROOT)
                if (albumNorm.contains(movie) || titleNorm.contains(movie)) 1.0 else 0.50
            }
            SearchIntentType.MOOD_SEARCH -> {
                if (track.mood.equals(intent.mood, ignoreCase = true) || track.tags.any { it.equals(intent.mood, ignoreCase = true) }) 1.0 else 0.40
            }
            SearchIntentType.ERA_SEARCH -> {
                if (track.tags.any { it.contains(intent.era.orEmpty(), ignoreCase = true) } || track.language.equals("Hindi", ignoreCase = true)) 0.90 else 0.50
            }
            SearchIntentType.GENRE_SEARCH -> {
                if (track.genres.any { it.equals(intent.genre.orEmpty(), ignoreCase = true) }) 1.0 else 0.50
            }
            SearchIntentType.SIMILAR_TRACK_SEARCH -> {
                if (titleNorm.contains(intent.trackTitle.orEmpty().lowercase(Locale.ROOT))) 1.0 else 0.60
            }
            else -> 0.70
        }

        // 5. Transliteration Match (8%)
        val transliterationScore = if (transliterated != null) {
            when {
                titleNorm == transliterated || artistNorm == transliterated -> 1.0
                titleNorm.contains(transliterated) || artistNorm.contains(transliterated) -> 0.85
                else -> 0.30
            }
        } else {
            0.50
        }

        // 6. Language Match (6%)
        val langScore = if (track.language.equals(preferredLanguage, ignoreCase = true) || (intent.detectedLanguage == "hi" && track.language.equals("Hindi", ignoreCase = true))) {
            1.0
        } else {
            0.40
        }

        // 7. Fuzzy Similarity (5%)
        val fuzzyScore = TypoCorrectionService.computeJaroWinkler(qNorm, titleNorm)

        // 8. Personalization (4%)
        val isLiked = LikedSongsStore.isLiked(track.id)
        val personalizationScore = if (isLiked) 1.0 else 0.50

        // 9. Popularity & Streamability (2%)
        val popScore = if (track.isOfficial && track.isPlayable) 1.0 else if (track.isPlayable) 0.80 else 0.20

        // 10. Freshness & Metadata completeness (2%)
        val freshScore = if (track.effectiveCoverUrl.isNotBlank() && track.durationMs > 0) 1.0 else 0.50

        val total = (config.exactMatchWeight * exactScore) +
                (config.titleMatchWeight * titleScore) +
                (config.artistMatchWeight * artistScore) +
                (config.intentMatchWeight * intentScore) +
                (config.transliterationWeight * transliterationScore) +
                (config.languageMatchWeight * langScore) +
                (config.fuzzySimilarityWeight * fuzzyScore) +
                (config.personalizationWeight * personalizationScore) +
                (config.popularityWeight * popScore) +
                (config.freshnessWeight * freshScore)

        return total.coerceIn(0.0, 1.0)
    }
}
