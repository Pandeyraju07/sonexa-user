package com.sonexa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.data.model.NotificationDto
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.NotificationViewModel

data class AppNotification(
    val title: String,
    val body: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

private val fallbackNotifications = listOf(
    NotificationDto("1", "New Single Alert", "Arijit Singh just dropped 'Satranga' — stream it now!", "music_note", "#E534B2", "10m ago", false, "music"),
    NotificationDto("2", "Zynera AI Suggestion", "Your custom mood mix 'Monday Blues' is ready to play.", "auto_awesome", "#9825DD", "3h ago", false, "ai"),
    NotificationDto("3", "Friend Activity", "Rahul Sharma started listening to Lo-Fi Beats playlist.", "group", "#0EA5E9", "5h ago", true, "social"),
    NotificationDto("4", "New Release", "Your favourite album 'Rockstar' turns 15 — relive the classics!", "music_note", "#F59E0B", "Yesterday", true, "music"),
    NotificationDto("5", "AI DJ Insight", "You've listened to 142h this month — explore your Music DNA!", "auto_awesome", "#8B5CF6", "2 days ago", true, "ai"),
    NotificationDto("6", "Artist Alert", "AP Dhillon just announced a live concert in Mumbai!", "notifications_active", "#10B981", "3 days ago", true, "social"),
)

private data class NotifCategory(val id: String, val label: String, val icon: ImageVector)
private val notifCategories = listOf(
    NotifCategory("all", "All", Icons.Default.NotificationsActive),
    NotifCategory("music", "Music", Icons.Default.MusicNote),
    NotifCategory("ai", "AI & DJ", Icons.Default.AutoAwesome),
    NotifCategory("social", "Social", Icons.Default.Group),
)

@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredNotifs by viewModel.filteredNotifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Resolve notification list
    val rawList: List<NotificationDto> = when (val s = uiState) {
        is CatalogUiState.Ready -> if (s.data.isEmpty()) fallbackNotifications else
            filteredNotifs.ifEmpty { s.data }
        else -> fallbackNotifications
    }

    // Group by time period
    val grouped = remember(rawList) {
        rawList.groupBy { notif ->
            when {
                notif.timeAgo.contains("m ago") || notif.timeAgo.contains("h ago") -> "Today"
                notif.timeAgo.equals("yesterday", ignoreCase = true) -> "Yesterday"
                else -> "Earlier"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF12082A), Color(0xFF0C0520))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SonexaInputBg)
                            .border(1.dp, SonexaInputBorder, CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SonexaTextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Updates",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SonexaTextWhite
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread notification${if (unreadCount > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = SonexaPurpleLight
                            )
                        }
                    }

                    // Mark all read button
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SonexaPurplePrimary.copy(alpha = 0.20f))
                                .border(1.dp, SonexaPurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.markAllRead() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Mark all read",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SonexaPurpleLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Category Tabs ────────────────────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(notifCategories) { _, cat ->
                        val isSelected = selectedCategory == cat.id
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) SonexaPurplePrimary else SonexaInputBg,
                            animationSpec = tween(250),
                            label = "tabBg"
                        )
                        val catUnread = if (cat.id == "all") unreadCount else
                            rawList.count { it.category == cat.id && !it.read }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .border(
                                    1.dp,
                                    if (isSelected) SonexaPurplePrimary else SonexaInputBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.selectCategory(cat.id) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SonexaTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = cat.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else SonexaTextMuted
                                )
                                if (catUnread > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color(0xFFE534B2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = catUnread.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Notification List ────────────────────────────────────────────────
        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = SonexaPurpleLight,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            is CatalogUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = SonexaTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = SonexaTextMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.load() }) {
                            Text("Retry", color = SonexaPurpleLight)
                        }
                    }
                }
            }
            else -> {
                if (rawList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No notifications yet",
                                color = SonexaTextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "We'll alert you when something new drops",
                                color = SonexaTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        grouped.forEach { (period, notifList) ->
                            item(key = "header_$period") {
                                Text(
                                    text = period,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SonexaTextSubtle,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                )
                            }
                            itemsIndexed(notifList, key = { _, n -> n.id + n.timeAgo }) { index, notif ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(300 + index * 60)) +
                                            slideInVertically(
                                                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                                                initialOffsetY = { 40 }
                                            )
                                ) {
                                    PremiumNotificationCard(
                                        notif = notif,
                                        onClick = { if (!notif.read) viewModel.markRead(notif.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumNotificationCard(
    notif: NotificationDto,
    onClick: () -> Unit
) {
    val isUnread = !notif.read
    val accentColor = notif.colorHex.toComposeColor(Color(0xFFE534B2))
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isUnread)
                    Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color(0xFF1A0D2E).copy(alpha = 0.95f)
                        )
                    )
                else
                    Brush.horizontalGradient(
                        listOf(SonexaInputBg, SonexaInputBg)
                    )
            )
            .border(
                width = if (isUnread) 1.5.dp else 1.dp,
                color = if (isUnread) accentColor.copy(alpha = 0.35f) else SonexaInputBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Gradient Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.30f),
                                accentColor.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notif.iconKey) {
                        "auto_awesome" -> Icons.Default.AutoAwesome
                        "group" -> Icons.Default.Group
                        "notifications_active" -> Icons.Default.NotificationsActive
                        else -> Icons.Default.MusicNote
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notif.title,
                        fontSize = 14.sp,
                        fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = SonexaTextWhite,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = notif.timeAgo,
                            fontSize = 10.sp,
                            color = SonexaTextSubtle
                        )
                        if (isUnread) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notif.message,
                    fontSize = 12.sp,
                    color = SonexaTextMuted,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Category chip
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = notif.category.replaceFirstChar { it.uppercaseChar() },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
