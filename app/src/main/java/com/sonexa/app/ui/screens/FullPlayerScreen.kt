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
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var lyricsData by remember { mutableStateOf<LyricsResponse?>(null) }
    var lyricsLoading by remember { mutableStateOf(false) }
    var lyricsError by remember { mutableStateOf<String?>(null) }

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

            // 4. SLEEK WHITE SCRUBBER & TIMESTAMPS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Slider(
                    value = currentProgress,
                    onValueChange = { playbackViewModel.seekFraction(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.20f)
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMutedPurple
                    )
                    Text(
                        text = formatMs(durationMs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMutedPurple
                    )
                }
            }

            // 5. MAIN PLAYBACK CONTROLS (Shuffle, Prev, HERO PURE WHITE PLAY/PAUSE, Next, Repeat)
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
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffle) SpotifyGreen else TextMutedPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.skipPrevious() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // HERO SOLID PURE WHITE CIRCLE PLAY/PAUSE BUTTON
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.3f), spotColor = Color.White)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (track != null) playbackViewModel.togglePlayPause()
                            else Toast.makeText(context, "Pick a song first", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.skipNext() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = { playbackViewModel.cycleRepeatMode() },
                    modifier = Modifier.size(40.dp)
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
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                        }
                    }
                }
            }

            // 6. UTILITY ACTION BAR (Lyrics, Queue, Device, More with Labels)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // 2. Queue
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
                    Text("Queue", fontSize = 11.sp, color = TextMutedPurple)
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
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMoreSheet = false }) {
                        Text("Close", color = SpotifyGreen)
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
