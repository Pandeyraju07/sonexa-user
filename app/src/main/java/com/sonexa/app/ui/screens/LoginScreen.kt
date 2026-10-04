package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.OtpResendRow
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.components.SonexaHeaderLogo
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AuthUiState
import com.sonexa.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

enum class LoginStep {
    EMAIL,
    VERIFY
}

enum class VerifyMode {
    OTP,
    PASSWORD
}

private const val LOGIN_OTP_COOLDOWN_SECONDS = 60

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

    var step by remember { mutableStateOf(LoginStep.EMAIL) }
    var verifyMode by remember { mutableStateOf(VerifyMode.OTP) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var resendSecondsLeft by remember { mutableIntStateOf(LOGIN_OTP_COOLDOWN_SECONDS) }

    val focusRequester = remember { FocusRequester() }
    val isLoading = authState is AuthUiState.Loading

    val selectionColors = TextSelectionColors(
        handleColor = SonexaPurpleLight,
        backgroundColor = SonexaPurpleLight.copy(alpha = 0.35f)
    )

    BackHandler(enabled = step == LoginStep.VERIFY) {
        step = LoginStep.EMAIL
        otpCode = ""
    }

    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(resendSecondsLeft) {
        if (step == LoginStep.VERIFY && verifyMode == VerifyMode.OTP && resendSecondsLeft > 0) {
            delay(1_000L)
            resendSecondsLeft--
        }
    }

    LaunchedEffect(step, verifyMode) {
        if (step == LoginStep.VERIFY && verifyMode == VerifyMode.OTP) {
            delay(200)
            runCatching { focusRequester.requestFocus() }
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                if (state.message.contains("OTP verified", ignoreCase = true) ||
                    state.message.contains("Login successful", ignoreCase = true) ||
                    state.user != null
                ) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    authViewModel.resetState()
                    onLoginSuccess()
                } else if (state.message.contains("OTP", ignoreCase = true) ||
                    state.message.contains("sent", ignoreCase = true)
                ) {
                    val toast = if (state.emailDelivered) {
                        state.message
                    } else {
                        state.message + (state.otp?.let { " OTP: $it" }.orEmpty())
                    }
                    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
                    authViewModel.resetState()
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                if (state.errorMessage.contains("verify your email", ignoreCase = true)) {
                    step = LoginStep.VERIFY
                    verifyMode = VerifyMode.OTP
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
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState == LoginStep.VERIFY) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "LoginStepAnimation"
            ) { currentStep ->
                when (currentStep) {
                    LoginStep.EMAIL -> {
                        EmailStepContent(
                            email = email,
                            onEmailChange = { email = it },
                            isLoading = isLoading,
                            onContinue = {
                                val trimmed = email.trim()
                                when {
                                    trimmed.isBlank() ->
                                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                    !trimmed.contains("@") || !trimmed.contains(".") ->
                                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        authViewModel.sendOtp(trimmed.lowercase(), "LOGIN")
                                        resendSecondsLeft = LOGIN_OTP_COOLDOWN_SECONDS
                                        otpCode = ""
                                        verifyMode = VerifyMode.OTP
                                        step = LoginStep.VERIFY
                                    }
                                }
                            },
                            onNavigateToCreateAccount = onNavigateToCreateAccount
                        )
                    }
                    LoginStep.VERIFY -> {
                        VerifyStepContent(
                            email = email.trim().lowercase(),
                            verifyMode = verifyMode,
                            onSwitchMode = { newMode ->
                                verifyMode = newMode
                                authViewModel.resetState()
                            },
                            onBackToEmail = {
                                step = LoginStep.EMAIL
                                otpCode = ""
                            },
                            otpCode = otpCode,
                            onOtpChange = { newOtp ->
                                otpCode = newOtp
                                if (newOtp.length == OTP_LENGTH) {
                                    authViewModel.verifyOtp(email.trim().lowercase(), newOtp, "LOGIN")
                                }
                            },
                            password = password,
                            onPasswordChange = { password = it },
                            resendSecondsLeft = resendSecondsLeft,
                            onResendOtp = {
                                authViewModel.sendOtp(email.trim().lowercase(), "LOGIN")
                                resendSecondsLeft = LOGIN_OTP_COOLDOWN_SECONDS
                            },
                            focusRequester = focusRequester,
                            isLoading = isLoading,
                            onLoginWithPassword = {
                                if (password.isBlank()) {
                                    Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                                } else {
                                    authViewModel.login(email.trim().lowercase(), password)
                                }
                            },
                            onLoginWithOtp = {
                                if (otpCode.length < OTP_LENGTH) {
                                    Toast.makeText(context, "Please enter 6-digit code", Toast.LENGTH_SHORT).show()
                                } else {
                                    authViewModel.verifyOtp(email.trim().lowercase(), otpCode, "LOGIN")
                                }
                            },
                            onNavigateToForgotPassword = onNavigateToForgotPassword,
                            onNavigateToCreateAccount = onNavigateToCreateAccount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailStepContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onContinue: () -> Unit,
    onNavigateToCreateAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SonexaHeaderLogo()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Log in to Zynera",
            style = AppleType.largeTitle,
            color = SonexaTextWhite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your email address to continue",
            style = AppleType.subheadline,
            color = SonexaTextMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppleFieldLabel("Email address")
        SonexaInputField(
            value = email,
            onValueChange = onEmailChange,
            placeholderText = "name@email.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            textStyle = AppleType.body.copy(color = SonexaTextWhite)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SonexaGradientButton(
            text = if (isLoading) "Continuing…" else "Continue",
            labelStyle = AppleType.headline.copy(color = Color.White),
            onClick = onContinue
        )

        Spacer(modifier = Modifier.height(32.dp))

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

@Composable
private fun VerifyStepContent(
    email: String,
    verifyMode: VerifyMode,
    onSwitchMode: (VerifyMode) -> Unit,
    onBackToEmail: () -> Unit,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    resendSecondsLeft: Int,
    onResendOtp: () -> Unit,
    focusRequester: FocusRequester,
    isLoading: Boolean,
    onLoginWithPassword: () -> Unit,
    onLoginWithOtp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToCreateAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Navigation Row
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
                    .clickable { onBackToEmail() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SonexaTextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Email pill badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x33120C24))
                    .border(1.dp, Color(0x44B062FF), RoundedCornerShape(999.dp))
                    .clickable { onBackToEmail() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = email,
                        style = AppleType.caption1.copy(fontWeight = FontWeight.SemiBold),
                        color = SonexaPurpleLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit",
                        style = AppleType.caption2.copy(fontWeight = FontWeight.Medium),
                        color = SonexaTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (verifyMode == VerifyMode.OTP) {
            // OTP Mode
            Text(
                text = "Check your email",
                style = AppleType.largeTitle,
                color = SonexaTextWhite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We sent a 6-digit verification code to\n$email",
                style = AppleType.subheadline,
                color = SonexaTextMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            OtpDigitBoxes(
                otpCode = otpCode,
                onOtpChange = onOtpChange,
                focusRequester = focusRequester,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OtpResendRow(
                secondsRemaining = resendSecondsLeft,
                onResend = onResendOtp
            )

            Spacer(modifier = Modifier.height(24.dp))

            SonexaGradientButton(
                text = if (isLoading) "Verifying…" else "Log In",
                labelStyle = AppleType.headline.copy(color = Color.White),
                onClick = onLoginWithOtp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(20.dp))

            // Prominent button: Log in with password
            SecondaryOptionButton(
                text = "Log in with password",
                icon = Icons.Default.Lock,
                onClick = { onSwitchMode(VerifyMode.PASSWORD) }
            )

        } else {
            // Password Mode
            Text(
                text = "Enter your password",
                style = AppleType.largeTitle,
                color = SonexaTextWhite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log in with the password for $email",
                style = AppleType.subheadline,
                color = SonexaTextMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            AppleFieldLabel("Password")
            SonexaInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholderText = "Enter password",
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
                text = if (isLoading) "Logging in…" else "Log In",
                labelStyle = AppleType.headline.copy(color = Color.White),
                onClick = onLoginWithPassword
            )

            Spacer(modifier = Modifier.height(24.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(20.dp))

            // Prominent button: Log in with OTP / code
            SecondaryOptionButton(
                text = "Log in with verification code",
                icon = Icons.Default.Email,
                onClick = { onSwitchMode(VerifyMode.OTP) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SonexaInputBorder.copy(alpha = 0.5f),
            thickness = 1.dp
        )
        Text(
            text = "OR",
            style = AppleType.caption2.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
            color = SonexaTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SonexaInputBorder.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun SecondaryOptionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SonexaInputBg)
            .border(1.dp, Color(0x66B062FF), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SonexaPurpleLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = AppleType.headline.copy(fontWeight = FontWeight.SemiBold),
                color = SonexaTextWhite
            )
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
