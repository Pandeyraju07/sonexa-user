package com.sonexa.app.ui.screens

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.LyricsResponse
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.ui.theme.SonexaCardDark
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.RepeatMode
import com.sonexa.app.ui.viewmodel.SettingsViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1ED760)
private val TextMutedPurple = Color(0xFF9E98AB)
private val DarkCardSurface = Color(0xFF181324)
private val DarkCardBorder = Color(0xFF282038)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier,
    playbackViewModel: PlaybackViewModel,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val playbackState by playbackViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var lyricsData by remember { mutableStateOf<LyricsResponse?>(null) }
    var lyricsLoading by remember { mutableStateOf(false) }
    var lyricsError by remember { mutableStateOf<String?>(null) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1) }
    var currentVolume by remember {
        mutableStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2))
    }
    var isMuted by remember { mutableStateOf(currentVolume == 0) }
    var previousVolume by remember { mutableStateOf(if (currentVolume > 0) currentVolume else (maxVolume / 3)) }

    // Live Hardware/Phone Physical Volume Listener
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                val action = intent?.action
                if (action == "android.media.VOLUME_CHANGED_ACTION" || action == "android.media.RINGER_MODE_CHANGED") {
                    val streamType = intent?.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) ?: -1
                    if (streamType == AudioManager.STREAM_MUSIC || streamType == -1) {
                        val newVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: currentVolume
                        currentVolume = newVol
                        isMuted = newVol == 0
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Exception) {}
        }

        // Secondary real-time ContentObserver on system volume uri
        val contentObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val newVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: currentVolume
                currentVolume = newVol
                isMuted = newVol == 0
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                contentObserver
            )
        } catch (_: Exception) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
            try {
                context.contentResolver.unregisterContentObserver(contentObserver)
            } catch (_: Exception) {}
        }
    }

    fun updateVolume(newVol: Int) {
        val clamped = newVol.coerceIn(0, maxVolume)
        currentVolume = clamped
        isMuted = clamped == 0
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
    }

    fun volumeDown() {
        updateVolume(currentVolume - 1)
    }

    fun volumeUp() {
        updateVolume(currentVolume + 1)
    }

    fun toggleMute() {
        if (isMuted || currentVolume == 0) {
            val restore = if (previousVolume > 0) previousVolume else (maxVolume / 3)
            updateVolume(restore)
        } else {
            previousVolume = currentVolume
            updateVolume(0)
        }
    }

    val track = playbackState.track

    // Background real-time synced lyrics preloader
    LaunchedEffect(track?.id, track?.title, track?.artist) {
        val t = track ?: return@LaunchedEffect
        lyricsLoading = true
        lyricsError = null
        scope.launch {
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
    }

    val queue = playbackState.queue.ifEmpty { listOfNotNull(track) }
    val trackTitle = track?.title ?: "Nothing playing"
    val trackArtist = track?.artist ?: "Select a song"
    val playlistName = playbackState.sourceTitle.ifBlank {
        track?.album?.ifBlank { "Daily Mix 1" } ?: "Daily Mix 1"
    }
    val isPlaying = playbackState.isPlaying
    val durationMs = playbackState.durationMs.coerceAtLeast(0)
    val positionMs = playbackState.positionMs.coerceAtLeast(0)
    val currentProgress = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val isFavorite = track?.isLiked == true

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

    // Exact Premium Vignette Dark Canvas
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
            // 1. TOP BAR (Chevron, "PLAYING FROM" + Source Title, 3-Dots Menu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextMutedPurple
                    )
                    Text(
                        text = playlistName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { showMoreSheet = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. ALBUM ARTWORK (Centered Square with rounded corners 24dp & Ambient Shadow)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val item = queue.getOrNull(page)
                    val cover = item?.effectiveCoverUrl.orEmpty()

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.96f)
                                .aspectRatio(1f)
                                .shadow(
                                    elevation = 24.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.8f),
                                    spotColor = Color.Black
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1F182E))
                        ) {
                            if (cover.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(cover)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item?.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .align(Alignment.Center)
                                        )
                            }
                        }
                    }
                }
            }

            // 3. TRACK TITLE, ARTIST, AND VIBRANT GREEN HEART
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trackTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trackArtist,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFAFA9BB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.toggleLike() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) SpotifyGreen else Color(0xFFAFA9BB),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 4. PREMIUM DYNAMIC SONG PROGRESS SCRUBBER & TIMESTAMPS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                // Interactive Custom Scrubber with Gradient Track & Glowing Thumb
                PremiumSongProgressBar(
                    progress = currentProgress,
                    onSeek = { playbackViewModel.seekFraction(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Timestamps, Quick Seek Pills, and Lossless Badge Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick -10s Seek Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { playbackViewModel.seekBackward10() }
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text("-10s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC4B5FD))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMs(positionMs),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    // Lossless Hi-Fi Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LOSSLESS • 24-BIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SpotifyGreen,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatMs(durationMs),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFAFA9BB)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Quick +10s Seek Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { playbackViewModel.seekForward30() }
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text("+10s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC4B5FD))
                        }
                    }
                }
            }

            val isPodcast = track?.provider == "podcast" || track?.album?.equals("Podcast", ignoreCase = true) == true || track?.providerType == "podcast"

            // 5. MAIN HERO PLAYBACK CONTROLS (Shuffle, Previous, HERO Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { playbackViewModel.toggleShuffle() },
                    modifier = Modifier.size(42.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playbackState.shuffle) SpotifyGreen else TextMutedPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        if (playbackState.shuffle) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                        }
                    }
                }

                // Skip Previous Button (Glassmorphic)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { playbackViewModel.skipPrevious() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // HERO PURE WHITE CIRCLE PLAY/PAUSE BUTTON WITH AMBIENT GLOW
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = if (isPlaying) SpotifyGreen.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                            spotColor = Color.White
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (track != null) playbackViewModel.togglePlayPause()
                            else Toast.makeText(context, "Pick a track first", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Skip Next Button (Glassmorphic)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { playbackViewModel.skipNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = { playbackViewModel.cycleRepeatMode() },
                    modifier = Modifier.size(42.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playbackState.repeatMode != RepeatMode.OFF) SpotifyGreen else TextMutedPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        if (playbackState.repeatMode != RepeatMode.OFF) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                        }
                    }
                }
            }

            // 6. DEDICATED PREMIUM VOLUME CONTROL BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0xFF191226).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF2E2442), RoundedCornerShape(21.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Volume Down / Mute Button
                IconButton(
                    onClick = { toggleMute() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted || currentVolume == 0) Icons.Default.VolumeOff else Icons.Default.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = if (isMuted || currentVolume == 0) Color(0xFFEF4444) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Interactive Volume Slider
                Slider(
                    value = (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        val newVol = (frac * maxVolume).toInt()
                        updateVolume(newVol)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = SpotifyGreen,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                )

                // Volume Up Button
                IconButton(
                    onClick = { volumeUp() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Up",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 6. UTILITY ACTION BAR (Speed / Lyrics, Timer, Queue, Device)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPodcast) {
                    // 1. Playback Speed
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showSpeedDialog = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${playbackState.playbackSpeed}x",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackState.playbackSpeed != 1.0f) SpotifyGreen else Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Speed", fontSize = 11.sp, color = TextMutedPurple)
                    }

                    // 2. Sleep Timer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showTimerDialog = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (playbackState.sleepTimerRemainingMs != null) SpotifyGreen else TextMutedPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (playbackState.sleepTimerRemainingMs != null) "${playbackState.sleepTimerRemainingMs!! / 60000}m" else "Timer",
                            fontSize = 11.sp,
                            color = if (playbackState.sleepTimerRemainingMs != null) SpotifyGreen else TextMutedPurple
                        )
                    }
                } else {
                    // 1. Lyrics
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showLyricsSheet = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Lyrics",
                            tint = if (lyricsData != null) Color.White else TextMutedPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Lyrics", fontSize = 11.sp, color = TextMutedPurple)
                    }
                }

                // Queue
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showQueueSheet = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = TextMutedPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isPodcast) "Episodes" else "Queue", fontSize = 11.sp, color = TextMutedPurple)
                }

                // 3. Device
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, "Scanning for Audio Devices / Cast...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Connected to: ${playbackState.connectedDevice.ifBlank { "Phone Speaker" }}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "Device",
                        tint = TextMutedPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Device", fontSize = 11.sp, color = TextMutedPurple)
                }

                // 4. More
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showMoreSheet = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "More",
                        tint = TextMutedPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("More", fontSize = 11.sp, color = TextMutedPurple)
                }
            }

            // 7. "UP NEXT" DOCKED BOTTOM CARD
            val upNextItems = remember(playbackState.queueIndex, queue) {
                if (queue.size > 1) {
                    queue.drop(playbackState.queueIndex + 1).take(3).ifEmpty {
                        queue.take(3)
                    }
                } else {
                    emptyList()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(DarkCardSurface)
                    .border(
                        1.dp,
                        DarkCardBorder,
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    // Header Row: ^ UP NEXT
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQueueSheet = true }
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand Up Next",
                            tint = TextMutedPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UP NEXT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextMutedPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (upNextItems.isEmpty()) {
                        Text(
                            text = "Add more songs to queue",
                            fontSize = 12.sp,
                            color = TextMutedPurple,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upNextItems.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val targetIndex = queue.indexOfFirst { it.id == item.id }
                                            if (targetIndex >= 0) {
                                                playbackViewModel.playFromQueueIndex(targetIndex)
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF261F36))
                                    ) {
                                        if (item.effectiveCoverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(item.effectiveCoverUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Title & Artist
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.artist,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = TextMutedPurple,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Drag handle (=) & 3-dots
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Reorder",
                                        tint = Color(0xFF6B6578),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color(0xFF6B6578),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modals, Sheets & Dialogs
        var selectedQuality by remember { mutableStateOf("Hi-Fi Lossless (320kbps Master)") }
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                containerColor = SonexaCardDark,
                title = { Text("Audio Stream Quality", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf(
                            "Normal" to "Normal (96 kbps)",
                            "High" to "High (160 kbps)",
                            "Very High" to "Very High (320 kbps)",
                            "Lossless" to "Hi-Fi Lossless (320kbps Master)"
                        ).forEach { (apiValue, label) ->
                            Text(
                                text = label,
                                color = if (selectedQuality == label) SpotifyGreen else SonexaTextWhite,
                                fontWeight = if (selectedQuality == label) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuality = label
                                        settingsViewModel.updateString("audioQuality", apiValue)
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualityDialog = false }) {
                        Text("Close", color = SpotifyGreen)
                    }
                }
            )
        }

        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                containerColor = SonexaCardDark,
                title = { Text("Playback Speed", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                            val isSelected = playbackState.playbackSpeed == speed
                            Text(
                                text = "${speed}x ${if (speed == 1.0f) "(Normal)" else ""}",
                                color = if (isSelected) SpotifyGreen else SonexaTextWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playbackViewModel.setPlaybackSpeed(speed)
                                        Toast.makeText(context, "Speed set to ${speed}x", Toast.LENGTH_SHORT).show()
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) {
                        Text("Close", color = SpotifyGreen)
                    }
                }
            )
        }

        if (showTimerDialog) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                containerColor = SonexaCardDark,
                title = { Text("Sleep Timer", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf(null to "Off", 5 to "5 min", 15 to "15 min", 30 to "30 min", 45 to "45 min", 60 to "1 hour")
                            .forEach { (mins, label) ->
                                Text(
                                    text = label,
                                    color = SonexaTextWhite,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playbackViewModel.setSleepTimerMinutes(mins)
                                            Toast.makeText(context, "Sleep timer: $label", Toast.LENGTH_SHORT).show()
                                            showTimerDialog = false
                                        }
                                        .padding(vertical = 10.dp)
                                )
                            }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTimerDialog = false }) {
                        Text("Close", color = SpotifyGreen)
                    }
                }
            )
        }

        if (showLyricsSheet) {
            com.sonexa.app.ui.components.PremiumLyricsSheet(
                trackTitle = trackTitle,
                trackArtist = trackArtist,
                positionMs = positionMs,
                lyrics = lyricsData,
                loading = lyricsLoading,
                error = lyricsError,
                onDismiss = { showLyricsSheet = false },
                onSeekToLine = { tMs -> playbackViewModel.seekToMs(tMs) }
            )
        }

        if (showQueueSheet) {
            AlertDialog(
                onDismissRequest = { showQueueSheet = false },
                containerColor = SonexaCardDark,
                title = { Text("Up Next (${queue.size} tracks)", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    if (queue.isEmpty()) {
                        Text("Queue is empty", color = SonexaTextMuted)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            itemsIndexed(queue) { index, item ->
                                val active = index == playbackState.queueIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playbackViewModel.playFromQueueIndex(index)
                                            showQueueSheet = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF261F36))
                                    ) {
                                        if (item.effectiveCoverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(item.effectiveCoverUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.title,
                                            color = if (active) SpotifyGreen else SonexaTextWhite,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 14.sp
                                        )
                                        Text(item.artist, color = SonexaTextMuted, fontSize = 12.sp)
                                    }
                                    if (active) {
                                        Icon(
                                            Icons.Default.GraphicEq,
                                            contentDescription = "Playing",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQueueSheet = false }) {
                        Text("Close", color = SpotifyGreen)
                    }
                }
            )
        }

        if (showMoreSheet) {
            AlertDialog(
                onDismissRequest = { showMoreSheet = false },
                containerColor = SonexaCardDark,
                title = { Text("Track Options", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    playbackViewModel.applyEqualizerPreset(playbackState.equalizer.presetName)
                                    showEqualizerSheet = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Audio Equalizer (5-Band DSP)", color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    showTimerDialog = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NightsStay, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Sleep Timer", color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    showQualityDialog = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Audio Stream Quality (320kbps)", color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    try {
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Listening to $trackTitle on Sonexa")
                                            putExtra(android.content.Intent.EXTRA_TEXT, "🎵 Listening to \"$trackTitle\" by $trackArtist on Sonexa Music!\nEnjoy high-fidelity 320kbps sound.")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Track"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open share dialog", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Share Song", color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    playbackViewModel.toggleLike()
                                    Toast.makeText(context, if (!isFavorite) "Saved to Your Library" else "Removed from Library", Toast.LENGTH_SHORT).show()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text(if (isFavorite) "Remove from Liked Songs" else "Save to Liked Songs", color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMoreSheet = false
                                    showAddToPlaylistSheet = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = SpotifyGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Add to Playlist", color = Color.White, fontSize = 15.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMoreSheet = false }) {
                        Text("Close", color = SpotifyGreen)
                    }
                }
            )
        }

        if (showAddToPlaylistSheet && track != null) {
            com.sonexa.app.ui.components.AddToPlaylistBottomSheet(
                track = track,
                onDismiss = { showAddToPlaylistSheet = false }
            )
        }

        if (showEqualizerSheet) {
            com.sonexa.app.ui.components.PremiumEqualizerSheet(
                snapshot = playbackState.equalizer,
                isPlaying = isPlaying,
                onDismiss = { showEqualizerSheet = false },
                onEnabledChange = { playbackViewModel.setEqualizerEnabled(it) },
                onBandChange = { index, level -> playbackViewModel.setEqualizerBand(index, level) },
                onBassChange = { playbackViewModel.setBassBoost(it) },
                onVirtualChange = { playbackViewModel.setVirtualizer(it) },
                onPreset = { playbackViewModel.applyEqualizerPreset(it) },
                onReset = { playbackViewModel.resetEqualizer() }
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

@Composable
private fun PremiumSongProgressBar(
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

        // Inactive background track (Glass / Dark Lavender)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.18f))
        )

        // Active played gradient track
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

        // Glowing Thumb Dot
        val thumbRadiusPx = if (isDragging) with(LocalDensity.current) { 9.dp.toPx() } else with(LocalDensity.current) { 6.5.dp.toPx() }
        val thumbOffsetPx = ((widthPx * activeFraction) - thumbRadiusPx).coerceIn(0f, (widthPx - thumbRadiusPx * 2).coerceAtLeast(0f))

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
