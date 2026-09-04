package com.sonexa.app.data.search

import com.sonexa.app.data.model.TrackDto
import org.junit.Assert.*
import org.junit.Test

class SearchSystemQualityTest {

    @Test
    fun testExactArtistResolution() {
        val q1 = "Arijit Singh"
        val intent1 = IntentDetector.detect(q1)
        assertEquals(SearchIntentType.ARTIST_SEARCH, intent1.type)
        assertEquals("arijit singh", intent1.artistName)

        val q2 = "arijit singh"
        val intent2 = IntentDetector.detect(q2)
        assertEquals(SearchIntentType.ARTIST_SEARCH, intent2.type)
        assertEquals("arijit singh", intent2.artistName)

        val q3 = "Arjit Singh"
        val typo3 = TypoCorrectionService.findCorrection(q3)
        assertNotNull(typo3)
        assertEquals("Arijit Singh", typo3?.correctedQuery)

        val q4 = "Arijeet Singh"
        val typo4 = TypoCorrectionService.findCorrection(q4)
        assertNotNull(typo4)
        assertEquals("Arijit Singh", typo4?.correctedQuery)
    }

    @Test
    fun testDevanagariTransliterationAndIntent() {
        // "अरिजीत सिंह" -> "arijit singh"
        val devArtist = "अरिजीत सिंह"
        val translitArtist = TransliterationService.devanagariToRoman(devArtist)
        assertEquals("arijit singh", translitArtist)

        val intentDev = IntentDetector.detect(devArtist)
        assertTrue(intentDev.isDevanagari)
        assertEquals("hi", intentDev.detectedLanguage)
        assertEquals("arijit singh", intentDev.transliteratedQuery)

        // "तुम ही हो" -> "tum hi ho"
        val devTrack = "तुम ही हो"
        val translitTrack = TransliterationService.devanagariToRoman(devTrack)
        assertEquals("tum hi ho", translitTrack)

        val intentTrack = IntentDetector.detect(devTrack)
        assertTrue(intentTrack.isDevanagari)
        assertEquals("tum hi ho", intentTrack.transliteratedQuery)

        // "कबीर सिंह" -> "kabir singh"
        val devMovie = "कबीर सिंह"
        val translitMovie = TransliterationService.devanagariToRoman(devMovie)
        assertEquals("kabir singh", translitMovie)

        // "पल पल दिल के पास" -> "pal pal dil ke paas"
        val devPal = "पल पल दिल के पास"
        assertEquals("pal pal dil ke paas", TransliterationService.devanagariToRoman(devPal))

        // "मेरा मन" -> "mera man"
        val devMan = "मेरा मन"
        assertEquals("mera man", TransliterationService.devanagariToRoman(devMan))
    }

    @Test
    fun testExactTrackMatching() {
        val q1 = "Tum Hi Ho"
        val intent1 = IntentDetector.detect(q1)
        assertEquals(SearchIntentType.TRACK_SEARCH, intent1.type)
        assertEquals("tum hi ho", intent1.trackTitle)

        val q2 = "tum hi ho"
        val intent2 = IntentDetector.detect(q2)
        assertEquals(SearchIntentType.TRACK_SEARCH, intent2.type)
        assertEquals("tum hi ho", intent2.trackTitle)
    }

    @Test
    fun testMovieSoundtrackIntent() {
        val q1 = "Kabir Singh"
        val intent1 = IntentDetector.detect(q1)
        assertEquals(SearchIntentType.MOVIE_SOUNDTRACK, intent1.type)
        assertEquals("Kabir Singh", intent1.movieName)

        val q2 = "Kabir Singh songs"
        val intent2 = IntentDetector.detect(q2)
        assertEquals(SearchIntentType.MOVIE_SOUNDTRACK, intent2.type)
        assertEquals("Kabir Singh", intent2.movieName)

        val q3 = "Shershaah"
        val intent3 = IntentDetector.detect(q3)
        assertEquals(SearchIntentType.MOVIE_SOUNDTRACK, intent3.type)
        assertEquals("Shershaah", intent3.movieName)

        val q4 = "Rockstar songs"
        val intent4 = IntentDetector.detect(q4)
        assertEquals(SearchIntentType.MOVIE_SOUNDTRACK, intent4.type)
        assertEquals("Rockstar", intent4.movieName)
    }

    @Test
    fun testHinglishAndNaturalLanguageIntents() {
        // "Arijit ke romantic songs"
        val q1 = "Arijit ke romantic songs"
        val intent1 = IntentDetector.detect(q1)
        assertEquals(SearchIntentType.ARTIST_MOOD_SEARCH, intent1.type)
        assertEquals("arijit", intent1.artistName)
        assertEquals("ROMANTIC", intent1.mood)

        // "Arijit ke gaane"
        val q2 = "Arijit ke gaane"
        val intent2 = IntentDetector.detect(q2)
        assertEquals(SearchIntentType.ARTIST_SEARCH, intent2.type)
        assertEquals("arijit", intent2.artistName)

        // "romantic hindi songs"
        val q3 = "romantic hindi songs"
        val intent3 = IntentDetector.detect(q3)
        assertEquals("ROMANTIC", intent3.mood)
        assertEquals("hi", intent3.detectedLanguage)

        // "हिंदी रोमांटिक गाने"
        val q4 = "हिंदी रोमांटिक गाने"
        val intent4 = IntentDetector.detect(q4)
        assertEquals("hi", intent4.detectedLanguage)
        assertTrue(intent4.isDevanagari)

        // "90s hindi songs"
        val q5 = "90s hindi songs"
        val intent5 = IntentDetector.detect(q5)
        assertEquals(SearchIntentType.ERA_SEARCH, intent5.type)
        assertEquals("90s", intent5.era)

        // "old Hindi songs"
        val q6 = "old Hindi songs"
        val intent6 = IntentDetector.detect(q6)
        assertEquals(SearchIntentType.ERA_SEARCH, intent6.type)
    }

    @Test
    fun testRegionalAndMoodQueries() {
        // "Punjabi party songs"
        val q1 = "Punjabi party songs"
        val intent1 = IntentDetector.detect(q1)
        assertEquals("pa", intent1.detectedLanguage)
        assertEquals("PARTY", intent1.mood)

        // "Tamil melody songs"
        val q2 = "Tamil melody songs"
        val intent2 = IntentDetector.detect(q2)
        assertEquals("ta", intent2.detectedLanguage)

        // "Telugu love songs"
        val q3 = "Telugu love songs"
        val intent3 = IntentDetector.detect(q3)
        assertEquals("te", intent3.detectedLanguage)
        assertEquals("ROMANTIC", intent3.mood)

        // "gym songs"
        val q4 = "gym songs"
        val intent4 = IntentDetector.detect(q4)
        assertEquals(SearchIntentType.MOOD_SEARCH, intent4.type)
        assertEquals("ENERGETIC", intent4.mood)

        // "relaxing music"
        val q5 = "relaxing music"
        val intent5 = IntentDetector.detect(q5)
        assertEquals(SearchIntentType.MOOD_SEARCH, intent5.type)
        assertEquals("RELAXING", intent5.mood)

        // "Songs like Tum Hi Ho"
        val q6 = "Songs like Tum Hi Ho"
        val intent6 = IntentDetector.detect(q6)
        assertEquals(SearchIntentType.SIMILAR_TRACK_SEARCH, intent6.type)
    }

    @Test
    fun testTypoCorrectionDidYouMean() {
        val typo1 = TypoCorrectionService.findCorrection("Arjit Sing")
        assertNotNull(typo1)
        assertEquals("Arijit Singh", typo1?.correctedQuery)

        val typo2 = TypoCorrectionService.findCorrection("Tum Hi Hoo")
        assertNotNull(typo2)
        assertEquals("Tum Hi Ho", typo2?.correctedQuery)

        val typo3 = TypoCorrectionService.findCorrection("Shershah")
        assertNotNull(typo3)
        assertEquals("Shershaah", typo3?.correctedQuery)

        val exact = TypoCorrectionService.findCorrection("Arijit Singh")
        assertNull(exact)
    }

    @Test
    fun testExactMatchPriorityOverRemixes() {
        val rankingService = SearchRankingService()
        val query = "Tum Hi Ho"
        val intent = IntentDetector.detect(query)

        val originalTrack = TrackDto(
            id = "t1",
            title = "Tum Hi Ho",
            artist = "Arijit Singh",
            album = "Aashiqui 2",
            durationMs = 262000L,
            versionType = "Original",
            isPlayable = true,
            isOfficial = true,
            audioUrl = "https://example.com/tumhiho.mp3",
            coverUrl = "https://example.com/cover.jpg"
        )

        val remixTrack = TrackDto(
            id = "t2",
            title = "Tum Hi Ho - Remix",
            artist = "DJ Remix",
            album = "Party Hits",
            durationMs = 210000L,
            versionType = "Remix",
            isPlayable = true,
            isOfficial = false,
            audioUrl = "https://example.com/remix.mp3",
            coverUrl = "https://example.com/cover2.jpg"
        )

        val coverTrack = TrackDto(
            id = "t3",
            title = "Tum Hi Ho Cover",
            artist = "Acoustic Singer",
            album = "Unplugged",
            durationMs = 230000L,
            versionType = "Cover",
            isPlayable = true,
            isOfficial = false,
            audioUrl = "https://example.com/cover.mp3",
            coverUrl = "https://example.com/cover3.jpg"
        )

        val ranked = rankingService.rankTracks(
            tracks = listOf(remixTrack, coverTrack, originalTrack),
            query = query,
            intent = intent
        )

        // Original track MUST rank #1
        assertEquals("t1", ranked.first().id)
        assertEquals("Tum Hi Ho", ranked.first().title)
        assertEquals("EXACT_MATCH", ranked.first().qualityTier)
    }

    @Test
    fun testQueryExpansionVariants() {
        val query = "Arijit ke romantic songs"
        val intent = IntentDetector.detect(query)
        val expanded = QueryExpansionService.expand(query, intent)

        assertTrue(expanded.isNotEmpty())
        assertTrue(expanded.any { it.contains("arijit") })
        assertTrue(expanded.any { it.contains("romantic") })
    }
}
