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
import androidx.compose.material.icons.outlined.Settings
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

@Composable
fun HomeFeedContent(
    avatarInitial: String,
    avatarUrl: String = "",
    userDisplayName: String = "Raju",
    selectedFeedCategory: String,
    onSelectFeedCategory: (String) -> Unit,
    onOpenSearchTab: () -> Unit = {},
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

    // Curated dynamic lists for "Made For You" and "Popular Playlists"
    val madeForYouList = remember(allTrending, continueListening) {
        val pool = (continueListening + allTrending).distinctBy { it.id }
        listOf(
            Triple("Your Daily Mix", "Songs picked just for you", pool.firstOrNull()?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500"),
            Triple("Chill Vibes", "Relax & Unwind", pool.getOrNull(1)?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500"),
            Triple("Workout", "High Energy", pool.getOrNull(2)?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500"),
            Triple("Romantic Hits", "Feel the Love", pool.getOrNull(3)?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=500")
        )
    }

    val popularPlaylistsList = remember(allAlbums, allTrending) {
        listOf(
            Triple("Bollywood Bop", "Top High-Energy Hits", "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg"),
            Triple("Bollywood Action India", "Adrenaline Blockbusters", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg"),
            Triple("Bollywood CyberPunk", "Futuristic Synth Desi", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500"),
            Triple("Masala Hits", "Crowd Pleasing Anthems", "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg"),
            Triple("Lo-Fi India", "Midnight Mellow Vibes", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg")
        )
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
                            style = AppleType.footnote,
                            color = ZyneraTextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userDisplayName,
                                style = AppleType.title2Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "👋", fontSize = 18.sp)
                        }
                    }
                }

                // Apple Frosted Glass Header Action Buttons (Notification & Settings)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppleMaterials.glassFill)
                            .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                            .appleBounce { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
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
                    .appleBounce { onOpenSearchTab() }
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
                        modifier = Modifier.weight(1f)
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

                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = SonexaTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
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
                listOf(
                    HeroSlideData(
                        prefix = "Music for",
                        highlight = "Every Mood",
                        description = "Discover, vibe and feel the music that understands you.",
                        action = "Play Now",
                        imageUrl = allTrending.firstOrNull()?.effectiveCoverUrl ?: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                        gradient = listOf(Color(0xFF1E0B38), Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A)),
                        onClick = {
                            val topSong = allTrending.firstOrNull() ?: continueListening.firstOrNull()
                            onPlayTrack(topSong, allTrending, "Music for Every Mood")
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
                            onPlayTrack(topSong, allTrending, "Trending Now")
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
                            val topSong = allTrending.getOrNull(2) ?: allTrending.firstOrNull()
                            onPlayTrack(topSong, allTrending, "New Voices")
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(colors))
                            .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(16.dp))
                            .appleBounce {
                                val moodTracks = allTrending.filter {
                                    it.genre.contains(moodName, ignoreCase = true) ||
                                    it.title.contains(moodName, ignoreCase = true)
                                }.ifEmpty { allTrending }
                                onPlayTrack(moodTracks.firstOrNull(), moodTracks, "$moodName Mood")
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = moodName,
                                style = AppleType.footnoteBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 6. "Made for You" Section ────────────────────────────────────────
            AppleSectionHeader(
                title = "Made for You",
                actionText = "See All",
                onActionClick = { onOpenLikedPlaylist() }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(madeForYouList) { (title, subtitle, coverUrl) ->
                    AppleMediaCard(
                        title = title,
                        subtitle = subtitle,
                        imageUrl = coverUrl,
                        onClick = {
                            val track = allTrending.firstOrNull()
                            onPlayTrack(track, allTrending, title)
                        },
                        onPlayClick = {
                            val track = allTrending.firstOrNull()
                            onPlayTrack(track, allTrending, title)
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
                onActionClick = {}
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
                actionText = "See All",
                onActionClick = {}
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(popularPlaylistsList) { (title, subtitle, coverUrl) ->
                    AppleMediaCard(
                        title = title,
                        subtitle = subtitle,
                        imageUrl = coverUrl,
                        onClick = {
                            val track = allTrending.firstOrNull()
                            onPlayTrack(track, allTrending, title)
                        },
                        onPlayClick = {
                            val track = allTrending.firstOrNull()
                            onPlayTrack(track, allTrending, title)
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
                .background(AppleMaterials.cardSurface)
                .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(18.dp))
        ) {
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
