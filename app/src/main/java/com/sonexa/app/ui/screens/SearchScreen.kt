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
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.RecentSearchItem
import com.sonexa.app.ui.viewmodel.SearchUiState
import com.sonexa.app.ui.viewmodel.SearchViewModel

private val SpotifyGreen = Color(0xFF1ED760)

data class BrowseCategory(
    val title: String,
    val color: Color,
    val imageUrl: String,
    val query: String
)

@Composable
fun SearchScreen(
    onOpenVoiceSearch: () -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel? = null,
    onOpenFullPlayer: () -> Unit = {},
    onOpenArtistProfile: (String) -> Unit = {},
    onOpenPlaylistDetail: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val searchState by searchViewModel.uiState.collectAsState()
    val recents by searchViewModel.recents.collectAsState()

    val heroCategories = listOf(
        BrowseCategory("Music", Color(0xFFE91E63), "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300", "Top Songs"),
        BrowseCategory("Podcasts", Color(0xFF006450), "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=300", "Podcasts"),
        BrowseCategory("Live Events", Color(0xFF8400E7), "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300", "Live Concert Hits"),
        BrowseCategory("Home of I-Pop", Color(0xFF1E3264), "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300", "Indian Pop Hits")
    )

    val discoverNewItems = listOf(
        Pair("#hindi pop", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400"),
        Pair("#hindi lofi", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400"),
        Pair("#pink princess", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"),
        Pair("#punjabi wave", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400")
    )

    val browseAllCategories = listOf(
        BrowseCategory("Made For You", Color(0xFF7B2CBF), "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=300", "Made For You Mix"),
        BrowseCategory("Upcoming releases", Color(0xFF007F5F), "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300", "New Releases 2024"),
        BrowseCategory("Bollywood", Color(0xFFE76F51), "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=300", "Bollywood Hits"),
        BrowseCategory("Punjabi", Color(0xFFF4A261), "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300", "Punjabi Top Hits"),
        BrowseCategory("Bhojpuri", Color(0xFFE63946), "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=300", "Bhojpuri Hits"),
        BrowseCategory("Pop", Color(0xFFD81159), "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300", "Global Pop"),
        BrowseCategory("Romance", Color(0xFF8338EC), "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300", "Romantic Hits"),
        BrowseCategory("Chill & Lo-Fi", Color(0xFF2A9D8F), "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300", "Lo-Fi Beats"),
        BrowseCategory("Workout", Color(0xFFD90429), "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=300", "Workout Energetic"),
        BrowseCategory("Devotional", Color(0xFFFFB703), "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=300", "Bhakti Songs")
    )

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
        userDisplayName.firstOrNull()?.uppercase() ?: "U"
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
                            text = "Search",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { Toast.makeText(context, "Scan Spotify Code", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
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
                            color = Color(0xFF555555)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BrowseHeroCard(heroCategories[0], Modifier.weight(1f)) {
                                    isSearchActive = true
                                    query = it.query
                                    searchViewModel.searchCategoryDirect(it.query)
                                }
                                BrowseHeroCard(heroCategories[1], Modifier.weight(1f)) {
                                    isSearchActive = true
                                    query = it.query
                                    searchViewModel.searchCategoryDirect(it.query)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BrowseHeroCard(heroCategories[2], Modifier.weight(1f)) {
                                    isSearchActive = true
                                    query = it.query
                                    searchViewModel.searchCategoryDirect(it.query)
                                }
                                BrowseHeroCard(heroCategories[3], Modifier.weight(1f)) {
                                    isSearchActive = true
                                    query = it.query
                                    searchViewModel.searchCategoryDirect(it.query)
                                }
                            }
                        }
                    }

                    // 2. "Discover something new"
                    item {
                        Column {
                            Text(
                                text = "Discover something new",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(discoverNewItems) { (tag, imgUrl) ->
                                    Box(
                                        modifier = Modifier
                                            .width(135.dp)
                                            .height(210.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF242424))
                                            .clickable {
                                                isSearchActive = true
                                                query = tag.replace("#", "")
                                                searchViewModel.searchCategoryDirect(query)
                                            }
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(imgUrl).crossfade(true).build(),
                                            contentDescription = tag,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                    )
                                                )
                                        )
                                        Text(
                                            text = tag,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp)
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
                            fontSize = 18.sp,
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
                            BrowseCategoryCard(pair[0], Modifier.weight(1f)) {
                                isSearchActive = true
                                query = it.query
                                searchViewModel.searchCategoryDirect(it.query)
                            }
                            if (pair.size > 1) {
                                BrowseCategoryCard(pair[1], Modifier.weight(1f)) {
                                    isSearchActive = true
                                    query = it.query
                                    searchViewModel.searchCategoryDirect(it.query)
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
    category: BrowseCategory,
    modifier: Modifier = Modifier,
    onClick: (BrowseCategory) -> Unit
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.color)
            .clickable { onClick(category) }
            .padding(12.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart)
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(category.imageUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 10.dp)
                .rotate(24f)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun BrowseCategoryCard(
    category: BrowseCategory,
    modifier: Modifier = Modifier,
    onClick: (BrowseCategory) -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.color)
            .clickable { onClick(category) }
            .padding(12.dp)
    ) {
        Text(
            text = category.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
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
