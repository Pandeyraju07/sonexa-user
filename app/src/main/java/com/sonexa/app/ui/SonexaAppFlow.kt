package com.sonexa.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.auth.social.SocialAuthEvents
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.ui.screens.*
import com.sonexa.app.ui.viewmodel.AuthViewModel
import com.sonexa.app.ui.viewmodel.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest

enum class AppStep {
    SPLASH,
    UPDATE_CHECK,
    ONBOARDING,
    CHOOSE_LANGUAGE,
    WELCOME,
    LOGIN,
    REGISTER,
    OTP_VERIFICATION,
    FORGOT_PASSWORD,
    RESET_PASSWORD,
    CREATE_PROFILE,
    GENRE_SELECTION,
    ARTIST_SELECTION,
    MOOD_SELECTION,
    NOTIFICATION_PERMISSION,
    DOWNLOAD_PERMISSION,
    HOME,
    FULL_PLAYER,
    AI_SIGNATURE_HUB,
    ALBUM_DETAIL,
    ARTIST_PROFILE,
    PLAYLIST_DETAIL,
    PODCAST_HUB,
    NOTIFICATION_CENTER,
    PREMIUM,
    PROFILE_HUB,
    SETTINGS,
    AUTH_UTILITIES,
    NO_INTERNET_ERROR,
    SERVER_ERROR
}

@Composable
fun SonexaAppFlow() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModel.Factory
    )
    val sessionManager = remember { SessionManager.getInstance(context) }
    var currentStep by remember {
        mutableStateOf(if (sessionManager.isLoggedIn()) AppStep.HOME else AppStep.SPLASH)
    }
    var otpEmail by remember { mutableStateOf(sessionManager.pendingOtpEmail.orEmpty()) }
    var detailId by remember { mutableStateOf("") }

    fun goHome() {
        currentStep = AppStep.HOME
    }

    // System / gesture back: return detail & hub screens to the dashboard
    val popsToDashboard = currentStep in setOf(
        AppStep.FULL_PLAYER,
        AppStep.AI_SIGNATURE_HUB,
        AppStep.ALBUM_DETAIL,
        AppStep.ARTIST_PROFILE,
        AppStep.PLAYLIST_DETAIL,
        AppStep.PODCAST_HUB,
        AppStep.NOTIFICATION_CENTER,
        AppStep.PREMIUM,
        AppStep.PROFILE_HUB,
        AppStep.SETTINGS,
        AppStep.AUTH_UTILITIES,
        AppStep.NO_INTERNET_ERROR,
        AppStep.SERVER_ERROR
    )
    BackHandler(enabled = popsToDashboard, onBack = ::goHome)

    LaunchedEffect(Unit) {
        SocialAuthEvents.appleResult.collectLatest { profile ->
            authViewModel.appleSignIn(
                identityToken = profile.idToken,
                email = profile.email,
                name = profile.name
            ) {
                currentStep = AppStep.HOME
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentStep,
            animationSpec = tween(durationMillis = 350),
            label = "SonexaFlowAnimation"
        ) { step ->
            when (step) {
                AppStep.SPLASH -> SplashScreen(
                    onSplashComplete = { currentStep = AppStep.UPDATE_CHECK },
                    onFatalError = { message ->
                        currentStep = AppErrorRouter.stepForMessage(message)
                    }
                )
                AppStep.UPDATE_CHECK -> AppUpdateCheckScreen(
                    onContinue = { currentStep = AppStep.ONBOARDING }
                )
                AppStep.ONBOARDING -> OnboardingScreen(
                    onOnboardingComplete = { currentStep = AppStep.CHOOSE_LANGUAGE }
                )
                AppStep.CHOOSE_LANGUAGE -> ChooseLanguageScreen(
                    onContinue = { currentStep = AppStep.WELCOME }
                )
                AppStep.WELCOME -> WelcomeScreen(
                    onNavigateToSignUp = { currentStep = AppStep.REGISTER },
                    onNavigateToLogin = { currentStep = AppStep.LOGIN }
                )
                AppStep.LOGIN -> LoginScreen(
                    onNavigateToCreateAccount = { currentStep = AppStep.REGISTER },
                    onNavigateToForgotPassword = { currentStep = AppStep.FORGOT_PASSWORD },
                    onNavigateToOtp = { email ->
                        otpEmail = email
                        currentStep = AppStep.OTP_VERIFICATION
                    },
                    onLoginSuccess = { currentStep = AppStep.HOME },
                    authViewModel = authViewModel
                )
                AppStep.REGISTER -> CreateAccountScreen(
                    onNavigateToLogin = { currentStep = AppStep.LOGIN },
                    onSignUpSuccess = { email ->
                        otpEmail = email
                        currentStep = AppStep.OTP_VERIFICATION
                    },
                    onSocialSuccess = { currentStep = AppStep.HOME },
                    authViewModel = authViewModel
                )
                AppStep.OTP_VERIFICATION -> OtpVerificationScreen(
                    email = otpEmail.ifBlank { authViewModel.pendingOtpEmail.value },
                    onNavigateBack = { currentStep = AppStep.REGISTER },
                    onOtpVerified = { currentStep = AppStep.CREATE_PROFILE },
                    authViewModel = authViewModel
                )
                AppStep.FORGOT_PASSWORD -> ForgotPasswordScreen(
                    onNavigateToLogin = { currentStep = AppStep.LOGIN },
                    authViewModel = authViewModel
                )
                AppStep.RESET_PASSWORD -> ResetPasswordScreen(
                    onNavigateBack = { currentStep = AppStep.FORGOT_PASSWORD },
                    onPasswordResetSuccess = { currentStep = AppStep.LOGIN }
                )
                AppStep.CREATE_PROFILE -> CreateProfileScreen(
                    onProfileCreated = { currentStep = AppStep.GENRE_SELECTION }
                )
                AppStep.GENRE_SELECTION -> GenreSelectionScreen(
                    onGenresSelected = { currentStep = AppStep.ARTIST_SELECTION }
                )
                AppStep.ARTIST_SELECTION -> ArtistSelectionScreen(
                    onArtistsSelected = { currentStep = AppStep.MOOD_SELECTION }
                )
                AppStep.MOOD_SELECTION -> MoodSelectionScreen(
                    onMoodsSelected = { currentStep = AppStep.NOTIFICATION_PERMISSION }
                )
                AppStep.NOTIFICATION_PERMISSION -> NotificationPermissionScreen(
                    onPermissionGranted = { currentStep = AppStep.DOWNLOAD_PERMISSION },
                    onSkip = { currentStep = AppStep.DOWNLOAD_PERMISSION }
                )
                AppStep.DOWNLOAD_PERMISSION -> DownloadPermissionScreen(
                    onPermissionGranted = { currentStep = AppStep.HOME },
                    onSkip = { currentStep = AppStep.HOME }
                )
                AppStep.HOME -> {
                    val homeVm: com.sonexa.app.ui.viewmodel.HomeViewModel = viewModel()
                    val homeState by homeVm.uiState.collectAsState()
                    LaunchedEffect(homeState) {
                        val err = homeState as? com.sonexa.app.ui.viewmodel.HomeUiState.Error ?: return@LaunchedEffect
                        // Surface hard failures from real backend (mock interceptor removed)
                        if (err.message.contains("failed", ignoreCase = true) ||
                            err.message.contains("Unable to resolve", ignoreCase = true) ||
                            err.message.contains("Failed to connect", ignoreCase = true)
                        ) {
                            currentStep = AppErrorRouter.stepForMessage(err.message)
                        }
                    }
                    HomeScreen(
                    onLogout = {
                        authViewModel.logout { currentStep = AppStep.WELCOME }
                    },
                    onOpenFullPlayer = { currentStep = AppStep.FULL_PLAYER },
                    onOpenAlbum = { id ->
                        detailId = id
                        currentStep = AppStep.ALBUM_DETAIL
                    },
                    onOpenPlaylist = { id ->
                        detailId = id
                        currentStep = AppStep.PLAYLIST_DETAIL
                    },
                    onOpenArtist = { id ->
                        detailId = id
                        currentStep = AppStep.ARTIST_PROFILE
                    },
                    onOpenAiSignature = { currentStep = AppStep.AI_SIGNATURE_HUB },
                    onOpenPodcasts = { currentStep = AppStep.PODCAST_HUB },
                    onOpenNotifications = { currentStep = AppStep.NOTIFICATION_CENTER },
                    onOpenPremium = { currentStep = AppStep.PREMIUM },
                    onOpenProfile = { currentStep = AppStep.PROFILE_HUB },
                    onOpenSettings = { currentStep = AppStep.SETTINGS },
                    homeViewModel = homeVm,
                    playbackViewModel = playbackViewModel
                )
                }
                AppStep.FULL_PLAYER -> FullPlayerScreen(
                    onMinimize = ::goHome,
                    playbackViewModel = playbackViewModel
                )
                AppStep.AI_SIGNATURE_HUB -> AISignatureScreen(
                    onNavigateBack = ::goHome
                )
                AppStep.ALBUM_DETAIL -> AlbumDetailScreen(
                    albumId = detailId,
                    onNavigateBack = ::goHome,
                    onOpenFullPlayer = { currentStep = AppStep.FULL_PLAYER },
                    playbackViewModel = playbackViewModel
                )
                AppStep.ARTIST_PROFILE -> ArtistProfileScreen(
                    artistId = detailId,
                    onNavigateBack = ::goHome,
                    onOpenFullPlayer = { currentStep = AppStep.FULL_PLAYER },
                    playbackViewModel = playbackViewModel
                )
                AppStep.PLAYLIST_DETAIL -> PlaylistDetailScreen(
                    playlistId = detailId,
                    onNavigateBack = ::goHome,
                    onOpenFullPlayer = { currentStep = AppStep.FULL_PLAYER },
                    playbackViewModel = playbackViewModel
                )
                AppStep.PODCAST_HUB -> PodcastHubScreen(
                    onNavigateBack = ::goHome,
                    onOpenFullPlayer = { currentStep = AppStep.FULL_PLAYER },
                    playbackViewModel = playbackViewModel
                )
                AppStep.NOTIFICATION_CENTER -> NotificationCenterScreen(
                    onNavigateBack = ::goHome
                )
                AppStep.PREMIUM -> PremiumScreen(
                    onNavigateBack = ::goHome
                )
                AppStep.PROFILE_HUB -> ProfileHubScreen(
                    onNavigateBack = ::goHome,
                    onOpenSettings = { currentStep = AppStep.SETTINGS },
                    onOpenPremium = { currentStep = AppStep.PREMIUM },
                    onOpenNotifications = { currentStep = AppStep.NOTIFICATION_CENTER },
                    onLogout = {
                        authViewModel.logout { currentStep = AppStep.WELCOME }
                    }
                )
                AppStep.SETTINGS -> SettingsScreen(
                    onNavigateBack = ::goHome,
                    onLogout = {
                        authViewModel.logout { currentStep = AppStep.WELCOME }
                    }
                )
                AppStep.AUTH_UTILITIES -> AuthUtilitiesScreen(
                    onNavigateBack = ::goHome
                )
                AppStep.NO_INTERNET_ERROR -> NoInternetScreen(
                    onRetry = ::goHome
                )
                AppStep.SERVER_ERROR -> ServerErrorScreen(
                    onRetry = ::goHome
                )
            }
        }
    }
}
