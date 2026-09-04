package com.sonexa.app.data.search

import java.util.Locale

object TransliterationService {

    // Common Hindi/Bollywood Canonical Dictionary for 100% accurate entity and phrase resolution
    private val CANONICAL_PHRASES = mapOf(
        "अरिजीत सिंह" to "arijit singh",
        "अरिजित सिंह" to "arijit singh",
        "अरिजीत" to "arijit",
        "तुम ही हो" to "tum hi ho",
        "कबीर सिंह" to "kabir singh",
        "शेरशाह" to "shershaah",
        "रॉकस्टार" to "rockstar",
        "तमाशा" to "tamasha",
        "आशिकी" to "aashiqui",
        "आशिकी 2" to "aashiqui 2",
        "श्रेया घोषाल" to "shreya ghoshal",
        "श्रेया" to "shreya",
        "प्रीतम" to "pritam",
        "आतिफ असलम" to "atif aslam",
        "आतिफ" to "atif",
        "दिलजीत दोसांझ" to "diljit dosanjh",
        "दिलजीत" to "diljit",
        "सोनू निगम" to "sonu nigam",
        "कुमार सानू" to "kumar sanu",
        "अलका याग्निक" to "alka yagnik",
        "लता मंगेशकर" to "lata mangeshkar",
        "किशोर कुमार" to "kishore kumar",
        "मुकेश" to "mukesh",
        "मोहम्मद रफी" to "mohammed rafi",
        "बादशाह" to "badshah",
        "हनी सिंह" to "honey singh",
        "यो यो हनी सिंह" to "yo yo honey singh",
        "सिद्धू मूसे वाला" to "sidhu moose wala",
        "पुराने हिंदी गाने" to "old hindi songs",
        "रोमांटिक गाने" to "romantic hindi songs",
        "हिंदी रोमांटिक गाने" to "romantic hindi songs",
        "दर्द भरे गाने" to "sad hindi songs",
        "पार्टी गाने" to "party songs",
        "भक्ति गाने" to "bhakti songs",
        "हनुमान चालीसा" to "hanuman chalisa",
        "पल पल दिल के पास" to "pal pal dil ke paas",
        "मेरा मन" to "mera man",
        "केसरिया" to "kesariya",
        "अपना बना ले" to "apna bana le",
        "रातां लम्बियां" to "raataan lambiyan",
        "चन्ना मेरेया" to "channa mereya",
        "तेरा यार हूं मैं" to "tera yaar hoon main",
        "कल हो ना हो" to "kal ho naa ho",
        "यह जवानी है दीवानी" to "yeh jawaani hai deewani",
        "जब वी मेट" to "jab we met",
        "गली बॉय" to "gully boy",
        "एनिमल" to "animal",
        "डंकी" to "dunki",
        "जवान" to "jawan",
        "पठान" to "pathaan"
    )

    // Devanagari Independent Vowels
    private val VOWELS: Map<Char, String> = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo",
        'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au"
    )

    // Devanagari Matras (Vowel signs)
    private val MATRAS: Map<Char, String> = mapOf(
        'ा' to "a", 'ि' to "i", 'ी' to "i", 'ु' to "u", 'ू' to "oo",
        'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au",
        'ं' to "n", 'ँ' to "n", 'ः' to "h", 'ॅ' to "e", 'ॉ' to "o"
    )

    // Devanagari Consonants (Single Unicode code points)
    private val CONSONANTS: Map<Char, String> = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "f", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'श' to "sh",
        'ष' to "sh", 'स' to "s", 'ह' to "h",
        '\u0958' to "q",  // क़
        '\u0959' to "kh", // ख़
        '\u095A' to "gh", // ग़
        '\u095B' to "z",  // ज़
        '\u095C' to "d",  // ड़
        '\u095D' to "dh", // ढ़
        '\u095E' to "f"   // फ़
    )

    private const val VIRAMA = '्'
    private const val NUKTA = '\u093C'

    fun transliterate(input: String): String = devanagariToRoman(input)

    /**
     * Transliterates Devanagari query to standard Roman/English text.
     * E.g. "अरिजीत सिंह" -> "arijit singh", "तुम ही हो" -> "tum hi ho"
     */
    fun devanagariToRoman(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""

        // 1. Check canonical phrase dictionary first
        CANONICAL_PHRASES[trimmed]?.let { return it }

        // Check if query contains any phrase matches
        for ((dev, rom) in CANONICAL_PHRASES) {
            if (trimmed.contains(dev)) {
                return trimmed.replace(dev, rom).let { devanagariToRoman(it) }
            }
        }

        // 2. Character-by-character algorithmic phoneme transliteration
        val sb = StringBuilder()
        val len = trimmed.length
        var i = 0

        while (i < len) {
            val ch = trimmed[i]

            // Ignore nukta if present separately
            if (ch == NUKTA) {
                i++
                continue
            }

            // Check if independent vowel
            if (VOWELS.containsKey(ch)) {
                sb.append(VOWELS[ch])
                i++
                continue
            }

            // Check if consonant
            if (CONSONANTS.containsKey(ch)) {
                val consonantRoman = CONSONANTS[ch]!!
                sb.append(consonantRoman)

                // Look ahead for matra or virama
                if (i + 1 < len) {
                    val nextCh = trimmed[i + 1]
                    if (nextCh == VIRAMA) {
                        // Half consonant, no inherent 'a'
                        i += 2
                        continue
                    } else if (MATRAS.containsKey(nextCh)) {
                        sb.append(MATRAS[nextCh])
                        i += 2
                        continue
                    } else if (CONSONANTS.containsKey(nextCh) || VOWELS.containsKey(nextCh) || nextCh.isWhitespace()) {
                        // Inherent vowel logic: add 'a' unless at word boundary
                        val isWordEnd = i + 1 == len || trimmed[i + 1].isWhitespace()
                        if (!isWordEnd && !consonantRoman.endsWith("a")) {
                            sb.append("a")
                        }
                    }
                }
                i++
                continue
            }

            // Matra alone
            if (MATRAS.containsKey(ch)) {
                sb.append(MATRAS[ch])
                i++
                continue
            }

            // Fallback: character as-is (spaces, Latin letters, numbers)
            sb.append(ch)
            i++
        }

        return sb.toString()
            .replace(Regex("""\s+"""), " ")
            .lowercase(Locale.ROOT)
            .trim()
    }

    /**
     * Checks if a string has Devanagari characters and provides both original + romanized variants.
     */
    fun expandScriptVariants(query: String): List<String> {
        val trimmed = query.trim()
        val isDevanagari = trimmed.any { it in '\u0900'..'\u097F' }

        if (!isDevanagari) {
            return listOf(trimmed)
        }

        val romanized = devanagariToRoman(trimmed)
        return if (romanized.isNotBlank() && romanized != trimmed) {
            listOf(romanized, trimmed)
        } else {
            listOf(trimmed)
        }
    }
}
