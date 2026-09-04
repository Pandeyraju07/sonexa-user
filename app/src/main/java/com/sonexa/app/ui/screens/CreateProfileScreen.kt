package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.viewmodel.ProfileSetupViewModel

@Composable
fun CreateProfileScreen(
    onProfileCreated: () -> Unit,
    profileViewModel: ProfileSetupViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val setupState by profileViewModel.uiState.collectAsState()
    val sessionManager = remember { com.sonexa.app.data.local.SessionManager.getInstance(context) }
    val initialDisplayName = remember {
        sessionManager.userName?.takeIf { it.isNotBlank() }
            ?: sessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Zynera Listener"
    }
    val initialHandle = remember {
        initialDisplayName.lowercase().replace(" ", "_")
    }
    var displayName by remember { mutableStateOf(initialDisplayName) }
    var handle by remember { mutableStateOf(initialHandle) }
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }

    val avatarGradients = listOf(
        listOf(Color(0xFF6B3CE9), Color(0xFFE534B2)),
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
        listOf(Color(0xFF10B981), Color(0xFF059669)),
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)),
        listOf(Color(0xFF64748B), Color(0xFF0F172A))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0726),
                        Color(0xFF080512),
                        Color(0xFF05030A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Set up your avatar & display name for Zynera",
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
            }

            // Avatar Picker & Inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Selected Avatar Preview
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(avatarGradients[selectedAvatarIndex]))
                        .border(2.5.dp, SonexaPurpleLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose Avatar Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SonexaTextSubtle
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar Presets Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(avatarGradients) { index, colors ->
                        val isSelected = selectedAvatarIndex == index
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors))
                                .border(
                                    if (isSelected) 2.dp else 0.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedAvatarIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Name Input
                SonexaInputField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholderText = "Display Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Handle Input
                SonexaInputField(
                    value = handle,
                    onValueChange = { handle = it },
                    placeholderText = "Username (@handle)",
                    leadingIcon = Icons.Default.AlternateEmail
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SonexaGradientButton(
                    text = "Save & Continue",
                    onClick = {
                        if (displayName.isBlank()) {
                            Toast.makeText(context, "Please enter a display name", Toast.LENGTH_SHORT).show()
                        } else {
                            profileViewModel.createProfile(displayName, handle, onProfileCreated)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
