package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun LoginScreen(
    onNavigateToCreateAccount: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToOtp: (String) -> Unit = {},
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
                onLoginSuccess()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                if (state.errorMessage.contains("verify your email", ignoreCase = true)) {
                    onNavigateToOtp(emailOrPhone.trim().lowercase())
                }
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                SonexaHeaderLogo()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Log in to Zynera",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome back to your music world",
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SonexaInputField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    placeholderText = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(12.dp))

                SonexaInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholderText = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SonexaPurpleLight,
                        modifier = Modifier.clickable { onNavigateToForgotPassword() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SonexaGradientButton(
                    text = if (authState is AuthUiState.Loading) "Logging in..." else "Log In",
                    onClick = {
                        when {
                            emailOrPhone.isBlank() || password.isBlank() ->
                                Toast.makeText(context, "Please enter your credentials", Toast.LENGTH_SHORT).show()
                            !emailOrPhone.contains("@") ->
                                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            else -> authViewModel.login(emailOrPhone.trim(), password)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
                SonexaOrDivider()
                Spacer(modifier = Modifier.height(16.dp))

                SocialContinueButtons(
                    enabled = authState !is AuthUiState.Loading,
                    onGoogleSuccess = { profile ->
                        authViewModel.googleSignIn(
                            idToken = profile.idToken,
                            email = profile.email,
                            name = profile.name,
                            profilePicUrl = profile.photoUrl
                        )
                    },
                    onAppleSuccess = { profile ->
                        authViewModel.appleSignIn(
                            identityToken = profile.idToken,
                            email = profile.email,
                            name = profile.name
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Don't have an account? ", fontSize = 13.sp, color = SonexaTextMuted)
                Text(
                    text = "Sign up for free",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaPurpleLight,
                    modifier = Modifier.clickable { onNavigateToCreateAccount() }
                )
            }
        }
    }
}
