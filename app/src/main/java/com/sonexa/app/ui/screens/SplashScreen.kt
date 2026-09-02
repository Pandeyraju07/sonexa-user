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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
            // Zynera Futuristic Sonic Mark
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Ambient Radial Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x809825DD),
                                Color(0x3038BDF8),
                                Color.Transparent
                            ),
                            center = Offset(w / 2, h / 2),
                            radius = (w * 0.60f) * pulseScale
                        )
                    )

                    val brandGrad = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFFB062FF),
                            Color(0xFFE534B2)
                        ),
                        start = Offset(w * 0.15f, h * 0.15f),
                        end = Offset(w * 0.85f, h * 0.85f)
                    )

                    // Zynera Geometric Z-Prism Mark
                    val zPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.30f)
                        lineTo(w * 0.76f, h * 0.30f)
                        lineTo(w * 0.32f, h * 0.70f)
                        lineTo(w * 0.76f, h * 0.70f)
                    }

                    drawPath(
                        path = zPath,
                        brush = brandGrad,
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Sonic Equalizer Core inside the Z Diagonal
                    val barWidth = 3.5.dp.toPx()
                    val barHeights = listOf(0.25f, 0.45f, 0.30f)
                    val barOffsetsX = listOf(0.42f, 0.54f, 0.66f)

                    barOffsetsX.forEachIndexed { index, xRatio ->
                        val x = w * xRatio
                        val bh = h * barHeights[index] * pulseScale
                        val y = h * 0.50f - bh / 2
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFFFF52C4))
                            ),
                            start = Offset(x, y),
                            end = Offset(x, y + bh),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Official Brand Name
            Text(
                text = "ZYNERA",
                fontSize = 34.sp,
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
