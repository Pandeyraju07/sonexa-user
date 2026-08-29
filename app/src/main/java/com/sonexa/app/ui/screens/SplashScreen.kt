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
                delay(1500)
                onSplashComplete()
            }
            is SplashUiState.Error -> {
                delay(1200)
                // Soft offline path continues; only escalate hard server failures without mock fallback
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
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
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
                        Color(0xFF130933),
                        Color(0xFF090614),
                        Color(0xFF05030A)
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
            // Animated Pulsing Logo
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Glowing Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x909825DD), Color(0x305935E5), Color.Transparent),
                            center = Offset(w / 2, h / 2),
                            radius = (w * 0.55f) * pulseScale
                        )
                    )

                    // Equalizer bars
                    val purpleGrad = Brush.verticalGradient(
                        colors = listOf(Color(0xFFB062FF), Color(0xFFE534B2))
                    )
                    val barW = 4.dp.toPx()
                    val barG = 5.dp.toPx()
                    val heights = listOf(0.35f, 0.65f, 0.95f, 0.7f, 0.45f)
                    val startX = w * 0.22f

                    heights.forEachIndexed { i, hr ->
                        val x = startX + i * (barW + barG)
                        val bh = h * 0.45f * hr * pulseScale
                        val y = h * 0.5f - bh / 2
                        drawLine(
                            brush = purpleGrad,
                            start = Offset(x, y),
                            end = Offset(x, y + bh),
                            strokeWidth = barW,
                            cap = StrokeCap.Round
                        )
                    }

                    // S Curve
                    val path = Path().apply {
                        moveTo(w * 0.75f, h * 0.22f)
                        cubicTo(
                            w * 0.42f, h * 0.1f,
                            w * 0.25f, h * 0.35f,
                            w * 0.48f, h * 0.5f
                        )
                        cubicTo(
                            w * 0.78f, h * 0.65f,
                            w * 0.62f, h * 0.9f,
                            w * 0.2f, h * 0.78f
                        )
                    }
                    drawPath(
                        path = path,
                        brush = purpleGrad,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SONEXA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SonexaTextWhite,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Feel the ", fontSize = 13.sp, color = SonexaTextMuted)
                Text(text = "Music", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SonexaPurpleLight)
                Text(text = ". Live the ", fontSize = 13.sp, color = SonexaTextMuted)
                Text(text = "Vibe", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SonexaMagenta)
                Text(text = ".", fontSize = 13.sp, color = SonexaTextMuted)
            }

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = SonexaPurpleLight,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
