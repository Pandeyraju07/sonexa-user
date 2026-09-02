package com.sonexa.app.audio.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlResolverTest {

    @Test
    fun blankUrlReturnsEmpty() {
        assertEquals("", NativeAudioPlaybackProvider.resolveMediaUrl(null))
        assertEquals("", NativeAudioPlaybackProvider.resolveMediaUrl("   "))
    }

    @Test
    fun localFileUrlsPassThrough() {
        val path = "/storage/emulated/0/Music/track.mp3"
        assertEquals(path, NativeAudioPlaybackProvider.resolveMediaUrl(path))
        assertEquals("file:///tmp/a.mp3", NativeAudioPlaybackProvider.resolveMediaUrl("file:///tmp/a.mp3"))
    }

    @Test
    fun httpsUrlsPassThrough() {
        val url = "https://cdn.example.com/audio.mp3"
        assertEquals(url, NativeAudioPlaybackProvider.resolveMediaUrl(url))
    }

    @Test
    fun resolverNeverInventedHttpStream() {
        val resolved = NativeAudioPlaybackProvider.resolveMediaUrl("https://legal.example/stream.m3u8")
        assertTrue(resolved.startsWith("https://"))
    }
}
