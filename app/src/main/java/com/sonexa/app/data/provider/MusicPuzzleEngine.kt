package com.sonexa.app.data.provider

import com.sonexa.app.data.model.MusicPuzzleChallenge
import com.sonexa.app.data.model.PuzzleType
import com.sonexa.app.data.model.TrackDto

object MusicPuzzleEngine {

    private val puzzles = listOf(
        MusicPuzzleChallenge(
            id = "puzzle_1",
            title = "Daily Link: Arijit Singh → A.R. Rahman",
            type = PuzzleType.ARTIST_LINK,
            description = "Connect Arijit Singh to A.R. Rahman in 3 steps or fewer.",
            startEntity = "Arijit Singh",
            targetEntity = "A.R. Rahman",
            maxSteps = 3,
            hints = listOf(
                "Hint 1: Think of a celebrated music director who scored 'Barfi' and 'Yeh Jawaani Hai Deewani'.",
                "Hint 2: Both composers frequently collaborate with singer Mohit Chauhan."
            ),
            solutionPath = listOf("Arijit Singh", "Pritam", "A.R. Rahman"),
            rewardsPlaylistTitle = "Arijit × Rahman Symphonic Fusion"
        ),
        MusicPuzzleChallenge(
            id = "puzzle_2",
            title = "Missing Link: The 90s Melody Chain",
            type = PuzzleType.BOLLYWOOD_CHAIN,
            description = "Identify the common composer between 'Dil Hai Ke Manta Nahin' and 'Aashiqui'.",
            startEntity = "Kumar Sanu",
            targetEntity = "Nadeem-Shravan",
            maxSteps = 2,
            hints = listOf(
                "Hint 1: A legendary composer duo who dominated the 1990s Hindi film music landscape."
            ),
            solutionPath = listOf("Kumar Sanu", "Nadeem-Shravan", "Alka Yagnik"),
            rewardsPlaylistTitle = "Golden 90s Nostalgia Mix"
        )
    )

    fun getDailyPuzzle(): MusicPuzzleChallenge {
        val dayIndex = ((System.currentTimeMillis() / (24 * 3600 * 1000)) % puzzles.size).toInt()
        return puzzles[dayIndex]
    }

    fun verifyGuess(puzzle: MusicPuzzleChallenge, stepEntity: String): Boolean {
        return puzzle.solutionPath.any { it.equals(stepEntity.trim(), ignoreCase = true) }
    }
}
