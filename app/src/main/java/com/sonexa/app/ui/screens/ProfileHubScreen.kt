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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*

data class BadgeItem(val title: String, val desc: String, val icon: String)
data class UserFriend(val name: String, val handle: String, val status: String, val song: String)

@Composable
fun ProfileHubScreen(
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: com.sonexa.app.ui.viewmodel.ProfileHubViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val profileState by viewModel.uiState.collectAsState()
    var showEditProfileModal by remember { mutableStateOf(false) }
    var userBio by remember { mutableStateOf("Music enthusiast • Hi-Fi Audio lover") }
    val profileName = when (val s = profileState) {
        is com.sonexa.app.ui.viewmodel.CatalogUiState.Ready -> s.data.name.ifBlank { "Sonexa Listener" }
        else -> "Sonexa Listener"
    }

    val badges = listOf(
        BadgeItem("AI DJ Pioneer", "Used AI DJ for 50+ hours", "🤖"),
        BadgeItem("Audiophile", "Streamed 100+ Hi-Fi Lossless tracks", "🎧"),
        BadgeItem("Night Owl", "Listened past midnight 20 times", "🌙"),
        BadgeItem("Social Star", "Collaborated on 5 shared playlists", "⭐")
    )

    val friends = listOf(
        UserFriend("Rahul Sharma", "@rahul_m", "Listening now", "Starboy • The Weeknd"),
        UserFriend("Priya Singh", "@priya_s", "Offline 2h ago", "Kesariya • Arijit Singh"),
        UserFriend("Ananya Kapoor", "@ananya_k", "Listening now", "Levitating • Dua Lipa")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Profile & Social", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                }
            }

            // Profile Header
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF6B3CE9), Color(0xFFE534B2)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = profileName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
                    Text(text = "@sonexa_user • Sonexa Member", fontSize = 13.sp, color = Color(0xFFC084FC))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = userBio, fontSize = 12.sp, color = SonexaTextMuted)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "248", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Text(text = "Followers", fontSize = 11.sp, color = SonexaTextSubtle)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "192", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Text(text = "Following", fontSize = 11.sp, color = SonexaTextSubtle)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "142h", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            Text(text = "Streamed", fontSize = 11.sp, color = SonexaTextSubtle)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(onClick = { showEditProfileModal = true }) {
                        Text(text = "Edit Profile", color = SonexaTextWhite)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = onOpenSettings, label = { Text("Settings") })
                        AssistChip(onClick = onOpenPremium, label = { Text("Premium") })
                        AssistChip(onClick = onOpenNotifications, label = { Text("Alerts") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onLogout) {
                        Text("Log out", color = Color(0xFFEF4444))
                    }
                }
            }

            // Listening Badges
            item {
                Text(text = "Earned Badges", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(badges) { b ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SonexaInputBg)
                                .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = b.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = b.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                Text(text = b.desc, fontSize = 10.sp, color = SonexaTextSubtle, maxLines = 2)
                            }
                        }
                    }
                }
            }

            // Social Activity Feed & Friends List
            item {
                Text(text = "Friends Activity & Live Listening", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
            }

            items(friends) { friend ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SonexaPurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = friend.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(text = friend.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                                Text(text = "${friend.status} • ${friend.song}", fontSize = 11.sp, color = SonexaTextMuted)
                            }
                        }

                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileModal) {
            var editName by remember { mutableStateOf(profileName) }
            AlertDialog(
                onDismissRequest = { showEditProfileModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Edit Profile", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display name", color = SonexaTextSubtle) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = userBio,
                            onValueChange = { userBio = it },
                            label = { Text("Bio", color = SonexaTextSubtle) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateProfile(editName.trim(), userBio.trim()) {
                            showEditProfileModal = false
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Save")
                    }
                }
            )
        }
    }
}
