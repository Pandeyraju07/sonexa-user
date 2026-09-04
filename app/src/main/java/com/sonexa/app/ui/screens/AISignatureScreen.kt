package com.sonexa.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.viewmodel.AiSignatureUiState
import com.sonexa.app.ui.viewmodel.AiSignatureViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PlaybackUiState

data class AITool(val name: String, val tag: String, val icon: ImageVector, val color1: Color, val color2: Color)

@Composable
fun AISignatureScreen(
    onNavigateBack: () -> Unit,
    aiViewModel: AiSignatureViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiState by aiViewModel.uiState.collectAsState()
    val playbackState by (playbackViewModel?.uiState ?: remember { kotlinx.coroutines.flow.MutableStateFlow(PlaybackUiState()) }).collectAsState()
    var activeTool by remember { mutableStateOf("Change The Vibe") }
    var promptInput by remember { mutableStateOf("") }
    var showChatModal by remember { mutableStateOf(false) }
    var showVibeSheet by remember { mutableStateOf(false) }

    val aiTools = listOf(
        AITool("Change The Vibe", "AI Vibe Studio", Icons.Default.Bolt, Color(0xFFF59E0B), Color(0xFFEF4444)),
        AITool("AI DJ", "Live Curation", Icons.Default.GraphicEq, Color(0xFF6B3CE9), Color(0xFF9825DD)),
        AITool("AI Chat", "Music Assistant", Icons.Default.ChatBubble, Color(0xFFE534B2), Color(0xFFFF52C4)),
        AITool("Mood Scanner", "Camera Mood", Icons.Default.Camera, Color(0xFF06B6D4), Color(0xFF3B82F6)),
        AITool("Playlist Gen", "Prompt to List", Icons.Default.AutoAwesome, Color(0xFFF59E0B), Color(0xFFEF4444)),
        AITool("Song Meaning", "Lyrics Intelligence", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF8B5CF6), Color(0xFFEC4899)),
        AITool("Podcast Summarizer", "AI Highlights", Icons.Default.Podcasts, Color(0xFF10B981), Color(0xFF059669)),
        AITool("Workout Coach", "Pace Sync", Icons.Default.FitnessCenter, Color(0xFFDC2626), Color(0xFF991B1B)),
        AITool("Sleep Assistant", "Rain & Binaural", Icons.Default.Bedtime, Color(0xFF4C1D95), Color(0xFF6D28D9)),
        AITool("Focus Mode", "Deep Work", Icons.Default.Psychology, Color(0xFF0284C7), Color(0xFF0369A1)),
        AITool("Driving Mode", "Voice Hands-Free", Icons.Default.DirectionsCar, Color(0xFFD97706), Color(0xFFB45309)),
        AITool("Cover Generator", "AI Playlist Art", Icons.Default.Image, Color(0xFFEC4899), Color(0xFFBE185D)),
        AITool("Voice Assistant", "Speech Control", Icons.Default.Mic, Color(0xFF9333EA), Color(0xFF7E22CE))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Signature Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Zynera AI Hub",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC084FC)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE534B2),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SonexaInputBg)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SonexaTextWhite, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Next-gen music intelligence & AI tools suite",
                fontSize = 13.sp,
                color = SonexaTextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Prompt Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SonexaInputField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholderText = "Ask AI: 'Generate a late night synthwave playlist'",
                    leadingIcon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SonexaGradientBrush)
                        .clickable {
                            if (promptInput.isBlank()) {
                                Toast.makeText(context, "Enter a prompt like 'Generate lo-fi rain beats'", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "AI generating: '$promptInput'", Toast.LENGTH_SHORT).show()
                                promptInput = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send Prompt", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid of 12 Signature AI Tools
            Text(text = "Signature AI Tools (12 Features)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(aiTools) { tool ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SonexaInputBg)
                            .border(1.dp, SonexaInputBorder, RoundedCornerShape(18.dp))
                            .clickable {
                                activeTool = tool.name
                                when (tool.name) {
                                    "Change The Vibe", "AI DJ" -> {
                                        showVibeSheet = true
                                    }
                                    "AI Chat" -> {
                                        showChatModal = true
                                    }
                                    else -> {
                                        Toast.makeText(context, "Activated ${tool.name} mode!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(tool.color1, tool.color2))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = tool.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(text = tool.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                    Text(text = tool.tag, fontSize = 12.sp, color = SonexaTextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x309825DD))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Open", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SonexaPurpleLight)
                            }
                        }
                    }
                }
            }
        }

        // AI Chat Conversation Modal
        if (showChatModal) {
            AlertDialog(
                onDismissRequest = { showChatModal = false },
                containerColor = SonexaCardDark,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFFE534B2), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Ask Zynera AI Assistant", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }
                },
                text = {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SonexaInputBg)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🤖 AI: 'Hello Dhiraj! I am your Zynera Music Assistant. What mood or genre would you like to explore today?'",
                                fontSize = 13.sp,
                                color = SonexaTextWhite,
                                lineHeight = 18.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    SonexaGradientButton(
                        text = "Generate AI Mix",
                        onClick = {
                            showChatModal = false
                            aiViewModel.generateSignature(
                                mood = activeTool,
                                prompt = promptInput.ifBlank { "Create a custom vibe mix" }
                            )
                            Toast.makeText(context, "Generating AI Signature…", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showChatModal = false }) {
                        Text(text = "Close", color = SonexaTextMuted)
                    }
                }
            )
        }

        // Change The Vibe Studio Modal
        if (showVibeSheet) {
            com.sonexa.app.ui.components.ChangeVibeBottomSheet(
                onDismiss = { showVibeSheet = false },
                currentTrack = playbackState.track,
                currentQueue = playbackState.queue,
                onApplyVibe = { newQueue, vibeTitle ->
                    if (newQueue.isNotEmpty()) {
                        playbackViewModel?.playQueue(newQueue, 0, "Vibe: $vibeTitle")
                    }
                }
            )
        }
    }
}
