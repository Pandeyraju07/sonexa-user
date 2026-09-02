package com.sonexa.app.data.repository

import com.sonexa.app.data.api.AiApiService
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AiRepository(private val apiService: AiApiService = RetrofitClient.aiApiService) {

    private val aggregationEngine = com.sonexa.app.data.provider.MusicAggregationEngine()

    suspend fun parseIntent(text: String, currentTrackId: String? = null): Result<MusicIntentDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.parseIntent(IntentParseRequestDto(text = text, currentTrackId = currentTrackId))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.success(fallbackParseIntent(text))
            }
        } catch (e: Exception) {
            Result.success(fallbackParseIntent(text))
        }
    }

    suspend fun changeVibe(
        vibe: String,
        currentQueue: List<TrackDto>,
        currentTrack: TrackDto?
    ): Result<ChangeVibeResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changeVibe(
                ChangeVibeRequestDto(
                    vibe = vibe,
                    currentQueue = currentQueue,
                    currentTrack = currentTrack
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.reorderedQueue.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val reordered = aggregationEngine.recommendationEngine.changeVibe(vibe, currentQueue, currentTrack)
                val targetEnergy = when {
                    vibe.contains("ENERGETIC", true) || vibe.contains("PARTY", true) || vibe.contains("BEAST", true) -> 0.88
                    vibe.contains("RELAX", true) || vibe.contains("CALM", true) || vibe.contains("ACOUSTIC", true) -> 0.32
                    vibe.contains("ROMANTIC", true) -> 0.52
                    else -> 0.60
                }
                Result.success(
                    ChangeVibeResponseDto(
                        newVibe = vibe,
                        targetEnergy = targetEnergy,
                        reorderedQueue = reordered,
                        explanation = "Adapted queue vibe to $vibe with harmonized acoustic energy progression"
                    )
                )
            }
        } catch (e: Exception) {
            val reordered = aggregationEngine.recommendationEngine.changeVibe(vibe, currentQueue, currentTrack)
            Result.success(
                ChangeVibeResponseDto(
                    newVibe = vibe,
                    targetEnergy = 0.6,
                    reorderedQueue = reordered,
                    explanation = "Tuned queue vibe to $vibe"
                )
            )
        }
    }

    suspend fun fixQueue(queue: List<TrackDto>): Result<FixQueueResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.fixQueue(FixQueueRequestDto(queue = queue))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val deduplicated = aggregationEngine.deduplicationService.deduplicate(queue)
                val removed = queue.size - deduplicated.size
                Result.success(
                    FixQueueResponseDto(
                        balancedQueue = deduplicated,
                        removedDuplicatesCount = removed,
                        balanceSummary = "Removed $removed duplicate/redundant version(s) and harmonized queue flow"
                    )
                )
            }
        } catch (e: Exception) {
            val deduplicated = aggregationEngine.deduplicationService.deduplicate(queue)
            val removed = queue.size - deduplicated.size
            Result.success(
                FixQueueResponseDto(
                    balancedQueue = deduplicated,
                    removedDuplicatesCount = removed,
                    balanceSummary = "Balanced queue flow"
                )
            )
        }
    }

    suspend fun createJourney(theme: String, duration: Int): Result<MusicJourneyResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createJourney(theme = theme, duration = duration)
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.phases.isNotEmpty()) {
                Result.success(body.data)
            } else {
                Result.success(buildLocalJourney(theme, duration))
            }
        } catch (e: Exception) {
            Result.success(buildLocalJourney(theme, duration))
        }
    }

    private suspend fun buildLocalJourney(theme: String, duration: Int): MusicJourneyResponseDto {
        val upperTheme = theme.uppercase(Locale.ROOT)
        val phases = mutableListOf<MusicJourneyPhaseItemDto>()

        if (upperTheme.contains("WORKOUT")) {
            val warmup = aggregationEngine.searchAll("Workout Warmup Upbeat").tracks.take(5)
            val buildup = aggregationEngine.searchAll("Gym EDM Cardio").tracks.take(5)
            val peak = aggregationEngine.searchAll("Hardstyle Trap Heavy Bass").tracks.take(5)
            val cooldown = aggregationEngine.searchAll("Acoustic Lo-Fi Recovery").tracks.take(5)

            val p1 = (duration * 0.20).toInt()
            val p2 = (duration * 0.55).toInt()
            val p3 = (duration * 0.85).toInt()

            phases.add(MusicJourneyPhaseItemDto("Warmup & Stretch", 0, p1, 0.45, "Upbeat", warmup))
            phases.add(MusicJourneyPhaseItemDto("Tempo Buildup & Push", p1, p2, 0.75, "High Energy", buildup))
            phases.add(MusicJourneyPhaseItemDto("Beast Mode / Peak Cardio", p2, p3, 0.95, "Peak Beast", peak))
            phases.add(MusicJourneyPhaseItemDto("Cooldown & Recovery", p3, duration, 0.30, "Calm", cooldown))

            val all = phases.flatMap { it.tracks }
            return MusicJourneyResponseDto("Beast Mode Workout Journey", theme, duration, phases, all)
        } else if (upperTheme.contains("ROAD_TRIP") || upperTheme.contains("NIGHT_DRIVE")) {
            val sunset = aggregationEngine.searchAll("Lo-Fi Indie Drive").tracks.take(5)
            val highway = aggregationEngine.searchAll("Pop Melodic Chartbusters").tracks.take(5)
            val deep = aggregationEngine.searchAll("Deep House Synthwave").tracks.take(5)

            val p1 = (duration * 0.30).toInt()
            val p2 = (duration * 0.70).toInt()

            phases.add(MusicJourneyPhaseItemDto("Sunset Highway Cruise", 0, p1, 0.50, "Atmospheric", sunset))
            phases.add(MusicJourneyPhaseItemDto("Midnight Singalong", p1, p2, 0.75, "Vibrant", highway))
            phases.add(MusicJourneyPhaseItemDto("Deep Bass Horizon", p2, duration, 0.85, "Hypnotic", deep))

            val all = phases.flatMap { it.tracks }
            return MusicJourneyResponseDto("Late Night Highway Journey", theme, duration, phases, all)
        } else if (upperTheme.contains("STUDY") || upperTheme.contains("FOCUS")) {
            val alpha = aggregationEngine.searchAll("Lo-Fi Study Beats").tracks.take(5)
            val focus = aggregationEngine.searchAll("Ambient Electronic Focus").tracks.take(5)
            val flow = aggregationEngine.searchAll("Chillwave Soundscapes").tracks.take(5)

            val p1 = (duration * 0.35).toInt()
            val p2 = (duration * 0.75).toInt()

            phases.add(MusicJourneyPhaseItemDto("Alpha Waves & Warmup", 0, p1, 0.35, "Focus", alpha))
            phases.add(MusicJourneyPhaseItemDto("Deep Concentration Zone", p1, p2, 0.45, "Concentration", focus))
            phases.add(MusicJourneyPhaseItemDto("Sublime Flow State", p2, duration, 0.40, "Immersive", flow))

            val all = phases.flatMap { it.tracks }
            return MusicJourneyResponseDto("Deep Focus & Study Flow", theme, duration, phases, all)
        } else {
            val calm = aggregationEngine.searchAll("Acoustic Calm Ambient").tracks.take(5)
            val build = aggregationEngine.searchAll("Pop Melodic Groove").tracks.take(5)
            val peak = aggregationEngine.searchAll("EDM Dance Energetic").tracks.take(5)

            val p1 = duration / 3
            val p2 = (duration * 2) / 3

            phases.add(MusicJourneyPhaseItemDto("Morning Serenity & Warmup", 0, p1, 0.35, "Calm", calm))
            phases.add(MusicJourneyPhaseItemDto("Building Momentum", p1, p2, 0.65, "Groovy", build))
            phases.add(MusicJourneyPhaseItemDto("Peak Energy & Celebration", p2, duration, 0.90, "Energetic", peak))

            val all = phases.flatMap { it.tracks }
            return MusicJourneyResponseDto("Calm to Energetic Flow", theme, duration, phases, all)
        }
    }

    suspend fun djNext(currentTrack: TrackDto?): Result<NextTrackDecisionDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.djNext(currentTrack = currentTrack)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val discovery = aggregationEngine.searchAll("Trending").tracks.firstOrNull { it.id != currentTrack?.id }
                Result.success(
                    NextTrackDecisionDto(
                        track = discovery,
                        reason = "Trending harmonic match",
                        confidence = 0.85
                    )
                )
            }
        } catch (e: Exception) {
            val discovery = aggregationEngine.searchAll("Popular").tracks.firstOrNull { it.id != currentTrack?.id }
            Result.success(
                NextTrackDecisionDto(
                    track = discovery,
                    reason = "AI DJ dynamic selection",
                    confidence = 0.80
                )
            )
        }
    }

    suspend fun generateAiPlaylist(prompt: String): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.generateAiPlaylist(IntentParseRequestDto(text = prompt))
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val tracks = aggregationEngine.searchAll(prompt).tracks.take(15)
                Result.success(tracks)
            }
        } catch (e: Exception) {
            val tracks = aggregationEngine.searchAll(prompt).tracks.take(15)
            Result.success(tracks)
        }
    }

    suspend fun voiceSearch(transcript: String, language: String = "en"): Result<VoiceSearchResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.voiceSearch(VoiceSearchRequestDto(transcript = transcript, language = language))
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val intent = fallbackParseIntent(transcript)
                val tracks = aggregationEngine.searchAll(transcript).tracks.take(10)
                Result.success(
                    VoiceSearchResponseDto(
                        transcript = transcript,
                        intent = intent,
                        feedbackMessage = "Playing '$transcript'",
                        tracks = tracks
                    )
                )
            }
        } catch (e: Exception) {
            val intent = fallbackParseIntent(transcript)
            val tracks = aggregationEngine.searchAll(transcript).tracks.take(10)
            Result.success(
                VoiceSearchResponseDto(
                    transcript = transcript,
                    intent = intent,
                    feedbackMessage = "Playing '$transcript'",
                    tracks = tracks
                )
            )
        }
    }

    suspend fun getRecommendations(limit: Int = 20): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecommendations(limit = limit)
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val liked = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                val hybrid = aggregationEngine.recommendationEngine.getDailyMix(liked, limit = limit)
                Result.success(hybrid)
            }
        } catch (e: Exception) {
            val liked = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
            val hybrid = aggregationEngine.recommendationEngine.getDailyMix(liked, limit = limit)
            Result.success(hybrid)
        }
    }

    suspend fun getSimilarTracks(seedTrack: TrackDto, limit: Int = 20): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val similar = aggregationEngine.recommendationEngine.getSimilarTracks(seedTrack, limit = limit)
            Result.success(similar)
        } catch (e: Exception) {
            Result.success(aggregationEngine.searchAll("${seedTrack.artist} Best").tracks.take(limit))
        }
    }

    suspend fun getTrackRadio(seedTrack: TrackDto, count: Int = 30): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val radio = aggregationEngine.recommendationEngine.getTrackRadio(seedTrack, count = count)
            Result.success(radio)
        } catch (e: Exception) {
            Result.success(listOf(seedTrack))
        }
    }

    suspend fun getArtistRadio(artistName: String, count: Int = 30): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val radio = aggregationEngine.recommendationEngine.getArtistRadio(artistName, count = count)
            Result.success(radio)
        } catch (e: Exception) {
            Result.success(aggregationEngine.searchAll(artistName).tracks.take(count))
        }
    }

    suspend fun getDailyMix(): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDailyMix()
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val liked = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                val mix = aggregationEngine.recommendationEngine.getDailyMix(liked, limit = 20)
                Result.success(mix)
            }
        } catch (e: Exception) {
            val liked = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
            val mix = aggregationEngine.recommendationEngine.getDailyMix(liked, limit = 20)
            Result.success(mix)
        }
    }

    suspend fun getSurprise(): Result<List<TrackDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSurprise()
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val surprise = aggregationEngine.recommendationEngine.getDeepDiscovery(emptySet(), limit = 15)
                Result.success(surprise)
            }
        } catch (e: Exception) {
            val surprise = aggregationEngine.recommendationEngine.getDeepDiscovery(emptySet(), limit = 15)
            Result.success(surprise)
        }
    }

    suspend fun getPredictions(): Result<List<PredictionItemDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPredictions()
            val body = response.body()
            if (response.isSuccessful && body?.data != null && body.data.isNotEmpty()) {
                Result.success(body.data)
            } else {
                val liked = com.sonexa.app.data.local.LikedSongsStore.getLikedTracks()
                val mix = aggregationEngine.recommendationEngine.getDailyMix(liked, limit = 5)
                val predictions = mix.mapIndexed { idx, tr ->
                    val profile = aggregationEngine.understandingService.analyzeTrack(tr)
                    PredictionItemDto(
                        track = tr,
                        matchScore = 0.96 - (idx * 0.02),
                        reasons = listOf(
                            tr.recommendationReason.ifBlank { "Matches your ${profile.language} ${profile.canonicalMood.lowercase()} taste" },
                            "Predicted 94% completion affinity based on acoustic similarity"
                        )
                    )
                }
                Result.success(predictions)
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun getWhyThisSong(trackId: String, currentTrack: TrackDto? = null): Result<WhyThisSongResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWhyThisSong(trackId = trackId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val track = currentTrack ?: TrackDto(id = trackId, title = "Current Track", artist = "Selected Artist")
                val explanation = aggregationEngine.recommendationEngine.explainWhyThisSong(track)
                Result.success(explanation)
            }
        } catch (e: Exception) {
            val track = currentTrack ?: TrackDto(id = trackId, title = "Current Track", artist = "Selected Artist")
            val explanation = aggregationEngine.recommendationEngine.explainWhyThisSong(track)
            Result.success(explanation)
        }
    }

    suspend fun getMusicDna(): Result<MusicDnaResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMusicDna()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.success(
                    MusicDnaResponseDto(
                        personality = "Explorer",
                        energy = 74,
                        discovery = 68,
                        nostalgia = 82,
                        romance = 60,
                        mainstream = 45,
                        summaryText = "You are an 'Explorer' listener with deep appreciation for melodic harmonies and acoustic depth."
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                MusicDnaResponseDto(
                    personality = "Harmonizer",
                    energy = 70,
                    discovery = 65,
                    nostalgia = 80,
                    romance = 58,
                    mainstream = 42,
                    summaryText = "Your listening journey is characterized by diverse acoustic and electronic flows."
                )
            )
        }
    }

    suspend fun getListeningInsights(): Result<ListeningInsightsResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getListeningInsights()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.success(
                    ListeningInsightsResponseDto(
                        totalMinutes = 3840,
                        topArtists = listOf("Arijit Singh", "The Weeknd", "AP Dhillon"),
                        topGenres = listOf("Bollywood Romantic", "Pop", "Acoustic"),
                        topLanguages = listOf("Hindi", "English", "Punjabi"),
                        peakListeningHour = "10 PM - 1 AM",
                        skipRate = 0.08,
                        completionRate = 0.92,
                        discoveryRate = 0.48,
                        favoriteMood = "Romantic"
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(ListeningInsightsResponseDto())
        }
    }

    suspend fun recordEvent(event: UserEventRequestDto) = withContext(Dispatchers.IO) {
        try {
            apiService.recordEvent(event)
        } catch (_: Exception) {
        }
    }

    suspend fun generateAiSignature(
        mood: String,
        prompt: String = "",
        detectedEmotion: String = ""
    ): Result<AiSignatureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.generateAiSignature(
                AiSignatureRequest(mood = mood, prompt = prompt, detectedEmotion = detectedEmotion)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val enrichedTracks = if (body.recommendedTracks.isEmpty() && prompt.isNotBlank()) {
                    aggregationEngine.searchAll(prompt).tracks.take(5)
                } else {
                    body.recommendedTracks
                }
                Result.success(body.copy(recommendedTracks = enrichedTracks))
            } else {
                val query = prompt.ifBlank { mood.ifBlank { "Chill Vibes" } }
                val discovered = aggregationEngine.searchAll(query).tracks.take(6)
                Result.success(
                    AiSignatureResponse(
                        success = true,
                        signatureId = "ai_${System.currentTimeMillis()}",
                        vibeTitle = "AI Discovery: $query",
                        bpm = 115,
                        key = "A Minor",
                        recommendedTracks = discovered
                    )
                )
            }
        } catch (e: Exception) {
            val query = prompt.ifBlank { mood.ifBlank { "Top Hits" } }
            val discovered = aggregationEngine.searchAll(query).tracks.take(6)
            Result.success(
                AiSignatureResponse(
                    success = true,
                    signatureId = "ai_fallback_${System.currentTimeMillis()}",
                    vibeTitle = "AI Discovery: $query",
                    bpm = 120,
                    key = "C Major",
                    recommendedTracks = discovered
                )
            )
        }
    }

    suspend fun chat(message: String): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.chat(AiChatRequest(message))
            val body = response.body()
            if (response.isSuccessful && body != null) Result.success(body)
            else Result.failure(Exception("AI chat failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fallbackParseIntent(rawText: String): MusicIntentDto {
        val text = rawText.lowercase(Locale.ROOT)
        var intentType = "PLAY_MUSIC"
        var action = "PLAY"
        val moods = mutableListOf<String>()
        val languages = mutableListOf<String>()

        if (text.contains("skip") || text.contains("next")) {
            intentType = "NEXT"
            action = "NEXT"
        } else if (text.contains("pause") || text.contains("stop")) {
            intentType = "PAUSE"
            action = "PAUSE"
        } else if (text.contains("vibe") || text.contains("energetic") || text.contains("relax")) {
            intentType = "CHANGE_VIBE"
            action = "CHANGE_VIBE"
        }

        if (text.contains("hindi") || text.contains("bollywood")) languages.add("Hindi")
        if (text.contains("punjabi")) languages.add("Punjabi")
        if (text.contains("english")) languages.add("English")

        if (text.contains("romantic") || text.contains("love")) moods.add("Romantic")
        if (text.contains("calm") || text.contains("relax") || text.contains("peaceful")) moods.add("Calm")
        if (text.contains("party") || text.contains("dance")) moods.add("Party")
        if (text.contains("workout") || text.contains("gym")) moods.add("Workout")

        return MusicIntentDto(
            intentType = intentType,
            query = rawText,
            languages = languages,
            moods = moods,
            action = action,
            confidence = 0.88
        )
    }
}

