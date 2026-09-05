package com.sonexa.app.data.provider

import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.data.model.LifeSoundtrackEra
import com.sonexa.app.data.model.LifeSoundtrackOverview
import com.sonexa.app.data.model.TrackDto

object LifeSoundtrackService {

    fun generateLifeSoundtrack(historyTracks: List<TrackDto> = emptyList()): LifeSoundtrackOverview {
        val liked = LikedSongsStore.likedSongs.value
        val allTracks = (liked + historyTracks).distinctBy { it.id }

        val currentMonthTracks = allTracks.take(8).ifEmpty {
            listOf(
                TrackDto(id = "era_1", title = "Pehla Nasha", artist = "Udit Narayan", album = "Jo Jeeta Wohi Sikandar"),
                TrackDto(id = "era_2", title = "Tum Se Hi", artist = "Mohit Chauhan", album = "Jab We Met"),
                TrackDto(id = "era_3", title = "Safarnama", artist = "Lucky Ali", album = "Tamasha")
            )
        }

        val currentEra = LifeSoundtrackEra(
            id = "era_current_2026",
            eraTitle = "The Romantic Awakening",
            monthYearLabel = "September 2026 — Present",
            description = "Characterized by soaring acoustic melodies, soulful vocal hooks, and introspective evening sessions.",
            topTracks = currentMonthTracks,
            dominantMood = "Soulful Romantic",
            energyCurve = listOf(0.42f, 0.58f, 0.72f, 0.65f, 0.50f),
            primaryGenres = listOf("Acoustic Hindi", "Bollywood Melodic", "Indie Folk")
        )

        val pastEras = listOf(
            LifeSoundtrackEra(
                id = "era_aug_2026",
                eraTitle = "Late Night Monsoon Phase",
                monthYearLabel = "August 2026",
                description = "Rainy night drives, lo-fi beats, and warm acoustic indie discoveries.",
                topTracks = allTracks.drop(3).take(5),
                dominantMood = "Atmospheric Lo-Fi",
                energyCurve = listOf(0.35f, 0.40f, 0.55f, 0.38f),
                primaryGenres = listOf("Lo-Fi Chill", "Monsoon Melodies", "Acoustic Pop")
            ),
            LifeSoundtrackEra(
                id = "era_july_2026",
                eraTitle = "Goa Road Trip & High Energy",
                monthYearLabel = "July 2026",
                description = "High energy highway anthems, upbeat electronic drops, and singalong chartbusters.",
                topTracks = allTracks.drop(5).take(5),
                dominantMood = "Energetic & Euphoric",
                energyCurve = listOf(0.70f, 0.85f, 0.95f, 0.80f),
                primaryGenres = listOf("EDM", "Punjabi Pop", "Dance Chartbusters")
            ),
            LifeSoundtrackEra(
                id = "era_june_2026",
                eraTitle = "Deep Focus & Study Era",
                monthYearLabel = "June 2026",
                description = "Continuous instrumental ambient soundscapes and minimal acoustic arrangements.",
                topTracks = allTracks.drop(2).take(4),
                dominantMood = "Calm Focus",
                energyCurve = listOf(0.25f, 0.30f, 0.35f, 0.28f),
                primaryGenres = listOf("Ambient", "Piano Instrumental", "Post-Rock")
            )
        )

        return LifeSoundtrackOverview(
            currentEra = currentEra,
            pastEras = pastEras,
            totalListeningHours = 142.5,
            topGenreEvolution = "Ambient Focus → Energetic Trip → Monsoon Lo-Fi → Romantic Awakening"
        )
    }
}
