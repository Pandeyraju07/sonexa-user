package com.sonexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import com.sonexa.app.ui.viewmodel.PodcastViewModel

private val BrandPurple = Color(0xFF7C3AED)
private val TextMuted = Color(0xFF9E98AB)
private val CardBg = Color(0xFF14101F)

@Composable
fun LanguageDiscoveryScreen(
    languageCode: String,
    onNavigateBack: () -> Unit,
    onOpenPodcastDetail: (String) -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    podcastViewModel: PodcastViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by podcastViewModel.uiState.collectAsState()

    val langDisplay = remember(languageCode) {
        when (languageCode.lowercase()) {
            "hindi" -> "Hindi (हिन्दी)"
            "tamil" -> "Tamil (தமிழ்)"
            "telugu" -> "Telugu (తెలుగు)"
            "bengali" -> "Bengali (বাংলা)"
            "marathi" -> "Marathi (मराठी)"
            "punjabi" -> "Punjabi (ਪੰਜਾਬੀ)"
            "spanish" -> "Spanish (Español)"
            "german" -> "German (Deutsch)"
            "japanese" -> "Japanese (日本語)"
            else -> languageCode.replaceFirstChar { it.uppercase() }
        }
    }

    var selectedSubCat by remember { mutableStateOf("All") }
    val subCategories = listOf("All", "Stories", "News", "Motivation", "True Crime", "Business", "Comedy", "Technology")

    LaunchedEffect(languageCode, selectedSubCat) {
        val query = if (selectedSubCat == "All") "$languageCode podcast" else "$languageCode $selectedSubCat podcast"
        podcastViewModel.searchPodcasts(query)
    }

    val podcasts = (uiState as? CatalogUiState.Ready)?.data?.podcasts.orEmpty()

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
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F1A28))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("Podcasts in", fontSize = 12.sp, color = TextMuted)
                        Text(langDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Description
            item {
                Text(
                    text = "Discover podcasts, stories, news and deep conversations in $langDisplay.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Sub-category Chips
            item {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subCategories) { cat ->
                        val isSelected = selectedSubCat == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPurple else Color(0xFF1F1A28))
                                .clickable { selectedSubCat = cat }
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Content Grid
            if (uiState is CatalogUiState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPurple)
                    }
                }
            } else if (podcasts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No podcasts found in this category", color = TextMuted)
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Trending in $langDisplay",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(podcasts.chunked(2)) { rowShows ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowShows.forEach { show ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .clickable { onOpenPodcastDetail(show.id) }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E172F))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(show.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = show.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = show.title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = show.host,
                                        fontSize = 11.5.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (rowShows.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
