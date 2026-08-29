package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.screens.toComposeColor

data class AppNotification(val title: String, val body: String, val time: String, val icon: ImageVector, val color: Color)

@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.sonexa.app.ui.viewmodel.NotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val notifications = when (val s = uiState) {
        is com.sonexa.app.ui.viewmodel.CatalogUiState.Ready -> s.data.map {
            AppNotification(
                it.title,
                it.message,
                it.timeAgo,
                Icons.Default.MusicNote,
                it.colorHex.toComposeColor(Color(0xFFE534B2))
            )
        }
        else -> listOf(
            AppNotification("New Single Alert", "Arijit Singh just released 'Satranga'. Stream it now!", "10m ago", Icons.Default.MusicNote, Color(0xFFE534B2)),
            AppNotification("Sonexa AI Suggestion", "Your custom mix is ready!", "3h ago", Icons.Default.AutoAwesome, Color(0xFF9825DD))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SonexaInputBg)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SonexaTextWhite, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(text = "Notifications & Activity", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SonexaInputBg)
                            .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                            .clickable { Toast.makeText(context, notif.title, Toast.LENGTH_SHORT).show() }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(notif.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = notif.icon, contentDescription = null, tint = notif.color, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = notif.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                    Text(text = notif.time, fontSize = 11.sp, color = SonexaTextSubtle)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = notif.body, fontSize = 12.sp, color = SonexaTextMuted, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
