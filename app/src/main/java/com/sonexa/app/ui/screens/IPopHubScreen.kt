package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.IPopArtistDto
import com.sonexa.app.data.model.IPopPlaylistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.IPopViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

private val IPopPink = Color(0xFFFF2A85)
private val IPopPurple = Color(0xFF8A2BE2)
private val IPopCardBg = Color(0xFF140D1D)
private val IPopBorder = Color(0xFF2E193C)
private val TextMuted = Color(0xFFA89BB5)

@Composable
fun IPopHubScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    onOpenArtist: (String) -> Unit = {},
    playbackViewModel: PlaybackViewModel,
    iPopViewModel: IPopViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeState by iPopViewModel.homeState.collectAsState()
    val selectedSubgenre by iPopViewModel.selectedSubgenre.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0612))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (val state = homeState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IPopPink)
                }
            }
            is CatalogUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load I-Pop", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { iPopViewModel.loadHome(selectedSubgenre) },
                        colors = ButtonDefaults.buttonColors(containerColor = IPopPink)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            is CatalogUiState.Ready -> {
                val feed = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 125.dp)
                ) {
                    // 1. Top Bar: Back + Glowing Title + Search
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "HOME OF I-POP 🇮🇳",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = IPopPink
                                )
                                Text(
                                    text = "The Sound of New India",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { Toast.makeText(context, "Explore I-Pop Playlists", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.MusicNote, contentDescription = "I-Pop", tint = IPopPink)
                            }
                        }
                    }

                    // 2. Hero Spotlight Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(190.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8A2BE2), Color(0xFFFF2A85), Color(0xFFFF8C00))
                                    )
                                )
                                .clickable {
                                    if (feed.trendingTracks.isNotEmpty()) {
                                        playbackViewModel.playQueue(feed.trendingTracks, 0, "I-Pop Superstars Mix")
                                        onOpenFullPlayer()
                                    }
                                }
                                .padding(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterStart),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "FEATURED SPOTLIGHT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = feed.spotlightTitle,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = feed.spotlightSubtitle,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Spotlight", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }

                    // 3. Subgenre Filters
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(feed.subgenres) { sub ->
                                val isSelected = selectedSubgenre.equals(sub, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) IPopPink else IPopCardBg)
                                        .border(1.dp, if (isSelected) IPopPink else IPopBorder, RoundedCornerShape(20.dp))
                                        .clickable { iPopViewModel.loadHome(sub) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // 4. Spotlight Artists
                    if (feed.spotlightArtists.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Spotlight Artists",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                items(feed.spotlightArtists) { artist ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(84.dp)
                                            .clickable { onOpenArtist(artist.name) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, IPopPink, CircleShape)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(artist.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = artist.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = artist.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${artist.followers} fans",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Featured Playlists & Mixtapes
                    if (feed.featuredPlaylists.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Featured I-Pop Playlists",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                items(feed.featuredPlaylists) { pl ->
                                    Box(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(IPopCardBg)
                                            .border(1.dp, IPopBorder, RoundedCornerShape(16.dp))
                                            .clickable {
                                                if (pl.tracks.isNotEmpty()) {
                                                    playbackViewModel.playQueue(pl.tracks, 0, pl.title)
                                                    onOpenFullPlayer()
                                                }
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(pl.coverUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = pl.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(6.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color.Black.copy(alpha = 0.7f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(pl.badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = pl.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${pl.trackCount} Tracks",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Trending I-Pop Tracks
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trending I-Pop Songs 🔥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${feed.trendingTracks.size} Tracks",
                                fontSize = 12.sp,
                                color = IPopPink
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(feed.trendingTracks) { track ->
                        val isPlayingThis = playbackState.track?.id == track.id && playbackState.isPlaying
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val idx = feed.trendingTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                                    playbackViewModel.playQueue(feed.trendingTracks, idx, "Trending I-Pop")
                                    onOpenFullPlayer()
                                }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IPopCardBg)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(track.effectiveCoverUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isPlayingThis) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0x80000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, tint = IPopPink, modifier = Modifier.size(22.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPlayingThis) IPopPink else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.album.orEmpty()}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val isTrackLiked = track.isLiked || com.sonexa.app.data.local.LikedSongsStore.isLiked(track.id)
                            IconButton(
                                onClick = { playbackViewModel.toggleLike(track) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTrackLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isTrackLiked) IPopPink else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
