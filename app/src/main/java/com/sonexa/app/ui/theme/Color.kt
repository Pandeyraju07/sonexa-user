package com.sonexa.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SonexaBgDark = Color(0xFF0A0714)
val SonexaCardDark = Color(0xFF130E26)
val SonexaCardBorder = Color(0xFF2B214A)
val SonexaInputBg = Color(0xFF120C24)
val SonexaInputBorder = Color(0xFF292048)

val SonexaPurplePrimary = Color(0xFF6B3CE9)
val SonexaPurpleLight = Color(0xFFB062FF)
val SonexaMagenta = Color(0xFFE534B2)
val SonexaPinkAccent = Color(0xFFFF52C4)

val SonexaTextWhite = Color(0xFFFFFFFF)
val SonexaTextMuted = Color(0xFF9EA4B0)
val SonexaTextSubtle = Color(0xFF6C7280)
val SpotifyGreen = Color(0xFF1ED760)

val SonexaGradientBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF5935E5),
        Color(0xFF9825DD),
        Color(0xFFE534B2)
    )
)

val SonexaGlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x409825DD),
        Color(0x106B3CE9),
        Color.Transparent
    )
)
