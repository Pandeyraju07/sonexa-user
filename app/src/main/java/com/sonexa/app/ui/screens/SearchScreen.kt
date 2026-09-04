package com.sonexa.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.BrowseCategoryItem
import com.sonexa.app.ui.viewmodel.DiscoverItem
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.RecentSearchItem
import com.sonexa.app.ui.viewmodel.SearchUiState
import com.sonexa.app.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onOpenVoiceSearch: () -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel? = null,
    onOpenFullPlayer: () -> Unit = {},
    onOpenArtistProfile: (String) -> Unit = {},
    onOpenPlaylistDetail: (String) -> Unit = {},
    onOpenPodcasts: () -> Unit = {},
    onOpenLiveEvents: () -> Unit = {},
    onOpenIPop: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }

    val searchState by searchViewModel.uiState.collectAsState()
    val recents by searchViewModel.recents.collectAsState()

    val heroCategories by searchViewModel.heroCategories.collectAsState()
    val discoverItems by searchViewModel.discoverItems.collectAsState()
    val browseAllCategories by searchViewModel.browseAllCategories.collectAsState()

    LaunchedEffect(Unit) {
        searchViewModel.init(context)
        searchViewModel.loadDynamicCategories()
    }

    androidx.activity.compose.BackHandler(enabled = isSearchActive || query.isNotBlank()) {
        if (query.isNotBlank()) {
            query = ""
            searchViewModel.onSearchQueryChanged("")
        } else {
            isSearchActive = false
            focusManager.clearFocus()
        }
    }

    fun playTrackList(tracks: List<TrackDto>, startIndex: Int, title: String) {
        if (tracks.isEmpty() || playbackViewModel == null) return
        playbackViewModel.playQueue(tracks, startIndex, title)
        onOpenFullPlayer()
    }

    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val userDisplayName = remember(sessionManager.userName, sessionManager.userEmail) {
        sessionManager.userName?.takeIf { it.isNotBlank() }
            ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Listener"
    }
    val avatarInitial = remember(userDisplayName) {
        userDisplayName.firstOrNull()?.uppercase() ?: "Y"
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

            // Header: Profile Initial, Title, Search Input Box
            if (!isSearchActive && query.isBlank()) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8C67AC))
                                .clickable { onOpenProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitial,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Search",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showVoiceSheet = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // White Search Bar Trigger Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { isSearchActive = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF121212),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "What do you want to listen to?",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF242424)
                            )
                        }

                        IconButton(
                            onClick = { showVoiceSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = Color(0xFF121212),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Active Search Input Header with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF242424))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isSearchActive = false
                            query = ""
                            searchViewModel.onSearchQueryChanged("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                searchViewModel.onSearchQueryChanged(it)
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(SpotifyGreen),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (query.isEmpty()) {
                                    Text(
                                        text = "What do you want to listen to?",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    IconButton(
                        onClick = { showVoiceSheet = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                query = ""
                                searchViewModel.onSearchQueryChanged("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (showVoiceSheet) {
                com.sonexa.app.ui.components.VoiceSearchBottomSheet(
                    onDismiss = { showVoiceSheet = false },
                    onVoiceResult = { response ->
                        query = response.transcript
                        isSearchActive = true
                        searchViewModel.onSearchQueryChanged(response.transcript)
                        if (response.tracks.isNotEmpty()) {
                            playTrackList(response.tracks, 0, "Voice: ${response.transcript}")
                        }
                    },
                    onDirectSearch = { voiceQuery ->
                        query = voiceQuery
                        isSearchActive = true
                        searchViewModel.onSearchQueryChanged(voiceQuery)
                    }
                )
            }

            // Main Content Area
            // Main Content Area
            if (!isSearchActive && query.isBlank()) {
                val quickMoodChips = remember {
                    listOf("🔥 Trending", "💖 Romance", "⚡ High Energy", "🎧 Lo-Fi Chill", "🪩 Party Hits", "🌿 Acoustic", "🧘 Alpha Focus", "🚗 Drive Mode")
                }

                // Browse Home (Quick Chips + 2x2 Hero categories + Discover + Browse All)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 0. Quick Mood & Vibe Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickMoodChips) { moodText ->
                                val cleanQuery = moodText.substringAfter(" ").trim()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SonexaInputBg)
                                        .border(1.dp, SonexaCardBorder, RoundedCornerShape(20.dp))
                                        .clickable {
                                            query = cleanQuery
                                            isSearchActive = true
                                            searchViewModel.onSearchQueryChanged(cleanQuery)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = moodText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 1. 2x2 Hero Category Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            heroCategories.chunked(2).forEach { rowPair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BrowseHeroCard(rowPair[0], Modifier.weight(1f)) { cat ->
                                        when {
                                            cat.id.contains("podcast", ignoreCase = true) || cat.title.contains("podcast", ignoreCase = true) || cat.query.contains("podcast", ignoreCase = true) -> onOpenPodcasts()
                                            cat.id.contains("event", ignoreCase = true) || cat.title.contains("event", ignoreCase = true) || cat.title.contains("concert", ignoreCase = true) -> onOpenLiveEvents()
                                            cat.id.contains("ipop", ignoreCase = true) || cat.title.contains("i-pop", ignoreCase = true) || cat.title.contains("I-Pop", ignoreCase = true) -> onOpenIPop()
                                            else -> searchViewModel.playCategoryOrTagDirect(cat.query, cat.title.replace("\n", " ")) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                        }
                                    }
                                    if (rowPair.size > 1) {
                                        BrowseHeroCard(rowPair[1], Modifier.weight(1f)) { cat ->
                                            when {
                                                cat.id.contains("podcast", ignoreCase = true) || cat.title.contains("podcast", ignoreCase = true) || cat.query.contains("podcast", ignoreCase = true) -> onOpenPodcasts()
                                                cat.id.contains("event", ignoreCase = true) || cat.title.contains("event", ignoreCase = true) || cat.title.contains("concert", ignoreCase = true) -> onOpenLiveEvents()
                                                cat.id.contains("ipop", ignoreCase = true) || cat.title.contains("i-pop", ignoreCase = true) || cat.title.contains("I-Pop", ignoreCase = true) -> onOpenIPop()
                                                else -> searchViewModel.playCategoryOrTagDirect(cat.query, cat.title.replace("\n", " ")) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // 2. "Discover something new" (Redesigned with guaranteed covers & Play action)
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Discover something new",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Curated trending tags & fresh sounds",
                                        fontSize = 12.sp,
                                        color = SonexaTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(discoverItems) { item ->
                                    Box(
                                        modifier = Modifier
                                            .width(155.dp)
                                            .height(235.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(SonexaInputBg)
                                            .border(1.dp, SonexaCardBorder, RoundedCornerShape(18.dp))
                                            .clickable {
                                                when {
                                                    item.tag.contains("podcast", ignoreCase = true) || item.query.contains("podcast", ignoreCase = true) -> onOpenPodcasts()
                                                    item.tag.contains("concert", ignoreCase = true) || item.tag.contains("event", ignoreCase = true) || item.id.contains("event", ignoreCase = true) -> onOpenLiveEvents()
                                                    item.tag.contains("i-pop", ignoreCase = true) || item.id.contains("ipop", ignoreCase = true) -> onOpenIPop()
                                                    item.track != null -> playTrackList(listOf(item.track), 0, item.tag)
                                                    else -> searchViewModel.playCategoryOrTagDirect(item.query, item.tag) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                                }
                                            }
                                    ) {
                                        // Background Image
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(item.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = item.tag,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Dark Scrim Gradient
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Black.copy(alpha = 0.35f),
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.90f)
                                                        )
                                                    )
                                                )
                                        )

                                        // Top Hashtag Badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.tag,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        // Bottom Info & Play Button
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Explore Flow",
                                                    fontSize = 11.sp,
                                                    color = SpotifyGreen,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            // Quick Play Circle
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(SpotifyGreen)
                                                    .clickable {
                                                        if (item.track != null) {
                                                            playTrackList(listOf(item.track), 0, item.tag)
                                                        } else {
                                                            searchViewModel.playCategoryOrTagDirect(item.query, item.tag) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. "Browse all"
                    item {
                        Text(
                            text = "Browse all",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Browse Grid (Pairs of cards)
                    items(browseAllCategories.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BrowseCategoryCard(pair[0], Modifier.weight(1f)) { cat ->
                                when {
                                    cat.id.contains("podcast", ignoreCase = true) || cat.title.contains("podcast", ignoreCase = true) || cat.query.contains("podcast", ignoreCase = true) -> onOpenPodcasts()
                                    cat.id.contains("event", ignoreCase = true) || cat.title.contains("event", ignoreCase = true) || cat.title.contains("concert", ignoreCase = true) -> onOpenLiveEvents()
                                    cat.id.contains("ipop", ignoreCase = true) || cat.title.contains("i-pop", ignoreCase = true) || cat.title.contains("I-Pop", ignoreCase = true) -> onOpenIPop()
                                    else -> searchViewModel.playCategoryOrTagDirect(cat.query, cat.title.replace("\n", " ")) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                }
                            }
                            if (pair.size > 1) {
                                BrowseCategoryCard(pair[1], Modifier.weight(1f)) { cat ->
                                    when {
                                        cat.id.contains("podcast", ignoreCase = true) || cat.title.contains("podcast", ignoreCase = true) || cat.query.contains("podcast", ignoreCase = true) -> onOpenPodcasts()
                                        cat.id.contains("event", ignoreCase = true) || cat.title.contains("event", ignoreCase = true) || cat.title.contains("concert", ignoreCase = true) -> onOpenLiveEvents()
                                        cat.id.contains("ipop", ignoreCase = true) || cat.title.contains("i-pop", ignoreCase = true) || cat.title.contains("I-Pop", ignoreCase = true) -> onOpenIPop()
                                        else -> searchViewModel.playCategoryOrTagDirect(cat.query, cat.title.replace("\n", " ")) { tracks, idx, title -> playTrackList(tracks, idx, title) }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (query.isBlank()) {
                // ── Premium Recents State ──────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Recently Searched",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to jump back in",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA19BAE)
                                )
                            }
                            if (recents.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1F1F1F))
                                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                                        .clickable { searchViewModel.clearAllRecents() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Clear all",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFA19BAE)
                                    )
                                }
                            }
                        }
                    }

                    if (recents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Nothing searched yet",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Your recent tracks & artists appear here",
                                        fontSize = 13.sp,
                                        color = Color(0xFFA19BAE)
                                    )
                                }
                            }
                        }
                    } else {
                        items(recents) { item ->
                            RecentSearchRow(
                                item = item,
                                onClick = {
                                    query = item.title
                                    searchViewModel.onSearchQueryChanged(item.title)
                                    if (item.track != null && playbackViewModel != null) {
                                        playTrackList(listOf(item.track), 0, item.title)
                                    }
                                },
                                onRemove = { searchViewModel.removeRecent(item.id) }
                            )
                        }
                    }
                }
            } else {
                // Active Results State (Real-time dynamic search results)
                when (val state = searchState) {
                    is SearchUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SpotifyGreen)
                        }
                    }
                    is SearchUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color(0xFFA19BAE), fontSize = 14.sp)
                        }
                    }
                    is SearchUiState.Success -> {
                        val tracks = state.tracks
                        val topArtist = state.topArtist
                        val movieSoundtrack = state.movieSoundtrack
                        val artistCatalog = state.artistCatalog
                        val didYouMean = state.didYouMean

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // -1. "Did You Mean?" Typo-Correction Banner
                            if (didYouMean != null && !didYouMean.correctedQuery.equals(query, ignoreCase = true)) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E1E2E))
                                            .border(1.dp, SpotifyGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                query = didYouMean.correctedQuery
                                                searchViewModel.onSearchQueryChanged(didYouMean.correctedQuery)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Correction",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Showing results for ${didYouMean.correctedQuery}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Search instead for \"${didYouMean.originalQuery}\"",
                                                fontSize = 11.sp,
                                                color = Color(0xFFA19BAE)
                                            )
                                        }
                                    }
                                }
                            }

                            // 0. Top Result (Spotify-Grade Best Match)
                            val topItem = state.response.topResult
                            if (topItem != null) {
                                item {
                                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                        Text(
                                            text = "Top result",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        when (topItem) {
                                            is TrackDto -> {
                                                // Spotify-Style Top Result: Song Card
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color(0xFF181818))
                                                        .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
                                                        .clickable {
                                                            searchViewModel.addRecentTrack(topItem)
                                                            playTrackList(listOf(topItem) + tracks.filter { it.id != topItem.id }, 0, topItem.title)
                                                        }
                                                        .padding(14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(72.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFF282828))
                                                        ) {
                                                            AsyncImage(
                                                                model = ImageRequest.Builder(context).data(topItem.effectiveCoverUrl).crossfade(true).build(),
                                                                contentDescription = topItem.title,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(14.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = topItem.title,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Spacer(modifier = Modifier.height(3.dp))
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(Color(0xFF333333))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "SONG",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color(0xFFA19BAE)
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = topItem.artist,
                                                                    fontSize = 13.sp,
                                                                    color = Color(0xFFA19BAE),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(CircleShape)
                                                                .background(SpotifyGreen)
                                                                .clickable {
                                                                    searchViewModel.addRecentTrack(topItem)
                                                                    playTrackList(listOf(topItem) + tracks.filter { it.id != topItem.id }, 0, topItem.title)
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Play",
                                                                tint = Color.Black,
                                                                modifier = Modifier.size(26.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            is com.sonexa.app.data.model.ArtistDto -> {
                                                // Spotify-Style Top Result: Artist Card
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color(0xFF181818))
                                                        .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
                                                        .clickable { onOpenArtistProfile(topItem.id) }
                                                        .padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(68.dp)
                                                            .clip(CircleShape)
                                                            .background(SonexaGradientBrush)
                                                    ) {
                                                        AsyncImage(
                                                            model = ImageRequest.Builder(context).data(topItem.imageUrl).crossfade(true).build(),
                                                            contentDescription = topItem.name,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(14.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = topItem.name,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                            if (topItem.verified) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Icon(
                                                                    Icons.Default.CheckCircle,
                                                                    contentDescription = "Verified",
                                                                    tint = Color(0xFF3B82F6),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text(
                                                            text = "Artist • ${topItem.followersCount / 1000000}M Followers",
                                                            fontSize = 13.sp,
                                                            color = Color(0xFFA19BAE)
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(SpotifyGreen)
                                                            .clickable {
                                                                val artistTracks = artistCatalog?.popularTracks ?: tracks
                                                                if (artistTracks.isNotEmpty()) {
                                                                    playTrackList(artistTracks, 0, "${topItem.name} Radio")
                                                                }
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    ) {
                                                        Text(text = "Play Radio", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            is com.sonexa.app.data.provider.MovieSoundtrack -> {
                                                // Spotify-Style Top Result: Soundtrack Card
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color(0xFF2E1065), Color(0xFF1E1B4B))
                                                            )
                                                        )
                                                        .border(1.dp, SonexaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                                        .padding(14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(68.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(Color(0xFF1E1B4B))
                                                        ) {
                                                            AsyncImage(
                                                                model = ImageRequest.Builder(context).data(topItem.bannerUrl).crossfade(true).build(),
                                                                contentDescription = topItem.movieTitle,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(14.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(SpotifyGreen.copy(alpha = 0.2f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "MOVIE SOUNDTRACK",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = SpotifyGreen,
                                                                    letterSpacing = 0.5.sp
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(3.dp))
                                                            Text(
                                                                text = topItem.movieTitle,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = "${topItem.tracks.size} Songs • ${topItem.musicDirector} • ${topItem.releaseYear}",
                                                                fontSize = 12.sp,
                                                                color = SonexaTextMuted,
                                                                maxLines = 1
                                                            )
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .size(42.dp)
                                                                .clip(CircleShape)
                                                                .background(SpotifyGreen)
                                                                .clickable {
                                                                    playTrackList(topItem.tracks, 0, "${topItem.movieTitle} Soundtrack")
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Play Full Soundtrack",
                                                                tint = Color.Black,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Songs Header & Track List (Spotify puts Songs directly below Top Result)
                            val songsToShow = if (topItem is TrackDto) tracks.filter { it.id != topItem.id } else tracks
                            if (songsToShow.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Songs",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                        )
                                        Text(
                                            text = "${tracks.size} tracks",
                                            fontSize = 12.sp,
                                            color = SonexaPurpleLight
                                        )
                                    }
                                }

                                itemsIndexed(songsToShow) { index, track ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                searchViewModel.addRecentTrack(track)
                                                playTrackList(songsToShow, index, "$query Results")
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF282828))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                                                contentDescription = track.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (track.versionType != "Original") {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(SonexaPurplePrimary.copy(alpha = 0.45f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(text = track.versionType, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                }
                                                Text(
                                                    text = "Song • ${track.artist}",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFA19BAE),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (track.availableProviders.isNotEmpty()) {
                                            Text(
                                                text = track.availableProviders.first(),
                                                fontSize = 10.sp,
                                                color = SonexaPurpleLight,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                playbackViewModel?.toggleLike()
                                                Toast.makeText(context, "Added to Your Library", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AddCircleOutline,
                                                contentDescription = "Add",
                                                tint = Color(0xFFA19BAE),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Artists Section (Below Songs)
                            if (topArtist != null && topItem !is com.sonexa.app.data.model.ArtistDto) {
                                item {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text(
                                            text = "Artists",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(SonexaInputBg)
                                                .border(1.dp, SonexaCardBorder, RoundedCornerShape(14.dp))
                                                .clickable { onOpenArtistProfile(topArtist.id) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(SonexaGradientBrush)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context).data(topArtist.imageUrl).crossfade(true).build(),
                                                    contentDescription = topArtist.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = topArtist.name,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    if (topArtist.verified) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = "Verified",
                                                            tint = Color(0xFF3B82F6),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Artist • ${topArtist.followersCount / 1000000}M Followers",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFA19BAE)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SpotifyGreen)
                                                    .clickable {
                                                        val artistTracks = artistCatalog?.popularTracks ?: tracks
                                                        if (artistTracks.isNotEmpty()) {
                                                            playTrackList(artistTracks, 0, "${topArtist.name} Radio")
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(text = "Play Radio", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Discography / Albums & Singles Shelf (Below Songs & Artists)
                            val albumItems = (artistCatalog?.albums.orEmpty().map {
                                com.sonexa.app.data.model.AlbumDto(
                                    id = it.id,
                                    title = it.title,
                                    artist = topArtist?.name ?: "Artist",
                                    year = it.year,
                                    coverUrl = it.coverUrl,
                                    trackCount = it.trackCount
                                )
                            } + state.matchingAlbums).distinctBy { it.id }

                            if (albumItems.isNotEmpty() && topItem !is com.sonexa.app.data.provider.MovieSoundtrack) {
                                item {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text(
                                            text = "Albums & Singles",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            items(albumItems) { alb ->
                                                Column(
                                                    modifier = Modifier
                                                        .width(110.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(SonexaInputBg)
                                                        .clickable {
                                                            searchViewModel.onSearchQueryChanged(alb.title)
                                                        }
                                                        .padding(6.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context).data(alb.coverUrl).crossfade(true).build(),
                                                        contentDescription = alb.title,
                                                        modifier = Modifier
                                                            .size(98.dp)
                                                            .clip(RoundedCornerShape(6.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(text = alb.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                                    Text(text = alb.year, fontSize = 10.sp, color = SonexaTextMuted)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Playlists Section
                            if (state.matchingPlaylists.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Text(
                                            text = "Playlists",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            items(state.matchingPlaylists) { pl ->
                                                Column(
                                                    modifier = Modifier
                                                        .width(130.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF1E1E2E))
                                                        .clickable { onOpenPlaylistDetail(pl.id) }
                                                        .padding(8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context).data(pl.coverUrl).crossfade(true).build(),
                                                        contentDescription = pl.title,
                                                        modifier = Modifier
                                                            .size(114.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(text = pl.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                                    Text(text = pl.subtitle, fontSize = 11.sp, color = Color(0xFFA19BAE), maxLines = 1)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 6. Related Searches Chips
                            val relatedList = state.response.relatedSearches
                            if (relatedList.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                                        Text(
                                            text = "Related searches",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(relatedList) { rQuery ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .background(Color(0xFF242436))
                                                        .border(1.dp, Color(0xFF3B3B52), RoundedCornerShape(18.dp))
                                                        .clickable {
                                                            query = rQuery
                                                            searchViewModel.onSearchQueryChanged(rQuery)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Search, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(text = rQuery, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 7. Infinite Pagination Load More Row
                            if (state.hasMoreTracks || tracks.size >= 15) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.isLoadingMore) {
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
                                                    .clickable { searchViewModel.loadMoreTracks() }
                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "Load More Songs",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is SearchUiState.Idle -> {}
                }
            }
        }
    }
}

@Composable
private fun BrowseHeroCard(
    category: BrowseCategoryItem,
    modifier: Modifier = Modifier,
    onClick: (BrowseCategoryItem) -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(category.colorHex),
                        Color(category.colorHex).copy(alpha = 0.75f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable { onClick(category) }
            .padding(14.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 21.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(category.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 12.dp)
                .rotate(22f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun BrowseCategoryCard(
    category: BrowseCategoryItem,
    modifier: Modifier = Modifier,
    onClick: (BrowseCategoryItem) -> Unit
) {
    Box(
        modifier = modifier
            .height(102.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(category.colorHex),
                        Color(category.colorHex).copy(alpha = 0.80f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable { onClick(category) }
            .padding(14.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(category.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 14.dp, y = 14.dp)
                .rotate(22f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
        )
    }
}


@Composable
private fun RecentSearchRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val typeIcon = when (item.type) {
        "artist" -> Icons.Default.Person
        "album" -> Icons.Default.Album
        "playlist" -> Icons.Default.QueueMusic
        else -> Icons.Default.MusicNote
    }
    val typeColor = when (item.type) {
        "artist" -> Color(0xFF3B82F6)
        "album" -> Color(0xFFF59E0B)
        "playlist" -> Color(0xFF10B981)
        else -> SpotifyGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, Color(0xFF2D2D3D), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art with gradient overlay
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(if (item.type == "artist") CircleShape else RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2D1B6B), Color(0xFF1A0D3D))
                        )
                    )
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
                    // subtle dark scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                } else {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.type.replaceFirstChar { it.uppercaseChar() },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Text(
                        text = item.subtitle.removePrefix("Song • ").removePrefix("Search"),
                        fontSize = 11.sp,
                        color = Color(0xFFA19BAE),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Play button (for tracks only)
            if (item.type == "song" && item.track != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Remove button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A3A))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
