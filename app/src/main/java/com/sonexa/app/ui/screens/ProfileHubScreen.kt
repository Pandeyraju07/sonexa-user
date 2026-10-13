package com.sonexa.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.ProfilePhotoManager
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.model.UserProfileDto
import com.sonexa.app.ui.components.LogoutConfirmationDialog
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.ProfileHubViewModel
import kotlinx.coroutines.launch

data class MilestoneBadge(
    val title: String,
    val subtitle: String,
    val icon: String,
    val progress: Float,
    val progressText: String,
    val accent: Color,
    val isLocked: Boolean = false
)

data class LiveFriendItem(
    val name: String,
    val handle: String,
    val initial: String,
    val avatarColor: Color,
    val songTitle: String,
    val artistName: String,
    val isLive: Boolean,
    val timeAgo: String
)

@Composable
fun ProfileHubScreen(
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenMusicDna: () -> Unit = {},
    onOpenMusicJourney: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileHubViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val profileState by viewModel.uiState.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val currentUserName by sessionManager.userNameFlow.collectAsState()
    val currentUserAvatar by sessionManager.userAvatarFlow.collectAsState()
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var selectedSocialFilter by remember { mutableStateOf("All") }

    val profile = (profileState as? CatalogUiState.Ready)?.data
    val profileName = currentUserName.takeIf { it.isNotBlank() }
        ?: profile?.name?.takeIf { it.isNotBlank() }
        ?: "Pandeyraju071"
    val profilePhotoUrl = currentUserAvatar.takeIf { it.isNotBlank() }
        ?: profile?.profilePicUrl?.takeIf { it.isNotBlank() }
        ?: ""
    val profileEmail = profile?.email.orEmpty()
    val handle = remember(profileEmail, profileName, profile?.handle) {
        profile?.handle?.takeIf { it.isNotBlank() } ?: run {
            val seed = profileEmail.substringBefore("@").ifBlank {
                profileName.lowercase().replace(" ", "")
            }
            "@$seed"
        }
    }
    val bio = profile?.bio?.takeIf { it.isNotBlank() }
        ?: "Music enthusiast • Hi-Fi Audio lover • Exploring AI vibes"
    val initial = profileName.firstOrNull()?.uppercase() ?: "P"
    val isLoading = profileState is CatalogUiState.Loading

    val badges = remember {
        listOf(
            MilestoneBadge("AI DJ Pioneer", "50+ AI DJ sessions", "🤖", 1f, "Completed", Color(0xFF9333EA)),
            MilestoneBadge("Audiophile Pro", "Stream in 320K Lossless", "🎧", 0.85f, "85/100 tracks", Color(0xFF06B6D4)),
            MilestoneBadge("Playlist Creator", "Create 10 playlists", "🎼", 0.4f, "4/10", Color(0xFF6B7280), isLocked = true),
            MilestoneBadge("Live Streamer", "Join 5 live sessions", "🔥", 0.2f, "1/5", Color(0xFFF97316)),
            MilestoneBadge("Taste Curator", "Explored all mood spectrums", "✨", 0.6f, "3/5", Color(0xFFEC4899))
        )
    }

    val friendsActivity = remember {
        listOf(
            LiveFriendItem("Rahul Sharma", "@rahul_m", "R", Color(0xFF3B82F6), "Starboy", "The Weeknd", true, "Listening now"),
            LiveFriendItem("Priya Singh", "@priya_s", "P", Color(0xFFEC4899), "Kesariya", "Arijit Singh", true, "Listening now"),
            LiveFriendItem("Ananya Kapoor", "@ananya_k", "A", Color(0xFF10B981), "Levitating", "Dua Lipa", false, "25m ago"),
            LiveFriendItem("Dev Patel", "@dev_beats", "D", Color(0xFFF59E0B), "Blinding Lights", "The Weeknd", false, "2h ago")
        )
    }

    val filteredFriends = remember(selectedSocialFilter, friendsActivity) {
        when (selectedSocialFilter) {
            "Listening", "Live Now" -> friendsActivity.filter { it.isLive }
            else -> friendsActivity
        }
    }

    fun shareProfile() {
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Follow $profileName on Zynera")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🎵 Join my listening circle on Zynera — $profileName ($handle)\nStream high-fidelity music & AI vibes together!"
                )
            }
            context.startActivity(Intent.createChooser(send, "Share Zynera profile"))
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyHandle() {
        clipboardManager.setText(AnnotatedString(handle))
        Toast.makeText(context, "Handle copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
    ) {
        // Atmospheric gradient glow in the backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B1578),
                            Color(0xFF210C42),
                            Color(0xFF110722),
                            Color.Transparent
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── TOP BAR ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ProfileCircleButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SonexaTextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Profile & Social",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SonexaTextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Your personal music hub",
                                fontSize = 12.sp,
                                color = SonexaTextSubtle
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileCircleButton(onClick = { shareProfile() }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Profile",
                                tint = SonexaTextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        ProfileCircleButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = SonexaTextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(99.dp)),
                        color = SonexaMagenta,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }

            // ── HERO CARD ──
            item {
                PremiumProfileHeroCard(
                    name = profileName,
                    handle = handle,
                    bio = bio,
                    initial = initial,
                    photoUrl = profilePhotoUrl,
                    isPremium = profile?.isPremium == true,
                    isVerified = profile?.isEmailVerified == true,
                    followers = profile?.followersCount ?: 248,
                    following = profile?.followingCount ?: 192,
                    onEdit = { showEditProfileModal = true },
                    onCopyHandle = { copyHandle() },
                    onShare = { shareProfile() }
                )
            }

            // ── STUDIO & INTELLIGENCE ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Studio & Intelligence",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See All >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.clickable { onOpenMusicDna() }
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tools to enhance your music journey",
                    fontSize = 12.sp,
                    color = Color(0xFF8E889E)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        StudioSquircleCard(
                            title = "Music DNA",
                            subtitle = "Discover your taste",
                            icon = Icons.Default.Psychology,
                            badgeBg = Color(0xFF581C87).copy(alpha = 0.5f),
                            badgeTint = Color(0xFFC084FC),
                            cardBg = Color(0xFF140E24),
                            borderColor = Color(0xFF281C44),
                            onClick = onOpenMusicDna
                        )
                    }
                    item {
                        StudioSquircleCard(
                            title = "Music Journey",
                            subtitle = "Your listening insights",
                            icon = Icons.Default.TrendingUp,
                            badgeBg = Color(0xFF0C4A6E).copy(alpha = 0.5f),
                            badgeTint = Color(0xFF38BDF8),
                            cardBg = Color(0xFF0C1929),
                            borderColor = Color(0xFF193252),
                            onClick = onOpenMusicJourney
                        )
                    }
                    item {
                        StudioSquircleCard(
                            title = if (profile?.isPremium == true) "Zynera Hi-Fi" else "Get Premium",
                            subtitle = if (profile?.isPremium == true) "Active lossless plan" else "Unlock 320K audio quality",
                            icon = Icons.Default.Star,
                            badgeBg = Color(0xFF831843).copy(alpha = 0.5f),
                            badgeTint = Color(0xFFF43F5E),
                            cardBg = Color(0xFF260D19),
                            borderColor = Color(0xFF4C1D33),
                            onClick = onOpenPremium
                        )
                    }
                    item {
                        StudioSquircleCard(
                            title = "Your Updates",
                            subtitle = "Alerts & releases",
                            icon = Icons.Default.Notifications,
                            badgeBg = Color(0xFF064E3B).copy(alpha = 0.5f),
                            badgeTint = Color(0xFF34D399),
                            cardBg = Color(0xFF0B2118),
                            borderColor = Color(0xFF174230),
                            onClick = onOpenNotifications
                        )
                    }
                }
            }

            // ── EARNED MILESTONES & BADGES ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Listening Milestones",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "2 of 5 unlocked >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Track your progress and earn badges",
                    fontSize = 12.sp,
                    color = Color(0xFF8E889E)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(badges) { badge ->
                        PremiumBadgeCard(badge)
                    }
                }
            }

            // ── SOCIAL CIRCLE & LIVE SESSIONS ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Social Circle & Live Sessions",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "See what your friends are listening to",
                            fontSize = 12.sp,
                            color = Color(0xFF8E889E)
                        )
                    }

                    // Filter row: All, Listening, Live Now
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF161026))
                            .border(1.dp, Color(0xFF282038), RoundedCornerShape(16.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("All", "Listening", "Live Now").forEach { filter ->
                            val isSelected = selectedSocialFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF7C3AED) else Color.Transparent)
                                    .clickable { selectedSocialFilter = filter }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF8E889E)
                                )
                            }
                        }
                    }
                }
            }

            items(filteredFriends) { friend ->
                LiveFriendActivityCard(friend)
            }

            // ── INVITE CARD ──
            item {
                ListeningCircleInviteCard(onInvite = { shareProfile() })
            }

            // ── LOGOUT BUTTON ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                        .clickable { showLogoutConfirm = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log out of Zynera",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // ── EDIT PROFILE MODAL ──
        if (showEditProfileModal) {
            EditProfileDialog(
                initialName = profileName,
                initialHandle = handle,
                initialBio = bio,
                initialPhotoUrl = profilePhotoUrl,
                busy = busy,
                sessionManager = sessionManager,
                onDismiss = { showEditProfileModal = false },
                onSave = { name, nextHandle, nextBio, nextPhotoUrl ->
                    viewModel.updateProfile(
                        name = name,
                        bio = nextBio,
                        handle = nextHandle,
                        profilePicUrl = nextPhotoUrl
                    ) {
                        showEditProfileModal = false
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // ── LOGOUT CONFIRMATION ──
        if (showLogoutConfirm) {
            LogoutConfirmationDialog(
                onConfirmLogout = {
                    showLogoutConfirm = false
                    onLogout()
                },
                onDismiss = { showLogoutConfirm = false }
            )
        }
    }
}

@Composable
private fun PremiumProfileHeroCard(
    name: String,
    handle: String,
    bio: String,
    initial: String,
    photoUrl: String,
    isPremium: Boolean,
    isVerified: Boolean,
    followers: Int,
    following: Int,
    onEdit: () -> Unit,
    onCopyHandle: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF22133D),
                        Color(0xFF150A26),
                        Color(0xFF0F071C)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.45f),
                        Color(0xFF6B21A8).copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row with Avatar on left and Name / Handle / Member / Bio on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Camera Edit Badge
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable(onClick = onEdit)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFA855F7),
                                        Color(0xFF6366F1),
                                        Color(0xFFEC4899),
                                        Color(0xFFA855F7)
                                    )
                                )
                            )
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF160E26)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Camera Edit Badge
                    Box(
                        modifier = Modifier
                            .offset(x = 2.dp, y = 2.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1730))
                            .border(1.5.dp, Color(0xFF382A54), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color(0xFFD1CCE0),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name, Handle, Member Badge, Bio
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onCopyHandle)
                        ) {
                            Text(
                                text = handle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA78BFA)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF8E889E),
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        // Member pill badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF3B1554))
                                .border(1.dp, Color(0xFF6B21A8).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPremium) "VIP" else "Member",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = bio,
                        fontSize = 11.5.sp,
                        color = Color(0xFFA19BAE),
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Stat boxes: Followers, Following, Quality Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Followers box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140E22))
                        .border(1.dp, Color(0xFF261A3C), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF581C87).copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = formatSocialCount(followers),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Followers",
                                fontSize = 10.sp,
                                color = Color(0xFF8E889E)
                            )
                        }
                    }
                }

                // Following box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140E22))
                        .border(1.dp, Color(0xFF261A3C), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = formatSocialCount(following),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Following",
                                fontSize = 10.sp,
                                color = Color(0xFF8E889E)
                            )
                        }
                    }
                }

                // Quality Score box
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140E22))
                        .border(1.dp, Color(0xFF261A3C), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF831843).copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPremium) "Lossless" else "320K",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Quality Score",
                                fontSize = 10.sp,
                                color = Color(0xFF8E889E)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: Edit Profile & Share Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Edit Profile Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                            )
                        )
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }

                // Share Profile Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF191328))
                        .border(1.dp, Color(0xFF352B4D), RoundedCornerShape(12.dp))
                        .clickable(onClick = onShare),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialHandle: String,
    initialBio: String,
    initialPhotoUrl: String,
    busy: Boolean,
    sessionManager: SessionManager,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var editName by remember { mutableStateOf(initialName) }
    var editHandle by remember { mutableStateOf(initialHandle.removePrefix("@")) }
    var editBio by remember { mutableStateOf(initialBio) }
    var editPhotoUrl by remember { mutableStateOf(initialPhotoUrl) }
    var isProcessingImage by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingImage = true
            coroutineScope.launch {
                val savedPath = ProfilePhotoManager.savePickedImage(context, uri, sessionManager.userId)
                isProcessingImage = false
                if (savedPath != null) {
                    editPhotoUrl = savedPath
                    Toast.makeText(context, "Photo ready to save!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not process photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val avatarPresets = listOf(
        "https://api.dicebear.com/7.x/initials/svg?seed=VIP&backgroundColor=6b3ce9,e534b2&textColor=ffffff",
        "https://api.dicebear.com/7.x/initials/svg?seed=Star&backgroundColor=06b6d4,3b82f6&textColor=ffffff",
        "https://api.dicebear.com/7.x/initials/svg?seed=Wave&backgroundColor=10b981,059669&textColor=ffffff",
        "https://api.dicebear.com/7.x/initials/svg?seed=Fire&backgroundColor=f59e0b,ef4444&textColor=ffffff",
        "https://api.dicebear.com/7.x/initials/svg?seed=Neon&backgroundColor=8b5cf6,ec4899&textColor=ffffff",
        "https://api.dicebear.com/7.x/bottts/svg?seed=CyberDJ&backgroundColor=1e1b4b",
        "https://api.dicebear.com/7.x/adventurer/svg?seed=Audiophile&backgroundColor=111827"
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SonexaTextWhite,
        unfocusedTextColor = SonexaTextWhite,
        focusedContainerColor = SonexaInputBg,
        unfocusedContainerColor = SonexaInputBg,
        focusedBorderColor = SonexaPurpleLight,
        unfocusedBorderColor = SonexaInputBorder,
        cursorColor = SonexaMagenta,
        focusedLabelColor = SonexaPurpleLight,
        unfocusedLabelColor = SonexaTextSubtle
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF231A38), Color(0xFF120C1E))
                    )
                )
                .border(1.dp, SonexaPurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Edit Profile", color = SonexaTextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // ── PHOTO UPLOAD SECTION ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF181126))
                    .border(1.dp, SonexaPurpleLight.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Avatar Circle with Camera Overlay
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .clickable(enabled = !isProcessingImage) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SonexaPurpleLight, SonexaMagenta)))
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF190C30)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = SonexaPurpleLight,
                                strokeWidth = 2.dp
                            )
                        } else if (editPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(editPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = editName.take(1).uppercase().ifBlank { "Z" },
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SonexaPurplePrimary)
                            .border(1.5.dp, Color(0xFF181126), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Upload",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Upload Actions
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profile Photo",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick from gallery or choose avatar",
                        color = SonexaTextMuted,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SonexaGradientBrush)
                                .clickable(enabled = !isProcessingImage) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Upload",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (editPhotoUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF7F1D1D).copy(alpha = 0.4f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { editPhotoUrl = "" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Remove",
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Avatar Presets Row
            Text(
                text = "OR CHOOSE ARTISTIC AVATAR",
                color = SonexaTextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(avatarPresets) { preset ->
                    val isSelected = editPhotoUrl == preset
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                brush = if (isSelected) Brush.linearGradient(listOf(SonexaPurpleLight, SonexaMagenta))
                                else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))),
                                shape = CircleShape
                            )
                            .clickable { editPhotoUrl = preset }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(preset)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar preset",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Display name") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = editHandle,
                onValueChange = { editHandle = it.replace("@", "").replace(" ", "_") },
                label = { Text("Username (@handle)") },
                prefix = { Text("@", color = SonexaPurpleLight) },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = editBio,
                onValueChange = { editBio = it },
                label = { Text("Bio") },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !busy && !isProcessingImage
                ) {
                    Text("Cancel", color = SonexaTextMuted)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SonexaGradientBrush)
                        .clickable(enabled = !busy && !isProcessingImage) {
                            val formattedHandle = if (editHandle.isNotBlank()) "@${editHandle.trim()}" else ""
                            onSave(editName.trim(), formattedHandle, editBio.trim(), editPhotoUrl.trim())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (busy || isProcessingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatSocialCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
    count >= 1_000 -> String.format("%.1fK", count / 1_000f)
    else -> count.toString()
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = SonexaTextSubtle,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StudioSquircleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeBg: Color,
    badgeTint: Color,
    cardBg: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(108.dp)
            .height(134.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFF8E889E),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumBadgeCard(badge: MilestoneBadge) {
    Box(
        modifier = Modifier
            .width(142.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF140E22))
            .border(
                1.dp,
                if (badge.progress >= 1f) badge.accent.copy(alpha = 0.5f) else Color(0xFF261A3C),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = badge.icon, fontSize = 22.sp)
                if (badge.progress >= 1f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF7C3AED))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UNLOCKED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                } else if (badge.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF8E889E),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column {
                Text(
                    text = badge.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = badge.subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF8E889E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = badge.accent,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.progressText,
                    fontSize = 9.5.sp,
                    color = if (badge.progress >= 1f) badge.accent else Color(0xFF8E889E),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LiveFriendActivityCard(friend: LiveFriendItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF140E22))
            .border(
                1.dp,
                if (friend.isLive) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFF261A3C),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
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
                // Friend Avatar with Online Dot
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(friend.avatarColor.copy(alpha = 0.22f))
                            .border(1.dp, friend.avatarColor.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = friend.initial,
                            color = friend.avatarColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    if (friend.isLive) {
                        Box(
                            modifier = Modifier
                                .offset(x = 1.dp, y = 1.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(1.5.dp, Color(0xFF140E22), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = friend.handle,
                            fontSize = 11.sp,
                            color = Color(0xFF8E889E)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${friend.songTitle} • ${friend.artistName}",
                            fontSize = 11.5.sp,
                            color = Color(0xFFA19BAE),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (friend.isLive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Live",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    Text(
                        text = friend.timeAgo,
                        fontSize = 10.5.sp,
                        color = Color(0xFF8E889E)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = Color(0xFF8E889E),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ListeningCircleInviteCard(onInvite: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF38144D), Color(0xFF180A26))
                )
            )
            .border(1.dp, Color(0xFF581C87).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .clickable(onClick = onInvite)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9333EA).copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = Color(0xFFE879F9),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Expand your Listening Circle",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Share vibe flows & discover tracks with friends",
                    color = Color(0xFFA19BAE),
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A153E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileCircleButton(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}
