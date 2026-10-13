package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.data.model.LyricsResponse
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1ED760)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier,
    playbackViewModel: PlaybackViewModel,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val playbackState by playbackViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showChangeVibeSheet by remember { mutableStateOf(false) }
    var showWhyThisSongDialog by remember { mutableStateOf(false) }
    var showQueueRepairDialog by remember { mutableStateOf(false) }
    var whyThisSongResponse by remember { mutableStateOf<com.sonexa.app.data.model.WhyThisSongResponseDto?>(null) }
    var fixQueueResponse by remember { mutableStateOf<com.sonexa.app.data.model.FixQueueResponseDto?>(null) }
    val aiRepository = remember { com.sonexa.app.data.repository.AiRepository() }
    var lyricsData by remember { mutableStateOf<LyricsResponse?>(null) }
    var lyricsLoading by remember { mutableStateOf(false) }
    var lyricsError by remember { mutableStateOf<String?>(null) }

    val track = playbackState.track
    LaunchedEffect(track?.id, track?.title, track?.artist) {
        val t = track ?: return@LaunchedEffect
        lyricsLoading = true
        lyricsError = null
        MusicRepository().getTrackLyrics(t.id, t.title, t.artist).fold(
            onSuccess = {
                lyricsData = it
                lyricsLoading = false
            },
            onFailure = {
                lyricsData = null
                lyricsError = it.message ?: "Lyrics not available"
                lyricsLoading = false
            }
        )
    }

    val queue = playbackState.queue.ifEmpty { listOfNotNull(track) }
    val trackTitle = track?.title ?: "Nothing playing"
    val trackArtist = track?.artist ?: "Select a song"
    val playlistName = playbackState.sourceTitle.ifBlank {
        track?.album?.ifBlank { "Daily Mix 1" } ?: "Daily Mix 1"
    }
    val isPlaying = playbackState.isPlaying
    val likedSongsList by com.sonexa.app.data.local.LikedSongsStore.likedSongs.collectAsState()
    val isFavorite = track != null && (track.isLiked || likedSongsList.any { it.id == track.id })
    val isPodcast = track?.provider == "podcast" ||
        track?.album?.equals("Podcast", ignoreCase = true) == true ||
        track?.providerType == "podcast"

    val pagerState = rememberPagerState(
        initialPage = playbackState.queueIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0)),
        pageCount = { queue.size.coerceAtLeast(1) }
    )

    LaunchedEffect(playbackState.queueIndex, queue.size) {
        if (queue.isNotEmpty() && pagerState.currentPage != playbackState.queueIndex) {
            pagerState.scrollToPage(playbackState.queueIndex.coerceIn(0, queue.lastIndex))
        }
    }
    LaunchedEffect(pagerState.settledPage) {
        if (queue.isNotEmpty() && pagerState.settledPage != playbackState.queueIndex) {
            playbackViewModel.playFromQueueIndex(pagerState.settledPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF170C28),
                        Color(0xFF10071C),
                        Color(0xFF0C0515),
                        Color(0xFF07030C)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FullPlayerTopBar(
                playlistName = playlistName,
                onMinimize = onMinimize,
                onMore = { showMoreSheet = true }
            )
            Spacer(modifier = Modifier.height(4.dp))
            FullPlayerArtworkPager(
                queue = queue,
                pagerState = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                youtubeProvider = playbackViewModel.playbackManager.youtubeProvider
            )
            Spacer(modifier = Modifier.height(6.dp))
            FullPlayerTrackMeta(
                title = trackTitle,
                artist = trackArtist,
                isFavorite = isFavorite,
                onToggleLike = { playbackViewModel.toggleLike() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            FullPlayerProgressSection(
                durationMs = playbackState.durationMs.coerceAtLeast(0),
                playbackViewModel = playbackViewModel
            )
            Spacer(modifier = Modifier.height(2.dp))
            FullPlayerTransportControls(
                shuffle = playbackState.shuffle,
                repeatMode = playbackState.repeatMode,
                isPlaying = isPlaying,
                onShuffle = { playbackViewModel.toggleShuffle() },
                onPrevious = { playbackViewModel.skipPrevious() },
                onPlayPause = {
                    if (track != null) playbackViewModel.togglePlayPause()
                    else Toast.makeText(context, "Pick a track first", Toast.LENGTH_SHORT).show()
                },
                onNext = { playbackViewModel.skipNext() },
                onRepeat = { playbackViewModel.cycleRepeatMode() }
            )
            Spacer(modifier = Modifier.height(6.dp))
            FullPlayerUtilityBar(
                isPodcast = isPodcast,
                playbackSpeed = playbackState.playbackSpeed,
                sleepTimerRemainingMs = playbackState.sleepTimerRemainingMs,
                hasLyrics = lyricsData != null,
                onSpeed = { showSpeedDialog = true },
                onTimer = { showTimerDialog = true },
                onLyrics = { showLyricsSheet = true },
                onVibe = { showChangeVibeSheet = true },
                onQueue = { showQueueSheet = true },
                onDevice = {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        Toast.makeText(context, "Scanning for Audio Devices / Cast...", Toast.LENGTH_SHORT).show()
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "Connected to: ${playbackState.connectedDevice.ifBlank { "Phone Speaker" }}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onMore = { showMoreSheet = true }
            )
            Spacer(modifier = Modifier.height(4.dp))
            FullPlayerUpNextCard(
                queue = queue,
                queueIndex = playbackState.queueIndex,
                onOpenQueue = { showQueueSheet = true },
                onFixQueue = {
                    scope.launch {
                        aiRepository.fixQueue(queue).onSuccess { res ->
                            fixQueueResponse = res
                            showQueueRepairDialog = true
                        }
                    }
                },
                onPlayIndex = { playbackViewModel.playFromQueueIndex(it) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        FullPlayerOverlays(
            playbackState = playbackState,
            track = track,
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            isFavorite = isFavorite,
            isPlaying = isPlaying,
            lyricsData = lyricsData,
            lyricsLoading = lyricsLoading,
            lyricsError = lyricsError,
            showTimerDialog = showTimerDialog,
            showSpeedDialog = showSpeedDialog,
            showQualityDialog = showQualityDialog,
            showLyricsSheet = showLyricsSheet,
            showQueueSheet = showQueueSheet,
            showEqualizerSheet = showEqualizerSheet,
            showMoreSheet = showMoreSheet,
            showAddToPlaylistSheet = showAddToPlaylistSheet,
            showChangeVibeSheet = showChangeVibeSheet,
            showWhyThisSongDialog = showWhyThisSongDialog,
            showQueueRepairDialog = showQueueRepairDialog,
            whyThisSongResponse = whyThisSongResponse,
            fixQueueResponse = fixQueueResponse,
            onDismissTimer = { showTimerDialog = false },
            onDismissSpeed = { showSpeedDialog = false },
            onDismissQuality = { showQualityDialog = false },
            onDismissLyrics = { showLyricsSheet = false },
            onDismissQueue = { showQueueSheet = false },
            onDismissEqualizer = { showEqualizerSheet = false },
            onDismissMore = { showMoreSheet = false },
            onDismissAddToPlaylist = { showAddToPlaylistSheet = false },
            onDismissChangeVibe = { showChangeVibeSheet = false },
            onDismissWhy = { showWhyThisSongDialog = false },
            onDismissQueueRepair = { showQueueRepairDialog = false },
            onShowTimer = { showTimerDialog = true },
            onShowQuality = { showQualityDialog = true },
            onShowEqualizer = { showEqualizerSheet = true },
            onShowAddToPlaylist = { showAddToPlaylistSheet = true },
            onWhyLoaded = {
                whyThisSongResponse = it
                showWhyThisSongDialog = true
            },
            onFixLoaded = {
                fixQueueResponse = it
                showQueueRepairDialog = true
            },
            playbackViewModel = playbackViewModel,
            settingsViewModel = settingsViewModel,
            aiRepository = aiRepository
        )
    }
}

@Composable
internal fun PremiumSongProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    val activeFraction = if (isDragging) dragProgress else progress.coerceIn(0f, 1f)

    // Pre-calculated representative musical waveform pattern (54 bars) matching Screenshot 5
    val waveHeights = remember {
        floatArrayOf(
            0.20f, 0.35f, 0.28f, 0.48f, 0.65f, 0.42f, 0.78f, 0.92f, 0.82f, 0.58f,
            0.72f, 0.88f, 0.98f, 0.68f, 0.48f, 0.62f, 0.82f, 0.92f, 0.58f, 0.38f,
            0.52f, 0.72f, 0.88f, 0.78f, 0.62f, 0.82f, 0.98f, 0.88f, 0.68f, 0.42f,
            0.58f, 0.78f, 0.68f, 0.48f, 0.32f, 0.42f, 0.62f, 0.52f, 0.68f, 0.82f,
            0.58f, 0.38f, 0.48f, 0.62f, 0.42f, 0.32f, 0.38f, 0.48f, 0.28f, 0.38f,
            0.22f, 0.32f, 0.18f, 0.22f
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
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
        val totalBars = waveHeights.size

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val spacingPx = 2.dp.toPx()
            val totalSpacing = (totalBars - 1) * spacingPx
            val barWidth = ((canvasWidth - totalSpacing) / totalBars).coerceAtLeast(1.5f)
            val step = barWidth + spacingPx
            val activeWidth = canvasWidth * activeFraction

            for (i in 0 until totalBars) {
                val barX = i * step
                val heightRatio = waveHeights[i]
                val barHeight = (canvasHeight * heightRatio * 0.82f).coerceAtLeast(3.dp.toPx())
                val topY = (canvasHeight - barHeight) / 2f

                val isPlayed = (barX + barWidth / 2f) <= activeWidth

                if (isPlayed) {
                    val fracInPlayed = if (activeWidth > 0) (barX / activeWidth).coerceIn(0f, 1f) else 0f
                    val barColor = lerp(
                        Color(0xFFE879F9), // vibrant neon magenta/purple
                        Color(0xFF38BDF8), // electric cyan/blue near thumb
                        fracInPlayed
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barX, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                } else {
                    drawRoundRect(
                        color = Color(0xFF322E42),
                        topLeft = Offset(barX, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            // Draw active vertical indicator line and glowing white circular thumb
            val thumbX = activeWidth.coerceIn(0f, canvasWidth)
            drawLine(
                color = Color.White,
                start = Offset(thumbX, 2.dp.toPx()),
                end = Offset(thumbX, canvasHeight - 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                radius = if (isDragging) 8.dp.toPx() else 6.dp.toPx(),
                center = Offset(thumbX, canvasHeight / 2f)
            )
            drawCircle(
                color = Color.White,
                radius = if (isDragging) 5.dp.toPx() else 4.dp.toPx(),
                center = Offset(thumbX, canvasHeight / 2f)
            )
        }
    }
}
