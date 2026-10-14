package com.sonexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.ArtistDto
import com.sonexa.app.data.model.TrackDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.PlaybackUiState
data class HeroSlideData(
    val prefix: String,
    val highlight: String,
    val description: String,
    val action: String,
    val imageUrl: String,
    val gradient: List<Color>,
    val onClick: () -> Unit
)

data class CuratedSectionCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverUrl: String,
    val tracks: List<TrackDto>
)

@Composable
fun HomeFeedContent(
    avatarInitial: String,
    avatarUrl: String = "",
    userDisplayName: String = "Raju",
    selectedFeedCategory: String,
    onSelectFeedCategory: (String) -> Unit,
    onOpenSearchTab: () -> Unit = {},
    onOpenVoiceAssistant: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenProfileDrawer: () -> Unit,
    onOpenMusicDna: () -> Unit,
    onOpenMusicJourney: () -> Unit,
    onOpenMusicIntelligence: () -> Unit = {},
    onOpenAudiobooks: () -> Unit = {},
    onOpenAudiobookDetail: (String) -> Unit = {},
    quickAccessItems: List<QuickCardItem>,
    continueListening: List<TrackDto>,
    allTrending: List<TrackDto>,
    allAlbums: List<AlbumDto>,
    allArtists: List<ArtistDto>,
    playbackState: PlaybackUiState,
    onPlayTrack: (TrackDto?, List<TrackDto>, String) -> Unit,
    onOpenLikedPlaylist: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenPlaylist: (String) -> Unit = {},
    onTogglePlayPause: () -> Unit
) {
    val context = LocalContext.current
    var selectedMood by remember { mutableStateOf("Chill") }

    // Curated dynamic lists for "Made For You" and "Popular Playlists" with distinct Spotify-grade track pools
    val madeForYouList = remember(allTrending, continueListening) {
        getCuratedMadeForYouCards(allTrending, continueListening)
    }

    val popularPlaylistsList = remember(allTrending) {
        getCuratedPopularPlaylistCards(allTrending)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonexaBgDark)
    ) {
        // Subtle Apple-style ambient radial mesh glow at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x356366F1),
                            Color(0x18FE2C55),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. Apple Header: Avatar, Greeting & Frosted Action Buttons ──────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.appleBounce { onOpenProfileDrawer() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.2.dp, AppleMaterials.specularBorder, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFE2C55), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = avatarInitial,
                                color = Color.White,
                                style = AppleType.headlineBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                    val greeting = remember(currentHour) {
                        when (currentHour) {
                            in 4..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            in 17..21 -> "Good Evening"
                            else -> "Late Night Discovery"
                        }
                    }

                    Column {
                        Text(
                            text = greeting,
                            style = AppleType.title2Bold,
                            color = Color.White
                        )
                    }
                }

                // Apple Frosted Glass Header Action Buttons (Notification & Alexa Mic)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppleMaterials.glassFill)
                            .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                            .appleBounce { onOpenNotifications() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Alexa Mic Voice AI Button (replaces settings button)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00CAFF), Color(0xFF0070F3))
                                )
                            )
                            .border(1.2.dp, Color(0xFF67E8F9).copy(alpha = 0.85f), CircleShape)
                            .appleBounce { onOpenVoiceAssistant() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Alexa Mic Voice AI",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── 2. Apple Glassmorphic Search Bar Trigger ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppleMaterials.inputBackground)
                    .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .appleBounce { onOpenSearchTab() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SonexaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search songs, artists, podcasts...",
                            style = AppleType.callout,
                            color = SonexaTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 1-Click Voice Assistant Mic Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onOpenVoiceAssistant() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── 3. Apple Filter Chips (All, Music, Podcasts, Live Events, etc.) ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipItems = listOf(
                    Triple("All", null, "All"),
                    Triple("Music", Icons.Default.MusicNote, "Music"),
                    Triple("Audiobooks", Icons.Default.AutoStories, "Audiobooks"),
                    Triple("Podcasts", Icons.Default.Mic, "Podcasts"),
                    Triple("Classics", Icons.Default.MenuBook, "Classics"),
                    Triple("Live Events", Icons.Default.Sensors, "Live Events")
                )

                chipItems.forEach { (cat, icon, key) ->
                    val isSelected = selectedFeedCategory == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) SpotifyGreen else AppleMaterials.glassFill
                            )
                            .border(
                                1.dp,
                                if (isSelected) SpotifyGreen else AppleMaterials.subtleBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .appleBounce {
                                if (key == "Audiobooks") {
                                    onOpenAudiobooks()
                                } else {
                                    onSelectFeedCategory(key)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else SonexaTextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = cat,
                                style = AppleType.footnoteBold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 4. Multi-Slide Hero Discovery Carousel (Section 4.D) ───────────
            var heroSlideIndex by remember { mutableIntStateOf(0) }
            val heroSlides = remember(allTrending) {
                val romanticMix = getCuratedTracksForMood("Romantic")
                val indieMix = getCuratedTracksForMood("Chill")
                listOf(
                    HeroSlideData(
                        prefix = "Music for",
                        highlight = "Every Mood",
                        description = "Discover, vibe and feel the music that understands you.",
                        action = "Play Now",
                        imageUrl = allTrending.firstOrNull()?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                        gradient = listOf(Color(0xFF1E0B38), Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A)),
                        onClick = {
                            val topSong = romanticMix.firstOrNull() ?: allTrending.firstOrNull()
                            onPlayTrack(topSong, romanticMix, "Music for Every Mood")
                        }
                    ),
                    HeroSlideData(
                        prefix = "Your Next",
                        highlight = "Great Listen",
                        description = "Timeless audiobooks and spoken-word literature.",
                        action = "Explore Books",
                        imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
                        gradient = listOf(Color(0xFF2A103D), Color(0xFF3B0764), Color(0xFF1E1B4B), Color(0xFF080810)),
                        onClick = { onOpenAudiobooks() }
                    ),
                    HeroSlideData(
                        prefix = "Trending",
                        highlight = "Across India",
                        description = "Top streamed chart-toppers, anthems and viral hits.",
                        action = "Listen Hits",
                        imageUrl = allTrending.getOrNull(1)?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                        gradient = listOf(Color(0xFF1F1235), Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF080810)),
                        onClick = {
                            val topSong = allTrending.firstOrNull()
                            onPlayTrack(topSong, allTrending, "Trending Across India")
                        }
                    ),
                    HeroSlideData(
                        prefix = "Classic Stories",
                        highlight = "Reimagined",
                        description = "Munshi Premchand, Rabindranath Tagore, and Doyle.",
                        action = "Listen Classics",
                        imageUrl = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600",
                        gradient = listOf(Color(0xFF281335), Color(0xFF371B58), Color(0xFF1F1133), Color(0xFF080810)),
                        onClick = { onOpenAudiobooks() }
                    ),
                    HeroSlideData(
                        prefix = "Discover",
                        highlight = "New Voices",
                        description = "Handpicked independent artists and storytellers.",
                        action = "Discover",
                        imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                        gradient = listOf(Color(0xFF1B1B3A), Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF080810)),
                        onClick = {
                            val topSong = indieMix.firstOrNull()
                            onPlayTrack(topSong, indieMix, "Discover New Voices")
                        }
                    )
                )
            }
            val activeSlide = heroSlides[heroSlideIndex.coerceIn(0, heroSlides.lastIndex)]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(184.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(activeSlide.gradient))
                    .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(22.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeSlide.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = activeSlide.highlight,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp)
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp))
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    activeSlide.gradient.first(),
                                    activeSlide.gradient.first().copy(alpha = 0.95f),
                                    activeSlide.gradient.first().copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = activeSlide.prefix,
                            style = AppleType.title2Bold,
                            color = Color.White
                        )
                        Text(
                            text = activeSlide.highlight,
                            style = AppleType.title1Bold,
                            color = ZyneraSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeSlide.description,
                            style = AppleType.caption1,
                            color = Color(0xFFDDD6FE),
                            lineHeight = 15.sp,
                            maxLines = 2
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .appleBounce { activeSlide.onClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = activeSlide.action,
                                style = AppleType.footnoteBold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // 5 Carousel Indicator Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    heroSlides.indices.forEach { idx ->
                        val isCurr = idx == heroSlideIndex
                        Box(
                            modifier = Modifier
                                .width(if (isCurr) 18.dp else 6.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurr) Color.White else Color.White.copy(alpha = 0.35f))
                                .clickable { heroSlideIndex = idx }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 5. 5 Apple Quick Action Shortcuts (Font Box Squircle Design) ─────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Liked Songs
                AppleShortcutCard(
                    icon = Icons.Default.Favorite,
                    title = "Liked\nSongs",
                    gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLikedPlaylist
                )

                // 2. Recently Played
                AppleShortcutCard(
                    icon = Icons.Default.Schedule,
                    title = "Recently\nPlayed",
                    gradientColors = listOf(Color(0xFF0D9488), Color(0xFF059669)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val track = continueListening.firstOrNull() ?: allTrending.firstOrNull()
                        onPlayTrack(track, continueListening, "Recently Played")
                    }
                )

                // 3. Downloads
                AppleShortcutCard(
                    icon = Icons.Default.Download,
                    title = "Down-\nloads",
                    gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenSettings
                )

                // 4. Your Audiobooks
                AppleShortcutCard(
                    icon = Icons.Default.AutoStories,
                    title = "Your\nBooks",
                    gradientColors = listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAudiobooks
                )

                // 5. Podcasts
                AppleShortcutCard(
                    icon = Icons.Default.Mic,
                    title = "Pod-\ncasts",
                    gradientColors = listOf(Color(0xFF4338CA), Color(0xFF2563EB)),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFeedCategory("Podcasts") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 6. Mood Discovery Selector (Section 4.F) ─────────────────────
            AppleSectionHeader(
                title = "Mood Discovery",
                subtitle = "Tune into how you want to feel right now",
                actionText = "",
                onActionClick = {}
            )

            val moods = listOf(
                Triple("Chill", listOf(Color(0xFF065F46), Color(0xFF047857)), "☕"),
                Triple("Romantic", listOf(Color(0xFFBE185D), Color(0xFF9D174D)), "💖"),
                Triple("Sad", listOf(Color(0xFF334155), Color(0xFF1E293B)), "🌧️"),
                Triple("Happy", listOf(Color(0xFFD97706), Color(0xFFB45309)), "☀️"),
                Triple("Workout", listOf(Color(0xFFDC2626), Color(0xFF991B1B)), "⚡"),
                Triple("Focus", listOf(Color(0xFF4338CA), Color(0xFF312E81)), "🎯"),
                Triple("Party", listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)), "🎉"),
                Triple("Sleep", listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)), "🌙"),
                Triple("Devotional", listOf(Color(0xFFEA580C), Color(0xFFC2410C)), "🪔")
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(moods) { (moodName, colors, emoji) ->
                    val isSelected = selectedMood == moodName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(colors))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = if (isSelected) Brush.linearGradient(listOf(Color.White, Color(0xFFFBBF24))) else AppleMaterials.specularBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .appleBounce {
                                selectedMood = moodName
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = moodName,
                                style = if (isSelected) AppleType.footnoteBold.copy(fontWeight = FontWeight.ExtraBold) else AppleType.footnoteBold,
                                color = Color.White
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFBBF24))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Dynamic Suggested Tracks for Selected Mood ───────────────────
            val moodSuggestedTracks = remember(selectedMood, allTrending, continueListening) {
                getSuggestedTracksForMood(selectedMood, allTrending, continueListening)
            }

            AppleSectionHeader(
                title = "$selectedMood Mood Mix",
                subtitle = "Handpicked tracks matching your $selectedMood vibe",
                actionText = "Play Mix",
                onActionClick = {
                    if (moodSuggestedTracks.isNotEmpty()) {
                        onPlayTrack(moodSuggestedTracks.first(), moodSuggestedTracks, "$selectedMood Mood Mix")
                    }
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(moodSuggestedTracks) { idx, track ->
                    val isCurrentTrack = playbackState.track?.id == track.id
                    val isCurrentlyPlaying = isCurrentTrack && playbackState.isPlaying

                    AppleMediaCard(
                        title = track.title,
                        subtitle = track.artist,
                        imageUrl = track.effectiveCoverUrl,
                        rankBadge = selectedMood.uppercase(),
                        isPlaying = isCurrentlyPlaying,
                        onClick = {
                            onPlayTrack(track, moodSuggestedTracks, "$selectedMood Mood Mix")
                        },
                        onPlayClick = {
                            if (isCurrentTrack) onTogglePlayPause()
                            else onPlayTrack(track, moodSuggestedTracks, "$selectedMood Mood Mix")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 6. "Made for You" Section ────────────────────────────────────────
            AppleSectionHeader(
                title = "Made for You",
                actionText = "Play Mix",
                onActionClick = {
                    val firstCard = madeForYouList.firstOrNull()
                    if (firstCard != null) {
                        onPlayTrack(firstCard.tracks.firstOrNull(), firstCard.tracks, firstCard.title)
                    }
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(madeForYouList) { card ->
                    val isCurrentlyPlaying = card.tracks.any { it.id == playbackState.track?.id } && playbackState.isPlaying
                    AppleMediaCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        imageUrl = card.coverUrl,
                        isPlaying = isCurrentlyPlaying,
                        onClick = {
                            val track = card.tracks.firstOrNull() ?: allTrending.firstOrNull()
                            onPlayTrack(track, card.tracks, card.title)
                        },
                        onPlayClick = {
                            if (isCurrentlyPlaying) {
                                onTogglePlayPause()
                            } else {
                                val track = card.tracks.firstOrNull() ?: allTrending.firstOrNull()
                                onPlayTrack(track, card.tracks, card.title)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 7. "Trending Now" Section (Top streamed songs in India) ──────────
            AppleSectionHeader(
                title = "Trending Now",
                subtitle = "Top streamed songs in India right now",
                actionText = "See All",
                onActionClick = {
                    val track = allTrending.firstOrNull()
                    if (track != null) onPlayTrack(track, allTrending, "Trending Now")
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(allTrending.take(10)) { idx, track ->
                    val isCurrentTrack = playbackState.track?.id == track.id
                    val isCurrentlyPlaying = isCurrentTrack && playbackState.isPlaying

                    AppleMediaCard(
                        title = track.title,
                        subtitle = track.artist,
                        imageUrl = track.effectiveCoverUrl,
                        rankBadge = "#${idx + 1}",
                        isPlaying = isCurrentlyPlaying,
                        onClick = { onPlayTrack(track, allTrending, "Trending Now") },
                        onPlayClick = {
                            if (isCurrentTrack) onTogglePlayPause()
                            else onPlayTrack(track, allTrending, "Trending Now")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 8. "Popular Playlists" Section ───────────────────────────────────
            AppleSectionHeader(
                title = "Popular Playlists",
                actionText = "Play Mix",
                onActionClick = {
                    val firstCard = popularPlaylistsList.firstOrNull()
                    if (firstCard != null) {
                        onPlayTrack(firstCard.tracks.firstOrNull(), firstCard.tracks, firstCard.title)
                    }
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(popularPlaylistsList) { card ->
                    val isCurrentlyPlaying = card.tracks.any { it.id == playbackState.track?.id } && playbackState.isPlaying
                    AppleMediaCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        imageUrl = card.coverUrl,
                        isPlaying = isCurrentlyPlaying,
                        onClick = {
                            val track = card.tracks.firstOrNull() ?: allTrending.firstOrNull()
                            onPlayTrack(track, card.tracks, card.title)
                        },
                        onPlayClick = {
                            if (isCurrentlyPlaying) {
                                onTogglePlayPause()
                            } else {
                                val track = card.tracks.firstOrNull() ?: allTrending.firstOrNull()
                                onPlayTrack(track, card.tracks, card.title)
                            }
                        }
                    )
                }
            }

            // ── 10. Popular Audiobooks & Classics (Section 4.I) ─────────────────
            AppleSectionHeader(
                title = "Popular Audiobooks & Classics",
                subtitle = "Premchand, Tagore, Doyle, Austen & Modern Guides",
                actionText = "Explore All",
                onActionClick = onOpenAudiobooks
            )

            val homeAudiobooks = remember { com.sonexa.app.data.repository.AudiobookRepository.bundledAudiobooks() }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(homeAudiobooks) { book ->
                    AppleMediaCard(
                        title = book.title,
                        subtitle = "${book.author} • ${book.formattedDuration}",
                        imageUrl = book.coverUrl,
                        rankBadge = if (book.isPublicDomain) "CLASSIC" else "BOOK",
                        onClick = { onOpenAudiobookDetail(book.id) },
                        onPlayClick = { onOpenAudiobookDetail(book.id) }
                    )
                }
            }

            // ── 10. Apple Music Intelligence Platform Card ──────────────────────
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1035), Color(0xFF140728), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(20.dp))
                    .appleBounce { onOpenMusicIntelligence() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7C3AED).copy(alpha = 0.35f))
                                .border(1.dp, AppleMaterials.subtleBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧠", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Music Intelligence",
                                style = AppleType.headlineBold,
                                color = Color.White
                            )
                            Text(
                                text = "Memories, Predictor, EQ & AI Eras",
                                style = AppleType.caption1,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SpotifyGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO AI",
                                style = AppleType.caption2Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Apple Design System UI Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppleShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(gradientColors)
            )
            .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(16.dp))
            .appleBounce { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = AppleType.caption2Bold,
                color = Color.White,
                maxLines = 2,
                minLines = 2,
                lineHeight = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AppleSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String = "See All",
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppleType.title3Bold,
                color = Color.White
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = AppleType.caption1,
                    color = SonexaTextMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .appleBounce { onActionClick() }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = actionText,
                style = AppleType.footnoteBold,
                color = SonexaTextMuted
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SonexaTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AppleMediaCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    rankBadge: String? = null,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .appleBounce { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF23143E),
                            Color(0xFF160B29)
                        )
                    )
                )
                .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(18.dp))
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0x33A78BFA),
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.Center)
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Numbered Ranking Badge (e.g. #1, #2)
            if (!rankBadge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = rankBadge,
                        style = AppleType.caption2Bold,
                        color = Color.White
                    )
                }
            }

            // Apple Floating Pure White Play Button
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                    .align(Alignment.BottomEnd)
                    .appleBounce { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = title,
            style = AppleType.subheadlineBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = AppleType.caption1,
            color = SonexaTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppleArtistCard(
    artist: ArtistDto,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(115.dp)
            .appleBounce { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(105.dp)
                .clip(CircleShape)
                .background(AppleMaterials.cardSurface)
                .border(1.dp, AppleMaterials.specularBorder, CircleShape)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artist.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = artist.name,
                style = AppleType.subheadlineBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artist.verified) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Text(
            text = if (artist.followersCount > 0) "${artist.followersCount / 1000}K Listeners" else "Artist",
            style = AppleType.caption2,
            color = SonexaTextMuted
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Intelligent Mood Discovery & Recommendation Helpers
// ─────────────────────────────────────────────────────────────────────────────

fun getSuggestedTracksForMood(
    mood: String,
    trending: List<TrackDto>,
    continueListening: List<TrackDto>
): List<TrackDto> {
    val pool = (continueListening + trending).distinctBy { it.id }

    // 1. Dynamic filtering based on active catalog metadata and acoustic features
    val dynamicMatches = pool.filter { track ->
        val title = track.title.lowercase()
        val artist = track.artist.lowercase()
        val genresStr = track.genres.joinToString(" ").lowercase() + " " + track.genre.lowercase()
        val tagsStr = track.tags.joinToString(" ").lowercase()

        val directMoodMatch = track.mood.equals(mood, ignoreCase = true) ||
                track.moods.any { it.equals(mood, ignoreCase = true) }

        val keywordMatch = when (mood) {
            "Chill" -> title.contains("husn") || title.contains("dooron") || title.contains("iktara") ||
                    title.contains("chill") || artist.contains("anuv jain") || artist.contains("prateek") ||
                    tagsStr.contains("chill") || genresStr.contains("acoustic") ||
                    (track.acousticness >= 0.45 && track.energy <= 0.65)
            "Romantic" -> title.contains("kesariya") || title.contains("apna bana le") || title.contains("tum hi ho") ||
                    title.contains("raataan") || title.contains("pehle bhi") || artist.contains("arijit") ||
                    artist.contains("jubin") || genresStr.contains("romantic") || tagsStr.contains("romantic") || tagsStr.contains("love")
            "Sad" -> title.contains("channa") || title.contains("agar tum") || title.contains("bekhayali") ||
                    title.contains("tune jo na") || tagsStr.contains("sad") || tagsStr.contains("heartbreak") ||
                    (track.acousticness >= 0.55 && track.energy <= 0.50)
            "Happy" -> title.contains("levitating") || title.contains("ilahi") || title.contains("gallan") ||
                    title.contains("badtameez") || (track.danceability >= 0.68 && track.energy >= 0.65)
            "Workout" -> title.contains("starboy") || title.contains("blinding") || title.contains("zinda") ||
                    title.contains("arjan") || title.contains("sultan") || track.energy >= 0.75 || track.bpm >= 130
            "Focus" -> track.isInstrumental || title.contains("lo-fi") || title.contains("focus") ||
                    title.contains("ambient") || genresStr.contains("lo-fi") || (track.energy in 0.3..0.6 && track.danceability <= 0.55)
            "Party" -> title.contains("blinding") || title.contains("levitating") || title.contains("kala chashma") ||
                    title.contains("tauba") || title.contains("chull") || track.danceability >= 0.75 || genresStr.contains("disco")
            "Sleep" -> title.contains("sleep") || title.contains("lullaby") || title.contains("rain") ||
                    (track.energy <= 0.40 && track.acousticness >= 0.65)
            "Devotional" -> title.contains("gita") || title.contains("namo") || title.contains("kun faya") ||
                    title.contains("krishna") || title.contains("shiv") || title.contains("bhakti") || tagsStr.contains("devotional")
            else -> false
        }

        directMoodMatch || keywordMatch
    }

    // 2. Curated supplementary catalog ensuring every mood is rich and complete
    val curatedCatalog = getCuratedTracksForMood(mood)

    val combined = (dynamicMatches + curatedCatalog).distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
    return combined.ifEmpty { pool.take(5) }
}

private fun getCuratedTracksForMood(mood: String): List<TrackDto> {
    return when (mood) {
        "Chill" -> listOf(
            TrackDto(
                id = "cur_chill_1",
                title = "Husn",
                artist = "Anuv Jain",
                album = "Husn - Single",
                durationMs = 217000L,
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Chill",
                moods = listOf("Chill", "Acoustic"),
                acousticness = 0.88,
                energy = 0.35
            ),
            TrackDto(
                id = "cur_chill_2",
                title = "Dooron Dooron",
                artist = "Twin Strings",
                album = "Dooron Dooron - Single",
                durationMs = 210000L,
                coverUrl = "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Chill",
                moods = listOf("Chill", "Indie"),
                acousticness = 0.75,
                energy = 0.42
            ),
            TrackDto(
                id = "cur_chill_3",
                title = "Baarishein",
                artist = "Anuv Jain",
                album = "Baarishein - Single",
                durationMs = 208000L,
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Chill",
                moods = listOf("Chill", "Monsoon"),
                acousticness = 0.82,
                energy = 0.38
            ),
            TrackDto(
                id = "cur_chill_4",
                title = "Iktara",
                artist = "Amit Trivedi, Kavita Seth",
                album = "Wake Up Sid",
                durationMs = 254000L,
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Chill",
                moods = listOf("Chill", "Bollywood"),
                acousticness = 0.70,
                energy = 0.45
            ),
            TrackDto(
                id = "cur_chill_5",
                title = "Kasoor (Acoustic)",
                artist = "Prateek Kuhad",
                album = "Kasoor",
                durationMs = 195000L,
                coverUrl = "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Chill",
                moods = listOf("Chill", "Acoustic"),
                acousticness = 0.90,
                energy = 0.32
            )
        )

        "Romantic" -> listOf(
            TrackDto(
                id = "cur_rom_1",
                title = "Kesariya",
                artist = "Arijit Singh, Pritam",
                album = "Brahmāstra",
                durationMs = 268000L,
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Romantic",
                moods = listOf("Romantic", "Love"),
                acousticness = 0.45,
                energy = 0.62
            ),
            TrackDto(
                id = "cur_rom_2",
                title = "Apna Bana Le",
                artist = "Arijit Singh, Sachin-Jigar",
                album = "Bhediya",
                durationMs = 261000L,
                coverUrl = "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Romantic",
                moods = listOf("Romantic", "Soulful"),
                acousticness = 0.62,
                energy = 0.55
            ),
            TrackDto(
                id = "cur_rom_3",
                title = "Tum Hi Ho",
                artist = "Arijit Singh, Mithoon",
                album = "Aashiqui 2",
                durationMs = 262000L,
                coverUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Romantic",
                moods = listOf("Romantic", "Classic"),
                acousticness = 0.58,
                energy = 0.52
            ),
            TrackDto(
                id = "cur_rom_4",
                title = "Raataan Lambiyan",
                artist = "Jubin Nautiyal, Asees Kaur",
                album = "Shershaah",
                durationMs = 230000L,
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Romantic",
                moods = listOf("Romantic", "Pop"),
                acousticness = 0.40,
                energy = 0.65
            ),
            TrackDto(
                id = "cur_rom_5",
                title = "Pehle Bhi Main",
                artist = "Vishal Mishra",
                album = "ANIMAL",
                durationMs = 250000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Romantic",
                moods = listOf("Romantic", "Intense"),
                acousticness = 0.50,
                energy = 0.58
            )
        )

        "Sad" -> listOf(
            TrackDto(
                id = "cur_sad_1",
                title = "Channa Mereya",
                artist = "Arijit Singh, Pritam",
                album = "Ae Dil Hai Mushkil",
                durationMs = 289000L,
                coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Sad",
                moods = listOf("Sad", "Heartbreak"),
                acousticness = 0.65,
                energy = 0.48
            ),
            TrackDto(
                id = "cur_sad_2",
                title = "Agar Tum Saath Ho",
                artist = "Alka Yagnik, Arijit Singh",
                album = "Tamasha",
                durationMs = 341000L,
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Sad",
                moods = listOf("Sad", "Emotional"),
                acousticness = 0.72,
                energy = 0.45
            ),
            TrackDto(
                id = "cur_sad_3",
                title = "Bekhayali",
                artist = "Sachet Tandon",
                album = "Kabir Singh",
                durationMs = 371000L,
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Sad",
                moods = listOf("Sad", "Rock Ballad"),
                acousticness = 0.30,
                energy = 0.68
            ),
            TrackDto(
                id = "cur_sad_4",
                title = "Tune Jo Na Kaha",
                artist = "Mohit Chauhan, Pritam",
                album = "New York",
                durationMs = 310000L,
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Sad",
                moods = listOf("Sad", "Melancholy"),
                acousticness = 0.68,
                energy = 0.40
            ),
            TrackDto(
                id = "cur_sad_5",
                title = "Hamari Adhuri Kahani",
                artist = "Arijit Singh",
                album = "Hamari Adhuri Kahani",
                durationMs = 398000L,
                coverUrl = "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Sad",
                moods = listOf("Sad", "Soulful"),
                acousticness = 0.75,
                energy = 0.38
            )
        )

        "Happy" -> listOf(
            TrackDto(
                id = "cur_hap_1",
                title = "Levitating",
                artist = "Dua Lipa",
                album = "Future Nostalgia",
                durationMs = 203000L,
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                mood = "Happy",
                moods = listOf("Happy", "Dance"),
                danceability = 0.92,
                energy = 0.88
            ),
            TrackDto(
                id = "cur_hap_2",
                title = "Ilahi",
                artist = "Arijit Singh, Pritam",
                album = "Yeh Jawaani Hai Deewani",
                durationMs = 229000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Happy",
                moods = listOf("Happy", "Travel"),
                danceability = 0.72,
                energy = 0.80
            ),
            TrackDto(
                id = "cur_hap_3",
                title = "Gallan Goodiyaan",
                artist = "Shankar-Ehsaan-Loy",
                album = "Dil Dhadakne Do",
                durationMs = 296000L,
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Happy",
                moods = listOf("Happy", "Celebration"),
                danceability = 0.85,
                energy = 0.86
            ),
            TrackDto(
                id = "cur_hap_4",
                title = "Badtameez Dil",
                artist = "Benny Dayal, Pritam",
                album = "Yeh Jawaani Hai Deewani",
                durationMs = 252000L,
                coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                mood = "Happy",
                moods = listOf("Happy", "Upbeat"),
                danceability = 0.88,
                energy = 0.90
            ),
            TrackDto(
                id = "cur_hap_5",
                title = "London Thumakda",
                artist = "Labh Janjua, Sonu Kakkar",
                album = "Queen",
                durationMs = 230000L,
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Happy",
                moods = listOf("Happy", "Festive"),
                danceability = 0.84,
                energy = 0.85
            )
        )

        "Workout" -> listOf(
            TrackDto(
                id = "cur_wrk_1",
                title = "Starboy",
                artist = "The Weeknd, Daft Punk",
                album = "Starboy",
                durationMs = 230000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Workout",
                moods = listOf("Workout", "Adrenaline"),
                danceability = 0.78,
                energy = 0.82,
                bpm = 186.0
            ),
            TrackDto(
                id = "cur_wrk_2",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                durationMs = 200000L,
                coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Workout",
                moods = listOf("Workout", "Cardio"),
                danceability = 0.85,
                energy = 0.91,
                bpm = 171.0
            ),
            TrackDto(
                id = "cur_wrk_3",
                title = "Zinda",
                artist = "Siddharth Mahadevan",
                album = "Bhaag Milkha Bhaag",
                durationMs = 211000L,
                coverUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Workout",
                moods = listOf("Workout", "Motivation"),
                energy = 0.92,
                bpm = 145.0
            ),
            TrackDto(
                id = "cur_wrk_4",
                title = "Arjan Vailly",
                artist = "Bhupinder Babbal",
                album = "ANIMAL",
                durationMs = 182000L,
                coverUrl = "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Workout",
                moods = listOf("Workout", "High-Energy"),
                energy = 0.95,
                bpm = 150.0
            ),
            TrackDto(
                id = "cur_wrk_5",
                title = "Sultan Title Track",
                artist = "Sukhwinder Singh",
                album = "Sultan",
                durationMs = 280000L,
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Workout",
                moods = listOf("Workout", "Power"),
                energy = 0.90,
                bpm = 138.0
            )
        )

        "Focus" -> listOf(
            TrackDto(
                id = "cur_foc_1",
                title = "Midnight Lo-Fi Banaras",
                artist = "Zynera Sound Lab",
                album = "Desi Chillhop Vol. 1",
                durationMs = 240000L,
                coverUrl = "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Focus",
                moods = listOf("Focus", "Lo-Fi"),
                isInstrumental = true,
                acousticness = 0.85,
                energy = 0.35
            ),
            TrackDto(
                id = "cur_foc_2",
                title = "Rain & Sitar Chillhop",
                artist = "Desi Lo-Fi Beats",
                album = "Monsoon Beats",
                durationMs = 215000L,
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Focus",
                moods = listOf("Focus", "Instrumental"),
                isInstrumental = true,
                acousticness = 0.80,
                energy = 0.40
            ),
            TrackDto(
                id = "cur_foc_3",
                title = "Bansuri Deep Focus",
                artist = "Pt. Hariprasad Chaurasia",
                album = "Meditative Ragas",
                durationMs = 360000L,
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Focus",
                moods = listOf("Focus", "Classical"),
                isInstrumental = true,
                acousticness = 0.92,
                energy = 0.28
            ),
            TrackDto(
                id = "cur_foc_4",
                title = "Alpha Waves Flow State",
                artist = "Zynera Neural Audio",
                album = "Brainwave Resonance",
                durationMs = 300000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Focus",
                moods = listOf("Focus", "Ambient"),
                isInstrumental = true,
                acousticness = 0.78,
                energy = 0.30
            ),
            TrackDto(
                id = "cur_foc_5",
                title = "Coffee Shop Ambient Piano",
                artist = "Study Acoustics",
                album = "Midnight Study",
                durationMs = 270000L,
                coverUrl = "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Focus",
                moods = listOf("Focus", "Piano"),
                isInstrumental = true,
                acousticness = 0.88,
                energy = 0.32
            )
        )

        "Party" -> listOf(
            TrackDto(
                id = "cur_pty_1",
                title = "Kala Chashma",
                artist = "Badshah, Neha Kakkar",
                album = "Baar Baar Dekho",
                durationMs = 187000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                mood = "Party",
                moods = listOf("Party", "Club"),
                danceability = 0.95,
                energy = 0.92
            ),
            TrackDto(
                id = "cur_pty_2",
                title = "Tauba Tauba",
                artist = "Karan Aujla",
                album = "Bad Newz",
                durationMs = 207000L,
                coverUrl = "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Party",
                moods = listOf("Party", "Punjabi Pop"),
                danceability = 0.90,
                energy = 0.88
            ),
            TrackDto(
                id = "cur_pty_3",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                durationMs = 200000L,
                coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Party",
                moods = listOf("Party", "Dance Pop"),
                danceability = 0.85,
                energy = 0.91
            ),
            TrackDto(
                id = "cur_pty_4",
                title = "Levitating",
                artist = "Dua Lipa",
                album = "Future Nostalgia",
                durationMs = 203000L,
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                mood = "Party",
                moods = listOf("Party", "Disco"),
                danceability = 0.92,
                energy = 0.88
            ),
            TrackDto(
                id = "cur_pty_5",
                title = "Kar Gayi Chull",
                artist = "Badshah, Fazilpuria",
                album = "Kapoor & Sons",
                durationMs = 186000L,
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Party",
                moods = listOf("Party", "Bollywood"),
                danceability = 0.94,
                energy = 0.93
            )
        )

        "Sleep" -> listOf(
            TrackDto(
                id = "cur_slp_1",
                title = "Deep Delta Sleep 432Hz",
                artist = "Zynera Sleep Lab",
                album = "Sound Asleep",
                durationMs = 420000L,
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Sleep",
                moods = listOf("Sleep", "Ambient"),
                isInstrumental = true,
                acousticness = 0.95,
                energy = 0.15
            ),
            TrackDto(
                id = "cur_slp_2",
                title = "Moonlit Raga Yaman",
                artist = "Midnight Bansuri & Tanpura",
                album = "Nocturnal Peace",
                durationMs = 480000L,
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Sleep",
                moods = listOf("Sleep", "Indian Classical"),
                isInstrumental = true,
                acousticness = 0.96,
                energy = 0.18
            ),
            TrackDto(
                id = "cur_slp_3",
                title = "Soft Rain & Ambient Chords",
                artist = "Sleep Harmonies",
                album = "Rainfall Slumber",
                durationMs = 390000L,
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Sleep",
                moods = listOf("Sleep", "Nature Sounds"),
                isInstrumental = true,
                acousticness = 0.90,
                energy = 0.20
            ),
            TrackDto(
                id = "cur_slp_4",
                title = "Celestial Night Drift",
                artist = "Calm Slumber",
                album = "Cosmic Dreams",
                durationMs = 450000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Sleep",
                moods = listOf("Sleep", "Drone"),
                isInstrumental = true,
                acousticness = 0.94,
                energy = 0.12
            ),
            TrackDto(
                id = "cur_slp_5",
                title = "Weightless Slumber",
                artist = "Deep Ambient Soundscape",
                album = "Restful Night",
                durationMs = 510000L,
                coverUrl = "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Sleep",
                moods = listOf("Sleep", "Relaxation"),
                isInstrumental = true,
                acousticness = 0.92,
                energy = 0.16
            )
        )

        "Devotional" -> listOf(
            TrackDto(
                id = "cur_dev_1",
                title = "Sampoorna Bhagavad Gita (18 Adhyayas)",
                artist = "Shailendra Bharti",
                album = "Shrimad Bhagavad Gita",
                durationMs = 3600000L,
                coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                mood = "Devotional",
                moods = listOf("Devotional", "Spirituality"),
                acousticness = 0.95,
                energy = 0.35
            ),
            TrackDto(
                id = "cur_dev_2",
                title = "Namo Namo",
                artist = "Amit Trivedi",
                album = "Kedarnath",
                durationMs = 322000L,
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                mood = "Devotional",
                moods = listOf("Devotional", "Shiva"),
                acousticness = 0.75,
                energy = 0.55
            ),
            TrackDto(
                id = "cur_dev_3",
                title = "Kun Faya Kun",
                artist = "A.R. Rahman, Javed Ali, Mohit Chauhan",
                album = "Rockstar",
                durationMs = 473000L,
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                mood = "Devotional",
                moods = listOf("Devotional", "Sufi"),
                acousticness = 0.80,
                energy = 0.48
            ),
            TrackDto(
                id = "cur_dev_4",
                title = "Shri Krishna Govind Hare Murari",
                artist = "Ravindra Jain",
                album = "Bhakti Ras",
                durationMs = 312000L,
                coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                mood = "Devotional",
                moods = listOf("Devotional", "Krishna Bhakti"),
                acousticness = 0.90,
                energy = 0.40
            ),
            TrackDto(
                id = "cur_dev_5",
                title = "Shiv Tandav Stotram",
                artist = "Shankar Mahadevan",
                album = "Shiv Stotra",
                durationMs = 374000L,
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                mood = "Devotional",
                moods = listOf("Devotional", "Stotram"),
                acousticness = 0.60,
                energy = 0.72
            )
        )

        else -> emptyList()
    }
}

private fun getCuratedMadeForYouCards(
    allTrending: List<TrackDto>,
    continueListening: List<TrackDto>
): List<CuratedSectionCard> {
    val chillPool = getCuratedTracksForMood("Chill")
    val workoutPool = getCuratedTracksForMood("Workout")
    val romanticPool = getCuratedTracksForMood("Romantic")
    val sadPool = getCuratedTracksForMood("Sad")
    val happyPool = getCuratedTracksForMood("Happy")

    val dailyMixTracks = listOfNotNull(
        sadPool.firstOrNull(), // Channa Mereya
        happyPool.firstOrNull(), // Levitating
        chillPool.firstOrNull(), // Husn
        workoutPool.firstOrNull(), // Starboy
        romanticPool.getOrNull(1), // Apna Bana Le
        chillPool.getOrNull(1), // Dooron Dooron
        romanticPool.firstOrNull() // Kesariya
    )

    val chillVibesTracks = chillPool + listOf(
        TrackDto(
            id = "cur_chl_extra_1",
            title = "Alag Aasmaan",
            artist = "Anuv Jain",
            album = "Alag Aasmaan - Single",
            durationMs = 212000L,
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            mood = "Chill",
            moods = listOf("Chill", "Indie"),
            acousticness = 0.85,
            energy = 0.30
        ),
        TrackDto(
            id = "cur_chl_extra_2",
            title = "Kho Gaye Hum Kahan",
            artist = "Jasleen Royal, Prateek Kuhad",
            album = "Baar Baar Dekho",
            durationMs = 213000L,
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            mood = "Chill",
            moods = listOf("Chill", "Mellow"),
            acousticness = 0.80,
            energy = 0.36
        )
    )

    val workoutTracks = workoutPool + listOf(
        TrackDto(
            id = "cur_wrk_extra_1",
            title = "Malhari",
            artist = "Vishal Dadlani",
            album = "Bajirao Mastani",
            durationMs = 245000L,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            mood = "Workout",
            moods = listOf("Workout", "Victory"),
            energy = 0.94,
            bpm = 148.0
        ),
        TrackDto(
            id = "cur_wrk_extra_2",
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            durationMs = 204000L,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            mood = "Workout",
            moods = listOf("Workout", "Anthem"),
            energy = 0.88,
            bpm = 125.0
        )
    )

    val romanticTracks = romanticPool + listOf(
        TrackDto(
            id = "cur_rom_extra_1",
            title = "O Maahi",
            artist = "Arijit Singh, Pritam",
            album = "Dunki",
            durationMs = 233000L,
            coverUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            mood = "Romantic",
            moods = listOf("Romantic", "Melody"),
            acousticness = 0.52,
            energy = 0.60
        )
    )

    return listOf(
        CuratedSectionCard(
            id = "sec_daily_mix",
            title = "Your Daily Mix",
            subtitle = "Personalized Daily Rotation",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
            tracks = dailyMixTracks
        ),
        CuratedSectionCard(
            id = "sec_chill_vibes",
            title = "Chill Vibes",
            subtitle = "Relax & Unwind",
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
            tracks = chillVibesTracks
        ),
        CuratedSectionCard(
            id = "sec_workout",
            title = "Workout Energy",
            subtitle = "High BPM & Adrenaline",
            coverUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600",
            tracks = workoutTracks
        ),
        CuratedSectionCard(
            id = "sec_romantic",
            title = "Romantic Hits",
            subtitle = "Feel the Love",
            coverUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600",
            tracks = romanticTracks
        )
    )
}

private fun getCuratedPopularPlaylistCards(allTrending: List<TrackDto>): List<CuratedSectionCard> {
    val happyPool = getCuratedTracksForMood("Happy")
    val focusPool = getCuratedTracksForMood("Focus")
    val chillPool = getCuratedTracksForMood("Chill")
    val workoutPool = getCuratedTracksForMood("Workout")

    val bollywoodBopTracks = listOf(
        TrackDto(
            id = "pop_bop_1",
            title = "Tauba Tauba",
            artist = "Karan Aujla",
            album = "Bad Newz",
            durationMs = 214000L,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            mood = "Party",
            moods = listOf("Party", "Dance"),
            danceability = 0.92,
            energy = 0.90
        )
    ) + happyPool.filter { it.title.contains("Badtameez") || it.title.contains("Gallan") || it.title.contains("London") } +
    listOf(
        TrackDto(
            id = "pop_bop_5",
            title = "Kar Gayi Chull",
            artist = "Badshah, Neha Kakkar",
            album = "Kapoor & Sons",
            durationMs = 187000L,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            mood = "Party",
            moods = listOf("Party", "Banger"),
            danceability = 0.90,
            energy = 0.88
        ),
        TrackDto(
            id = "pop_bop_6",
            title = "Kala Chashma",
            artist = "Amar Arshi, Badshah, Neha Kakkar",
            album = "Baar Baar Dekho",
            durationMs = 194000L,
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            mood = "Party",
            moods = listOf("Party", "Wedding"),
            danceability = 0.95,
            energy = 0.92
        )
    )

    val actionIndiaTracks = listOfNotNull(
        workoutPool.find { it.title.contains("Arjan") },
        workoutPool.find { it.title.contains("Zinda") },
        workoutPool.find { it.title.contains("Sultan") },
        TrackDto(
            id = "pop_act_4",
            title = "Malhari",
            artist = "Vishal Dadlani",
            album = "Bajirao Mastani",
            durationMs = 245000L,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            mood = "Workout",
            energy = 0.94
        ),
        TrackDto(
            id = "pop_act_5",
            title = "Dhoom Again",
            artist = "Vishal Dadlani",
            album = "Dhoom 2",
            durationMs = 302000L,
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            mood = "Workout",
            energy = 0.91
        )
    )

    val cyberPunkTracks = listOf(
        TrackDto(
            id = "pop_cyb_1",
            title = "Starboy (Cyber Synthwave)",
            artist = "The Weeknd, Daft Punk",
            album = "Starboy Synth",
            durationMs = 230000L,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            mood = "Party",
            energy = 0.85
        ),
        TrackDto(
            id = "pop_cyb_2",
            title = "Blinding Lights (Neon Club)",
            artist = "The Weeknd",
            album = "After Hours Club",
            durationMs = 200000L,
            coverUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            mood = "Party",
            energy = 0.91
        ),
        TrackDto(
            id = "pop_cyb_3",
            title = "Midnight Cyber Varanasi",
            artist = "Zynera Sound Lab",
            album = "Desi Cyber Vol. 1",
            durationMs = 240000L,
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            mood = "Focus",
            energy = 0.70
        ),
        TrackDto(
            id = "pop_cyb_4",
            title = "Neon Mumbai 2099",
            artist = "Desi Synth Collective",
            album = "Cyber India",
            durationMs = 215000L,
            coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            mood = "Party",
            energy = 0.78
        )
    )

    val masalaHitsTracks = listOf(
        TrackDto(
            id = "pop_msl_1",
            title = "Chaiyya Chaiyya",
            artist = "Sukhwinder Singh, Sapna Awasthi",
            album = "Dil Se..",
            durationMs = 392000L,
            coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            mood = "Party",
            energy = 0.88
        ),
        TrackDto(
            id = "pop_msl_2",
            title = "Senorita",
            artist = "Farhan Akhtar, Hrithik Roshan, Abhay Deol",
            album = "Zindagi Na Milegi Dobara",
            durationMs = 231000L,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            mood = "Happy",
            energy = 0.82
        ),
        TrackDto(
            id = "pop_msl_3",
            title = "Mauja Hi Mauja",
            artist = "Mika Singh",
            album = "Jab We Met",
            durationMs = 244000L,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            mood = "Party",
            energy = 0.90
        )
    ) + happyPool.filter { it.title.contains("Gallan") || it.title.contains("London") }

    val loFiIndiaTracks = focusPool + chillPool.filter { it.title.contains("Husn") || it.title.contains("Dooron") || it.title.contains("Baarishein") }

    return listOf(
        CuratedSectionCard(
            id = "sec_bop",
            title = "Bollywood Bop",
            subtitle = "Top High-Energy Hits",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            tracks = bollywoodBopTracks
        ),
        CuratedSectionCard(
            id = "sec_action",
            title = "Bollywood Action India",
            subtitle = "Adrenaline Blockbusters",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            tracks = actionIndiaTracks
        ),
        CuratedSectionCard(
            id = "sec_cyber",
            title = "Bollywood CyberPunk",
            subtitle = "Futuristic Synth Desi",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            tracks = cyberPunkTracks
        ),
        CuratedSectionCard(
            id = "sec_masala",
            title = "Masala Hits",
            subtitle = "Crowd Pleasing Anthems",
            coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            tracks = masalaHitsTracks
        ),
        CuratedSectionCard(
            id = "sec_lofi",
            title = "Lo-Fi India",
            subtitle = "Midnight Mellow Vibes",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            tracks = loFiIndiaTracks
        )
    )
}

