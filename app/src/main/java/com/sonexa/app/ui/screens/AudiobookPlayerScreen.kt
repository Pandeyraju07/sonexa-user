package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val savedAudiobookIds by AudiobookStore.savedAudiobookIds.collectAsState()

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
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF130726),
                        Color(0xFF0F0520),
                        Color(0xFF070210)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ambient soft purple radial glow behind artwork
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopCenter)
                .offset(y = 50.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.35f),
                            Color(0xFF4C1D95).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 1. Top Bar (Like FullPlayerTopBar) ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF191326))
                        .border(1.dp, Color(0xFF282038), CircleShape)
                        .clickable(onClick = onMinimize),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AUDIOBOOK EDITION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        color = Color(0xFF8E889E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1430))
                            .border(1.dp, Color(0xFF32244C), RoundedCornerShape(12.dp))
                            .clickable { showChaptersSheet = true }
                            .padding(horizontal = 10.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = book.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33A855F7))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "Ch ${currentChapterIndex + 1}/${book.chapters.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC084FC)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF191326))
                        .border(1.dp, Color(0xFF282038), CircleShape)
                        .clickable { showChaptersSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    ChapterListIcon(tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 2. Artwork Section (Responsive & Weight(1f), Like FullPlayerArtworkPager) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.96f)
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .shadow(
                            elevation = 28.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color(0xFF6B21A8).copy(alpha = 0.35f),
                            spotColor = Color.Black
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF1F182E))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
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

                    // Top-Right HQ Audio Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HQ AUDIO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Bottom-Right Chapter Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CH ${currentChapterIndex + 1}/${book.chapters.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── 3. Chapter Title, Narrator & Bookmark (Like FullPlayerTrackMeta) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentChapter?.title ?: "Chapter 1",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${book.author} • Narrated by ${book.narrator}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFAFA9BB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val isSaved = savedAudiobookIds.contains(book.id)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D172B))
                        .border(1.dp, Color(0xFF2C253E), CircleShape)
                        .clickable {
                            val newlySaved = AudiobookStore.toggleSaveAudiobook(book.id)
                            Toast.makeText(
                                context,
                                if (newlySaved) "Saved to your library" else "Removed from library",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Book",
                        tint = if (isSaved) Color(0xFFC084FC) else Color(0xFF8B849C),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── 4. Premium Modern Duration & Scrubber Section ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Interactive Custom Scrubber with Glowing Thumb
                PremiumAudiobookProgressBar(
                    progress = chapterProgress,
                    onSeek = { frac ->
                        val target = (chapterDurationMs * frac).toLong()
                        playbackViewModel.seekToMs(target)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Modern Time Display & Progress Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Elapsed Time
                    Text(
                        text = formatMs(chapterPositionMs),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    // Premium Center Capsule Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1D1430))
                            .border(0.8.dp, Color(0xFF382956), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA855F7))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Ch ${currentChapterIndex + 1}/${book.chapters.size} • ${(totalProgress * 100).toInt()}% of Book",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC084FC)
                            )
                        }
                    }

                    // Remaining Time (-mm:ss or -hh:mm:ss)
                    Text(
                        text = "-${formatMs((chapterDurationMs - chapterPositionMs).coerceAtLeast(0L))}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFAFA9BB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ── 5. Transport Controls (Matching FullPlayerTransportControls) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek -15s
                IconButton(
                    onClick = { playbackViewModel.seekBackward15() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Replay15Icon(tint = Color.White)
                }

                // Previous Chapter
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B152A))
                        .border(1.dp, Color(0xFF2E2642), CircleShape)
                        .clickable { playbackViewModel.previousChapter() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Chapter",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Glowing White Center Play/Pause Jewel Button
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(
                            elevation = 28.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFA855F7).copy(alpha = 0.8f),
                            spotColor = Color(0xFFA855F7)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { playbackViewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Chapter
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B152A))
                        .border(1.dp, Color(0xFF2E2642), CircleShape)
                        .clickable { playbackViewModel.nextChapter() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Chapter",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Seek +30s
                IconButton(
                    onClick = { playbackViewModel.seekForward30() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Forward30Icon(tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 6. Sleek Utility Bar (Matching FullPlayerUtilityBar) ──
            val sleepRemaining = playbackState.sleepTimerRemainingMs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed
                val isSpeedActive = playbackState.playbackSpeed != 1.0f
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showSpeedSheet = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = "Speed",
                        tint = if (isSpeedActive) Color(0xFFA855F7) else Color(0xFF9E98AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${playbackState.playbackSpeed}x",
                        fontSize = 11.sp,
                        fontWeight = if (isSpeedActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSpeedActive) Color(0xFFA855F7) else Color(0xFF9E98AB)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(16.dp)
                            .clip(CircleShape)
                            .background(if (isSpeedActive) Color(0xFFA855F7) else Color.Transparent)
                    )
                }

                // Timer
                val isTimerActive = sleepRemaining != null && sleepRemaining > 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showTimerSheet = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Timer",
                        tint = if (isTimerActive) Color(0xFFFBBF24) else Color(0xFF9E98AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isTimerActive) {
                            val remM = (sleepRemaining / 60000)
                            if (remM > 0) "${remM + 1}m" else "${sleepRemaining / 1000}s"
                        } else "Timer",
                        fontSize = 11.sp,
                        fontWeight = if (isTimerActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTimerActive) Color(0xFFFBBF24) else Color(0xFF9E98AB)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(16.dp)
                            .clip(CircleShape)
                            .background(if (isTimerActive) Color(0xFFFBBF24) else Color.Transparent)
                    )
                }

                // Chapters
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showChaptersSheet = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Chapters",
                        tint = Color(0xFF9E98AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Chapters",
                        fontSize = 11.sp,
                        color = Color(0xFF9E98AB)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(16.dp)
                    )
                }

                // Bookmark (Add mark)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showAddBookmarkDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Add Bookmark",
                        tint = Color(0xFF9E98AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Mark",
                        fontSize = 11.sp,
                        color = Color(0xFF9E98AB)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(16.dp)
                    )
                }

                // Saved Bookmarks
                val isSavedActive = currentBookBookmarks.isNotEmpty()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showBookmarksSheet = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isSavedActive) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Saved Bookmarks",
                        tint = if (isSavedActive) Color(0xFFA855F7) else Color(0xFF9E98AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Saved (${currentBookBookmarks.size})",
                        fontSize = 11.sp,
                        fontWeight = if (isSavedActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSavedActive) Color(0xFFA855F7) else Color(0xFF9E98AB)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(16.dp)
                            .clip(CircleShape)
                            .background(if (isSavedActive) Color(0xFFA855F7) else Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(8.dp))
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
            val remChapterMs = (chapterDurationMs - chapterPositionMs).coerceAtLeast(0L)
            val remChapterMins = (remChapterMs / 60000).toInt().coerceAtLeast(1)
            com.sonexa.app.ui.components.PremiumSleepTimerBottomSheet(
                sleepTimerRemainingMs = playbackState.sleepTimerRemainingMs,
                onSetTimerMinutes = { mins -> playbackViewModel.setSleepTimerMinutes(mins) },
                onDismiss = { showTimerSheet = false },
                endOfContentMinutes = remChapterMins,
                endOfContentLabel = "End of Chapter"
            )
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

@Composable
private fun AudioWaveformIndicator(tint: Color = Color(0xFFC084FC)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.width(2.5.dp).height(8.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.5.dp).height(14.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.5.dp).height(6.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.5.dp).height(12.dp).clip(CircleShape).background(tint))
    }
}

@Composable
private fun ChapterListIcon(tint: Color = Color.White) {
    Column(
        modifier = Modifier.size(19.dp),
        verticalArrangement = Arrangement.spacedBy(3.5.dp)
    ) {
        repeat(3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(tint)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(tint)
                )
            }
        }
    }
}

@Composable
private fun Replay15Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val inset = 3.dp.toPx()
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) / 2f) - inset

            drawArc(
                color = tint,
                startAngle = -60f,
                sweepAngle = -275f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            val rad = Math.toRadians(-60.0)
            val ax = cx + radius * Math.cos(rad).toFloat()
            val ay = cy + radius * Math.sin(rad).toFloat()
            val arrowSize = 4.2.dp.toPx()
            val path = Path().apply {
                moveTo(ax - arrowSize * 1.2f, ay - arrowSize * 0.4f)
                lineTo(ax + arrowSize * 0.4f, ay - arrowSize * 1.3f)
                lineTo(ax + arrowSize * 0.2f, ay + arrowSize * 0.5f)
                close()
            }
            drawPath(path, color = tint)
        }
        Text(
            text = "15",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Forward30Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val inset = 3.dp.toPx()
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) / 2f) - inset

            drawArc(
                color = tint,
                startAngle = -120f,
                sweepAngle = 275f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            val rad = Math.toRadians(-120.0)
            val ax = cx + radius * Math.cos(rad).toFloat()
            val ay = cy + radius * Math.sin(rad).toFloat()
            val arrowSize = 4.2.dp.toPx()
            val path = Path().apply {
                moveTo(ax + arrowSize * 1.2f, ay - arrowSize * 0.4f)
                lineTo(ax - arrowSize * 0.4f, ay - arrowSize * 1.3f)
                lineTo(ax - arrowSize * 0.2f, ay + arrowSize * 0.5f)
                close()
            }
            drawPath(path, color = tint)
        }
        Text(
            text = "30",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PremiumAudiobookProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    val activeFraction = if (isDragging) dragProgress else progress.coerceIn(0f, 1f)

    val animatedHeight by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 5.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackHeight"
    )

    val animatedThumbRadius by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 7.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbRadius"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(frac)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val trackHeightPx = animatedHeight.toPx()
            val thumbRadiusPx = animatedThumbRadius.toPx()
            val centerY = canvasHeight / 2f
            val trackCorner = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)

            // 1. Inactive Track (Dark Violet Charcoal Pill)
            drawRoundRect(
                color = Color(0xFF261D3B),
                topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                size = Size(canvasWidth, trackHeightPx),
                cornerRadius = trackCorner
            )

            // 2. Active Gradient Track
            val activeWidth = (canvasWidth * activeFraction).coerceIn(0f, canvasWidth)
            if (activeWidth > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF2D55),
                            Color(0xFFA855F7),
                            Color(0xFFC084FC)
                        ),
                        startX = 0f,
                        endX = canvasWidth
                    ),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(activeWidth, trackHeightPx),
                    cornerRadius = trackCorner
                )
            }

            // 3. Glowing Precision Thumb
            val thumbCenterX = activeWidth.coerceIn(thumbRadiusPx, canvasWidth - thumbRadiusPx)

            // Outer soft glow shadow
            drawCircle(
                color = Color(0xFFA855F7).copy(alpha = if (isDragging) 0.65f else 0.45f),
                radius = thumbRadiusPx + (if (isDragging) 5.dp.toPx() else 3.dp.toPx()),
                center = Offset(thumbCenterX, centerY)
            )

            // Inner solid white disc
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx,
                center = Offset(thumbCenterX, centerY)
            )

            // Subtle purple center pip
            drawCircle(
                color = Color(0xFFA855F7),
                radius = thumbRadiusPx * 0.35f,
                center = Offset(thumbCenterX, centerY)
            )
        }
    }
}

