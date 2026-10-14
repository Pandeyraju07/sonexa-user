package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onOpenLibrary: () -> Unit = {},
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

    // Section datasets matching the screenshot & user request
    val classicAudiobooks = remember(allAudiobooks) {
        // Includes the masterworks shown in the screenshot
        allAudiobooks.filter { it.isClassic || it.isPublicDomain || it.id in listOf("ab_godan", "ab_idgah", "ab_kabuliwala", "ab_atomic_habits_guide", "ab_psychology_wealth") }
    }

    val mostListenedAudiobooks = remember(allAudiobooks) {
        // Bhagavad Gita by Shailendra Bharti featured first, followed by top listened titles
        val gita = allAudiobooks.find { it.id == "ab_bhagavad_gita" }
        val others = allAudiobooks.filter { it.id != "ab_bhagavad_gita" }
            .sortedByDescending { it.reviewsCount }
        listOfNotNull(gita) + others
    }

    val featuredHero = remember(allAudiobooks) {
        allAudiobooks.firstOrNull { it.id == "ab_godan" }
            ?: allAudiobooks.firstOrNull { it.rating >= 4.9f }
            ?: allAudiobooks.firstOrNull()
    }

    val continueListeningList = remember(allAudiobooks, progressMap) {
        val active = progressMap.keys.mapNotNull { id ->
            allAudiobooks.find { it.id == id }
        }
        if (active.isNotEmpty()) {
            active
        } else {
            // Default 2 cards matching user screenshot: Idgah & Kabuliwala
            listOfNotNull(
                allAudiobooks.find { it.id == "ab_idgah" },
                allAudiobooks.find { it.id == "ab_kabuliwala" }
            ).ifEmpty { allAudiobooks.take(2) }
        }
    }

    // Interactive See All sheet state
    var seeAllSectionTitle by remember { mutableStateOf<String?>(null) }
    var seeAllCategoryFilter by remember { mutableStateOf<String?>(null) }
    var isAudiobookSearchOpen by remember { mutableStateOf(false) }

    // Intercept back button when Search overlay or See All sheet is open
    BackHandler(enabled = isAudiobookSearchOpen || seeAllSectionTitle != null) {
        if (isAudiobookSearchOpen) {
            isAudiobookSearchOpen = false
        } else {
            seeAllSectionTitle = null
            seeAllCategoryFilter = null
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
            // ── 1. Top Bar / Header (Guaranteed single row) ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "Audiobooks",
                        style = AppleType.largeTitle,
                        color = ZyneraTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Immersive storytelling, classic literature & spoken word",
                        style = AppleType.footnote,
                        color = ZyneraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Single-Row Circular Search Icon Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF2E154F),
                                    Color(0xFF1B0D30),
                                    Color(0xFF251040)
                                )
                            )
                        )
                        .border(
                            1.4.dp,
                            Brush.linearGradient(
                                listOf(
                                    ZyneraPrimaryBright,
                                    Color(0xFFEC4899),
                                    Color(0xFFFBBF24)
                                )
                            ),
                            CircleShape
                        )
                        .appleBounce { isAudiobookSearchOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Audiobooks",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── 2. Category Chips Filter Row (with right chevron button) ────
            val defaultCategories = listOf(
                "All",
                "Most Listened",
                "Classic Literature",
                "Hindi & Urdu Literature",
                "Self-Improvement",
                "Business & Finance",
                "Fiction & Drama",
                "Sci-Fi & Fantasy",
                "Philosophy & Spirituality",
                "Short Stories"
            )
            val categoryChips = remember(uiState.categories) {
                if (uiState.categories.isNotEmpty()) {
                    listOf("All", "Most Listened") + uiState.categories.map { it.name }.filter { it !in listOf("All", "Most Listened") }
                } else defaultCategories
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
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

                // Chevron arrow button on the right edge (as shown in screenshot)
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppleMaterials.glassFill)
                        .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                        .appleBounce {
                            seeAllSectionTitle = "Browse All Audiobooks"
                            seeAllCategoryFilter = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Explore All Categories",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
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

            // ── 4. Hero Featured Audiobook Showcase (Godan) ────────────────
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

            // ── 5. "Continue Listening" (with View Library ->) ─────────────
            if (continueListeningList.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Continue Listening",
                    subtitle = "Pick up where you left off",
                    actionText = "View Library",
                    onActionClick = { onOpenLibrary() }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueListeningList) { book ->
                        val savedProgress = progressMap[book.id]
                        val percent = savedProgress?.completionPercentage
                            ?: when (book.id) {
                                "ab_idgah" -> 25
                                "ab_kabuliwala" -> 10
                                else -> 15
                            }
                        val chapterNum = savedProgress?.currentChapterNumber
                            ?: when (book.id) {
                                "ab_idgah" -> 2
                                "ab_kabuliwala" -> 1
                                else -> 1
                            }

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

            // ── 6. Classic Literature & Public Domain (with See All ->) ────
            if (classicAudiobooks.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Classic Literature & Public Domain",
                    subtitle = "Munshi Premchand, Tagore, Doyle & Austen masterworks",
                    actionText = "See All",
                    onActionClick = {
                        seeAllSectionTitle = "Classic Literature & Public Domain"
                        seeAllCategoryFilter = "Classic Literature"
                    }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(classicAudiobooks) { book ->
                        val badge = when {
                            book.isPublicDomain -> "CLASSIC"
                            book.isClassic -> "CLASSIC"
                            else -> "MODERN"
                        }
                        AudiobookMediaCard(
                            audiobook = book,
                            badge = badge,
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

            // ── 7. Most Listened Audiobooks (featuring Complete Geeta by Shailendra Bharti) ─
            if (mostListenedAudiobooks.isNotEmpty()) {
                AudiobookSectionHeader(
                    title = "Most Listened Audiobooks",
                    subtitle = "Top spiritual masterworks & trending bestsellers",
                    actionText = "See All",
                    onActionClick = {
                        seeAllSectionTitle = "Most Listened Audiobooks"
                        seeAllCategoryFilter = "Most Listened"
                    }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mostListenedAudiobooks) { book ->
                        val badge = when (book.id) {
                            "ab_bhagavad_gita" -> "18 ADHYAYAS"
                            "ab_godan" -> "#2 TRENDING"
                            "ab_atomic_habits_guide" -> "BESTSELLER"
                            "ab_psychology_wealth" -> "TOP WEALTH"
                            else -> "POPULAR"
                        }
                        AudiobookMediaCard(
                            audiobook = book,
                            badge = badge,
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

            // ── 8. Browse Categories Grid (with icons matching screenshot) ──
            val orderedCategoryNames = listOf(
                "Classic Literature",
                "Hindi & Urdu Literature",
                "Self-Improvement",
                "Business & Finance",
                "Fiction & Drama",
                "Sci-Fi & Fantasy",
                "Philosophy & Spirituality",
                "Short Stories"
            )

            val displayCategories = remember(uiState.categories) {
                orderedCategoryNames.map { name ->
                    uiState.categories.find { it.name.equals(name, ignoreCase = true) }
                        ?: AudiobookCategoryDto(
                            id = "cat_${name.lowercase().replace(" ", "_")}",
                            name = name,
                            slug = name.lowercase().replace(" ", "-"),
                            description = "Curated $name audiobooks",
                            bookCount = when (name) {
                                "Classic Literature" -> 18
                                "Hindi & Urdu Literature" -> 12
                                "Self-Improvement" -> 24
                                "Business & Finance" -> 15
                                "Fiction & Drama" -> 30
                                "Sci-Fi & Fantasy" -> 8
                                "Philosophy & Spirituality" -> 16
                                else -> 20
                            }
                        )
                }
            }

            AudiobookSectionHeader(
                title = "Browse Categories",
                subtitle = "Find your next great listen by genre",
                actionText = "See All",
                onActionClick = {
                    seeAllSectionTitle = "All Categories & Audiobooks"
                    seeAllCategoryFilter = null
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                displayCategories.chunked(2).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCats.forEach { cat ->
                            AudiobookCategoryTile(
                                category = cat,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    seeAllSectionTitle = cat.name
                                    seeAllCategoryFilter = cat.name
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

        // ── 9. Fully-working "See All" Interactive Modal / Catalog Overlay ────
        AnimatedVisibility(
            visible = seeAllSectionTitle != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val title = seeAllSectionTitle ?: "Audiobooks Catalog"
            AudiobookSeeAllSheet(
                title = title,
                initialCategory = seeAllCategoryFilter,
                allAudiobooks = allAudiobooks,
                onClose = {
                    seeAllSectionTitle = null
                    seeAllCategoryFilter = null
                },
                onOpenAudiobookDetail = { bookId ->
                    seeAllSectionTitle = null
                    seeAllCategoryFilter = null
                    onOpenAudiobookDetail(bookId)
                },
                onPlayAudiobook = { book ->
                    playbackViewModel.playAudiobook(book, 0, 0L)
                    onOpenAudiobookPlayer()
                }
            )
        }

        // ── 10. Dedicated In-Page Audiobook Search Overlay (Audiobooks Only) ───
        AnimatedVisibility(
            visible = isAudiobookSearchOpen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            AudiobookSearchOverlay(
                allAudiobooks = allAudiobooks,
                onClose = { isAudiobookSearchOpen = false },
                onOpenAudiobookDetail = { bookId ->
                    isAudiobookSearchOpen = false
                    onOpenAudiobookDetail(bookId)
                },
                onPlayAudiobook = { book ->
                    playbackViewModel.playAudiobook(book, 0, 0L)
                    onOpenAudiobookPlayer()
                }
            )
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
            Column(modifier = Modifier.fillMaxWidth(0.68f)) {
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
            .width(235.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ZyneraSurface)
            .border(1.dp, ZyneraBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ch $chapterNumber • $completionPercentage%",
                        style = AppleType.caption1Bold,
                        color = ZyneraPrimaryBright
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = audiobook.author,
                        style = AppleType.caption2,
                        color = ZyneraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ZyneraPrimary)
                        .appleBounce { onResume() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
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

            // Top-left or Bottom-left badge (matching screenshot bottom-left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
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

            // Quick Play FAB bottom right (white circle with black play icon)
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

private fun getCategoryIconAndColor(name: String): Pair<ImageVector, Color> {
    return when {
        name.contains("Classic", ignoreCase = true) ->
            Icons.AutoMirrored.Filled.MenuBook to Color(0xFFFF5C5C) // Coral red
        name.contains("Hindi", ignoreCase = true) || name.contains("Urdu", ignoreCase = true) ->
            Icons.Default.HistoryEdu to Color(0xFFA855F7) // Purple
        name.contains("Self-Improvement", ignoreCase = true) || name.contains("Growth", ignoreCase = true) ->
            Icons.Default.Psychology to Color(0xFF10B981) // Mint green
        name.contains("Business", ignoreCase = true) || name.contains("Finance", ignoreCase = true) ->
            Icons.Default.BarChart to Color(0xFF38BDF8) // Bright blue
        name.contains("Fiction", ignoreCase = true) || name.contains("Drama", ignoreCase = true) ->
            Icons.Default.TheaterComedy to Color(0xFFE879F9) // Neon magenta
        name.contains("Sci-Fi", ignoreCase = true) || name.contains("Fantasy", ignoreCase = true) ->
            Icons.Default.Public to Color(0xFF22D3EE) // Cyan planet
        name.contains("Philosophy", ignoreCase = true) || name.contains("Spirituality", ignoreCase = true) ->
            Icons.Default.SelfImprovement to Color(0xFFF472B6) // Lotus pink
        name.contains("Short", ignoreCase = true) || name.contains("Stories", ignoreCase = true) ->
            Icons.Default.Description to Color(0xFFFB923C) // Warm orange
        else ->
            Icons.AutoMirrored.Filled.MenuBook to Color(0xFFA855F7)
    }
}

@Composable
private fun AudiobookCategoryTile(
    category: AudiobookCategoryDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (icon, tintColor) = remember(category.name) { getCategoryIconAndColor(category.name) }

    Box(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ZyneraSurface)
            .border(0.8.dp, ZyneraBorder, RoundedCornerShape(16.dp))
            .appleBounce { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tintColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = AppleType.subheadlineBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category.bookCount} audiobooks",
                        style = AppleType.caption2,
                        color = ZyneraTextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ZyneraTextSubtle,
                modifier = Modifier.size(18.dp)
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
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                style = AppleType.title3Bold,
                color = Color.White
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = AppleType.caption1,
                    color = ZyneraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (actionText.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onActionClick() }
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = actionText,
                    style = AppleType.footnoteBold,
                    color = ZyneraPrimaryBright
                )
                Spacer(modifier = Modifier.width(3.dp))
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

// ─────────────────────────────────────────────────────────────────────────────
// Fully Working "See All" View Sheet / Catalog Modal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AudiobookSeeAllSheet(
    title: String,
    initialCategory: String?,
    allAudiobooks: List<AudiobookDto>,
    onClose: () -> Unit,
    onOpenAudiobookDetail: (String) -> Unit,
    onPlayAudiobook: (AudiobookDto) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(initialCategory ?: "All") }

    val filterChips = listOf(
        "All",
        "Most Listened",
        "Classic Literature",
        "Hindi & Urdu Literature",
        "Philosophy & Spirituality",
        "Self-Improvement",
        "Business & Finance",
        "Short Stories"
    )

    val filteredList = remember(allAudiobooks, searchQuery, selectedCategoryFilter) {
        allAudiobooks.filter { book ->
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Most Listened" -> "Most Listened" in book.categories || book.rating >= 4.9f
                "Classic Literature" -> book.isClassic || book.isPublicDomain || "Classics" in book.categories || "Classic Literature" in book.categories
                "Philosophy & Spirituality" -> "Philosophy & Spirituality" in book.categories || "Spirituality" in book.categories
                "Self-Improvement" -> "Self-Improvement" in book.categories
                "Business & Finance" -> "Business & Finance" in book.categories
                "Hindi & Urdu Literature" -> book.languageCode == "hi" || book.languageCode == "ur" || "Hindi Literature" in book.categories
                "Short Stories" -> "Short Stories" in book.categories
                else -> book.category.contains(selectedCategoryFilter, ignoreCase = true) ||
                        book.categories.any { it.contains(selectedCategoryFilter, ignoreCase = true) }
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                book.narrator.lowercase().contains(q) ||
                book.description.lowercase().contains(q)
            }

            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZyneraBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppleMaterials.glassFill)
                            .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                            .appleBounce { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = AppleType.title3Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${filteredList.size} audiobooks available",
                            style = AppleType.caption2,
                            color = ZyneraPrimaryBright
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .appleBounce { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Search input field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ZyneraSurface)
                    .border(1.dp, ZyneraBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ZyneraTextSubtle,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search in $title...",
                                style = AppleType.subheadline,
                                color = ZyneraTextSubtle
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterChips.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ZyneraPrimary else AppleMaterials.glassFill)
                            .border(
                                1.dp,
                                if (isSelected) ZyneraPrimaryBright else AppleMaterials.subtleBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .appleBounce { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = AppleType.caption1Bold,
                            color = if (isSelected) Color.White else ZyneraTextSecondary
                        )
                    }
                }
            }

            // Audiobooks List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No audiobooks found matching your query",
                                style = AppleType.subheadline,
                                color = ZyneraTextSecondary
                            )
                        }
                    }
                }

                items(filteredList) { book ->
                    AudiobookCatalogRowItem(
                        audiobook = book,
                        onClick = { onOpenAudiobookDetail(book.id) },
                        onPlay = { onPlayAudiobook(book) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dedicated In-Page Audiobook Search Experience (No redirect to Music Search)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AudiobookSearchOverlay(
    allAudiobooks: List<AudiobookDto>,
    onClose: () -> Unit,
    onOpenAudiobookDetail: (String) -> Unit,
    onPlayAudiobook: (AudiobookDto) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val quickPills = listOf(
        "Bhagavad Gita",
        "Shailendra Bharti",
        "Munshi Premchand",
        "Godan",
        "Rabindranath Tagore",
        "Kabuliwala",
        "Philosophy & Spirituality",
        "Self-Improvement",
        "Business & Finance"
    )

    val filteredList = remember(searchQuery, allAudiobooks) {
        if (searchQuery.isBlank()) {
            val gita = allAudiobooks.find { it.id == "ab_bhagavad_gita" }
            val others = allAudiobooks.filter { it.id != "ab_bhagavad_gita" }
            listOfNotNull(gita) + others
        } else {
            val q = searchQuery.trim().lowercase()
            allAudiobooks.filter { book ->
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                book.narrator.lowercase().contains(q) ||
                book.category.lowercase().contains(q) ||
                book.categories.any { it.lowercase().contains(q) } ||
                book.description.lowercase().contains(q) ||
                book.chapters.any { it.title.lowercase().contains(q) || it.summary?.lowercase()?.contains(q) == true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZyneraBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppleMaterials.glassFill)
                        .border(1.dp, AppleMaterials.subtleBorder, CircleShape)
                        .appleBounce { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Search Audiobooks",
                        style = AppleType.title3Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (searchQuery.isBlank()) "Explore masterworks & complete recordings" else "${filteredList.size} audiobooks found",
                        style = AppleType.caption2,
                        color = ZyneraPrimaryBright
                    )
                }
            }

            // Search input field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ZyneraSurface)
                    .border(1.dp, ZyneraBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search by title, author, narrator (e.g. Gita)...",
                                style = AppleType.subheadline,
                                color = ZyneraTextSubtle
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Quick suggestion search pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPills.forEach { pill ->
                    val isSelected = searchQuery.equals(pill, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ZyneraPrimary else AppleMaterials.glassFill)
                            .border(
                                1.dp,
                                if (isSelected) ZyneraPrimaryBright else AppleMaterials.subtleBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .appleBounce {
                                searchQuery = if (isSelected) "" else pill
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (pill == "Bhagavad Gita") "📖 $pill" else pill,
                            style = AppleType.caption1Bold,
                            color = if (isSelected) Color.White else ZyneraTextSecondary
                        )
                    }
                }
            }

            // Results List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "Popular Audiobooks to Listen" else "Search Results",
                    style = AppleType.footnoteBold,
                    color = ZyneraTextSecondary
                )
                Text(
                    text = "${filteredList.size} available",
                    style = AppleType.caption2,
                    color = ZyneraTextSubtle
                )
            }

            // Results List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No audiobooks found for \"$searchQuery\"",
                            style = AppleType.headline,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching for 'Bhagavad Gita', 'Premchand', 'Godan', or 'Philosophy'",
                            style = AppleType.footnote,
                            color = ZyneraTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { book ->
                        AudiobookCatalogRowItem(
                            audiobook = book,
                            onClick = { onOpenAudiobookDetail(book.id) },
                            onPlay = { onPlayAudiobook(book) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudiobookCatalogRowItem(
    audiobook: AudiobookDto,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZyneraSurface)
            .border(0.8.dp, ZyneraBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
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

                if (audiobook.id == "ab_bhagavad_gita") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GITA",
                            style = AppleType.caption2Bold,
                            color = Color.Black,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audiobook.title,
                    style = AppleType.subheadlineBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "By ${audiobook.author}",
                    style = AppleType.caption1,
                    color = ZyneraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Narrated by ${audiobook.narrator}",
                    style = AppleType.caption2,
                    color = ZyneraPrimaryBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ ${audiobook.rating}",
                        style = AppleType.caption2Bold,
                        color = Color(0xFFFBBF24)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${audiobook.formattedDuration} • ${audiobook.chapterCount} Ch",
                        style = AppleType.caption2,
                        color = ZyneraTextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ZyneraPrimary)
                    .appleBounce { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Listening",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
