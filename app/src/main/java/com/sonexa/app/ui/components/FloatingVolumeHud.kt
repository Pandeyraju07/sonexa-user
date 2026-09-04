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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun triggerHud(newVol: Int) {
        currentVolume = newVol
        isVisible = true
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(2200)
            isVisible = false
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

    val volumeFraction = (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = volumeFraction,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "volAnim"
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
                .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF1ED760).copy(alpha = 0.5f), spotColor = Color.White)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E1730).copy(alpha = 0.95f), Color(0xFF100D1A).copy(alpha = 0.95f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val icon = when {
                    currentVolume == 0 -> Icons.AutoMirrored.Filled.VolumeOff
                    volumeFraction < 0.4f -> Icons.AutoMirrored.Filled.VolumeMute
                    else -> Icons.AutoMirrored.Filled.VolumeUp
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Volume",
                    tint = if (currentVolume == 0) Color(0xFFEF4444) else Color(0xFF1ED760),
                    modifier = Modifier.size(20.dp)
                )

                // Neon Volume Bar
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction.coerceIn(0.01f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF1ED760))
                                )
                            )
                    )
                }

                // Volume Percent Text
                Text(
                    text = "${(volumeFraction * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
