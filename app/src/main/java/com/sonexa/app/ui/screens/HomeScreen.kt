package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.HomeUiState
import com.sonexa.app.ui.viewmodel.HomeViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

data class QuickCardItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val isLiked: Boolean = false
)

data class RadioStationItem(
    val title: String,
    val artists: String,
    val color: Color,
    val artistImages: List<String>,
    val query: String
)

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onOpenFullPlayer: () -> Unit = {},
    onOpenAlbum: (String) -> Unit = {},
    onOpenPlaylist: (String) -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    onOpenAiSignature: () -> Unit = {},
    onOpenPodcasts: () -> Unit = {},
    onOpenLiveEvents: () -> Unit = {},
    onOpenIPop: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenMusicDna: () -> Unit = {},
    onOpenMusicJourney: () -> Unit = {},
    onOpenMusicIntelligence: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeState by homeViewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    var selectedNavTab by remember { mutableStateOf("Home") }
    var selectedFeedCategory by remember { mutableStateOf("All") }
    var showProfileDrawer by remember { mutableStateOf(false) }

    var showAccountDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var addAccountEmail by remember { mutableStateOf("") }
    var addAccountPassword by remember { mutableStateOf("") }
    var isAddingAccount by remember { mutableStateOf(false) }
    var isLoginLoading by remember { mutableStateOf(false) }
    val authRepo = remember { com.sonexa.app.data.repository.AuthRepository.create(context) }
    val coroutineScope = rememberCoroutineScope()

    val likedSongsList by com.sonexa.app.data.local.LikedSongsStore.likedSongs.collectAsState()
    val isFavorite = playbackState.track != null && (playbackState.track?.isLiked == true || likedSongsList.any { it.id == playbackState.track?.id })

    androidx.activity.compose.BackHandler(enabled = showProfileDrawer || showAccountDialog || showLogoutConfirmDialog || selectedFeedCategory != "All" || selectedNavTab != "Home") {
        when {
            showLogoutConfirmDialog -> showLogoutConfirmDialog = false
            showProfileDrawer -> showProfileDrawer = false
            showAccountDialog -> showAccountDialog = false
            selectedFeedCategory != "All" -> selectedFeedCategory = "All"
            selectedNavTab != "Home" -> selectedNavTab = "Home"
        }
    }

    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    fun playTrack(track: TrackDto?, queue: List<TrackDto> = emptyList(), sourceTitle: String = "") {
        if (track == null) {
            Toast.makeText(context, "Track unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val safeAlbum = track.album.orEmpty()
        val safeSource = sourceTitle.ifBlank { safeAlbum }
        if (queue.size > 1) {
            val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            playbackViewModel.playQueue(queue, index, safeSource)
        } else {
            playbackViewModel.play(track, safeSource)
        }
        onOpenFullPlayer()
    }

    val feed = homeState as? HomeUiState.Success
    val allTrending = feed?.trendingNow.orEmpty()
    val allAlbums = feed?.popularAlbums.orEmpty()
    val allPlaylists = feed?.madeForYou.orEmpty()
    val allArtists = feed?.recommendedArtists.orEmpty()
    val continueListening = feed?.continueListening.orEmpty()

    // 100% Dynamic Quick Access Grid derived directly from API feed + Liked Songs
    val quickAccessItems: List<QuickCardItem> = remember(allTrending, continueListening, likedSongsList) {
        val list = mutableListOf<QuickCardItem>()
        // 1. Liked Songs
        list.add(QuickCardItem("quick_liked", "Liked Songs", "", isLiked = true))
        
        // 2. Top API tracks / trending entries
        val pool = (continueListening + allTrending).distinctBy { it.id }
        pool.take(7).forEachIndexed { idx, track ->
            list.add(
                QuickCardItem(
                    id = track.id,
                    title = track.title,
                    imageUrl = track.effectiveCoverUrl,
                    isLiked = false
                )
            )
        }
        list
    }

    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val currentUserName by sessionManager.userNameFlow.collectAsState()
    val currentUserAvatar by sessionManager.userAvatarFlow.collectAsState()
    val apiDisplayName = feed?.userDisplayName?.takeIf { it.isNotBlank() && it != "Music Lover" }
    val userDisplayName = currentUserName.takeIf { it.isNotBlank() }
        ?: apiDisplayName
        ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Listener"
    val avatarInitial = userDisplayName.firstOrNull()?.uppercase() ?: "U"
    val avatarPhotoUrl = currentUserAvatar.takeIf { it.isNotBlank() } ?: sessionManager.profilePicUrl.orEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B18))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (selectedNavTab) {
            "Home" -> {
                HomeFeedContent(
                    avatarInitial = avatarInitial,
                    avatarUrl = avatarPhotoUrl,
                    selectedFeedCategory = selectedFeedCategory,
                    onSelectFeedCategory = { cat ->
                        selectedFeedCategory = cat
                        when (cat) {
                            "Podcasts" -> onOpenPodcasts()
                            "Live Events" -> onOpenLiveEvents()
                            "I-Pop" -> onOpenIPop()
                        }
                    },
                    onOpenProfileDrawer = { showProfileDrawer = true },
                    onOpenMusicDna = onOpenMusicDna,
                    onOpenMusicJourney = onOpenMusicJourney,
                    onOpenMusicIntelligence = onOpenMusicIntelligence,
                    quickAccessItems = quickAccessItems,
                    continueListening = continueListening,
                    allTrending = allTrending,
                    allAlbums = allAlbums,
                    allArtists = allArtists,
                    playbackState = playbackState,
                    onPlayTrack = { track, queue, src -> playTrack(track, queue, src) },
                    onOpenLikedPlaylist = { onOpenPlaylist("pl_liked") },
                    onOpenAlbum = onOpenAlbum,
                    onOpenArtist = onOpenArtist,
                    onTogglePlayPause = { playbackViewModel.togglePlayPause() }
                )
            }

            "Search" -> SearchScreen(
                playbackViewModel = playbackViewModel,
                onOpenFullPlayer = onOpenFullPlayer,
                onOpenArtistProfile = onOpenArtist,
                onOpenPlaylistDetail = onOpenPlaylist,
                onOpenPodcasts = onOpenPodcasts,
                onOpenLiveEvents = onOpenLiveEvents,
                onOpenIPop = onOpenIPop,
                onOpenProfile = { showProfileDrawer = true }
            )

            "Library", "Your Library" -> LibraryScreen(
                onOpenDownloads = { onOpenSettings() },
                onOpenPlaylist = onOpenPlaylist,
                onOpenAlbum = onOpenAlbum,
                onOpenArtist = onOpenArtist,
                onOpenPodcast = { onOpenPodcasts() },
                onPlayDownloadedEpisode = { downloaded ->
                    playbackViewModel.play(
                        com.sonexa.app.data.local.PodcastDownloadManager.toTrack(downloaded),
                        downloaded.podcastTitle
                    )
                    onOpenFullPlayer()
                },
                onOpenProfile = { showProfileDrawer = true }
            )

            "Premium" -> PremiumScreen(onNavigateBack = { selectedNavTab = "Home" })

            "Create" -> CreateHubScreen(
                onOpenPlaylist = onOpenPlaylist,
                onOpenFullPlayer = onOpenFullPlayer,
                playbackViewModel = playbackViewModel
            )
        }

        // Sticky Footer: Mini Player & Spotify 5-Tab Bar
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HomeStickyMiniPlayer(
                track = playbackState.track,
                isPlaying = playbackState.isPlaying,
                isFavorite = isFavorite,
                onOpenFullPlayer = onOpenFullPlayer,
                onToggleLike = { playbackViewModel.toggleLike() },
                onTogglePlayPause = {
                    if (playbackState.track != null) {
                        playbackViewModel.togglePlayPause()
                    } else {
                        playTrack(allTrending.firstOrNull(), allTrending, "Home Feed")
                    }
                },
                onCastClick = {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Scanning for Cast / Bluetooth devices", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            ZyneraBottomNavigationBar(
                selectedTab = selectedNavTab,
                onSelectTab = { selectedNavTab = it }
            )
        }

        // Spotify Profile Drawer (Screenshot 4)
        if (showProfileDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showProfileDrawer = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(Color(0xFF1E1E1E))
                        .clickable(enabled = false) {}
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Profile Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showProfileDrawer = false
                                onOpenProfile()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC4B5FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarPhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarPhotoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = avatarInitial,
                                    color = Color(0xFF1B1629),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = userDisplayName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View profile",
                                color = Color(0xFFA19BAE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawer Items
                    DrawerMenuItem(Icons.Default.Add, "Add account") {
                        showProfileDrawer = false
                        showAccountDialog = true
                    }
                    DrawerMenuItem(Icons.Outlined.History, "Recents") {
                        showProfileDrawer = false
                        selectedNavTab = "Search"
                    }
                    DrawerMenuItem(Icons.Outlined.Campaign, "Your Updates") {
                        showProfileDrawer = false
                        onOpenNotifications()
                    }
                    DrawerMenuItem(Icons.Default.Psychology, "Music Intelligence Hub") {
                        showProfileDrawer = false
                        onOpenMusicIntelligence()
                    }
                    DrawerMenuItem(Icons.Outlined.Settings, "Settings and privacy") {
                        showProfileDrawer = false
                        onOpenSettings()
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerMenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Log out") {
                        showProfileDrawer = false
                        showLogoutConfirmDialog = true
                    }
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirmDialog) {
            com.sonexa.app.ui.components.LogoutConfirmationDialog(
                onConfirmLogout = {
                    showLogoutConfirmDialog = false
                    onLogout()
                },
                onDismiss = { showLogoutConfirmDialog = false }
            )
        }

        // Premium Account Switcher & Add Account Dialog
        if (showAccountDialog) {
            com.sonexa.app.ui.components.AccountSwitcherDialog(
                onDismiss = { showAccountDialog = false },
                onAccountSwitched = {
                    homeViewModel.loadHomeFeed()
                }
            )
        }
    }
}

@Composable
internal fun QuickAccessCard(
    item: QuickCardItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF282828))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.isLiked) {
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = item.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun RadioStationCard(
    station: RadioStationItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(station.color)
                .padding(10.dp)
        ) {
            // Radio Logo top right
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RADIO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }

            // Station Name
            Text(
                text = station.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = station.artists,
            fontSize = 12.sp,
            color = Color(0xFFA19BAE),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun MediaSquareCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    tag: String? = null,
    isPlayingThis: Boolean = false,
    onQuickPlay: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(155.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF282828))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-left Tag Badge (e.g. "TRENDING #1")
            if (!tag.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpotifyGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Spotify Signature Floating Green Quick-Play Action
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        if (onQuickPlay != null) onQuickPlay() else onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPlayingThis) SpotifyGreen else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFFA19BAE),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )
    }
}

@Composable
internal fun ArtistCircleCard(
    artist: ArtistDto,
    isRadioPlaying: Boolean = false,
    onQuickPlay: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFF282828))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(artist.imageUrl).crossfade(true).build(),
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Spotify Signature Floating Green Quick-Play on Artist
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        if (onQuickPlay != null) onQuickPlay() else onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRadioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Artist",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = artist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRadioPlaying) SpotifyGreen else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artist.verified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Text(
            text = if (artist.followersCount > 0) "${artist.followersCount / 1000}K Listeners" else "Artist",
            fontSize = 12.sp,
            color = Color(0xFFA19BAE)
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ZyneraBottomNavigationBar(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF100720).copy(alpha = 0.98f),
        modifier = Modifier.height(58.dp)
    ) {
        val navItems = listOf(
            Triple("Home", Icons.Default.Home, "Home"),
            Triple("Search", Icons.Default.Search, "Search"),
            Triple("Your Library", Icons.Default.VideoLibrary, "Your Library"),
            Triple("Premium", Icons.Default.Stars, "Premium"),
            Triple("Create", Icons.Default.Add, "Create")
        )

        navItems.forEach { (label, icon, _) ->
            val isSelected = selectedTab == label
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(label) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color(0xFFB062FF) else Color(0xFFA19BAE),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.5.sp,
                        color = if (isSelected) Color.White else Color(0xFFA19BAE),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun HomeStickyMiniPlayer(
    track: TrackDto?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onOpenFullPlayer: () -> Unit,
    onToggleLike: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onCastClick: () -> Unit
) {
    if (track == null) return
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2E1C44))
            .clickable { onOpenFullPlayer() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E1E))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 11.5.sp,
                    color = Color(0xFFC4B5FD),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onCastClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Default.Devices,
                    contentDescription = "Devices",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(onClick = onToggleLike, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) SonexaMagenta else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
