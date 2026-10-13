package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.AudiobookBookmarkDto
import com.sonexa.app.data.model.AudiobookChapterDto
import com.sonexa.app.data.model.AudiobookDto
import com.sonexa.app.data.repository.AudiobookRepository
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookPlayerScreen(
    onMinimize: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playbackState by playbackViewModel.uiState.collectAsState()
    val elapsedMs by playbackViewModel.elapsedMs.collectAsState()

    val book = playbackState.activeAudiobook ?: AudiobookRepository.bundledAudiobooks().first()
    val currentChapterIndex = playbackState.activeChapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
    val currentChapter = book.chapters.getOrNull(currentChapterIndex) ?: book.chapters.firstOrNull()

    var showSpeedSheet by remember { mutableStateOf(false) }
    var showTimerSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkNoteText by remember { mutableStateOf("") }

    val bookmarksList by AudiobookStore.bookmarksMap.collectAsState()
    val currentBookBookmarks = bookmarksList[book.id].orEmpty()

    val chapterDurationMs = (currentChapter?.durationMs ?: playbackState.durationMs).coerceAtLeast(1L)
    val chapterPositionMs = (elapsedMs.takeIf { it > 0 } ?: playbackState.positionMs).coerceIn(0L, chapterDurationMs)
    val chapterProgress = (chapterPositionMs.toFloat() / chapterDurationMs.toFloat()).coerceIn(0f, 1f)

    // Total Book Progress calculation
    val totalBookDurationMs = (book.totalDurationMs).coerceAtLeast(1L)
    val priorChaptersMs = book.chapters.take(currentChapterIndex).sumOf { it.durationMs }
    val totalPositionMs = (priorChaptersMs + chapterPositionMs).coerceIn(0L, totalBookDurationMs)
    val totalProgress = (totalPositionMs.toFloat() / totalBookDurationMs.toFloat()).coerceIn(0f, 1f)

    fun formatMs(ms: Long): String {
        val sec = (ms / 1000).coerceAtLeast(0)
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1F1133),
                        Color(0xFF140D24),
                        Color(0xFF090810)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── 1. Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinimize) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AUDIOBOOK",
                        style = AppleType.caption2Bold,
                        color = ZyneraPrimaryBright
                    )
                    Text(
                        text = "Chapter ${currentChapterIndex + 1} of ${book.chapters.size}",
                        style = AppleType.footnoteBold,
                        color = Color.White
                    )
                }

                IconButton(onClick = { showChaptersSheet = true }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatListNumbered,
                        contentDescription = "Chapters",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── 2. Prominent Book Artwork ───────────────────────────────────
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(ZyneraSurfaceElevated)
                    .border(1.2.dp, AppleMaterials.specularBorder, RoundedCornerShape(26.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(book.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── 3. Chapter & Book Title Info ────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentChapter?.title ?: "Chapter 1",
                    style = AppleType.title2Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = book.title,
                    style = AppleType.subheadlineBold,
                    color = ZyneraPrimaryBright,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${book.author} • Narrated by ${book.narrator}",
                    style = AppleType.caption1,
                    color = ZyneraTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── 4. Dual Progress: Chapter Progress & Total Book Progress ─────
            Column(modifier = Modifier.fillMaxWidth()) {
                // Chapter Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(chapterPositionMs),
                        style = AppleType.caption1Bold,
                        color = Color.White
                    )
                    Text(
                        text = "-${formatMs(chapterDurationMs - chapterPositionMs)}",
                        style = AppleType.caption1,
                        color = ZyneraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = chapterProgress,
                    onValueChange = { frac ->
                        val target = (chapterDurationMs * frac).toLong()
                        playbackViewModel.seekToMs(target)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = ZyneraPrimaryBright,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Total Book Progress Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Book Progress",
                        style = AppleType.caption2,
                        color = ZyneraTextSubtle
                    )
                    Text(
                        text = "${(totalProgress * 100).toInt()}% complete",
                        style = AppleType.caption2Bold,
                        color = ZyneraSecondary
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(totalProgress)
                            .fillMaxHeight()
                            .background(ZyneraVioletGradient)
                    )
                }
            }

            // ── 5. Main Audio Playback Transport Controls ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek -15s
                IconButton(
                    onClick = { playbackViewModel.seekBackward15() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Seek Backward 15s",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Previous Chapter
                IconButton(
                    onClick = { playbackViewModel.previousChapter() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Chapter",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .appleBounce {
                            playbackViewModel.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Chapter
                IconButton(
                    onClick = { playbackViewModel.nextChapter() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Chapter",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Seek +30s
                IconButton(
                    onClick = { playbackViewModel.seekForward30() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward30,
                        contentDescription = "Seek Forward 30s",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // ── 6. Utility Bar: Speed, Sleep Timer, Bookmark, Chapters ──────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback Speed
                UtilityChip(
                    icon = Icons.Outlined.Speed,
                    label = "${playbackState.playbackSpeed}x",
                    onClick = { showSpeedSheet = true }
                )

                // Sleep Timer
                val sleepRemaining = playbackState.sleepTimerRemainingMs
                UtilityChip(
                    icon = Icons.Outlined.Timer,
                    label = if (sleepRemaining != null) "${(sleepRemaining / 60000) + 1}m" else "Timer",
                    isHighlight = sleepRemaining != null,
                    onClick = { showTimerSheet = true }
                )

                // Add Bookmark
                UtilityChip(
                    icon = Icons.Outlined.BookmarkBorder,
                    label = "Bookmark",
                    onClick = { showAddBookmarkDialog = true }
                )

                // Saved Bookmarks List
                UtilityChip(
                    icon = Icons.Default.Bookmarks,
                    label = if (currentBookBookmarks.isNotEmpty()) "${currentBookBookmarks.size}" else "Saved",
                    onClick = { showBookmarksSheet = true }
                )
            }
        }

        // ── Speed Selector Bottom Sheet ─────────────────────────────────────
        if (showSpeedSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSpeedSheet = false },
                containerColor = ZyneraSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Playback Speed",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                    speeds.forEach { spd ->
                        val isSel = playbackState.playbackSpeed == spd
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackViewModel.setPlaybackSpeed(spd)
                                    showSpeedSheet = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${spd}x ${if (spd == 1.0f) "(Normal)" else ""}",
                                style = if (isSel) AppleType.bodyBold else AppleType.body,
                                color = if (isSel) ZyneraPrimaryBright else Color.White
                            )
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ZyneraPrimaryBright
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Sleep Timer Bottom Sheet ────────────────────────────────────────
        if (showTimerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTimerSheet = false },
                containerColor = ZyneraSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Sleep Timer",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val timers = listOf(
                        "Off" to null,
                        "5 Minutes" to 5,
                        "15 Minutes" to 15,
                        "30 Minutes" to 30,
                        "45 Minutes" to 45,
                        "End of Chapter" to ((chapterDurationMs - chapterPositionMs) / 60000).toInt().coerceAtLeast(1)
                    )

                    timers.forEach { (label, mins) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackViewModel.setSleepTimerMinutes(mins)
                                    showTimerSheet = false
                                    Toast.makeText(
                                        context,
                                        if (mins != null) "Timer set for $label" else "Sleep timer cancelled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = label,
                                style = AppleType.body,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── Chapters List Bottom Sheet ──────────────────────────────────────
        if (showChaptersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChaptersSheet = false },
                containerColor = ZyneraSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Chapters",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(book.chapters) { ch ->
                            val isCurrent = (ch.chapterNumber - 1) == currentChapterIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) ZyneraPrimary.copy(alpha = 0.2f) else ZyneraSurface)
                                    .clickable {
                                        playbackViewModel.seekAudiobookChapter(ch.chapterNumber - 1)
                                        showChaptersSheet = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.title,
                                            style = if (isCurrent) AppleType.subheadlineBold else AppleType.subheadline,
                                            color = if (isCurrent) ZyneraPrimaryBright else Color.White
                                        )
                                        Text(
                                            text = ch.formattedDuration,
                                            style = AppleType.caption2,
                                            color = ZyneraTextSecondary
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = ZyneraPrimaryBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Add Bookmark Dialog ─────────────────────────────────────────────
        if (showAddBookmarkDialog) {
            AlertDialog(
                onDismissRequest = { showAddBookmarkDialog = false },
                title = { Text(text = "Add Bookmark", color = Color.White) },
                text = {
                    Column {
                        Text(
                            text = "Position: ${formatMs(chapterPositionMs)} in ${currentChapter?.title}",
                            style = AppleType.caption1,
                            color = ZyneraPrimaryBright
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bookmarkNoteText,
                            onValueChange = { bookmarkNoteText = it },
                            placeholder = { Text("Note (optional)", color = ZyneraTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ZyneraPrimaryBright,
                                unfocusedBorderColor = ZyneraBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val chId = currentChapter?.id ?: "${book.id}_ch1"
                            AudiobookStore.addBookmark(
                                audiobookId = book.id,
                                chapterId = chId,
                                chapterNumber = currentChapterIndex + 1,
                                positionSeconds = chapterPositionMs / 1000L,
                                note = bookmarkNoteText
                            )
                            bookmarkNoteText = ""
                            showAddBookmarkDialog = false
                            Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "Save", color = ZyneraPrimaryBright)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBookmarkDialog = false }) {
                        Text(text = "Cancel", color = ZyneraTextSecondary)
                    }
                },
                containerColor = ZyneraSurfaceElevated
            )
        }

        // ── Saved Bookmarks Bottom Sheet ────────────────────────────────────
        if (showBookmarksSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBookmarksSheet = false },
                containerColor = ZyneraSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Saved Bookmarks",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (currentBookBookmarks.isEmpty()) {
                        Text(
                            text = "No bookmarks saved for this audiobook yet.",
                            style = AppleType.body,
                            color = ZyneraTextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.5f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentBookBookmarks) { bm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ZyneraSurface)
                                        .clickable {
                                            playbackViewModel.playAudiobook(
                                                audiobook = book,
                                                chapterIndex = (bm.chapterNumber - 1).coerceAtLeast(0),
                                                startPositionMs = bm.positionMs
                                            )
                                            showBookmarksSheet = false
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Chapter ${bm.chapterNumber} at ${bm.formattedPosition}",
                                            style = AppleType.subheadlineBold,
                                            color = ZyneraPrimaryBright
                                        )
                                        if (bm.note.isNotBlank()) {
                                            Text(
                                                text = bm.note,
                                                style = AppleType.caption1,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            AudiobookStore.removeBookmark(book.id, bm.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UtilityChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) ZyneraPrimary.copy(alpha = 0.25f) else ZyneraSurfaceElevated)
            .border(
                0.8.dp,
                if (isHighlight) ZyneraPrimaryBright else ZyneraBorder,
                RoundedCornerShape(16.dp)
            )
            .appleBounce { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) ZyneraPrimaryBright else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = AppleType.caption1Bold,
                color = if (isHighlight) ZyneraPrimaryBright else Color.White
            )
        }
    }
}
