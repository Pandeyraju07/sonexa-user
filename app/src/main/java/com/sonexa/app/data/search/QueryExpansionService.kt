package com.sonexa.app.data.search

import com.sonexa.app.data.provider.MovieSoundtrackCatalog
import java.util.Locale

object QueryExpansionService {

    /**
     * Given an intent and query, expands it into a prioritized list of search variants
     * to ensure parallel providers match the widest relevant index without losing precision.
     */
    fun expand(rawQuery: String, intent: SearchIntent): List<String> {
        val variants = mutableListOf<String>()

        val original = rawQuery.trim()
        val normalized = intent.normalizedQuery
        val cleanNoStopWords = QueryNormalizer.stripStopWords(normalized)

        variants.add(original)
        if (normalized.isNotBlank() && normalized != original) {
            variants.add(normalized)
        }
        if (cleanNoStopWords.isNotBlank() && !variants.contains(cleanNoStopWords)) {
            variants.add(cleanNoStopWords)
        }

        // Add Transliterated variant if available
        intent.transliteratedQuery?.let {
            if (it.isNotBlank() && !variants.contains(it)) {
                variants.add(it)
                val cleanTranslit = QueryNormalizer.stripStopWords(it)
                if (cleanTranslit.isNotBlank() && !variants.contains(cleanTranslit)) {
                    variants.add(cleanTranslit)
                }
            }
        }

        when (intent.type) {
            SearchIntentType.MOVIE_SOUNDTRACK -> {
                intent.movieName?.let { movie ->
                    variants.add(movie)
                    variants.add("$movie songs")
                    variants.add("$movie soundtrack")
                    variants.add("$movie audio")
                }
            }

            SearchIntentType.ARTIST_MOOD_SEARCH -> {
                val artist = intent.artistName.orEmpty()
                val mood = intent.mood.orEmpty().lowercase(Locale.ROOT)
                val era = intent.era.orEmpty().lowercase(Locale.ROOT)
                if (artist.isNotBlank()) {
                    variants.add(artist)
                    if (mood.isNotBlank()) {
                        variants.add("$artist $mood")
                        variants.add("$artist $mood songs")
                    }
                    if (era.isNotBlank()) {
                        variants.add("$artist $era")
                        variants.add("$era $artist songs")
                    }
                }
            }

            SearchIntentType.ARTIST_SEARCH -> {
                intent.artistName?.let { artist ->
                    variants.add(artist)
                    variants.add("$artist songs")
                    variants.add("$artist hits")
                }
            }

            SearchIntentType.TRACK_SEARCH -> {
                intent.trackTitle?.let { track ->
                    variants.add(track)
                    variants.add("$track song")
                }
            }

            SearchIntentType.MOOD_SEARCH -> {
                val mood = intent.mood.orEmpty().lowercase(Locale.ROOT)
                if (mood.isNotBlank()) {
                    variants.add("$mood songs")
                    variants.add("$mood hindi songs")
                    variants.add("$mood hits")
                    variants.add("$mood music")
                }
            }

            SearchIntentType.ERA_SEARCH -> {
                val era = intent.era.orEmpty().lowercase(Locale.ROOT)
                variants.add("$era hindi songs")
                variants.add("$era bollywood hits")
                variants.add("$era songs")
                variants.add("$era old songs")
            }

            SearchIntentType.GENRE_SEARCH -> {
                val genre = intent.genre.orEmpty().lowercase(Locale.ROOT)
                variants.add("$genre songs")
                variants.add("$genre hits")
                variants.add("$genre music")
            }

            SearchIntentType.LANGUAGE_SEARCH -> {
                val lang = intent.detectedLanguage
                val langName = when (lang) {
                    "pa" -> "Punjabi"
                    "te" -> "Telugu"
                    "ta" -> "Tamil"
                    "bn" -> "Bengali"
                    "gu" -> "Gujarati"
                    "mr" -> "Marathi"
                    "kn" -> "Kannada"
                    "ml" -> "Malayalam"
                    "ur" -> "Urdu"
                    "bho" -> "Bhojpuri"
                    else -> "Hindi"
                }
                variants.add("$langName songs")
                variants.add("$langName hits")
                variants.add("Top $langName songs")
            }

            SearchIntentType.SIMILAR_TRACK_SEARCH -> {
                intent.trackTitle?.let { track ->
                    variants.add(track)
                    variants.add("$track similar")
                }
            }

            else -> {
                val scriptExpansions = TransliterationService.expandScriptVariants(original)
                scriptExpansions.forEach { exp ->
                    if (!variants.contains(exp)) variants.add(exp)
                }
            }
        }

        return variants.distinct().take(6)
    }
}
