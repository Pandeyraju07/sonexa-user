package com.sonexa.app.data.api

import com.google.gson.*
import com.sonexa.app.data.model.TrackDto
import java.lang.reflect.Type

class SafeTrackDtoDeserializer : JsonDeserializer<TrackDto> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TrackDto {
        if (json == null || !json.isJsonObject) {
            return TrackDto()
        }
        val obj = json.asJsonObject

        fun getString(key: String, default: String = ""): String {
            if (!obj.has(key) || obj.get(key).isJsonNull) return default
            val el = obj.get(key)
            return runCatching {
                if (el.isJsonPrimitive) el.asString else el.toString()
            }.getOrDefault(default)
        }

        fun getLong(key: String, default: Long = 0L): Long {
            if (!obj.has(key) || obj.get(key).isJsonNull) return default
            return runCatching { obj.get(key).asLong }.getOrDefault(default)
        }

        fun getDouble(key: String, default: Double = 0.0): Double {
            if (!obj.has(key) || obj.get(key).isJsonNull) return default
            return runCatching { obj.get(key).asDouble }.getOrDefault(default)
        }

        fun getBoolean(key: String, default: Boolean = false): Boolean {
            if (!obj.has(key) || obj.get(key).isJsonNull) return default
            return runCatching { obj.get(key).asBoolean }.getOrDefault(default)
        }

        fun getStringList(key: String): List<String> {
            if (!obj.has(key) || obj.get(key).isJsonNull || !obj.get(key).isJsonArray) {
                return emptyList()
            }
            val arr = obj.getAsJsonArray(key)
            val list = mutableListOf<String>()
            for (el in arr) {
                if (!el.isJsonNull) {
                    val str = runCatching { if (el.isJsonPrimitive) el.asString else el.toString() }.getOrNull()
                    if (!str.isNullOrBlank()) list.add(str)
                }
            }
            return list
        }

        return TrackDto(
            id = getString("id"),
            title = getString("title"),
            artist = getString("artist"),
            album = getString("album"),
            durationMs = getLong("durationMs"),
            audioUrl = getString("audioUrl").ifBlank { getString("audio_url") },
            coverUrl = getString("coverUrl").ifBlank { getString("cover_url").ifBlank { getString("image") } },
            playsCount = getString("playsCount").ifBlank { getString("plays_count") },
            isLiked = getBoolean("isLiked"),
            provider = getString("provider", "zynera"),
            providerTrackId = getString("providerTrackId").ifBlank { getString("provider_track_id") },
            videoId = getString("videoId").ifBlank { getString("video_id") },
            providerUrl = getString("providerUrl").ifBlank { getString("provider_url") },
            isPlayable = getBoolean("isPlayable", true),
            providerType = getString("providerType", "audio").ifBlank { getString("provider_type", "audio") },
            availability = getString("availability", "AVAILABLE"),
            availableProviders = getStringList("availableProviders"),
            channelTitle = getString("channelTitle").ifBlank { getString("channel_title") },
            isOfficial = getBoolean("isOfficial"),
            bpm = getDouble("bpm", 110.0),
            energy = getDouble("energy", 0.55),
            mood = getString("mood", "Chill"),
            moods = getStringList("moods"),
            genres = getStringList("genres"),
            language = getString("language", "Hindi"),
            eraDecade = getString("eraDecade", "2020s"),
            acousticness = getDouble("acousticness", 0.45),
            danceability = getDouble("danceability", 0.60),
            isInstrumental = getBoolean("isInstrumental"),
            tags = getStringList("tags"),
            versionType = getString("versionType", "Original"),
            recommendationReason = getString("recommendationReason"),
            qualityTier = getString("qualityTier", "EXACT_MATCH")
        )
    }
}

