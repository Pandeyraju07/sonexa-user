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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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

private val SpotifyGreen = Color(0xFF1ED760)

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
    var addAccountEmail by remember { mutableStateOf("") }
    var addAccountPassword by remember { mutableStateOf("") }
    var isAddingAccount by remember { mutableStateOf(false) }
    var isLoginLoading by remember { mutableStateOf(false) }
    val authRepo = remember { com.sonexa.app.data.repository.AuthRepository.create(context) }
    val coroutineScope = rememberCoroutineScope()

    val isFavorite = playbackState.track?.isLiked == true

    androidx.activity.compose.BackHandler(enabled = showProfileDrawer || showAccountDialog || selectedFeedCategory != "All" || selectedNavTab != "Home") {
        when {
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

    val quickGridItems = listOf(
        QuickCardItem("quick_liked", "Liked Songs", "", isLiked = true),
        QuickCardItem("quick_peace", "Peace 🖤", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg"),
        QuickCardItem("quick_10s", "<10s", "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg"),
        QuickCardItem("quick_bolly", "Bollywood spicy 🔥", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg"),
        QuickCardItem("quick_workout", "WORKOUT PLAYLIST 2026", "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg"),
        QuickCardItem("quick_holly", "Hollywood ✨", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg"),
        QuickCardItem("quick_metro", "Metro In Dino - All Songs", "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg"),
        QuickCardItem("quick_vaapas", "Main Vaapas Aaunga (Origi...", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg")
    )

    val radioStations = listOf(
        RadioStationItem(
            title = "Sajjan Raazi",
            artists = "Satinder Sartaaj, Harrdy Sandhu, Garry Sandhu...",
            color = Color(0xFF90CAF9),
            artistImages = listOf(
                "https://c.saavncdn.com/artists/Satinder_Sartaaj_500x500.jpg",
                "https://c.saavncdn.com/artists/Harrdy_Sandhu_500x500.jpg",
                "https://c.saavncdn.com/artists/Garry_Sandhu_500x500.jpg"
            ),
            query = "Sajjan Raazi Satinder Sartaaj"
        ),
        RadioStationItem(
            title = "The PropheC",
            artists = "Sidhu Moose Wala, AP Dhillon, Chani Nattan, I...",
            color = Color(0xFFA7F3D0),
            artistImages = listOf(
                "https://c.saavncdn.com/artists/The_PropheC_500x500.jpg",
                "https://c.saavncdn.com/artists/Sidhu_Moose_Wala_500x500.jpg",
                "https://c.saavncdn.com/artists/AP_Dhillon_500x500.jpg"
            ),
            query = "The PropheC Sidhu Moose Wala"
        ),
        RadioStationItem(
            title = "Arijit Singh",
            artists = "Pritam, Jasleen Royal, Vishal Mishra, Atif Aslam...",
            color = Color(0xFFFFCC80),
            artistImages = listOf(
                "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg",
                "https://c.saavncdn.com/artists/Pritam_500x500.jpg",
                "https://c.saavncdn.com/artists/Atif_Aslam_500x500.jpg"
            ),
            query = "Arijit Singh"
        )
    )

    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val apiDisplayName = feed?.userDisplayName?.takeIf { it.isNotBlank() && it != "Music Lover" }
    val userDisplayName = remember(apiDisplayName, sessionManager.userName, sessionManager.userEmail) {
        apiDisplayName
            ?: sessionManager.userName?.takeIf { it.isNotBlank() }
            ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Listener"
    }
    val avatarInitial = remember(userDisplayName) {
        userDisplayName.firstOrNull()?.uppercase() ?: "U"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (selectedNavTab) {
            "Home" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 125.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top App Bar with User Avatar & Filter Chips (Screenshot 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC4B5FD))
                                .clickable { showProfileDrawer = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitial,
                                color = Color(0xFF1B1629),
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
                                        .background(if (isSelected) SpotifyGreen else Color(0xFF282828))
                                        .clickable {
                                            selectedFeedCategory = cat
                                            when (cat) {
                                                "Podcasts" -> onOpenPodcasts()
                                                "Live Events" -> onOpenLiveEvents()
                                                "I-Pop" -> onOpenIPop()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Top 8-Card 2-Column Grid (Screenshot 1)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickGridItems.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAccessCard(
                                    item = pair[0],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (pair[0].isLiked) {
                                            onOpenPlaylist("pl_liked")
                                        } else {
                                            val match = allTrending.firstOrNull() ?: allTrending.getOrNull(0)
                                            playTrack(match, allTrending, pair[0].title)
                                        }
                                    }
                                )
                                if (pair.size > 1) {
                                    QuickAccessCard(
                                        item = pair[1],
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val match = allTrending.getOrNull(1) ?: allTrending.firstOrNull()
                                            playTrack(match, allTrending, pair[1].title)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section: Recommended Stations (Screenshot 1)
                    Text(
                        text = "Recommended Stations",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(radioStations) { station ->
                            RadioStationCard(station) {
                                onOpenArtist(station.query)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section: Jump back in (Screenshot 1)
                    Text(
                        text = "Jump back in",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(allTrending.take(8)) { idx, track ->
                            MediaSquareCard(
                                title = track.title,
                                subtitle = "Song • ${track.artist}",
                                imageUrl = track.effectiveCoverUrl,
                                onClick = { playTrack(track, allTrending, "Jump back in") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section: Popular Artists
                    Text(
                        text = "Popular Artists",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(allArtists) { artist ->
                            ArtistCircleCard(artist) {
                                onOpenArtist(artist.name)
                            }
                        }
                    }
                }
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

            SpotifyBottomNavigationBar(
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
                            Text(
                                text = avatarInitial,
                                color = Color(0xFF1B1629),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
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
                    DrawerMenuItem(Icons.Outlined.Settings, "Settings and privacy") {
                        showProfileDrawer = false
                        onOpenSettings()
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerMenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Log out") {
                        showProfileDrawer = false
                        onLogout()
                    }
                }
            }
        }

        // Account Switcher & Add Account Dialog
        if (showAccountDialog) {
            val savedAccounts = remember(showAccountDialog) { sessionManager.getSavedAccounts() }
            AlertDialog(
                onDismissRequest = {
                    showAccountDialog = false
                    isAddingAccount = false
                },
                containerColor = Color(0xFF1E1E1E),
                title = {
                    Text(
                        text = if (isAddingAccount) "Add New Account" else "Switch or Add Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isAddingAccount) {
                            Text(
                                text = "Signed in accounts:",
                                color = Color(0xFFA19BAE),
                                fontSize = 13.sp
                            )

                            savedAccounts.forEach { acc ->
                                val isCurrent = acc.userId == sessionManager.userId || acc.email.equals(sessionManager.userEmail, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) Color(0xFF282828) else Color.Transparent)
                                        .clickable {
                                            if (!isCurrent) {
                                                sessionManager.switchAccount(acc.userId)
                                                showAccountDialog = false
                                                Toast.makeText(context, "Switched to ${acc.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFC4B5FD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = acc.name.firstOrNull()?.uppercase() ?: "U",
                                            color = Color(0xFF1B1629),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = acc.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = acc.email,
                                            color = Color(0xFFA19BAE),
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { isAddingAccount = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add another account")
                            }
                        } else {
                            // Login Form for Adding Account
                            OutlinedTextField(
                                value = addAccountEmail,
                                onValueChange = { addAccountEmail = it },
                                label = { Text("Email address", color = Color(0xFFA19BAE)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = addAccountPassword,
                                onValueChange = { addAccountPassword = it },
                                label = { Text("Password", color = Color(0xFFA19BAE)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (isLoginLoading) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = SpotifyGreen, modifier = Modifier.size(28.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (addAccountEmail.isBlank() || addAccountPassword.isBlank()) {
                                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isLoginLoading = true
                                        coroutineScope.launch {
                                            val result = authRepo.login(addAccountEmail, addAccountPassword)
                                            isLoginLoading = false
                                            result.fold(
                                                onSuccess = { resp ->
                                                    showAccountDialog = false
                                                    isAddingAccount = false
                                                    addAccountEmail = ""
                                                    addAccountPassword = ""
                                                    Toast.makeText(context, "Successfully signed in as ${resp.resolvedUser?.name ?: "User"}", Toast.LENGTH_LONG).show()
                                                },
                                                onFailure = { err ->
                                                    Toast.makeText(context, err.message ?: "Authentication failed", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Sign In & Connect Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (isAddingAccount) isAddingAccount = false
                        else showAccountDialog = false
                    }) {
                        Text(if (isAddingAccount) "Back" else "Close", color = SpotifyGreen)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
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
private fun MediaSquareCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(145.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF282828))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFFA19BAE),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArtistCircleCard(
    artist: ArtistDto,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF282828))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(artist.imageUrl).crossfade(true).build(),
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
private fun SpotifyBottomNavigationBar(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF121212).copy(alpha = 0.96f),
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
                        tint = if (isSelected) Color.White else Color(0xFFA19BAE),
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
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Cast",
                    tint = SpotifyGreen,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(onClick = onToggleLike, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) SpotifyGreen else Color.White,
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
