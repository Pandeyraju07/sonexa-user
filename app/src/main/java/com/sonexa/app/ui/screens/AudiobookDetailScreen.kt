package com.sonexa.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.AudiobookChapterDto
import com.sonexa.app.data.model.AudiobookDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AudiobookViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

@Composable
fun AudiobookDetailScreen(
    audiobookId: String,
    onNavigateBack: () -> Unit,
    onOpenAudiobookPlayer: () -> Unit,
    audiobookViewModel: AudiobookViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by audiobookViewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    var isDescriptionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(audiobookId) {
        audiobookViewModel.loadAudiobookDetail(audiobookId)
    }

    val book = uiState.selectedAudiobook
    val progress = AudiobookStore.getProgress(audiobookId)
    val isSaved = AudiobookStore.isSaved(audiobookId)

    if (book == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ZyneraBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ZyneraPrimaryBright)
        }
        return
    }

    val resumeChapterIndex = (progress?.currentChapterNumber?.minus(1) ?: 0).coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
    val resumePositionMs = progress?.positionMs ?: 0L
    val hasStarted = progress != null && progress.completionPercentage > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZyneraBg)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── 1. Top Bar Back Navigation ──────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppleMaterials.glassFill)
                            .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                            .appleBounce { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AppleMaterials.glassFill)
                                .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                                .appleBounce {
                                    audiobookViewModel.toggleSave(book.id)
                                    Toast.makeText(
                                        context,
                                        if (!isSaved) "Saved to Library" else "Removed from Library",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isSaved) ZyneraPrimaryBright else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AppleMaterials.glassFill)
                                .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                                .appleBounce {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Listening to '${book.title}' by ${book.author} on Zynera!\nhttps://zynera.app/audiobook/${book.id}"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Audiobook"))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // ── 2. Immersive Cover Artwork & Book Header ────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ZyneraSurfaceElevated)
                            .border(1.2.dp, AppleMaterials.specularBorder, RoundedCornerShape(22.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(book.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = book.title,
                        style = AppleType.title1Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Written by ${book.author}",
                        style = AppleType.subheadlineBold,
                        color = ZyneraPrimaryBright
                    )

                    Text(
                        text = "Narrated by ${book.narrator} • ${book.languageName}",
                        style = AppleType.caption1,
                        color = ZyneraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badges row: Rating, Duration, Chapters, Copyright status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailTagBadge("⭐ ${book.rating} (${book.reviewsCount})")
                        DetailTagBadge(book.formattedDuration)
                        DetailTagBadge("${book.chapterCount} Chapters")
                        if (book.isPublicDomain) {
                            DetailTagBadge("PUBLIC DOMAIN", isHighlight = true)
                        }
                    }
                }
            }

            // ── 3. Primary CTA Actions ──────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Play / Resume Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(ZyneraVioletGradient)
                            .appleBounce {
                                playbackViewModel.playAudiobook(
                                    audiobook = book,
                                    chapterIndex = resumeChapterIndex,
                                    startPositionMs = resumePositionMs
                                )
                                onOpenAudiobookPlayer()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasStarted) "Continue Chapter ${resumeChapterIndex + 1} (${progress?.formattedPosition})" else "Start Listening",
                                style = AppleType.headlineBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryActionButton(
                            icon = Icons.Outlined.Download,
                            label = "Download",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Toast.makeText(context, "Downloading audio for offline playback...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        SecondaryActionButton(
                            icon = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            label = if (isSaved) "In Library" else "Save Book",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                audiobookViewModel.toggleSave(book.id)
                            }
                        )
                    }
                }
            }

            // ── 4. Book Synopsis / Description ──────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Synopsis",
                        style = AppleType.title3Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = book.description,
                        style = AppleType.body,
                        color = ZyneraTextSecondary,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (isDescriptionExpanded) "Show Less" else "Read More",
                        style = AppleType.footnoteBold,
                        color = ZyneraPrimaryBright,
                        modifier = Modifier
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // ── 5. Full Chapter List ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chapters (${book.chapters.size})",
                        style = AppleType.title3Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Total ${book.formattedDuration}",
                        style = AppleType.caption1,
                        color = ZyneraTextSecondary
                    )
                }
            }

            items(book.chapters) { chapter ->
                val isCurrentlyPlaying = playbackState.isAudiobookMode &&
                        playbackState.activeAudiobook?.id == book.id &&
                        playbackState.activeChapterIndex == (chapter.chapterNumber - 1) &&
                        playbackState.isPlaying

                AudiobookChapterItem(
                    chapter = chapter,
                    isPlaying = isCurrentlyPlaying,
                    onClick = {
                        playbackViewModel.playAudiobook(
                            audiobook = book,
                            chapterIndex = (chapter.chapterNumber - 1).coerceAtLeast(0),
                            startPositionMs = 0L
                        )
                        onOpenAudiobookPlayer()
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chapter Item & Helper Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AudiobookChapterItem(
    chapter: AudiobookChapterDto,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPlaying) ZyneraSurfaceElevated else ZyneraSurface)
            .border(
                0.8.dp,
                if (isPlaying) ZyneraPrimary else ZyneraBorder,
                RoundedCornerShape(14.dp)
            )
            .appleBounce { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) ZyneraPrimary else Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isPlaying) Color.White else ZyneraTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    style = AppleType.subheadlineBold,
                    color = if (isPlaying) ZyneraPrimaryBright else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!chapter.summary.isNullOrBlank()) {
                    Text(
                        text = chapter.summary,
                        style = AppleType.caption2,
                        color = ZyneraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = chapter.formattedDuration,
                style = AppleType.caption1Bold,
                color = ZyneraTextSubtle
            )
        }
    }
}

@Composable
private fun DetailTagBadge(
    text: String,
    isHighlight: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHighlight) ZyneraPrimary.copy(alpha = 0.2f) else ZyneraSurfaceElevated)
            .border(
                0.8.dp,
                if (isHighlight) ZyneraPrimaryBright else ZyneraBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = AppleType.caption2Bold,
            color = if (isHighlight) ZyneraPrimaryBright else ZyneraTextSecondary
        )
    }
}

@Composable
private fun SecondaryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ZyneraSurface)
            .border(0.8.dp, ZyneraBorder, RoundedCornerShape(14.dp))
            .appleBounce { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = AppleType.footnoteBold,
                color = Color.White
            )
        }
    }
}
