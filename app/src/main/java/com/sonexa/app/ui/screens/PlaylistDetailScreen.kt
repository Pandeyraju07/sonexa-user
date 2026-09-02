package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
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
import com.sonexa.app.data.local.UserPlaylistStore
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.JioSaavnMusicProvider
import com.sonexa.app.ui.components.AddToPlaylistBottomSheet
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PlaylistDetailViewModel
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1ED760)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    playlistId: String = "pl_1",
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    var showAddSongsSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var selectedTrackForMenu by remember { mutableStateOf<TrackDto?>(null) }
    var trackForAddToAnotherPlaylist by remember { mutableStateOf<TrackDto?>(null) }

    LaunchedEffect(playlistId) {
        viewModel.load(playlistId)
    }

    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    val ready = uiState as? CatalogUiState.Ready
    val playlist = ready?.data?.playlist
    val tracks = ready?.data?.tracks.orEmpty()
    val playlistTitle = playlist?.title ?: "Playlist"
    val coverUrl = playlist?.coverUrl.orEmpty()
    val isUserCreated = playlist?.isUserCreated == true || UserPlaylistStore.getPlaylist(playlistId) != null
    val isLikedPlaylist = playlistId == "pl_liked" || playlistId.contains("liked", ignoreCase = true)

    fun playTrack(track: TrackDto) {
        if (track.audioUrl.isBlank()) {
            Toast.makeText(context, "Audio preview loading...", Toast.LENGTH_SHORT).show()
        }
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackViewModel.playQueue(tracks.ifEmpty { listOf(track) }, index, playlistTitle)
        onOpenFullPlayer()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                CircularProgressIndicator(
                    color = SpotifyGreen,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is CatalogUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.message, color = SonexaTextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SonexaGradientButton(text = "Retry", onClick = { viewModel.load(playlistId) })
                }
            }
            is CatalogUiState.Ready -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Navigation Bar (Back + 3-dots Menu)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF242424))
                                    .clickable { onNavigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (isUserCreated && !isLikedPlaylist) {
                                Box {
                                    IconButton(onClick = { showOptionsMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Playlist Options",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showOptionsMenu,
                                        onDismissRequest = { showOptionsMenu = false },
                                        modifier = Modifier.background(Color(0xFF282828))
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Add songs to playlist", color = Color.White) },
                                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = SpotifyGreen) },
                                            onClick = {
                                                showOptionsMenu = false
                                                showAddSongsSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit playlist details", color = Color.White) },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                            onClick = {
                                                showOptionsMenu = false
                                                showEditDialog = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete playlist", color = Color(0xFFEF4444)) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                                            onClick = {
                                                showOptionsMenu = false
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Hero Header: Cover Art + Title + Subtitle + Action Buttons
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Cover Image or Gradient Box
                            if (isLikedPlaylist) {
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
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
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (coverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = playlistTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(70.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = playlistTitle,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = playlist?.subtitle?.ifBlank { null }
                                    ?: "Playlist • ${playlist?.creatorName ?: "You"} • ${tracks.size} songs",
                                fontSize = 13.sp,
                                color = Color(0xFFA19BAE)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Play Buttons: Shuffle Play & Green Play Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (isUserCreated && !isLikedPlaylist) {
                                        Button(
                                            onClick = { showAddSongsSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282828)),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add songs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            tracks.randomOrNull()?.let { playTrack(it) }
                                                ?: Toast.makeText(context, "No tracks in playlist", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282828)),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Shuffle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Green FAB Play
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(SpotifyGreen)
                                        .clickable {
                                            val first = tracks.firstOrNull()
                                            if (first != null) {
                                                playTrack(first)
                                            } else {
                                                Toast.makeText(context, "Add songs to play this playlist", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "Tracks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Track List or Empty State
                    if (tracks.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "This playlist is empty",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add some songs to get started",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA19BAE)
                                )
                                if (isUserCreated) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddSongsSheet = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Find & Add Songs", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(tracks) { idx, track ->
                            val isCurrent = playbackState.track?.id == track.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { playTrack(track) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) SpotifyGreen else Color(0xFFA19BAE),
                                        modifier = Modifier.width(28.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF282828))
                                    ) {
                                        if (track.effectiveCoverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                                                contentDescription = track.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) SpotifyGreen else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            fontSize = 12.sp,
                                            color = Color(0xFFA19BAE),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // 3-Dots on Track
                                Box {
                                    IconButton(onClick = { selectedTrackForMenu = track }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Track Options",
                                            tint = Color(0xFFA19BAE),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (selectedTrackForMenu?.id == track.id) {
                                        DropdownMenu(
                                            expanded = true,
                                            onDismissRequest = { selectedTrackForMenu = null },
                                            modifier = Modifier.background(Color(0xFF282828))
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Add to other playlist", color = Color.White) },
                                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = Color.White) },
                                                onClick = {
                                                    val tr = selectedTrackForMenu
                                                    selectedTrackForMenu = null
                                                    if (tr != null) trackForAddToAnotherPlaylist = tr
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    val isLiked = LikedSongsStore.isLiked(track.id)
                                                    Text(if (isLiked) "Remove from Liked Songs" else "Add to Liked Songs", color = Color.White)
                                                },
                                                leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White) },
                                                onClick = {
                                                    val tr = selectedTrackForMenu
                                                    selectedTrackForMenu = null
                                                    if (tr != null) {
                                                        LikedSongsStore.toggleLike(context, tr)
                                                        Toast.makeText(context, "Liked songs updated", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                            if (isUserCreated) {
                                                DropdownMenuItem(
                                                    text = { Text("Remove from this playlist", color = Color(0xFFEF4444)) },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                                                    onClick = {
                                                        val tr = selectedTrackForMenu
                                                        selectedTrackForMenu = null
                                                        if (tr != null) {
                                                            viewModel.removeTrack(context, playlistId, tr.id)
                                                            Toast.makeText(context, "Removed ${tr.title}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add to Another Playlist Bottom Sheet
    trackForAddToAnotherPlaylist?.let { tr ->
        AddToPlaylistBottomSheet(
            track = tr,
            onDismiss = { trackForAddToAnotherPlaylist = null }
        )
    }

    // "Add Songs to Playlist" Search Bottom Sheet
    if (showAddSongsSheet) {
        AddSongsSearchBottomSheet(
            playlistId = playlistId,
            onDismiss = { showAddSongsSheet = false },
            onTrackAdded = { track ->
                viewModel.addTrack(context, playlistId, track)
            }
        )
    }

    // Edit Playlist Dialog
    if (showEditDialog && playlist != null) {
        var editTitle by remember { mutableStateOf(playlist.title) }
        var editDesc by remember { mutableStateOf(playlist.subtitle) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Edit playlist", color = Color.White, fontWeight = FontWeight.Bold) },
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
                        viewModel.updatePlaylist(
                            context = context,
                            id = playlistId,
                            title = editTitle.trim(),
                            description = editDesc.trim(),
                            coverUrl = playlist.coverUrl
                        )
                        showEditDialog = false
                        Toast.makeText(context, "Playlist updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Delete Playlist Dialog
    if (showDeleteDialog && playlist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Delete playlist?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${playlist.title}\"? This action cannot be undone.", color = Color(0xFFA19BAE)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePlaylist(context, playlistId) {
                            Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongsSearchBottomSheet(
    playlistId: String,
    onDismiss: () -> Unit,
    onTrackAdded: (TrackDto) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val saavnProvider = remember { JioSaavnMusicProvider() }
    val coroutineScope = rememberCoroutineScope()

    // Default suggestions
    LaunchedEffect(Unit) {
        isLoading = true
        val trending = saavnProvider.search("Top Trending Hindi Hits", limit = 15).getOrDefault(emptyList())
        searchResults = trending
        isLoading = false
    }

    fun doSearch(text: String) {
        query = text
        coroutineScope.launch {
            if (text.isBlank()) {
                isLoading = true
                searchResults = saavnProvider.search("Top Trending Hindi Hits", limit = 15).getOrDefault(emptyList())
                isLoading = false
                return@launch
            }
            isLoading = true
            val results = saavnProvider.search(text, limit = 20).getOrDefault(emptyList())
            searchResults = results
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF555555))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add to this playlist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { doSearch(it) },
                placeholder = { Text("Search songs or artists", color = Color(0xFFA19BAE)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFA19BAE))
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF282828),
                    unfocusedContainerColor = Color(0xFF282828),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { track ->
                        val addedTracks = UserPlaylistStore.getTracks(playlistId)
                        val isAdded = addedTracks.any { it.id == track.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF282828))
                                ) {
                                    if (track.effectiveCoverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(track.effectiveCoverUrl).crossfade(true).build(),
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        fontSize = 12.sp,
                                        color = Color(0xFFA19BAE),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Plus / Added button
                            IconButton(
                                onClick = {
                                    if (!isAdded) {
                                        onTrackAdded(track)
                                        Toast.makeText(context, "Added ${track.title}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                if (isAdded) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Added",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AddCircleOutline,
                                        contentDescription = "Add",
                                        tint = Color.White,
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
}
