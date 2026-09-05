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
    val accent: Color
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
        ?: "Zynera Listener"
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
    val initial = profileName.firstOrNull()?.uppercase() ?: "Z"
    val isLoading = profileState is CatalogUiState.Loading

    val badges = remember {
        listOf(
            MilestoneBadge("AI DJ Pioneer", "50h+ AI DJ sessions", "🤖", 1f, "Completed", Color(0xFF9825DD)),
            MilestoneBadge("Audiophile Pro", "Stream in 320k Lossless", "🎧", 0.85f, "85/100 tracks", Color(0xFF10B981)),
            MilestoneBadge("Night Explorer", "Late night listening flow", "🌙", 0.7f, "14/20 nights", Color(0xFF38BDF8)),
            MilestoneBadge("Taste Curator", "Created 5+ custom mixes", "✨", 0.6f, "3/5 playlists", Color(0xFFF59E0B)),
            MilestoneBadge("Vibe Master", "Explored all mood spectrums", "🔥", 1f, "Mastered", Color(0xFFEC4899))
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
            "Listening Now" -> friendsActivity.filter { it.isLive }
            "Recent" -> friendsActivity.filter { !it.isLive }
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
                .navigationBarsPadding()
                .padding(bottom = 135.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
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

            // ── MUSIC STUDIO & TASTE SHORTCUTS ──
            item {
                Text(
                    text = "STUDIO & INTELLIGENCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextMuted,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumStudioTile(
                        title = "Music DNA",
                        subtitle = "Acoustic taste map",
                        icon = Icons.Default.Psychology,
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                        onClick = onOpenMusicDna,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumStudioTile(
                        title = "Music Journey",
                        subtitle = "Your listening story",
                        icon = Icons.Default.Timeline,
                        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0284C7)),
                        onClick = onOpenMusicJourney,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumStudioTile(
                        title = if (profile?.isPremium == true) "Zynera Hi-Fi" else "Get Premium",
                        subtitle = if (profile?.isPremium == true) "Active lossless plan" else "Unlock 320k audio",
                        icon = Icons.Default.Star,
                        gradientColors = listOf(Color(0xFFE534B2), Color(0xFF9825DD)),
                        onClick = onOpenPremium,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumStudioTile(
                        title = "Your Updates",
                        subtitle = "Alerts & releases",
                        icon = Icons.Default.Notifications,
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                        onClick = onOpenNotifications,
                        modifier = Modifier.weight(1f)
                    )
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
                        text = "LISTENING MILESTONES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SonexaTextMuted,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "${badges.count { it.progress >= 1f }} of ${badges.size} unlocked",
                        fontSize = 11.sp,
                        color = SonexaPurpleLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(badges) { badge ->
                        PremiumBadgeCard(badge)
                    }
                }
            }

            // ── SOCIAL CIRCLE & LIVE FRIENDS ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOCIAL CIRCLE & LIVE SESSIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SonexaTextMuted,
                        letterSpacing = 1.4.sp
                    )

                    // Filter row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SonexaInputBg)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("All", "Listening Now").forEach { filter ->
                            val isSelected = selectedSocialFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) SonexaPurplePrimary else Color.Transparent)
                                    .clickable { selectedSocialFilter = filter }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else SonexaTextMuted
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
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF28154A),
                        Color(0xFF190C30),
                        Color(0xFF100720)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SonexaPurpleLight.copy(alpha = 0.5f),
                        SonexaMagenta.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar with neon gradient glow ring & tap to change photo
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable(onClick = onEdit)
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFB062FF), Color(0xFFE534B2), Color(0xFF38BDF8))
                            )
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(SonexaCardDark),
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
                            color = SonexaTextWhite,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Camera Edit Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(SonexaPurpleLight, SonexaMagenta))
                        )
                        .border(2.dp, SonexaCardDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Display Name & Verified Tick
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Handle pill (clickable to copy)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable(onClick = onCopyHandle)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = handle,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SonexaPurpleLight
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy",
                    tint = SonexaTextSubtle,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPremium) "• Hi-Fi" else "• Member",
                    fontSize = 11.sp,
                    color = SonexaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio
            Text(
                text = bio,
                fontSize = 12.5.sp,
                color = SonexaTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Stats row (Followers, Following, Streamed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    value = formatSocialCount(followers),
                    label = "Followers",
                    accent = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    value = formatSocialCount(following),
                    label = "Following",
                    accent = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    value = if (isPremium) "Hi-Fi" else "320k",
                    label = "Quality",
                    accent = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons: Edit Profile & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SonexaGradientBrush)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onShare),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = SonexaTextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Profile",
                            color = SonexaTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
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
private fun PremiumStudioTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = SonexaTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SonexaTextSubtle,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PremiumBadgeCard(badge: MilestoneBadge) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SonexaInputBg)
            .border(
                1.dp,
                if (badge.progress >= 1f) badge.accent.copy(alpha = 0.5f) else SonexaInputBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = badge.icon, fontSize = 24.sp)
                if (badge.progress >= 1f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badge.accent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UNLOCKED",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badge.accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SonexaTextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = badge.subtitle,
                fontSize = 10.5.sp,
                color = SonexaTextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { badge.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = badge.accent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.progressText,
                fontSize = 9.5.sp,
                color = if (badge.progress >= 1f) badge.accent else SonexaTextSubtle,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LiveFriendActivityCard(friend: LiveFriendItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SonexaInputBg)
            .border(
                1.dp,
                if (friend.isLive) Color(0xFF10B981).copy(alpha = 0.35f) else SonexaInputBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
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
                // Friend Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(friend.avatarColor.copy(alpha = 0.25f))
                        .border(1.dp, friend.avatarColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.initial,
                        color = friend.avatarColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonexaTextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = friend.handle,
                            fontSize = 11.sp,
                            color = SonexaTextSubtle
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (friend.isLive) Color(0xFF10B981) else SonexaTextSubtle,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${friend.songTitle} • ${friend.artistName}",
                            fontSize = 11.5.sp,
                            color = if (friend.isLive) Color.White else SonexaTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (friend.isLive) {
                // Animated Equalizer Wave indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Live",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
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
                    color = SonexaTextSubtle
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
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF28154A), Color(0xFF160C2A))
                )
            )
            .border(1.dp, SonexaPurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SonexaPurplePrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = SonexaPurpleLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expand your Listening Circle",
                        color = SonexaTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Share vibe flows & discover tracks with friends",
                        color = SonexaTextMuted,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "When your friends join Zynera, their live sessions and shared playlists will automatically appear in your feed.",
                color = SonexaTextSubtle,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onInvite),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = SonexaTextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Invite friends to Zynera",
                        color = SonexaTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
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
