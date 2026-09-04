package com.sonexa.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.theme.*
import kotlinx.coroutines.delay

import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.viewmodel.SplashUiState
import com.sonexa.app.ui.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    onFatalError: (String) -> Unit = {},
    splashViewModel: SplashViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val splashState by splashViewModel.uiState.collectAsState()

    LaunchedEffect(splashState) {
        when (val state = splashState) {
            is SplashUiState.Success -> {
                delay(1400)
                onSplashComplete()
            }
            is SplashUiState.Error -> {
                delay(1200)
                if (state.message.contains("500") || state.message.contains("Internal", ignoreCase = true)) {
                    onFatalError(state.message)
                } else {
                    onSplashComplete()
                }
            }
            else -> Unit
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF160938),
                        Color(0xFF0C061E),
                        Color(0xFF06030F)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Zynera Official App Logo Mark
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(
                        scaleX = pulseScale,
                        scaleY = pulseScale
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Ambient Radial Glow behind logo
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x90EC4899),
                                    Color(0x508B5CF6),
                                    Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.sonexa.app.R.drawable.zynera_logo),
                    contentDescription = "Zynera Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Official Brand Name
            Text(
                text = "ZYNERA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SonexaTextWhite,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Official Positioning Tagline
            Text(
                text = "Your mood. Your music.",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = SonexaPurpleLight,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = SonexaPurpleLight,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
