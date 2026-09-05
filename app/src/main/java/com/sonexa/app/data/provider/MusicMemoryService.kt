package com.sonexa.app.data.provider

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.MusicMemory
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object MusicMemoryService {
    private val gson = RetrofitClient.gson
    private const val PREFS_NAME = "zynera_music_memories"
    private const val KEY_MEMORIES = "user_saved_memories"

    private val _memories = MutableStateFlow<List<MusicMemory>>(emptyList())
    val memories: StateFlow<List<MusicMemory>> = _memories.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MEMORIES, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<MusicMemory>>() {}.type
                val loaded: List<MusicMemory> = gson.fromJson(json, type)
                _memories.value = loaded.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialize default sample memories if empty so user immediately experiences the feature
        if (_memories.value.isEmpty()) {
            val sampleMemories = listOf(
                MusicMemory(
                    id = "mem_goa_2026",
                    title = "Goa Highway Drive",
                    description = "Cruising along the Konkan coast during sunset with the windows down.",
                    timestamp = System.currentTimeMillis() - (14L * 24 * 3600 * 1000),
                    mood = "Nostalgic & Warm",
                    locationTag = "Goa Coastal Highway",
                    dominantArtist = "Prateek Kuhad",
                    tags = listOf("Road Trip", "Sunset", "Acoustic"),
                    tracks = listOf(
                        TrackDto(
                            id = "mem_t1",
                            title = "Kasoor",
                            artist = "Prateek Kuhad",
                            album = "Kasoor Single",
                            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                            audioUrl = "https://audius-discovery-1.cultur3stake.com/v1/tracks/sample1/stream"
                        ),
                        TrackDto(
                            id = "mem_t2",
                            title = "Baarishein",
                            artist = "Anuv Jain",
                            album = "Baarishein Single",
                            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                            audioUrl = "https://audius-discovery-1.cultur3stake.com/v1/tracks/sample2/stream"
                        ),
                        TrackDto(
                            id = "mem_t3",
                            title = "Kho Gaye Hum Kahan",
                            artist = "Jasleen Royal & Prateek Kuhad",
                            album = "Baar Baar Dekho",
                            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                            audioUrl = "https://audius-discovery-1.cultur3stake.com/v1/tracks/sample3/stream"
                        )
                    ),
                    replayCount = 4
                ),
                MusicMemory(
                    id = "mem_college_late_night",
                    title = "Hostel Terrace Night Session",
                    description = "Late night deep conversations under the stars with tea and acoustic melodies.",
                    timestamp = System.currentTimeMillis() - (60L * 24 * 3600 * 1000),
                    mood = "Soulful & Melancholic",
                    locationTag = "Campus Terrace",
                    dominantArtist = "Arijit Singh",
                    tags = listOf("Late Night", "College", "Heartfelt"),
                    tracks = listOf(
                        TrackDto(
                            id = "mem_t4",
                            title = "Ilahi",
                            artist = "Arijit Singh",
                            album = "Yeh Jawaani Hai Deewani",
                            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600"
                        ),
                        TrackDto(
                            id = "mem_t5",
                            title = "Kabira",
                            artist = "Tochi Raina & Rekha Bhardwaj",
                            album = "Yeh Jawaani Hai Deewani",
                            coverUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600"
                        )
                    ),
                    replayCount = 7
                )
            )
            saveMemories(context, sampleMemories)
        }
    }

    fun createMemory(
        context: Context,
        title: String,
        description: String = "",
        currentTrack: TrackDto? = null,
        currentQueue: List<TrackDto> = emptyList(),
        mood: String = "Vibrant",
        locationTag: String? = null,
        photoUris: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): MusicMemory {
        val memoryTracks = mutableListOf<TrackDto>()
        if (currentTrack != null) memoryTracks.add(currentTrack)
        memoryTracks.addAll(currentQueue.filter { it.id != currentTrack?.id }.take(10))

        val dominantArtist = currentTrack?.artist ?: memoryTracks.firstOrNull()?.artist.orEmpty()

        val memory = MusicMemory(
            id = "mem_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            title = title.ifBlank { "Memory — ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}" },
            description = description,
            timestamp = System.currentTimeMillis(),
            mood = mood,
            locationTag = locationTag,
            photoUris = photoUris,
            tracks = memoryTracks,
            totalDurationMs = memoryTracks.sumOf { it.durationMs.toLong() },
            replayCount = 1,
            tags = tags,
            dominantArtist = dominantArtist
        )

        val updated = listOf(memory) + _memories.value
        saveMemories(context, updated)
        return memory
    }

    fun incrementReplayCount(context: Context, memoryId: String) {
        val updated = _memories.value.map { mem ->
            if (mem.id == memoryId) mem.copy(replayCount = mem.replayCount + 1) else mem
        }
        saveMemories(context, updated)
    }

    fun deleteMemory(context: Context, memoryId: String) {
        val updated = _memories.value.filter { it.id != memoryId }
        saveMemories(context, updated)
    }

    fun findMemoryByQuery(query: String): MusicMemory? {
        val q = query.lowercase(java.util.Locale.ROOT)
        return _memories.value.firstOrNull { mem ->
            mem.title.lowercase(java.util.Locale.ROOT).contains(q) ||
            mem.tags.any { it.lowercase(java.util.Locale.ROOT).contains(q) } ||
            mem.mood.lowercase(java.util.Locale.ROOT).contains(q) ||
            mem.locationTag?.lowercase(java.util.Locale.ROOT)?.contains(q) == true
        }
    }

    private fun saveMemories(context: Context, list: List<MusicMemory>) {
        _memories.value = list
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(list)
            prefs.edit().putString(KEY_MEMORIES, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
