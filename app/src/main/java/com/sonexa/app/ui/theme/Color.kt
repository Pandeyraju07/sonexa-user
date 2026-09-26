package com.sonexa.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Apple Design (OLED Obsidian & Hairline Glass) & Publicis Sapient Palettes
val SonexaBgDark = Color(0xFF07070A)
val SonexaCardDark = Color(0xFF121218)
val SonexaCardBorder = Color(0xFF24242F)
val SonexaInputBg = Color(0xFF0F0F14)
val SonexaInputBorder = Color(0xFF22222C)

// Brand Accents: Publicis Sapient Energetic Ruby & Apple System Indigo
val SonexaPurplePrimary = Color(0xFFFE2C55)
val SonexaPurpleLight = Color(0xFFFF6584)
val SonexaMagenta = Color(0xFFFF2D55)
val SonexaPinkAccent = Color(0xFFFF52C4)

// High Contrast Typography: Apple Pure Whites & Slate Muted
val SonexaTextWhite = Color(0xFFFFFFFF)
val SonexaTextMuted = Color(0xFF8E8E98)
val SonexaTextSubtle = Color(0xFF575762)
val SpotifyGreen = Color(0xFF30D158) // Apple Emerald Green

// Luxury Specular Gradients
val SonexaGradientBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFE2C55),
        Color(0xFF8B5CF6),
        Color(0xFF6366F1)
    )
)

val SonexaGlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x35FE2C55),
        Color(0x156366F1),
        Color.Transparent
    )
)
