package com.sonexa.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.data.provider.HybridRecommendationEngine
import com.sonexa.app.ui.theme.*
import kotlinx.coroutines.launch

data class VibeOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val defaultEnergy: Float,
    val defaultAcoustic: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeVibeBottomSheet(
    onDismiss: () -> Unit,
    currentTrack: TrackDto? = null,
    currentQueue: List<TrackDto> = emptyList(),
    onApplyVibe: (List<TrackDto>, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recommendationEngine = remember { HybridRecommendationEngine() }

    val vibes = remember {
        listOf(
            VibeOption(
                id = "MORE_ENERGETIC",
                title = "Beast Mode",
                subtitle = "High Energy",
                description = "Pumping beats, high tempo & gym motivation",
                icon = Icons.Rounded.Bolt,
                color = Color(0xFFF59E0B),
                defaultEnergy = 0.90f,
                defaultAcoustic = 0.15f
            ),
            VibeOption(
                id = "MORE_RELAXING",
                title = "Deep Chill",
                subtitle = "Calm & Lo-Fi",
                description = "Soothing acoustic tones & mellow beats",
                icon = Icons.Rounded.Spa,
                color = Color(0xFF10B981),
                defaultEnergy = 0.30f,
                defaultAcoustic = 0.80f
            ),
            VibeOption(
                id = "MORE_ROMANTIC",
                title = "Pure Romance",
                subtitle = "Heartfelt Love",
                description = "Passionate love ballads & acoustic intimacy",
                icon = Icons.Rounded.Favorite,
                color = Color(0xFFEC4899),
                defaultEnergy = 0.50f,
                defaultAcoustic = 0.70f
            ),
            VibeOption(
                id = "MORE_PARTY",
                title = "Party Anthem",
                subtitle = "Dance & EDM",
                description = "Club bangers, dance drops & remix hits",
                icon = Icons.Rounded.Celebration,
                color = Color(0xFF8B5CF6),
                defaultEnergy = 0.95f,
                defaultAcoustic = 0.10f
            ),
            VibeOption(
                id = "MORE_ACOUSTIC",
                title = "Unplugged",
                subtitle = "Acoustic Live",
                description = "Organic strings, guitar & raw vocal warmth",
                icon = Icons.Rounded.MusicNote,
                color = Color(0xFF06B6D4),
                defaultEnergy = 0.40f,
                defaultAcoustic = 0.95f
            ),
            VibeOption(
                id = "MORE_MELANCHOLIC",
                title = "Soulful Sad",
                subtitle = "Emotional Depth",
                description = "Soul-stirring lyrics, moody strings & dard",
                icon = Icons.Rounded.Nightlight,
                color = Color(0xFF6366F1),
                defaultEnergy = 0.35f,
                defaultAcoustic = 0.65f
            ),
            VibeOption(
                id = "DEEP_FOCUS",
                title = "Alpha Focus",
                subtitle = "Study & Work",
                description = "Ambient soundscapes & zero distraction flow",
                icon = Icons.Rounded.Headphones,
                color = Color(0xFF3B82F6),
                defaultEnergy = 0.45f,
                defaultAcoustic = 0.50f
            ),
            VibeOption(
                id = "NOSTALGIA",
                title = "Retro Golden",
                subtitle = "90s / 2000s Hits",
                description = "Timeless classics & evergreen melodies",
                icon = Icons.Rounded.Radio,
                color = Color(0xFFD97706),
                defaultEnergy = 0.60f,
                defaultAcoustic = 0.60f
            ),
            VibeOption(
                id = "SURPRISE",
                title = "Deep Discovery",
                subtitle = "Fresh & Emerging",
                description = "Hidden gems & new indie discoveries",
                icon = Icons.Rounded.AutoAwesome,
                color = Color(0xFF14B8A6),
                defaultEnergy = 0.65f,
                defaultAcoustic = 0.45f
            )
        )
    }

    var selectedVibe by remember { mutableStateOf(vibes.first()) }
    var energyLevel by remember { mutableStateOf(selectedVibe.defaultEnergy) }
    var acousticRatio by remember { mutableStateOf(selectedVibe.defaultAcoustic) }
    var previewQueue by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var isTransforming by remember { mutableStateOf(false) }

    fun refreshVibeQueue(vibe: VibeOption) {
        coroutineScope.launch {
            isTransforming = true
            try {
                val reordered = recommendationEngine.changeVibe(
                    vibe = vibe.id,
                    currentQueue = currentQueue,
                    currentTrack = currentTrack
                )
                previewQueue = reordered
            } catch (_: Exception) {
                previewQueue = currentQueue
            } finally {
                isTransforming = false
            }
        }
    }

    LaunchedEffect(selectedVibe) {
        refreshVibeQueue(selectedVibe)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D0A1A),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with AI Glow Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF6B3CE9), Color(0xFFE534B2))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI VIBE STUDIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Change The Vibe",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Instantly re-tune tempo, mood and acoustic energy",
                            fontSize = 13.sp,
                            color = SonexaTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SonexaInputBg)
                            .border(1.dp, SonexaCardBorder, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Currently Playing Seed Track Mini Card
            if (currentTrack != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SonexaInputBg)
                            .border(1.dp, SonexaCardBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentTrack.effectiveCoverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentTrack.title,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE SEED TRACK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonexaPurpleLight,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = currentTrack.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack.artist,
                                fontSize = 12.sp,
                                color = SonexaTextMuted,
                                maxLines = 1
                            )
                        }

                        // Equalizer Wave Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            repeat(4) { i ->
                                val infiniteTransition = rememberInfiniteTransition(label = "eq_$i")
                                val height by infiniteTransition.animateFloat(
                                    initialValue = 6f,
                                    targetValue = 20f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 350 + (i * 120), easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "bar_$i"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(height.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(selectedVibe.color)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Vibe Presets Grid
            item {
                Text(
                    text = "Select Target Vibe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    items(vibes) { vibe ->
                        val isSelected = selectedVibe.id == vibe.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) vibe.color.copy(alpha = 0.18f) else SonexaInputBg
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) vibe.color else SonexaCardBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedVibe = vibe
                                    energyLevel = vibe.defaultEnergy
                                    acousticRatio = vibe.defaultAcoustic
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(vibe.color.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = vibe.icon,
                                            contentDescription = vibe.title,
                                            tint = vibe.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(vibe.color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = vibe.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = vibe.subtitle,
                                    fontSize = 11.sp,
                                    color = if (isSelected) vibe.color else SonexaTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = vibe.description,
                                    fontSize = 10.sp,
                                    color = SonexaTextSubtle,
                                    maxLines = 2,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Interactive Fine-Tuning Sliders
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SonexaInputBg)
                        .border(1.dp, SonexaCardBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Vibe Energy & Timbre Tuning",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Energy Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Energy Output", fontSize = 12.sp, color = SonexaTextMuted)
                        Text(
                            text = "${(energyLevel * 100).toInt()}% • ${if (energyLevel > 0.7f) "High Energy" else if (energyLevel < 0.4f) "Calm" else "Balanced"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedVibe.color
                        )
                    }
                    Slider(
                        value = energyLevel,
                        onValueChange = {
                            energyLevel = it
                        },
                        onValueChangeFinished = {
                            refreshVibeQueue(selectedVibe)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = selectedVibe.color,
                            activeTrackColor = selectedVibe.color,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Acousticness Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Acoustic vs Electronic", fontSize = 12.sp, color = SonexaTextMuted)
                        Text(
                            text = if (acousticRatio > 0.6f) "Organic Acoustic" else "Electronic Synth",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonexaPurpleLight
                        )
                    }
                    Slider(
                        value = acousticRatio,
                        onValueChange = {
                            acousticRatio = it
                        },
                        onValueChangeFinished = {
                            refreshVibeQueue(selectedVibe)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = SonexaPurpleLight,
                            activeTrackColor = SonexaPurpleLight,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        )
                    )
                }
            }

            // 5. Live Queue Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vibe Flow Queue Preview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isTransforming) {
                        CircularProgressIndicator(
                            color = selectedVibe.color,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "${previewQueue.size} tracks adapted",
                            fontSize = 12.sp,
                            color = selectedVibe.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (previewQueue.isNotEmpty()) {
                itemsIndexed(previewQueue.take(8)) { index, tr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SonexaInputBg.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonexaTextMuted,
                            modifier = Modifier.width(22.dp)
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tr.effectiveCoverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = tr.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (tr.versionType != "Original") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(selectedVibe.color.copy(alpha = 0.35f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = tr.versionType, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = tr.artist,
                                    fontSize = 11.sp,
                                    color = SonexaTextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        // Energy Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(selectedVibe.color.copy(alpha = 0.20f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${(tr.energy * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedVibe.color
                            )
                        }
                    }
                }
            }

            // 6. Action Button: Apply & Play Vibe Flow
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6B3CE9), Color(0xFFE534B2), selectedVibe.color)
                            )
                        )
                        .clickable {
                            val appliedList = if (previewQueue.isNotEmpty()) previewQueue else currentQueue
                            onApplyVibe(appliedList, selectedVibe.title)
                            Toast.makeText(context, "Vibe applied: ${selectedVibe.title}", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Apply & Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Apply & Play ${selectedVibe.title} Flow",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}