package com.sonexa.app.data.provider

import com.sonexa.app.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MusicIntelligenceTest {

    @Test
    fun testMusicPuzzleDailyAndGuessVerification() {
        val daily = MusicPuzzleEngine.getDailyPuzzle()
        assertNotNull("Daily puzzle should not be null", daily)
        assertTrue("Puzzle should have solution steps", daily.solutionPath.isNotEmpty())

        val correctGuess = daily.solutionPath.first()
        val isVerified = MusicPuzzleEngine.verifyGuess(daily, correctGuess)
        assertTrue("Correct guess should verify to true", isVerified)

        val incorrectGuess = "Unknown Artist X 123"
        val isFailed = MusicPuzzleEngine.verifyGuess(daily, incorrectGuess)
        assertFalse("Incorrect guess should verify to false", isFailed)
    }

    @Test
    fun testCulturalExplainerService() {
        val kesariya = TrackDto(id = "kes_1", title = "Kesariya", artist = "Arijit Singh", album = "Brahmastra")
        val explanation = CulturalExplainerService.explainSongCulture(kesariya)

        assertEquals("Kesariya", explanation.trackTitle)
        assertTrue("Should explain saffron cultural context", explanation.culturalContext.contains("saffron", ignoreCase = true))
        assertTrue("Should have cultural expressions", explanation.expressions.isNotEmpty())
    }

    @Test
    fun testEmotionalEqualizerTuning() {
        val service = EmotionalEqualizerService()
        val queue = listOf(
            TrackDto(id = "1", title = "Lofi Rain", artist = "Artist A", bpm = 80.0, acousticness = 0.8),
            TrackDto(id = "2", title = "EDM Drop", artist = "Artist B", bpm = 128.0, acousticness = 0.1),
            TrackDto(id = "3", title = "Romantic Melody", artist = "Artist C", bpm = 95.0, isLiked = true)
        )
        val current = TrackDto(id = "0", title = "Playing", artist = "Artist 0")

        val highEnergyState = EmotionalEqualizerState(energy = 95f, happiness = 80f)
        val tunedHigh = service.tuneQueue(queue, current, highEnergyState)

        assertEquals(4, tunedHigh.tunedQueue.size)
        assertEquals("Current track should stay at the front", "0", tunedHigh.tunedQueue.first().id)
        assertTrue("Should contain profile summary", tunedHigh.explanation.isNotBlank())
    }

    @Test
    fun testNextTrackPrediction() = runBlocking {
        val service = NextTrackPredictionService()
        val arijitTrack = TrackDto(id = "t_arijit", title = "Tum Hi Ho", artist = "Arijit Singh", album = "Aashiqui 2")
        val prediction = service.predictNextTrack(arijitTrack, emptyList())

        assertNotNull(prediction.predictedTrack)
        assertTrue("Confidence should be high", prediction.confidence >= 0.75)
        assertTrue("Reason should be explainable", prediction.reason.isNotBlank())
    }

    @Test
    fun testRabbitHoleGraphGeneration() = runBlocking {
        val engine = MusicRabbitHoleEngine()
        val graph = engine.exploreRabbitHole("Arijit Singh")

        assertEquals("Arijit Singh", graph.rootTitle)
        assertTrue("Graph should have nodes", graph.nodes.size >= 3)
        assertTrue("Graph should have connections", graph.edges.isNotEmpty())
    }

    @Test
    fun testLifeSoundtrackOverview() {
        val soundtrack = LifeSoundtrackService.generateLifeSoundtrack()
        assertNotNull(soundtrack.currentEra)
        assertTrue("Should contain past eras", soundtrack.pastEras.isNotEmpty())
        assertTrue("Should have genre evolution", soundtrack.topGenreEvolution.isNotBlank())
    }

    @Test
    fun testTimeMachineTravel() = runBlocking {
        val engine = MusicTimeMachineEngine()
        val era2016 = engine.travelToYear(2016)

        assertEquals(2016, era2016.year)
        assertTrue("Should have description", era2016.description.contains("Bollywood", ignoreCase = true))
        assertTrue("Should have historical milestones", era2016.historicalMilestones.isNotEmpty())
    }

    @Test
    fun testMusicCompatibilityCalculation() = runBlocking {
        val service = MusicCompatibilityService()
        val report = service.calculateCompatibility("UserA", "UserB")

        assertTrue("Match score should be between 0 and 100", report.matchPercentage in 0..100)
        assertTrue("Should have shared genres", report.sharedGenres.isNotEmpty())
    }
}
