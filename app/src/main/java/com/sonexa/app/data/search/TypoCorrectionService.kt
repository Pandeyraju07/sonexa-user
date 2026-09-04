package com.sonexa.app.data.search

import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object TypoCorrectionService {

    // Common music dictionary entities for rapid and accurate typo correction
    private val KNOWN_ENTITIES = listOf(
        "Arijit Singh",
        "Shreya Ghoshal",
        "Pritam",
        "A.R. Rahman",
        "Atif Aslam",
        "Diljit Dosanjh",
        "Sidhu Moose Wala",
        "Badshah",
        "Yo Yo Honey Singh",
        "Sonu Nigam",
        "Kumar Sanu",
        "Alka Yagnik",
        "Neha Kakkar",
        "Armaan Malik",
        "Darshan Raval",
        "Jubin Nautiyal",
        "Anuv Jain",
        "Prateek Kuhad",
        "King",
        "Ritviz",
        "AP Dhillon",
        "Karan Aujla",
        "The Weeknd",
        "Taylor Swift",
        "Ed Sheeran",
        "Drake",
        "Dua Lipa",
        "Billie Eilish",
        "Justin Bieber",
        "Tum Hi Ho",
        "Kesariya",
        "Channa Mereya",
        "Apna Bana Le",
        "Raataan Lambiyan",
        "Kabir Singh",
        "Shershaah",
        "Animal",
        "Rockstar",
        "Tamasha",
        "Yeh Jawaani Hai Deewani",
        "Jab We Met",
        "Kal Ho Naa Ho",
        "Gully Boy",
        "Cocktail",
        "Romantic Hindi Songs",
        "Bollywood Party Hits",
        "90s Hindi Songs",
        "Punjabi Party Songs",
        "Tamil Melody Songs",
        "Telugu Love Songs",
        "Gym Workout Songs",
        "Relaxing Music"
    )

    /**
     * Finds the closest entity match if the query is a likely typo.
     * E.g. "Arjit Singh" -> "Arijit Singh", "Tum Hi Hoo" -> "Tum Hi Ho", "Shershah" -> "Shershaah"
     */
    fun findCorrection(rawQuery: String): DidYouMeanSuggestion? {
        val query = rawQuery.trim()
        if (query.length < 3) return null

        val qNorm = QueryNormalizer.normalize(query)
        val qClean = QueryNormalizer.stripStopWords(qNorm)

        var bestMatch: String? = null
        var highestScore = 0.0

        for (entity in KNOWN_ENTITIES) {
            val entityNorm = QueryNormalizer.normalize(entity)

            // Exact match - not a typo
            if (qNorm == entityNorm || qClean == entityNorm) return null

            val jaroWinkler = computeJaroWinkler(qNorm, entityNorm)
            val levDist = computeLevenshteinDistance(qNorm, entityNorm)
            val maxLen = max(qNorm.length, entityNorm.length)
            val levSimilarity = if (maxLen > 0) 1.0 - (levDist.toDouble() / maxLen.toDouble()) else 0.0

            // Phonetic / Vowel normalization comparison (e.g. ee -> i, oo -> u, aa -> a)
            val phoneticSim = computePhoneticSimilarity(qNorm, entityNorm)

            val combinedSimilarity = (jaroWinkler * 0.45) + (levSimilarity * 0.35) + (phoneticSim * 0.20)

            if (combinedSimilarity > 0.72 && combinedSimilarity > highestScore) {
                highestScore = combinedSimilarity
                bestMatch = entity
            }
        }

        return if (bestMatch != null && highestScore >= 0.75) {
            DidYouMeanSuggestion(
                originalQuery = query,
                correctedQuery = bestMatch,
                confidence = highestScore,
                reason = "Fuzzy similarity ${(highestScore * 100).toInt()}%"
            )
        } else null
    }

    private fun computePhoneticSimilarity(s1: String, s2: String): Double {
        fun simplifyVowels(s: String): String {
            return s.replace("ee", "i")
                .replace("oo", "u")
                .replace("aa", "a")
                .replace("ai", "e")
                .replace("ou", "o")
                .replace("sh", "s")
                .replace("ph", "f")
                .replace("kh", "k")
                .replace("gh", "g")
                .replace("th", "t")
                .replace("dh", "d")
                .replace("bh", "b")
        }

        val v1 = simplifyVowels(s1)
        val v2 = simplifyVowels(s2)

        if (v1 == v2) return 1.0
        return computeJaroWinkler(v1, v2)
    }

    /**
     * Levenshtein edit distance calculation.
     */
    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
            }
        }
        return dp[m][n]
    }

    /**
     * Jaro-Winkler string similarity (0.0 to 1.0).
     */
    fun computeJaroWinkler(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val matchDistance = max(s1.length, s2.length) / 2 - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)

        var matches = 0
        for (i in s1.indices) {
            val start = max(0, i - matchDistance)
            val end = min(i + matchDistance + 1, s2.length)
            for (j in start until end) {
                if (s2Matches[j] || s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return 0.0

        var transpositions = 0
        var k = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }

        val jaro = ((matches.toDouble() / s1.length) +
                (matches.toDouble() / s2.length) +
                ((matches - transpositions / 2.0) / matches)) / 3.0

        // Winkler prefix bonus (up to 4 characters)
        var prefixLength = 0
        val maxPrefix = min(4, min(s1.length, s2.length))
        for (i in 0 until maxPrefix) {
            if (s1[i] == s2[i]) prefixLength++ else break
        }

        val p = 0.1 // Standard scaling factor
        return jaro + (prefixLength * p * (1 - jaro))
    }
}
