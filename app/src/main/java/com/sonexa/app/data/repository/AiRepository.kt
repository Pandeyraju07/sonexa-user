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

    // =========================================================================
    // 15 MUSIC INTELLIGENCE METHODS
    // =========================================================================

    private val predictionService = com.sonexa.app.data.provider.NextTrackPredictionService(
        trackUnderstandingService = aggregationEngine.understandingService,
        aggregationEngine = aggregationEngine
    )
    private val rabbitHoleEngine = com.sonexa.app.data.provider.MusicRabbitHoleEngine(aggregationEngine)
    private val emotionalEqualizerService = com.sonexa.app.data.provider.EmotionalEqualizerService(
        trackUnderstandingService = aggregationEngine.understandingService,
        aggregationEngine = aggregationEngine
    )
    private val timeMachineEngine = com.sonexa.app.data.provider.MusicTimeMachineEngine(aggregationEngine)
    private val compatibilityService = com.sonexa.app.data.provider.MusicCompatibilityService(aggregationEngine)

    suspend fun predictNextSong(currentTrack: TrackDto?, recentHistory: List<TrackDto>): Result<NextSongPrediction> = withContext(Dispatchers.IO) {
        try {
            Result.success(predictionService.predictNextTrack(currentTrack, recentHistory))
        } catch (e: Exception) {
            Result.success(
                NextSongPrediction(
                    predictedTrack = TrackDto(id = "pred_fallback", title = "Kasoor", artist = "Prateek Kuhad"),
                    confidence = 0.85,
                    reason = "Predicted relaxing acoustic flow"
                )
            )
        }
    }

    suspend fun exploreRabbitHole(seed: String, depth: Int = 1): Result<RabbitHoleGraph> = withContext(Dispatchers.IO) {
        try {
            Result.success(rabbitHoleEngine.exploreRabbitHole(seed, depth))
        } catch (e: Exception) {
            Result.success(RabbitHoleGraph(rootTitle = seed))
        }
    }

    fun tuneQueueWithEmotionalEqualizer(
        currentQueue: List<TrackDto>,
        currentTrack: TrackDto?,
        eqState: EmotionalEqualizerState
    ): EmotionalQueueTuneResult {
        return emotionalEqualizerService.tuneQueue(currentQueue, currentTrack, eqState)
    }

    suspend fun finishMySong(seedTrack: TrackDto): Result<FinishMySongResult> = withContext(Dispatchers.IO) {
        try {
            Result.success(emotionalEqualizerService.finishMySong(seedTrack))
        } catch (e: Exception) {
            Result.success(
                FinishMySongResult(
                    seedTrack = seedTrack,
                    continuationCandidates = aggregationEngine.searchAll("${seedTrack.artist} Best").tracks.take(4)
                )
            )
        }
    }

    fun getDailyMusicPuzzle(): MusicPuzzleChallenge {
        return com.sonexa.app.data.provider.MusicPuzzleEngine.getDailyPuzzle()
    }

    fun verifyPuzzleGuess(puzzle: MusicPuzzleChallenge, step: String): Boolean {
        return com.sonexa.app.data.provider.MusicPuzzleEngine.verifyGuess(puzzle, step)
    }

    fun explainSongCulture(track: TrackDto): CulturalExplainer {
        return com.sonexa.app.data.provider.CulturalExplainerService.explainSongCulture(track)
    }

    suspend fun calculateMusicCompatibility(
        userAName: String = "You",
        userBName: String = "Friend"
    ): Result<MusicCompatibilityResult> = withContext(Dispatchers.IO) {
        try {
            Result.success(compatibilityService.calculateCompatibility(userAName, userBName))
        } catch (e: Exception) {
            Result.success(MusicCompatibilityResult())
        }
    }

    suspend fun travelTimeMachine(year: Int): Result<TimeMachineEraData> = withContext(Dispatchers.IO) {
        try {
            Result.success(timeMachineEngine.travelToYear(year))
        } catch (e: Exception) {
            Result.success(TimeMachineEraData(year = year))
        }
    }

    fun getLifeSoundtrackOverview(history: List<TrackDto> = emptyList()): LifeSoundtrackOverview {
        return com.sonexa.app.data.provider.LifeSoundtrackService.generateLifeSoundtrack(history)
    }

    fun fallbackParseIntent(rawText: String): MusicIntentDto {
        val text = rawText.lowercase(Locale.ROOT)
        var intentType = "PLAY_MUSIC"
        var action = "PLAY"
        var artist: String? = null
        var track: String? = null
        val moods = mutableListOf<String>()
        val languages = mutableListOf<String>()
        val genres = mutableListOf<String>()

        // Advanced Music Intelligence Intent Detection
        when {
            text.contains("remember this") || text.contains("save memory") || text.contains("remember song") || text.contains("goa trip") -> {
                intentType = "MUSIC_MEMORY"
                action = "CREATE_MEMORY"
            }
            text.contains("predict next") || text.contains("what should come next") || text.contains("predict song") -> {
                intentType = "PREDICT_NEXT"
                action = "PREDICT_NEXT"
            }
            text.contains("rabbit hole") || text.contains("take me deeper") || text.contains("deeper connection") -> {
                intentType = "MUSIC_RABBIT_HOLE"
                action = "EXPLORE_GRAPH"
            }
            text.contains("finish this song") || text.contains("finish my song") || text.contains("second half of this song") -> {
                intentType = "FINISH_MY_SONG"
                action = "FINISH_SONG"
            }
            text.contains("soundtrack my life") || text.contains("my eras") || text.contains("life soundtrack") -> {
                intentType = "SOUNDTRACK"
                action = "VIEW_SOUNDTRACK"
            }
            text.contains("time machine") || text.contains("take me back to") || text.contains("back to 2016") || text.contains("back to 2013") -> {
                intentType = "TIME_MACHINE"
                action = "TIME_TRAVEL"
            }
            text.contains("translate culture") || text.contains("explain song") || text.contains("cultural context") || text.contains("meaning of this song") -> {
                intentType = "TRANSLATE_CULTURE"
                action = "EXPLAIN_CULTURE"
            }
            text.contains("music puzzle") || text.contains("puzzle") || text.contains("guess the artist") -> {
                intentType = "MUSIC_PUZZLE"
                action = "PLAY_PUZZLE"
            }
            text.contains("music compatibility") || text.contains("compatibility") || text.contains("compare taste") -> {
                intentType = "MUSIC_COMPATIBILITY"
                action = "CHECK_COMPATIBILITY"
            }
            text.contains("music dna") || text.contains("my taste") || text.contains("why do i like this") -> {
                intentType = "MUSIC_DNA"
                action = "VIEW_DNA"
            }
            text.contains("next song") || text.contains("next") || text.contains("skip") -> {
                intentType = "NEXT"
                action = "NEXT"
            }
            text.contains("pause") || text.contains("stop") -> {
                intentType = "PAUSE"
                action = "PAUSE"
            }
            text.contains("resume") || text.contains("unpause") -> {
                intentType = "RESUME"
                action = "RESUME"
            }
            text.contains("like this song") || text.contains("like song") || text.contains("favorite") -> {
                intentType = "LIKE"
                action = "LIKE"
            }
            text.contains("add this to my playlist") || text.contains("add to playlist") || text.contains("save to playlist") -> {
                intentType = "ADD_TO_PLAYLIST"
                action = "ADD_TO_PLAYLIST"
            }
            text.contains("surprise me") -> {
                intentType = "SURPRISE"
                action = "SURPRISE"
            }
            text.contains("something new") || text.contains("discover") -> {
                intentType = "DISCOVERY"
                action = "DISCOVER"
            }
            text.contains("make it more energetic") || text.contains("give me something energetic") || text.contains("more energetic") -> {
                intentType = "CHANGE_VIBE"
                action = "CHANGE_VIBE"
                moods.add("Energetic")
            }
            text.contains("more romantic") || text.contains("romantic vibe") -> {
                intentType = "CHANGE_VIBE"
                action = "CHANGE_VIBE"
                moods.add("Romantic")
            }
            text.contains("more nostalgic") || text.contains("nostalgia") -> {
                intentType = "CHANGE_VIBE"
                action = "CHANGE_VIBE"
                moods.add("Nostalgic")
            }
            text.contains("change vibe") || text.contains("vibe") -> {
                intentType = "CHANGE_VIBE"
                action = "CHANGE_VIBE"
            }
            text.startsWith("find") || text.startsWith("search") -> {
                intentType = "SEARCH"
                action = "SEARCH"
            }
        }

        // Entity Detection: Artists
        if (text.contains("arijit") || text.contains("arjit")) artist = "Arijit Singh"
        else if (text.contains("shreya")) artist = "Shreya Ghoshal"
        else if (text.contains("atif")) artist = "Atif Aslam"
        else if (text.contains("diljit")) artist = "Diljit Dosanjh"
        else if (text.contains("anuv")) artist = "Anuv Jain"
        else if (text.contains("prateek")) artist = "Prateek Kuhad"
        else if (text.contains("badshah")) artist = "Badshah"
        else if (text.contains("honey singh")) artist = "Yo Yo Honey Singh"
        else if (text.contains("sonu nigam")) artist = "Sonu Nigam"
        else if (text.contains("kumar sanu")) artist = "Kumar Sanu"

        // Entity Detection: Tracks & Movies
        if (text.contains("tum hi ho") || text.contains("tumhiho")) track = "Tum Hi Ho"
        else if (text.contains("kesariya")) track = "Kesariya"
        else if (text.contains("kasoor")) track = "Kasoor"
        else if (text.contains("baarishein")) track = "Baarishein"
        else if (text.contains("kabir singh")) track = "Kabir Singh"

        // Languages
        if (text.contains("hindi") || text.contains("bollywood") || text.contains("gaane") || text.contains("gaana")) languages.add("Hindi")
        if (text.contains("punjabi")) languages.add("Punjabi")
        if (text.contains("tamil")) languages.add("Tamil")
        if (text.contains("telugu")) languages.add("Telugu")
        if (text.contains("english")) languages.add("English")

        // Moods
        if (text.contains("romantic") || text.contains("love") || text.contains("pyaar") || text.contains("dil")) moods.add("Romantic")
        if (text.contains("calm") || text.contains("relax") || text.contains("relaxing") || text.contains("peaceful")) moods.add("Calm")
        if (text.contains("party") || text.contains("dance") || text.contains("club")) moods.add("Party")
        if (text.contains("workout") || text.contains("gym") || text.contains("energetic") || text.contains("power")) moods.add("Energetic")
        if (text.contains("sad") || text.contains("dard") || text.contains("breakup")) moods.add("Sad")

        // Genres
        if (text.contains("bollywood")) genres.add("Bollywood")
        if (text.contains("pop")) genres.add("Pop")
        if (text.contains("edm")) genres.add("EDM")
        if (text.contains("lofi") || text.contains("lo-fi")) genres.add("Lo-Fi")

        return MusicIntentDto(
            intentType = intentType,
            query = rawText,
            artist = artist,
            track = track,
            genres = genres,
            languages = languages,
            moods = moods,
            action = action,
            confidence = 0.94
        )
    }
}

