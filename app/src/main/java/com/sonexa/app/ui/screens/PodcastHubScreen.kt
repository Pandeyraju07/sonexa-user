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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.PodcastEpisodeDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PodcastViewModel

private fun PodcastEpisodeDto.toTrack(podcastTitle: String, host: String, coverUrl: String): TrackDto =
    TrackDto(
        id = id.ifBlank { title },
        title = title,
        artist = host,
        album = podcastTitle,
        audioUrl = audioUrl,
        coverUrl = coverUrl
    )

@Composable
fun PodcastHubScreen(
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    viewModel: PodcastViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAiSummary by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val detail by viewModel.detail.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.loadDetail("pod_1")
    }
    LaunchedEffect(playbackState.errorMessage) {
        val msg = playbackState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        playbackViewModel.clearError()
    }

    fun playEpisode(episode: PodcastEpisodeDto, podcastTitle: String, host: String, coverUrl: String) {
        if (episode.audioUrl.isBlank()) {
            Toast.makeText(context, "No audio available for this episode", Toast.LENGTH_SHORT).show()
            return
        }
        playbackViewModel.play(episode.toTrack(podcastTitle, host, coverUrl))
        onOpenFullPlayer()
    }

    val hubReady = uiState as? CatalogUiState.Ready
    val detailReady = detail as? CatalogUiState.Ready
    val podcast = detailReady?.data?.podcast ?: hubReady?.data?.podcasts?.firstOrNull()
    val episodes = detailReady?.data?.episodes.orEmpty()
    val hubTitle = podcast?.title ?: "Podcast Hub"
    val host = podcast?.host.orEmpty()
    val coverUrl = podcast?.coverUrl.orEmpty()
    val isLoading = uiState is CatalogUiState.Loading || detail is CatalogUiState.Loading
    val errorMessage = (detail as? CatalogUiState.Error)?.message
        ?: (uiState as? CatalogUiState.Error)?.message

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = SonexaPurpleLight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorMessage, color = SonexaTextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SonexaGradientButton(
                        text = "Retry",
                        onClick = {
                            viewModel.load()
                            viewModel.loadDetail("pod_1")
                        }
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SonexaInputBg)
                                    .clickable { onNavigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SonexaTextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFE534B2), Color(0xFF6B3CE9)))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = hubTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Podcasts,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(70.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = hubTitle, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
                            Text(
                                text = buildString {
                                    if (host.isNotBlank()) append("By $host")
                                    if (episodes.isNotEmpty()) {
                                        if (isNotEmpty()) append(" • ")
                                        append("${episodes.size} Episodes")
                                    }
                                }.ifBlank { "Podcast" },
                                fontSize = 13.sp,
                                color = SonexaTextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SonexaGradientButton(
                                text = "AI Episode Summary 🤖",
                                onClick = { showAiSummary = true }
                            )
                        }
                    }

                    item {
                        Text(text = "Recent Episodes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }

                    if (episodes.isEmpty()) {
                        item {
                            Text(
                                text = "No episodes available",
                                fontSize = 14.sp,
                                color = SonexaTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(episodes) { episode ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SonexaInputBg)
                                    .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                                    .clickable { playEpisode(episode, hubTitle, host, coverUrl) }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(text = episode.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${episode.durationLabel} • Ep ${episode.episodeNumber}",
                                        fontSize = 12.sp,
                                        color = SonexaTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAiSummary) {
            AlertDialog(
                onDismissRequest = { showAiSummary = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Sonexa AI Episode Summary", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "📌 Key Takeaways from Ep #245:\n1. AI will assist music artists in lyric writing and melody creation.\n2. Lossless audio + 3D spatial audio will redefine streaming.\n3. Personalized AI DJs will curate 80% of daily music listening by 2027.",
                        fontSize = 13.sp,
                        color = SonexaTextWhite,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(onClick = { showAiSummary = false }) { Text(text = "Close") }
                }
            )
        }
    }
}
