package com.sonexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SpotifyGreen
import com.sonexa.app.ui.viewmodel.PlaybackUiState

@Composable
fun HomeFeedContent(
    avatarInitial: String,
    selectedFeedCategory: String,
    onSelectFeedCategory: (String) -> Unit,
    onOpenProfileDrawer: () -> Unit,
    onOpenMusicDna: () -> Unit,
    onOpenMusicJourney: () -> Unit,
    quickAccessItems: List<QuickCardItem>,
    continueListening: List<TrackDto>,
    allTrending: List<TrackDto>,
    allAlbums: List<AlbumDto>,
    allArtists: List<ArtistDto>,
    playbackState: PlaybackUiState,
    onPlayTrack: (TrackDto?, List<TrackDto>, String) -> Unit,
    onOpenLikedPlaylist: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onTogglePlayPause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 125.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar with User Avatar & Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFC084FC)))
                    )
                    .clickable { onOpenProfileDrawer() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarInitial,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Filter Chips: All, Music, Podcasts, Live Events, I-Pop
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Music", "Podcasts", "Live Events", "I-Pop").forEach { cat ->
                    val isSelected = selectedFeedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SpotifyGreen else Color(0xFF1E172E))
                            .border(
                                1.dp,
                                if (isSelected) SpotifyGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectFeedCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color(0xFFD1D5DB)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 100% Dynamic Quick Access Grid (from live API data)
        if (quickAccessItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAccessItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessCard(
                            item = pair[0],
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (pair[0].isLiked) {
                                    onOpenLikedPlaylist()
                                } else {
                                    val track = (continueListening + allTrending).firstOrNull { it.id == pair[0].id }
                                        ?: allTrending.firstOrNull()
                                    onPlayTrack(track, allTrending, pair[0].title)
                                }
                            }
                        )
                        if (pair.size > 1) {
                            QuickAccessCard(
                                item = pair[1],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val track = (continueListening + allTrending).firstOrNull { it.id == pair[1].id }
                                        ?: allTrending.getOrNull(1) ?: allTrending.firstOrNull()
                                    onPlayTrack(track, allTrending, pair[1].title)
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Intelligence Cards (Music DNA & AI Journey)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2E1065), Color(0xFF1E1B4B)))
                    )
                    .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onOpenMusicDna() }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Music DNA", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Your Taste", color = Color(0xFFDDD6FE), fontSize = 11.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF064E3B)))
                    )
                    .border(1.dp, SpotifyGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onOpenMusicJourney() }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("AI Journey", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Vibe Flow", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Trending Now (Full Spotify Style with Ranks & Floating Play)
        if (allTrending.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trending Now",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Top streamed songs in India right now",
                        fontSize = 12.sp,
                        color = SonexaTextMuted
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(allTrending) { idx, track ->
                    val isCurrentTrack = playbackState.track?.id == track.id
                    val isCurrentlyPlaying = isCurrentTrack && playbackState.isPlaying
                    MediaSquareCard(
                        title = track.title,
                        subtitle = "Song • ${track.artist}",
                        imageUrl = track.effectiveCoverUrl,
                        tag = if (idx < 5) "#${idx + 1} ON CHARTS" else null,
                        isPlayingThis = isCurrentlyPlaying,
                        onQuickPlay = {
                            if (isCurrentTrack) {
                                onTogglePlayPause()
                            } else {
                                onPlayTrack(track, allTrending, "Trending Now")
                            }
                        },
                        onClick = { onPlayTrack(track, allTrending, "Trending Now") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section 2: Popular Albums & Releases (From API)
        if (allAlbums.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Popular Albums",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Trending full-length projects and compilations",
                        fontSize = 12.sp,
                        color = SonexaTextMuted
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allAlbums) { album ->
                    val isAlbumPlaying = playbackState.track?.album?.equals(album.title, ignoreCase = true) == true && playbackState.isPlaying
                    MediaSquareCard(
                        title = album.title,
                        subtitle = "Album • ${album.artist}",
                        imageUrl = album.coverUrl,
                        tag = "ALBUM",
                        isPlayingThis = isAlbumPlaying,
                        onQuickPlay = { onOpenAlbum(album.id) },
                        onClick = { onOpenAlbum(album.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section 3: Recommended Artists (From API)
        if (allArtists.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Popular Artists",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Top chart toppers and playback legends",
                        fontSize = 12.sp,
                        color = SonexaTextMuted
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allArtists) { artist ->
                    val isArtistPlaying = playbackState.track?.artist?.contains(artist.name, ignoreCase = true) == true && playbackState.isPlaying
                    ArtistCircleCard(
                        artist = artist,
                        isRadioPlaying = isArtistPlaying,
                        onQuickPlay = {
                            onOpenArtist(artist.name)
                        },
                        onClick = {
                            onOpenArtist(artist.name)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
