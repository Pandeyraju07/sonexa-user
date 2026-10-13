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
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.sonexa.app.data.local.UserPlaylistStore
import com.sonexa.app.data.model.PlaylistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.MusicRepository
import com.sonexa.app.ui.components.AddToPlaylistBottomSheet
import com.sonexa.app.ui.theme.AppleMaterials
import com.sonexa.app.ui.theme.AppleType
import com.sonexa.app.ui.theme.appleBounce
import com.sonexa.app.ui.viewmodel.LibraryViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

private val PurplePrimary = Color(0xFFA855F7)
private val PurpleDeep = Color(0xFF7C3AED)

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

    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val currentUserName by sessionManager.userNameFlow.collectAsState()
    val currentUserAvatar by sessionManager.userAvatarFlow.collectAsState()
    val userDisplayName = currentUserName.takeIf { it.isNotBlank() }
        ?: sessionManager.userName?.takeIf { it.isNotBlank() }
        ?: "Creator"
    val avatarInitial = userDisplayName.firstOrNull()?.uppercase() ?: "C"

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAiStudioDialog by remember { mutableStateOf(false) }
    var showBlendDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // AI Studio State
    var selectedAiMood by remember { mutableStateOf("Lo-Fi Chill") }
    var selectedAiDuration by remember { mutableStateOf("1 min") }
    var aiPrompt by remember { mutableStateOf("") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var generatedAiTrack by remember { mutableStateOf<TrackDto?>(null) }
    var trackForAddToPlaylist by remember { mutableStateOf<TrackDto?>(null) }

    val moodChips = listOf(
        "Lo-Fi Chill" to "🎧",
        "Cyberpunk" to "🎮",
        "Bollywood" to "🎬",
        "Hip Hop" to "📶"
    )

    val durationChips = listOf("30s", "1 min", "3 min", "Custom")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B18))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Top Header: User Profile + Title + Tips Button ─────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .background(Color(0xFF2E1C4D)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUserAvatar.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
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
                                text = "Creator Studio",
                                style = AppleType.title1Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Turn your ideas into music",
                                style = AppleType.caption1,
                                color = Color(0xFFA19BAE)
                            )
                        }
                    }

                    // "Tips" Pill Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable {
                                Toast.makeText(context, "💡 Tip: Use descriptive prompts to generate custom beats!", Toast.LENGTH_LONG).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = "Tips",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tips",
                            style = AppleType.subheadlineBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── 2. 2x2 Feature Action Cards Grid (Screenshot 2) ─────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: New Playlist & AI Beat Studio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CreatorFeatureCard(
                            icon = Icons.Default.MusicNote,
                            title = "New Playlist",
                            subtitle = "Create your\nperfect mix",
                            gradientColors = listOf(Color(0xFFE11D48), Color(0xFF8B5CF6)),
                            modifier = Modifier.weight(1f),
                            onClick = { showCreatePlaylistDialog = true }
                        )

                        CreatorFeatureCard(
                            icon = Icons.Default.AutoAwesome,
                            title = "AI Beat Studio",
                            subtitle = "Generate unique\nbeats with AI",
                            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF312E81)),
                            modifier = Modifier.weight(1f),
                            onClick = { showAiStudioDialog = true }
                        )
                    }

                    // Row 2: Friend Blend & Import Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CreatorFeatureCard(
                            icon = Icons.Default.PersonAdd,
                            title = "Friend Blend",
                            subtitle = "Create and share\nplaylists with friends",
                            gradientColors = listOf(Color(0xFF059669), Color(0xFF044E3B)),
                            modifier = Modifier.weight(1f),
                            onClick = { showBlendDialog = true }
                        )

                        CreatorFeatureCard(
                            icon = Icons.Default.Link,
                            title = "Import Track",
                            subtitle = "Add link or stream\nfrom any source",
                            gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F)),
                            modifier = Modifier.weight(1f),
                            onClick = { showImportDialog = true }
                        )
                    }
                }
            }

            // ── 3. AI Signature Music Generator (BETA Card matching Screenshot 2) ─────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1430), Color(0xFF150D24))
                            )
                        )
                        .border(1.dp, PurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Title Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AI Signature Music Generator",
                                        style = AppleType.headlineBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PurpleDeep)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "BETA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = "Powered by Zynera Neural Audio Engine",
                                    style = AppleType.caption1,
                                    color = Color(0xFFC4B5FD)
                                )
                            }
                        }

                        Text(
                            text = "Describe your idea and let AI create original music for you",
                            style = AppleType.subheadline,
                            color = Color(0xFFA19BAE)
                        )

                        // Prompt Input Field (matches screenshot)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF140C22))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFFA19BAE),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                androidx.compose.foundation.text.BasicTextField(
                                    value = aiPrompt,
                                    onValueChange = { aiPrompt = it },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color.White,
                                        fontSize = 13.5.sp
                                    ),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (aiPrompt.isEmpty()) {
                                            Text(
                                                text = "E.g. \"Chill lo-fi for late night coding\"",
                                                color = Color(0xFFA19BAE),
                                                style = AppleType.subheadline
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Select Mood Section
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Select Mood",
                                style = AppleType.caption1Bold,
                                color = Color.White
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(moodChips) { (mood, emoji) ->
                                    val isSelected = selectedAiMood == mood
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) {
                                                    Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFF221835), Color(0xFF1B132B)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedAiMood = mood }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$emoji  $mood",
                                            style = AppleType.caption1Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Duration Section
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Duration",
                                style = AppleType.caption1Bold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                durationChips.forEach { duration ->
                                    val isSelected = selectedAiDuration == duration
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) {
                                                    Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color(0xFF221835), Color(0xFF1B132B)))
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedAiDuration = duration }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = duration,
                                            style = AppleType.caption1Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Generate AI Track Now Button (Full Width Gradient Pill with Circle Arrow on right)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFEC4899), Color(0xFFA855F7), Color(0xFF6366F1))
                                    )
                                )
                                .appleBounce {
                                    coroutineScope.launch {
                                        isGeneratingAi = true
                                        val result = musicRepository.generateAiSignature(
                                            mood = selectedAiMood,
                                            genre = "Lo-Fi Piano",
                                            prompt = aiPrompt.ifBlank { "Personalized beat for $selectedAiMood, duration $selectedAiDuration" }
                                        )
                                        isGeneratingAi = false
                                        if (result.isSuccess) {
                                            val res = result.getOrNull()!!
                                            val newTrack = TrackDto(
                                                id = "ai_" + System.currentTimeMillis(),
                                                title = res.vibeTitle.ifBlank { "AI $selectedAiMood Mix" },
                                                artist = "Zynera AI Studio",
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
                                }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(28.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isGeneratingAi) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Synthesizing Neural Audio...",
                                            style = AppleType.headlineBold,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = "✨ Generate AI Track Now",
                                            style = AppleType.headlineBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Generate",
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 4. "Your Playlists (3)" Section Header + Sort + New Button ─────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Playlists (${userPlaylists.size})",
                        style = AppleType.title2Bold,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sort Pill
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Sort",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sort",
                                    style = AppleType.caption1Bold,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color(0xFF241C35))
                            ) {
                                listOf("Recents", "Recently Added", "Alphabetical").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = { showSortMenu = false }
                                    )
                                }
                            }
                        }

                        // "+ New" Pill Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PurplePrimary)
                                .clickable { showCreatePlaylistDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ New",
                                style = AppleType.caption1Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ── 5. User Playlists List (matches Screenshot 2) ─────
            if (userPlaylists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No playlists created yet. Tap '+ New' to start!",
                            style = AppleType.subheadline,
                            color = Color(0xFFA19BAE)
                        )
                    }
                }
            } else {
                items(userPlaylists) { pl ->
                    var showItemMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
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
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF221735))
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
                                        tint = Color(0xFFC4B5FD),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pl.title,
                                    style = AppleType.headlineBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pl.trackCount} songs • You",
                                    style = AppleType.caption1,
                                    color = Color(0xFFA19BAE)
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showItemMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFFA19BAE),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showItemMenu,
                                onDismissRequest = { showItemMenu = false },
                                modifier = Modifier.background(Color(0xFF241C35))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open Playlist", color = Color.White) },
                                    onClick = {
                                        showItemMenu = false
                                        onOpenPlaylist(pl.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Playlist", color = Color(0xFFEF4444)) },
                                    onClick = {
                                        showItemMenu = false
                                        UserPlaylistStore.deletePlaylist(context, pl.id)
                                        Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs: Create Playlist, Friend Blend, Import Track ─────
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = Color(0xFF211833),
            title = { Text("Create New Playlist", color = Color.White, style = AppleType.title3Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
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
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Description (Optional)", color = Color(0xFFA19BAE)) },
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
                        val name = playlistName.trim().ifBlank { "My Playlist #${userPlaylists.size + 1}" }
                        UserPlaylistStore.createPlaylist(context, name, playlistDesc.trim(), "")
                        showCreatePlaylistDialog = false
                        Toast.makeText(context, "Playlist '$name' created!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    if (showBlendDialog) {
        var friendCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBlendDialog = false },
            containerColor = Color(0xFF211833),
            title = { Text("Friend Blend", color = Color.White, style = AppleType.title3Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Combine taste with a friend into a dynamic synchronized blend playlist.", color = Color(0xFFA19BAE), style = AppleType.subheadline)
                    OutlinedTextField(
                        value = friendCode,
                        onValueChange = { friendCode = it },
                        label = { Text("Friend's Zynera Username or Invite Code", color = Color(0xFFA19BAE)) },
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
                        val blendName = "Blend: $userDisplayName & ${friendCode.trim().ifBlank { "Friend" }}"
                        UserPlaylistStore.createPlaylist(context, blendName, "Dynamic Friend Blend session", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500")
                        showBlendDialog = false
                        Toast.makeText(context, "Blend playlist created!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Create Blend", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlendDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    if (showImportDialog) {
        var importUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color(0xFF211833),
            title = { Text("Import Audio Track", color = Color.White, style = AppleType.title3Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste any public MP3, YouTube, or direct audio link to stream or add to playlists.", color = Color(0xFFA19BAE), style = AppleType.subheadline)
                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        label = { Text("Media Stream URL", color = Color(0xFFA19BAE)) },
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
                        val url = importUrl.trim()
                        if (url.isNotBlank()) {
                            val importedTrack = TrackDto(
                                id = "import_" + System.currentTimeMillis(),
                                title = "Imported Track",
                                artist = "External Source",
                                album = "Web Stream",
                                durationMs = 180000L,
                                audioUrl = url,
                                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500"
                            )
                            playbackViewModel.play(importedTrack, "Imported Stream")
                            onOpenFullPlayer()
                            showImportDialog = false
                            Toast.makeText(context, "Stream playing!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Play & Import", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }
}

@Composable
private fun CreatorFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(18.dp))
            .appleBounce { onClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = AppleType.headlineBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppleType.caption1,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }

        // Circular Arrow Button on bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
