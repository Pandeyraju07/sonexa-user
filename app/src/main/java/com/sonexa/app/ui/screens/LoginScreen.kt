package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.components.SonexaHeaderLogo
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*
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
    val selectionColors = TextSelectionColors(
        handleColor = SonexaPurpleLight,
        backgroundColor = SonexaPurpleLight.copy(alpha = 0.35f)
    )

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

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 24.dp)
            ) {
                SonexaHeaderLogo()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Log in to Zynera",
                    style = AppleType.largeTitle,
                    color = SonexaTextWhite,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome back to your music world.",
                    style = AppleType.subheadline,
                    color = SonexaTextMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                AppleFieldLabel("Email address")
                SonexaInputField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    placeholderText = "name@email.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    textStyle = AppleType.body.copy(color = SonexaTextWhite)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppleFieldLabel("Password")
                SonexaInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholderText = "Required",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    textStyle = AppleType.body.copy(color = SonexaTextWhite)
                )

                Text(
                    text = "Forgot password?",
                    style = AppleType.subheadline.copy(fontWeight = FontWeight.SemiBold),
                    color = SonexaPurpleLight,
                    modifier = Modifier
                        .align(Alignment.End)
                        .defaultMinSize(minHeight = 44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigateToForgotPassword() }
                        .wrapContentHeight(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.height(8.dp))

                SonexaGradientButton(
                    text = if (authState is AuthUiState.Loading) "Logging in…" else "Log In",
                    labelStyle = AppleType.headline.copy(color = Color.White),
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = AppleType.footnote,
                        color = SonexaTextMuted
                    )
                    Text(
                        text = "Sign up for free",
                        style = AppleType.footnote.copy(fontWeight = FontWeight.SemiBold),
                        color = SonexaPurpleLight,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .clickable { onNavigateToCreateAccount() }
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleFieldLabel(text: String) {
    Text(
        text = text,
        style = AppleType.footnote.copy(fontWeight = FontWeight.SemiBold),
        color = SonexaTextWhite,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
}
