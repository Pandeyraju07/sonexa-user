package com.sonexa.app.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

// =========================================================================
// 1. MUSIC MEMORY — "Remember This Moment"
// =========================================================================
data class MusicMemory(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String = "Vibrant",
    val locationTag: String? = null,
    val photoUris: List<String> = emptyList(),
    val tracks: List<TrackDto> = emptyList(),
    val totalDurationMs: Long = 0L,
    val replayCount: Int = 0,
    val tags: List<String> = emptyList(),
    val dominantArtist: String = ""
)

data class CreateMemoryRequestDto(
    val title: String,
    val description: String = "",
    val mood: String = "Chill",
    val locationTag: String? = null,
    val trackIds: List<String> = emptyList(),
    val photoUris: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

// =========================================================================
// 2. SOUNDTRACK MY LIFE — Personal Eras & Milestone Timelines
// =========================================================================
data class LifeSoundtrackEra(
    val id: String = UUID.randomUUID().toString(),
    val eraTitle: String = "New Beginning",
    val monthYearLabel: String = "Current Era",
    val description: String = "",
    val topTracks: List<TrackDto> = emptyList(),
    val dominantMood: String = "Acoustic",
    val energyCurve: List<Float> = listOf(0.4f, 0.6f, 0.8f, 0.5f),
    val startDate: Long = System.currentTimeMillis() - (30L * 24 * 3600 * 1000),
    val endDate: Long = System.currentTimeMillis(),
    val primaryGenres: List<String> = listOf("Bollywood", "Acoustic", "Pop")
)

data class LifeSoundtrackOverview(
    val currentEra: LifeSoundtrackEra = LifeSoundtrackEra(),
    val pastEras: List<LifeSoundtrackEra> = emptyList(),
    val totalListeningHours: Double = 0.0,
    val topGenreEvolution: String = "Pop → Acoustic → Indie"
)

// =========================================================================
// 3. PREDICT MY NEXT SONG — Contextual Next-Track Transition Predictor
// =========================================================================
data class NextSongPrediction(
    val predictedTrack: TrackDto = TrackDto(),
    val confidence: Double = 0.88,
    val reason: String = "Harmonically aligns with your late-night acoustic transition pattern",
    val transitionType: String = "CALMER_ACOUSTIC",
    val alternativeCandidates: List<TrackDto> = emptyList()
)

// =========================================================================
// 4. MUSIC RABBIT HOLE — Multi-Hop Deep Discovery Graph
// =========================================================================
enum class RabbitHoleNodeType {
    SONG, ARTIST, PRODUCER, COMPOSER, GENRE, SAMPLE, ORIGINAL_SONG, SIMILAR_ARTIST
}

enum class RabbitHoleRelation {
    PERFORMED_BY,
    PRODUCED_BY,
    COMPOSED_BY,
    SAMPLED_IN,
    SAMPLE_OF,
    SIMILAR_TO,
    INFLUENCED_BY,
    GENRE_ROOT
}

data class RabbitHoleNode(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: RabbitHoleNodeType = RabbitHoleNodeType.SONG,
    val imageUrl: String = "",
    val streamTrack: TrackDto? = null,
    val trivia: String = ""
)

data class RabbitHoleEdge(
    val fromId: String,
    val toId: String,
    val relation: RabbitHoleRelation,
    val description: String = ""
)

data class RabbitHoleGraph(
    val rootId: String = "",
    val rootTitle: String = "",
    val nodes: List<RabbitHoleNode> = emptyList(),
    val edges: List<RabbitHoleEdge> = emptyList(),
    val depth: Int = 1,
    val narrative: String = ""
)

// =========================================================================
// 5. MUSIC PERSONALITY EVOLUTION
// =========================================================================
data class PersonalityEvolutionPeriod(
    val periodName: String = "Today",
    val personaTitle: String = "Emotional Explorer",
    val familiarPct: Int = 70,
    val discoveryPct: Int = 30,
    val energyLevel: Int = 65,
    val emotionLevel: Int = 85,
    val nostalgiaLevel: Int = 60,
    val experimentLevel: Int = 50,
    val topLanguages: List<String> = listOf("Hindi", "English"),
    val topGenres: List<String> = listOf("Acoustic", "Bollywood", "Indie")
)

data class PersonalityEvolutionReport(
    val currentPersona: PersonalityEvolutionPeriod = PersonalityEvolutionPeriod(),
    val historicalPeriods: List<PersonalityEvolutionPeriod> = emptyList(),
    val growthSummary: String = "Your taste has shifted +35% toward Indie & Acoustic discovery over the last 90 days."
)

// =========================================================================
// 6. MUSIC DNA 2.0 — "Why Do I Like This?"
// =========================================================================
data class MusicDnaDeepPattern(
    val coreTasteNarrative: String = "You gravitate toward acoustic male vocals with gradual musical buildups and melancholic melodies.",
    val preferredVocalTimbre: String = "Warm & Melodic (Male Acoustic)",
    val preferredInstrumentation: List<String> = listOf("Acoustic Guitar", "Piano", "Subtle Percussion", "Violin Arpeggios"),
    val tempoPreferenceBpm: String = "85 - 118 BPM (Moderate)",
    val structuralPreference: String = "Strong melodic chorus with slow atmospheric intro",
    val whyYouLikeArtistsExplanation: Map<String, String> = emptyMap(),
    val acousticAffinities: List<String> = listOf(
        "Low instrumental clutter",
        "Rich lyrical depth",
        "Gradual emotional crescendo",
        "Hindi / Punjabi acoustic vocal delivery"
    )
)

// =========================================================================
// 7. EMOTIONAL EQUALIZER
// =========================================================================
data class EmotionalEqualizerState(
    val energy: Float = 50f,       // 0 - 100
    val happiness: Float = 50f,    // 0 - 100
    val nostalgia: Float = 50f,    // 0 - 100
    val romance: Float = 50f,      // 0 - 100
    val intensity: Float = 50f,    // 0 - 100
    val discovery: Float = 50f     // 0 - 100
)

data class EmotionalQueueTuneResult(
    val tunedQueue: List<TrackDto>,
    val explanation: String,
    val targetAcousticProfile: String
)

// =========================================================================
// 8. MUSIC JOURNEY ENGINE
// =========================================================================
data class MusicJourneyTrajectory(
    val journeyId: String = UUID.randomUUID().toString(),
    val title: String = "Late Night Drive Journey",
    val theme: String = "LATE_NIGHT_DRIVE",
    val durationMinutes: Int = 60,
    val phases: List<MusicJourneyPhaseItemDto> = emptyList(),
    val allTracks: List<TrackDto> = emptyList(),
    val currentPhaseIndex: Int = 0
)

// =========================================================================
// 9. FINISH MY SONG — Musical Climax & Continuation
// =========================================================================
data class FinishMySongResult(
    val seedTrack: TrackDto,
    val continuationCandidates: List<TrackDto> = emptyList(),
    val matchExplanation: String = "These tracks match the harmonic buildup, tempo elevation, and emotional energy of the second half of '${seedTrack.title}'"
)

// =========================================================================
// 10. MUSIC PUZZLE — Daily Music Discovery Connection Game
// =========================================================================
enum class PuzzleType {
    ARTIST_LINK, MISSING_SONG, PRODUCER_CONNECTION, BOLLYWOOD_CHAIN, GLOBAL_ROOTS
}

data class MusicPuzzleChallenge(
    val id: String = "daily_puzzle_${System.currentTimeMillis() / (24 * 3600 * 1000)}",
    val title: String = "Daily Connection: Arijit Singh → A.R. Rahman",
    val type: PuzzleType = PuzzleType.ARTIST_LINK,
    val description: String = "Find the musical path connecting Arijit Singh to A.R. Rahman in 3 steps or fewer.",
    val startEntity: String = "Arijit Singh",
    val targetEntity: String = "A.R. Rahman",
    val maxSteps: Int = 3,
    val hints: List<String> = listOf("Pritam has collaborated with both artists", "Look for sound engineers and movie soundtracks from 2017"),
    val solutionPath: List<String> = listOf("Arijit Singh", "Pritam", "A.R. Rahman"),
    val rewardsPlaylistTitle: String = "Arijit × Rahman Fusion Collection"
)

// =========================================================================
// 11. TRANSLATE THE CULTURE — Cultural Music Explainer
// =========================================================================
data class CulturalExpression(
    val phrase: String = "",
    val pronunciation: String = "",
    val literalMeaning: String = "",
    val culturalSignificance: String = ""
)

data class CulturalExplainer(
    val trackId: String = "",
    val trackTitle: String = "",
    val artist: String = "",
    val language: String = "Punjabi",
    val culturalContext: String = "",
    val storyBehindSong: String = "",
    val expressions: List<CulturalExpression> = emptyList(),
    val traditionalInstruments: List<String> = emptyList(),
    val emotionalEssence: String = ""
)

// =========================================================================
// 12. CONVERSATIONAL MUSIC SEARCH
// =========================================================================
data class ConversationTurn(
    val speaker: String = "USER", // USER or ZYNERA
    val message: String = "",
    val quickReplies: List<String> = emptyList(),
    val activeSessionTracks: List<TrackDto> = emptyList()
)

data class ConversationalSearchState(
    val turns: List<ConversationTurn> = emptyList(),
    val accumulatedIntent: MusicIntentDto = MusicIntentDto(),
    val isReadyToPlay: Boolean = false
)

// =========================================================================
// 13. MUSIC COMPATIBILITY — Social Taste Matrix
// =========================================================================
data class MusicCompatibilityResult(
    val userNameA: String = "You",
    val userNameB: String = "Friend",
    val matchPercentage: Int = 82,
    val sharedGenres: List<String> = listOf("Bollywood Romantic", "Indie Hindi", "Acoustic Pop"),
    val sharedArtists: List<String> = listOf("Arijit Singh", "Anuv Jain", "Prateek Kuhad"),
    val tasteDivergence: String = "You lean 70% toward discovery, while your friend prefers familiar 2000s classics.",
    val mutualDiscoveryTracks: List<TrackDto> = emptyList()
)

// =========================================================================
// 14. TEACH ZYNERA MY TASTE — Direct Algorithmic Controls
// =========================================================================
data class UserTasteControls(
    val familiarity: Float = 50f,     // 0 (100% discovery) - 100 (100% familiar)
    val discovery: Float = 70f,       // 0 - 100
    val mainstream: Float = 40f,      // 0 (Underground) - 100 (Chartbusters)
    val experimental: Float = 60f,    // 0 (Traditional) - 100 (Avant-garde)
    val nostalgia: Float = 50f,       // 0 (Current) - 100 (Throwback)
    val preferredLanguages: List<String> = listOf("Hindi", "Punjabi", "English")
)

// =========================================================================
// 15. MUSIC TIME MACHINE — Historical Era Reconstructor
// =========================================================================
data class TimeMachineEraData(
    val year: Int = 2016,
    val eraTitle: String = "The 2016 Golden Wave",
    val description: String = "A transformative year for Bollywood melodies, tropical house, and acoustic indie emergence.",
    val chartbusters: List<TrackDto> = emptyList(),
    val hiddenGems: List<TrackDto> = emptyList(),
    val globalSoundtrack: List<TrackDto> = emptyList(),
    val historicalMilestones: List<String> = listOf(
        "Ae Dil Hai Mushkil redefined modern Hindi cinema soundtracks",
        "Rise of streaming transformed indie music discovery in India",
        "Global explosion of EDM and tropical pop harmonies"
    )
)
