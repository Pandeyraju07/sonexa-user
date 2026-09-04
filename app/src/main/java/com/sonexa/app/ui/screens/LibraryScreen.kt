package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.data.local.PodcastDownloadManager
import com.sonexa.app.data.local.UserPlaylistStore
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.ui.viewmodel.LibraryViewModel

private val SpotifyGreen = Color(0xFF1ED760)

data class LibraryItemModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val isLikedSongs: Boolean = false,
    val isPinned: Boolean = false,
    val isUserCreated: Boolean = false,
    val type: String = "playlist" // "playlist", "album", "artist", "podcast", "downloaded_podcast"
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
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val userDisplayName = remember(sessionManager.userName, sessionManager.userEmail) {
        sessionManager.userName?.takeIf { it.isNotBlank() }
            ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Listener"
    }
    val avatarInitial = remember(userDisplayName) {
        userDisplayName.firstOrNull()?.uppercase() ?: "U"
    }

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Edit/Delete state for playlist
    var playlistToEdit by remember { mutableStateOf<PlaylistDto?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistDto?>(null) }

    val userPlaylists by UserPlaylistStore.playlists.collectAsState()
    val likedSongs by LikedSongsStore.likedSongs.collectAsState()
    val likedCount = likedSongs.size
    val downloadedEpisodes by PodcastDownloadManager.downloadedEpisodes.collectAsState()
    val libraryUiState by viewModel.uiState.collectAsState()
    val apiLibrary = (libraryUiState as? com.sonexa.app.ui.viewmodel.CatalogUiState.Ready)?.data

    // Aggregate 100% Dynamic Library Items directly from User & API responses
    val allItems = remember(userPlaylists, likedCount, downloadedEpisodes, userDisplayName, apiLibrary) {
        val list = mutableListOf<LibraryItemModel>()

        // 1. Liked Songs (Always on top / pinned)
        list.add(
            LibraryItemModel(
                id = "pl_liked",
                title = "Liked Songs",
                subtitle = if (likedCount > 0) "Playlist • $userDisplayName • $likedCount songs" else "Playlist • $userDisplayName",
                imageUrl = "",
                isLikedSongs = true,
                isPinned = true,
                type = "playlist"
            )
        )

        // 2. User Created & Synced Playlists
        val combinedPlaylists = (userPlaylists + (apiLibrary?.playlists.orEmpty())).distinctBy { it.id }
        combinedPlaylists.forEach { pl ->
            if (pl.id != "pl_liked") {
                list.add(
                    LibraryItemModel(
                        id = pl.id,
                        title = pl.title,
                        subtitle = pl.subtitle.ifBlank { "Playlist • ${pl.creatorName.ifBlank { userDisplayName }} • ${pl.trackCount} songs" },
                        imageUrl = pl.coverUrl,
                        isPinned = pl.isPinned,
                        isUserCreated = pl.isUserCreated,
                        type = "playlist"
                    )
                )
            }
        }

        // 3. Saved Albums (From API)
        apiLibrary?.savedAlbums.orEmpty().forEach { alb ->
            list.add(
                LibraryItemModel(
                    id = alb.id,
                    title = alb.title,
                    subtitle = "Album • ${alb.artist}",
                    imageUrl = alb.coverUrl,
                    type = "album"
                )
            )
        }

        // 4. Followed Artists (From API)
        apiLibrary?.followedArtists.orEmpty().forEach { art ->
            list.add(
                LibraryItemModel(
                    id = "art_${art.name}",
                    title = art.name,
                    subtitle = "Artist • ${if (art.followersCount > 0) "${art.followersCount / 1000}K Listeners" else "Followed"}",
                    imageUrl = art.imageUrl,
                    type = "artist"
                )
            )
        }

        // 5. Downloaded Podcast Episodes
        downloadedEpisodes.forEach { ep ->
            list.add(
                LibraryItemModel(
                    id = ep.id,
                    title = ep.title,
                    subtitle = "Downloaded • ${ep.podcastTitle} • ${ep.durationLabel}",
                    imageUrl = ep.coverUrl,
                    type = "downloaded_podcast"
                )
            )
        }

        list
    }

    // Filter & Search & Sort Logic
    val filteredItems = remember(allItems, selectedFilter, searchQuery, sortOrder) {
        var result = when (selectedFilter) {
            "Playlists" -> allItems.filter { it.type == "playlist" || it.isLikedSongs }
            "Podcasts" -> allItems.filter { it.type == "podcast" || it.type == "downloaded_podcast" }
            "Downloaded" -> allItems.filter { it.type == "downloaded_podcast" }
            "Albums" -> allItems.filter { it.type == "album" }
            "Artists" -> allItems.filter { it.type == "artist" }
            else -> allItems
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q)
            }
        }

        // Apply Sorting
        val pinned = result.filter { it.isPinned }
        val unpinned = result.filter { !it.isPinned }

        val sortedUnpinned = when (sortOrder) {
            "Alphabetical" -> unpinned.sortedBy { it.title.lowercase() }
            "Creator" -> unpinned.sortedBy { it.subtitle.lowercase() }
            "Recently Added" -> unpinned.reversed()
            else -> unpinned // "Recents"
        }

        pinned + sortedUnpinned
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

            // Top Header: User Avatar + "Your Library" + Search + Add Playlist
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
                            .size(36.dp)
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
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (isSearchExpanded) SpotifyGreen else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Playlist",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Expanding Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Find in Your Library", color = Color(0xFFA19BAE), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFA19BAE), modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF242424),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Filter Chips: All, Playlists, Podcasts, Downloaded, Albums, Artists
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
                            .clickable {
                                viewModel.setFilter(if (isSelected && filter != "All") "All" else filter)
                            }
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

            // Sort & Grid Toggle Bar: "⇅ [Sort]" + Grid Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSortMenu = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Sort",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sortOrder,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color(0xFF282828))
                    ) {
                        listOf("Recents", "Recently Added", "Alphabetical", "Creator").forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = if (sortOrder == option) SpotifyGreen else Color.White,
                                        fontWeight = if (sortOrder == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setSortOrder(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleGridView() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isGridView) Icons.Default.FormatListBulleted else Icons.Outlined.GridView,
                        contentDescription = "Toggle View",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content: Empty State OR List View OR Grid View
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = Color(0xFF555555),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matches found in your library" else "Your library is empty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching for a different keyword" else "Create your first playlist and start adding songs!",
                            fontSize = 13.sp,
                            color = Color(0xFFA19BAE)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Create Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (isGridView) {
                // Grid View (2-Column)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredItems) { item ->
                        LibraryGridItem(
                            item = item,
                            onClick = { handleItemClick(item, downloadedEpisodes, onOpenAlbum, onOpenArtist, onOpenPodcast, onPlayDownloadedEpisode, onOpenPlaylist) }
                        )
                    }
                }
            } else {
                // List View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        LibraryRowItem(
                            item = item,
                            onClick = { handleItemClick(item, downloadedEpisodes, onOpenAlbum, onOpenArtist, onOpenPodcast, onPlayDownloadedEpisode, onOpenPlaylist) },
                            onPinToggle = { viewModel.togglePin(context, item.id) },
                            onEdit = {
                                val pl = userPlaylists.find { it.id == item.id }
                                if (pl != null) playlistToEdit = pl
                            },
                            onDelete = {
                                val pl = userPlaylists.find { it.id == item.id }
                                if (pl != null) playlistToDelete = pl
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Playlist Dialog (Spotify Style)
    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }
        var selectedCoverPreset by remember { mutableStateOf(0) }

        val coverPresets = listOf(
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500"
        )

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF242424),
            title = {
                Text(
                    text = "Give your playlist a name",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("My playlist #${userPlaylists.size + 1}", color = Color(0xFFA19BAE)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        placeholder = { Text("Add an optional description", color = Color(0xFFA19BAE)) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Choose a cover style:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        coverPresets.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (selectedCoverPreset == index) 2.dp else 0.dp,
                                        color = if (selectedCoverPreset == index) SpotifyGreen else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCoverPreset = index }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = playlistName.trim().ifBlank { "My Playlist #${userPlaylists.size + 1}" }
                        val cover = coverPresets.getOrElse(selectedCoverPreset) { coverPresets[0] }
                        viewModel.createPlaylist(
                            context = context,
                            title = title,
                            description = playlistDesc.trim(),
                            coverUrl = cover
                        ) { newPl ->
                            onOpenPlaylist(newPl.id)
                        }
                        showCreateDialog = false
                        Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Edit Playlist Dialog
    playlistToEdit?.let { pl ->
        var editTitle by remember { mutableStateOf(pl.title) }
        var editDesc by remember { mutableStateOf(pl.subtitle) }

        AlertDialog(
            onDismissRequest = { playlistToEdit = null },
            containerColor = Color(0xFF242424),
            title = { Text("Edit playlist details", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Playlist Name", color = Color(0xFFA19BAE)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description", color = Color(0xFFA19BAE)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        UserPlaylistStore.updatePlaylist(context, pl.id, title = editTitle.trim(), description = editDesc.trim())
                        playlistToEdit = null
                        Toast.makeText(context, "Playlist updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToEdit = null }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Delete Playlist Confirmation Dialog
    playlistToDelete?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = Color(0xFF242424),
            title = { Text("Delete playlist?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${pl.title}\"? This action cannot be undone.", color = Color(0xFFA19BAE)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlaylist(context, pl.id)
                        playlistToDelete = null
                        Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }
}

private fun handleItemClick(
    item: LibraryItemModel,
    downloadedEpisodes: List<com.sonexa.app.data.local.DownloadedEpisode>,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenPodcast: (String) -> Unit,
    onPlayDownloadedEpisode: (com.sonexa.app.data.local.DownloadedEpisode) -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    when (item.type) {
        "album" -> onOpenAlbum(item.id)
        "artist" -> onOpenArtist(item.title)
        "podcast" -> onOpenPodcast(item.id)
        "downloaded_podcast" -> {
            val found = downloadedEpisodes.find { it.id == item.id }
            if (found != null) onPlayDownloadedEpisode(found) else onOpenPodcast(item.id)
        }
        else -> onOpenPlaylist(item.id)
    }
}

@Composable
private fun LibraryRowItem(
    item: LibraryItemModel,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or Gradient Box
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
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
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

        // 3-Dots Menu for User-Created Playlists
        if (item.isUserCreated) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFFA19BAE), modifier = Modifier.size(20.dp))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF282828))
                ) {
                    DropdownMenuItem(
                        text = { Text(if (item.isPinned) "Unpin from top" else "Pin to top", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = SpotifyGreen) },
                        onClick = {
                            onPinToggle()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit playlist details", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete playlist", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryGridItem(
    item: LibraryItemModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        if (item.isLikedSongs) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF450AF5), Color(0xFF8E8EE5), Color(0xFFC4B5FD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Liked", tint = Color.White, modifier = Modifier.size(48.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(if (item.type == "artist") CircleShape else RoundedCornerShape(8.dp))
                    .background(Color(0xFF282828))
            ) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.isPinned) {
                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = SpotifyGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                color = Color(0xFFA19BAE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
