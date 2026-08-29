package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.PodcastChapterDto
import com.sonexa.app.data.model.PodcastEpisodeDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PodcastViewModel

private val BrandPurple = Color(0xFF7C3AED)
private val TextMuted = Color(0xFF9E98AB)
private val CardBg = Color(0xFF14101F)
private val CardBorder = Color(0xFF221B30)

private fun PodcastEpisodeDto.toTrack(showTitle: String, host: String): TrackDto =
    TrackDto(
        id = id.ifBlank { title },
        title = title,
        artist = host.ifBlank { "Podcast Host" },
        album = showTitle.ifBlank { "Podcast" },
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        provider = "podcast",
        providerType = "audio"
    )

@Composable
fun EpisodeDetailScreen(
    episodeId: String,
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    podcastViewModel: PodcastViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by podcastViewModel.detail.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    val detailData = (detailState as? CatalogUiState.Ready)?.data
    val podcast = detailData?.podcast
    val episode = detailData?.episodes?.find { it.id == episodeId } ?: detailData?.episodes?.firstOrNull()

    var isSaved by remember { mutableStateOf(false) }

    fun playEpisode() {
        if (episode == null || episode.audioUrl.isBlank()) {
            Toast.makeText(context, "Episode stream unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val showTitle = podcast?.title ?: "Podcast"
        val host = podcast?.host ?: "Host"
        playbackViewModel.play(episode.toTrack(showTitle, host), showTitle)
        onOpenFullPlayer()
    }

    val isCurrentPlaying = playbackState.track?.audioUrl == episode?.audioUrl && playbackState.isPlaying

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090611))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (episode == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPurple)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Top Navigation Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F1A28))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${episode.title} on TuneFlow!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Episode"))
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1A28))
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                            }
                        }
                    }
                }

                // 2. Episode Hero Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = BrandPurple, spotColor = BrandPurple)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1E172F))
                        ) {
                            val img = episode.coverUrl.ifBlank { podcast?.coverUrl.orEmpty() }
                            if (img.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(img)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = episode.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = episode.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = podcast?.title ?: "Podcast Show",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPurple
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${episode.durationLabel} • ${episode.publishedAt}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Bar: Play Button + Download + Save + Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { playEpisode() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCurrentPlaying) "Pause" else "Play Episode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            IconButton(
                                onClick = {
                                    isSaved = !isSaved
                                    Toast.makeText(context, if (isSaved) "Episode saved to your library" else "Episode removed", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1A28))
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) BrandPurple else Color.White
                                )
                            }

                            IconButton(
                                onClick = { Toast.makeText(context, "Downloading episode for offline listening...", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1A28))
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = "Download", tint = Color.White)
                            }
                        }
                    }
                }

                // 3. About This Episode / Description
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "About this episode",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = episode.description.ifBlank { "No detailed notes available for this episode." },
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFD1D5DB)
                        )
                    }
                }

                // 4. Interactive Chapters Section
                if (episode.chapters.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Chapters",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    items(episode.chapters) { chapter ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isCurrentPlaying) {
                                        playEpisode()
                                    }
                                    playbackViewModel.seekToChapter(chapter.startTimeSeconds)
                                    Toast.makeText(context, "Jumped to ${chapter.title}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PlayCircleOutline,
                                        contentDescription = null,
                                        tint = BrandPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = chapter.title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                val mins = chapter.startTimeSeconds / 60
                                val secs = chapter.startTimeSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPurple
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
