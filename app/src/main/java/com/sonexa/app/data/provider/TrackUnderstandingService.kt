package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.model.TrackUnderstandingProfile
import java.util.Locale
import kotlin.math.sqrt

class TrackUnderstandingService {

    companion object {
        val CANONICAL_MOODS = listOf(
            "ROMANTIC", "HAPPY", "SAD", "MELANCHOLIC", "CALM",
            "RELAXING", "ENERGETIC", "PARTY", "DREAMY", "NOSTALGIC",
            "EMOTIONAL", "DARK", "MOTIVATIONAL", "FOCUS", "CHILL",
            "CONFIDENT", "AGGRESSIVE", "PEACEFUL", "SENSUAL", "UPBEAT"
        )
    }

    /**
     * Generates a rich musical profile for any track.
     */
    fun analyzeTrack(track: TrackDto): TrackUnderstandingProfile {
        val titleLower = track.title.lowercase(Locale.ROOT)
        val artistLower = track.artist.lowercase(Locale.ROOT)
        val albumLower = track.album.lowercase(Locale.ROOT)
        val allTags = (track.tags + track.genres + track.moods).map { it.lowercase(Locale.ROOT) }

        val mood = classifyCanonicalMood(titleLower, allTags, track.genre, track.mood)
        val language = detectLanguage(titleLower, artistLower, allTags)
        val isRomantic = isRomanticTrack(titleLower, allTags, mood, language)
        val energy = calculateNormalizedEnergy(track, mood, allTags)
        val tempo = if (track.bpm > 0) track.bpm else estimateTempo(energy, mood)
        val primaryGenre = track.genres.firstOrNull() ?: track.genre.ifBlank { "Pop" }
        val era = detectEra(track.album, allTags)
        val acousticness = if (track.acousticness != 0.45 && track.acousticness > 0) track.acousticness else estimateAcousticness(genre = primaryGenre, mood = mood)
        val danceability = if (track.danceability > 0) track.danceability else estimateDanceability(energy, tempo)
        val isInstrumental = track.isInstrumental || allTags.any { it.contains("instrumental") || it.contains("piano") || it.contains("ambient") }

        return TrackUnderstandingProfile(
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            primaryGenre = track.genre.ifBlank { "Pop" },
            subgenres = track.genres.ifEmpty { listOf(track.genre) },
            canonicalMood = mood,
            moods = listOf(mood, if (isRomantic) "ROMANTIC" else "CHILL").distinct(),
            normalizedEnergy = energy,
            language = language,
            eraDecade = era,
            tempoBpm = tempo,
            isRomantic = isRomantic,
            acousticness = acousticness,
            danceability = danceability,
            isInstrumental = isInstrumental,
            tags = allTags,
            confidence = 0.92
        )
    }

    /**
     * Maps provider metadata & tags into 20 Canonical Moods.
     */
    fun classifyCanonicalMood(title: String, tags: List<String>, genre: String, rawMood: String): String {
        val combined = (tags + listOf(title, genre, rawMood)).joinToString(" ").lowercase(Locale.ROOT)

        return when {
            combined.contains("romantic") || combined.contains("love") || combined.contains("ishq") ||
                    combined.contains("dil") || combined.contains("pyaar") || combined.contains("mohabbat") ||
                    combined.contains("sensual") || combined.contains("ballad") -> "ROMANTIC"

            combined.contains("party") || combined.contains("club") || combined.contains("dance") ||
                    combined.contains("dhol") || combined.contains("bhangra") || combined.contains("edm") -> "PARTY"

            combined.contains("sad") || combined.contains("dard") || combined.contains("heartbreak") ||
                    combined.contains("judaai") || combined.contains("tujhe") || combined.contains("crying") -> "SAD"

            combined.contains("melanchol") || combined.contains("lonely") || combined.contains("grief") -> "MELANCHOLIC"

            combined.contains("gym") || combined.contains("workout") || combined.contains("energetic") ||
                    combined.contains("power") || combined.contains("hard") || combined.contains("trap") -> "ENERGETIC"

            combined.contains("lo-fi") || combined.contains("lofi") || combined.contains("chill") ||
                    combined.contains("slowed") || combined.contains("reverb") || combined.contains("lounge") -> "CHILL"

            combined.contains("calm") || combined.contains("peaceful") || combined.contains("meditation") ||
                    combined.contains("sleep") || combined.contains("spa") -> "PEACEFUL"

            combined.contains("relax") || combined.contains("soft") || combined.contains("acoustic") -> "RELAXING"

            combined.contains("focus") || combined.contains("study") || combined.contains("ambient") ||
                    combined.contains("coding") || combined.contains("instrumental") -> "FOCUS"

            combined.contains("happy") || combined.contains("joy") || combined.contains("sunshine") ||
                    combined.contains("smile") || combined.contains("cheerful") -> "HAPPY"

            combined.contains("motivat") || combined.contains("inspirational") || combined.contains("dream") ||
                    combined.contains("triumph") -> "MOTIVATIONAL"

            combined.contains("nostalg") || combined.contains("retro") || combined.contains("classic") ||
                    combined.contains("90s") || combined.contains("80s") || combined.contains("golden") -> "NOSTALGIC"

            combined.contains("dark") || combined.contains("shadow") || combined.contains("gothic") -> "DARK"

            combined.contains("upbeat") || combined.contains("groove") || combined.contains("pop") -> "UPBEAT"

            else -> "CHILL"
        }
    }

    /**
     * Multi-signal Romantic song detector.
     */
    fun isRomanticTrack(title: String, tags: List<String>, canonicalMood: String, language: String): Boolean {
        if (canonicalMood == "ROMANTIC") return true

        val combined = (tags + listOf(title)).joinToString(" ").lowercase(Locale.ROOT)
        val romanticKeywords = listOf(
            "love", "ishq", "pyaar", "mohabbat", "dil", "humsafar", "jaan", "romantic",
            "sanam", "deewana", "pehla", "forever", "valentine", "heart", "romance", "sweetheart", "kiss"
        )
        val matches = romanticKeywords.count { combined.contains(it) }
        return matches >= 1
    }

    /**
     * Energy score normalization between 0.0 (very calm) and 1.0 (extremely energetic).
     */
    fun calculateNormalizedEnergy(track: TrackDto, mood: String, tags: List<String>): Double {
        if (track.energy in 0.05..0.98) return track.energy

        return when (mood) {
            "ENERGETIC", "PARTY", "AGGRESSIVE" -> 0.88
            "UPBEAT", "CONFIDENT", "MOTIVATIONAL" -> 0.72
            "HAPPY", "DREAMY" -> 0.60
            "ROMANTIC", "SENSUAL", "NOSTALGIC" -> 0.52
            "CHILL", "EMOTIONAL", "SAD", "MELANCHOLIC" -> 0.40
            "RELAXING", "CALM", "PEACEFUL", "FOCUS" -> 0.28
            else -> 0.55
        }
    }

    /**
     * Detects language using keyword tokens and artist origin signatures.
     */
    fun detectLanguage(title: String, artist: String, tags: List<String>): String {
        val combined = (tags + listOf(title, artist)).joinToString(" ").lowercase(Locale.ROOT)

        return when {
            combined.contains("hindi") || combined.contains("bollywood") || combined.contains("arijit") ||
                    combined.contains("pritam") || combined.contains("shreya") || combined.contains("ishq") ||
                    combined.contains("dil") || combined.contains("pyaar") || combined.contains("tera") -> "Hindi"

            combined.contains("punjabi") || combined.contains("diljit") || combined.contains("sidhu") ||
                    combined.contains("karan aujla") || combined.contains("bhangra") || combined.contains("jatt") -> "Punjabi"

            combined.contains("tamil") || combined.contains("anirudh") || combined.contains("ar rahman") ||
                    combined.contains("kollywood") -> "Tamil"

            combined.contains("telugu") || combined.contains("tollywood") || combined.contains("dsp") -> "Telugu"

            combined.contains("spanish") || combined.contains("latino") || combined.contains("reggaeton") -> "Spanish"

            else -> "English"
        }
    }

    private fun detectEra(album: String, tags: List<String>): String {
        val combined = (tags + listOf(album)).joinToString(" ")
        return when {
            combined.contains("2024") || combined.contains("2023") || combined.contains("2022") || combined.contains("2021") || combined.contains("2020") -> "2020s"
            combined.contains("201") -> "2010s"
            combined.contains("200") -> "2000s"
            combined.contains("199") -> "1990s"
            else -> "2020s"
        }
    }

    private fun estimateTempo(energy: Double, mood: String): Double {
        return (70.0 + (energy * 90.0)).coerceIn(60.0, 180.0)
    }

    private fun estimateAcousticness(genre: String, mood: String): Double {
        return when {
            mood in listOf("RELAXING", "CALM", "PEACEFUL", "ROMANTIC") -> 0.70
            genre.contains("Acoustic", true) || genre.contains("Folk", true) || genre.contains("Classical", true) -> 0.85
            genre.contains("EDM", true) || genre.contains("Electronic", true) -> 0.10
            else -> 0.45
        }
    }

    private fun estimateDanceability(energy: Double, tempo: Double): Double {
        val tempoScore = if (tempo in 100.0..130.0) 0.9 else 0.6
        return ((energy * 0.5) + (tempoScore * 0.5)).coerceIn(0.2, 0.95)
    }

    /**
     * Computes multi-dimensional cosine similarity between two track profiles.
     */
    fun calculateCosineSimilarity(p1: TrackUnderstandingProfile, p2: TrackUnderstandingProfile): Double {
        // Feature vector: [genreMatch, moodMatch, energyDiff, tempoDiff, languageMatch, eraMatch, acousticnessDiff, romanticMatch]
        val genreWeight = if (p1.primaryGenre.equals(p2.primaryGenre, ignoreCase = true)) 1.0 else 0.4
        val moodWeight = if (p1.canonicalMood.equals(p2.canonicalMood, ignoreCase = true)) 1.0 else 0.3
        val energySim = 1.0 - kotlin.math.abs(p1.normalizedEnergy - p2.normalizedEnergy).coerceIn(0.0, 1.0)
        val tempoSim = 1.0 - (kotlin.math.abs(p1.tempoBpm - p2.tempoBpm) / 120.0).coerceIn(0.0, 1.0)
        val languageSim = if (p1.language.equals(p2.language, ignoreCase = true)) 1.0 else 0.2
        val romanticSim = if (p1.isRomantic == p2.isRomantic) 1.0 else 0.4
        val acousticSim = 1.0 - kotlin.math.abs(p1.acousticness - p2.acousticness).coerceIn(0.0, 1.0)

        val weightedSum = (0.25 * moodWeight) +
                (0.20 * genreWeight) +
                (0.15 * energySim) +
                (0.15 * languageSim) +
                (0.10 * romanticSim) +
                (0.08 * tempoSim) +
                (0.07 * acousticSim)

        return weightedSum.coerceIn(0.0, 1.0)
    }
}
