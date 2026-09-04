package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MusicIntelligenceQualityTest {

    private val trackUnderstandingService = TrackUnderstandingService()
    private val deduplicationService = TrackDeduplicationService()
    private val recommendationEngine = HybridRecommendationEngine()

    @Test
    fun testTrackUnderstandingAndAcousticFeatures() {
        val energeticTrack = TrackDto(
            id = "t_party",
            title = "Abhi Toh Party Shuru Hui Hai",
            artist = "Badshah",
            album = "Party Hits",
            genres = listOf("EDM", "Bollywood"),
            mood = "Party",
            moods = listOf("Party", "Energetic"),
            bpm = 128.0,
            energy = 0.92
        )

        val profile = trackUnderstandingService.analyzeTrack(energeticTrack)
        assertTrue(profile.normalizedEnergy >= 0.80)
        assertEquals("PARTY", profile.canonicalMood)
        assertTrue(profile.danceability >= 0.60)

        val romanticTrack = TrackDto(
            id = "t_romance",
            title = "Tum Hi Ho",
            artist = "Arijit Singh",
            album = "Aashiqui 2",
            genres = listOf("Romantic", "Bollywood"),
            mood = "Romantic",
            moods = listOf("Romantic"),
            bpm = 85.0,
            energy = 0.45
        )

        val romanticProfile = trackUnderstandingService.analyzeTrack(romanticTrack)
        assertTrue(romanticProfile.isRomantic)
        assertEquals("ROMANTIC", romanticProfile.canonicalMood)
        assertTrue(romanticProfile.acousticness >= 0.60)
    }

    @Test
    fun testCosineSimilarityComputation() {
        val trackA = TrackDto(
            id = "t1",
            title = "Channa Mereya",
            artist = "Arijit Singh",
            album = "Ae Dil Hai Mushkil",
            mood = "Sad",
            energy = 0.50
        )
        val trackB = TrackDto(
            id = "t2",
            title = "Agar Tum Saath Ho",
            artist = "Arijit Singh, Alka Yagnik",
            album = "Tamasha",
            mood = "Sad",
            energy = 0.48
        )
        val trackC = TrackDto(
            id = "t3",
            title = "Kala Chashma",
            artist = "Badshah, Neha Kakkar",
            album = "Baar Baar Dekho",
            mood = "Party",
            energy = 0.95
        )

        val profileA = trackUnderstandingService.analyzeTrack(trackA)
        val profileB = trackUnderstandingService.analyzeTrack(trackB)
        val profileC = trackUnderstandingService.analyzeTrack(trackC)

        val simAB = trackUnderstandingService.calculateCosineSimilarity(profileA, profileB)
        val simAC = trackUnderstandingService.calculateCosineSimilarity(profileA, profileC)

        // Similar sad romantic ballads should have much higher similarity than high-energy party tracks
        assertTrue("Expected simAB ($simAB) > simAC ($simAC)", simAB > simAC)
        assertTrue("Expected simAB >= 0.60", simAB >= 0.60)
    }

    @Test
    fun testDeduplicationService() {
        val originalTrack = TrackDto(
            id = "aud_101",
            title = "Kesariya",
            artist = "Arijit Singh, Pritam",
            durationMs = 268000L,
            audioUrl = "https://example.com/kesariya.mp3"
        )
        val duplicateTrack = TrackDto(
            id = "aud_102",
            title = "Kesariya (From Brahmastra)",
            artist = "Arijit Singh",
            durationMs = 269000L,
            audioUrl = "https://example.com/kesariya2.mp3"
        )
        val differentTrack = TrackDto(
            id = "aud_103",
            title = "Apna Bana Le",
            artist = "Arijit Singh",
            durationMs = 240000L,
            audioUrl = "https://example.com/apna.mp3"
        )

        val rawList = listOf(originalTrack, duplicateTrack, differentTrack)
        val deduplicated = deduplicationService.deduplicate(rawList)

        assertEquals(2, deduplicated.size)
        assertTrue(deduplicated.any { it.title.contains("Kesariya") })
        assertTrue(deduplicated.any { it.title.contains("Apna Bana Le") })
    }

    @Test
    fun testDiversityFilter() {
        val tracks = listOf(
            TrackDto(id = "1", title = "Song 1", artist = "Arijit Singh"),
            TrackDto(id = "2", title = "Song 2", artist = "Arijit Singh"),
            TrackDto(id = "3", title = "Song 3", artist = "Arijit Singh"),
            TrackDto(id = "4", title = "Song 4", artist = "Shreya Ghoshal"),
            TrackDto(id = "5", title = "Song 5", artist = "Arijit Singh")
        )

        val diverse = recommendationEngine.applyDiversityFilter(tracks, maxConsecutiveSameArtist = 2)

        // Cannot have 3 consecutive Arijit tracks
        for (i in 0 until diverse.size - 2) {
            val a1 = diverse[i].artist
            val a2 = diverse[i + 1].artist
            val a3 = diverse[i + 2].artist
            assertFalse("Found 3 consecutive tracks by $a1", a1 == a2 && a2 == a3)
        }
    }

    @Test
    fun testChangeVibeReordering() = runBlocking {
        val calmTrack = TrackDto(id = "c1", title = "Calm Melodies", artist = "Artist A", mood = "Calm", energy = 0.25)
        val midTrack = TrackDto(id = "m1", title = "Balanced Vibe", artist = "Artist B", mood = "Chill", energy = 0.55)
        val highTrack = TrackDto(id = "h1", title = "High Voltage Party", artist = "Artist C", mood = "Party", energy = 0.95)

        val initialQueue = listOf(calmTrack, midTrack, highTrack)

        val energeticQueue = recommendationEngine.changeVibe("MORE_ENERGETIC", initialQueue, currentTrack = null)
        assertEquals("h1", energeticQueue.first().id)

        val relaxingQueue = recommendationEngine.changeVibe("MORE_RELAXING", initialQueue, currentTrack = null)
        assertEquals("c1", relaxingQueue.first().id)
    }

    @Test
    fun testFullAudioStreamResolver() = runBlocking {
        val directAudiusTrack = TrackDto(
            id = "aud_1",
            title = "Chill Lofi",
            artist = "Lofi Artist",
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/aud_1/stream"
        )
        val resolved = FullAudioStreamResolver.resolveFullStreamUrl(directAudiusTrack)
        assertEquals(directAudiusTrack.audioUrl, resolved)
    }

    @Test
    fun testHindiTransliterationAndEntityResolution() {
        val transliteratedArtist = com.sonexa.app.data.search.TransliterationService.transliterate("अरिजीत सिंह")
        assertEquals("arijit singh", transliteratedArtist.lowercase())

        val transliteratedSong = com.sonexa.app.data.search.TransliterationService.transliterate("तुम ही हो")
        assertEquals("tum hi ho", transliteratedSong.lowercase())

        val transliteratedMovie = com.sonexa.app.data.search.TransliterationService.transliterate("कबीर सिंह")
        assertEquals("kabir singh", transliteratedMovie.lowercase())
    }

    @Test
    fun testIntentDetectionAndHinglishNLP() {
        val intent1 = com.sonexa.app.data.search.IntentDetector.detect("Arijit ke romantic gaane")
        assertTrue(intent1.artistName?.contains("arijit") == true)
        assertEquals("ROMANTIC", intent1.mood)
        assertEquals("hi", intent1.detectedLanguage)

        val intent2 = com.sonexa.app.data.search.IntentDetector.detect("Tum Hi Ho")
        assertEquals("tum hi ho", intent2.trackTitle?.lowercase())

        val intent3 = com.sonexa.app.data.search.IntentDetector.detect("Punjabi party songs")
        assertEquals("Punjabi", intent3.genre)
        assertEquals("PARTY", intent3.mood)
    }

    @Test
    fun testTypoCorrectionAndFuzzyMatching() {
        val suggestion1 = com.sonexa.app.data.search.TypoCorrectionService.findCorrection("Arjit Singh")
        assertEquals("Arijit Singh", suggestion1?.correctedQuery)

        val suggestion2 = com.sonexa.app.data.search.TypoCorrectionService.findCorrection("Tum Hi Hoo")
        assertEquals("Tum Hi Ho", suggestion2?.correctedQuery)
    }

    @Test
    fun testSearchRankingExactMatchPriority() {
        val rankingEngine = SearchRankingEngine()
        val original = TrackDto(id = "1", title = "Tum Hi Ho", artist = "Arijit Singh", versionType = "Original")
        val remix = TrackDto(id = "2", title = "Tum Hi Ho (Club Remix)", artist = "DJ X", versionType = "Remix")
        val cover = TrackDto(id = "3", title = "Tum Hi Ho Cover", artist = "Singer Y", versionType = "Cover")

        val ranked = rankingEngine.rankSearchResults(listOf(remix, cover, original), query = "Tum Hi Ho")
        assertEquals("1", ranked.first().id)
        assertEquals("Tum Hi Ho", ranked.first().title)
    }

    @Test
    fun testFullAudioStreamResolverPreviewDetectionAndCache() {
        // Test preview detection
        assertTrue(FullAudioStreamResolver.isAudioPreview("https://cdns-preview-d.deezer.com/stream/preview.mp3", "deezer"))
        assertTrue(FullAudioStreamResolver.isAudioPreview("", "zynera"))
        assertFalse(FullAudioStreamResolver.isAudioPreview("https://discoveryprovider.audius.co/v1/tracks/123/stream", "audius"))
        assertFalse(FullAudioStreamResolver.isAudioPreview("https://aac.saavncdn.com/123/track_320.mp4", "jiosaavn"))
        assertFalse(FullAudioStreamResolver.isAudioPreview("https://prod-1.storage.jamendo.com/download/track/456/mp32", "jamendo"))

        // Test cache key generation
        val track = TrackDto(
            id = "saavn_12345",
            title = "Kesariya (From Brahmastra)",
            artist = "Arijit Singh, Pritam",
            durationMs = 268000L,
            audioUrl = "https://aac.saavncdn.com/123/Kesariya_320.mp4",
            provider = "jiosaavn"
        )
        val key = FullAudioStreamResolver.getCacheKey(track)
        assertTrue(key.startsWith("kesariya:::arijit singh"))
    }
}


