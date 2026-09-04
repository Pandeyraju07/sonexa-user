package com.sonexa.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.util.SonexaShareHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PlayerGreen = Color(0xFF1ED760)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongShareBottomSheet(
    track: TrackDto?,
    onDismiss: () -> Unit
) {
    if (track == null) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val shareUrl = remember(track) { SonexaShareHelper.generateTrackShareUrl(track) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14111E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sheet Title
            Text(
                text = "Share Song",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SonexaTextWhite
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Spotify-Style Premium Track Share Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF281E48),
                                Color(0xFF171228),
                                Color(0xFF0F0B1A)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Album Art with Neon Elevation
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .shadow(20.dp, RoundedCornerShape(16.dp), ambientColor = PlayerGreen.copy(alpha = 0.4f), spotColor = Color.White.copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2438))
                    ) {
                        AsyncImage(
                            model = track.effectiveCoverUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Artist
                    Text(
                        text = track.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = track.artist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SonexaTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spotify-Style Audio Waveform Visualizer Graphic
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PlayerGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        // Dynamic Simulated Audio Bars
                        listOf(4, 9, 16, 7, 20, 12, 6, 18, 22, 14, 8, 19, 11, 5, 17, 13, 8, 21, 10, 4).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(PlayerGreen)
                            )
                        }
                        Text(
                            text = "320K",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PlayerGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row: Copy Link, WhatsApp, More Apps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Copy Link Action
                ShareActionButton(
                    icon = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    label = if (isCopied) "Copied!" else "Copy Link",
                    iconTint = if (isCopied) PlayerGreen else Color.White,
                    containerColor = if (isCopied) PlayerGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.09f),
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Zynera Song Link", shareUrl)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2500)
                            isCopied = false
                        }
                    }
                )

                // WhatsApp Action
                ShareActionButton(
                    icon = Icons.Default.Chat,
                    label = "WhatsApp",
                    iconTint = Color(0xFF25D366),
                    containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                `package` = "com.whatsapp"
                                putExtra(Intent.EXTRA_TEXT, "🎵 Listen to \"${track.title}\" by ${track.artist} on Zynera:\n$shareUrl\n\nExperience Lossless 320kbps audio & AI vibes on Zynera.")
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (_: Exception) {
                            SonexaShareHelper.shareTrack(context, track)
                            onDismiss()
                        }
                    }
                )

                // More Apps / System Share Chooser Action
                ShareActionButton(
                    icon = Icons.Default.Share,
                    label = "More",
                    iconTint = Color.White,
                    containerColor = Color.White.copy(alpha = 0.09f),
                    onClick = {
                        SonexaShareHelper.shareTrack(context, track)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(containerColor)
                .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SonexaTextWhite
        )
    }
}
