package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.OtpResendRow
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.SonexaGradientBrush
import com.sonexa.app.ui.theme.SonexaInputBg
import com.sonexa.app.ui.theme.SonexaInputBorder
import com.sonexa.app.ui.theme.SonexaMagenta
import com.sonexa.app.ui.theme.SonexaPurpleLight
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextSubtle
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.AuthUiState
import com.sonexa.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

private const val OTP_COOLDOWN_SECONDS = 60
private const val OTP_LENGTH = 6

@Composable
fun OtpVerificationScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onOtpVerified: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    var resendSecondsLeft by remember { mutableIntStateOf(OTP_COOLDOWN_SECONDS) }
    val targetEmail = email.ifBlank { authViewModel.pendingOtpEmail.value }
    val isLoading = authState is AuthUiState.Loading
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        authViewModel.resetState()
        delay(180)
        runCatching { focusRequester.requestFocus() }
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
                if (state.message.contains("OTP verified", ignoreCase = true) ||
                    state.user != null
                ) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    authViewModel.resetState()
                    onOtpVerified()
                } else if (state.message.contains("OTP", ignoreCase = true) ||
                    state.message.contains("sent", ignoreCase = true) ||
                    state.message.contains("generated", ignoreCase = true)
                ) {
                    val toast = buildString {
                        append(state.message)
                        if (!state.emailDelivered) {
                            append(" Check Spam.")
                        } else {
                            append(" Check inbox & Spam (valid 1 min).")
                        }
                    }
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                    authViewModel.resetState()
                    resendSecondsLeft = OTP_COOLDOWN_SECONDS
                    otpCode = ""
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF140A2E),
                        Color(0xFF0A0718),
                        Color(0xFF05030C)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x559825DD),
                            Color(0x226B3CE9),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x33120C24))
                        .border(1.dp, Color(0x55B062FF), CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SonexaTextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "ZYNERA",
                    color = SonexaPurpleLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF5935E5),
                                Color(0xFF9825DD),
                                SonexaMagenta
                            )
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF0D081C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = SonexaPurpleLight,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Verify your email",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SonexaTextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to",
                fontSize = 14.sp,
                color = SonexaTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x33120C24))
                    .border(1.dp, Color(0x44B062FF), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = targetEmail.ifBlank { "your email" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SonexaPurpleLight
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Visible OTP boxes (fixed height — previous layout collapsed to 0)
            OtpDigitBoxes(
                otpCode = otpCode,
                onOtpChange = { otpCode = it.take(OTP_LENGTH).filter(Char::isDigit) },
                focusRequester = focusRequester,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OtpResendRow(
                secondsRemaining = resendSecondsLeft,
                onResend = {
                    when {
                        isLoading -> Unit
                        targetEmail.isBlank() ->
                            Toast.makeText(context, "Missing email for OTP", Toast.LENGTH_SHORT).show()
                        else -> authViewModel.sendOtp(targetEmail, "REGISTER")
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            SonexaGradientButton(
                text = if (isLoading) "Verifying..." else "Verify and Login",
                onClick = {
                    when {
                        isLoading -> Unit
                        targetEmail.isBlank() ->
                            Toast.makeText(context, "Missing email for OTP", Toast.LENGTH_SHORT).show()
                        otpCode.length < OTP_LENGTH ->
                            Toast.makeText(context, "Enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
                        else -> authViewModel.verifyOtp(targetEmail, otpCode, "REGISTER")
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Code expires in 1 minute · Check Spam if missing",
                fontSize = 11.sp,
                color = SonexaTextSubtle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OtpDigitBoxes(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    focusRequester: FocusRequester,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = enabled) { focusRequester.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(OTP_LENGTH) { index ->
                val char = otpCode.getOrNull(index)?.toString().orEmpty()
                val active = otpCode.length == index || (otpCode.length == OTP_LENGTH && index == OTP_LENGTH - 1)
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) Color(0xFF1A1035) else SonexaInputBg)
                        .border(
                            width = 1.5.dp,
                            brush = if (active || char.isNotEmpty()) {
                                SonexaGradientBrush
                            } else {
                                Brush.linearGradient(listOf(SonexaInputBorder, SonexaInputBorder))
                            },
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.ifEmpty { "" },
                        color = SonexaTextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        BasicTextField(
            value = otpCode,
            onValueChange = { value ->
                if (value.length <= OTP_LENGTH && value.all(Char::isDigit)) {
                    onOtpChange(value)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = 1.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}
