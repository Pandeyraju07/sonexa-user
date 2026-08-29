package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.sonexa.app.ui.components.*
import com.sonexa.app.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.viewmodel.AuthUiState
import com.sonexa.app.ui.viewmodel.AuthViewModel

@Composable
fun CreateAccountScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    onSocialSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("register") }

    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                val toast = if (state.emailDelivered) {
                    state.message
                } else {
                    state.message + (state.otp?.let { " OTP: $it" }.orEmpty())
                }
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
                if (pendingAction == "social") {
                    onSocialSuccess()
                } else {
                    onSignUpSuccess(email.trim().lowercase())
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

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
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SonexaInputBg)
                        .border(1.dp, SonexaInputBorder, CircleShape)
                        .clickable { onNavigateToLogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SonexaTextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sign up for Sonexa",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "We'll send a verification OTP to your email",
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SonexaInputField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholderText = "Full name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(10.dp))

                SonexaInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholderText = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(10.dp))

                SonexaInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholderText = "Phone number (optional)",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(10.dp))

                SonexaInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholderText = "Create password (min 6 chars)",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                SonexaCheckboxRow(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                SonexaGradientButton(
                    text = if (authState is AuthUiState.Loading) "Creating Account..." else "Create Account",
                    onClick = {
                        when {
                            fullName.isBlank() || email.isBlank() || password.isBlank() ->
                                Toast.makeText(context, "Please complete required fields", Toast.LENGTH_SHORT).show()
                            !email.contains("@") ->
                                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            password.length < 6 ->
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            !agreedToTerms ->
                                Toast.makeText(context, "Please accept Terms of Service", Toast.LENGTH_SHORT).show()
                            else -> {
                                pendingAction = "register"
                                authViewModel.register(
                                    email = email.trim(),
                                    name = fullName.trim(),
                                    pass = password,
                                    phone = phone.ifBlank { null }
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))
                SonexaOrDivider()
                Spacer(modifier = Modifier.height(12.dp))

                SocialContinueButtons(
                    enabled = authState !is AuthUiState.Loading,
                    onGoogleSuccess = { profile ->
                        pendingAction = "social"
                        authViewModel.googleSignIn(
                            idToken = profile.idToken,
                            email = profile.email,
                            name = profile.name ?: fullName.ifBlank { null },
                            profilePicUrl = profile.photoUrl
                        )
                    },
                    onAppleSuccess = { profile ->
                        pendingAction = "social"
                        authViewModel.appleSignIn(
                            identityToken = profile.idToken,
                            email = profile.email,
                            name = profile.name ?: fullName.ifBlank { null }
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ", fontSize = 13.sp, color = SonexaTextMuted)
                Text(
                    text = "Log in",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaPurpleLight,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
