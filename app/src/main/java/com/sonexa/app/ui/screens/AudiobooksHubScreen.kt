package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.AudiobookCategoryDto
import com.sonexa.app.data.model.AudiobookDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AudiobookViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

@Composable
fun AudiobooksHubScreen(
    onNavigateBack: () -> Unit = {},
    onOpenAudiobookDetail: (String) -> Unit,
    onOpenAudiobookPlayer: () -> Unit,
    onOpenSearch: () -> Unit = {},
    audiobookViewModel: AudiobookViewModel = viewModel(),
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by audiobookViewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.uiState.collectAsState()

    val progressMap by AudiobookStore.progressMap.collectAsState()
    val savedIds by AudiobookStore.savedAudiobookIds.collectAsState()

    val allAudiobooks = uiState.audiobooks
    val classicAudiobooks = remember(allAudiobooks) { allAudiobooks.filter { it.isClassic || it.isPublicDomain } }
    val modernAudiobooks = remember(allAudiobooks) { allAudiobooks.filter { !it.isClassic } }
    val featuredHero = remember(allAudiobooks) { allAudiobooks.firstOrNull { it.rating >= 4.9f } ?: allAudiobooks.firstOrNull() }

    val continueListeningList = remember(allAudiobooks, progressMap) {
        progressMap.keys.mapNotNull { id ->
            allAudiobooks.find { it.id == id }
        }.ifEmpty {
            allAudiobooks.take(2)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZyneraBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. Top Bar / Header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Audiobooks",
                            style = AppleType.largeTitle,
                            color = ZyneraTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ZyneraPrimary.copy(alpha = 0.25f))
                                .border(0.8.dp, ZyneraPrimaryBright.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ORIGINALS & CLASSICS",
                                style = AppleType.caption2Bold,
                                color = ZyneraPrimaryBright
                            )
                        }
                    }
                    Text(
                        text = "Immersive storytelling, classic literature & spoken word",
                        style = AppleType.footnote,
                        color = ZyneraTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppleMaterials.glassFill)
                        .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                        .appleBounce { onOpenSearch() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Audiobooks",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── 2. Category Chips Filter ────────────────────────────────────
            val categoryChips = listOf("All") + uiState.categories.map { it.name }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryChips.forEach { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ZyneraPrimary else AppleMaterials.glassFill)
                            .border(
                                1.dp,
                                if (isSelected) ZyneraPrimaryBright else AppleMaterials.subtleBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .appleBounce { audiobookViewModel.selectCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            style = AppleType.footnoteBold,
                            color = if (isSelected) Color.White else ZyneraTextSecondary
                        )
                    }
                }
            }

            // ── 3. Multilingual Filter Row ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val languages = listOf("All" to "All") + uiState.languages.map { it.code to "${it.name} (${it.nativeName})" }
                languages.forEach { (code, label) ->
                    val isSelected = uiState.selectedLanguage == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ZyneraSecondary.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                0.8.dp,
                                if (isSelected) ZyneraSecondary else AppleMaterials.subtleBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .appleBounce { audiobookViewModel.selectLanguage(code) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = AppleType.caption1,
                            color = if (isSelected) ZyneraSecondary else ZyneraTextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 4. Hero Featured Audiobook Showcase ─────────────────────────
            if (featuredHero != null) {
                AudiobookHeroCard(
                    audiobook = featuredHero,
                    onOpenDetail = { onOpenAudiobookDetail(featuredHero.id) },
                    onPlay = {
                        playbackViewModel.playAudiobook(featuredHero, 0, 0L)
                        onOpenAudiobookPlayer()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 5. "Continue Listening" (with progress & resume) ────────────
            if (continueListeningList.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Continue Listening",
                    subtitle = "Pick up where you left off",
                    actionText = "View Library",
                    onActionClick = {}
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueListeningList) { book ->
                        val savedProgress = progressMap[book.id]
                        val percent = savedProgress?.completionPercentage ?: 15
                        val chapterNum = savedProgress?.currentChapterNumber ?: 1

                        AudiobookContinueCard(
                            audiobook = book,
                            chapterNumber = chapterNum,
                            completionPercentage = percent,
                            onClick = { onOpenAudiobookDetail(book.id) },
                            onResume = {
                                playbackViewModel.playAudiobook(
                                    audiobook = book,
                                    chapterIndex = (chapterNum - 1).coerceAtLeast(0),
                                    startPositionMs = savedProgress?.positionMs ?: 0L
                                )
                                onOpenAudiobookPlayer()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── 6. Classic Literature & Public-Domain Stories ───────────────
            if (classicAudiobooks.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Classic Literature & Public Domain",
                    subtitle = "Munshi Premchand, Tagore, Doyle & Austen masterworks",
                    actionText = "Explore All",
                    onActionClick = { audiobookViewModel.selectCategory("Classic Literature") }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(classicAudiobooks) { book ->
                        AudiobookMediaCard(
                            audiobook = book,
                            badge = if (book.isPublicDomain) "PUBLIC DOMAIN" else "CLASSIC",
                            onClick = { onOpenAudiobookDetail(book.id) },
                            onPlay = {
                                playbackViewModel.playAudiobook(book, 0, 0L)
                                onOpenAudiobookPlayer()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── 7. Modern Audiobooks & Personal Development ─────────────────
            if (modernAudiobooks.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Modern Voices & Bestsellers",
                    subtitle = "Finance, psychological habits, and contemporary fiction",
                    actionText = "See All",
                    onActionClick = {}
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(modernAudiobooks) { book ->
                        AudiobookMediaCard(
                            audiobook = book,
                            badge = "MODERN",
                            onClick = { onOpenAudiobookDetail(book.id) },
                            onPlay = {
                                playbackViewModel.playAudiobook(book, 0, 0L)
                                onOpenAudiobookPlayer()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── 8. Browse Categories Grid ───────────────────────────────────
            AudiobookSectionHeader(
                title = "Browse Categories",
                subtitle = "Find your next great listen by genre",
                actionText = "",
                onActionClick = {}
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.categories.chunked(2).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCats.forEach { cat ->
                            AudiobookCategoryTile(
                                category = cat,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    audiobookViewModel.selectCategory(cat.name)
                                }
                            )
                        }
                        if (rowCats.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI Components for Audiobook Discovery
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AudiobookHeroCard(
    audiobook: AudiobookDto,
    onOpenDetail: () -> Unit,
    onPlay: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF26123E),
                        Color(0xFF1E1033),
                        Color(0xFF120C22)
                    )
                )
            )
            .border(1.dp, AppleMaterials.specularBorder, RoundedCornerShape(22.dp))
            .clickable { onOpenDetail() }
    ) {
        // Book cover artwork on the right with smooth scrim
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(audiobook.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = audiobook.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .width(180.dp)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1A0C2E),
                            Color(0xF01A0C2E),
                            Color(0xBB1A0C2E),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ZyneraPrimary.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FEATURED LISTEN",
                        style = AppleType.caption2Bold,
                        color = ZyneraPrimaryBright
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = audiobook.title,
                    style = AppleType.title2Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "By ${audiobook.author}",
                    style = AppleType.subheadline,
                    color = ZyneraTextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${audiobook.formattedDuration} • Narrated by ${audiobook.narrator}",
                    style = AppleType.caption1,
                    color = ZyneraSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .appleBounce { onPlay() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Listen",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start Listening",
                            style = AppleType.footnoteBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "⭐ ${audiobook.rating}",
                    style = AppleType.caption1Bold,
                    color = Color(0xFFFBBF24)
                )
            }
        }
    }
}

@Composable
private fun AudiobookContinueCard(
    audiobook: AudiobookDto,
    chapterNumber: Int,
    completionPercentage: Int,
    onClick: () -> Unit,
    onResume: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ZyneraSurface)
            .border(1.dp, ZyneraBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(audiobook.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = audiobook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audiobook.title,
                        style = AppleType.subheadlineBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ch $chapterNumber • $completionPercentage%",
                        style = AppleType.caption1,
                        color = ZyneraPrimaryBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ZyneraPrimary)
                            .appleBounce { onResume() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((completionPercentage / 100f).coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .background(ZyneraVioletGradient)
                )
            }
        }
    }
}

@Composable
private fun AudiobookMediaCard(
    audiobook: AudiobookDto,
    badge: String,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(148.dp)
            .appleBounce { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ZyneraSurfaceElevated)
                .border(0.8.dp, AppleMaterials.specularBorder, RoundedCornerShape(18.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(audiobook.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = audiobook.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = AppleType.caption2Bold,
                    color = ZyneraPrimaryBright
                )
            }

            // Quick Play FAB bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .appleBounce { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = audiobook.title,
            style = AppleType.subheadlineBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = audiobook.author,
            style = AppleType.caption1,
            color = ZyneraTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AudiobookCategoryTile(
    category: AudiobookCategoryDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ZyneraSurface)
            .border(0.8.dp, ZyneraBorder, RoundedCornerShape(16.dp))
            .appleBounce { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = AppleType.subheadlineBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${category.bookCount} audiobooks",
                    style = AppleType.caption2,
                    color = ZyneraPrimaryBright
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ZyneraTextSubtle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AudiobookSectionHeader(
    title: String,
    subtitle: String = "",
    actionText: String = "",
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = AppleType.title3Bold,
                color = Color.White
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = AppleType.caption1,
                    color = ZyneraTextSecondary
                )
            }
        }

        if (actionText.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Text(
                    text = actionText,
                    style = AppleType.footnoteBold,
                    color = ZyneraPrimaryBright
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ZyneraPrimaryBright,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
