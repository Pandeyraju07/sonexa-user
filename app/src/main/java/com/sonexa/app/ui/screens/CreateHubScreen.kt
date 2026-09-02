package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
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
import com.sonexa.app.data.local.UserPlaylistStore
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.ui.components.AddToPlaylistBottomSheet
import com.sonexa.app.ui.viewmodel.LibraryViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1ED760)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHubScreen(
    onOpenPlaylist: (String) -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val musicRepository = remember { MusicRepository() }
    val userPlaylists by UserPlaylistStore.playlists.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAiStudioDialog by remember { mutableStateOf(false) }
    var showBlendDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // AI Studio State
    var selectedAiMood by remember { mutableStateOf("Lo-Fi Chill") }
    var selectedAiGenre by remember { mutableStateOf("Lo-Fi Piano") }
    var aiPrompt by remember { mutableStateOf("") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var generatedAiTrack by remember { mutableStateOf<TrackDto?>(null) }
    var trackForAddToPlaylist by remember { mutableStateOf<TrackDto?>(null) }

    val aiMoods = listOf(
        "Lo-Fi Chill" to Color(0xFF6B3CE9),
        "Cyberpunk Synth" to Color(0xFF06B6D4),
        "Bollywood Euphoria" to Color(0xFFEC4899),
        "Monsoon Acoustic" to Color(0xFF3B82F6),
        "High Energy Gym" to Color(0xFFEF4444),
        "Soulful Peace" to Color(0xFF10B981)
    )

    val aiStyles = listOf("Lo-Fi Piano", "808 Bass & Trap", "Acoustic Guitar", "Synthwave 80s", "Tabla & Classical", "EDM Drop")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 125.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Creator Studio",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create custom playlists, generate AI beats, and import sounds",
                        fontSize = 13.sp,
                        color = Color(0xFFA19BAE)
                    )
                }
            }

            // Top Creation Action Cards Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Create Playlist Card
                        CreationActionCard(
                            title = "New Playlist",
                            subtitle = "Curate your mix",
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
                            modifier = Modifier.weight(1f),
                            onClick = { showCreatePlaylistDialog = true }
                        )

                        // 2. AI Mix Studio Card
                        CreationActionCard(
                            title = "AI Beat Studio",
                            subtitle = "Generate smart music",
                            icon = Icons.Default.AutoAwesome,
                            gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
                            modifier = Modifier.weight(1f),
                            onClick = { showAiStudioDialog = true }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 3. Collaborative Blend Card
                        CreationActionCard(
                            title = "Friend Blend",
                            subtitle = "Shared playlist session",
                            icon = Icons.Default.GroupAdd,
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            modifier = Modifier.weight(1f),
                            onClick = { showBlendDialog = true }
                        )

                        // 4. Import Audio URL Card
                        CreationActionCard(
                            title = "Import Track",
                            subtitle = "Add link or stream",
                            icon = Icons.Default.Link,
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            modifier = Modifier.weight(1f),
                            onClick = { showImportDialog = true }
                        )
                    }
                }
            }

            // AI Studio Live Generator Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1F1235), Color(0xFF2A1B4E))
                            )
                        )
                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("AI Signature Music Generator", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Powered by Sonexa Neural Audio Engine", fontSize = 11.5.sp, color = Color(0xFFC4B5FD))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Select Mood:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(aiMoods) { (mood, color) ->
                                val isSelected = selectedAiMood == mood
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) color else Color(0xFF2A2A2A))
                                        .clickable { selectedAiMood = mood }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = mood,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGeneratingAi = true
                                    val result = musicRepository.generateAiSignature(
                                        mood = selectedAiMood,
                                        genre = selectedAiGenre,
                                        prompt = aiPrompt.ifBlank { "Smart personalized AI beat for $selectedAiMood" }
                                    )
                                    isGeneratingAi = false
                                    if (result.isSuccess) {
                                        val res = result.getOrNull()!!
                                        val newTrack = TrackDto(
                                            id = "ai_" + System.currentTimeMillis(),
                                            title = res.vibeTitle.ifBlank { "AI $selectedAiMood Mix" },
                                            artist = "Sonexa AI Studio",
                                            album = "AI Signatures",
                                            durationMs = 180000L,
                                            audioUrl = res.aiGeneratedAudioUrl.ifBlank { "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3" },
                                            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                                            playsCount = "Generated just now",
                                            isLiked = false
                                        )
                                        generatedAiTrack = newTrack
                                        playbackViewModel.play(newTrack, "AI Studio Mix")
                                        onOpenFullPlayer()
                                        Toast.makeText(context, "AI Track Generated & Playing!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to generate AI track", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isGeneratingAi) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Neural Audio...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate AI Track Now ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Section: Your Created Playlists
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Playlists (${userPlaylists.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { showCreatePlaylistDialog = true }) {
                        Text("+ New", color = SpotifyGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // User Playlists List
            if (userPlaylists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No playlists created yet. Tap '+ New' to start!", color = Color(0xFFA19BAE), fontSize = 13.sp)
                    }
                }
            } else {
                items(userPlaylists) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenPlaylist(pl.id) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF282828))
                            ) {
                                if (pl.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(pl.coverUrl).crossfade(true).build(),
                                        contentDescription = pl.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pl.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pl.trackCount} songs • ${pl.creatorName}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA19BAE)
                                )
                            }
                        }

                        IconButton(onClick = { onOpenPlaylist(pl.id) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA19BAE))
                        }
                    }
                }
            }
        }
    }

    // 1. Create Playlist Dialog
    if (showCreatePlaylistDialog) {
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
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Create New Playlist", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("My Studio Playlist", color = Color(0xFFA19BAE)) },
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
                        placeholder = { Text("Description (optional)", color = Color(0xFFA19BAE)) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Choose cover artwork:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        val title = playlistName.trim().ifBlank { "My Studio Playlist #${userPlaylists.size + 1}" }
                        val cover = coverPresets.getOrElse(selectedCoverPreset) { coverPresets[0] }
                        libraryViewModel.createPlaylist(
                            context = context,
                            title = title,
                            description = playlistDesc.trim(),
                            coverUrl = cover
                        ) { newPl ->
                            onOpenPlaylist(newPl.id)
                        }
                        showCreatePlaylistDialog = false
                        Toast.makeText(context, "Playlist Created!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Create & Open", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // 2. AI Studio Prompt Dialog
    if (showAiStudioDialog) {
        AlertDialog(
            onDismissRequest = { showAiStudioDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("AI DJ Beat Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Describe what you want to hear:", color = Color(0xFFA19BAE), fontSize = 13.sp)
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = { Text("e.g. Dreamy midnight lo-fi beats with rain sounds", color = Color(0xFFA19BAE)) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Instrument / Beat Style:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(aiStyles) { style ->
                            val isSelected = selectedAiGenre == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SpotifyGreen else Color(0xFF333333))
                                    .clickable { selectedAiGenre = style }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = style,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAiStudioDialog = false
                        coroutineScope.launch {
                            isGeneratingAi = true
                            val result = musicRepository.generateAiSignature(
                                mood = selectedAiMood,
                                genre = selectedAiGenre,
                                prompt = aiPrompt.ifBlank { "Custom studio mix in $selectedAiGenre" }
                            )
                            isGeneratingAi = false
                            if (result.isSuccess) {
                                val res = result.getOrNull()!!
                                val newTrack = TrackDto(
                                    id = "ai_" + System.currentTimeMillis(),
                                    title = res.vibeTitle.ifBlank { "Studio AI: $selectedAiGenre" },
                                    artist = "Sonexa AI Studio",
                                    album = "AI Generator",
                                    durationMs = 180000L,
                                    audioUrl = res.aiGeneratedAudioUrl.ifBlank { "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3" },
                                    coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                                    isLiked = false
                                )
                                playbackViewModel.play(newTrack, "Studio AI")
                                onOpenFullPlayer()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Generate & Play", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiStudioDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // 3. Friend Blend Invitation Dialog
    if (showBlendDialog) {
        val blendCode = remember { "SONEXA-BLEND-${(1000..9999).random()}" }
        AlertDialog(
            onDismissRequest = { showBlendDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Friend Blend Session", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Blend combines your music tastes with friends into a daily updated shared playlist.", color = Color(0xFFA19BAE), fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(blendCode, color = SpotifyGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "Join my Sonexa Blend Session with code $blendCode!\nhttps://sonexa.app/blend/$blendCode")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Blend Invite"))
                        showBlendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Share Invite Code", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlendDialog = false }) {
                    Text("Close", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // 4. Import Audio URL Dialog
    if (showImportDialog) {
        var importUrl by remember { mutableStateOf("") }
        var importTitle by remember { mutableStateOf("") }
        var importArtist by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Import Audio Track", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        label = { Text("Audio URL / YouTube Link", color = Color(0xFFA19BAE)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = importTitle,
                        onValueChange = { importTitle = it },
                        label = { Text("Track Title", color = Color(0xFFA19BAE)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = importArtist,
                        onValueChange = { importArtist = it },
                        label = { Text("Artist Name", color = Color(0xFFA19BAE)) },
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
                        val url = importUrl.trim()
                        val title = importTitle.trim().ifBlank { "Imported Track" }
                        val artist = importArtist.trim().ifBlank { "External Artist" }

                        if (url.isBlank()) {
                            Toast.makeText(context, "Please enter an audio URL", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newTrack = TrackDto(
                            id = "imp_" + System.currentTimeMillis(),
                            title = title,
                            artist = artist,
                            album = "Imported Tracks",
                            durationMs = 210000L,
                            audioUrl = url,
                            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
                        )
                        showImportDialog = false
                        trackForAddToPlaylist = newTrack
                        Toast.makeText(context, "Track ready! Select a playlist to save.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Add to Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    trackForAddToPlaylist?.let { tr ->
        AddToPlaylistBottomSheet(
            track = tr,
            onDismiss = { trackForAddToPlaylist = null }
        )
    }
}

@Composable
private fun CreationActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}
