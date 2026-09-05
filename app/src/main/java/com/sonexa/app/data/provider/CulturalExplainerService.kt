package com.sonexa.app.data.provider

import com.sonexa.app.data.model.CulturalExplainer
import com.sonexa.app.data.model.CulturalExpression
import com.sonexa.app.data.model.TrackDto

object CulturalExplainerService {

    fun explainSongCulture(track: TrackDto): CulturalExplainer {
        val title = track.title
        val artist = track.artist

        return when {
            title.contains("Kesariya", ignoreCase = true) -> {
                CulturalExplainer(
                    trackId = track.id,
                    trackTitle = track.title,
                    artist = track.artist,
                    language = "Hindi / Braj Bhasha roots",
                    culturalContext = "'Kesariya' refers to the sacred saffron color, symbolizing sacrifice, eternal devotion, and divine spiritual love in Indian literary tradition.",
                    storyBehindSong = "Composed by Pritam with lyrics by Amitabh Bhattacharya, blending Sufi romanticism with modern acoustic orchestral arrangements.",
                    expressions = listOf(
                        CulturalExpression(
                            phrase = "Kesariya tera ishq hai piya",
                            pronunciation = "Kay-sa-ree-ya tay-ra ish-q hai pee-ya",
                            literalMeaning = "Your love is colored saffron, my beloved",
                            culturalSignificance = "Saffron signifies complete surrender, purity, and passion in traditional poetry."
                        ),
                        CulturalExpression(
                            phrase = "Rang jaaun jo main haath lagaun",
                            pronunciation = "Rung jaoon jo main haath la-ga-oon",
                            literalMeaning = "I am stained in color the moment I touch it",
                            culturalSignificance = "Metaphor for being deeply transformed by true connection."
                        )
                    ),
                    traditionalInstruments = listOf("Rabab", "Dholak", "Flute (Bansuri)", "Acoustic Guitar"),
                    emotionalEssence = "Devotional romantic euphoria"
                )
            }
            title.contains("Chaleya", ignoreCase = true) || artist.contains("Arijit", ignoreCase = true) -> {
                CulturalExplainer(
                    trackId = track.id,
                    trackTitle = track.title,
                    artist = track.artist,
                    language = "Hindustani / Punjabi nuances",
                    culturalContext = "Captures the poetic concept of 'Deewangi' (loving madness) prevalent in classic Urdu ghazal and Punjabi folk music.",
                    storyBehindSong = "Anirudh Ravichander's fusion of contemporary synth grooves with classical melodic phrasing.",
                    expressions = listOf(
                        CulturalExpression(
                            phrase = "Ishq mein dil bana hai",
                            pronunciation = "Ishq mein dil ba-na hai",
                            literalMeaning = "The heart is forged in the fire of passion",
                            culturalSignificance = "Classic trope in romantic literature where trials temper devotion."
                        )
                    ),
                    traditionalInstruments = listOf("Shehnai", "Tabla", "Synth Bass"),
                    emotionalEssence = "High-spirited festive romance"
                )
            }
            artist.contains("Diljit", ignoreCase = true) || track.language.contains("Punjabi", ignoreCase = true) -> {
                CulturalExplainer(
                    trackId = track.id,
                    trackTitle = track.title,
                    artist = track.artist,
                    language = "Punjabi",
                    culturalContext = "Rooted in the vibrant 'Malwa & Majha' folk culture of Punjab, celebrating hospitality, rural pride, and chivalric romance.",
                    storyBehindSong = "Combines traditional high-pitch Punjabi folk hooks with modern western basslines and trap percussions.",
                    expressions = listOf(
                        CulturalExpression(
                            phrase = "Vibe",
                            pronunciation = "Vibe",
                            literalMeaning = "Energy & Atmosphere",
                            culturalSignificance = "Reflects the modern urban Punjabi diaspora blending global street culture with Punjabi warmth."
                        )
                    ),
                    traditionalInstruments = listOf("Tumbi", "Dhol", "Algoza", "Harmonium"),
                    emotionalEssence = "Celebratory, energetic, authentic pride"
                )
            }
            else -> {
                CulturalExplainer(
                    trackId = track.id,
                    trackTitle = track.title,
                    artist = track.artist,
                    language = track.language.ifBlank { "Universal" },
                    culturalContext = "This song draws from contemporary acoustic narrative storytelling and melodic harmony traditions.",
                    storyBehindSong = "Written and produced to evoke authentic emotional resonance through minimalist instrumentation and vocal intimacy.",
                    expressions = listOf(
                        CulturalExpression(
                            phrase = track.title,
                            pronunciation = track.title,
                            literalMeaning = "Core melodic anchor",
                            culturalSignificance = "Represents the central thematic hook of the narrative."
                        )
                    ),
                    traditionalInstruments = listOf("Acoustic Strings", "Percussion"),
                    emotionalEssence = "Reflective and emotionally evocative"
                )
            }
        }
    }
}
