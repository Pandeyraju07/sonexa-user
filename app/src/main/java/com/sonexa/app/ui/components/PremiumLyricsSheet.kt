package com.sonexa.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.data.model.LyricsLineDto
import com.sonexa.app.data.model.LyricsResponse
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextSubtle
import com.sonexa.app.ui.theme.SonexaTextWhite
import kotlin.math.abs
private val LyricsBg = Color(0xFF07060C)
private val LyricsSurface = Color(0xFF12101A)
private val LyricsAccent = Color(0xFFC9A0FF)
private val LyricsDim = Color(0xFF6E687A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumLyricsSheet(
    trackTitle: String,
    trackArtist: String,
    positionMs: Long,
    lyrics: LyricsResponse?,
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSeekToLine: ((Long) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lines = remember(lyrics) {
        when {
            lyrics == null -> emptyList()
            lyrics.lines.isNotEmpty() -> lyrics.lines
            lyrics.plainText.isNotBlank() -> lyrics.plainText
                .split('\n')
                .mapIndexed { i, t -> LyricsLineDto(tMs = i * 3000L, text = t.trim()) }
                .filter { it.text.isNotBlank() }
            else -> emptyList()
        }
    }
    val synced = lyrics?.synced == true && lyrics.lines.isNotEmpty()
    val activeIndex = if (synced && lines.isNotEmpty()) {
        lines.indexOfLast { it.tMs <= positionMs }.coerceAtLeast(0)
    } else -1

    val listState = rememberLazyListState()
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            val target = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LyricsBg,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
        ) {
            // Soft ambient wash
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x339825DD), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LyricsSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = LyricsAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackTitle,
                            color = SonexaTextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = trackArtist,
                                color = SonexaTextSubtle,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            val badge = sourceBadge(lyrics)
                            if (badge != null) {
                                Text("  ·  ", color = SonexaTextSubtle, fontSize = 12.sp)
                                Text(badge, color = LyricsAccent.copy(alpha = 0.9f), fontSize = 11.sp)
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SonexaTextMuted)
                    }
                }

                Spacer(Modifier.height(4.dp))

                AnimatedContent(
                    targetState = when {
                        loading -> LyricsUiState.Loading
                        error != null -> LyricsUiState.Error
                        lines.isEmpty() -> LyricsUiState.Empty
                        else -> LyricsUiState.Ready
                    },
                    transitionSpec = {
                        fadeIn(tween(280, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(tween(180))
                    },
                    label = "lyricsState"
                ) { state ->
                    when (state) {
                        LyricsUiState.Loading -> LyricsLoadingState()
                        LyricsUiState.Error -> LyricsMessageState(
                            title = "Couldn’t load lyrics",
                            body = error ?: "Please try again"
                        )
                        LyricsUiState.Empty -> LyricsMessageState(
                            title = "No lyrics yet",
                            body = lyrics?.plainText?.takeIf { it.isNotBlank() }
                                ?: "Synced lyrics will appear when a match is found"
                        )
                        LyricsUiState.Ready -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = 28.dp,
                                        end = 28.dp,
                                        top = 48.dp,
                                        bottom = 120.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(lines, key = { i, line -> "${line.tMs}_$i" }) { index, line ->
                                        PremiumLyricLine(
                                            text = line.text,
                                            active = index == activeIndex,
                                            near = synced && abs(index - activeIndex) == 1,
                                            dimmed = synced && activeIndex >= 0 && abs(index - activeIndex) > 1,
                                            onClick = {
                                                if (synced) onSeekToLine?.invoke(line.tMs)
                                            }
                                        )
                                    }
                                }

                                // Top & bottom soft fades
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .align(Alignment.TopCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(LyricsBg, Color.Transparent)
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, LyricsBg)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class LyricsUiState { Loading, Error, Empty, Ready }

@Composable
private fun PremiumLyricLine(
    text: String,
    active: Boolean,
    near: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = when {
            active -> 1.06f
            near -> 1f
            else -> 0.96f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "lyricScale"
    )
    val alpha by animateFloatAsState(
        targetValue = when {
            active -> 1f
            near -> 0.55f
            dimmed -> 0.28f
            else -> 0.72f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "lyricAlpha"
    )
    val color by animateColorAsState(
        targetValue = when {
            active -> Color.White
            near -> Color(0xFFD8D0E8)
            else -> LyricsDim
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "lyricColor"
    )

    Text(
        text = text,
        color = color,
        fontSize = if (active) 26.sp else 18.sp,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        lineHeight = if (active) 34.sp else 26.sp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = if (active) 14.dp else 10.dp)
    )
}

@Composable
private fun LyricsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = LyricsAccent,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Finding lyrics…", color = SonexaTextSubtle, fontSize = 13.sp)
    }
}

@Composable
private fun LyricsMessageState(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp)
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.GraphicEq,
            contentDescription = null,
            tint = LyricsAccent.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            color = SonexaTextWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            color = SonexaTextSubtle,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

private fun sourceBadge(lyrics: LyricsResponse?): String? {
    if (lyrics == null) return null
    return when (lyrics.source) {
        "lrclib" -> if (lyrics.synced) "Synced" else "Lyrics"
        "musixmatch" -> "Musixmatch"
        "lyrics.ovh" -> "Lyrics"
        "cache" -> if (lyrics.synced) "Synced" else "Saved"
        "none", "" -> null
        else -> lyrics.source.replaceFirstChar { it.uppercase() }
    }
}
