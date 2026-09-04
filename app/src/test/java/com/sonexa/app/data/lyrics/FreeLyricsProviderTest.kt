package com.sonexa.app.data.lyrics

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeLyricsProviderTest {

    @Test
    fun testLyricsResolutionForPopularTracks() = runBlocking {
        val provider = FreeLyricsProvider()

        // Test English track resolution
        val resultEnglish = provider.getLyrics("test_1", "Yellow", "Coldplay", 269000L)
        assertNotNull("Yellow lyrics should not be null", resultEnglish)
        assertTrue("Yellow lyrics should have content", resultEnglish?.plainText?.contains("yellow", ignoreCase = true) == true || resultEnglish?.lines?.isNotEmpty() == true)

        // Test Bollywood track resolution
        val resultHindi = provider.getLyrics("test_2", "Apna Bana Le - Bhediya | Varun Dhawan | Arijit Singh", "Arijit Singh, Sachin-Jigar", 262000L)
        assertNotNull("Apna Bana Le lyrics should not be null", resultHindi)
        assertTrue("Apna Bana Le lyrics should have lines or plain text", (resultHindi?.lines?.isNotEmpty() == true) || resultHindi?.plainText?.isNotBlank() == true)
    }
}
