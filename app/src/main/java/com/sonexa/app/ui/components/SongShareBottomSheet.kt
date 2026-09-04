package com.sonexa.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
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

private val ShareGreen = Color(0xFF1ED760)
private val ShareSurface = Color(0xFF12101A)

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
    val shareText = "🎵 Listen to \"${track.title}\" by ${track.artist} on Sonexa:\n$shareUrl\n\nExperience Lossless 320kbps audio & AI vibes on Sonexa."

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ShareSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.22f))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SonexaTextWhite,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Premium horizontal track card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2C2050), Color(0xFF1A1530), Color(0xFF100D1C))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(14.dp, RoundedCornerShape(12.dp),
                                ambientColor = ShareGreen.copy(alpha = 0.35f),
                                spotColor = Color.Black)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF241E38))
                    ) {
                        AsyncImage(
                            model = track.effectiveCoverUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = track.artist,
                            fontSize = 13.sp,
                            color = SonexaTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ShareGreen.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SONEXA",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ShareGreen,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.07f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "320K HQ",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }

                    // Audio bars decoration
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(10, 18, 13, 20, 8).forEachIndexed { i, h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ShareGreen.copy(alpha = 0.55f + i * 0.09f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Divider with label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                Text(
                    text = "  SHARE TO  ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.35f),
                    letterSpacing = 1.5.sp
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.08f)))
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Social brand icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BrandShareButton(
                    logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/WhatsApp.svg/120px-WhatsApp.svg.png",
                    label = "WhatsApp",
                    ringColor = Color(0xFF25D366),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                `package` = "com.whatsapp"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (_: Exception) {
                            try {
                                val waIntent = Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(shareText)}"))
                                context.startActivity(waIntent)
                                onDismiss()
                            } catch (_: Exception) {
                                SonexaShareHelper.shareTrack(context, track)
                                onDismiss()
                            }
                        }
                    }
                )

                BrandShareButton(
                    logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Instagram_icon.png/120px-Instagram_icon.png",
                    label = "Instagram",
                    ringColor = Color(0xFFE1306C),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                `package` = "com.instagram.android"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (_: Exception) {
                            SonexaShareHelper.shareTrack(context, track)
                            onDismiss()
                        }
                    }
                )

                BrandShareButton(
                    logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/X_logo_2023_%28white%29.png/120px-X_logo_2023_%28white%29.png",
                    label = "X (Twitter)",
                    ringColor = Color(0xFF1DA1F2),
                    bgColor = Color(0xFF14171A),
                    onClick = {
                        try {
                            val twitterText = Uri.encode("🎵 ${track.title} by ${track.artist} $shareUrl via @sonexaapp")
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://twitter.com/intent/tweet?text=$twitterText"))
                            context.startActivity(intent)
                            onDismiss()
                        } catch (_: Exception) {
                            SonexaShareHelper.shareTrack(context, track)
                            onDismiss()
                        }
                    }
                )

                BrandShareButton(
                    logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Telegram_logo.svg/120px-Telegram_logo.svg.png",
                    label = "Telegram",
                    ringColor = Color(0xFF2CA5E0),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                `package` = "org.telegram.messenger"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (_: Exception) {
                            try {
                                val tgUrl = Uri.parse(
                                    "https://t.me/share/url?url=${Uri.encode(shareUrl)}&text=${Uri.encode("🎵 ${track.title} by ${track.artist}")}"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, tgUrl))
                                onDismiss()
                            } catch (_: Exception) {
                                SonexaShareHelper.shareTrack(context, track)
                                onDismiss()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Copy link + More Apps row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val copyBg by animateColorAsState(
                    targetValue = if (isCopied) ShareGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                    animationSpec = tween(300), label = "copyBg"
                )
                val copyScale by animateFloatAsState(
                    targetValue = if (isCopied) 0.96f else 1f,
                    animationSpec = tween(150), label = "copyScale"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .scale(copyScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(copyBg)
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Sonexa Song Link", shareUrl)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(2500)
                                isCopied = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCopied) ShareGreen else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy Link",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCopied) ShareGreen else Color.White.copy(alpha = 0.75f)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .clickable {
                            SonexaShareHelper.shareTrack(context, track)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "More",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "More Apps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandShareButton(
    logoUrl: String,
    label: String,
    ringColor: Color,
    bgColor: Color = Color(0xFF1C1928),
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = tween(120), label = "brandScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = ringColor.copy(alpha = 0.3f),
                    spotColor = ringColor.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = 1.5.dp,
                    brush = Brush.radialGradient(
                        listOf(ringColor.copy(alpha = 0.6f), ringColor.copy(alpha = 0.15f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
