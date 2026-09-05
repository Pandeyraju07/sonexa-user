package com.sonexa.app.data.provider

import com.sonexa.app.data.model.TimeMachineEraData
import com.sonexa.app.data.model.TrackDto

class MusicTimeMachineEngine(
    private val aggregationEngine: MusicAggregationEngine = MusicAggregationEngine()
) {

    suspend fun travelToYear(year: Int): TimeMachineEraData {
        val (title, desc, milestones) = when (year) {
            2016 -> Triple(
                "The 2016 Golden Wave",
                "A transformative era for Bollywood melodies, tropical house, and acoustic indie emergence.",
                listOf(
                    "Ae Dil Hai Mushkil redefined modern Hindi cinematic romanticism",
                    "Arijit Singh established an unrivaled dominance across Indian charts",
                    "Global breakthrough of streaming apps transformed indie music access"
                )
            )
            2013 -> Triple(
                "The 2013 Melodic Revolution",
                "The year of Aashiqui 2, Yeh Jawaani Hai Deewani, and unforgettable soul tracks.",
                listOf(
                    "'Tum Hi Ho' became an immortal romantic anthem across the subcontinent",
                    "EDM festivals exploded across metropolitan centers in India",
                    "Acoustic singer-songwriters began building massive independent followings"
                )
            )
            2007 -> Triple(
                "The 2007 Fusion Renaissance",
                "Pritam, Shankar-Ehsaan-Loy, and A.R. Rahman delivered timeless masterpieces.",
                listOf(
                    "'Jab We Met' and 'Guru' set new artistic benchmarks in Indian film music",
                    "Sufi-rock fusion reached mainstream radio saturation",
                    "Pop-rock bands shaped campus youth culture"
                )
            )
            else -> Triple(
                "The Year $year Retrospective",
                "Authentic soundscapes, beloved chartbusters, and nostalgic memories from $year.",
                listOf(
                    "Unforgettable anthems that defined the cultural zeitgeist of $year",
                    "Hidden gems and acoustic milestones from the era"
                )
            )
        }

        val chartQuery = "$year Bollywood Top Hits Songs"
        val gemQuery = "$year Acoustic Indie Classics"
        val globalQuery = "$year Global Billboard Hits"

        val chartTracks = aggregationEngine.searchAll(chartQuery).tracks.take(6)
        val gemTracks = aggregationEngine.searchAll(gemQuery).tracks.take(4)
        val globalTracks = aggregationEngine.searchAll(globalQuery).tracks.take(4)

        return TimeMachineEraData(
            year = year,
            eraTitle = title,
            description = desc,
            chartbusters = chartTracks,
            hiddenGems = gemTracks,
            globalSoundtrack = globalTracks,
            historicalMilestones = milestones
        )
    }
}
