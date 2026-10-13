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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.sonexa.app.ui.theme.AppleMaterials
import com.sonexa.app.ui.theme.AppleType
import com.sonexa.app.ui.theme.appleBounce
import com.sonexa.app.ui.viewmodel.LibraryViewModel

private val PurplePrimary = Color(0xFFA855F7)
private val PurpleDeep = Color(0xFF7C3AED)

data class LibraryItemModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val isLikedSongs: Boolean = false,
    val isPinned: Boolean = false,
    val isUserCreated: Boolean = false,
    val trackCount: Int = 0,
    val type: String = "playlist" // "playlist", "album", "artist", "podcast", "downloaded_podcast"
)

private data class RecentPlaylistCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val gradientColors: List<Color>
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
    onOpenAudiobook: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val currentUserName by sessionManager.userNameFlow.collectAsState()
    val currentUserAvatar by sessionManager.userAvatarFlow.collectAsState()
    val userDisplayName = currentUserName.takeIf { it.isNotBlank() }
        ?: sessionManager.userName?.takeIf { it.isNotBlank() }
        ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Pandeyraju071"
    val avatarInitial = userDisplayName.firstOrNull()?.uppercase() ?: "P"

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

    var playlistToEdit by remember { mutableStateOf<PlaylistDto?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistDto?>(null) }

    val userPlaylists by UserPlaylistStore.playlists.collectAsState()
    val likedSongs by LikedSongsStore.likedSongs.collectAsState()
    val likedCount = likedSongs.size
    val downloadedEpisodes by PodcastDownloadManager.downloadedEpisodes.collectAsState()
    val savedAudiobookIds by com.sonexa.app.data.local.AudiobookStore.savedAudiobookIds.collectAsState()
    val audiobookProgressMap by com.sonexa.app.data.local.AudiobookStore.progressMap.collectAsState()
    val libraryUiState by viewModel.uiState.collectAsState()
    val apiLibrary = (libraryUiState as? com.sonexa.app.ui.viewmodel.CatalogUiState.Ready)?.data

    // Dynamic library items derived from User & API
    val allItems = remember(userPlaylists, likedCount, downloadedEpisodes, userDisplayName, apiLibrary) {
        val list = mutableListOf<LibraryItemModel>()

        // 1. Liked Songs (Always on top / pinned)
        list.add(
            LibraryItemModel(
                id = "pl_liked",
                title = "Liked Songs",
                subtitle = "Playlist • $userDisplayName",
                imageUrl = "",
                isLikedSongs = true,
                isPinned = true,
                trackCount = likedCount,
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
                        subtitle = pl.subtitle.ifBlank { "Playlist • ${pl.creatorName.ifBlank { "You" }}" },
                        imageUrl = pl.coverUrl,
                        isPinned = pl.isPinned,
                        isUserCreated = pl.isUserCreated,
                        trackCount = pl.trackCount,
                        type = "playlist"
                    )
                )
            }
        }

        // 3. Saved Albums
        apiLibrary?.savedAlbums.orEmpty().forEach { alb ->
            list.add(
                LibraryItemModel(
                    id = alb.id,
                    title = alb.title,
                    subtitle = "Album • ${alb.artist}",
                    imageUrl = alb.coverUrl,
                    trackCount = alb.trackCount,
                    type = "album"
                )
            )
        }

        // 4. Followed Artists
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

        // 5. Downloaded Episodes
        downloadedEpisodes.forEach { ep ->
            list.add(
                LibraryItemModel(
                    id = ep.id,
                    title = ep.title,
                    subtitle = "Downloaded • ${ep.podcastTitle}",
                    imageUrl = ep.coverUrl,
                    type = "downloaded_podcast"
                )
            )
        }

        // 6. Saved & Continued Audiobooks
        val relevantAudiobookIds = (savedAudiobookIds + audiobookProgressMap.keys).distinct()
        val allAudiobooks = com.sonexa.app.data.repository.AudiobookRepository.bundledAudiobooks()
        allAudiobooks.filter { it.id in relevantAudiobookIds }.forEach { book ->
            val progress = audiobookProgressMap[book.id]
            val subtitleText = if (progress != null && progress.completionPercentage > 0) {
                "Audiobook • ${progress.completionPercentage}% listened"
            } else {
                "Audiobook • ${book.author}"
            }
            list.add(
                LibraryItemModel(
                    id = book.id,
                    title = book.title,
                    subtitle = subtitleText,
                    imageUrl = book.coverUrl,
                    trackCount = book.chapterCount,
                    type = "audiobook"
                )
            )
        }

        list
    }

    // Filter, Search, Sort Logic
    val filteredItems = remember(allItems, selectedFilter, searchQuery, sortOrder) {
        var result = when (selectedFilter) {
            "Playlists" -> allItems.filter { it.type == "playlist" || it.isLikedSongs }
            "Audiobooks" -> allItems.filter { it.type == "audiobook" }
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

        val pinned = result.filter { it.isPinned }
        val unpinned = result.filter { !it.isPinned }

        val sortedUnpinned = when (sortOrder) {
            "Alphabetical" -> unpinned.sortedBy { it.title.lowercase() }
            "Creator" -> unpinned.sortedBy { it.subtitle.lowercase() }
            "Recently Added" -> unpinned.reversed()
            else -> unpinned
        }

        pinned + sortedUnpinned
    }

    // Curated "Recently Played" horizontal cards matching Screenshot 1
    val recentCards = remember {
        listOf(
            RecentPlaylistCardItem(
                id = "rp_chill",
                title = "Chill Vibes",
                subtitle = "Playlist",
                imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500",
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFF4C0519))
            ),
            RecentPlaylistCardItem(
                id = "rp_bollywood",
                title = "Bollywood Hits",
                subtitle = "Playlist",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF083344))
            ),
            RecentPlaylistCardItem(
                id = "rp_lofi",
                title = "Lo-Fi Beats",
                subtitle = "Playlist",
                imageUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
                gradientColors = listOf(Color(0xFFD97706), Color(0xFF451A03))
            ),
            RecentPlaylistCardItem(
                id = "rp_workout",
                title = "Workout Mix",
                subtitle = "Playlist",
                imageUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500",
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF2E1065))
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B18))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ── 1. Top Header: Avatar + "Your Library" + Search + Add Playlist ─────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .background(Color(0xFF2E1C4D))
                                .clickable { onOpenProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUserAvatar.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentUserAvatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = avatarInitial,
                                    style = AppleType.headlineBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Your Library",
                                style = AppleType.title1Bold,
                                color = Color.White
                            )
                            Text(
                                text = "All your music in one place",
                                style = AppleType.caption1,
                                color = Color(0xFFA19BAE)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search Circular Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) viewModel.setSearchQuery("")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = if (isSearchExpanded) PurplePrimary else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Add Playlist Circular Button with Purple Tint
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PurpleDeep.copy(alpha = 0.4f))
                                .border(1.dp, PurplePrimary.copy(alpha = 0.5f), CircleShape)
                                .clickable { showCreateDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create Playlist",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Expanding Search Input Field
            if (isSearchExpanded) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Find in Your Library", color = Color(0xFFA19BAE), style = AppleType.subheadline) },
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
                            focusedContainerColor = Color(0xFF1E1430),
                            unfocusedContainerColor = Color(0xFF140F22),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // ── 2. Filter Chips Row: All, Playlists, Podcasts, Downloaded ─────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                ) {
                    val filterChips = listOf(
                        FilterChipData("All", null),
                        FilterChipData("Playlists", Icons.AutoMirrored.Filled.QueueMusic),
                        FilterChipData("Audiobooks", Icons.Default.AutoStories),
                        FilterChipData("Podcasts", Icons.Default.Mic),
                        FilterChipData("Downloaded", Icons.Default.Download)
                    )

                    items(filterChips) { chip ->
                        val isSelected = selectedFilter == chip.label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0xFF1F1833), Color(0xFF171226)))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    viewModel.setFilter(if (isSelected && chip.label != "All") "All" else chip.label)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (chip.icon != null) {
                                    Icon(
                                        imageVector = chip.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color(0xFFC4B5FD),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = chip.label,
                                    style = AppleType.subheadlineBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. 2x2 Highlights Grid (Screenshot 1: Liked, Downloaded, Recent, Playlists) ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Liked Songs & Downloaded
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryHighlightCard(
                            icon = Icons.Default.Favorite,
                            title = "Liked Songs",
                            subtitle = if (likedCount > 0) "$likedCount songs" else "0 songs",
                            gradientColors = listOf(Color(0xFFE11D48), Color(0xFF9F1239)),
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenPlaylist("pl_liked") }
                        )

                        LibraryHighlightCard(
                            icon = Icons.Default.Download,
                            title = "Downloaded",
                            subtitle = "${downloadedEpisodes.size} songs",
                            gradientColors = listOf(Color(0xFF059669), Color(0xFF064E3B)),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenDownloads
                        )
                    }

                    // Row 2: Recently Played & Your Playlists
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryHighlightCard(
                            icon = Icons.Default.Schedule,
                            title = "Recently Played",
                            subtitle = "32 items",
                            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setFilter("All") }
                        )

                        LibraryHighlightCard(
                            icon = Icons.Default.Star,
                            title = "Your Playlists",
                            subtitle = "${userPlaylists.size + (apiLibrary?.playlists?.size ?: 0)} playlists",
                            gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F)),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setFilter("Playlists") }
                        )
                    }
                }
            }

            // ── 4. Recently Played Horizontal Section (Screenshot 1) ─────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Played",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See All >",
                        style = AppleType.subheadlineBold,
                        color = Color(0xFFC4B5FD),
                        modifier = Modifier.clickable { viewModel.setFilter("All") }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(recentCards) { card ->
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .appleBounce { onOpenPlaylist(card.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.verticalGradient(card.gradientColors))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(card.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = card.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Circular Play Button Overlaid
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { onOpenPlaylist(card.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = card.title,
                                style = AppleType.subheadlineBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = card.subtitle,
                                style = AppleType.caption1,
                                color = Color(0xFFA19BAE)
                            )
                        }
                    }
                }
            }

            // ── 5. "Your Playlists" Section Header + Sort + View Mode ─────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Playlists",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Sort",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sortOrder,
                                    style = AppleType.caption1Bold,
                                    color = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color(0xFF241C35))
                            ) {
                                listOf("Recents", "Recently Added", "Alphabetical", "Creator").forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = if (sortOrder == option) PurplePrimary else Color.White,
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

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.toggleGridView() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isGridView) Icons.Default.FormatListBulleted else Icons.Outlined.GridView,
                                contentDescription = "Toggle View",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── 6. Playlists Content List / Grid (Screenshot 1) ─────
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = Color(0xFF6B5E87),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matches found" else "Your library is empty",
                                style = AppleType.headlineBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Create Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredItems) { item ->
                    LibraryRowItem(
                        item = item,
                        onClick = {
                            handleItemClick(
                                item,
                                downloadedEpisodes,
                                onOpenAlbum,
                                onOpenArtist,
                                onOpenPodcast,
                                onPlayDownloadedEpisode,
                                onOpenPlaylist,
                                onOpenAudiobook
                            )
                        },
                        onPlayClick = {
                            handleItemClick(
                                item,
                                downloadedEpisodes,
                                onOpenAlbum,
                                onOpenArtist,
                                onOpenPodcast,
                                onPlayDownloadedEpisode,
                                onOpenPlaylist,
                                onOpenAudiobook
                            )
                        },
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

    // ── Dialogs: Create, Edit, Delete Playlist ─────
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
            containerColor = Color(0xFF211833),
            title = {
                Text(
                    text = "Give your playlist a name",
                    color = Color.White,
                    style = AppleType.title3Bold
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
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0xFF443860)
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
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0xFF443860)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Choose a cover style:",
                        color = Color.White,
                        style = AppleType.caption1Bold
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
                                        color = if (selectedCoverPreset == index) PurplePrimary else Color.Transparent,
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
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
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
            containerColor = Color(0xFF211833),
            title = { Text("Edit playlist details", color = Color.White, style = AppleType.title3Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Playlist Name", color = Color(0xFFA19BAE)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0xFF443860)
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
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0xFF443860)
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
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToEdit = null }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Delete Playlist Dialog
    playlistToDelete?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = Color(0xFF211833),
            title = { Text("Delete playlist?", color = Color.White, style = AppleType.title3Bold) },
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

private data class FilterChipData(val label: String, val icon: ImageVector?)

@Composable
private fun LibraryHighlightCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(16.dp))
            .appleBounce { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppleType.subheadlineBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppleType.caption1,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LibraryRowItem(
    item: LibraryItemModel,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail: Liked Songs purple-blue gradient or playlist artwork
        if (item.isLikedSongs) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Liked Songs",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(if (item.type == "artist") CircleShape else RoundedCornerShape(12.dp))
                    .background(Color(0xFF231B36))
            ) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = AppleType.headlineBold,
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
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = item.subtitle,
                    style = AppleType.caption1,
                    color = Color(0xFFA19BAE),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.trackCount > 0) {
                Text(
                    text = "${item.trackCount} songs",
                    style = AppleType.caption2,
                    color = Color(0xFF8B7E9F)
                )
            }
        }

        // Play Circular Button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { onPlayClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 3-Dots Menu
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFFA19BAE),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color(0xFF241C35))
            ) {
                DropdownMenuItem(
                    text = { Text(if (item.isPinned) "Unpin from top" else "Pin to top", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFF10B981)) },
                    onClick = {
                        onPinToggle()
                        showMenu = false
                    }
                )
                if (item.isUserCreated) {
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

private fun handleItemClick(
    item: LibraryItemModel,
    downloadedEpisodes: List<com.sonexa.app.data.local.DownloadedEpisode>,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenPodcast: (String) -> Unit,
    onPlayDownloadedEpisode: (com.sonexa.app.data.local.DownloadedEpisode) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onOpenAudiobook: (String) -> Unit = {}
) {
    when (item.type) {
        "album" -> onOpenAlbum(item.id)
        "artist" -> onOpenArtist(item.title)
        "podcast" -> onOpenPodcast(item.id)
        "audiobook" -> onOpenAudiobook(item.id)
        "downloaded_podcast" -> {
            val found = downloadedEpisodes.find { it.id == item.id }
            if (found != null) onPlayDownloadedEpisode(found) else onOpenPodcast(item.id)
        }
        else -> onOpenPlaylist(item.id)
    }
}
