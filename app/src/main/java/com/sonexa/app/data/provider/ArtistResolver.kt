package com.sonexa.app.data.provider

import com.sonexa.app.data.model.ResolvedArtist
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.Normalizer
import java.util.Locale

class ArtistResolver(
    private val audiusProvider: AudiusMusicProvider = AudiusMusicProvider(),
    private val saavnProvider: JioSaavnMusicProvider = JioSaavnMusicProvider()
) {

    // Common artist canonical aliases & transliteration mapping
    private val knownCanonicalAliases = mapOf(
        "arijit" to "Arijit Singh",
        "arijit singh" to "Arijit Singh",
        "arjit singh" to "Arijit Singh",
        "arjit" to "Arijit Singh",
        "अरिजीत सिंह" to "Arijit Singh",
        "अरिजीत" to "Arijit Singh",

        "shreya" to "Shreya Ghoshal",
        "shreya ghoshal" to "Shreya Ghoshal",
        "श्रेया घोषाल" to "Shreya Ghoshal",

        "pritam" to "Pritam",
        "pritam chakraborty" to "Pritam",

        "ar rahman" to "A.R. Rahman",
        "a r rahman" to "A.R. Rahman",
        "rahman" to "A.R. Rahman",
        "ar rehman" to "A.R. Rahman",

        "atif" to "Atif Aslam",
        "atif aslam" to "Atif Aslam",

        "diljit" to "Diljit Dosanjh",
        "diljit dosanjh" to "Diljit Dosanjh",

        "sidhu" to "Sidhu Moose Wala",
        "sidhu moosewala" to "Sidhu Moose Wala",
        "sidhu moose wala" to "Sidhu Moose Wala",

        "badshah" to "Badshah",
        "yo yo honey singh" to "Yo Yo Honey Singh",
        "honey singh" to "Yo Yo Honey Singh",

        "weeknd" to "The Weeknd",
        "the weeknd" to "The Weeknd",
        "abel tesfaye" to "The Weeknd",

        "taylor" to "Taylor Swift",
        "taylor swift" to "Taylor Swift",

        "ed sheeran" to "Ed Sheeran",
        "sheeran" to "Ed Sheeran",

        "drake" to "Drake",
        "dua lipa" to "Dua Lipa",
        "billie eilish" to "Billie Eilish",
        "justin bieber" to "Justin Bieber",
        "ariana grande" to "Ariana Grande",
        "post malone" to "Post Malone",
        "alan walker" to "Alan Walker",
        "martin garrix" to "Martin Garrix",
        "coldplay" to "Coldplay",
        "imagine dragons" to "Imagine Dragons"
    )

    suspend fun resolve(query: String): ResolvedArtist = coroutineScope {
        val raw = query.trim()
        val normalized = normalizeString(raw)

        // 1. Direct Alias & Dictionary Match
        val canonicalFromName = knownCanonicalAliases[normalized]
            ?: knownCanonicalAliases[raw.lowercase(Locale.ROOT)]

        val targetName = canonicalFromName ?: raw

        // 2. Query Audius & Saavn concurrently to gather rich artist metadata
        val audiusDeferred = async { audiusProvider.resolveArtist(targetName).getOrNull() }
        val saavnTracksDeferred = async { saavnProvider.search(targetName, limit = 15).getOrDefault(emptyList()) }

        val audiusResult = audiusDeferred.await()
        val saavnTracks = saavnTracksDeferred.await()

        val providerIds = mutableMapOf<String, String>()
        if (audiusResult != null && audiusResult.providerIds.containsKey("audius")) {
            providerIds["audius"] = audiusResult.providerIds["audius"]!!
        }

        // Determine highest quality image and metadata
        val firstTrack = saavnTracks.firstOrNull()
        val resolvedName = when {
            canonicalFromName != null -> canonicalFromName
            audiusResult != null && audiusResult.canonicalName.isNotBlank() -> audiusResult.canonicalName
            firstTrack != null && isCloseMatch(firstTrack.artist, targetName) -> firstTrack.artist.split(",", "&", "feat.").first().trim()
            else -> targetName
        }

        val imageUrl = when {
            audiusResult?.imageUrl?.isNotBlank() == true -> audiusResult.imageUrl
            firstTrack?.effectiveCoverUrl?.isNotBlank() == true -> firstTrack.effectiveCoverUrl
            else -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
        }

        val related = deriveRelatedArtists(resolvedName)

        ResolvedArtist(
            canonicalName = resolvedName,
            canonicalId = "art_" + normalizeId(resolvedName),
            providerIds = providerIds,
            aliases = listOf(raw, normalized, resolvedName).distinct(),
            confidence = if (canonicalFromName != null) 1.0 else if (audiusResult != null) 0.92 else 0.85,
            bio = if (audiusResult?.bio?.isNotBlank() == true) audiusResult.bio else "Discover top hits, albums, collaborations, and essential tracks by $resolvedName.",
            imageUrl = imageUrl,
            genres = listOf("Pop", "Romantic", "Playback", "Bollywood", "Indie"),
            languages = listOf("Hindi", "English", "Punjabi"),
            relatedArtists = related,
            followersCount = audiusResult?.followersCount ?: 4800000L,
            isVerified = true
        )
    }

    private fun isCloseMatch(artistName: String, query: String): Boolean {
        val a = normalizeString(artistName)
        val q = normalizeString(query)
        return a.contains(q) || q.contains(a)
    }

    private fun normalizeString(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun normalizeId(name: String): String {
        return name.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "_")
    }

    private fun deriveRelatedArtists(name: String): List<String> {
        val lower = name.lowercase(Locale.ROOT)
        return when {
            lower.contains("arijit") -> listOf("Atif Aslam", "Pritam", "Mohit Chauhan", "Armaan Malik", "Jubin Nautiyal", "Shreya Ghoshal")
            lower.contains("shreya") -> listOf("Sunidhi Chauhan", "Arijit Singh", "Alka Yagnik", "Shankar Mahadevan", "Neeti Mohan")
            lower.contains("pritam") -> listOf("Arijit Singh", "Vishal-Shekhar", "Sachin-Jigar", "Amit Trivedi", "KK")
            lower.contains("atif") -> listOf("Arijit Singh", "Mustafa Zahid", "Ali Zafar", "Rahat Fateh Ali Khan", "Shafqat Amanat Ali")
            lower.contains("diljit") -> listOf("Sidhu Moose Wala", "AP Dhillon", "Karan Aujla", "Guru Randhawa", "Ammy Virk")
            lower.contains("weeknd") -> listOf("Drake", "Post Malone", "Dua Lipa", "Travis Scott", "Bruno Mars")
            lower.contains("taylor") -> listOf("Olivia Rodrigo", "Lana Del Rey", "Billie Eilish", "Katy Perry", "Selena Gomez")
            lower.contains("ed sheeran") -> listOf("Shawn Mendes", "James Arthur", "Lewis Capaldi", "Charlie Puth", "Sam Smith")
            else -> listOf("Arijit Singh", "The Weeknd", "Dua Lipa", "Ed Sheeran", "Shreya Ghoshal")
        }
    }
}
