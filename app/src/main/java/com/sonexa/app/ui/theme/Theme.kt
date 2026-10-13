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
    primary = ZyneraPrimaryBright,
    secondary = ZyneraSecondary,
    background = ZyneraBg,
    surface = ZyneraSurface,
    onPrimary = ZyneraTextPrimary,
    onSecondary = ZyneraTextPrimary,
    onBackground = ZyneraTextPrimary,
    onSurface = ZyneraTextPrimary,
    outline = ZyneraBorder
)

@Composable
fun ZyneraTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ZyneraBg.toArgb()
            window.navigationBarColor = ZyneraBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

// Alias for existing callers
@Composable
fun SonexaTheme(
    content: @Composable () -> Unit
) {
    ZyneraTheme(content = content)
}
