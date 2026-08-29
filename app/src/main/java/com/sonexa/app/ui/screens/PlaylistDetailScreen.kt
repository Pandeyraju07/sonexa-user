package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PlaylistDetailViewModel

@Composable
fun PlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    playlistId: String = "pl_1",
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var isCollaborative by remember { mutableStateOf(true) }
    var showCreateModal by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()
    LaunchedEffect(playlistId) { viewModel.load(playlistId) }
    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    val ready = uiState as? CatalogUiState.Ready
    val playlist = ready?.data?.playlist
    val tracks = ready?.data?.tracks.orEmpty()
    val playlistTitle = playlist?.title ?: "Playlist"
    val coverUrl = playlist?.coverUrl.orEmpty()

    fun playTrack(track: TrackDto) {
        if (track.audioUrl.isBlank()) {
            Toast.makeText(context, "No audio URL for this track", Toast.LENGTH_SHORT).show()
            return
        }
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackViewModel.playQueue(tracks.ifEmpty { listOf(track) }, index, playlistTitle)
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
                    SonexaGradientButton(text = "Retry", onClick = { viewModel.load(playlistId) })
                }
            }
            is CatalogUiState.Ready -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                            IconButton(onClick = { showCreateModal = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Playlist",
                                    tint = SonexaPurpleLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = playlistTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(70.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = playlistTitle, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
                            Text(
                                text = playlist?.subtitle?.ifBlank { null }
                                    ?: "${tracks.size} Songs",
                                fontSize = 13.sp,
                                color = SonexaTextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x209825DD))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = SonexaPurpleLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCollaborative) "Collaborative Playlist Enabled" else "Personal Playlist",
                                    fontSize = 12.sp,
                                    color = SonexaPurpleLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            SonexaGradientButton(
                                text = "Shuffle Play ▶",
                                onClick = {
                                    tracks.randomOrNull()?.let { playTrack(it) }
                                        ?: Toast.makeText(context, "No tracks in this playlist", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    item {
                        Text(text = "Playlist Tracks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
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
                                        Text(text = track.artist, fontSize = 12.sp, color = SonexaTextSubtle)
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

        if (showCreateModal) {
            AlertDialog(
                onDismissRequest = { showCreateModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Create New Playlist", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("My New Mix 2026", color = SonexaTextSubtle) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showCreateModal = false
                        Toast.makeText(context, "Playlist Created!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text(text = "Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateModal = false }) {
                        Text(text = "Cancel", color = SonexaTextMuted)
                    }
                }
            )
        }
    }
}
