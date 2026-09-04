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
            return if (obj.has(key) && !obj.get(key).isJsonNull) obj.get(key).asString else default
        }

        fun getLong(key: String, default: Long = 0L): Long {
            return if (obj.has(key) && !obj.get(key).isJsonNull) {
                runCatching { obj.get(key).asLong }.getOrDefault(default)
            } else default
        }

        fun getDouble(key: String, default: Double = 0.0): Double {
            return if (obj.has(key) && !obj.get(key).isJsonNull) {
                runCatching { obj.get(key).asDouble }.getOrDefault(default)
            } else default
        }

        fun getBoolean(key: String, default: Boolean = false): Boolean {
            return if (obj.has(key) && !obj.get(key).isJsonNull) {
                runCatching { obj.get(key).asBoolean }.getOrDefault(default)
            } else default
        }

        fun getStringList(key: String): List<String> {
            if (!obj.has(key) || obj.get(key).isJsonNull || !obj.get(key).isJsonArray) {
                return emptyList()
            }
            val arr = obj.getAsJsonArray(key)
            val list = mutableListOf<String>()
            for (el in arr) {
                if (!el.isJsonNull) list.add(el.asString)
            }
            return list
        }

        return TrackDto(
            id = getString("id"),
            title = getString("title"),
            artist = getString("artist"),
            album = getString("album"),
            durationMs = getLong("durationMs"),
            audioUrl = getString("audioUrl"),
            coverUrl = getString("coverUrl"),
            playsCount = getString("playsCount"),
            isLiked = getBoolean("isLiked"),
            provider = getString("provider", "zynera"),
            providerTrackId = getString("providerTrackId"),
            videoId = getString("videoId"),
            providerUrl = getString("providerUrl"),
            isPlayable = getBoolean("isPlayable", true),
            providerType = getString("providerType", "audio"),
            availability = getString("availability", "AVAILABLE"),
            availableProviders = getStringList("availableProviders"),
            channelTitle = getString("channelTitle"),
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
