package com.sonexa.app.data.search

import com.sonexa.app.data.provider.MovieSoundtrackCatalog
import java.util.Locale

object IntentDetector {

    private val KNOWN_ARTISTS = listOf(
        "arijit singh", "arijit", "arjit singh", "shreya ghoshal", "shreya", "pritam",
        "a.r. rahman", "ar rahman", "atif aslam", "atif", "diljit dosanjh", "diljit",
        "sidhu moose wala", "badshah", "honey singh", "yo yo honey singh", "sonu nigam",
        "kumar sanu", "alka yagnik", "neha kakkar", "armaan malik", "darshan raval",
        "jubin nautiyal", "anuv jain", "prateek kuhad", "king", "ritviz", "ap dhillon",
        "karan aujla", "lata mangeshkar", "kishore kumar", "mohammed rafi", "mukesh",
        "kk", "mohit chauhan", "udit narayan", "b praak", "jasleen royal", "vishal mishra",
        "sachet tandon", "guru randhawa", "himesh reshammiya", "shankar mahadevan",
        "the weeknd", "taylor swift", "ed sheeran", "drake", "dua lipa", "billie eilish",
        "justin bieber", "charlie puth", "post malone", "bruno mars", "eminem"
    )

    private val KNOWN_TRACKS = listOf(
        "tum hi ho", "kesariya", "channa mereya", "apna bana le", "raataan lambiyan",
        "tera yaar hoon main", "kal ho naa ho", "pal pal dil ke paas", "mera man",
        "agar tum saath ho", "shayad", "tujhe kitna chahne lage", "bekhayali",
        "ghungroo", "jai jai shivshankar", "nashe si chadh gayi", "satranga", "heeriye",
        "o maahi", "ve kamleya", "chaleya", "tere pyaar mein", "aaj ki raat"
    )

    private val MOOD_MAP = mapOf(
        "romantic" to "ROMANTIC", "romance" to "ROMANTIC", "love" to "ROMANTIC", "ishq" to "ROMANTIC",
        "pyaar" to "ROMANTIC", "pyar" to "ROMANTIC", "dil" to "ROMANTIC", "heart touching" to "ROMANTIC",
        "mohabbat" to "ROMANTIC", "aashiqui" to "ROMANTIC", "couple" to "ROMANTIC", "valentine" to "ROMANTIC",
        "sad" to "SAD", "dard" to "SAD", "breakup" to "SAD", "heartbreak" to "SAD", "judaai" to "SAD", "gam" to "SAD",
        "alone" to "SAD", "crying" to "SAD", "dukh" to "SAD", "bewafa" to "SAD",
        "party" to "PARTY", "club" to "PARTY", "dance" to "PARTY", "dhol" to "PARTY", "dj" to "PARTY", "masti" to "PARTY",
        "celebration" to "PARTY", "wedding" to "PARTY", "shaadi" to "PARTY",
        "gym" to "ENERGETIC", "workout" to "ENERGETIC", "energy" to "ENERGETIC", "hard" to "ENERGETIC", "motivational" to "ENERGETIC", "power" to "ENERGETIC",
        "running" to "ENERGETIC", "pump" to "ENERGETIC", "fitness" to "ENERGETIC",
        "chill" to "CHILL", "relax" to "RELAXING", "relaxing" to "RELAXING", "calm" to "CALM", "peaceful" to "PEACEFUL", "feel good" to "HAPPY", "happy" to "HAPPY",
        "lofi" to "CHILL", "lo-fi" to "CHILL", "slowed" to "CHILL", "reverb" to "CHILL", "aesthetic" to "CHILL",
        "bhakti" to "DEVOTIONAL", "devotional" to "DEVOTIONAL", "aarti" to "DEVOTIONAL", "chalisa" to "DEVOTIONAL", "bhajan" to "DEVOTIONAL",
        "krishna" to "DEVOTIONAL", "shiva" to "DEVOTIONAL", "hanuman" to "DEVOTIONAL", "ram" to "DEVOTIONAL",
        "acoustic" to "ACOUSTIC", "unplugged" to "ACOUSTIC", "melody" to "MELODY", "soothing" to "MELODY"
    )

    private val GENRE_MAP = mapOf(
        "bollywood" to "Bollywood", "pop" to "Pop", "hip hop" to "Hip-Hop", "hiphop" to "Hip-Hop", "rap" to "Rap",
        "rock" to "Rock", "punjabi" to "Punjabi", "edm" to "EDM", "indie" to "Indie", "folk" to "Folk",
        "classical" to "Classical", "sufi" to "Sufi", "ghazal" to "Ghazal", "bhangra" to "Bhangra", "qawwali" to "Qawwali"
    )

    private val ERA_REGEX = Regex("""\b(90s|80s|70s|60s|2000s|2010s|2020s|retro|old|classic|purane|latest|new)\b""", RegexOption.IGNORE_CASE)

    fun detect(rawQuery: String): SearchIntent {
        val normalized = QueryNormalizer.normalize(rawQuery)
        val langResult = LanguageDetector.detect(rawQuery)
        val transliterated = if (langResult.isDevanagari) TransliterationService.devanagariToRoman(rawQuery) else null
        val queryForIntent = (transliterated ?: normalized).lowercase(Locale.ROOT)
        val queryClean = QueryNormalizer.stripStopWords(queryForIntent)

        // 1. Check for Similar Track intent ("songs like Tum Hi Ho", "more like kesariya")
        if (queryForIntent.contains("songs like") || queryForIntent.contains("similar to") || queryForIntent.contains("more like")) {
            val trackName = queryForIntent.replace("songs like", "")
                .replace("similar to", "")
                .replace("more like", "")
                .trim()
            return SearchIntent(
                type = SearchIntentType.SIMILAR_TRACK_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                trackTitle = trackName,
                confidence = 0.95
            )
        }

        // 2. Check for Movie / Soundtrack intent (e.g. "Kabir Singh songs", "Animal soundtrack", "Aashiqui 2", "Shershaah")
        val movieQuery = MovieSoundtrackCatalog.extractMovieQuery(queryForIntent)
        val matchedMovieOST = MovieSoundtrackCatalog.findMovieSoundtrack(movieQuery.ifBlank { queryForIntent })
            ?: MovieSoundtrackCatalog.findMovieSoundtrack(queryClean)

        if (matchedMovieOST != null) {
            return SearchIntent(
                type = SearchIntentType.MOVIE_SOUNDTRACK,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                movieName = matchedMovieOST.movieTitle,
                confidence = 0.98
            )
        }

        // 3. Check for Artist + Mood / Era / Language combo (e.g. "Arijit Singh ke romantic songs", "old Kumar Sanu songs")
        val detectedArtist = KNOWN_ARTISTS.firstOrNull { queryForIntent.contains(it) || queryClean.contains(it) }
        val detectedMood = MOOD_MAP.entries.firstOrNull { queryForIntent.contains(it.key) }?.value
        val detectedGenre = GENRE_MAP.entries.firstOrNull { queryForIntent.contains(it.key) }?.value
        val eraMatch = ERA_REGEX.find(queryForIntent)?.value

        if (detectedArtist != null && (detectedMood != null || eraMatch != null)) {
            return SearchIntent(
                type = SearchIntentType.ARTIST_MOOD_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                artistName = detectedArtist,
                mood = detectedMood,
                era = eraMatch,
                confidence = 0.95
            )
        }

        // 4. Check for pure Artist query (e.g. "Arijit Singh", "Arijit", "Arijit ke gaane")
        if (detectedArtist != null && (queryClean == detectedArtist || queryForIntent.contains(detectedArtist))) {
            return SearchIntent(
                type = SearchIntentType.ARTIST_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                artistName = detectedArtist,
                confidence = 0.95
            )
        }

        // 5. Check for Known Track query (e.g. "Tum Hi Ho", "Kesariya")
        val detectedTrack = KNOWN_TRACKS.firstOrNull { queryClean == it || queryForIntent.contains(it) }
        if (detectedTrack != null) {
            return SearchIntent(
                type = SearchIntentType.TRACK_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                trackTitle = detectedTrack,
                confidence = 0.95
            )
        }

        // 6. Check for Era / Mood / Genre
        if (eraMatch != null) {
            return SearchIntent(
                type = SearchIntentType.ERA_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                era = eraMatch,
                mood = detectedMood,
                genre = detectedGenre,
                confidence = 0.90
            )
        }

        if (detectedMood != null) {
            return SearchIntent(
                type = SearchIntentType.MOOD_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                mood = detectedMood,
                genre = detectedGenre,
                confidence = 0.90
            )
        }

        if (detectedGenre != null) {
            return SearchIntent(
                type = SearchIntentType.GENRE_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                genre = detectedGenre,
                confidence = 0.90
            )
        }

        // 7. Check for Language intent (e.g. "Punjabi songs", "Telugu songs")
        if (langResult.language != "en") {
            return SearchIntent(
                type = SearchIntentType.LANGUAGE_SEARCH,
                query = rawQuery,
                normalizedQuery = normalized,
                detectedLanguage = langResult.language,
                isDevanagari = langResult.isDevanagari,
                transliteratedQuery = transliterated,
                confidence = 0.88
            )
        }

        // 8. General / Track Search fallback
        return SearchIntent(
            type = SearchIntentType.GENERAL_MUSIC_SEARCH,
            query = rawQuery,
            normalizedQuery = normalized,
            detectedLanguage = langResult.language,
            isDevanagari = langResult.isDevanagari,
            transliteratedQuery = transliterated,
            confidence = 0.85
        )
    }
}
