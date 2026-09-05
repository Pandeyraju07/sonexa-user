package com.sonexa.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.repository.AuthRepository
import com.sonexa.app.ui.theme.*
import kotlinx.coroutines.launch

enum class AccountDialogTab {
    ACCOUNTS_LIST,
    SIGN_IN,
    CREATE_ACCOUNT
}

@Composable
fun AccountSwitcherDialog(
    onDismiss: () -> Unit,
    onAccountSwitched: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val currentUserName by sessionManager.userNameFlow.collectAsState()
    val currentUserAvatar by sessionManager.userAvatarFlow.collectAsState()
    val authRepo = remember { AuthRepository.create(context) }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(AccountDialogTab.ACCOUNTS_LIST) }
    var savedAccounts by remember { mutableStateOf(sessionManager.getSavedAccounts()) }

    // Sign in form state
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var showSignInPassword by remember { mutableStateOf(false) }

    // Register form state
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF22173B),
                            Color(0xFF140F22),
                            Color(0xFF0D0A17)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.5f),
                            Color(0xFF4C1D95).copy(alpha = 0.2f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (activeTab != AccountDialogTab.ACCOUNTS_LIST) {
                            IconButton(
                                onClick = {
                                    activeTab = AccountDialogTab.ACCOUNTS_LIST
                                    errorMessage = null
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SonexaTextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SonexaPurplePrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = SonexaPurpleLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }

                        Text(
                            text = when (activeTab) {
                                AccountDialogTab.ACCOUNTS_LIST -> "Switch Account"
                                AccountDialogTab.SIGN_IN -> "Sign In to Account"
                                AccountDialogTab.CREATE_ACCOUNT -> "Create Zynera Account"
                            },
                            color = SonexaTextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SonexaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message banner
                errorMessage?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.3f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = err,
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                when (activeTab) {
                    AccountDialogTab.ACCOUNTS_LIST -> {
                        // 1. Current Active Account Card
                        Text(
                            text = "ACTIVE ACCOUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonexaTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val currentName = currentUserName.ifBlank { sessionManager.userName ?: "Zynera Listener" }
                        val currentEmail = sessionManager.userEmail ?: "listener@zynera.app"
                        val currentAvatar = currentUserAvatar.ifBlank { sessionManager.profilePicUrl ?: "" }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF261D3D))
                                .border(1.dp, SonexaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentAvatar.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(currentAvatar)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentName.firstOrNull()?.uppercase() ?: "U",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentName,
                                        color = SonexaTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = currentEmail,
                                        color = SonexaTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SpotifyGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SpotifyGreen)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            text = "ACTIVE",
                                            color = SpotifyGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Other Saved Accounts List
                        val otherAccounts = savedAccounts.filter {
                            it.email != sessionManager.userEmail && it.userId != sessionManager.userId
                        }

                        if (otherAccounts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "OTHER SAVED ACCOUNTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonexaTextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                otherAccounts.forEach { acc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(SonexaInputBg)
                                            .border(1.dp, SonexaInputBorder, RoundedCornerShape(14.dp))
                                            .clickable {
                                                sessionManager.switchAccount(acc.userId)
                                                savedAccounts = sessionManager.getSavedAccounts()
                                                Toast.makeText(context, "Switched to ${acc.name}", Toast.LENGTH_SHORT).show()
                                                onAccountSwitched()
                                                onDismiss()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!acc.profilePicUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(acc.profilePicUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Profile Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF3B2D54)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = acc.name.firstOrNull()?.uppercase() ?: "U",
                                                    color = SonexaPurpleLight,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = acc.name,
                                                color = SonexaTextWhite,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = acc.email,
                                                color = SonexaTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                sessionManager.removeAccount(acc.userId)
                                                savedAccounts = sessionManager.getSavedAccounts()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Account",
                                                tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Actions: Sign In & Create Account
                        Button(
                            onClick = {
                                errorMessage = null
                                activeTab = AccountDialogTab.SIGN_IN
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SonexaPurplePrimary)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign In with Another Account", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                errorMessage = null
                                activeTab = AccountDialogTab.CREATE_ACCOUNT
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SonexaPurpleLight.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SonexaPurpleLight)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create New Account", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    AccountDialogTab.SIGN_IN -> {
                        Text(
                            text = "Connect with your Zynera email & password to access saved playlists, liked songs and history.",
                            color = SonexaTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field
                        OutlinedTextField(
                            value = signInEmail,
                            onValueChange = { signInEmail = it },
                            label = { Text("Email address") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Mail, contentDescription = null, tint = SonexaTextMuted)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonexaPurpleLight,
                                unfocusedBorderColor = SonexaInputBorder,
                                focusedContainerColor = SonexaInputBg,
                                unfocusedContainerColor = SonexaInputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = signInPassword,
                            onValueChange = { signInPassword = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = SonexaTextMuted)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showSignInPassword = !showSignInPassword }) {
                                    Icon(
                                        imageVector = if (showSignInPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = null,
                                        tint = SonexaTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (showSignInPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonexaPurpleLight,
                                unfocusedBorderColor = SonexaInputBorder,
                                focusedContainerColor = SonexaInputBg,
                                unfocusedContainerColor = SonexaInputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SonexaPurpleLight, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (signInEmail.isBlank() || signInPassword.isBlank()) {
                                        errorMessage = "Please enter both email and password."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = authRepo.login(signInEmail, signInPassword)
                                        isLoading = false
                                        result.fold(
                                            onSuccess = { resp ->
                                                Toast.makeText(
                                                    context,
                                                    "Signed in as ${resp.resolvedUser?.name ?: "User"}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onAccountSwitched()
                                                onDismiss()
                                            },
                                            onFailure = { err ->
                                                errorMessage = err.message ?: "Invalid email or password"
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SonexaPurplePrimary)
                            ) {
                                Text("Sign In & Switch", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    AccountDialogTab.CREATE_ACCOUNT -> {
                        Text(
                            text = "Create a new profile with personalized AI recommendations and lossless audio streams.",
                            color = SonexaTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name Field
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full name") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = SonexaTextMuted)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonexaPurpleLight,
                                unfocusedBorderColor = SonexaInputBorder,
                                focusedContainerColor = SonexaInputBg,
                                unfocusedContainerColor = SonexaInputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Field
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email address") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Mail, contentDescription = null, tint = SonexaTextMuted)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonexaPurpleLight,
                                unfocusedBorderColor = SonexaInputBorder,
                                focusedContainerColor = SonexaInputBg,
                                unfocusedContainerColor = SonexaInputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = SonexaTextMuted)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = null,
                                        tint = SonexaTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonexaPurpleLight,
                                unfocusedBorderColor = SonexaInputBorder,
                                focusedContainerColor = SonexaInputBg,
                                unfocusedContainerColor = SonexaInputBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SonexaPurpleLight, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                        errorMessage = "Please fill in all fields."
                                        return@Button
                                    }
                                    if (regPassword.length < 6) {
                                        errorMessage = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val regResult = authRepo.register(regEmail, regName, regPassword)
                                        if (regResult.isSuccess) {
                                            // Auto login upon successful registration
                                            val loginResult = authRepo.login(regEmail, regPassword)
                                            isLoading = false
                                            loginResult.fold(
                                                onSuccess = { resp ->
                                                    Toast.makeText(
                                                        context,
                                                        "Account created! Welcome, ${resp.resolvedUser?.name ?: regName}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    onAccountSwitched()
                                                    onDismiss()
                                                },
                                                onFailure = {
                                                    Toast.makeText(
                                                        context,
                                                        "Account created. Please sign in.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    activeTab = AccountDialogTab.SIGN_IN
                                                    signInEmail = regEmail
                                                }
                                            )
                                        } else {
                                            isLoading = false
                                            errorMessage = regResult.exceptionOrNull()?.message ?: "Registration failed."
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SonexaPurplePrimary)
                            ) {
                                Text("Create Account & Join", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
