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
import androidx.compose.material.icons.outlined.*
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
import com.sonexa.app.data.model.EventTicketTierDto
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveEventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    playbackViewModel: PlaybackViewModel,
    liveEventsViewModel: LiveEventsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by liveEventsViewModel.detailState.collectAsState()
    val remindedEvents by liveEventsViewModel.remindedEvents.collectAsState()

    var showTicketModal by remember { mutableStateOf(false) }
    var selectedTierIndex by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        liveEventsViewModel.loadDetail(eventId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090610))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (val state = detailState) {
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
                    Text("Failed to load event details", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { liveEventsViewModel.loadDetail(eventId) }) {
                        Text("Retry")
                    }
                }
            }
            is CatalogUiState.Ready -> {
                val event = state.data.event ?: LiveEventDto(
                    id = eventId,
                    title = "Live Concert",
                    artistName = "Artist",
                    venue = "Stadium Arena",
                    city = "Mumbai",
                    date = "Upcoming",
                    time = "7:00 PM"
                )
                val ticketTiers = state.data.ticketTiers
                val isReminded = remindedEvents.contains(event.id)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // 1. Hero Stage Banner & Floating Navigation
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
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

                            // Gradient Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x99000000),
                                                Color.Transparent,
                                                Color(0xCC090610),
                                                Color(0xFF090610)
                                            )
                                        )
                                    )
                            )

                            // Top Back & Action Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x80000000))
                                        .clickable { onNavigateBack() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x80000000))
                                            .clickable {
                                                liveEventsViewModel.toggleReminder(event.id)
                                                Toast.makeText(context, if (!isReminded) "Reminder set for ${event.title}" else "Reminder removed", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isReminded) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                            contentDescription = "Remind",
                                            tint = if (isReminded) BrandGold else Color.White
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x80000000))
                                            .clickable {
                                                Toast.makeText(context, "Event link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Event Title & Metadata
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (event.status == "LIVE_NOW") Color(0xFF1ED760) else BrandOrange)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = event.status.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = event.category,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = event.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Date & Venue Details Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.CalendarToday, contentDescription = "Date", tint = BrandOrange, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "${event.date} • ${event.time}",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Place, contentDescription = "Venue", tint = BrandOrange, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "${event.venue} (${event.city})",
                                            fontSize = 13.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.ConfirmationNumber, contentDescription = "Price", tint = BrandGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Starting from ${event.priceStarting}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Primary Action: Book Tickets
                            Button(
                                onClick = { showTicketModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Book Tickets • From ${event.priceStarting}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // 3. Lineup / Performing Artists
                    if (event.lineup.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Artist Lineup",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(event.lineup) { artist ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CardBg)
                                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = "🎙️ $artist",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Tour Setlist Preview
                    if (event.setlist.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Official Tour Setlist",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Preview songs live on tour",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }

                                Button(
                                    onClick = {
                                        val tracks = event.setlist.map { st ->
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
                                        playbackViewModel.playQueue(tracks, 0, event.title)
                                        onOpenFullPlayer()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D33)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play All", fontSize = 12.sp, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        items(event.setlist) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val q = event.setlist.map { st ->
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
                                        val idx = q.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                                        playbackViewModel.playQueue(q, idx, event.title)
                                        onOpenFullPlayer()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF221A2E))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(track.coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${track.artist} • ${track.durationLabel}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    Icons.Default.PlayCircle,
                                    contentDescription = "Play",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Interactive Ticket Booking Modal Sheet
                if (showTicketModal) {
                    ModalBottomSheet(
                        onDismissRequest = { showTicketModal = false },
                        containerColor = Color(0xFF161120),
                        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Select Ticket Tier",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "${event.title} • ${event.venue}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            ticketTiers.forEachIndexed { index, tier ->
                                val isSelected = selectedTierIndex == index
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) BrandOrange.copy(alpha = 0.15f) else CardBg)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) BrandOrange else CardBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedTierIndex = index }
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tier.name,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = tier.price,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = BrandGold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tier.description,
                                            fontSize = 11.5.sp,
                                            color = TextMuted
                                        )
                                        if (tier.perks.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                tier.perks.forEach { perk ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF261D33))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("✓ $perk", fontSize = 10.sp, color = BrandGold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            val currentTier = ticketTiers.getOrNull(selectedTierIndex)
                            Button(
                                onClick = {
                                    showTicketModal = false
                                    Toast.makeText(
                                        context,
                                        "Booking Confirmed! 🎟️ Tier: ${currentTier?.name ?: "Pass"} (${currentTier?.price}). E-Tickets sent to your registered email.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Text(
                                    text = "Pay ${currentTier?.price ?: event.priceStarting} & Get E-Pass →",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}
