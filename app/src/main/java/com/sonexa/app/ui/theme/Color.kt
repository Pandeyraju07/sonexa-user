package com.sonexa.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── ZYNERA PREMIUM DESIGN SYSTEM TOKENS ──────────────────────────────────────
val ZyneraBg = Color(0xFF080810)              // Near-black and deep obsidian
val ZyneraSurface = Color(0xFF12111F)         // Layered dark navy / subtle purple-black
val ZyneraSurfaceElevated = Color(0xFF1B1830) // Elevated card surface
val ZyneraPrimary = Color(0xFFA855F7)         // Vibrant violet / purple
val ZyneraPrimaryBright = Color(0xFFC084FC)   // Lavender bright violet
val ZyneraSecondary = Color(0xFF38BDF8)       // Electric Cyan / Restrained Blue
val ZyneraTextPrimary = Color(0xFFF8FAFC)     // High-contrast pure slate white
val ZyneraTextSecondary = Color(0xFFA1A1B5)   // Muted slate secondary text
val ZyneraTextSubtle = Color(0xFF6B6A82)      // Subtle caption / metadata text
val ZyneraBorder = Color(0xFF302744)          // Controlled dark violet hairline border
val ZyneraSuccess = Color(0xFF22C55E)         // Emerald green for downloads / active states

// Legacy / Component Compatibility Aliases
val SonexaBgDark = ZyneraBg
val SonexaCardDark = ZyneraSurface
val SonexaCardBorder = ZyneraBorder
val SonexaInputBg = Color(0xFF100F1C)
val SonexaInputBorder = ZyneraBorder
val SonexaPurplePrimary = ZyneraPrimary
val SonexaPurpleLight = ZyneraPrimaryBright
val SonexaMagenta = Color(0xFFFF2D55)
val SonexaPinkAccent = Color(0xFFFF52C4)
val SonexaTextWhite = ZyneraTextPrimary
val SonexaTextMuted = ZyneraTextSecondary
val SonexaTextSubtle = ZyneraTextSubtle
val SpotifyGreen = ZyneraSuccess

// Luxury Gradients
val ZyneraVioletGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFA855F7),
        Color(0xFF7C3AED),
        Color(0xFF38BDF8)
    )
)

val ZyneraHeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF231640),
        Color(0xFF16122C),
        Color(0xFF0C0A18)
    )
)

val SonexaGradientBrush = ZyneraVioletGradient

val SonexaGlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x35A855F7),
        Color(0x1538BDF8),
        Color.Transparent
    )
)
