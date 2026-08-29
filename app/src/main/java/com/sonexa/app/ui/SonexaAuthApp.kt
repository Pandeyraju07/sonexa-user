package com.sonexa.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.screens.CreateAccountScreen
import com.sonexa.app.ui.screens.ForgotPasswordScreen
import com.sonexa.app.ui.screens.HomeScreen
import com.sonexa.app.ui.screens.LoginScreen
import com.sonexa.app.ui.screens.OtpVerificationScreen
import com.sonexa.app.ui.viewmodel.AuthViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

enum class AuthScreen {
    LOGIN,
    CREATE_ACCOUNT,
    OTP_VERIFICATION,
    FORGOT_PASSWORD,
    HOME
}

@Composable
fun SonexaAuthApp() {
    val authViewModel: AuthViewModel = viewModel()
    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModel.Factory
    )
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    var otpEmail by remember { mutableStateOf("") }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 350),
        label = "AuthScreenAnimation"
    ) { screen ->
        when (screen) {
            AuthScreen.LOGIN -> LoginScreen(
                onNavigateToCreateAccount = { currentScreen = AuthScreen.CREATE_ACCOUNT },
                onNavigateToForgotPassword = { currentScreen = AuthScreen.FORGOT_PASSWORD },
                onNavigateToOtp = { email ->
                    otpEmail = email
                    currentScreen = AuthScreen.OTP_VERIFICATION
                },
                onLoginSuccess = { currentScreen = AuthScreen.HOME },
                authViewModel = authViewModel
            )
            AuthScreen.CREATE_ACCOUNT -> CreateAccountScreen(
                onNavigateToLogin = { currentScreen = AuthScreen.LOGIN },
                onSignUpSuccess = { email ->
                    otpEmail = email
                    currentScreen = AuthScreen.OTP_VERIFICATION
                },
                onSocialSuccess = { currentScreen = AuthScreen.HOME },
                authViewModel = authViewModel
            )
            AuthScreen.OTP_VERIFICATION -> OtpVerificationScreen(
                email = otpEmail.ifBlank { authViewModel.pendingOtpEmail.value },
                onNavigateBack = { currentScreen = AuthScreen.CREATE_ACCOUNT },
                onOtpVerified = { currentScreen = AuthScreen.HOME },
                authViewModel = authViewModel
            )
            AuthScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                onNavigateToLogin = { currentScreen = AuthScreen.LOGIN },
                authViewModel = authViewModel
            )
            AuthScreen.HOME -> HomeScreen(
                onLogout = {
                    authViewModel.logout { currentScreen = AuthScreen.LOGIN }
                },
                playbackViewModel = playbackViewModel
            )
        }
    }
}
