package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

private val PodcastPurple = Color(0xFF7C3AED)
private val PodcastMuted = Color(0xFF9E98AB)
private val CardBg = Color(0xFF14101F)
private val CardBorder = Color(0xFF221B30)

private fun PodcastEpisodeDto.toTrack(podcastTitle: String, host: String, coverUrl: String): TrackDto =
    TrackDto(
        id = id.ifBlank { title },
        title = title,
        artist = host.ifBlank { "Podcast Host" },
        album = podcastTitle.ifBlank { "Podcasts" },
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        provider = "podcast",
        providerType = "audio"
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val detailState by viewModel.detail.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Technology", "Business", "Comedy", "True Crime", "Health", "Science", "News"
    )

    fun playEpisode(episode: PodcastEpisodeDto, podcast: PodcastDto?) {
        if (episode.audioUrl.isBlank()) {
            Toast.makeText(context, "Episode stream unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val title = podcast?.title ?: "Podcasts"
        val host = podcast?.host ?: "Host"
        val cover = podcast?.coverUrl.orEmpty()
        playbackViewModel.play(episode.toTrack(title, host, cover), title)
        onOpenFullPlayer()
    }

    val podcastsList = (uiState as? CatalogUiState.Ready)?.data?.podcasts.orEmpty()
    val detailData = (detailState as? CatalogUiState.Ready)?.data
    val currentPodcast = detailData?.podcast ?: podcastsList.firstOrNull()
    val episodes = detailData?.episodes.orEmpty()

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
            // 1. TOP APP BAR (Back, Title, Search toggle)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Podcasts & Shows",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Free streaming audio shows",
                                fontSize = 12.sp,
                                color = PodcastMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSearching = !isSearching },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSearching) PodcastPurple else Color(0xFF1F1A28))
                    ) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            }

            // 2. SEARCH BAR (When active)
            item {
                AnimatedVisibility(visible = isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchPodcasts(it)
                            },
                            placeholder = { Text("Search podcasts & creators...", color = PodcastMuted, fontSize = 14.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = PodcastPurple)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        viewModel.searchPodcasts("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                viewModel.searchPodcasts(searchQuery)
                            }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF14101F),
                                unfocusedContainerColor = Color(0xFF14101F),
                                focusedBorderColor = PodcastPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. CATEGORY PILLS HORIZONTAL CAROUSEL
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PodcastPurple else Color(0xFF1F1A28))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    searchQuery = ""
                                    viewModel.load(cat)
                                }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. HERO FEATURED SHOW BANNER
            if (currentPodcast != null && !isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PodcastPurple.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "FEATURED SHOW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentPodcast.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentPodcast.host,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = PodcastMuted,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Play Latest Button
                                val firstEp = episodes.firstOrNull()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .clickable {
                                            if (firstEp != null) {
                                                playEpisode(firstEp, currentPodcast)
                                            } else {
                                                Toast.makeText(context, "Loading episodes...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Play Latest",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            // Show Cover Image
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = PodcastPurple, spotColor = PodcastPurple)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E172F))
                            ) {
                                if (currentPodcast.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(currentPodcast.coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = currentPodcast.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. TOP SHOWS CAROUSEL
            if (podcastsList.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Shows in $selectedCategory",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(podcastsList) { pod ->
                            val isSelectedShow = pod.id == currentPodcast?.id
                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clickable { viewModel.loadDetail(pod.id) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(136.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(CardBg)
                                        .border(
                                            if (isSelectedShow) 2.dp else 1.dp,
                                            if (isSelectedShow) PodcastPurple else CardBorder,
                                            RoundedCornerShape(18.dp)
                                        )
                                ) {
                                    if (pod.coverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(pod.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = pod.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pod.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = pod.host,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = PodcastMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 6. EPISODES LIST SECTION
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentPodcast != null) "Episodes • ${currentPodcast.title}" else "Episodes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (episodes.isNotEmpty()) {
                        Text(
                            text = "${episodes.size} episodes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PodcastPurple
                        )
                    }
                }
            }

            if (detailState is CatalogUiState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PodcastPurple, modifier = Modifier.size(36.dp))
                    }
                }
            } else if (episodes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a show above to view and play episodes", color = PodcastMuted, fontSize = 13.sp)
                    }
                }
            } else {
                itemsIndexed(episodes) { index, ep ->
                    val isCurrentPlaying = playbackState.track?.audioUrl == ep.audioUrl
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBg)
                            .border(
                                1.dp,
                                if (isCurrentPlaying) PodcastPurple else CardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { playEpisode(ep, currentPodcast) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Episode Number or Play Icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentPlaying) PodcastPurple else Color(0xFF1F1A28)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentPlaying && playbackState.isPlaying) {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Playing",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Episode Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ep.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentPlaying) PodcastPurple else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ep.description.ifBlank { "Listen to full episode" },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = PodcastMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF231C32))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = ep.durationLabel.ifBlank { "Episode" },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFA855F7)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ep. #${ep.episodeNumber}",
                                        fontSize = 10.5.sp,
                                        color = PodcastMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING MINI PLAYER (if playing)
        val currentTrack = playbackState.track
        if (currentTrack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = PodcastPurple, spotColor = PodcastPurple)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E172F))
                    .border(1.dp, Color(0xFF382A54), RoundedCornerShape(14.dp))
                    .clickable { onOpenFullPlayer() }
            ) {
                // Purple bottom glow accent bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(PodcastPurple)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2B2142))
                    ) {
                        if (currentTrack.effectiveCoverUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentTrack.effectiveCoverUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = currentTrack.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack.artist,
                            fontSize = 11.5.sp,
                            color = PodcastMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { playbackViewModel.togglePlayPause() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
