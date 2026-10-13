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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
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
import com.sonexa.app.data.model.AudioQuality
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
                text = "PLAYING FROM",
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
                    .clickable(onClick = onMore)
                    .padding(horizontal = 10.dp, vertical = 3.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = playlistName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF191326))
                .border(1.dp, Color(0xFF282038), CircleShape)
                .clickable(onClick = onMore),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
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
            contentPadding = PaddingValues(horizontal = 42.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = queue.getOrNull(page)
            val cover = item?.effectiveCoverUrl.orEmpty()
            val isYouTube = item?.isYouTube == true || (item?.effectiveVideoId?.isNotBlank() == true && (item.audioUrl.isBlank()))

            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val scale = (1f - (abs(pageOffset) * 0.12f)).coerceIn(0.85f, 1f)
            val alpha = (1f - (abs(pageOffset) * 0.35f)).coerceIn(0.45f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
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
                            tint = Color(0xFFA78BFA),
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
                fontSize = 24.sp,
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D172B))
                .border(1.dp, Color(0xFF2C253E), CircleShape)
                .clickable(onClick = onToggleLike),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFF43F5E) else Color(0xFF8B849C),
                modifier = Modifier.size(22.dp)
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
            .padding(vertical = 4.dp)
    ) {
        PremiumSongProgressBar(
            progress = progress,
            onSeek = { playbackViewModel.seekFraction(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPlayerMs(safePosition),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = formatPlayerMs(safeDuration),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFAFA9BB)
            )
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffle) Color(0xFFA855F7) else Color(0xFF7E7790),
                modifier = Modifier.size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B152A))
                .border(1.dp, Color(0xFF2E2642), CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        // Glowing White Play/Pause Button
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
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B152A))
                .border(1.dp, Color(0xFF2E2642), CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onRepeat, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) Color(0xFFA855F7) else Color(0xFF7E7790),
                modifier = Modifier.size(24.dp)
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
        // Lyrics
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onLyrics)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Lyrics",
                tint = Color(0xFF9E98AB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lyrics", fontSize = 11.sp, color = Color(0xFF9E98AB))
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.height(2.5.dp).width(16.dp))
        }

        // Vibe (Active with purple bolt and purple indicator underline)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onVibe)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Vibe",
                tint = Color(0xFFA855F7),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vibe", fontSize = 11.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                Modifier
                    .height(2.5.dp)
                    .width(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA855F7))
            )
        }

        // Queue
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onQueue)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Queue",
                tint = Color(0xFF9E98AB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (isPodcast) "Episodes" else "Queue", fontSize = 11.sp, color = Color(0xFF9E98AB))
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.height(2.5.dp).width(16.dp))
        }

        // Device
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onDevice)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Device",
                tint = Color(0xFF9E98AB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Device", fontSize = 11.sp, color = Color(0xFF9E98AB))
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.height(2.5.dp).width(16.dp))
        }

        // More
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onMore)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "More",
                tint = Color(0xFF9E98AB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("More", fontSize = 11.sp, color = Color(0xFF9E98AB))
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.height(2.5.dp).width(16.dp))
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
            .background(Color(0xFF140E22))
            .border(1.dp, Color(0xFF2A203C), RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenQueue)
            .padding(horizontal = 14.dp, vertical = 8.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF201636)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UP NEXT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF8E889E)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = nextItem?.title ?: "Queue (${queue.size} songs)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nextItem?.artist ?: "Tap to view upcoming tracks",
                        fontSize = 11.5.sp,
                        color = Color(0xFF8E889E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF092E1B))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onFixQueue)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI Fix", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Open Queue",
                    tint = Color(0xFF8E889E),
                    modifier = Modifier.size(20.dp)
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
    var showShareSheet by remember { mutableStateOf(false) }

    if (showShareSheet && track != null) {
        com.sonexa.app.ui.components.SongShareBottomSheet(
            track = track,
            onDismiss = { showShareSheet = false }
        )
    }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = onDismissQuality,
            containerColor = SonexaCardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = PlayerGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio Stream Quality", color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AudioQuality.values().forEach { quality ->
                        val isSelected = playbackState.audioQuality == quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PlayerGreen.copy(alpha = 0.14f) else Color.Transparent)
                                .clickable {
                                    playbackViewModel.setAudioQuality(quality)
                                    settingsViewModel.updateString("audioQuality", quality.key)
                                    onDismissQuality()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quality.displayName,
                                    color = if (isSelected) PlayerGreen else SonexaTextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = quality.description,
                                    color = SonexaTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = PlayerGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissQuality) { Text("Done", color = PlayerGreen, fontWeight = FontWeight.Bold) }
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
        ModalBottomSheet(
            onDismissRequest = onDismissQueue,
            containerColor = Color(0xFF090D1C),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF333E5E))
                )
            },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Up Next (${queue.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Playing from ${playbackState.sourceTitle.ifBlank { "Bollywood Bop" }}",
                            fontSize = 13.sp,
                            color = Color(0xFFAFA9BB)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF092E1B))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        aiRepository.fixQueue(queue).onSuccess(onFixLoaded)
                                    }
                                }
                                .padding(horizontal = 9.dp, vertical = 4.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("AI Fix", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("Smart Queue", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Pills Row: Shuffle, Repeat, Clear Queue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Shuffle
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161024))
                            .border(1.dp, Color(0xFF261D38), RoundedCornerShape(14.dp))
                            .clickable { playbackViewModel.toggleShuffle() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = if (playbackState.shuffle) Color(0xFF10B981) else Color(0xFF8B849C), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    // Repeat
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161024))
                            .border(1.dp, Color(0xFF261D38), RoundedCornerShape(14.dp))
                            .clickable { playbackViewModel.cycleRepeatMode() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = if (playbackState.repeatMode != RepeatMode.OFF) Color(0xFFA855F7) else Color(0xFF8B849C), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Repeat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    // Clear Queue
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161024))
                            .border(1.dp, Color(0xFF261D38), RoundedCornerShape(14.dp))
                            .clickable {
                                playbackViewModel.clearQueue()
                                Toast.makeText(context, "Queue cleared", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFF8B849C), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clear Queue", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Queue List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 340.dp)
                ) {
                    itemsIndexed(queue) { index, item ->
                        val active = index == playbackState.queueIndex
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF21153B))
                                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        playbackViewModel.playFromQueueIndex(index)
                                        onDismissQueue()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF281C44))
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
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 14.5.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(item.artist, color = Color(0xFFAFA9BB), fontSize = 12.sp, maxLines = 1)
                                    }
                                    IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF8B849C), modifier = Modifier.size(18.dp))
                                    }
                                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF8B849C), modifier = Modifier.size(20.dp))
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playbackViewModel.playFromQueueIndex(index)
                                        onDismissQueue()
                                    }
                                    .padding(horizontal = 18.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF8B849C),
                                    modifier = Modifier.width(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E172E))
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
                                        color = Color.White,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(item.artist, color = Color(0xFFAFA9BB), fontSize = 12.sp, maxLines = 1)
                                }
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF8B849C), modifier = Modifier.size(18.dp))
                                }
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF8B849C), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sticky Bottom Action Container matching Screenshot 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0C1022))
                        .border(1.dp, Color(0xFF1E2848), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Add to Playlist (Weight 1f)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(0xFF13182E))
                                .border(1.dp, Color(0xFF222B4E), RoundedCornerShape(13.dp))
                                .clickable {
                                    onDismissQueue()
                                    onShowAddToPlaylist()
                                }
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFF2B1854)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Add to Playlist",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 2. Save Queue (Weight 1f)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(0xFF13182E))
                                .border(1.dp, Color(0xFF222B4E), RoundedCornerShape(13.dp))
                                .clickable {
                                    Toast.makeText(context, "Queue saved to playlist", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF382A10)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Save Queue",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 3. Download All with Gold Crown Badge (Weight 1f - EXACT same size)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF13182E))
                                    .border(1.dp, Color(0xFF222B4E), RoundedCornerShape(13.dp))
                                    .clickable {
                                        Toast.makeText(context, "Queued songs for lossless download", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0C3020)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Download All",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Crown Badge pinned at top-end
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-5).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                                    .border(1.5.dp, Color(0xFF0C1022), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 9.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF13182E))
                        .border(1.dp, Color(0xFF222B4E), RoundedCornerShape(24.dp))
                        .clickable { onDismissQueue() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Close", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissMore,
            containerColor = Color(0xFF0F0B18),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF4B4360))
                )
            },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // Mini track banner (cover with play overlay, title, artist, circular dark button with red heart)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E162D))
                    ) {
                        if (track?.effectiveCoverUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                                contentDescription = trackTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.align(Alignment.Center).size(24.dp))
                        }
                        // Play badge overlay at bottom right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = trackArtist,
                            fontSize = 13.sp,
                            color = Color(0xFFAFA9BB),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Circular dark button with red heart
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D172B))
                            .border(1.dp, Color(0xFF2C253E), CircleShape)
                            .clickable {
                                playbackViewModel.toggleLike()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isFavorite) Color(0xFFF43F5E) else Color(0xFF8B849C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Track Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 8 Squircle Option Cards
                // 1. Audio Equalizer (5-Band DSP)
                TrackOptionCard(
                    iconBg = Color(0xFF2C1948),
                    icon = Icons.Default.Equalizer,
                    iconTint = Color(0xFFC084FC),
                    title = "Audio Equalizer (5-Band DSP)",
                    subtitle = "Customize your sound",
                    onClick = {
                        onDismissMore()
                        playbackViewModel.applyEqualizerPreset(playbackState.equalizer.presetName)
                        onShowEqualizer()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Sleep Timer
                TrackOptionCard(
                    iconBg = Color(0xFF0D3328),
                    icon = Icons.Default.NightsStay,
                    iconTint = Color(0xFF2DD4BF),
                    title = "Sleep Timer",
                    subtitle = "Stop playback automatically",
                    onClick = {
                        onDismissMore()
                        onShowTimer()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Audio Stream Quality
                TrackOptionCard(
                    iconBg = Color(0xFF112648),
                    icon = Icons.Default.GraphicEq,
                    iconTint = Color(0xFF38BDF8),
                    title = "Audio Stream Quality",
                    subtitle = "Hi-Fi Lossless (320kbps Master)",
                    trailingContent = {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF281440))
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("320K", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC4B5FD))
                            Spacer(Modifier.width(3.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFC4B5FD), modifier = Modifier.size(14.dp))
                        }
                    },
                    onClick = {
                        onDismissMore()
                        onShowQuality()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Share Song
                TrackOptionCard(
                    iconBg = Color(0xFF381525),
                    icon = Icons.Default.Share,
                    iconTint = Color(0xFFFB7185),
                    title = "Share Song",
                    subtitle = "Share with friends",
                    onClick = {
                        onDismissMore()
                        showShareSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Save to Liked Songs
                TrackOptionCard(
                    iconBg = Color(0xFF38151D),
                    icon = Icons.Default.Favorite,
                    iconTint = Color(0xFFF43F5E),
                    title = "Save to Liked Songs",
                    subtitle = "Add to your liked songs",
                    trailingContent = {
                        if (isFavorite) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF092E1B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    },
                    onClick = {
                        onDismissMore()
                        if (!isFavorite) {
                            playbackViewModel.toggleLike()
                            Toast.makeText(context, "Saved to Liked Songs", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 6. Why this song? (AI Match Insight)
                TrackOptionCard(
                    iconBg = Color(0xFF2C1948),
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFC084FC),
                    title = "Why this song? (AI Match Insight)",
                    subtitle = "Get AI powered insights",
                    onClick = {
                        onDismissMore()
                        scope.launch {
                            aiRepository.getWhyThisSong(track?.id ?: "unknown").onSuccess(onWhyLoaded)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 7. Add to Playlist
                TrackOptionCard(
                    iconBg = Color(0xFF112648),
                    icon = Icons.Default.PlaylistAdd,
                    iconTint = Color(0xFF38BDF8),
                    title = "Add to Playlist",
                    subtitle = "Add to existing or new playlist",
                    onClick = {
                        onDismissMore()
                        onShowAddToPlaylist()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 8. Remove from Liked Songs
                TrackOptionCard(
                    iconBg = Color(0xFF38151D),
                    icon = Icons.Default.DeleteOutline,
                    iconTint = Color(0xFFEF4444),
                    title = "Remove from Liked Songs",
                    titleColor = Color(0xFFEF4444),
                    subtitle = "Remove from your liked songs",
                    onClick = {
                        onDismissMore()
                        if (isFavorite) {
                            playbackViewModel.toggleLike()
                            Toast.makeText(context, "Removed from Liked Songs", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1C182A))
                        .border(1.dp, Color(0xFF2E2744), RoundedCornerShape(24.dp))
                        .clickable { onDismissMore() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Close", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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

@Composable
private fun TrackOptionCard(
    iconBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color.White,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161024))
            .border(1.dp, Color(0xFF261D38), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFFAFA9BB),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF8B849C),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

