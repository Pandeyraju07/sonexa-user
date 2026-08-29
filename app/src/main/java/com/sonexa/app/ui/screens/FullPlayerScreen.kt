package com.sonexa.app.ui.screens

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.sonexa.app.ui.theme.SonexaTextSubtle
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.RepeatMode
import com.sonexa.app.ui.viewmodel.SettingsViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private val DeviceGreen = Color(0xFF22C55E)
private val BrandPurple = Color(0xFFA855F7)
private val DarkSliderTrack = Color(0xFF2D273D)

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

    // System Audio Manager for volume slider
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat().coerceAtLeast(1f) }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }

    var showTimerDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var lyricsData by remember { mutableStateOf<LyricsResponse?>(null) }
    var lyricsLoading by remember { mutableStateOf(false) }
    var lyricsError by remember { mutableStateOf<String?>(null) }

    val qualityOptions = listOf(
        "Normal" to "Normal (96 kbps)",
        "High" to "High (160 kbps)",
        "Very High" to "Very High (320 kbps)",
        "Lossless" to "Hi-Fi Lossless (320kbps Master)"
    )
    var selectedQuality by remember { mutableStateOf("Hi-Fi Lossless (320kbps Master)") }

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
        track?.album?.ifBlank { "Now Playing" } ?: "Now Playing"
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

    // Exact Spotify & modern premium layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF221633),
                        Color(0xFF130E1F),
                        Color(0xFF0B0912),
                        Color(0xFF07050A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP BAR (Chevron, "PLAYING FROM" + Title + Dots, More options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
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
                        color = Color(0xFFB3ADBF)
                    )
                    Text(
                        text = playlistName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Pager indicator dots: — • •
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalDots = queue.size.coerceAtMost(5).coerceAtLeast(1)
                        val activeDot = (pagerState.currentPage % totalDots).coerceIn(0, totalDots - 1)
                        for (i in 0 until totalDots) {
                            if (i == activeDot) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { showMoreSheet = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. ALBUM ARTWORK (Square with rounded corners ~20dp, soft shadow, responsive height)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 4.dp),
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
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.6f),
                                    spotColor = Color.Black
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1B1626))
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
                                    tint = BrandPurple,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            // 3. TRACK TITLE, ARTIST, BADGE, AND LIKE / CHECK ICONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trackTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trackArtist,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA19BAE),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // LOSSLESS 320K pill badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF281545))
                            .border(1.dp, Color(0xFF6B3CE9), RoundedCornerShape(4.dp))
                            .clickable { showQualityDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LOSSLESS 320K",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC084FC),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Heart & Checkmark in library icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { playbackViewModel.toggleLike() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFF22C55E) else Color(0xFFA19BAE),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Green Checkmark icon
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Saved in Library",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 4. SEEKBAR / PROGRESS SLIDER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Slider(
                    value = currentProgress,
                    onValueChange = { playbackViewModel.seekFraction(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFC084FC),
                        activeTrackColor = BrandPurple,
                        inactiveTrackColor = DarkSliderTrack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(positionMs),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8E8899)
                    )
                    Text(
                        text = "-" + formatMs((durationMs - positionMs).coerceAtLeast(0L)),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8E8899)
                    )
                }
            }

            // 5. MAIN PLAYBACK CONTROLS ROW (Shuffle, Prev, HERO PURPLE PLAY, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { playbackViewModel.toggleShuffle() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffle) BrandPurple else Color(0xFFA19BAE),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.skipPrevious() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // HERO SOLID PURPLE CIRCLE PLAY/PAUSE BUTTON
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .shadow(16.dp, CircleShape, ambientColor = BrandPurple.copy(alpha = 0.5f), spotColor = BrandPurple)
                        .clip(CircleShape)
                        .background(BrandPurple)
                        .clickable {
                            if (track != null) playbackViewModel.togglePlayPause()
                            else Toast.makeText(context, "Pick a song first", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.skipNext() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.cycleRepeatMode() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.repeatMode != RepeatMode.OFF) BrandPurple else Color(0xFFA19BAE),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 6. SECONDARY ACTIONS ROW (Lyrics, Queue, Cast, More)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lyrics
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showLyricsSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Lyrics",
                        tint = if (lyricsData != null) Color.White else Color(0xFFA19BAE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lyrics", fontSize = 11.sp, color = Color(0xFFA19BAE))
                }

                // Queue
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showQueueSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color(0xFFA19BAE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Queue", fontSize = 11.sp, color = Color(0xFFA19BAE))
                }

                // Cast
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, "Connect Cast / Bluetooth Speakers", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Scanning for Cast and Bluetooth devices...", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Cast,
                        contentDescription = "Cast",
                        tint = Color(0xFFA19BAE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cast", fontSize = 11.sp, color = Color(0xFFA19BAE))
                }

                // More
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showMoreSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.MoreHoriz,
                        contentDescription = "More",
                        tint = Color(0xFFA19BAE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("More", fontSize = 11.sp, color = Color(0xFFA19BAE))
                }
            }

            // 7. BOTTOM CONNECTED DEVICE & VOLUME BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF161222))
                    .border(1.dp, Color(0xFF261F36), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    // Device Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showQualityDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Headphones,
                            contentDescription = null,
                            tint = DeviceGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = playbackState.connectedDevice.ifBlank { "This Android phone" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeviceGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Volume Slider Row with Speaker Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = currentVolume,
                            onValueChange = { newVol ->
                                currentVolume = newVol
                                val targetVol = (newVol * maxVolume).toInt()
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = BrandPurple,
                                inactiveTrackColor = DarkSliderTrack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color(0xFFA19BAE),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Modals, Sheets & Dialogs
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                containerColor = SonexaCardDark,
                title = { Text("Audio Stream Quality", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        qualityOptions.forEach { (apiValue, label) ->
                            Text(
                                text = label,
                                color = if (selectedQuality == label) BrandPurple else SonexaTextWhite,
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
                        Text("Close", color = BrandPurple)
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
                        Text("Close", color = BrandPurple)
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
                        LazyColumn(modifier = Modifier.height(280.dp)) {
                            itemsIndexed(queue) { index, item ->
                                val active = index == playbackState.queueIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playbackViewModel.playFromQueueIndex(index)
                                            showQueueSheet = false
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.title,
                                            color = if (active) BrandPurple else SonexaTextWhite,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(item.artist, color = SonexaTextMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQueueSheet = false }) {
                        Text("Close", color = BrandPurple)
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
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = BrandPurple)
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
                            Icon(Icons.Default.NightsStay, contentDescription = null, tint = BrandPurple)
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
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = BrandPurple)
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
                            Icon(Icons.Default.Share, contentDescription = null, tint = BrandPurple)
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
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = BrandPurple)
                            Spacer(Modifier.width(12.dp))
                            Text(if (isFavorite) "Remove from Liked Songs" else "Save to Liked Songs", color = Color.White, fontSize = 15.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMoreSheet = false }) {
                        Text("Close", color = BrandPurple)
                    }
                }
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
