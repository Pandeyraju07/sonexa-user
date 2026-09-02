package com.sonexa.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.theme.SpotifyGreen
import com.sonexa.app.ui.viewmodel.AiIntelligenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicDnaScreen(
    onBack: () -> Unit,
    aiViewModel: AiIntelligenceViewModel = viewModel()
) {
    val uiState by aiViewModel.uiState.collectAsState()
    val dna = uiState.musicDna
    val insights = uiState.insights

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Music DNA", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0A1C))
            )
        },
        containerColor = Color(0xFF0F0A1C)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HERO PERSONALITY CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.20f))
                            .border(2.dp, SpotifyGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dna?.personality ?: "The Sonic Explorer",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dna?.summaryText ?: "Your music journey is driven by deep emotional resonance and diverse acoustic soundscapes.",
                        fontSize = 13.sp,
                        color = Color(0xFFDDD6FE),
                        lineHeight = 18.sp
                    )
                }
            }

            // 2. 5-DIMENSION LISTENING METRICS
            Text(
                text = "Taste Spectrum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF171126))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DnaMetricRow("Energy & Intensity", dna?.energy ?: 72, Color(0xFFF59E0B))
                DnaMetricRow("Discovery & Novelty", dna?.discovery ?: 64, SpotifyGreen)
                DnaMetricRow("Nostalgia & Classics", dna?.nostalgia ?: 81, Color(0xFF8B5CF6))
                DnaMetricRow("Romance & Emotion", dna?.romance ?: 58, Color(0xFFEC4899))
                DnaMetricRow("Mainstream vs Indie", dna?.mainstream ?: 42, Color(0xFF06B6D4))
            }

            // 3. LISTENING STATS OVERVIEW
            Text(
                text = "Listening Intelligence",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF171126))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("Total Minutes", fontSize = 11.sp, color = Color(0xFFAFA9BB))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${insights?.totalMinutes ?: 3640} min", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF171126))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("Peak Hours", fontSize = 11.sp, color = Color(0xFFAFA9BB))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(insights?.peakListeningHour ?: "10 PM - 1 AM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SpotifyGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DnaMetricRow(label: String, value: Int, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "metric_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(text = "$value%", fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0.01f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}