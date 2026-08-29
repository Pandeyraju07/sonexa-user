package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AlbumDetailViewModel
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

private fun formatDurationMs(ms: Long): String {
    val mins = ms / 60000
    val secs = (ms / 1000) % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

@Composable
fun AlbumDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    albumId: String = "alb_1",
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()
    LaunchedEffect(albumId) { viewModel.load(albumId) }
    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    val ready = uiState as? CatalogUiState.Ready
    val album = ready?.data?.album
    val tracks = ready?.data?.tracks.orEmpty()
    val albumTitle = album?.title ?: "Album"
    val coverUrl = album?.coverUrl.orEmpty()

    fun playTrack(track: TrackDto) {
        if (track.audioUrl.isBlank()) {
            Toast.makeText(context, "No audio URL for this track", Toast.LENGTH_SHORT).show()
            return
        }
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackViewModel.playQueue(tracks.ifEmpty { listOf(track) }, index, albumTitle)
        onOpenFullPlayer()
    }

    fun playAlbum() {
        val first = tracks.firstOrNull { it.audioUrl.isNotBlank() } ?: tracks.firstOrNull()
        if (first == null) {
            Toast.makeText(context, "No tracks available", Toast.LENGTH_SHORT).show()
            return
        }
        playbackViewModel.playQueue(tracks, tracks.indexOf(first).coerceAtLeast(0), albumTitle)
        onOpenFullPlayer()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                CircularProgressIndicator(
                    color = SonexaPurpleLight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is CatalogUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.message, color = SonexaTextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SonexaGradientButton(text = "Retry", onClick = { viewModel.load(albumId) })
                }
            }
            is CatalogUiState.Ready -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SonexaInputBg)
                                    .clickable { onNavigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SonexaTextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF6B3CE9), Color(0xFFE534B2)))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = albumTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(70.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = albumTitle, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
                            Text(
                                text = album?.let { "${it.artist} • ${it.year} • ${tracks.size} Songs" }
                                    ?: "Album details",
                                fontSize = 13.sp,
                                color = SonexaTextMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SonexaGradientButton(
                                text = "Play Album ▶",
                                onClick = { playAlbum() }
                            )
                        }
                    }

                    item {
                        Text(text = "Tracklist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }

                    if (tracks.isEmpty()) {
                        item {
                            Text(
                                text = "No tracks available",
                                fontSize = 14.sp,
                                color = SonexaTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        itemsIndexed(tracks) { idx, track ->
                            val subtitle = buildString {
                                append(formatDurationMs(track.durationMs))
                                if (track.playsCount.isNotBlank()) append(" • ${track.playsCount}")
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { playTrack(track) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SonexaTextMuted,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    Column {
                                        Text(text = track.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                        Text(text = subtitle, fontSize = 12.sp, color = SonexaTextSubtle)
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = SonexaTextMuted,
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
