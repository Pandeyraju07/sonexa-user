package com.sonexa.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SonexaPurpleLight,
    secondary = SonexaMagenta,
    background = SonexaBgDark,
    surface = SonexaCardDark,
    onPrimary = SonexaTextWhite,
    onSecondary = SonexaTextWhite,
    onBackground = SonexaTextWhite,
    onSurface = SonexaTextWhite
)

@Composable
fun SonexaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SonexaBgDark.toArgb()
            window.navigationBarColor = SonexaBgDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
