package com.sonexa.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * iOS text styles from Apple's Human Interface Guidelines.
 * Sizes, weights, line heights, and tracking follow the default
 * Large content-size styles on https://developer.apple.com/design/human-interface-guidelines/typography
 * SF Pro is licensed for Apple platforms, so these styles use the system sans.
 */
object AppleType {
    private val family = FontFamily.SansSerif

    val largeTitle = style(34, FontWeight.Bold, 41, -0.40f)
    val title1 = style(28, FontWeight.Normal, 34, 0.36f)
    val title1Bold = style(28, FontWeight.Bold, 34, 0.36f)
    val title2 = style(22, FontWeight.Normal, 28, 0.35f)
    val title2Bold = style(22, FontWeight.Bold, 28, 0.35f)
    val title3 = style(20, FontWeight.Normal, 25, 0.38f)
    val title3Bold = style(20, FontWeight.SemiBold, 25, 0.38f)
    val headline = style(17, FontWeight.SemiBold, 22, -0.41f)
    val headlineBold = style(17, FontWeight.Bold, 22, -0.41f)
    val body = style(17, FontWeight.Normal, 22, -0.41f)
    val bodyBold = style(17, FontWeight.SemiBold, 22, -0.41f)
    val callout = style(16, FontWeight.Normal, 21, -0.32f)
    val calloutBold = style(16, FontWeight.SemiBold, 21, -0.32f)
    val subheadline = style(15, FontWeight.Normal, 20, -0.24f)
    val subheadlineBold = style(15, FontWeight.SemiBold, 20, -0.24f)
    val footnote = style(13, FontWeight.Normal, 18, -0.08f)
    val footnoteBold = style(13, FontWeight.SemiBold, 18, -0.08f)
    val caption1 = style(12, FontWeight.Normal, 16, 0.00f)
    val caption1Bold = style(12, FontWeight.SemiBold, 16, 0.00f)
    val caption2 = style(11, FontWeight.Normal, 13, 0.06f)
    val caption2Bold = style(11, FontWeight.SemiBold, 13, 0.06f)

    private fun style(
        size: Int,
        weight: FontWeight,
        lineHeight: Int,
        tracking: Float
    ) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = tracking.sp
    )
}

/**
 * Apple Human Interface Guidelines Materials and Surface Design Tokens
 */
object AppleMaterials {
    val glassFill = Color(0x12FFFFFF)
    val glassFillMedium = Color(0x1CFFFFFF)
    val glassFillThick = Color(0xFF161622)
    val cardSurface = Color(0xFF13131D)
    val cardSurfaceSecondary = Color(0xFF1A1A28)
    val inputBackground = Color(0xFF161622)

    val hairlineBorder = Color.White.copy(alpha = 0.12f)
    val subtleBorder = Color.White.copy(alpha = 0.07f)

    val specularBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.06f)
        )
    )

    val cardBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.04f)
        )
    )
}

/**
 * Tactile spring micro-interaction for Apple-style cards and buttons
 */
fun Modifier.appleBounce(
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 380f
        ),
        label = "appleBounce"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        tryAwaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = { onClick() }
            )
        }
}
