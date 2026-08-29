package com.sonexa.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
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
import com.sonexa.app.data.model.EventSetlistTrackDto
import com.sonexa.app.data.model.LiveEventDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.LiveEventsViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

private val BrandOrange = Color(0xFFFF5722)
private val BrandGold = Color(0xFFFFB300)
private val CardBg = Color(0xFF14101A)
private val CardBorder = Color(0xFF261D33)
private val TextMuted = Color(0xFF9E98AB)

@Composable
fun LiveEventsHubScreen(
    onNavigateBack: () -> Unit,
    onOpenEventDetail: (String) -> Unit,
    playbackViewModel: PlaybackViewModel,
    liveEventsViewModel: LiveEventsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val feedState by liveEventsViewModel.feedState.collectAsState()
    val selectedCity by liveEventsViewModel.selectedCity.collectAsState()
    val selectedCategory by liveEventsViewModel.selectedCategory.collectAsState()
    val remindedEvents by liveEventsViewModel.remindedEvents.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090610))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (val state = feedState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandOrange)
                }
            }
            is CatalogUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load live events", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { liveEventsViewModel.loadFeed(selectedCity, selectedCategory) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            is CatalogUiState.Ready -> {
                val feed = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // 1. Top Bar: Back + Title + Search
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LIVE EVENTS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Concerts, Tours & Festivals",
                                    fontSize = 11.5.sp,
                                    color = BrandGold
                                )
                            }
                            IconButton(onClick = { Toast.makeText(context, "Location set to: $selectedCity", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Outlined.Place, contentDescription = "Location", tint = BrandOrange)
                            }
                        }
                    }

                    // 2. City Selector Horizontal Pills
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(feed.cities) { city ->
                                val isSelected = selectedCity.equals(city, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) BrandOrange else CardBg)
                                        .border(1.dp, if (isSelected) BrandOrange else CardBorder, RoundedCornerShape(20.dp))
                                        .clickable { liveEventsViewModel.loadFeed(city, selectedCategory) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = city,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // 3. Featured Stadium Tours Spotlight Carousel
                    if (feed.featuredTours.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Featured Tours & Mega Concerts 🔥",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                items(feed.featuredTours) { tour ->
                                    FeaturedTourCard(
                                        tour = tour,
                                        isReminded = remindedEvents.contains(tour.id),
                                        onOpenDetail = { onOpenEventDetail(tour.id) },
                                        onToggleReminder = { liveEventsViewModel.toggleReminder(tour.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Category Filter Chips
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Events in $selectedCity",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            items(feed.categories) { cat ->
                                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color.White else CardBg)
                                        .border(1.dp, if (isSelected) Color.White else CardBorder, RoundedCornerShape(16.dp))
                                        .clickable { liveEventsViewModel.loadFeed(selectedCity, cat) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.Black else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // 5. Events List
                    items(feed.events) { event ->
                        LiveEventRowCard(
                            event = event,
                            isReminded = remindedEvents.contains(event.id),
                            onOpenDetail = { onOpenEventDetail(event.id) },
                            onToggleReminder = { liveEventsViewModel.toggleReminder(event.id) },
                            onPlaySetlist = {
                                val setlistTracks = event.setlist.map { st ->
                                    TrackDto(
                                        id = st.id,
                                        title = st.title,
                                        artist = st.artist,
                                        album = event.title,
                                        durationMs = st.durationMs,
                                        audioUrl = st.audioUrl,
                                        coverUrl = st.coverUrl.ifBlank { event.bannerUrl },
                                        provider = "live_event",
                                        providerType = "audio"
                                    )
                                }
                                if (setlistTracks.isNotEmpty()) {
                                    playbackViewModel.playQueue(setlistTracks, 0, event.title)
                                    Toast.makeText(context, "Playing ${event.title} setlist preview", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedTourCard(
    tour: LiveEventDto,
    isReminded: Boolean,
    onOpenDetail: () -> Unit,
    onToggleReminder: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { onOpenDetail() }
    ) {
        // Banner Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(tour.bannerUrl)
                .crossfade(true)
                .build(),
            contentDescription = tour.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x99000000), Color(0xF0090610)),
                        startY = 50f
                    )
                )
        )

        // Top Status Badge & Reminder Bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tour.status == "LIVE_NOW") Color(0xFF1ED760) else BrandOrange)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tour.status.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x80000000))
                    .clickable {
                        onToggleReminder()
                        Toast.makeText(context, if (!isReminded) "Reminder set for ${tour.title}" else "Reminder removed", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isReminded) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Remind",
                    tint = if (isReminded) BrandGold else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = tour.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${tour.venue} • ${tour.city}",
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tour.date} • ${tour.time}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandGold
                )
                Text(
                    text = tour.priceStarting,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LiveEventRowCard(
    event: LiveEventDto,
    isReminded: Boolean,
    onOpenDetail: () -> Unit,
    onToggleReminder: () -> Unit,
    onPlaySetlist: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onOpenDetail() }
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail Image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF201828))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(event.bannerUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (event.status == "LIVE_NOW") Color(0xFF1ED760) else BrandOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.status.replace("_", " "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (event.status == "LIVE_NOW") Color.Black else BrandOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.category,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.venue} • ${event.date}",
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Starts at ${event.priceStarting}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play Setlist Preview or Book Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (event.setlist.isNotEmpty()) {
                    IconButton(
                        onClick = onPlaySetlist,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play Setlist",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onToggleReminder()
                        Toast.makeText(context, if (!isReminded) "Reminder set for ${event.title}" else "Reminder removed", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isReminded) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Reminder",
                        tint = if (isReminded) BrandGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
