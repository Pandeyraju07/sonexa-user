package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TrackDto
import java.util.Locale
import kotlin.math.abs

class TrackDeduplicationService {

    fun deduplicate(tracks: List<TrackDto>): List<TrackDto> {
        if (tracks.isEmpty()) return emptyList()

        val grouped = mutableMapOf<String, MutableList<TrackDto>>()

        for (track in tracks) {
            val versionTag = detectVersionType(track.title)
            val baseKey = createNormalizedKey(track.title, track.artist)
            val fullKey = "$baseKey::$versionTag"
            grouped.getOrPut(fullKey) { mutableListOf() }.add(track)
        }

        val deduplicated = mutableListOf<TrackDto>()

        for ((_, cluster) in grouped) {
            if (cluster.size == 1) {
                val single = cluster[0]
                deduplicated.add(
                    single.copy(
                        versionType = detectVersionType(single.title),
                        qualityTier = determineQualityTier(single)
                    )
                )
            } else {
                // Secondary check: cluster tracks with similar duration (within 4 seconds)
                val durationSubClusters = mutableListOf<MutableList<TrackDto>>()
                for (item in cluster) {
                    val match = durationSubClusters.firstOrNull { existing ->
                        val refDur = existing.first().durationMs
                        refDur == 0L || item.durationMs == 0L || abs(refDur - item.durationMs) <= 4000L
                    }
                    if (match != null) {
                        match.add(item)
                    } else {
                        durationSubClusters.add(mutableListOf(item))
                    }
                }

                for (subCluster in durationSubClusters) {
                    // Pick the highest quality streamable track
                    val primary = subCluster.firstOrNull { it.isOfficial && it.isPlayable }
                        ?: subCluster.firstOrNull { it.isPlayable && it.audioUrl.isNotBlank() }
                        ?: subCluster.first()

                    val allProviders = subCluster.map { it.provider.replaceFirstChar { c -> c.uppercase() } }.distinct()

                    deduplicated.add(
                        primary.copy(
                            availableProviders = allProviders,
                            versionType = detectVersionType(primary.title),
                            qualityTier = determineQualityTier(primary)
                        )
                    )
                }
            }
        }

        return deduplicated
    }

    fun detectVersionType(title: String): String {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("acoustic") || lower.contains("unplugged") -> "Acoustic"
            lower.contains("remix") || lower.contains("club mix") || lower.contains("edm mix") -> "Remix"
            lower.contains("live") || lower.contains("concert") || lower.contains("tour") -> "Live"
            lower.contains("instrumental") || lower.contains("karaoke") -> "Instrumental"
            lower.contains("lo-fi") || lower.contains("lofi") || lower.contains("slowed") -> "Lo-Fi"
            lower.contains("radio edit") -> "Radio Edit"
            lower.contains("extended") -> "Extended"
            else -> "Original"
        }
    }

    private fun determineQualityTier(track: TrackDto): String {
        return when {
            track.isOfficial && track.isPlayable && track.effectiveCoverUrl.isNotBlank() -> "EXACT_MATCH"
            track.isPlayable && track.audioUrl.isNotBlank() -> "STRONG_MATCH"
            else -> "DISCOVERY"
        }
    }

    private fun createNormalizedKey(title: String, artist: String): String {
        val cleanTitle = cleanString(stripVersionLabels(title))
        val cleanArtist = cleanString(artist.split(",", "&", "feat.", "ft.").firstOrNull().orEmpty())
        return "$cleanTitle::$cleanArtist"
    }

    private fun stripVersionLabels(title: String): String {
        return title
            .replace(Regex("""(?i)\(.*?remix.*?\)|\(.*?\)|\[.*?\]"""), "")
            .replace(Regex("""(?i)-.*?remix.*|-.*?audio.*|-.*?official.*"""), "")
            .trim()
    }

    private fun cleanString(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
