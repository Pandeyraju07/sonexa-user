package com.sonexa.app.data.search

import java.util.Locale

object LanguageDetector {

    // Unicode ranges for Indian Indic scripts
    private val DEVANAGARI_RANGE = '\u0900'..'\u097F'
    private val GURMUKHI_RANGE = '\u0A00'..'\u0A7F'
    private val TELUGU_RANGE = '\u0C00'..'\u0C7F'
    private val TAMIL_RANGE = '\u0B80'..'\u0BFF'
    private val BENGALI_RANGE = '\u0980'..'\u09FF'
    private val GUJARATI_RANGE = '\u0A80'..'\u0AFF'
    private val MALAYALAM_RANGE = '\u0D00'..'\u0D7F'
    private val KANNADA_RANGE = '\u0C80'..'\u0CFF'
    private val ARABIC_URDU_RANGE = '\u0600'..'\u06FF'

    data class DetectionResult(
        val language: String, // "hi", "pa", "te", "ta", "bn", "gu", "mr", "kn", "ml", "ur", "bho", "en"
        val script: String,   // "Devanagari", "Gurmukhi", "Latin", "Telugu", "Tamil", etc.
        val isDevanagari: Boolean,
        val isIndicScript: Boolean
    )

    fun detect(query: String): DetectionResult {
        if (query.isBlank()) {
            return DetectionResult("en", "Latin", isDevanagari = false, isIndicScript = false)
        }

        var devanagariCount = 0
        var gurmukhiCount = 0
        var teluguCount = 0
        var tamilCount = 0
        var bengaliCount = 0
        var gujaratiCount = 0
        var kannadaCount = 0
        var malayalamCount = 0
        var arabicCount = 0

        for (ch in query) {
            when (ch) {
                in DEVANAGARI_RANGE -> devanagariCount++
                in GURMUKHI_RANGE -> gurmukhiCount++
                in TELUGU_RANGE -> teluguCount++
                in TAMIL_RANGE -> tamilCount++
                in BENGALI_RANGE -> bengaliCount++
                in GUJARATI_RANGE -> gujaratiCount++
                in KANNADA_RANGE -> kannadaCount++
                in MALAYALAM_RANGE -> malayalamCount++
                in ARABIC_URDU_RANGE -> arabicCount++
            }
        }

        if (devanagariCount > 0) return DetectionResult("hi", "Devanagari", isDevanagari = true, isIndicScript = true)
        if (gurmukhiCount > 0) return DetectionResult("pa", "Gurmukhi", isDevanagari = false, isIndicScript = true)
        if (teluguCount > 0) return DetectionResult("te", "Telugu", isDevanagari = false, isIndicScript = true)
        if (tamilCount > 0) return DetectionResult("ta", "Tamil", isDevanagari = false, isIndicScript = true)
        if (bengaliCount > 0) return DetectionResult("bn", "Bengali", isDevanagari = false, isIndicScript = true)
        if (gujaratiCount > 0) return DetectionResult("gu", "Gujarati", isDevanagari = false, isIndicScript = true)
        if (kannadaCount > 0) return DetectionResult("kn", "Kannada", isDevanagari = false, isIndicScript = true)
        if (malayalamCount > 0) return DetectionResult("ml", "Malayalam", isDevanagari = false, isIndicScript = true)
        if (arabicCount > 0) return DetectionResult("ur", "Arabic", isDevanagari = false, isIndicScript = true)

        // Check for Hinglish / Romanized language keyword clues
        val lower = query.lowercase(Locale.ROOT)
        val lang = when {
            lower.contains("hindi") || lower.contains("bollywood") || lower.contains("gaane") || lower.contains("gaana") || lower.contains("geet") -> "hi"
            lower.contains("punjabi") || lower.contains("bhangra") -> "pa"
            lower.contains("tamil") || lower.contains("kollywood") -> "ta"
            lower.contains("telugu") || lower.contains("tollywood") -> "te"
            lower.contains("bengali") || lower.contains("bangla") -> "bn"
            lower.contains("marathi") -> "mr"
            lower.contains("gujarati") || lower.contains("garba") -> "gu"
            lower.contains("bhojpuri") -> "bho"
            lower.contains("malayalam") || lower.contains("mollywood") -> "ml"
            lower.contains("kannada") || lower.contains("sandalwood") -> "kn"
            lower.contains("urdu") || lower.contains("qawwali") || lower.contains("ghazal") -> "ur"
            else -> "en"
        }

        return DetectionResult(lang, "Latin", isDevanagari = false, isIndicScript = false)
    }
}
