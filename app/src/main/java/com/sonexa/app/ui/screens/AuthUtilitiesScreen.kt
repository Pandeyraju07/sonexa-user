package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*

@Composable
fun AuthUtilitiesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentEmail = remember { com.sonexa.app.data.local.SessionManager.getInstance(context).userEmail ?: "your email" }
    var showPasswordModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }

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
                    Text(text = "Security & Account Utilities", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .clickable { showPasswordModal = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Change Account Password", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .clickable { Toast.makeText(context, "Verification email sent to $currentEmail", Toast.LENGTH_SHORT).show() }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Verify Email Address ($currentEmail)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .clickable { Toast.makeText(context, "OTP sent to +91 9876543210", Toast.LENGTH_SHORT).show() }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Verify Phone Number (+91 9876543210)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .clickable { Toast.makeText(context, "Logged out 2 remote sessions", Toast.LENGTH_SHORT).show() }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Manage Active Sessions (2 Devices)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDeleteModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x30DC2626)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Account Permanently", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showPasswordModal) {
            AlertDialog(
                onDismissRequest = { showPasswordModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Change Password", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Current Password") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("New Password") })
                    }
                },
                confirmButton = {
                    SonexaGradientButton(text = "Update", onClick = { showPasswordModal = false; Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show() })
                }
            )
        }

        if (showDeleteModal) {
            AlertDialog(
                onDismissRequest = { showDeleteModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Delete Account?", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
                text = { Text(text = "This action is permanent and will delete all playlists, downloads, and AI history.", color = SonexaTextWhite) },
                confirmButton = {
                    Button(onClick = { showDeleteModal = false; Toast.makeText(context, "Account deletion initiated", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                        Text(text = "Confirm Delete")
                    }
                }
            )
        }
    }
}
