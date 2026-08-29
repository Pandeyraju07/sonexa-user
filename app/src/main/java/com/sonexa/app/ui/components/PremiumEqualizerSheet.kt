package com.sonexa.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.audio.SonexaEqualizerEngine
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextSubtle
import com.sonexa.app.ui.theme.SonexaTextWhite
import kotlin.math.sin

private val EqBg = Color(0xFF0B0914)
private val EqSurface = Color(0xFF151222)
private val EqBorder = Color(0xFF2A2438)
private val EqAccent = Color(0xFFBB86FC)
private val EqMagenta = Color(0xFFFF2D95)
private val EqCyan = Color(0xFF5CE1E6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumEqualizerSheet(
    snapshot: SonexaEqualizerEngine.Snapshot,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onBandChange: (Int, Float) -> Unit,
    onBassChange: (Float) -> Unit,
    onVirtualChange: (Float) -> Unit,
    onPreset: (String) -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = EqBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Ambient glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(280.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x55B062FF), Color(0x228B5CF6), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(12.dp, CircleShape, ambientColor = EqAccent, spotColor = EqMagenta)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF7B2FF7), Color(0xFFE534B2)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GraphicEq, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Studio Equalizer",
                            color = SonexaTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            if (snapshot.supported) {
                                "${snapshot.bands.size}-band • ${snapshot.presetName}"
                            } else {
                                "Waiting for audio session…"
                            },
                            color = SonexaTextSubtle,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, "Reset", tint = SonexaTextMuted)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = SonexaTextMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Power row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EqSurface)
                        .border(1.dp, EqBorder, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EQ Engine", color = SonexaTextWhite, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (snapshot.enabled) "Live processing active" else "Bypassed",
                            color = if (snapshot.enabled) EqCyan else SonexaTextSubtle,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = snapshot.enabled,
                        onCheckedChange = onEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EqAccent,
                            uncheckedThumbColor = Color.White.copy(0.8f),
                            uncheckedTrackColor = Color(0xFF2A2735)
                        )
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Wave visualizer
                EqWaveVisualizer(
                    enabled = snapshot.enabled && isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(EqSurface)
                        .border(1.dp, EqBorder, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "PRESETS",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SonexaEqualizerEngine.PRESET_NAMES.forEach { name ->
                        val selected = snapshot.presetName == name
                        val bg by animateColorAsState(
                            if (selected) Color.Transparent else EqSurface,
                            label = "presetBg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .then(
                                    if (selected) {
                                        Modifier.background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF7B2FF7), Color(0xFFE534B2))
                                            )
                                        )
                                    } else {
                                        Modifier
                                            .background(bg)
                                            .border(1.dp, EqBorder, RoundedCornerShape(22.dp))
                                    }
                                )
                                .clickable { onPreset(name) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                name,
                                color = if (selected) Color.White else SonexaTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    "FREQUENCY BANDS",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(EqSurface)
                        .border(1.dp, EqBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 10.dp, vertical = 16.dp)
                ) {
                    if (snapshot.bands.isEmpty()) {
                        Text(
                            "Play a song to activate hardware EQ bands",
                            color = SonexaTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            snapshot.bands.forEach { band ->
                                EqBandSlider(
                                    label = band.label,
                                    value = band.level,
                                    enabled = snapshot.enabled,
                                    onValueChange = { onBandChange(band.index, it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "SPATIAL & BASS",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))

                EqHorizontalControl(
                    title = "Bass Boost",
                    subtitle = "Low-end punch",
                    value = snapshot.bassBoost,
                    accent = EqMagenta,
                    enabled = snapshot.enabled,
                    onValueChange = onBassChange
                )
                Spacer(Modifier.height(10.dp))
                EqHorizontalControl(
                    title = "Virtualizer",
                    subtitle = "Stereo width",
                    value = snapshot.virtualizer,
                    accent = EqCyan,
                    enabled = snapshot.enabled,
                    onValueChange = onVirtualChange
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Changes apply instantly to the current audio session.",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EqWaveVisualizer(enabled: Boolean, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "eqWave")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    Canvas(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        val bars = 28
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (bars - 1)) / bars
        for (i in 0 until bars) {
            val wave = if (enabled) {
                val n = i / bars.toFloat()
                0.25f + 0.75f * (
                    0.55f * sin(phase + n * 6.5f) +
                        0.35f * sin(phase * 1.7f + n * 11f) +
                        0.25f
                    ).coerceIn(0.15f, 1f)
            } else {
                0.18f
            }
            val h = size.height * wave
            val x = i * (barWidth + gap)
            val y = (size.height - h) / 2f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(EqAccent.copy(alpha = if (enabled) 0.95f else 0.25f), EqMagenta.copy(alpha = if (enabled) 0.8f else 0.15f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var local by remember(value) { mutableFloatStateOf(value) }
    val animated by animateFloatAsState(local, label = "bandAnim")
    val fillAlpha = if (enabled) 1f else 0.35f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = String.format("%+.1f", animated * 12f),
            color = SonexaTextSubtle.copy(alpha = fillAlpha),
            fontSize = 9.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .width(28.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    enabled = enabled,
                    state = rememberDraggableState { delta ->
                        val trackPx = with(density) { 180.dp.toPx() }.coerceAtLeast(1f)
                        // Drag up = increase
                        local = (local - delta / trackPx * 2f).coerceIn(-1f, 1f)
                        onValueChange(local)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackW = 8.dp.toPx()
                val cx = size.width / 2f
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                val trackH = bottom - top
                // Track
                drawRoundRect(
                    color = Color(0xFF2A2735),
                    topLeft = Offset(cx - trackW / 2f, top),
                    size = Size(trackW, trackH),
                    cornerRadius = CornerRadius(trackW / 2f)
                )
                // Active fill from center
                val midY = top + trackH / 2f
                val thumbY = midY - animated * (trackH / 2f)
                val fillTop = minOf(midY, thumbY)
                val fillBot = maxOf(midY, thumbY)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(EqAccent, EqMagenta)),
                    topLeft = Offset(cx - trackW / 2f, fillTop),
                    size = Size(trackW, (fillBot - fillTop).coerceAtLeast(2f)),
                    cornerRadius = CornerRadius(trackW / 2f)
                )
                // Thumb
                drawCircle(
                    color = Color.White.copy(alpha = fillAlpha),
                    radius = 9.dp.toPx(),
                    center = Offset(cx, thumbY)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(EqAccent, EqMagenta)),
                    radius = 5.dp.toPx(),
                    center = Offset(cx, thumbY)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = SonexaTextMuted.copy(alpha = fillAlpha),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EqHorizontalControl(
    title: String,
    subtitle: String,
    value: Float,
    accent: Color,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    var local by remember(value) { mutableFloatStateOf(value) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EqSurface)
            .border(1.dp, EqBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = SonexaTextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = SonexaTextSubtle, fontSize = 11.sp)
            }
            Text(
                "${(local * 100).toInt()}%",
                color = accent.copy(alpha = if (enabled) 1f else 0.4f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                    state = rememberDraggableState { delta ->
                        val widthPx = with(density) { 280.dp.toPx() }.coerceAtLeast(1f)
                        local = (local + delta / widthPx).coerceIn(0f, 1f)
                        onValueChange(local)
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val h = 8.dp.toPx()
                val y = size.height / 2f - h / 2f
                drawRoundRect(
                    color = Color(0xFF2A2735),
                    topLeft = Offset(0f, y),
                    size = Size(size.width, h),
                    cornerRadius = CornerRadius(h / 2f)
                )
                val w = size.width * local.coerceIn(0f, 1f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(accent.copy(0.5f), accent)),
                    topLeft = Offset(0f, y),
                    size = Size(w.coerceAtLeast(h), h),
                    cornerRadius = CornerRadius(h / 2f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = Offset(w.coerceIn(10.dp.toPx(), size.width - 10.dp.toPx()), size.height / 2f)
                )
                drawCircle(
                    color = accent,
                    radius = 5.dp.toPx(),
                    center = Offset(w.coerceIn(10.dp.toPx(), size.width - 10.dp.toPx()), size.height / 2f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
