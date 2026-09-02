package com.sonexa.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.sonexa.app.audio.VoiceSearchManager
import com.sonexa.app.audio.VoiceSearchUiState
import com.sonexa.app.data.model.VoiceSearchResponseDto
import com.sonexa.app.ui.theme.SpotifyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSearchBottomSheet(
    onDismiss: () -> Unit,
    onVoiceResult: (VoiceSearchResponseDto) -> Unit,
    onDirectSearch: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceSearchManager(context) }
    val uiState by voiceManager.uiState.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()

    var selectedLangCode by remember { mutableStateOf("en-US") }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // System Speech Recognizer Activity Launcher Fallback
    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcript = spoken?.firstOrNull()?.trim().orEmpty()
            if (transcript.isNotBlank()) {
                onDirectSearch(transcript)
                onDismiss()
            }
        }
    }

    fun launchSystemVoiceDialog() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLangCode)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a song, artist, or vibe...")
            }
            systemSpeechLauncher.launch(intent)
        } catch (e: Exception) {
            // If system speech dialog is completely absent
            onDismiss()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (granted) {
            voiceManager.startListening(selectedLangCode)
        } else {
            launchSystemVoiceDialog()
        }
    }

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
        }
    }

    // React to Success
    LaunchedEffect(uiState) {
        if (uiState is VoiceSearchUiState.Success) {
            val transcript = (uiState as VoiceSearchUiState.Success).transcript
            onDirectSearch(transcript)
            onDismiss()
        }
    }

    // Pulsing Animation for Mic
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState is VoiceSearchUiState.Listening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF140E20),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Voice Assistant",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(0.6f))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Language Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("English" to "en-US", "Hindi" to "hi-IN", "Punjabi" to "pa-IN").forEach { (name, code) ->
                    val isSelected = selectedLangCode == code
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f))
                            .clickable {
                                selectedLangCode = code
                                if (hasRecordPermission) {
                                    voiceManager.startListening(code)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Waveform & Animated Microphone Ring
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse Halo
                Box(
                    modifier = Modifier
                        .size((110 * (1f + rmsLevel * 0.45f)).dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(SpotifyGreen.copy(alpha = 0.15f))
                )

                // Mid Glow Ring
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SpotifyGreen.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                // Core Mic Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape, ambientColor = SpotifyGreen, spotColor = SpotifyGreen)
                        .clip(CircleShape)
                        .background(if (uiState is VoiceSearchUiState.Listening) SpotifyGreen else Color(0xFF2A1E40))
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
                        contentDescription = "Microphone",
                        tint = if (uiState is VoiceSearchUiState.Listening) Color.Black else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Status & Live Transcription
            when (val state = uiState) {
                is VoiceSearchUiState.Listening -> {
                    Text(
                        text = if (state.partialText.isBlank()) "Listening..." else "\"${state.partialText}\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.partialText.isBlank()) Color(0xFFC4B5FD) else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is VoiceSearchUiState.Processing -> {
                    Text(
                        text = "Searching: \"${state.finalText}\"",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpotifyGreen,
                        textAlign = TextAlign.Center
                    )
                }
                is VoiceSearchUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF87171),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Retry in-app
                            Button(
                                onClick = {
                                    if (hasRecordPermission) {
                                        voiceManager.startListening(selectedLangCode)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Try Again", color = Color.White, fontSize = 13.sp)
                            }

                            // System Voice Fallback Button
                            Button(
                                onClick = { launchSystemVoiceDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use System Voice", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Tap microphone to speak",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Suggestion Chips
            Text(
                text = "Or tap to play immediately:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    "Romantic Hindi songs",
                    "Gym workout hits",
                    "Acoustic relax"
                ).forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable {
                                onDirectSearch(prompt)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = Color(0xFFDDD6FE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}