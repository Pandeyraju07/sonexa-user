package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.sonexa.app.data.model.*
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PodcastViewModel

private val BrandPurple = Color(0xFF7C3AED)
private val TextMuted = Color(0xFF9E98AB)
private val CardBg = Color(0xFF14101F)
private val CardBorder = Color(0xFF221B30)

private fun PodcastEpisodeDto.toTrack(podcastTitle: String, host: String, fallbackCover: String): TrackDto =
    TrackDto(
        id = id.ifBlank { title },
        title = title,
        artist = host.ifBlank { "Podcast Host" },
        album = podcastTitle.ifBlank { "Podcast" },
        audioUrl = audioUrl,
        coverUrl = coverUrl.ifBlank { fallbackCover },
        provider = "podcast",
        providerType = "audio"
    )

@Composable
fun PodcastHubScreen(
    onNavigateBack: () -> Unit = {},
    onOpenFullPlayer: () -> Unit = {},
    onOpenPodcastDetail: (String) -> Unit = {},
    onOpenEpisodeDetail: (String) -> Unit = {},
    onOpenLanguageDiscovery: (String) -> Unit = {},
    onSwitchToMusic: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    viewModel: PodcastViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val homeFeedState by viewModel.homeState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val feedData = (homeFeedState as? CatalogUiState.Ready)?.data
    val languages = remember(feedData) { feedData?.languages ?: viewModel.getLanguagesSafe() }
    val categories = remember(feedData) { feedData?.categories ?: viewModel.getCategoriesSafe() }

    val continueList = feedData?.continueListening.orEmpty()
    val trendingShows = (uiState as? CatalogUiState.Ready)?.data?.podcasts
        ?: feedData?.trendingPodcasts.orEmpty()

    fun playEpisode(episode: PodcastEpisodeDto, podcast: PodcastDto?) {
        if (episode.audioUrl.isBlank()) {
            Toast.makeText(context, "Audio stream unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val showTitle = podcast?.title ?: "Podcast"
        val host = podcast?.host ?: "Host"
        val cover = episode.coverUrl.ifBlank { podcast?.coverUrl.orEmpty() }
        playbackViewModel.play(episode.toTrack(showTitle, host, cover), showTitle)
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
            // 1. Header with Greeting, Profile, Notifications & Search Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good evening",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Listen to something interesting",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSearching = !isSearching },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F1A28))
                        ) {
                            Icon(
                                imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F1A28))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenProfile,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandPurple),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Text("U", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 2. Subtle Segmented Content Switcher: [ Music ] [ Podcasts ]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1F1A28))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Music Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Transparent)
                                .clickable { onSwitchToMusic() }
                                .padding(horizontal = 18.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Music",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }

                        // Podcasts Pill (Active)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BrandPurple)
                                .padding(horizontal = 18.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Podcasts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 3. Search Bar (when expanded)
            if (isSearching) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchPodcasts(it)
                        },
                        placeholder = { Text("Search shows, creators, topics...", color = TextMuted, fontSize = 13.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandPurple) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.searchPodcasts("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBg,
                            unfocusedContainerColor = CardBg,
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // 4. Continue Listening (if active)
            if (continueList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Continue Listening",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueList) { ep ->
                            val isPlaying = playbackState.track?.audioUrl == ep.audioUrl && playbackState.isPlaying
                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                    .clickable { onOpenEpisodeDetail(ep.id) }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E172F))
                                    ) {
                                        if (ep.coverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(ep.coverUrl).crossfade(true).build(),
                                                contentDescription = ep.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ep.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ep.durationLabel} • 42%",
                                            fontSize = 11.5.sp,
                                            color = BrandPurple
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.42f },
                                            modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                            color = BrandPurple,
                                            trackColor = Color(0xFF2D243E)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable { playEpisode(ep, null) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Explore by Language Section
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore by Language",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See all",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPurple,
                        modifier = Modifier.clickable { onOpenLanguageDiscovery("hindi") }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languages) { lang ->
                        val isSelected = selectedLanguage.equals(lang.code, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPurple else Color(0xFF1F1A28))
                                .border(
                                    1.dp,
                                    if (isSelected) BrandPurple else Color(0xFF2D253B),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    viewModel.selectLanguage(lang.code)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (lang.name == lang.nativeName) lang.name else "${lang.name} (${lang.nativeName})",
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 6. Trending Podcasts Carousel
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Trending Podcasts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (uiState is CatalogUiState.Loading && trendingShows.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPurple, modifier = Modifier.size(32.dp))
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(trendingShows) { show ->
                            Box(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .clickable { onOpenPodcastDetail(show.id) }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E172F))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(show.coverUrl).crossfade(true).build(),
                                            contentDescription = show.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = show.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = show.host,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = show.language,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Explore Categories Section (Visual Tiles)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Explore Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(categories.chunked(2)) { rowCats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCats.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(android.graphics.Color.parseColor(cat.gradientFrom)),
                                            Color(android.graphics.Color.parseColor(cat.gradientTo))
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.load(cat.name)
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = cat.icon,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                    if (rowCats.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun PodcastViewModel.getLanguagesSafe(): List<PodcastLanguageDto> = listOf(
    PodcastLanguageDto("hindi", "Hindi", "हिन्दी", "", 1420),
    PodcastLanguageDto("english", "English", "English", "", 5200),
    PodcastLanguageDto("tamil", "Tamil", "தமிழ்", "", 860),
    PodcastLanguageDto("telugu", "Telugu", "తెలుగు", "", 790),
    PodcastLanguageDto("bengali", "Bengali", "বাংলা", "", 640),
    PodcastLanguageDto("marathi", "Marathi", "मराठी", "", 580),
    PodcastLanguageDto("punjabi", "Punjabi", "ਪੰਜਾਬੀ", "", 610),
    PodcastLanguageDto("spanish", "Spanish", "Español", "", 1850)
)

private fun PodcastViewModel.getCategoriesSafe(): List<PodcastCategoryDto> = listOf(
    PodcastCategoryDto("comedy", "Comedy", "🎙️", "#D97706", "#78350F", "#D97706"),
    PodcastCategoryDto("news", "News", "📰", "#0EA5E9", "#0C4A6E", "#0EA5E9"),
    PodcastCategoryDto("business", "Business", "💼", "#059669", "#064E3B", "#059669"),
    PodcastCategoryDto("education", "Education", "🧠", "#8B5CF6", "#4C1D95", "#8B5CF6"),
    PodcastCategoryDto("technology", "Technology", "🚀", "#2563EB", "#1E3A8A", "#2563EB"),
    PodcastCategoryDto("relationships", "Relationships", "❤️", "#EC4899", "#831843", "#EC4899"),
    PodcastCategoryDto("motivation", "Motivation", "🔥", "#F97316", "#7C2D12", "#F97316"),
    PodcastCategoryDto("true_crime", "True Crime", "🔎", "#DC2626", "#7F1D1D", "#DC2626")
)
