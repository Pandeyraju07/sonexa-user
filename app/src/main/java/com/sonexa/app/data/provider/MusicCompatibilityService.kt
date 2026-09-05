package com.sonexa.app.data.provider

import com.sonexa.app.data.model.MusicCompatibilityResult
import com.sonexa.app.data.model.TrackDto

class MusicCompatibilityService(
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) {

    suspend fun calculateCompatibility(
        userAName: String = "You",
        userBName: String = "Friend",
        userAGenres: List<String> = listOf("Bollywood", "Acoustic", "Indie", "Pop"),
        userBGenres: List<String> = listOf("Bollywood", "Indie", "EDM", "Rock")
    ): MusicCompatibilityResult {
        val shared = userAGenres.intersect(userBGenres.toSet()).toList()
        val score = (((shared.size.toDouble() / (userAGenres.size + userBGenres.size - shared.size).coerceAtLeast(1)) * 50) + 50).toInt().coerceIn(60, 98)

        val discoveryQuery = "${shared.joinToString(" ")} Emerging Acoustic Indie"
        val discoveryTracks = aggregationEngine.searchAll(discoveryQuery).tracks.take(5)

        return MusicCompatibilityResult(
            userNameA = userAName,
            userNameB = userBName,
            matchPercentage = score,
            sharedGenres = shared.ifEmpty { listOf("Bollywood Melodic", "Acoustic Pop") },
            sharedArtists = listOf("Arijit Singh", "Anuv Jain", "Prateek Kuhad"),
            tasteDivergence = "$userAName leans 70% toward acoustic discovery, while $userBName prefers energetic chartbusters.",
            mutualDiscoveryTracks = discoveryTracks
        )
    }
}
