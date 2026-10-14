package com.sonexa.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sonexa.app.audio.VoiceCommand
import com.sonexa.app.audio.VoiceCommandParser
import com.sonexa.app.audio.VoiceSearchManager
import com.sonexa.app.audio.VoiceSearchUiState
import com.sonexa.app.audio.ZyneraTtsManager
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.repository.AudiobookRepository
import com.sonexa.app.data.repository.RecommendationRepository
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Alexa-Grade Voice Assistant for Zynera.
 * Provides true 1-click instant listening:
 * Tap mic once -> speaks -> command executed directly on the audio engine with voice feedback!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZyneraVoiceAssistantSheet(
    onDismiss: () -> Unit,
    playbackViewModel: PlaybackViewModel? = null,
    searchViewModel: SearchViewModel? = null,
    onFallbackTextSearch: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val voiceManager = remember { VoiceSearchManager(context) }
    val ttsManager = remember { ZyneraTtsManager(context) }
    val musicEngine = remember { com.sonexa.app.data.provider.MusicAggregationEngine() }

    val uiState by voiceManager.uiState.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()

    var selectedLangCode by remember { mutableStateOf("en-IN") }
    var spokenResponseText by remember { mutableStateOf<String?>(null) }
    var isExecutingAction by remember { mutableStateOf(false) }
    var textFallbackInput by remember { mutableStateOf("") }
    var showTextInputField by remember { mutableStateOf(false) }

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Single permission launcher: immediately starts listening upon granting!
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (granted) {
            voiceManager.startListening(selectedLangCode)
        } else {
            spokenResponseText = "Microphone permission is required for voice control."
        }
    }

    // ── Instant 1-Click Activation ───────────────────────────────────────────
    LaunchedEffect(Unit) {
        if (hasRecordPermission) {
            voiceManager.startListening(selectedLangCode)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
            ttsManager.shutdown()
        }
    }

    // Function to execute parsed commands directly on player & speak back
    fun executeParsedCommand(rawText: String) {
        if (isExecutingAction || rawText.isBlank()) return
        isExecutingAction = true

        val parsed = VoiceCommandParser.parse(rawText)
        spokenResponseText = parsed.spokenConfirmation

        // Speak audio feedback
        ttsManager.speak(parsed.spokenConfirmation)

        coroutineScope.launch {
            when (val cmd = parsed.command) {
                is VoiceCommand.Pause -> {
                    if (playbackViewModel?.uiState?.value?.isPlaying == true) {
                        playbackViewModel.togglePlayPause()
                    }
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.Resume -> {
                    if (playbackViewModel?.uiState?.value?.isPlaying == false) {
                        playbackViewModel.togglePlayPause()
                    }
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.Next -> {
                    playbackViewModel?.skipNext()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.Previous -> {
                    playbackViewModel?.skipPrevious()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.Shuffle -> {
                    playbackViewModel?.toggleShuffle()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.Repeat -> {
                    playbackViewModel?.cycleRepeatMode()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.LikeTrack -> {
                    playbackViewModel?.toggleLike()
                    delay(1400)
                    onDismiss()
                }

                is VoiceCommand.SetPlaybackSpeed -> {
                    playbackViewModel?.setPlaybackSpeed(cmd.speed)
                    delay(1400)
                    onDismiss()
                }

                is VoiceCommand.SetSleepTimer -> {
                    playbackViewModel?.setSleepTimerMinutes(cmd.minutes)
                    delay(1400)
                    onDismiss()
                }

                is VoiceCommand.NextChapter -> {
                    playbackViewModel?.nextChapter()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.PreviousChapter -> {
                    playbackViewModel?.previousChapter()
                    delay(1200)
                    onDismiss()
                }

                is VoiceCommand.SaveBookmark -> {
                    val pvm = playbackViewModel
                    val book = pvm?.uiState?.value?.activeAudiobook
                    if (book != null) {
                        val activeIdx = pvm.uiState.value.activeChapterIndex
                        val ch = book.chapters.getOrNull(activeIdx)
                        if (ch != null) {
                            val posSeconds = pvm.elapsedMs.value / 1000L
                            AudiobookStore.addBookmark(
                                audiobookId = book.id,
                                chapterId = ch.id,
                                chapterNumber = ch.chapterNumber,
                                positionSeconds = posSeconds,
                                note = "Voice bookmark"
                            )
                        }
                    }
                    delay(1400)
                    onDismiss()
                }

                is VoiceCommand.PlayAudiobook -> {
                    val repo = AudiobookRepository(context)
                    val booksResult = repo.getAudiobooks()
                    val books = booksResult.getOrNull().orEmpty()
                    val targetBook = books.firstOrNull { b ->
                        b.title.contains(cmd.bookTitle, ignoreCase = true) ||
                        cmd.bookTitle.contains(b.title, ignoreCase = true)
                    } ?: books.firstOrNull()

                    if (targetBook != null && playbackViewModel != null) {
                        playbackViewModel.playAudiobook(targetBook, 0, 0L)
                    }
                    delay(1600)
                    onDismiss()
                }

                is VoiceCommand.PlayTrack,
                is VoiceCommand.PlayArtist,
                is VoiceCommand.PlayAlbum,
                is VoiceCommand.PlayPlaylist,
                is VoiceCommand.PlayPodcast,
                is VoiceCommand.SearchContent -> {
                    val searchQuery = when (cmd) {
                        is VoiceCommand.PlayTrack -> cmd.trackTitle
                        is VoiceCommand.PlayArtist -> cmd.artistName
                        is VoiceCommand.PlayAlbum -> cmd.albumTitle
                        is VoiceCommand.PlayPlaylist -> cmd.playlistName
                        is VoiceCommand.PlayPodcast -> cmd.podcastTitle
                        is VoiceCommand.SearchContent -> cmd.query
                        else -> rawText
                    }

                    // Query real catalog tracks across providers (JioSaavn, Audius, Sonexa, Jamendo, Deezer)
                    val results = musicEngine.searchAll(searchQuery).tracks
                    if (results.isNotEmpty() && playbackViewModel != null) {
                        playbackViewModel.playQueue(results, 0, "Voice: $searchQuery")
                        delay(1600)
                        onDismiss()
                    } else {
                        // Fallback to top trending or direct search navigation
                        searchViewModel?.onSearchQueryChanged(searchQuery)
                        onFallbackTextSearch?.invoke(searchQuery)
                        delay(1400)
                        onDismiss()
                    }
                }

                is VoiceCommand.Unsupported -> {
                    delay(2000)
                    isExecutingAction = false
                }
            }
        }
    }

    // Auto-execute upon speech recognition success
    LaunchedEffect(uiState) {
        if (uiState is VoiceSearchUiState.Success) {
            val transcript = (uiState as VoiceSearchUiState.Success).transcript
            executeParsedCommand(transcript)
        }
    }

    // Alexa Pulse & Dynamic Glowing Halo
    val infiniteTransition = rememberInfiniteTransition(label = "alexa_ring")
    val pulseHalo by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState is VoiceSearchUiState.Listening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF10091D),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZYNERA VOICE AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFFC084FC)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    "English (IN)" to "en-IN",
                    "English (US)" to "en-US",
                    "Hindi (हिन्दी)" to "hi-IN"
                ).forEach { (name, code) ->
                    val isSelected = selectedLangCode == code
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.08f))
                            .border(1.dp, if (isSelected) Color(0xFFC084FC) else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable {
                                selectedLangCode = code
                                if (hasRecordPermission) {
                                    voiceManager.startListening(code)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFAFA9BB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Luminous Alexa-Style Center Voice Aura ───────────────────────
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Cyan-Violet Glowing Halo
                Box(
                    modifier = Modifier
                        .size((125 * (1f + rmsLevel * 0.45f)).dp)
                        .scale(pulseHalo)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.25f),
                                    Color(0xFFA855F7).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle Ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFFA855F7),
                                    Color(0xFFFF2D55),
                                    Color(0xFF00E5FF)
                                )
                            )
                        )
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF120B20))
                )

                // Center Mic Core
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(20.dp, CircleShape, ambientColor = Color(0xFFA855F7), spotColor = Color(0xFF00E5FF))
                        .clip(CircleShape)
                        .background(
                            if (uiState is VoiceSearchUiState.Listening) {
                                Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF6366F1)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF24163E), Color(0xFF180E2B)))
                            }
                        )
                        .clickable {
                            if (hasRecordPermission) {
                                voiceManager.startListening(selectedLangCode)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Live Speech Status & Audio Response ──────────────────────────
            val responseText = spokenResponseText
            if (responseText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1338))
                        .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = responseText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE9D5FF),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                when (val state = uiState) {
                    is VoiceSearchUiState.Listening -> {
                        Text(
                            text = if (state.partialText.isBlank()) "Listening... Speak now" else "\"${state.partialText}\"",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (state.partialText.isBlank()) Color(0xFFC4B5FD) else Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is VoiceSearchUiState.Processing -> {
                        Text(
                            text = "Understanding: \"${state.finalText}\"",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00E5FF),
                            textAlign = TextAlign.Center
                        )
                    }

                    is VoiceSearchUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                fontSize = 13.5.sp,
                                color = Color(0xFFF87171),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (hasRecordPermission) {
                                        voiceManager.startListening(selectedLangCode)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Speak Again", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "Tap mic to speak",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Suggested Voice Commands Chips ───────────────────────────────
            Text(
                text = "TRY SAYING:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = Color(0xFF8E889E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    "Play Kesariya",
                    "Romantic Hindi",
                    "Resume Audiobook",
                    "Sleep Timer 30m"
                ).forEach { samplePrompt ->
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { executeParsedCommand(samplePrompt) }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = samplePrompt,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Input Fallback Toggle
            if (!showTextInputField) {
                Text(
                    text = "Or type your request",
                    fontSize = 11.5.sp,
                    color = Color(0xFFA855F7),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showTextInputField = true }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textFallbackInput,
                        onValueChange = { textFallbackInput = it },
                        placeholder = { Text("Type e.g. Play Kesariya", fontSize = 13.sp, color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (textFallbackInput.isNotBlank()) {
                                executeParsedCommand(textFallbackInput)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Submit", tint = Color(0xFFA855F7))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
