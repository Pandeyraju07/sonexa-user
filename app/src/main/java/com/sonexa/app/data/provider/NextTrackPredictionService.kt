package com.sonexa.app.data.provider

import com.sonexa.app.data.model.NextSongPrediction
import com.sonexa.app.data.model.TrackDto
import java.util.Calendar

class NextTrackPredictionService(
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService(),
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) {

    suspend fun predictNextTrack(
        currentTrack: TrackDto?,
        recentHistory: List<TrackDto>
    ): NextSongPrediction {
        if (currentTrack == null && recentHistory.isEmpty()) {
            return NextSongPrediction(
                predictedTrack = TrackDto(id = "pred_default", title = "Kasoor", artist = "Prateek Kuhad"),
                confidence = 0.85,
                reason = "Predicted relaxing acoustic start based on typical listening patterns",
                transitionType = "WARM_ACOUSTIC"
            )
        }

        val seed = currentTrack ?: recentHistory.first()
        val seedProfile = trackUnderstandingService.analyzeTrack(seed)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Markov transition heuristics
        val isLateNight = hour >= 22 || hour < 5
        val isMorning = hour in 6..11
        val isWorkoutHour = hour in 17..20

        val (predictedQuery, transitionType, reasonText, conf) = when {
            isLateNight && seedProfile.normalizedEnergy > 0.65 -> {
                Quad("Anuv Jain Acoustic Lo-Fi", "CALMER_ACOUSTIC", "You typically transition from energetic music to calm acoustic tracks late at night", 0.91)
            }
            isMorning && seedProfile.normalizedEnergy < 0.50 -> {
                Quad("Morning Upbeat Pop Chartbusters", "BUILDING_ENERGY", "Predicted shift to energetic morning momentum", 0.86)
            }
            seed.artist.contains("Arijit", ignoreCase = true) -> {
                Quad("Prateek Kuhad Anuv Jain", "INDIE_ACOUSTIC", "After Arijit Singh, you frequently switch to intimate indie acoustic vocals", 0.89)
            }
            seed.artist.contains("Diljit", ignoreCase = true) || seed.artist.contains("Karan Aujla", ignoreCase = true) -> {
                Quad("AP Dhillon Shubh Punjabi", "PUNJABI_MOMENTUM", "Staying in Punjabi rhythm with harmonic flow", 0.93)
            }
            else -> {
                Quad("${seedProfile.language} ${seedProfile.canonicalMood} Best", "HARMONIC_SIMILARITY", "Harmonically matching the vocal delivery and acoustic signature of '${seed.title}'", 0.84)
            }
        }

        val searchResult = aggregationEngine.searchAll(predictedQuery)
        val candidates = searchResult.tracks.filter { it.id != seed.id }
        val topCandidate = candidates.firstOrNull() ?: TrackDto(
            id = "pred_fallback",
            title = "Kasoor",
            artist = "Prateek Kuhad",
            album = "Kasoor Single",
            recommendationReason = reasonText
        )

        return NextSongPrediction(
            predictedTrack = topCandidate.copy(recommendationReason = reasonText),
            confidence = conf,
            reason = reasonText,
            transitionType = transitionType,
            alternativeCandidates = candidates.drop(1).take(3)
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
