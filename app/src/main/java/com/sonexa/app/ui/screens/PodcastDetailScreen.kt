package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Download
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
import com.sonexa.app.data.model.PodcastDto
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

private fun PodcastEpisodeDto.toTrack(podcast: PodcastDto): TrackDto =
    TrackDto(
        id = id.ifBlank { title },
        title = title,
        artist = podcast.host.ifBlank { "Podcast Host" },
        album = podcast.title.ifBlank { "Podcast" },
        audioUrl = audioUrl,
        coverUrl = coverUrl.ifBlank { podcast.coverUrl },
        provider = "podcast",
        providerType = "audio"
    )

@Composable
fun PodcastDetailScreen(
    podcastId: String,
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    onOpenEpisodeDetail: (String) -> Unit = {},
    playbackViewModel: PlaybackViewModel,
    podcastViewModel: PodcastViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by podcastViewModel.detail.collectAsState()
    val followedPodcasts by podcastViewModel.followedPodcasts.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()
    val downloadedEpisodes by com.sonexa.app.data.local.PodcastDownloadManager.downloadedEpisodes.collectAsState()
    val downloadingIds by com.sonexa.app.data.local.PodcastDownloadManager.downloadingIds.collectAsState()

    var selectedFilter by remember { mutableStateOf("Newest") }
    val filterOptions = listOf("Newest", "Oldest", "Most Popular", "Unplayed")

    LaunchedEffect(podcastId) {
        podcastViewModel.loadDetail(podcastId)
    }

    val detailData = (detailState as? CatalogUiState.Ready)?.data
    val podcast = detailData?.podcast ?: PodcastDto(
        id = podcastId,
        title = "Podcast Show",
        host = "Host",
        description = "Discover conversations, insights, and stories.",
        coverUrl = "",
        category = "Society",
        language = "Hindi"
    )
    val rawEpisodes = detailData?.episodes.orEmpty()
    val episodes = remember(rawEpisodes, selectedFilter) {
        when (selectedFilter) {
            "Oldest" -> rawEpisodes.reversed()
            "Most Popular" -> rawEpisodes.sortedByDescending { it.durationMs }
            else -> rawEpisodes
        }
    }

    val isFollowed = followedPodcasts.contains(podcast.id) || podcast.isFollowed

    fun playEpisode(ep: PodcastEpisodeDto) {
        val localAudio = com.sonexa.app.data.local.PodcastDownloadManager.getLocalAudioUrl(ep.id)
        val streamUrl = localAudio ?: ep.audioUrl
        if (streamUrl.isBlank()) {
            Toast.makeText(context, "Episode stream unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val queue = episodes.map {
            val epLocal = com.sonexa.app.data.local.PodcastDownloadManager.getLocalAudioUrl(it.id)
            it.toTrack(podcast).copy(audioUrl = epLocal ?: it.audioUrl)
        }
        val index = queue.indexOfFirst { it.id == ep.id }.coerceAtLeast(0)
        playbackViewModel.playQueue(queue, index, podcast.title)
        onOpenFullPlayer()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090611))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
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
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${podcast.title} on TuneFlow!")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Podcast"))
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

            // 2. Hero Podcast Header
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
                        if (podcast.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(podcast.coverUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = podcast.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = podcast.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Show by ${podcast.host}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPurple
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Language & Category Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1A28))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(podcast.language, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1A28))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(podcast.category, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1A28))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${podcast.followerCount} followers", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = podcast.description,
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Buttons: Follow + Play
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play Latest Button
                        Button(
                            onClick = {
                                val firstEp = episodes.firstOrNull()
                                if (firstEp != null) {
                                    playEpisode(firstEp)
                                } else {
                                    Toast.makeText(context, "No episodes available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Latest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Follow Button
                        OutlinedButton(
                            onClick = { podcastViewModel.toggleFollow(podcast.id) },
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFollowed) BrandPurple else Color.White.copy(alpha = 0.4f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isFollowed) BrandPurple.copy(alpha = 0.15f) else Color.Transparent,
                                contentColor = if (isFollowed) BrandPurple else Color.White
                            ),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(
                                if (isFollowed) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFollowed) "Following" else "Follow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 3. Filter Chips: Newest / Oldest / Most Popular
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { opt ->
                        val isSelected = selectedFilter == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPurple else Color(0xFF1F1A28))
                                .clickable { selectedFilter = opt }
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. Episodes Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Episodes (${episodes.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 5. Episode List Items
            if (detailState is CatalogUiState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPurple)
                    }
                }
            } else if (episodes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No episodes available", color = TextMuted)
                    }
                }
            } else {
                itemsIndexed(episodes) { index, ep ->
                    val isPlaying = playbackState.track?.audioUrl == ep.audioUrl && playbackState.isPlaying
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBg)
                            .border(1.dp, if (isPlaying) BrandPurple else CardBorder, RoundedCornerShape(16.dp))
                            .clickable { onOpenEpisodeDetail(ep.id) }
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "EPISODE ${ep.episodeNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = BrandPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ep.title,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ep.description,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${ep.durationLabel} • ${ep.publishedAt}",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val isDownloaded = downloadedEpisodes.any { it.id == ep.id }
                                    val isDownloading = downloadingIds.contains(ep.id)

                                    if (isDownloading) {
                                        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = BrandPurple, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                        }
                                    } else if (isDownloaded) {
                                        IconButton(
                                            onClick = {
                                                com.sonexa.app.data.local.PodcastDownloadManager.deleteDownloadedEpisode(context, ep.id)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = BrandPurple, modifier = Modifier.size(20.dp))
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                com.sonexa.app.data.local.PodcastDownloadManager.downloadEpisode(context, ep, podcast)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Outlined.Download, contentDescription = "Download", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    // Play Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable { playEpisode(ep) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
