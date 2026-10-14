package com.sonexa.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.theme.AppleType
import com.sonexa.app.ui.theme.appleBounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSleepTimerBottomSheet(
    sleepTimerRemainingMs: Long?,
    onSetTimerMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit,
    endOfContentMinutes: Int? = null,
    endOfContentLabel: String = "End of Track"
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customMins by remember { mutableFloatStateOf(30f) }

    // Pulsing animation for active countdown halo
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F0B18),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 10.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Ambient Radial Top Glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(320.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0x3DF59E0B),
                                Color(0x1EA855F7),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // ── 1. Header Row ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFFF59E0B),
                                    spotColor = Color(0xFFA855F7)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Sleep Timer",
                                style = AppleType.title2Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Automatically pause when you drift off",
                                fontSize = 12.sp,
                                color = Color(0xFF9E98AB)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E172E))
                            .border(1.dp, Color(0xFF33274D), CircleShape)
                            .clickable(onClick = onDismiss),
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

                // ── 2. Active Timer Glowing Live Card (if timer is active) ──
                if (sleepTimerRemainingMs != null && sleepTimerRemainingMs > 0) {
                    val totalSecs = (sleepTimerRemainingMs / 1000).coerceAtLeast(0)
                    val remMins = totalSecs / 60
                    val remSecs = totalSecs % 60
                    val timeString = if (remMins > 0) "${remMins}m ${"%02d".format(remSecs)}s" else "${remSecs}s"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2E1908), Color(0xFF1C112E))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFF59E0B).copy(alpha = pulseAlpha),
                                        Color(0xFFA855F7).copy(alpha = pulseAlpha)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B).copy(alpha = pulseAlpha))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TIMER ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = Color(0xFFFBBF24)
                                    )
                                }

                                // Cancel button chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33EF4444))
                                        .border(1.dp, Color(0x66EF4444), RoundedCornerShape(10.dp))
                                        .clickable {
                                            onSetTimerMinutes(null)
                                            Toast.makeText(context, "Sleep timer stopped", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Stop Timer",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = timeString,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Playback will fade out and pause at zero",
                                fontSize = 12.sp,
                                color = Color(0xFFAFA9BB)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Extension Chips: +5m, +15m, +30m
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5 to "+5 min", 15 to "+15 min", 30 to "+30 min").forEach { (extra, label) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2B1F42))
                                            .border(1.dp, Color(0xFF3F2F61), RoundedCornerShape(10.dp))
                                            .clickable {
                                                val newTotalMins = (remMins + extra).toInt()
                                                onSetTimerMinutes(newTotalMins)
                                                Toast.makeText(context, "Added $extra min to timer", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // ── 3. Quick Presets Section ────────────────────────────────
                Text(
                    text = "QUICK PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    color = Color(0xFF8E889E)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val presets = remember(endOfContentMinutes, endOfContentLabel) {
                    val base = mutableListOf(
                        Triple(15, "15 Minutes", "Quick power nap"),
                        Triple(30, "30 Minutes", "Relaxing unwind"),
                        Triple(45, "45 Minutes", "Deep rest"),
                        Triple(60, "1 Hour", "Standard bedtime"),
                        Triple(90, "1.5 Hours", "Extended listening")
                    )
                    if (endOfContentMinutes != null && endOfContentMinutes > 0) {
                        base.add(0, Triple(endOfContentMinutes, endOfContentLabel, "~$endOfContentMinutes min remaining"))
                    }
                    base
                }

                // Grid of Preset Cards (2 columns)
                val chunked = presets.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (mins, title, desc) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF161024))
                                    .border(1.dp, Color(0xFF2B203E), RoundedCornerShape(16.dp))
                                    .appleBounce {
                                        onSetTimerMinutes(mins)
                                        Toast.makeText(context, "Sleep timer set for $title", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${mins}m",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x33A855F7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Timer,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFEDE9FE),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = desc,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF8E889E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Fill remainder if odd count
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 4. Custom Minutes Stepper & Slider ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF130E20))
                        .border(1.dp, Color(0xFF281C3E), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CUSTOM DURATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFF8E889E)
                            )
                            Text(
                                text = "${customMins.toInt()} Minutes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // -5m Stepper Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF221738))
                                    .border(1.dp, Color(0xFF382956), CircleShape)
                                    .clickable {
                                        customMins = (customMins - 5f).coerceAtLeast(5f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus 5 mins",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Slider in Center
                            Slider(
                                value = customMins,
                                onValueChange = { customMins = it },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF59E0B),
                                    activeTrackColor = Color(0xFFF59E0B),
                                    inactiveTrackColor = Color(0xFF26193E)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            )

                            // +5m Stepper Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF221738))
                                    .border(1.dp, Color(0xFF382956), CircleShape)
                                    .clickable {
                                        customMins = (customMins + 5f).coerceAtMost(120f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus 5 mins",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Start Timer Action Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFA855F7))
                                    )
                                )
                                .clickable {
                                    onSetTimerMinutes(customMins.toInt())
                                    Toast.makeText(context, "Timer set for ${customMins.toInt()} minutes", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start ${customMins.toInt()} Min Timer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // ── 5. Turn Off Option at Bottom ────────────────────────────
                if (sleepTimerRemainingMs != null && sleepTimerRemainingMs > 0) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF171020))
                            .border(1.dp, Color(0xFF2A1C3C), RoundedCornerShape(14.dp))
                            .clickable {
                                onSetTimerMinutes(null)
                                Toast.makeText(context, "Sleep timer turned off", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TimerOff,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Turn Off Timer",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
