package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(
    onNavigateToSection: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    val genres by viewModel.genres.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf(
        "All", "Trending", "Genres", "Moods", "Charts", "New Releases",
        "Radio", "Podcasts", "Audiobooks", "AI Picks", "Regional"
    )

    val genresList = genres.map { genre ->
        GenreItem(
            name = genre.name,
            color1 = genre.color1.toComposeColor(Color(0xFF6B3CE9)),
            color2 = genre.color2.toComposeColor(Color(0xFF9825DD))
        )
    }

    val liveRadioStations = listOf(
        Pair("Zynera Hits Radio", "24/7 Non-stop Hits"),
        Pair("Lo-Fi Chill Radio", "Beats to Study/Relax"),
        Pair("Bollywood Retro FM", "Gold 90s Hits"),
        Pair("Global Top 40 FM", "Worldwide Trending")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Explore & Discover",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Explore trending charts, genres, live radio & AI picks",
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SonexaPurplePrimary else SonexaInputBg)
                            .border(
                                1.dp,
                                if (isSelected) SonexaPurpleLight else SonexaInputBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SonexaTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    ExploreSectionHeader(title = "Live Radio Stations", onSeeAll = { onNavigateToSection("Live Radio") })
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(liveRadioStations) { radio ->
                            Box(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF6B3CE9), Color(0xFFE534B2))))
                                    .clickable { Toast.makeText(context, "Tuning into ${radio.first}", Toast.LENGTH_SHORT).show() }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Radio, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "LIVE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = radio.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                    Text(text = radio.second, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
                                }
                            }
                        }
                    }
                }

                item {
                    ExploreSectionHeader(title = "Browse Genres", onSeeAll = { onNavigateToSection("Genres") })
                    Spacer(modifier = Modifier.height(10.dp))
                    when {
                        uiState is CatalogUiState.Loading && genresList.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SonexaPurpleLight, modifier = Modifier.size(28.dp))
                            }
                        }
                        genresList.isEmpty() -> {
                            Text(
                                text = "No genres available",
                                fontSize = 14.sp,
                                color = SonexaTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                genresList.take(3).forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(70.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.linearGradient(listOf(genre.color1, genre.color2)))
                                            .clickable { Toast.makeText(context, "Opening ${genre.name}", Toast.LENGTH_SHORT).show() }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = genre.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    ExploreSectionHeader(title = "Regional Music & Charts", onSeeAll = { onNavigateToSection("Regional Music") })
                    Spacer(modifier = Modifier.height(10.dp))
                    val regionals = listOf("Top 50 India", "Punjabi Pop Hits", "Tamil Superhits", "Telugu Melodies", "Global Viral 50")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(regionals) { item ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SonexaInputBg)
                                    .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                                    .clickable { Toast.makeText(context, "Opening $item", Toast.LENGTH_SHORT).show() }
                                    .padding(12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = item, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreSectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSeeAll() }
        ) {
            Text(text = "See all", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SonexaPurpleLight)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(14.dp))
        }
    }
}
