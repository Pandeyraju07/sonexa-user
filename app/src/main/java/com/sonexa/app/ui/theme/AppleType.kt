package com.sonexa.app.ui.theme

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
    val title2 = style(22, FontWeight.Normal, 28, 0.35f)
    val title3 = style(20, FontWeight.Normal, 25, 0.38f)
    val headline = style(17, FontWeight.SemiBold, 22, -0.41f)
    val body = style(17, FontWeight.Normal, 22, -0.41f)
    val callout = style(16, FontWeight.Normal, 21, -0.32f)
    val subheadline = style(15, FontWeight.Normal, 20, -0.24f)
    val footnote = style(13, FontWeight.Normal, 18, -0.08f)
    val caption1 = style(12, FontWeight.Normal, 16, 0.00f)
    val caption2 = style(11, FontWeight.Normal, 13, 0.06f)

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
