package com.sonexa.app.ui.screens

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.LyricsResponse
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.AiRepository
import com.sonexa.app.ui.components.YouTubePlayerView
import com.sonexa.app.ui.theme.SonexaCardDark
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.PlaybackUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.RepeatMode
import com.sonexa.app.ui.viewmodel.SettingsViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private val PlayerGreen = Color(0xFF1ED760)
private val PlayerMuted = Color(0xFF9E98AB)
private val PlayerCard = Color(0xFF181324)
private val PlayerCardBorder = Color(0xFF282038)

@Composable
internal fun FullPlayerTopBar(
    playlistName: String,
    onMinimize: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMinimize, modifier = Modifier.size(36.dp)) {
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
                color = PlayerMuted
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
        IconButton(onClick = onMore, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun FullPlayerArtworkPager(
    queue: List<TrackDto>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    youtubeProvider: com.sonexa.app.audio.playback.YouTubePlaybackProvider? = null
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            val isYouTube = item?.isYouTube == true || (item?.effectiveVideoId?.isNotBlank() == true && (item.audioUrl.isBlank()))
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.98f)
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.85f),
                            spotColor = Color.Black
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF1F182E))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                ) {
                    if (isYouTube && page == pagerState.currentPage && youtubeProvider != null) {
                        YouTubePlayerView(
                            youtubeProvider = youtubeProvider,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (cover.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(cover).crossfade(true).build(),
                            contentDescription = item?.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PlayerGreen,
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FullPlayerTrackMeta(
    title: String,
    artist: String,
    isFavorite: Boolean,
    onToggleLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = artist,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAFA9BB),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onToggleLike, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) PlayerGreen else Color(0xFFAFA9BB),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
internal fun FullPlayerProgressSection(
    durationMs: Long,
    playbackViewModel: PlaybackViewModel
) {
    val positionMs by playbackViewModel.elapsedMs.collectAsState()
    val safeDuration = durationMs.coerceAtLeast(0)
    val safePosition = positionMs.coerceAtLeast(0)
    val progress = if (safeDuration > 0) (safePosition.toFloat() / safeDuration).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        PremiumSongProgressBar(
            progress = progress,
            onSeek = { playbackViewModel.seekFraction(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = formatPlayerMs(safePosition),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
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
                    color = PlayerGreen,
                    letterSpacing = 0.5.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPlayerMs(safeDuration),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFAFA9BB)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
}

@Composable
internal fun FullPlayerTransportControls(
    shuffle: Boolean,
    repeatMode: RepeatMode,
    isPlaying: Boolean,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle, modifier = Modifier.size(42.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffle) PlayerGreen else PlayerMuted,
                    modifier = Modifier.size(24.dp)
                )
                if (shuffle) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(PlayerGreen)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = if (isPlaying) PlayerGreen.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                    spotColor = Color.White
                )
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        IconButton(onClick = onRepeat, modifier = Modifier.size(42.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) PlayerGreen else PlayerMuted,
                    modifier = Modifier.size(24.dp)
                )
                if (repeatMode != RepeatMode.OFF) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(PlayerGreen)
                    )
                }
            }
        }
    }
}

@Composable
internal fun FullPlayerVolumeBar() {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1) }
    var currentVolume by remember {
        mutableStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2))
    }
    var isMuted by remember { mutableStateOf(currentVolume == 0) }
    var previousVolume by remember { mutableStateOf(if (currentVolume > 0) currentVolume else (maxVolume / 3)) }

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
            } catch (_: Exception) {
            }
        }
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
        } catch (_: Exception) {
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
            try {
                context.contentResolver.unregisterContentObserver(contentObserver)
            } catch (_: Exception) {
            }
        }
    }

    fun updateVolume(newVol: Int) {
        val clamped = newVol.coerceIn(0, maxVolume)
        currentVolume = clamped
        isMuted = clamped == 0
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    if (isMuted || currentVolume == 0) {
                        val restore = if (previousVolume > 0) previousVolume else (maxVolume / 3)
                        updateVolume(restore)
                    } else {
                        previousVolume = currentVolume
                        updateVolume(0)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted || currentVolume == 0) Icons.Default.VolumeOff else Icons.Default.VolumeMute,
                contentDescription = "Mute",
                tint = if (isMuted || currentVolume == 0) Color(0xFFEF4444) else Color(0xFF8E889D),
                modifier = Modifier.size(14.dp)
            )
        }

        var isDragging by remember { mutableStateOf(false) }
        var dragFraction by remember { mutableFloatStateOf(0f) }
        val volFraction = (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
        val activeFraction = if (isDragging) dragFraction else volFraction

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width).coerceIn(0f, 1f)
                        updateVolume((frac * maxVolume).toInt())
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                            dragFraction = frac
                            updateVolume((frac * maxVolume).toInt())
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val trackHeight = if (isDragging) 4.dp else 2.5.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(activeFraction.coerceIn(0.001f, 1f))
                    .height(trackHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), PlayerGreen)
                        )
                    )
            )
            val thumbRadiusPx = with(LocalDensity.current) { (if (isDragging) 5.dp else 3.5.dp).toPx() }
            val thumbOffsetPx = ((widthPx * activeFraction) - thumbRadiusPx)
                .coerceIn(0f, (widthPx - thumbRadiusPx * 2).coerceAtLeast(0f))
            Box(
                modifier = Modifier
                    .offset(x = with(LocalDensity.current) { thumbOffsetPx.toDp() })
                    .size(if (isDragging) 10.dp else 7.dp)
                    .shadow(3.dp, CircleShape, ambientColor = PlayerGreen, spotColor = Color.White)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable { updateVolume((currentVolume + 1).coerceAtMost(maxVolume)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Max Volume",
                tint = Color(0xFF8E889D),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
internal fun FullPlayerUtilityBar(
    isPodcast: Boolean,
    playbackSpeed: Float,
    sleepTimerRemainingMs: Long?,
    hasLyrics: Boolean,
    onSpeed: () -> Unit,
    onTimer: () -> Unit,
    onLyrics: () -> Unit,
    onVibe: () -> Unit,
    onQueue: () -> Unit,
    onDevice: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPodcast) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onSpeed)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${playbackSpeed}x",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (playbackSpeed != 1.0f) PlayerGreen else Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Speed", fontSize = 11.sp, color = PlayerMuted)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onTimer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Sleep Timer",
                    tint = if (sleepTimerRemainingMs != null) PlayerGreen else PlayerMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (sleepTimerRemainingMs != null) "${sleepTimerRemainingMs / 60000}m" else "Timer",
                    fontSize = 11.sp,
                    color = if (sleepTimerRemainingMs != null) PlayerGreen else PlayerMuted
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onLyrics)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Lyrics",
                    tint = if (hasLyrics) Color.White else PlayerMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Lyrics", fontSize = 11.sp, color = PlayerMuted)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onVibe)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Bolt, contentDescription = "Change Vibe", tint = PlayerGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vibe", fontSize = 11.sp, color = PlayerGreen, fontWeight = FontWeight.Bold)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onQueue)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue", tint = PlayerMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (isPodcast) "Episodes" else "Queue", fontSize = 11.sp, color = PlayerMuted)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onDevice)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Cast, contentDescription = "Device", tint = PlayerMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Device", fontSize = 11.sp, color = PlayerMuted)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onMore)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Outlined.MoreHoriz, contentDescription = "More", tint = PlayerMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("More", fontSize = 11.sp, color = PlayerMuted)
        }
    }
}

@Composable
internal fun FullPlayerUpNextCard(
    queue: List<TrackDto>,
    queueIndex: Int,
    onOpenQueue: () -> Unit,
    onFixQueue: () -> Unit,
    onPlayIndex: (Int) -> Unit
) {
    val nextItem = remember(queueIndex, queue) {
        if (queue.size > 1) {
            queue.getOrNull(queueIndex + 1) ?: queue.firstOrNull()
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF140E22).copy(alpha = 0.92f))
            .border(1.dp, Color(0xFF282038), RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenQueue)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (nextItem != null && nextItem.effectiveCoverUrl.isNotBlank()) {
                    AsyncImage(
                        model = nextItem.effectiveCoverUrl,
                        contentDescription = nextItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = PlayerGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (nextItem != null) "UP NEXT • ${nextItem.title}" else "QUEUE (${queue.size} songs)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (nextItem != null) {
                        Text(
                            text = nextItem.artist,
                            fontSize = 10.sp,
                            color = PlayerMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (queue.size > 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PlayerGreen.copy(alpha = 0.15f))
                            .clickable(onClick = onFixQueue)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PlayerGreen, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("AI Fix", color = PlayerGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Open Queue",
                    tint = PlayerMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullPlayerOverlays(
    playbackState: PlaybackUiState,
    track: TrackDto?,
    trackTitle: String,
    trackArtist: String,
    isFavorite: Boolean,
    isPlaying: Boolean,
    lyricsData: LyricsResponse?,
    lyricsLoading: Boolean,
    lyricsError: String?,
    showTimerDialog: Boolean,
    showSpeedDialog: Boolean,
    showQualityDialog: Boolean,
    showLyricsSheet: Boolean,
    showQueueSheet: Boolean,
    showEqualizerSheet: Boolean,
    showMoreSheet: Boolean,
    showAddToPlaylistSheet: Boolean,
    showChangeVibeSheet: Boolean,
    showWhyThisSongDialog: Boolean,
    showQueueRepairDialog: Boolean,
    whyThisSongResponse: com.sonexa.app.data.model.WhyThisSongResponseDto?,
    fixQueueResponse: com.sonexa.app.data.model.FixQueueResponseDto?,
    onDismissTimer: () -> Unit,
    onDismissSpeed: () -> Unit,
    onDismissQuality: () -> Unit,
    onDismissLyrics: () -> Unit,
    onDismissQueue: () -> Unit,
    onDismissEqualizer: () -> Unit,
    onDismissMore: () -> Unit,
    onDismissAddToPlaylist: () -> Unit,
    onDismissChangeVibe: () -> Unit,
    onDismissWhy: () -> Unit,
    onDismissQueueRepair: () -> Unit,
    onShowTimer: () -> Unit,
    onShowQuality: () -> Unit,
    onShowEqualizer: () -> Unit,
    onShowAddToPlaylist: () -> Unit,
    onWhyLoaded: (com.sonexa.app.data.model.WhyThisSongResponseDto) -> Unit,
    onFixLoaded: (com.sonexa.app.data.model.FixQueueResponseDto) -> Unit,
    playbackViewModel: PlaybackViewModel,
    settingsViewModel: SettingsViewModel,
    aiRepository: AiRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val queue = playbackState.queue.ifEmpty { listOfNotNull(track) }
    var selectedQuality by remember { mutableStateOf("Hi-Fi Lossless (320kbps Master)") }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = onDismissQuality,
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
                            color = if (selectedQuality == label) PlayerGreen else SonexaTextWhite,
                            fontWeight = if (selectedQuality == label) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuality = label
                                    settingsViewModel.updateString("audioQuality", apiValue)
                                    onDismissQuality()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissQuality) { Text("Close", color = PlayerGreen) }
            }
        )
    }

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = onDismissSpeed,
            containerColor = SonexaCardDark,
            title = { Text("Playback Speed", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                        val isSelected = playbackState.playbackSpeed == speed
                        Text(
                            text = "${speed}x ${if (speed == 1.0f) "(Normal)" else ""}",
                            color = if (isSelected) PlayerGreen else SonexaTextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackViewModel.setPlaybackSpeed(speed)
                                    Toast.makeText(context, "Speed set to ${speed}x", Toast.LENGTH_SHORT).show()
                                    onDismissSpeed()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissSpeed) { Text("Close", color = PlayerGreen) }
            }
        )
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = onDismissTimer,
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
                                        onDismissTimer()
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissTimer) { Text("Close", color = PlayerGreen) }
            }
        )
    }

    if (showLyricsSheet) {
        val elapsedMs by playbackViewModel.elapsedMs.collectAsState()
        com.sonexa.app.ui.components.PremiumLyricsSheet(
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            positionMs = elapsedMs,
            lyrics = lyricsData,
            loading = lyricsLoading,
            error = lyricsError,
            onDismiss = onDismissLyrics,
            onSeekToLine = { tMs -> playbackViewModel.seekToMs(tMs) }
        )
    }

    if (showQueueSheet) {
        AlertDialog(
            onDismissRequest = onDismissQueue,
            containerColor = SonexaCardDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Up Next (${queue.size})",
                        color = SonexaTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (queue.size > 2) {
                        Spacer(Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PlayerGreen.copy(alpha = 0.15f))
                                .clickable {
                                    scope.launch {
                                        aiRepository.fixQueue(queue).onSuccess(onFixLoaded)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PlayerGreen, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("AI Fix", color = PlayerGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            },
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
                                        onDismissQueue()
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
                                            model = ImageRequest.Builder(context).data(item.effectiveCoverUrl).crossfade(true).build(),
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
                                        color = if (active) PlayerGreen else SonexaTextWhite,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp
                                    )
                                    Text(item.artist, color = SonexaTextMuted, fontSize = 12.sp)
                                }
                                if (active) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = "Playing", tint = PlayerGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissQueue) { Text("Close", color = PlayerGreen) }
            }
        )
    }

    if (showMoreSheet) {
        AlertDialog(
            onDismissRequest = onDismissMore,
            containerColor = SonexaCardDark,
            title = { Text("Track Options", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                playbackViewModel.applyEqualizerPreset(playbackState.equalizer.presetName)
                                onShowEqualizer()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Audio Equalizer (5-Band DSP)", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                onShowTimer()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Sleep Timer", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                onShowQuality()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Audio Stream Quality (320kbps)", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                track?.let { com.sonexa.app.util.SonexaShareHelper.shareTrack(context, it) }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Share Song", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                playbackViewModel.toggleLike()
                                Toast.makeText(context, if (!isFavorite) "Saved to Your Library" else "Removed from Library", Toast.LENGTH_SHORT).show()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text(if (isFavorite) "Remove from Liked Songs" else "Save to Liked Songs", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                scope.launch {
                                    aiRepository.getWhyThisSong(track?.id ?: "unknown").onSuccess(onWhyLoaded)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Why this song? (AI Match Insight)", color = Color.White, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissMore()
                                onShowAddToPlaylist()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = PlayerGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Add to Playlist", color = Color.White, fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissMore) { Text("Close", color = PlayerGreen) }
            }
        )
    }

    if (showAddToPlaylistSheet && track != null) {
        com.sonexa.app.ui.components.AddToPlaylistBottomSheet(
            track = track,
            onDismiss = onDismissAddToPlaylist
        )
    }
    if (showEqualizerSheet) {
        com.sonexa.app.ui.components.PremiumEqualizerSheet(
            snapshot = playbackState.equalizer,
            isPlaying = isPlaying,
            onDismiss = onDismissEqualizer,
            onEnabledChange = { playbackViewModel.setEqualizerEnabled(it) },
            onBandChange = { index, level -> playbackViewModel.setEqualizerBand(index, level) },
            onBassChange = { playbackViewModel.setBassBoost(it) },
            onVirtualChange = { playbackViewModel.setVirtualizer(it) },
            onPreset = { playbackViewModel.applyEqualizerPreset(it) },
            onReset = { playbackViewModel.resetEqualizer() }
        )
    }
    if (showChangeVibeSheet) {
        com.sonexa.app.ui.components.ChangeVibeBottomSheet(
            onDismiss = onDismissChangeVibe,
            currentTrack = track,
            currentQueue = playbackState.queue,
            onApplyVibe = { newQueue, vibeTitle ->
                if (newQueue.isNotEmpty()) {
                    playbackViewModel.playQueue(newQueue, 0, "Vibe: $vibeTitle")
                }
            }
        )
    }
    if (showWhyThisSongDialog && whyThisSongResponse != null) {
        com.sonexa.app.ui.components.WhyThisSongDialog(
            response = whyThisSongResponse,
            onDismiss = onDismissWhy
        )
    }
    if (showQueueRepairDialog && fixQueueResponse != null) {
        com.sonexa.app.ui.components.QueueRepairDialog(
            response = fixQueueResponse,
            onApply = {
                val balanced = fixQueueResponse.balancedQueue
                if (balanced.isNotEmpty()) {
                    playbackViewModel.playQueue(balanced, 0, "AI Balanced Queue")
                    Toast.makeText(context, "Applied balanced queue", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = onDismissQueueRepair
        )
    }
}

internal fun formatPlayerMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
