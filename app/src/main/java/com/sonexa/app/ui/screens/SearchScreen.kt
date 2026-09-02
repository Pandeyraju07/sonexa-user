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

private val SpotifyGreen = Color(0xFF1ED760)

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

            // Top Header / Search Bar Row
            if (!isSearchActive && query.isBlank()) {
                // Spotify Browse Home Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8590C))
                                .clickable { onOpenProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitial,
                                color = Color.Black,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

            // Main Content Area
            if (!isSearchActive && query.isBlank()) {
                // Browse Home (2x2 Hero categories + Discover + Browse All)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    // 2. "Discover something new"
                    item {
                        Column {
                            Text(
                                text = "Discover something new",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(discoverItems) { item ->
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(215.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF242424))
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
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(item.imageUrl).crossfade(true).build(),
                                            contentDescription = item.tag,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                                                    )
                                                )
                                        )
                                        Text(
                                            text = item.tag,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp)
                                        )
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
                // Recents State (Screenshot 3)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recents",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (recents.isNotEmpty()) {
                                Text(
                                    text = "Clear all",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA19BAE),
                                    modifier = Modifier.clickable { searchViewModel.clearAllRecents() }
                                )
                            }
                        }
                    }

                    items(recents) { item ->
                        RecentSearchRow(
                            item = item,
                            onClick = {
                                if (item.type == "artist") {
                                    query = item.title
                                    searchViewModel.onSearchQueryChanged(item.title)
                                } else {
                                    query = item.title
                                    searchViewModel.onSearchQueryChanged(item.title)
                                }
                            },
                            onRemove = { searchViewModel.removeRecent(item.id) }
                        )
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

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Artist Match (if singer searched e.g. Arijit Singh)
                            if (topArtist != null) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .clickable { onOpenArtistProfile(topArtist.id) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF333333))
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
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Verified",
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Artist • Verified Singer",
                                                fontSize = 13.sp,
                                                color = Color(0xFFA19BAE)
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Open Artist",
                                            tint = Color(0xFFA19BAE)
                                        )
                                    }
                                }
                            }

                            // Matching Songs List
                            item {
                                Text(
                                    text = "Songs",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                                )
                            }

                            itemsIndexed(tracks) { index, track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchViewModel.addRecentTrack(track)
                                            playTrackList(tracks, index, "$query Results")
                                        }
                                        .padding(vertical = 6.dp),
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
                                        Text(
                                            text = "Song • ${track.artist}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFA19BAE),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
            .height(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(category.colorHex))
            .clickable { onClick(category) }
            .padding(12.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(category.imageUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 10.dp)
                .rotate(24f)
                .clip(RoundedCornerShape(4.dp))
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
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(category.colorHex))
            .clickable { onClick(category) }
            .padding(12.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(category.imageUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 12.dp)
                .rotate(24f)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun RecentSearchRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.type == "artist") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 13.sp,
                color = Color(0xFFA19BAE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.isSaved) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Saved",
                tint = SpotifyGreen,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
        } else {
            IconButton(
                onClick = { /* Add to library */ },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "Add",
                    tint = Color(0xFFA19BAE),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFFA19BAE),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
