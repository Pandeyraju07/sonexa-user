package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.*
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

    // Keep password / OTP fields above the keyboard while typing
    LaunchedEffect(newPassword, otpCode, isCodeSent) {
        if (isCodeSent) {
            scrollState.animateScrollTo(scrollState.maxValue)
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            SonexaHeaderLogo()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reset Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SonexaTextWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (!isCodeSent)
                    "Enter your Gmail to receive a reset OTP"
                else
                    "Enter the OTP sent to $emailOrPhone (expires in 1 min)",
                fontSize = 13.sp,
                color = SonexaTextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isCodeSent) {
                SonexaInputField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    placeholderText = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(20.dp))

                SonexaGradientButton(
                    text = if (isLoading) "Sending..." else "Send Reset Code",
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
                SonexaInputField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                    placeholderText = "4–6 digit reset OTP",
                    leadingIcon = Icons.Default.Pin,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                SonexaInputField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholderText = "New password (min 6 chars)",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OtpResendRow(
                    secondsRemaining = resendSecondsLeft,
                    onResend = {
                        if (!isLoading && emailOrPhone.isNotBlank()) {
                            authViewModel.forgotPassword(emailOrPhone.trim())
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                SonexaGradientButton(
                    text = if (isLoading) "Updating..." else "Update Password",
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

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Remember your password? ",
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
                Text(
                    text = "Log in",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaPurpleLight,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            // Extra space so last field stays above IME
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
