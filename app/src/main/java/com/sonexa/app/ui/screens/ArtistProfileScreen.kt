package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val catalog by viewModel.catalog.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    LaunchedEffect(artistId) { viewModel.load(artistId) }
    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    val ready = uiState as? CatalogUiState.Ready
    val artist = ready?.data?.artist ?: catalog?.artist
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

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SonexaInputBg)
                                    .clickable {
                                        com.sonexa.app.util.SonexaShareHelper.shareArtist(
                                            context = context,
                                            artistId = artistId,
                                            artistName = artistName
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Artist",
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

                            val listeners = artist?.followersCount?.let { "$it Monthly Listeners • Top Artist" } ?: "Artist"
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

                    // Albums & Discography section if present
                    if (catalog?.albums?.isNotEmpty() == true) {
                        item {
                            Text(text = "Albums & Discography", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(catalog!!.albums) { album ->
                                    Column(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SonexaInputBg)
                                            .clickable {
                                                if (album.tracks.isNotEmpty()) {
                                                    playbackViewModel.playQueue(album.tracks, 0, album.title)
                                                    onOpenFullPlayer()
                                                }
                                            }
                                            .padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(album.coverUrl).crossfade(true).build(),
                                            contentDescription = album.title,
                                            modifier = Modifier
                                                .size(114.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = album.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite, maxLines = 1)
                                        Text(text = "${album.year} • ${album.trackCount} Tracks", fontSize = 11.sp, color = SonexaTextMuted)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Popular Songs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Text(
                                text = "${tracks.size} songs",
                                fontSize = 12.sp,
                                color = SonexaPurpleLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                        itemsIndexed(tracks) { index, track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (index % 2 == 0) SonexaInputBg.copy(alpha = 0.5f) else Color.Transparent)
                                    .clickable { playTrack(track) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 13.sp,
                                        color = SonexaTextMuted,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                                        contentDescription = track.title,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = track.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite, maxLines = 1)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (track.versionType != "Original") {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(SonexaPurplePrimary.copy(alpha = 0.4f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(text = track.versionType, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = if (track.album.isNotBlank()) track.album else track.artist,
                                                fontSize = 12.sp,
                                                color = SonexaTextSubtle,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (track.availableProviders.isNotEmpty()) {
                                        Text(
                                            text = track.availableProviders.first(),
                                            fontSize = 10.sp,
                                            color = SonexaPurpleLight,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Play",
                                        tint = SonexaPurpleLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Load more trigger & indicator
                        if (catalog?.hasMore == true || tracks.size >= 30) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(
                                            color = SonexaPurpleLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SonexaInputBg)
                                                .border(1.dp, SonexaCardBorder, RoundedCornerShape(12.dp))
                                                .clickable { viewModel.loadMore() }
                                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = "Load More Songs",
                                                color = SonexaTextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Similar Artists shelf
                    if (catalog?.relatedArtists?.isNotEmpty() == true) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Fans Also Like", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(catalog!!.relatedArtists) { relArtist ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(90.dp)
                                            .clickable { viewModel.load(relArtist.name) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(CircleShape)
                                                .background(SonexaInputBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SonexaPurpleLight)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = relArtist.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SonexaTextWhite, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
