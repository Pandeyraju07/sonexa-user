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
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.ArtistProfileViewModel
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

@Composable
fun ArtistProfileScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    artistId: String = "art_1",
    modifier: Modifier = Modifier,
    viewModel: ArtistProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var isFollowing by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()
    LaunchedEffect(artistId) { viewModel.load(artistId) }
    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    val ready = uiState as? CatalogUiState.Ready
    val artist = ready?.data?.artist
    val tracks = ready?.data?.tracks.orEmpty()
    val artistName = artist?.name ?: "Artist"
    val imageUrl = artist?.imageUrl.orEmpty()

    fun playTrack(track: TrackDto) {
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackViewModel.playQueue(tracks.ifEmpty { listOf(track) }, index, artistName)
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
                    com.sonexa.app.ui.components.SonexaGradientButton(
                        text = "Retry",
                        onClick = { viewModel.load(artistId) }
                    )
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
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF5935E5), Color(0xFF9825DD)))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = artistName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = artistName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
                                if (artist?.verified == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            val listeners = artist?.followersCount?.let { "$it Followers" } ?: "Artist"
                            Text(text = listeners, fontSize = 13.sp, color = SonexaTextMuted)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isFollowing) SonexaInputBg else SonexaPurplePrimary)
                                        .border(1.dp, SonexaPurpleLight, RoundedCornerShape(14.dp))
                                        .clickable {
                                            isFollowing = !isFollowing
                                            Toast.makeText(
                                                context,
                                                if (isFollowing) "Following $artistName" else "Unfollowed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFollowing) "Following" else "Follow +",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SonexaGradientBrush)
                                        .clickable {
                                            val first = tracks.firstOrNull()
                                            if (first == null) {
                                                Toast.makeText(context, "No tracks available", Toast.LENGTH_SHORT).show()
                                            } else {
                                                playTrack(first)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(text = "Popular Songs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
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
                        itemsIndexed(tracks) { _, track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { playTrack(track) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SonexaPurpleLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = track.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                        if (track.album.isNotBlank()) {
                                            Text(text = track.album, fontSize = 12.sp, color = SonexaTextSubtle)
                                        }
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
