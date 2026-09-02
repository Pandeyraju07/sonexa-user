package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.sonexa.app.data.model.MusicJourneyPhaseItemDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.SpotifyGreen
import com.sonexa.app.ui.viewmodel.AiIntelligenceViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

data class JourneyThemeOption(
    val title: String,
    val key: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicJourneyScreen(
    onBack: () -> Unit,
    aiViewModel: AiIntelligenceViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by aiViewModel.uiState.collectAsState()
    var selectedDuration by remember { mutableIntStateOf(60) }
    var selectedTheme by remember { mutableStateOf("WORKOUT") }

    val themes = remember {
        listOf(
            JourneyThemeOption("Workout Grind", "WORKOUT", Icons.Default.FitnessCenter, Color(0xFFF59E0B)),
            JourneyThemeOption("Night Drive", "ROAD_TRIP", Icons.Default.DirectionsCar, Color(0xFF8B5CF6)),
            JourneyThemeOption("Deep Focus", "STUDY_FLOW", Icons.Default.Psychology, Color(0xFF06B6D4)),
            JourneyThemeOption("Party Starter", "PARTY", Icons.Default.Celebration, Color(0xFFEC4899)),
            JourneyThemeOption("Calm Flow", "CALM_TO_ENERGETIC", Icons.Default.Spa, SpotifyGreen)
        )
    }

    LaunchedEffect(selectedDuration, selectedTheme) {
        aiViewModel.createMusicJourney(selectedTheme, selectedDuration)
    }

    val journey = uiState.currentJourney
    val activeTrackId = playbackViewModel.uiState.collectAsState().value.track?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Music Journey", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 19.sp)
                        Text(journey?.title ?: "Multi-Phase Flow", fontSize = 12.sp, color = Color(0xFFAFA9BB))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E091A))
            )
        },
        containerColor = Color(0xFF0E091A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. HORIZONTAL SINGLE-ROW THEME SELECTOR CAROUSEL (Handy & Compact)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { theme ->
                    val isSelected = selectedTheme == theme.key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) theme.accentColor else Color(0xFF1E172E)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTheme = theme.key }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = theme.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else theme.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = theme.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // 2. DURATION SELECTOR PILLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Session Duration:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFAFA9BB)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 45, 60, 90, 120).forEach { mins ->
                        val isSelected = selectedDuration == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SpotifyGreen else Color(0xFF1E172E))
                                .clickable { selectedDuration = mins }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 11.sp,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. HERO ACTION DECK (Play Journey & Shuffle All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val tracks = journey?.allTracks.orEmpty()
                        if (tracks.isNotEmpty()) {
                            playbackViewModel.playQueue(tracks, 0, journey?.title ?: "Journey Flow")
                            Toast.makeText(context, "Playing entire journey (${tracks.size} tracks)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Journey", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        val shuffled = journey?.allTracks.orEmpty().shuffled()
                        if (shuffled.isNotEmpty()) {
                            playbackViewModel.playQueue(shuffled, 0, "${journey?.title} (Shuffled)")
                            Toast.makeText(context, "Shuffled journey (${shuffled.size} tracks)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D3B))
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            // 4. MULTI-PHASE SECTION LIST WITH SECTION CONTROLS & TRACKS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
            ) {
                journey?.phases?.forEachIndexed { phaseIdx, phase ->
                    item {
                        JourneySectionCard(
                            phase = phase,
                            phaseIndex = phaseIdx + 1,
                            totalPhases = journey.phases.size,
                            activeTrackId = activeTrackId,
                            allJourneyTracks = journey.allTracks,
                            onPlaySection = {
                                if (phase.tracks.isNotEmpty()) {
                                    playbackViewModel.playQueue(phase.tracks, 0, "Phase: ${phase.name}")
                                    Toast.makeText(context, "Playing section: ${phase.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShuffleSection = {
                                val shuffled = phase.tracks.shuffled()
                                if (shuffled.isNotEmpty()) {
                                    playbackViewModel.playQueue(shuffled, 0, "Phase: ${phase.name} (Shuffled)")
                                    Toast.makeText(context, "Shuffled section: ${phase.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onTrackClick = { track ->
                                val fullQueue = journey.allTracks
                                val trackIndex = fullQueue.indexOf(track).coerceAtLeast(0)
                                playbackViewModel.playQueue(fullQueue, trackIndex, journey.title)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneySectionCard(
    phase: MusicJourneyPhaseItemDto,
    phaseIndex: Int,
    totalPhases: Int,
    activeTrackId: String?,
    allJourneyTracks: List<TrackDto>,
    onPlaySection: () -> Unit,
    onShuffleSection: () -> Unit,
    onTrackClick: (TrackDto) -> Unit
) {
    val phaseColor = when (phaseIndex % 4) {
        1 -> Color(0xFFF59E0B)
        2 -> Color(0xFF8B5CF6)
        3 -> Color(0xFFEC4899)
        else -> SpotifyGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF161024))
            .border(1.dp, phaseColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(phaseColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PHASE $phaseIndex OF $totalPhases",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = phaseColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${phase.startMinute}m - ${phase.endMinute}m",
                        fontSize = 11.sp,
                        color = Color(0xFFAFA9BB)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phase.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Quick Play & Shuffle Action Buttons for Section
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onShuffleSection,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle Section",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onPlaySection,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(phaseColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Section",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Energy Indicator Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ ${(phase.targetEnergy * 100).toInt()}% Energy",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = phaseColor
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(phase.targetEnergy.toFloat().coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(phaseColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracks in this Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            phase.tracks.forEachIndexed { idx, track ->
                val isCurrentPlaying = track.id == activeTrackId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrentPlaying) SpotifyGreen.copy(0.12f) else Color.White.copy(0.03f))
                        .clickable { onTrackClick(track) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isCurrentPlaying) SpotifyGreen else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color(0xFFAFA9BB),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (isCurrentPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isCurrentPlaying) SpotifyGreen else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}