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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaCheckboxRow
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.components.SonexaInputField
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AuthUiState
import com.sonexa.app.ui.viewmodel.AuthViewModel

enum class CreateAccountStep {
    EMAIL,
    PASSWORD
}

@Composable
fun CreateAccountScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onSocialSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()

    var step by remember { mutableStateOf(CreateAccountStep.EMAIL) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(true) }

    var isCheckingEmail by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val selectionColors = TextSelectionColors(
        handleColor = SonexaPurpleLight,
        backgroundColor = SonexaPurpleLight.copy(alpha = 0.35f)
    )

    BackHandler(enabled = step == CreateAccountStep.PASSWORD) {
        step = CreateAccountStep.EMAIL
    }

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
                onSignUpSuccess(email.trim().lowercase())
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
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
                    if (targetState == CreateAccountStep.PASSWORD) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "CreateAccountStepAnimation"
            ) { currentStep ->
                when (currentStep) {
                    CreateAccountStep.EMAIL -> {
                        EmailSignupStep(
                            email = email,
                            onEmailChange = {
                                email = it
                                emailError = null
                            },
                            emailError = emailError,
                            isChecking = isCheckingEmail,
                            onBackToLogin = onNavigateToLogin,
                            onNext = {
                                val trimmed = email.trim()
                                when {
                                    trimmed.isBlank() ->
                                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                    !trimmed.contains("@") || !trimmed.contains(".") ->
                                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        isCheckingEmail = true
                                        authViewModel.checkEmail(
                                            email = trimmed.lowercase(),
                                            onResult = { exists ->
                                                isCheckingEmail = false
                                                if (exists) {
                                                    emailError = "An account with this email already exists."
                                                    Toast.makeText(context, "This email is already registered. Please log in.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    emailError = null
                                                    step = CreateAccountStep.PASSWORD
                                                }
                                            },
                                            onError = {
                                                isCheckingEmail = false
                                                // If network check failed or server error, allow advancing gracefully
                                                step = CreateAccountStep.PASSWORD
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                    CreateAccountStep.PASSWORD -> {
                        PasswordSignupStep(
                            email = email.trim().lowercase(),
                            password = password,
                            onPasswordChange = { password = it },
                            fullName = fullName,
                            onFullNameChange = { fullName = it },
                            agreedToTerms = agreedToTerms,
                            onAgreedToTermsChange = { agreedToTerms = it },
                            isLoading = authState is AuthUiState.Loading,
                            onBackToEmail = { step = CreateAccountStep.EMAIL },
                            onNavigateToLogin = onNavigateToLogin,
                            onCreateAccount = {
                                when {
                                    password.isBlank() ->
                                        Toast.makeText(context, "Please choose a password", Toast.LENGTH_SHORT).show()
                                    password.length < 6 ->
                                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    !agreedToTerms ->
                                        Toast.makeText(context, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        val trimmedEmail = email.trim().lowercase()
                                        val resolvedName = fullName.trim().ifBlank {
                                            trimmedEmail.substringBefore("@")
                                        }
                                        authViewModel.register(
                                            email = trimmedEmail,
                                            name = resolvedName,
                                            pass = password
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailSignupStep(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isChecking: Boolean,
    onBackToLogin: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button to Login
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
                    .clickable { onBackToLogin() },
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

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "What's your email address?",
            style = AppleType.largeTitle,
            color = SonexaTextWhite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You'll need to confirm this email later.",
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

        if (emailError != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33EF4444))
                    .border(1.dp, Color(0x66EF4444), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = emailError,
                            style = AppleType.footnote.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFFCA5A5)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log In",
                        style = AppleType.caption1.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x44FFFFFF))
                            .clickable { onBackToLogin() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SonexaGradientButton(
            text = if (isChecking) "Checking email…" else "Next",
            labelStyle = AppleType.headline.copy(color = Color.White),
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = AppleType.footnote,
                color = SonexaTextMuted
            )
            Text(
                text = "Log in",
                style = AppleType.footnote.copy(fontWeight = FontWeight.SemiBold),
                color = SonexaPurpleLight,
                modifier = Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .clickable { onBackToLogin() }
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun PasswordSignupStep(
    email: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    agreedToTerms: Boolean,
    onAgreedToTermsChange: (Boolean) -> Unit,
    isLoading: Boolean,
    onBackToEmail: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onCreateAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
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

            // Verified Email Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x33120C24))
                    .border(1.dp, Color(0x44B062FF), RoundedCornerShape(999.dp))
                    .clickable { onBackToEmail() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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

        Text(
            text = "Create a password",
            style = AppleType.largeTitle,
            color = SonexaTextWhite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use at least 6 characters.",
            style = AppleType.subheadline,
            color = SonexaTextMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppleFieldLabel("Password")
        SonexaInputField(
            value = password,
            onValueChange = onPasswordChange,
            placeholderText = "At least 6 characters",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            textStyle = AppleType.body.copy(color = SonexaTextWhite)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppleFieldLabel("What should we call you? (optional)")
        SonexaInputField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholderText = "Display name",
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            textStyle = AppleType.body.copy(color = SonexaTextWhite)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SonexaCheckboxRow(
            checked = agreedToTerms,
            onCheckedChange = onAgreedToTermsChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        SonexaGradientButton(
            text = if (isLoading) "Creating Account…" else "Create Account",
            labelStyle = AppleType.headline.copy(color = Color.White),
            onClick = onCreateAccount
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
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
