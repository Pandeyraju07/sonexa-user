package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
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
import com.sonexa.app.ui.components.OtpResendRow
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.components.SonexaHeaderLogo
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.viewmodel.AuthUiState
import com.sonexa.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

private const val OTP_COOLDOWN_SECONDS = 60

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var emailOrPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var resendSecondsLeft by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val isLoading = authState is AuthUiState.Loading
    val fieldStyle = AppleType.body.copy(color = SonexaTextWhite)
    val selectionColors = TextSelectionColors(
        handleColor = SonexaPurpleLight,
        backgroundColor = SonexaPurpleLight.copy(alpha = 0.35f)
    )

    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(resendSecondsLeft) {
        if (resendSecondsLeft > 0) {
            delay(1_000L)
            resendSecondsLeft--
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                val toast = buildString {
                    append(state.message)
                    if (!state.emailDelivered) {
                        append(" Check Spam/Promotions.")
                        state.otp?.let { append(" OTP: $it") }
                    } else {
                        append(" Check inbox & Spam (valid 1 min).")
                    }
                }
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
                if (state.message.contains("sent", ignoreCase = true)) {
                    isCodeSent = true
                    resendSecondsLeft = OTP_COOLDOWN_SECONDS
                    otpCode = ""
                } else if (
                    isCodeSent &&
                    (state.message.contains("updated successfully", ignoreCase = true) ||
                        state.message.contains("log in with your new password", ignoreCase = true))
                ) {
                    onNavigateToLogin()
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(newPassword, otpCode, isCodeSent) {
        if (isCodeSent) {
            scrollState.animateScrollTo(scrollState.maxValue)
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SonexaHeaderLogo()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Reset Password",
                    style = AppleType.largeTitle,
                    color = SonexaTextWhite,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (!isCodeSent) {
                        "Enter your email address and we’ll send you a reset code."
                    } else {
                        "Enter the code sent to $emailOrPhone. It expires in 1 minute."
                    },
                    style = AppleType.subheadline,
                    color = SonexaTextMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isCodeSent) {
                    FieldLabel("Email address")
                    SonexaInputField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        placeholderText = "name@email.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        textStyle = fieldStyle
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SonexaGradientButton(
                        text = if (isLoading) "Sending…" else "Send Reset Code",
                        labelStyle = AppleType.headline.copy(color = Color.White),
                        onClick = {
                            when {
                                isLoading -> Unit
                                emailOrPhone.isBlank() ->
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                !emailOrPhone.contains("@") ->
                                    Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                else -> authViewModel.forgotPassword(emailOrPhone.trim())
                            }
                        }
                    )
                } else {
                    FieldLabel("Reset code")
                    SonexaInputField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                        placeholderText = "6-digit code",
                        leadingIcon = Icons.Default.Pin,
                        keyboardType = KeyboardType.Number,
                        textStyle = fieldStyle
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FieldLabel("New password")
                    SonexaInputField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholderText = "At least 6 characters",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        textStyle = fieldStyle
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OtpResendRow(
                        secondsRemaining = resendSecondsLeft,
                        onResend = {
                            if (!isLoading && emailOrPhone.isNotBlank()) {
                                authViewModel.forgotPassword(emailOrPhone.trim())
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SonexaGradientButton(
                        text = if (isLoading) "Updating…" else "Update Password",
                        labelStyle = AppleType.headline.copy(color = Color.White),
                        onClick = {
                            when {
                                isLoading -> Unit
                                otpCode.isBlank() || newPassword.isBlank() ->
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                newPassword.length < 6 ->
                                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                else -> authViewModel.resetPassword(emailOrPhone.trim(), otpCode.trim(), newPassword)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remember your password? ",
                        style = AppleType.footnote,
                        color = SonexaTextMuted
                    )
                    Text(
                        text = "Log in",
                        style = AppleType.footnote.copy(fontWeight = FontWeight.SemiBold),
                        color = SonexaPurpleLight,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .clickable { onNavigateToLogin() }
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = AppleType.footnote.copy(fontWeight = FontWeight.SemiBold),
        color = SonexaTextWhite,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
}
