package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.data.local.UserTastePreferencesStore
import com.sonexa.app.data.model.*
import com.sonexa.app.data.provider.*
import com.sonexa.app.data.repository.AiRepository
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1ED760)
private val DeepViolet = Color(0xFF170C28)
private val CardSurface = Color(0xFF1A132F)
private val AccentCyan = Color(0xFF38BDF8)
private val AccentPink = Color(0xFFF43F5E)
private val AccentGold = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicIntelligenceHubScreen(
    onBack: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiRepository = remember { AiRepository() }
    val playbackState by playbackViewModel.uiState.collectAsState()
    val currentTrack = playbackState.track

    // Sub-feature states
    val memories by MusicMemoryService.memories.collectAsState()
    val tasteControls by UserTastePreferencesStore.tasteControls.collectAsState()
    var lifeSoundtrack by remember { mutableStateOf(LifeSoundtrackService.generateLifeSoundtrack()) }
    var nextPrediction by remember { mutableStateOf<NextSongPrediction?>(null) }
    var rabbitHoleGraph by remember { mutableStateOf<RabbitHoleGraph?>(null) }
    var selectedRabbitHoleSeed by remember { mutableStateOf(currentTrack?.artist ?: "Arijit Singh") }
    var emotionalEqState by remember { mutableStateOf(EmotionalEqualizerState()) }
    var finishSongResult by remember { mutableStateOf<FinishMySongResult?>(null) }
    var dailyPuzzle by remember { mutableStateOf(MusicPuzzleEngine.getDailyPuzzle()) }
    var puzzleSolved by remember { mutableStateOf(false) }
    var puzzleGuessInput by remember { mutableStateOf("") }
    var culturalExplainer by remember { mutableStateOf(currentTrack?.let { CulturalExplainerService.explainSongCulture(it) }) }
    var compatibilityReport by remember { mutableStateOf<MusicCompatibilityResult?>(null) }
    var timeMachineData by remember { mutableStateOf<TimeMachineEraData?>(null) }
    var selectedYear by remember { mutableIntStateOf(2016) }
    var showCreateMemoryDialog by remember { mutableStateOf(false) }
    var newMemoryTitle by remember { mutableStateOf("") }
    var newMemoryDesc by remember { mutableStateOf("") }
    var newMemoryMood by remember { mutableStateOf("Soulful & Warm") }

    // Initial load
    LaunchedEffect(currentTrack?.id) {
        if (currentTrack != null) {
            culturalExplainer = CulturalExplainerService.explainSongCulture(currentTrack)
            aiRepository.predictNextSong(currentTrack, playbackState.queue).onSuccess { nextPrediction = it }
            aiRepository.finishMySong(currentTrack).onSuccess { finishSongResult = it }
        }
    }

    LaunchedEffect(Unit) {
        aiRepository.exploreRabbitHole(selectedRabbitHoleSeed).onSuccess { rabbitHoleGraph = it }
        aiRepository.travelTimeMachine(selectedYear).onSuccess { timeMachineData = it }
        aiRepository.calculateMusicCompatibility("You", "Best Friend").onSuccess { compatibilityReport = it }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F071D),
                        Color(0xFF140B26),
                        Color(0xFF090412)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Music Intelligence",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Zynera understands why you want music",
                        fontSize = 11.sp,
                        color = AccentCyan
                    )
                }

                IconButton(onClick = { showCreateMemoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "Remember This Moment",
                        tint = SpotifyGreen
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // -------------------------------------------------------------
                // 1. PREDICT MY NEXT SONG (Live Predictor Pill)
                // -------------------------------------------------------------
                item {
                    nextPrediction?.let { pred ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF22153D)),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🔮", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Predict My Next Song",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = "${(pred.confidence * 100).toInt()}% Confidence",
                                        color = AccentCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = pred.reason,
                                    fontSize = 12.sp,
                                    color = Color(0xFFD1D5DB)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = pred.predictedTrack.coverUrl.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600" },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pred.predictedTrack.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = pred.predictedTrack.artist,
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            playbackViewModel.playTrack(pred.predictedTrack, listOf(pred.predictedTrack), "Predicted Track")
                                            Toast.makeText(context, "Playing prediction: ${pred.predictedTrack.title}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text("Play Next", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 2. EMOTIONAL EQUALIZER (Feature 7)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎚️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Emotional Equalizer",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        val tuned = aiRepository.tuneQueueWithEmotionalEqualizer(
                                            playbackState.queue,
                                            currentTrack,
                                            emotionalEqState
                                        )
                                        Toast.makeText(context, tuned.explanation, Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Apply to Queue", color = SpotifyGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = "Adjust emotional vectors to continuously reshape your musical trajectory.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA78BFA)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            EqSlider("Energy", emotionalEqState.energy, AccentPink) { emotionalEqState = emotionalEqState.copy(energy = it) }
                            EqSlider("Happiness", emotionalEqState.happiness, AccentGold) { emotionalEqState = emotionalEqState.copy(happiness = it) }
                            EqSlider("Nostalgia", emotionalEqState.nostalgia, AccentCyan) { emotionalEqState = emotionalEqState.copy(nostalgia = it) }
                            EqSlider("Romance", emotionalEqState.romance, Color(0xFFEC4899)) { emotionalEqState = emotionalEqState.copy(romance = it) }
                            EqSlider("Discovery", emotionalEqState.discovery, SpotifyGreen) { emotionalEqState = emotionalEqState.copy(discovery = it) }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. MUSIC MEMORY — "Remember This Moment" (Feature 1)
                // -------------------------------------------------------------
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🧠", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Music Memories",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            TextButton(onClick = { showCreateMemoryDialog = true }) {
                                Text("+ Remember Now", color = SpotifyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (memories.isEmpty()) {
                            Text(
                                text = "No memories saved yet. Tap '+ Remember Now' to attach songs to your life moments.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(memories) { mem ->
                                    MemoryCard(
                                        memory = mem,
                                        onPlay = {
                                            MusicMemoryService.incrementReplayCount(context, mem.id)
                                            if (mem.tracks.isNotEmpty()) {
                                                playbackViewModel.playTrack(mem.tracks.first(), mem.tracks, "Memory: ${mem.title}")
                                                Toast.makeText(context, "Playing memory: ${mem.title}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 4. SOUNDTRACK MY LIFE (Feature 2)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎬", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Soundtrack My Life",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Era: ${lifeSoundtrack.currentEra.eraTitle}",
                                color = AccentCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = lifeSoundtrack.currentEra.description,
                                color = Color(0xFFD1D5DB),
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(lifeSoundtrack.currentEra.primaryGenres) { genre ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = genre,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val tracks = lifeSoundtrack.currentEra.topTracks
                                    if (tracks.isNotEmpty()) {
                                        playbackViewModel.playTrack(tracks.first(), tracks, "Era: ${lifeSoundtrack.currentEra.eraTitle}")
                                        Toast.makeText(context, "Playing ${lifeSoundtrack.currentEra.eraTitle}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play My 2026 Soundtrack", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 5. MUSIC RABBIT HOLE (Feature 4)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🕳️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Music Rabbit Hole",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = "Deep Graph",
                                    color = Color(0xFF818CF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Discover lineage: Song → Artist → Producer → Composer → Genre → Roots",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            rabbitHoleGraph?.nodes?.forEach { node ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable {
                                            node.streamTrack?.let { tr ->
                                                playbackViewModel.playTrack(tr, listOf(tr), "Rabbit Hole: ${node.title}")
                                                Toast.makeText(context, "Playing ${tr.title}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = node.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(node.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(node.subtitle, color = Color(0xFFA78BFA), fontSize = 11.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        aiRepository.exploreRabbitHole(selectedRabbitHoleSeed, depth = 2)
                                            .onSuccess {
                                                rabbitHoleGraph = it
                                                Toast.makeText(context, "Dived deeper into $selectedRabbitHoleSeed roots", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Take Me Deeper ➔", color = AccentCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 6. FINISH MY SONG (Feature 9)
                // -------------------------------------------------------------
                item {
                    finishSongResult?.let { res ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🪄", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Finish My Song",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.matchExplanation,
                                    color = Color(0xFFC7D2FE),
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                res.continuationCandidates.take(3).forEach { tr ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .clickable {
                                                playbackViewModel.playTrack(tr, res.continuationCandidates, "Finish My Song")
                                                Toast.makeText(context, "Playing continuation: ${tr.title}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tr.title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                            Text(tr.artist, color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                        }
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SpotifyGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 7. TRANSLATE THE CULTURE (Feature 11)
                // -------------------------------------------------------------
                item {
                    culturalExplainer?.let { exp ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌎", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Translate the Culture",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${exp.trackTitle} — ${exp.artist}",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = exp.culturalContext,
                                    color = Color(0xFFE5E7EB),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                exp.expressions.forEach { expr ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(8.dp)
                                    ) {
                                        Text("“${expr.phrase}”", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Pronunciation: ${expr.pronunciation}", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                        Text("Meaning: ${expr.literalMeaning}", color = Color(0xFFA7F3D0), fontSize = 11.5.sp)
                                        Text(expr.culturalSignificance, color = Color(0xFFE5E7EB), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 8. MUSIC PUZZLE (Feature 10)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🧩", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily Music Puzzle",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dailyPuzzle.title,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = dailyPuzzle.description,
                                color = Color(0xFFD1D5DB),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (puzzleSolved) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF064E3B))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎉 Solved! Path: ${dailyPuzzle.solutionPath.joinToString(" ➔ ")}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                            } else {
                                OutlinedTextField(
                                    value = puzzleGuessInput,
                                    onValueChange = { puzzleGuessInput = it },
                                    label = { Text("Enter connecting artist/producer") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SpotifyGreen,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (aiRepository.verifyPuzzleGuess(dailyPuzzle, puzzleGuessInput)) {
                                            puzzleSolved = true
                                            Toast.makeText(context, "Correct! Unlocked reward playlist", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Not quite! Try: ${dailyPuzzle.hints.firstOrNull()}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Submit Connection", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 9. MUSIC TIME MACHINE (Feature 15)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🤯", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Music Time Machine",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(2016, 2013, 2007, 1998).forEach { year ->
                                    FilterChip(
                                        selected = selectedYear == year,
                                        onClick = {
                                            selectedYear = year
                                            scope.launch {
                                                aiRepository.travelTimeMachine(year).onSuccess { timeMachineData = it }
                                            }
                                        },
                                        label = { Text("$year", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentPink,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            timeMachineData?.let { data ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(data.eraTitle, color = AccentPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(data.description, color = Color(0xFFD1D5DB), fontSize = 11.5.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val tracks = data.chartbusters
                                        if (tracks.isNotEmpty()) {
                                            playbackViewModel.playTrack(tracks.first(), tracks, "Time Machine: ${data.year}")
                                            Toast.makeText(context, "Traveling to ${data.year}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Play ${data.year} Time Capsule", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 10. TEACH ZYNERA MY TASTE (Feature 14)
                // -------------------------------------------------------------
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🧠", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Teach Zynera My Taste",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Directly adjust the algorithmic recommendation weights.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            EqSlider("Familiarity", tasteControls.familiarity, AccentCyan) {
                                UserTastePreferencesStore.updateControls(context, tasteControls.copy(familiarity = it))
                            }
                            EqSlider("Discovery Bias", tasteControls.discovery, SpotifyGreen) {
                                UserTastePreferencesStore.updateControls(context, tasteControls.copy(discovery = it))
                            }
                            EqSlider("Mainstream vs Underground", tasteControls.mainstream, AccentGold) {
                                UserTastePreferencesStore.updateControls(context, tasteControls.copy(mainstream = it))
                            }
                            EqSlider("Experimental Tolerance", tasteControls.experimental, AccentPink) {
                                UserTastePreferencesStore.updateControls(context, tasteControls.copy(experimental = it))
                            }
                            EqSlider("Nostalgia Weight", tasteControls.nostalgia, Color(0xFF8B5CF6)) {
                                UserTastePreferencesStore.updateControls(context, tasteControls.copy(nostalgia = it))
                            }
                        }
                    }
                }
            }
        }

        // Create Memory Modal Dialog
        if (showCreateMemoryDialog) {
            AlertDialog(
                onDismissRequest = { showCreateMemoryDialog = false },
                title = { Text("Remember This Moment", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Save this listening moment with attached songs, mood, and description.",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = newMemoryTitle,
                            onValueChange = { newMemoryTitle = it },
                            label = { Text("Memory Title (e.g. Goa Road Trip)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newMemoryDesc,
                            onValueChange = { newMemoryDesc = it },
                            label = { Text("Description / Story") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newMemoryMood,
                            onValueChange = { newMemoryMood = it },
                            label = { Text("Mood / Vibe") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            MusicMemoryService.createMemory(
                                context = context,
                                title = newMemoryTitle,
                                description = newMemoryDesc,
                                currentTrack = currentTrack,
                                currentQueue = playbackState.queue,
                                mood = newMemoryMood,
                                tags = listOf("Moment", "Memory", newMemoryMood)
                            )
                            showCreateMemoryDialog = false
                            newMemoryTitle = ""
                            newMemoryDesc = ""
                            Toast.makeText(context, "Memory saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                    ) {
                        Text("Save Memory", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateMemoryDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E1430)
            )
        }
    }
}

@Composable
private fun EqSlider(
    label: String,
    value: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("${value.toInt()}%", color = activeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun MemoryCard(
    memory: MusicMemory,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .width(200.dp)
            .clickable { onPlay() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val cover = memory.tracks.firstOrNull()?.coverUrl?.ifBlank {
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"
            } ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"

            AsyncImage(
                model = cover,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memory.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = memory.mood,
                color = AccentCyan,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${memory.tracks.size} songs",
                    color = Color.Gray,
                    fontSize = 10.5.sp
                )
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(28.dp)
                        .background(SpotifyGreen, CircleShape)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
