package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.ui.theme.*

private val SpotifyGreen = Color(0xFF1ED760)

data class LibraryListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val isLikedSongs: Boolean = false,
    val isPinned: Boolean = false,
    val isGreenTitle: Boolean = false,
    val type: String = "playlist" // "playlist", "album", "artist"
)

@Composable
fun LibraryScreen(
    onOpenDownloads: () -> Unit = {},
    onOpenPlaylist: (String) -> Unit = {},
    onOpenAlbum: (String) -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    onOpenPodcast: (String) -> Unit = {},
    onOpenEpisode: (String) -> Unit = {},
    onPlayDownloadedEpisode: (com.sonexa.app.data.local.DownloadedEpisode) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    var isGridView by remember { mutableStateOf(false) }

    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val userDisplayName = remember(sessionManager.userName, sessionManager.userEmail) {
        sessionManager.userName?.takeIf { it.isNotBlank() }
            ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Listener"
    }
    val avatarInitial = remember(userDisplayName) {
        userDisplayName.firstOrNull()?.uppercase() ?: "U"
    }

    val likedSongs by LikedSongsStore.likedSongs.collectAsState()
    val likedCount = likedSongs.size
    val downloadedEpisodes by com.sonexa.app.data.local.PodcastDownloadManager.downloadedEpisodes.collectAsState()

    val libraryItems = remember(likedCount, userDisplayName, downloadedEpisodes) {
        val baseList = mutableListOf(
            LibraryListItem(
                id = "pl_liked",
                title = "Liked Songs",
                subtitle = if (likedCount > 0) "Playlist • $userDisplayName • $likedCount songs" else "Playlist • $userDisplayName",
                imageUrl = "",
                isLikedSongs = true,
                isPinned = true
            ),
            LibraryListItem(
                id = "pl_bolly",
                title = "Bollywood spicy 🔥",
                subtitle = "Playlist • Yash Kashyap",
                imageUrl = "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg",
                isGreenTitle = true
            ),
            LibraryListItem(
                id = "alb_vaapas",
                title = "Main Vaapas Aaunga (Original Mo...",
                subtitle = "Album • A.R. Rahman",
                imageUrl = "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg",
                type = "album"
            ),
            LibraryListItem(
                id = "pl_10s",
                title = "<10s",
                subtitle = "Playlist • Yash Kashyap",
                imageUrl = "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg"
            ),
            LibraryListItem(
                id = "alb_chand",
                title = "Chand Mera Dil",
                subtitle = "Album • Pritam",
                imageUrl = "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg",
                type = "album"
            ),
            LibraryListItem(
                id = "pod_1542452346",
                title = "The Ranveer Show (TRS हिंदी)",
                subtitle = "Podcast • BeerBiceps • New episode today",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Podcasts126/v4/4a/12/f9/4a12f915-0557-0a2a-281b-5e60d2ecb3fb/mza_16382103562699898858.jpg/600x600bb.jpg",
                isPinned = true,
                type = "podcast"
            ),
            LibraryListItem(
                id = "art_arijit",
                title = "Arijit Singh",
                subtitle = "Artist",
                imageUrl = "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg",
                type = "artist"
            )
        )

        // Add downloaded episodes
        downloadedEpisodes.forEach { ep ->
            baseList.add(
                LibraryListItem(
                    id = ep.id,
                    title = ep.title,
                    subtitle = "Downloaded • ${ep.podcastTitle} • ${ep.durationLabel}",
                    imageUrl = ep.coverUrl,
                    type = "downloaded_podcast"
                )
            )
        }

        baseList
    }

    val filteredItems = remember(selectedFilter, libraryItems) {
        when (selectedFilter) {
            "Playlists" -> libraryItems.filter { it.type == "playlist" || it.isLikedSongs }
            "Podcasts" -> libraryItems.filter { it.type == "podcast" || it.type == "downloaded_podcast" }
            "Downloaded" -> libraryItems.filter { it.type == "downloaded_podcast" }
            "Albums" -> libraryItems.filter { it.type == "album" }
            "Artists" -> libraryItems.filter { it.type == "artist" }
            else -> libraryItems
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 125.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header: Avatar + "Your Library" + Search + Add
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFC4B5FD))
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInitial,
                            color = Color(0xFF1B1629),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Your Library",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { Toast.makeText(context, "Search your library", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { Toast.makeText(context, "Create new playlist", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Filter Chips: Playlists, Podcasts, Downloaded, Albums, Artists
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Playlists", "Podcasts", "Downloaded", "Albums", "Artists")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SpotifyGreen else Color(0xFF282828))
                            .clickable { selectedFilter = if (isSelected && filter != "All") "All" else filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sort & View Toggle Row: "⇅ Recents" + Grid icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { Toast.makeText(context, "Sorted by Recents", Toast.LENGTH_SHORT).show() }
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = "Sort",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recents",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.GridView,
                        contentDescription = "Grid",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Library List Content (Exact Screenshot Match)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    LibraryRowItem(
                        item = item,
                        onClick = {
                            when (item.type) {
                                "album" -> onOpenAlbum(item.id)
                                "artist" -> onOpenArtist(item.title)
                                "podcast" -> onOpenPodcast(item.id)
                                "downloaded_podcast" -> {
                                    val found = downloadedEpisodes.find { it.id == item.id }
                                    if (found != null) {
                                        onPlayDownloadedEpisode(found)
                                    } else {
                                        onOpenPodcast(item.id)
                                    }
                                }
                                else -> onOpenPlaylist(item.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryRowItem(
    item: LibraryListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Image or Gradient Liked Songs Box
        if (item.isLikedSongs) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF450AF5), Color(0xFF8E8EE5), Color(0xFFC4B5FD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Liked",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(if (item.type == "artist") CircleShape else RoundedCornerShape(4.dp))
                    .background(Color(0xFF282828))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isGreenTitle) SpotifyGreen else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = item.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFFA19BAE),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
