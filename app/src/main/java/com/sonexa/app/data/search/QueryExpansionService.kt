package com.sonexa.app.data.search

import com.sonexa.app.data.provider.MovieSoundtrackCatalog
import java.util.Locale

object QueryExpansionService {

    private val ROMANTIC_EXPANSIONS = listOf(
        "Top Romantic Hindi Songs",
        "Bollywood Love Hits",
        "Kesariya",
        "Tum Hi Ho",
        "Apna Bana Le",
        "Raataan Lambiyan",
        "Shayad",
        "Pee Loon",
        "Agar Tum Saath Ho",
        "Tujhe Kitna Chahne Lage",
        "Kaise Hua",
        "Tera Ban Jaunga",
        "Ve Kamleya",
        "Heeriye",
        "O Maahi",
        "Tere Hawaale",
        "Satranga",
        "Samjhawan",
        "Pehla Nasha",
        "Zaalima",
        "Hawayein"
    )

    private val SAD_EXPANSIONS = listOf(
        "Sad Hindi Songs",
        "Heartbreak Bollywood Hits",
        "Channa Mereya",
        "Bekhayali",
        "Agar Tum Saath Ho",
        "Tujhe Kitna Chahne Lage",
        "Hamari Adhuri Kahani",
        "Duaa",
        "Kabira",
        "Khairiyat",
        "O Saathi",
        "Roke Na Ruke Naina",
        "Main Dhoondne Ko Zamaane Mein",
        "Bhula Dena",
        "Judaai"
    )

    private val PARTY_EXPANSIONS = listOf(
        "Bollywood Party Hits",
        "Top Dance Hindi Songs",
        "Ghungroo",
        "Nashe Si Chadh Gayi",
        "Kar Gayi Chull",
        "Abhi Toh Party Shuru Hui Hai",
        "Aankh Marey",
        "Kala Chashma",
        "Badtameez Dil",
        "Tauba Tauba",
        "What Jhumka",
        "Hookah Bar",
        "Garmi",
        "Lungi Dance"
    )

    private val WORKOUT_EXPANSIONS = listOf(
        "Gym Workout Songs",
        "High Energy Hindi Beats",
        "Zinda",
        "Sultan",
        "Brothers Anthem",
        "Dangal",
        "Chak De India",
        "Kar Har Maidaan Fateh",
        "Believer",
        "Aarambh Hai Prachand",
        "Shiv Tandav"
    )

    private val LOFI_CHILL_EXPANSIONS = listOf(
        "Lo-Fi Hindi Songs",
        "Chill Bollywood Acoustic",
        "Baarishein",
        "Alag Aasmaan",
        "Gul",
        "Kasoor",
        "Husn",
        "Choo Lo",
        "Waqt Ki Baatein",
        "Faasle",
        "Kho Gaye Hum Kahan",
        "Cold/Mess"
    )

    private val DEVOTIONAL_EXPANSIONS = listOf(
        "Top Bhakti Songs",
        "Morning Bhajan Hits",
        "Hanuman Chalisa",
        "Namo Namo",
        "Shiv Tandav Stotram",
        "Achyutam Keshavam",
        "Radhe Radhe",
        "Shree Krishna Govind",
        "Jai Shri Ram",
        "Maha Mrityunjaya Mantra"
    )

    private val RETRO_90S_EXPANSIONS = listOf(
        "90s Romantic Bollywood",
        "Best of 90s Hindi Hits",
        "Pehla Nasha",
        "Tujhe Dekha Toh",
        "Dil Deewana",
        "Tip Tip Barsa Paani",
        "Chura Ke Dil Mera",
        "Dheere Dheere Se",
        "Mera Dil Bhi Kitna Pagal Hai",
        "Bahon Ke Darmiyan"
    )

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
                val movieTarget = intent.movieName ?: original
                variants.add(movieTarget)
                variants.add("$movieTarget songs")
                variants.add("$movieTarget soundtrack")
                variants.add("$movieTarget original motion picture")

                // Add individual track names from movie soundtrack catalog
                val ost = MovieSoundtrackCatalog.findMovieSoundtrack(movieTarget)
                if (ost != null && ost.searchTerms.isNotEmpty()) {
                    ost.searchTerms.take(5).forEach { trackName ->
                        variants.add("$trackName $movieTarget")
                        variants.add(trackName)
                    }
                }
            }

            SearchIntentType.ARTIST_MOOD_SEARCH -> {
                val artist = intent.artistName.orEmpty()
                val mood = intent.mood.orEmpty().lowercase(Locale.ROOT)
                val era = intent.era.orEmpty().lowercase(Locale.ROOT)
                if (artist.isNotBlank()) {
                    variants.add(artist)
                    if (mood.isNotBlank()) {
                        variants.add("$artist $mood songs")
                        variants.add("$artist $mood hits")
                    }
                    if (era.isNotBlank()) {
                        variants.add("$era $artist songs")
                        variants.add("$artist $era")
                    }
                }
            }

            SearchIntentType.ARTIST_SEARCH -> {
                intent.artistName?.let { artist ->
                    variants.add(artist)
                    variants.add("$artist best songs")
                    variants.add("$artist hits")
                    variants.add("$artist all songs")
                }
            }

            SearchIntentType.TRACK_SEARCH -> {
                intent.trackTitle?.let { track ->
                    variants.add(track)
                    variants.add("$track song")
                }
            }

            SearchIntentType.MOOD_SEARCH -> {
                val mood = intent.mood.orEmpty().uppercase(Locale.ROOT)
                when (mood) {
                    "ROMANTIC" -> ROMANTIC_EXPANSIONS.take(6).forEach { variants.add(it) }
                    "SAD" -> SAD_EXPANSIONS.take(6).forEach { variants.add(it) }
                    "PARTY" -> PARTY_EXPANSIONS.take(6).forEach { variants.add(it) }
                    "ENERGETIC" -> WORKOUT_EXPANSIONS.take(6).forEach { variants.add(it) }
                    "CHILL", "RELAXING", "CALM" -> LOFI_CHILL_EXPANSIONS.take(6).forEach { variants.add(it) }
                    "DEVOTIONAL" -> DEVOTIONAL_EXPANSIONS.take(6).forEach { variants.add(it) }
                    else -> {
                        val mLower = intent.mood.orEmpty().lowercase(Locale.ROOT)
                        variants.add("$mLower songs")
                        variants.add("$mLower hindi songs")
                        variants.add("Top $mLower songs")
                    }
                }
            }

            SearchIntentType.ERA_SEARCH -> {
                val era = intent.era.orEmpty().lowercase(Locale.ROOT)
                if (era.contains("90") || era.contains("retro") || era.contains("old")) {
                    RETRO_90S_EXPANSIONS.take(6).forEach { variants.add(it) }
                } else {
                    variants.add("$era hindi songs")
                    variants.add("$era bollywood hits")
                    variants.add("$era songs")
                }
            }

            SearchIntentType.GENRE_SEARCH -> {
                val genre = intent.genre.orEmpty().lowercase(Locale.ROOT)
                if (genre.contains("romance") || genre.contains("love")) {
                    ROMANTIC_EXPANSIONS.take(6).forEach { variants.add(it) }
                } else {
                    variants.add("$genre songs")
                    variants.add("$genre hits")
                    variants.add("Top $genre songs")
                }
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
                // If query mentions love / romantic / romantic song
                val lower = original.lowercase(Locale.ROOT)
                if (lower.contains("love") || lower.contains("romance") || lower.contains("romantic") || lower.contains("pyaar") || lower.contains("ishq")) {
                    ROMANTIC_EXPANSIONS.take(5).forEach { variants.add(it) }
                } else if (lower.contains("sad") || lower.contains("dard") || lower.contains("breakup")) {
                    SAD_EXPANSIONS.take(5).forEach { variants.add(it) }
                } else if (lower.contains("party") || lower.contains("dance") || lower.contains("club")) {
                    PARTY_EXPANSIONS.take(5).forEach { variants.add(it) }
                } else {
                    val scriptExpansions = TransliterationService.expandScriptVariants(original)
                    scriptExpansions.forEach { exp ->
                        if (!variants.contains(exp)) variants.add(exp)
                    }
                }
            }
        }

        return variants.distinct().take(8)
    }
}
