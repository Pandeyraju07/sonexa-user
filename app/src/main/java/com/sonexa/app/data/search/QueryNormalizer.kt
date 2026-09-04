package com.sonexa.app.data.search

import java.text.Normalizer
import java.util.Locale

object QueryNormalizer {

    private val ENGLISH_STOP_WORDS = setOf(
        "songs", "song", "music", "tracks", "track", "audio", "mp3", "video",
        "play", "please", "play me", "give me", "find", "show", "show me",
        "listen", "listen to", "all", "full", "soundtrack", "ost", "hits", "best", "top",
        "like", "from", "for", "by", "of", "the", "a", "an", "with"
    )

    private val HINDI_STOP_WORDS = setOf(
        "ke", "ki", "ka", "ko", "se", "mein", "me", "par", "pe",
        "gaane", "gaana", "gana", "gane", "geet", "dhun", "sangeet",
        "wala", "wali", "wale", "waale", "waali", "waala", "waley",
        "jaisa", "jaise", "kaisi", "kaise", "bhi", "aur", "toh", "to",
        "batao", "sunao", "chalaye", "chalao", "bajao", "karo", "do"
    )

    private val PUNCTUATION_REGEX = Regex("""[^\p{L}\p{Nd}\s]""")
    private val MULTI_SPACE_REGEX = Regex("""\s+""")

    /**
     * Fully normalizes a raw search query:
     * - Trims & converts to lowercase
     * - Strips emojis and special punctuation/symbols
     * - Decomposes diacritics
     * - Normalizes multi-spaces
     * - Preserves Devanagari and Indic Unicode characters
     */
    fun normalize(rawQuery: String): String {
        if (rawQuery.isBlank()) return ""

        val decomposed = Normalizer.normalize(rawQuery, Normalizer.Form.NFD)
        val withoutAccents = decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        return withoutAccents
            .lowercase(Locale.ROOT)
            .replace(PUNCTUATION_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    /**
     * Strips conversational Hindi/English search stop words to extract core musical entities.
     * E.g.: "Arijit Singh ke romantic songs" -> "arijit singh romantic"
     * E.g.: "Kabir Singh ke gaane" -> "kabir singh"
     * E.g.: "Play Tum Hi Ho" -> "tum hi ho"
     */
    fun stripStopWords(normalizedQuery: String): String {
        val tokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        val filtered = tokens.filter { token ->
            !ENGLISH_STOP_WORDS.contains(token) && !HINDI_STOP_WORDS.contains(token)
        }
        return if (filtered.isNotEmpty()) filtered.joinToString(" ") else normalizedQuery
    }

    /**
     * Splits query into distinct keyword tokens.
     */
    fun tokenize(normalizedQuery: String): List<String> {
        return normalizedQuery.split(" ").filter { it.isNotBlank() }
    }
}
