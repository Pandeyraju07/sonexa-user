package com.sonexa.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FullPlayerTopBar(
                playlistName = playlistName,
                onMinimize = onMinimize,
                onMore = { showMoreSheet = true }
            )
            FullPlayerArtworkPager(
                queue = queue,
                pagerState = pagerState,
                modifier = Modifier.weight(1f)
            )
            FullPlayerTrackMeta(
                title = trackTitle,
                artist = trackArtist,
                isFavorite = isFavorite,
                onToggleLike = { playbackViewModel.toggleLike() }
            )
            FullPlayerProgressSection(
                durationMs = playbackState.durationMs.coerceAtLeast(0),
                playbackViewModel = playbackViewModel
            )
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
            FullPlayerVolumeBar()
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
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
        val widthPx = constraints.maxWidth.toFloat()
        val trackHeight = if (isDragging) 6.dp else 4.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(activeFraction.coerceIn(0.001f, 1f))
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE0E7FF), Color(0xFF1ED760), Color(0xFF1ED760))
                    )
                )
        )
        val thumbRadiusPx = if (isDragging) {
            with(LocalDensity.current) { 9.dp.toPx() }
        } else {
            with(LocalDensity.current) { 6.5.dp.toPx() }
        }
        val thumbOffsetPx = ((widthPx * activeFraction) - thumbRadiusPx)
            .coerceIn(0f, (widthPx - thumbRadiusPx * 2).coerceAtLeast(0f))
        Box(
            modifier = Modifier
                .offset(x = with(LocalDensity.current) { thumbOffsetPx.toDp() })
                .size(if (isDragging) 18.dp else 13.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = SpotifyGreen,
                    spotColor = SpotifyGreen
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, SpotifyGreen, CircleShape)
        )
    }
}
