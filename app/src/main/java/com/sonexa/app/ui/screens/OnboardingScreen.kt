package com.sonexa.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.SonexaCardBorder
import com.sonexa.app.ui.theme.SonexaPurpleLight
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color1: Color,
    val color2: Color
)

private val onboardingPalette = listOf(
    Triple(Icons.Default.Headphones, Color(0xFF5935E5), Color(0xFF9825DD)),
    Triple(Icons.Default.GraphicEq, Color(0xFF9825DD), Color(0xFFE534B2)),
    Triple(Icons.Default.Download, Color(0xFFE534B2), Color(0xFFFF52C4)),
    Triple(Icons.Default.SpatialAudioOff, Color(0xFF5935E5), Color(0xFFE534B2))
)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val fallbackSlides = listOf(
        OnboardingSlide(
            "Endless Streaming",
            "Stream millions of songs & curated playlists in studio-quality lossless audio.",
            Icons.Default.Headphones,
            Color(0xFF5935E5),
            Color(0xFF9825DD)
        ),
        OnboardingSlide(
            "Personalized AI DJ",
            "Let Sonexa AI curate perfect music mixes tailored to your mood & current activity.",
            Icons.Default.GraphicEq,
            Color(0xFF9825DD),
            Color(0xFFE534B2)
        ),
        OnboardingSlide(
            "Offline Downloads",
            "Download your favorite albums and playlists to listen anywhere without data.",
            Icons.Default.Download,
            Color(0xFFE534B2),
            Color(0xFFFF52C4)
        ),
        OnboardingSlide(
            "Immersive 3D Spatial Audio",
            "Experience 360-degree spatial sound and glowing visualizer effects.",
            Icons.Default.SpatialAudioOff,
            Color(0xFF5935E5),
            Color(0xFFE534B2)
        )
    )

    val slides = when (val state = uiState) {
        is CatalogUiState.Ready -> state.data.mapIndexed { index, dto ->
            val palette = onboardingPalette[index % onboardingPalette.size]
            OnboardingSlide(
                title = dto.title.ifBlank { fallbackSlides.getOrNull(index)?.title ?: "Sonexa" },
                description = dto.subtitle.ifBlank { fallbackSlides.getOrNull(index)?.description.orEmpty() },
                icon = palette.first,
                color1 = palette.second,
                color2 = palette.third
            )
        }.ifEmpty { fallbackSlides }
        else -> fallbackSlides
    }

    val pagerState = rememberPagerState(pageCount = { slides.size.coerceAtLeast(1) })
    val coroutineScope = rememberCoroutineScope()

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
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Skip Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SonexaTextMuted,
                    modifier = Modifier.clickable { onOnboardingComplete() }
                )
            }

            // Center Horizontal Pager for Left/Right Swipe Gestures
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val slide = slides[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    // Glowing Circle Graphic Card
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Brush.linearGradient(listOf(slide.color1.copy(alpha = 0.3f), slide.color2.copy(alpha = 0.3f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(slide.color2.copy(alpha = 0.6f), Color.Transparent),
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width * 0.7f
                                )
                            )
                        }

                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = slide.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SonexaTextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = slide.description,
                        fontSize = 14.sp,
                        color = SonexaTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Bottom Carousel Indicators & Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val active = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (active) SonexaPurpleLight else SonexaCardBorder)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SonexaGradientButton(
                    text = if (pagerState.currentPage < slides.size - 1) "Next" else "Get Started",
                    onClick = {
                        if (pagerState.currentPage < slides.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
