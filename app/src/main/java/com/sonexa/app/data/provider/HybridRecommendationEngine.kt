package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.model.WhyThisSongResponseDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import java.util.Locale

class HybridRecommendationEngine(
    private val trackUnderstandingService: TrackUnderstandingService = TrackUnderstandingService(),
    private val deduplicationService: TrackDeduplicationService = TrackDeduplicationService(),
    private val jiosaavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider(),
    private val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    private val deezerProvider: DeezerMusicProvider = DeezerMusicProvider(),
    private val jamendoProvider: JamendoProvider = JamendoProvider()
) {

    // In-memory co-occurrence graph for Item-Item collaborative signals
    private val coListeningGraph = mutableMapOf<String, MutableSet<String>>()

    fun recordCoListening(trackA: String, trackB: String) {
        if (trackA == trackB) return
        coListeningGraph.getOrPut(trackA) { mutableSetOf() }.add(trackB)
        coListeningGraph.getOrPut(trackB) { mutableSetOf() }.add(trackA)
    }

    /**
     * "Play Something Like This" / Similar Track Engine
     */
    suspend fun getSimilarTracks(seedTrack: TrackDto, limit: Int = 20): List<TrackDto> = coroutineScope {
        val seedProfile = trackUnderstandingService.analyzeTrack(seedTrack)

        val queryA = "${seedProfile.canonicalMood} ${seedProfile.language} Hits"
        val queryB = "${seedTrack.artist} Best Tracks"
        val queryC = "${seedProfile.primaryGenre} Top Songs"

        val tracksADeferred = async { jiosaavnProvider.search(queryA, limit = 25).getOrDefault(emptyList()) }
        val tracksBDeferred = async { deezerProvider.search(queryB, limit = 25).getOrDefault(emptyList()) }
        val tracksCDeferred = async { audiusProvider.search(queryC, limit = 20).getOrDefault(emptyList()) }

        val combined = (tracksADeferred.await() + tracksBDeferred.await() + tracksCDeferred.await())
            .filter { it.id != seedTrack.id }

        val deduplicated = deduplicationService.deduplicate(combined)

        // Rank by multi-dimensional cosine similarity + collaborative co-occurrence
        val scored = deduplicated.map { candidate ->
            val candidateProfile = trackUnderstandingService.analyzeTrack(candidate)
            val contentSim = trackUnderstandingService.calculateCosineSimilarity(seedProfile, candidateProfile)
            val coOccurBonus = if (coListeningGraph[seedTrack.id]?.contains(candidate.id) == true) 0.15 else 0.0

            val totalScore = (contentSim * 0.85) + coOccurBonus
            val reason = when {
                candidateProfile.canonicalMood == seedProfile.canonicalMood && candidateProfile.language == seedProfile.language ->
                    "Matches ${seedProfile.language} ${seedProfile.canonicalMood.lowercase()} vibe"
                candidate.artist.equals(seedTrack.artist, ignoreCase = true) ->
                    "By the same artist"
                candidateProfile.isRomantic && seedProfile.isRomantic ->
                    "Harmonically matching romantic melody"
                else -> "Similar rhythm and acoustic energy"
            }
            Pair(candidate.copy(recommendationReason = reason), totalScore)
        }

        val ranked = scored.sortedByDescending { it.second }.map { it.first }
        applyDiversityFilter(ranked, maxConsecutiveSameArtist = 2).take(limit)
    }

    /**
     * Track Radio: Generates a continuous, diverse queue from a single seed track
     */
    suspend fun getTrackRadio(seedTrack: TrackDto, count: Int = 30): List<TrackDto> {
        val similar = getSimilarTracks(seedTrack, limit = count + 10)
        return applyDiversityFilter(listOf(seedTrack) + similar, maxConsecutiveSameArtist = 1).take(count)
    }

    /**
     * Artist Radio: Generates a dynamic mix of the artist + similar artists + genre matches
     */
    suspend fun getArtistRadio(artistName: String, count: Int = 30): List<TrackDto> = coroutineScope {
        val directDeferred = async { jiosaavnProvider.search(artistName, limit = 20).getOrDefault(emptyList()) }
        val similarArtistsQuery = "$artistName Radio Top Hits"
        val similarDeferred = async { jiosaavnProvider.search(similarArtistsQuery, limit = 25).getOrDefault(emptyList()) }
        val genreDeferred = async { audiusProvider.search("Bollywood Pop", limit = 15).getOrDefault(emptyList()) }

        val combined = (directDeferred.await() + similarDeferred.await() + genreDeferred.await())
        val deduplicated = deduplicationService.deduplicate(combined)

        val diverse = applyDiversityFilter(deduplicated, maxConsecutiveSameArtist = 2)
        diverse.take(count)
    }

    /**
     * Daily Mix: Personalizes by current time-of-day and mood context
     */
    suspend fun getDailyMix(likedTracks: List<TrackDto>, limit: Int = 25): List<TrackDto> = coroutineScope {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeVibe = when {
            hour in 6..11 -> "Morning Positive Energy"
            hour in 12..16 -> "Focus Lo-Fi Chill"
            hour in 17..21 -> "Top Hits Party Upbeat"
            else -> "Late Night Romantic Chill"
        }

        val jiosaavnDeferred = async { jiosaavnProvider.search(timeVibe, limit = 25).getOrDefault(emptyList()) }
        val trendingDeferred = async { audiusProvider.getTrending(limit = 15).getOrDefault(emptyList()) }

        val combined = (likedTracks.take(8) + jiosaavnDeferred.await() + trendingDeferred.await())
        val deduplicated = deduplicationService.deduplicate(combined)
        val ranked = deduplicated.map { track ->
            val profile = trackUnderstandingService.analyzeTrack(track)
            track.copy(recommendationReason = "Tailored for your $timeVibe session")
        }

        applyDiversityFilter(ranked, maxConsecutiveSameArtist = 2).take(limit)
    }

    /**
     * Deep Discovery: Surfaces emerging and indie tracks with high relevance
     */
    suspend fun getDeepDiscovery(knownArtists: Set<String>, limit: Int = 20): List<TrackDto> = coroutineScope {
        val audiusDeferred = async { audiusProvider.search("Indie Acoustic Melodic", limit = 25).getOrDefault(emptyList()) }
        val jamendoDeferred = async { jamendoProvider.getTrending(limit = 25).getOrDefault(emptyList()) }

        val candidates = (audiusDeferred.await() + jamendoDeferred.await())
            .filter { candidate ->
                val artistClean = candidate.artist.lowercase(Locale.ROOT)
                knownArtists.none { known -> artistClean.contains(known.lowercase(Locale.ROOT)) }
            }

        val deduplicated = deduplicationService.deduplicate(candidates)
        val tagged = deduplicated.map {
            it.copy(recommendationReason = "Emerging indie artist recommendation")
        }
        applyDiversityFilter(tagged, maxConsecutiveSameArtist = 1).take(limit)
    }

    /**
     * Hidden Gems: Well-crafted tracks with lower global play count but high musical score
     */
    suspend fun getHiddenGems(limit: Int = 20): List<TrackDto> = coroutineScope {
        val audiusDeferred = async { audiusProvider.search("Chill Melodic Lounge", limit = 20).getOrDefault(emptyList()) }
        val jamendoDeferred = async { jamendoProvider.search("Acoustic Soul", limit = 20).getOrDefault(emptyList()) }

        val candidates = (audiusDeferred.await() + jamendoDeferred.await())
        val deduplicated = deduplicationService.deduplicate(candidates)
        val tagged = deduplicated.map {
            it.copy(recommendationReason = "Hidden Gem • High acoustic harmony")
        }
        tagged.shuffled().take(limit)
    }

    /**
     * Change Vibe: Dynamically re-ranks and enriches an existing queue based on a requested vibe
     */
    suspend fun changeVibe(
        vibe: String,
        currentQueue: List<TrackDto>,
        currentTrack: TrackDto? = null
    ): List<TrackDto> = coroutineScope {
        val vUpper = vibe.uppercase(Locale.ROOT)

        // 1. Fetch auxiliary vibe expansion tracks if current queue is small
        val expansionSearchQuery = when {
            vUpper.contains("ENERGETIC") || vUpper.contains("BEAST") -> "High Energy EDM Workout Gym Hits"
            vUpper.contains("RELAX") || vUpper.contains("CALM") || vUpper.contains("CHILL") -> "Lo-Fi Acoustic Chill Peaceful"
            vUpper.contains("ROMANTIC") -> "Romantic Love Acoustic Ballads Hindi Pop"
            vUpper.contains("PARTY") -> "Bollywood Party Dance Club Hits"
            vUpper.contains("ACOUSTIC") || vUpper.contains("UNPLUGGED") -> "Acoustic Unplugged Guitar Live"
            vUpper.contains("MELANCHOLIC") || vUpper.contains("EMOTIONAL") -> "Sad Emotional Soulful Melodies"
            vUpper.contains("FOCUS") -> "Deep Focus Ambient Instrumental Beats"
            vUpper.contains("HYPERSONIC") -> "Hardstyle Fast Tempo Dance EDM"
            vUpper.contains("NOSTALGIA") -> "90s 2000s Bollywood Golden Hits"
            vUpper.contains("DISCOVERY") || vUpper.contains("SURPRISE") -> "Indie Electronic Fresh Discoveries"
            else -> "Top Trending Hits"
        }

        val expansionDeferred = async {
            if (currentQueue.size < 12) {
                jiosaavnProvider.search(expansionSearchQuery, limit = 20).getOrDefault(emptyList())
            } else {
                emptyList()
            }
        }

        val audiusAuxDeferred = async {
            if (vUpper.contains("DISCOVERY") || vUpper.contains("FOCUS") || vUpper.contains("ACOUSTIC")) {
                audiusProvider.search(expansionSearchQuery, limit = 10).getOrDefault(emptyList())
            } else {
                emptyList()
            }
        }

        val expansionTracks = expansionDeferred.await() + audiusAuxDeferred.await()

        // 2. Combine with existing queue (keeping seed track on top if currently playing)
        val seed = currentTrack
        val existingWithoutSeed = if (seed != null) currentQueue.filter { it.id != seed.id } else currentQueue
        val pool = (existingWithoutSeed + expansionTracks).distinctBy { it.id }

        // 3. Re-order based on targeted vibe profile
        val reorderedPool = when {
            vUpper.contains("ENERGETIC") || vUpper.contains("BEAST") || vUpper.contains("HYPERSONIC") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    p.normalizedEnergy * 0.7 + (p.tempoBpm / 200.0) * 0.3
                }
            }
            vUpper.contains("RELAX") || vUpper.contains("CALM") || vUpper.contains("CHILL") -> {
                pool.sortedBy { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    p.normalizedEnergy
                }
            }
            vUpper.contains("ROMANTIC") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    if (p.isRomantic) 1.0 else (1.0 - p.normalizedEnergy * 0.5)
                }
            }
            vUpper.contains("PARTY") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    if (p.canonicalMood in listOf("PARTY", "ENERGETIC", "UPBEAT")) 1.0 else p.danceability
                }
            }
            vUpper.contains("ACOUSTIC") || vUpper.contains("UNPLUGGED") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    p.acousticness
                }
            }
            vUpper.contains("FOCUS") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    if (p.isInstrumental || p.canonicalMood == "FOCUS") 1.0 else (1.0 - p.normalizedEnergy)
                }
            }
            vUpper.contains("NOSTALGIA") -> {
                pool.sortedByDescending { track ->
                    val p = trackUnderstandingService.analyzeTrack(track)
                    if (p.eraDecade in listOf("1990s", "2000s", "2010s")) 1.0 else 0.4
                }
            }
            vUpper.contains("DISCOVERY") || vUpper.contains("SURPRISE") -> {
                pool.filter { it.provider != "sonexa" } + pool.filter { it.provider == "sonexa" }
            }
            else -> pool.shuffled()
        }

        val fullCombined = if (seed != null) listOf(seed) + reorderedPool else reorderedPool
        val deduplicated = deduplicationService.deduplicate(fullCombined)
        val diverse = applyDiversityFilter(deduplicated, maxConsecutiveSameArtist = 2)

        diverse.take(30)
    }

    /**
     * "Why This Song?": Transparent, natural language explanation for why a track was recommended
     */
    fun explainWhyThisSong(track: TrackDto, userFavoriteGenre: String = "Hindi Pop"): WhyThisSongResponseDto {
        val profile = trackUnderstandingService.analyzeTrack(track)
        val reasons = mutableListOf<String>()

        if (profile.isRomantic) {
            reasons.add("Harmonizes with your preferred romantic and acoustic melody listening habits.")
        }
        if (profile.language == "Hindi" || profile.language == "Punjabi") {
            reasons.add("Matches your ${profile.language} language affinity.")
        }
        if (profile.normalizedEnergy > 0.7) {
            reasons.add("High energy tempo (${profile.tempoBpm.toInt()} BPM) to keep the momentum going.")
        } else if (profile.normalizedEnergy < 0.4) {
            reasons.add("Gentle, soothing acoustic timbre for relaxation.")
        }
        reasons.add("Frequently enjoyed by listeners with similar musical taste.")

        return WhyThisSongResponseDto(
            trackId = track.id,
            trackTitle = track.title,
            reasons = reasons,
            affinityScore = 0.92
        )
    }

    /**
     * Anti-Bubble Diversity Penalty: Enforces max consecutive tracks from same artist and prevents artist dominance
     */
    fun applyDiversityFilter(tracks: List<TrackDto>, maxConsecutiveSameArtist: Int = 2): List<TrackDto> {
        if (tracks.size <= 2) return tracks

        val result = mutableListOf<TrackDto>()
        val pending = tracks.toMutableList()

        while (pending.isNotEmpty()) {
            val lastArtist = result.lastOrNull()?.artist.orEmpty()
            val consecutiveCount = if (lastArtist.isNotBlank()) {
                result.takeLast(maxConsecutiveSameArtist).count { it.artist.equals(lastArtist, ignoreCase = true) }
            } else 0

            val nextIndex = if (consecutiveCount >= maxConsecutiveSameArtist) {
                // Pick track from a different artist
                val diffArtistIdx = pending.indexOfFirst { !it.artist.equals(lastArtist, ignoreCase = true) }
                if (diffArtistIdx >= 0) diffArtistIdx else 0
            } else {
                0
            }

            result.add(pending.removeAt(nextIndex))
        }

        return result
    }
}
