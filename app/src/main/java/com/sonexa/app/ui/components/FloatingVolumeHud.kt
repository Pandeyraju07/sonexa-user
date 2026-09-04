package com.sonexa.app.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val HIGH_SOUND_THRESHOLD = 0.80f

/**
 * Modern floating glassmorphic volume HUD with left/right drag-to-adjust gesture,
 * real-time percentage readout, and High Sound safety threshold indicator.
 */
@Composable
fun FloatingVolumeHud(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1) }

    var currentVolume by remember {
        mutableStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2))
    }
    var isVisible by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(currentVolume.toFloat() / maxVolume.toFloat()) }
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleHide(delayMillis: Long = 2400L) {
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(delayMillis)
            if (!isDragging) {
                isVisible = false
            }
        }
    }

    fun triggerHud(newVol: Int) {
        currentVolume = newVol
        dragFraction = (newVol.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
        isVisible = true
        scheduleHide()
    }

    fun updateVolumeFromFraction(fraction: Float) {
        val clampedFrac = fraction.coerceIn(0f, 1f)
        dragFraction = clampedFrac
        val newVol = (clampedFrac * maxVolume).roundToInt().coerceIn(0, maxVolume)
        if (newVol != currentVolume) {
            currentVolume = newVol
            try {
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(context) {
        var isInitial = true
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == "android.media.VOLUME_CHANGED_ACTION" || action == "android.media.RINGER_MODE_CHANGED") {
                    val streamType = intent?.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) ?: -1
                    if (streamType == AudioManager.STREAM_MUSIC || streamType == -1) {
                        val newVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: currentVolume
                        if (!isInitial && newVol != currentVolume) {
                            triggerHud(newVol)
                        } else if (isInitial) {
                            currentVolume = newVol
                            isInitial = false
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.RINGER_MODE_CHANGED")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {}

        val contentObserver = object : android.database.ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val newVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: currentVolume
                if (newVol != currentVolume) {
                    triggerHud(newVol)
                }
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                contentObserver
            )
        } catch (_: Exception) {}

        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            try { context.contentResolver.unregisterContentObserver(contentObserver) } catch (_: Exception) {}
            hideJob?.cancel()
        }
    }

    val activeFraction = if (isDragging) dragFraction else (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = activeFraction,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
        label = "volFractionAnim"
    )

    val isHighSound = activeFraction >= HIGH_SOUND_THRESHOLD
    val ambientGlowColor by animateColorAsState(
        targetValue = when {
            activeFraction == 0f -> Color(0xFFEF4444)
            isHighSound -> Color(0xFFEF4444)
            else -> Color(0xFF1ED760)
        },
        label = "ambientGlow"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = ambientGlowColor.copy(alpha = 0.6f),
                    spotColor = ambientGlowColor.copy(alpha = 0.8f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1F1835).copy(alpha = 0.96f), Color(0xFF0F0B18).copy(alpha = 0.96f))
                    )
                )
                .border(
                    1.dp,
                    if (isHighSound) Color(0xFFEF4444).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.16f),
                    RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Volume Icon
                val icon = when {
                    currentVolume == 0 -> Icons.AutoMirrored.Filled.VolumeOff
                    activeFraction < 0.4f -> Icons.AutoMirrored.Filled.VolumeMute
                    else -> Icons.AutoMirrored.Filled.VolumeUp
                }
                val iconTint = when {
                    currentVolume == 0 -> Color(0xFFEF4444)
                    isHighSound -> Color(0xFFF87171)
                    else -> Color(0xFF1ED760)
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Volume",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )

                // Drag Left & Right Interactive Volume Bar
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(28.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val frac = (offset.x / size.width).coerceIn(0f, 1f)
                                updateVolumeFromFraction(frac)
                                scheduleHide()
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    hideJob?.cancel()
                                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                                    updateVolumeFromFraction(frac)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                                    updateVolumeFromFraction(frac)
                                },
                                onDragEnd = {
                                    isDragging = false
                                    scheduleHide()
                                },
                                onDragCancel = {
                                    isDragging = false
                                    scheduleHide()
                                }
                            )
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Background track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isDragging) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                    )

                    // Active fill bar
                    val activeBarBrush = if (isHighSound) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFF59E0B), Color(0xFFEF4444))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF1ED760))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction.coerceIn(0.01f, 1f))
                            .height(if (isDragging) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(activeBarBrush)
                    )
                }

                // Volume Percent Text & High Sound Warning Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${(activeFraction * 100).toInt()}%",
                        color = if (isHighSound) Color(0xFFFCA5A5) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isHighSound) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.35f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "High Sound Warning",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "HIGH SOUND",
                                    color = Color(0xFFFECACA),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
