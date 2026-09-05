package com.sonexa.app.data.provider

import com.sonexa.app.data.model.*
import kotlin.math.abs

class EmotionalEqualizerService(
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService(),
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) {

    fun tuneQueue(
        currentQueue: List<TrackDto>,
        currentTrack: TrackDto?,
        eqState: EmotionalEqualizerState
    ): EmotionalQueueTuneResult {
        if (currentQueue.isEmpty()) {
            return EmotionalQueueTuneResult(
                tunedQueue = emptyList(),
                explanation = "Queue is empty",
                targetAcousticProfile = "Neutral"
            )
        }

        val targetEnergyNorm = (eqState.energy / 100.0).coerceIn(0.0, 1.0)
        val targetHappinessNorm = (eqState.happiness / 100.0).coerceIn(0.0, 1.0)
        val targetNostalgia = eqState.nostalgia > 60f
        val targetRomance = eqState.romance > 60f

        val nonPlaying = currentQueue.filter { it.id != currentTrack?.id }

        val scored: List<Pair<TrackDto, Double>> = nonPlaying.map { track ->
            val profile = trackUnderstandingService.analyzeTrack(track)

            val energyDist = 1.0 - abs(profile.normalizedEnergy - targetEnergyNorm)
            val valenceDist = 1.0 - abs(profile.danceability - targetHappinessNorm)
            val romanceBonus = if (targetRomance && profile.isRomantic) 0.25 else 0.0
            val nostalgiaBonus = if (targetNostalgia && (profile.eraDecade == "Retro" || profile.acousticness > 0.5)) 0.25 else 0.0

            val totalScore = (energyDist * 0.40) + (valenceDist * 0.35) + romanceBonus + nostalgiaBonus
            Pair(track, totalScore)
        }

        val reordered = scored.sortedByDescending { it.second }.map { it.first }
        val finalQueue = if (currentTrack != null) listOf(currentTrack) + reordered else reordered

        val profileSummary = buildString {
            if (eqState.energy > 70f) append("High Energy ")
            else if (eqState.energy < 35f) append("Calm & Acoustic ")
            if (eqState.romance > 60f) append("• Romantic ")
            if (eqState.nostalgia > 60f) append("• Nostalgic ")
            if (eqState.discovery > 60f) append("• High Discovery")
        }.ifBlank { "Balanced Flow" }

        return EmotionalQueueTuneResult(
            tunedQueue = finalQueue,
            explanation = "Queue tuned to $profileSummary",
            targetAcousticProfile = profileSummary
        )
    }

    suspend fun finishMySong(seedTrack: TrackDto): FinishMySongResult {
        val profile = trackUnderstandingService.analyzeTrack(seedTrack)
        val continuationQuery = when {
            profile.normalizedEnergy > 0.70 -> "${seedTrack.artist} Peak High Energy EDM Dance"
            profile.isRomantic -> "${seedTrack.artist} Soulful Climax Romantic Melodies"
            profile.acousticness > 0.55 -> "Intimate Acoustic Crescendo Harmonies"
            else -> "${profile.language} ${profile.primaryGenre} Top Tracks"
        }

        val candidates = aggregationEngine.searchAll(continuationQuery).tracks
            .filter { it.id != seedTrack.id }
            .take(5)

        return FinishMySongResult(
            seedTrack = seedTrack,
            continuationCandidates = candidates,
            matchExplanation = "Harmonically tuned to continue the crescendo and emotional resolution of '${seedTrack.title}'."
        )
    }
}
