package com.sonexa.app.data.repository

import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VoiceAndAiQualityTest {

    private val aiRepository = AiRepository()

    @Test
    fun testVoiceCommandIntentParsing() {
        // "Play Arijit Singh songs"
        val i1 = aiRepository.fallbackParseIntent("Play Arijit Singh songs")
        assertEquals("PLAY_MUSIC", i1.intentType)
        assertEquals("Arijit Singh", i1.artist)
        assertEquals("PLAY", i1.action)
        assertTrue(i1.confidence >= 0.85)

        // "Arijit ke romantic gaane"
        val i2 = aiRepository.fallbackParseIntent("Arijit ke romantic gaane")
        assertEquals("PLAY_MUSIC", i2.intentType)
        assertEquals("Arijit Singh", i2.artist)
        assertTrue(i2.moods.contains("Romantic"))
        assertTrue(i2.languages.contains("Hindi"))

        // "Play Tum Hi Ho"
        val i3 = aiRepository.fallbackParseIntent("Play Tum Hi Ho")
        assertEquals("PLAY_MUSIC", i3.intentType)
        assertEquals("Tum Hi Ho", i3.track)

        // "Find Kabir Singh songs"
        val i4 = aiRepository.fallbackParseIntent("Find Kabir Singh songs")
        assertEquals("SEARCH", i4.intentType)
        assertEquals("Kabir Singh", i4.track)

        // "Play Punjabi party songs"
        val i5 = aiRepository.fallbackParseIntent("Play Punjabi party songs")
        assertEquals("PLAY_MUSIC", i5.intentType)
        assertTrue(i5.languages.contains("Punjabi"))
        assertTrue(i5.moods.contains("Party"))

        // "Play something relaxing"
        val i6 = aiRepository.fallbackParseIntent("Play something relaxing")
        assertEquals("PLAY_MUSIC", i6.intentType)
        assertTrue(i6.moods.contains("Calm"))

        // "Give me something energetic"
        val i7 = aiRepository.fallbackParseIntent("Give me something energetic")
        assertEquals("CHANGE_VIBE", i7.intentType)
        assertTrue(i7.moods.contains("Energetic"))

        // "Give me something new"
        val i8 = aiRepository.fallbackParseIntent("Give me something new")
        assertEquals("DISCOVERY", i8.intentType)

        // "Surprise me"
        val i9 = aiRepository.fallbackParseIntent("Surprise me")
        assertEquals("SURPRISE", i9.intentType)

        // "Next song"
        val i10 = aiRepository.fallbackParseIntent("Next song")
        assertEquals("NEXT", i10.intentType)
        assertEquals("NEXT", i10.action)

        // "Pause"
        val i11 = aiRepository.fallbackParseIntent("Pause")
        assertEquals("PAUSE", i11.intentType)

        // "Resume"
        val i12 = aiRepository.fallbackParseIntent("Resume")
        assertEquals("RESUME", i12.intentType)

        // "Like this song"
        val i13 = aiRepository.fallbackParseIntent("Like this song")
        assertEquals("LIKE", i13.intentType)

        // "Add this to my playlist"
        val i14 = aiRepository.fallbackParseIntent("Add this to my playlist")
        assertEquals("ADD_TO_PLAYLIST", i14.intentType)

        // "Make it more energetic"
        val i15 = aiRepository.fallbackParseIntent("Make it more energetic")
        assertEquals("CHANGE_VIBE", i15.intentType)
    }

    @Test
    fun testAiQueueFixAndDeduplication() = runBlocking {
        val original = TrackDto(id = "1", title = "Kesariya", artist = "Arijit Singh")
        val duplicate = TrackDto(id = "2", title = "Kesariya (From Brahmastra)", artist = "Arijit Singh")
        val unique = TrackDto(id = "3", title = "Apna Bana Le", artist = "Arijit Singh")

        val dirtyQueue = listOf(original, duplicate, unique)
        val result = aiRepository.fixQueue(dirtyQueue)

        assertTrue(result.isSuccess)
        val fixed = result.getOrNull()
        assertNotNull(fixed)
        assertEquals(2, fixed?.balancedQueue?.size)
        assertEquals(1, fixed?.removedDuplicatesCount)
    }
}
