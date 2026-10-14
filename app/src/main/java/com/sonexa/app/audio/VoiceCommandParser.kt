package com.sonexa.app.audio

import java.util.Locale
import java.util.regex.Pattern

sealed class VoiceCommand {
    data class PlayTrack(val trackTitle: String, val artistName: String? = null, val rawQuery: String) : VoiceCommand()
    data class PlayArtist(val artistName: String) : VoiceCommand()
    data class PlayAlbum(val albumTitle: String) : VoiceCommand()
    data class PlayPlaylist(val playlistName: String) : VoiceCommand()
    data class PlayAudiobook(val bookTitle: String) : VoiceCommand()
    data class PlayPodcast(val podcastTitle: String) : VoiceCommand()
    data class SearchContent(val query: String) : VoiceCommand()

    object Pause : VoiceCommand()
    object Resume : VoiceCommand()
    object Next : VoiceCommand()
    object Previous : VoiceCommand()
    object Shuffle : VoiceCommand()
    object Repeat : VoiceCommand()
    object LikeTrack : VoiceCommand()

    data class SetPlaybackSpeed(val speed: Float) : VoiceCommand()
    data class SetSleepTimer(val minutes: Int) : VoiceCommand()

    object NextChapter : VoiceCommand()
    object PreviousChapter : VoiceCommand()
    object SaveBookmark : VoiceCommand()

    data class Unsupported(val rawQuery: String) : VoiceCommand()
}

data class ParsedVoiceAction(
    val command: VoiceCommand,
    val spokenConfirmation: String,
    val intentType: String
)

object VoiceCommandParser {

    fun parse(rawText: String?): ParsedVoiceAction {
        val query = rawText?.trim().orEmpty()
        if (query.isBlank()) {
            return ParsedVoiceAction(
                command = VoiceCommand.Unsupported(""),
                spokenConfirmation = "I didn't hear anything. Please try speaking again.",
                intentType = "EMPTY"
            )
        }

        val text = query.lowercase(Locale.ROOT)

        // 1. Direct Playback Controls
        if (text == "pause" || text.contains("pause music") || text.contains("stop music") || text == "stop" || text.contains("ruk") || text.contains("roko")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Pause,
                spokenConfirmation = "Paused.",
                intentType = "PAUSE"
            )
        }

        if (text == "resume" || text.contains("resume music") || text.contains("play again") || text == "continue" || text.contains("chalao") || text.contains("shuru karo")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Resume,
                spokenConfirmation = "Resuming playback.",
                intentType = "RESUME"
            )
        }

        if (text == "next" || text.contains("next song") || text.contains("skip this") || text.contains("skip") || text.contains("agla") || text.contains("change song")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Next,
                spokenConfirmation = "Skipping to next track.",
                intentType = "NEXT"
            )
        }

        if (text == "previous" || text.contains("previous song") || text.contains("pichla") || text.contains("go back")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Previous,
                spokenConfirmation = "Playing previous track.",
                intentType = "PREVIOUS"
            )
        }

        if (text.contains("shuffle") || text.contains("random")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Shuffle,
                spokenConfirmation = "Shuffle toggled.",
                intentType = "SHUFFLE"
            )
        }

        if (text.contains("repeat") || text.contains("loop")) {
            return ParsedVoiceAction(
                command = VoiceCommand.Repeat,
                spokenConfirmation = "Repeat mode toggled.",
                intentType = "REPEAT"
            )
        }

        if (text.contains("like this") || text.contains("favorite") || text.contains("dil do") || text.contains("add to favorites") || text.contains("liked song")) {
            return ParsedVoiceAction(
                command = VoiceCommand.LikeTrack,
                spokenConfirmation = "Added to your Liked Songs.",
                intentType = "LIKE_TRACK"
            )
        }

        // 2. Playback Speed ("speed to 1.5", "playback speed 2x", "1.25x speed")
        val speedPattern = Pattern.compile("(speed|playback speed)\\s*(to)?\\s*([0-9.]+)\\s*(x|times)?")
        val speedMatcher = speedPattern.matcher(text)
        if (speedMatcher.find()) {
            val spdVal = speedMatcher.group(3)?.toFloatOrNull() ?: 1.0f
            val clamped = spdVal.coerceIn(0.5f, 2.5f)
            return ParsedVoiceAction(
                command = VoiceCommand.SetPlaybackSpeed(clamped),
                spokenConfirmation = "Playback speed set to ${clamped}x.",
                intentType = "SET_PLAYBACK_SPEED"
            )
        }

        // 3. Sleep Timer ("sleep timer 30 minutes", "set timer for 15 mins", "stop in 45 minutes")
        val timerPattern = Pattern.compile("(sleep timer|timer|stop in)\\s*(for)?\\s*(\\d+)\\s*(m|min|mins|minutes|minute|hour|hours|hr|hrs)?")
        val timerMatcher = timerPattern.matcher(text)
        if (timerMatcher.find()) {
            val num = timerMatcher.group(3)?.toIntOrNull() ?: 15
            val unit = timerMatcher.group(4).orEmpty()
            val totalMinutes = if (unit.startsWith("h")) num * 60 else num
            return ParsedVoiceAction(
                command = VoiceCommand.SetSleepTimer(totalMinutes),
                spokenConfirmation = "Sleep timer set for $totalMinutes minutes.",
                intentType = "SET_SLEEP_TIMER"
            )
        }

        // 4. Audiobook Commands ("play audiobook", "resume audiobook", "next chapter", "play geeta", "play godan")
        if (text.contains("next chapter") || text.contains("skip chapter")) {
            return ParsedVoiceAction(
                command = VoiceCommand.NextChapter,
                spokenConfirmation = "Skipping to next chapter.",
                intentType = "NEXT_CHAPTER"
            )
        }

        if (text.contains("previous chapter") || text.contains("last chapter")) {
            return ParsedVoiceAction(
                command = VoiceCommand.PreviousChapter,
                spokenConfirmation = "Going to previous chapter.",
                intentType = "PREVIOUS_CHAPTER"
            )
        }

        if (text.contains("bookmark") || text.contains("save bookmark")) {
            return ParsedVoiceAction(
                command = VoiceCommand.SaveBookmark,
                spokenConfirmation = "Bookmark saved.",
                intentType = "SAVE_BOOKMARK"
            )
        }

        if (text.contains("audiobook") || text.contains("geeta") || text.contains("godan") || text.contains("sherlock")) {
            val title = when {
                text.contains("geeta") -> "श्रीमद्भगवद्गीता (Complete Geeta)"
                text.contains("godan") -> "गोदान (Godan)"
                text.contains("sherlock") -> "The Hound of the Baskervilles"
                else -> "Audiobook"
            }
            return ParsedVoiceAction(
                command = VoiceCommand.PlayAudiobook(title),
                spokenConfirmation = "Playing audiobook $title.",
                intentType = "PLAY_AUDIOBOOK"
            )
        }

        // 5. Podcast Commands ("play podcast", "play musician's mind")
        if (text.contains("podcast")) {
            val cleanTitle = text.replace("play", "").replace("podcast", "").trim()
            return ParsedVoiceAction(
                command = VoiceCommand.PlayPodcast(cleanTitle.ifBlank { "Top Podcast" }),
                spokenConfirmation = "Playing podcast.",
                intentType = "PLAY_PODCAST"
            )
        }

        // 6. Play Playlist ("play workout playlist", "play trending playlist")
        if (text.contains("playlist")) {
            val plName = text.replace("play", "").replace("playlist", "").trim()
            return ParsedVoiceAction(
                command = VoiceCommand.PlayPlaylist(plName.ifBlank { "Featured Playlist" }),
                spokenConfirmation = "Playing $plName playlist.",
                intentType = "PLAY_PLAYLIST"
            )
        }

        // 7. Play Album ("play starboy album", "play brahmastra album")
        if (text.contains("album")) {
            val albumName = text.replace("play", "").replace("album", "").trim()
            return ParsedVoiceAction(
                command = VoiceCommand.PlayAlbum(albumName.ifBlank { "Featured Album" }),
                spokenConfirmation = "Playing album $albumName.",
                intentType = "PLAY_ALBUM"
            )
        }

        // 8. Play Artist ("play songs by arijit singh", "play arijit singh", "play taylor swift")
        val artistPattern = Pattern.compile("(play songs by|songs by|play artist|tracks by)\\s+([a-zA-Z0-9\\s]+)")
        val artistMatcher = artistPattern.matcher(text)
        if (artistMatcher.find()) {
            val artist = artistMatcher.group(2)?.trim().orEmpty()
            if (artist.isNotBlank()) {
                return ParsedVoiceAction(
                    command = VoiceCommand.PlayArtist(artist),
                    spokenConfirmation = "Playing songs by $artist.",
                    intentType = "PLAY_ARTIST"
                )
            }
        }

        // 9. Play Track / General Search ("play kesariya", "play romantic hindi songs", "play starboy")
        var cleanTrack = text
        if (cleanTrack.startsWith("play ")) {
            cleanTrack = cleanTrack.substring(5).trim()
        }
        if (cleanTrack.startsWith("suno ") || cleanTrack.startsWith("bajao ") || cleanTrack.startsWith("chalao ")) {
            cleanTrack = cleanTrack.substring(6).trim()
        }

        if (cleanTrack.isNotBlank()) {
            return ParsedVoiceAction(
                command = VoiceCommand.PlayTrack(
                    trackTitle = cleanTrack,
                    rawQuery = query
                ),
                spokenConfirmation = "Playing $cleanTrack.",
                intentType = "PLAY_TRACK"
            )
        }

        // 10. Fallback Search
        return ParsedVoiceAction(
            command = VoiceCommand.SearchContent(query),
            spokenConfirmation = "Searching for $query.",
            intentType = "SEARCH_CONTENT"
        )
    }
}
